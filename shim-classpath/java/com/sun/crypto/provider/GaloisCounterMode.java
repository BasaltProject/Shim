/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;

abstract class GaloisCounterMode extends CipherSpi {
  private static final int kBlock = 16;
  private static final int kDefaultIvLen = 12;
  private static final int kDefaultTagBits = 128;

  private final AESCrypt aes = new AESCrypt();
  private final int fixedKeySize;

  private boolean initialized;
  private boolean encrypting;
  private boolean requireReinit;
  private byte[] lastKey;
  private byte[] lastIv;
  private byte[] ivBytes;
  private int tagLenBytes = kDefaultTagBits / 8;
  private byte[] keyBytes;

  private long hH;
  private long hL;
  private long[] hhTable;
  private long[] hlTable;
  private byte[] j0;
  private byte[] counter;
  private byte[] keystream;
  private int keystreamUsed;

  private long ghHi;
  private long ghLo;
  private long aadLen;
  private long dataLen;
  private ByteArrayOutputStream aad = new ByteArrayOutputStream();
  private boolean dataStarted;
  private final byte[] partial = new byte[kBlock];
  private int partialLen;
  private ByteArrayOutputStream decBuffer = new ByteArrayOutputStream();

  protected GaloisCounterMode(int keySize) {
    this.fixedKeySize = keySize;
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    if (!mode.equalsIgnoreCase("GCM")) {
      throw new NoSuchAlgorithmException("Mode must be GCM");
    }
  }

  @Override
  protected void engineSetPadding(String padding) throws NoSuchPaddingException {
    if (!padding.equalsIgnoreCase("NoPadding")) {
      throw new NoSuchPaddingException("Padding must be NoPadding");
    }
  }

  @Override
  protected int engineGetBlockSize() {
    return kBlock;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    int total = (encrypting ? partialLen : decBuffer.size()) + inputLen;
    if (encrypting) {
      return total + tagLenBytes;
    }
    return Math.max(0, total - tagLenBytes);
  }

  @Override
  protected byte[] engineGetIV() {
    return ivBytes == null ? null : ivBytes.clone();
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    byte[] useIv = ivBytes;
    if (useIv == null) {
      useIv = new byte[kDefaultIvLen];
      new SecureRandom().nextBytes(useIv);
    }
    try {
      AlgorithmParameters params = AlgorithmParameters.getInstance("GCM", "SunJCE");
      params.init(new GCMParameterSpec(tagLenBytes * 8, useIv));
      return params;
    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException e) {
      throw new ProviderException("Cannot find GCM AlgorithmParameters implementation in SunJCE provider");
    }
  }

  @Override
  protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
    try {
      engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
    } catch (InvalidAlgorithmParameterException e) {
      throw new InvalidKeyException(e.getMessage());
    }
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    boolean enc = opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE;
    byte[] kb = CipherKeys.rawKeyBytes(key);
    if (fixedKeySize != -1 && fixedKeySize != kb.length) {
      throw new InvalidKeyException("The key must be " + fixedKeySize + " bytes");
    }
    if (!AESCrypt.isKeySizeValid(kb.length)) {
      throw new InvalidKeyException("Invalid AES key length: " + kb.length + " bytes");
    }
    byte[] iv;
    int tagBits = kDefaultTagBits;
    if (params == null) {
      if (!enc) {
        throw new InvalidKeyException("No GCMParameterSpec specified");
      }
      iv = new byte[kDefaultIvLen];
      (random == null ? new SecureRandom() : random).nextBytes(iv);
    } else {
      if (!(params instanceof GCMParameterSpec)) {
        throw new InvalidAlgorithmParameterException("AlgorithmParameterSpec not of GCMParameterSpec");
      }
      GCMParameterSpec spec = (GCMParameterSpec) params;
      tagBits = spec.getTLen();
      if (tagBits < 96 || tagBits > 128 || ((tagBits & 0x07) != 0)) {
        throw new InvalidAlgorithmParameterException(
            "Unsupported TLen value.  Must be one of {128, 120, 112, 104, 96}");
      }
      iv = spec.getIV();
      if (iv == null) {
        throw new InvalidAlgorithmParameterException("IV is null");
      }
      if (iv.length == 0) {
        throw new InvalidAlgorithmParameterException("IV is empty");
      }
    }
    if (enc && lastKey != null && Arrays.equals(lastKey, kb) && Arrays.equals(lastIv, iv)) {
      throw new InvalidAlgorithmParameterException("Cannot reuse iv for GCM encryption");
    }
    aes.init(false, "AES", kb);
    this.keyBytes = kb;
    this.encrypting = enc;
    this.ivBytes = iv.clone();
    this.tagLenBytes = tagBits / 8;
    if (enc) {
      lastKey = kb.clone();
      lastIv = iv.clone();
    }
    requireReinit = false;
    initialized = true;
    reset();
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    GCMParameterSpec spec = null;
    if (params != null) {
      try {
        spec = params.getParameterSpec(GCMParameterSpec.class);
      } catch (InvalidParameterSpecException e) {
        throw new InvalidAlgorithmParameterException("Wrong parameter type: GCM expected");
      }
    }
    engineInit(opmode, key, spec, random);
  }

  private void reset() {
    byte[] zero = new byte[kBlock];
    byte[] h = new byte[kBlock];
    aes.encryptBlock(zero, 0, h, 0);
    hH = getLong(h, 0);
    hL = getLong(h, 8);
    buildTable();
    if (ivBytes.length == 12) {
      j0 = new byte[kBlock];
      System.arraycopy(ivBytes, 0, j0, 0, 12);
      j0[15] = 1;
    } else {
      ghHi = 0;
      ghLo = 0;
      ghashBytes(ivBytes, 0, ivBytes.length);
      byte[] lenBlock = new byte[kBlock];
      putLong(lenBlock, 8, (long) ivBytes.length * 8);
      ghashBlock(lenBlock, 0);
      j0 = new byte[kBlock];
      putLong(j0, 0, ghHi);
      putLong(j0, 8, ghLo);
    }
    counter = j0.clone();
    incrementCounter(counter);
    keystream = new byte[kBlock];
    keystreamUsed = kBlock;
    ghHi = 0;
    ghLo = 0;
    aadLen = 0;
    dataLen = 0;
    aad = new ByteArrayOutputStream();
    dataStarted = false;
    partialLen = 0;
    decBuffer = new ByteArrayOutputStream();
  }

  private void checkReinit() {
    if (requireReinit) {
      throw new IllegalStateException("Must use either different key or  iv for GCM encryption");
    }
    if (!initialized) {
      throw new IllegalStateException("Cipher not initialized");
    }
  }

  private static void incrementCounter(byte[] c) {
    for (int i = 15; i >= 12; i--) {
      if (++c[i] != 0) {
        break;
      }
    }
  }

  private void buildTable() {
    hhTable = new long[16];
    hlTable = new long[16];
    long vh = hH;
    long vl = hL;
    hlTable[8] = vl;
    hhTable[8] = vh;
    for (int i = 4; i > 0; i >>= 1) {
      long t = (vl & 1L) * 0xe1000000L;
      vl = (vh << 63) | (vl >>> 1);
      vh = (vh >>> 1) ^ (t << 32);
      hlTable[i] = vl;
      hhTable[i] = vh;
    }
    for (int i = 2; i < 16; i <<= 1) {
      long bh = hhTable[i];
      long bl = hlTable[i];
      for (int j = 1; j < i; j++) {
        hhTable[i + j] = bh ^ hhTable[j];
        hlTable[i + j] = bl ^ hlTable[j];
      }
    }
  }

  private static final long[] kLast4 = {
    0x0000, 0x1c20, 0x3840, 0x2460, 0x7080, 0x6ca0, 0x48c0, 0x54e0, 0xe100, 0xfd20, 0xd940, 0xc560, 0x9180, 0x8da0,
    0xa9c0, 0xb5e0,
  };

  private void ghashBlock(byte[] block, int off) {
    long xh = ghHi ^ getLong(block, off);
    long xl = ghLo ^ getLong(block, off + 8);
    long zh;
    long zl;
    int lo = (int) (xl & 0xf);
    zh = hhTable[lo];
    zl = hlTable[lo];
    for (int i = 15; i >= 0; i--) {
      long word = i >= 8 ? xl : xh;
      int shift = (15 - i) % 8 * 8;
      int b = (int) ((word >>> shift) & 0xff);
      lo = b & 0xf;
      int hi = (b >>> 4) & 0xf;
      if (i != 15) {
        int rem = (int) (zl & 0xf);
        zl = (zh << 60) | (zl >>> 4);
        zh = zh >>> 4;
        zh ^= kLast4[rem] << 48;
        zh ^= hhTable[lo];
        zl ^= hlTable[lo];
      }
      int rem = (int) (zl & 0xf);
      zl = (zh << 60) | (zl >>> 4);
      zh = zh >>> 4;
      zh ^= kLast4[rem] << 48;
      zh ^= hhTable[hi];
      zl ^= hlTable[hi];
    }
    ghHi = zh;
    ghLo = zl;
  }

  private void ghashBytes(byte[] data, int off, int len) {
    int i = 0;
    while (len - i >= kBlock) {
      ghashBlock(data, off + i);
      i += kBlock;
    }
    if (i < len) {
      byte[] last = new byte[kBlock];
      System.arraycopy(data, off + i, last, 0, len - i);
      ghashBlock(last, 0);
    }
  }

  private static long getLong(byte[] b, int o) {
    long v = 0;
    for (int i = 0; i < 8; i++) {
      v = (v << 8) | (b[o + i] & 0xffL);
    }
    return v;
  }

  private static void putLong(byte[] b, int o, long v) {
    for (int i = 7; i >= 0; i--) {
      b[o + i] = (byte) v;
      v >>>= 8;
    }
  }

  private void startData() {
    if (dataStarted) {
      return;
    }
    dataStarted = true;
    byte[] a = aad.toByteArray();
    aadLen = a.length;
    if (a.length > 0) {
      ghashBytes(a, 0, a.length);
    }
  }

  private byte[] ctr(byte[] in, int off, int len) {
    byte[] out = new byte[len];
    for (int i = 0; i < len; i++) {
      if (keystreamUsed >= kBlock) {
        aes.encryptBlock(counter, 0, keystream, 0);
        incrementCounter(counter);
        keystreamUsed = 0;
      }
      out[i] = (byte) (in[off + i] ^ keystream[keystreamUsed++]);
    }
    return out;
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    try {
      byte[] out = new byte[engineGetOutputSize(inputLen)];
      int n = engineUpdate(input, inputOffset, inputLen, out, 0);
      return n == out.length ? out : Arrays.copyOf(out, n);
    } catch (ShortBufferException e) {
      throw new ProviderException("Unexpected exception", e);
    }
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    checkReinit();
    if (!encrypting) {
      decBuffer.write(input, inputOffset, inputLen);
      dataStarted = true;
      return 0;
    }
    int total = partialLen + inputLen;
    int emit = total - (total % kBlock);
    if (output == null || output.length - outputOffset < emit) {
      throw new ShortBufferException("Output buffer invalid");
    }
    startData();
    byte[] all = new byte[total];
    System.arraycopy(partial, 0, all, 0, partialLen);
    System.arraycopy(input, inputOffset, all, partialLen, inputLen);
    byte[] ct = ctr(all, 0, emit);
    for (int i = 0; i < emit; i += kBlock) {
      ghashBlock(ct, i);
    }
    dataLen += emit;
    System.arraycopy(ct, 0, output, outputOffset, emit);
    partialLen = total - emit;
    System.arraycopy(all, emit, partial, 0, partialLen);
    return emit;
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    try {
      byte[] out = new byte[engineGetOutputSize(inputLen)];
      int n = engineDoFinal(input, inputOffset, inputLen, out, 0);
      return n == out.length ? out : Arrays.copyOf(out, n);
    } catch (ShortBufferException e) {
      throw new ProviderException("Unexpected exception", e);
    }
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    checkReinit();
    int needed = engineGetOutputSize(inputLen);
    if (output == null || output.length - outputOffset < needed) {
      throw new ShortBufferException("Output buffer invalid");
    }
    startData();
    if (encrypting) {
      int total = partialLen + inputLen;
      byte[] all = new byte[total];
      System.arraycopy(partial, 0, all, 0, partialLen);
      if (inputLen > 0) {
        System.arraycopy(input, inputOffset, all, partialLen, inputLen);
      }
      byte[] ct = ctr(all, 0, total);
      for (int i = 0; i < total; i += kBlock) {
        int n = Math.min(kBlock, total - i);
        byte[] blk = new byte[kBlock];
        System.arraycopy(ct, i, blk, 0, n);
        ghashBlock(blk, 0);
      }
      dataLen += total;
      byte[] tag = computeTag();
      System.arraycopy(ct, 0, output, outputOffset, total);
      System.arraycopy(tag, 0, output, outputOffset + total, tagLenBytes);
      requireReinit = true;
      return total + tagLenBytes;
    }
    byte[] data;
    if (inputLen > 0) {
      decBuffer.write(input, inputOffset, inputLen);
    }
    data = decBuffer.toByteArray();
    if (data.length < tagLenBytes) {
      reset();
      throw new AEADBadTagException("Input data too short to contain an expected tag length of " + tagLenBytes
          + "bytes");
    }
    int ctLen = data.length - tagLenBytes;
    for (int i = 0; i < ctLen; i += kBlock) {
      int n = Math.min(kBlock, ctLen - i);
      byte[] blk = new byte[kBlock];
      System.arraycopy(data, i, blk, 0, n);
      ghashBlock(blk, 0);
    }
    dataLen = ctLen;
    byte[] expect = computeTag();
    int diff = 0;
    for (int i = 0; i < tagLenBytes; i++) {
      diff |= expect[i] ^ data[ctLen + i];
    }
    if (diff != 0) {
      reset();
      throw new AEADBadTagException("Tag mismatch");
    }
    byte[] pt = ctr(data, 0, ctLen);
    System.arraycopy(pt, 0, output, outputOffset, ctLen);
    reset();
    return ctLen;
  }

  private byte[] computeTag() {
    byte[] lenBlock = new byte[kBlock];
    putLong(lenBlock, 0, aadLen * 8);
    putLong(lenBlock, 8, dataLen * 8);
    ghashBlock(lenBlock, 0);
    byte[] s = new byte[kBlock];
    putLong(s, 0, ghHi);
    putLong(s, 8, ghLo);
    byte[] ek = new byte[kBlock];
    aes.encryptBlock(j0, 0, ek, 0);
    for (int i = 0; i < kBlock; i++) {
      s[i] ^= ek[i];
    }
    return s;
  }

  @Override
  protected void engineUpdateAAD(byte[] src, int offset, int len) {
    checkReinit();
    if (dataStarted) {
      throw new IllegalStateException("Update has been called; no more AAD data");
    }
    aad.write(src, offset, len);
  }

  @Override
  protected void engineUpdateAAD(ByteBuffer src) {
    byte[] data = new byte[src.remaining()];
    src.get(data);
    engineUpdateAAD(data, 0, data.length);
  }

  @Override
  protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    byte[] encoded = key.getEncoded();
    if ((encoded == null) || (encoded.length == 0)) {
      throw new InvalidKeyException("Cannot get an encoding of the key to be wrapped");
    }
    try {
      return engineDoFinal(encoded, 0, encoded.length);
    } catch (BadPaddingException e) {
      throw new InvalidKeyException("Wrapping failed", e);
    }
  }

  @Override
  protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] encoded;
    try {
      encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
    } catch (BadPaddingException e) {
      throw new InvalidKeyException("The wrapped key is not padded correctly");
    } catch (IllegalBlockSizeException e) {
      throw new InvalidKeyException("The wrapped key does not have the correct length");
    }
    return ConstructKeys.constructKey(encoded, wrappedKeyAlgorithm, wrappedKeyType);
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    byte[] encoded = key.getEncoded();
    if (!AESCrypt.isKeySizeValid(encoded.length)) {
      throw new InvalidKeyException("Invalid key length: " + encoded.length + " bytes");
    }
    return encoded.length * 8;
  }

  public static final class AESGCM extends GaloisCounterMode {
    public AESGCM() {
      super(-1);
    }
  }

  public static final class AES128 extends GaloisCounterMode {
    public AES128() {
      super(16);
    }
  }

  public static final class AES192 extends GaloisCounterMode {
    public AES192() {
      super(24);
    }
  }

  public static final class AES256 extends GaloisCounterMode {
    public AES256() {
      super(32);
    }
  }
}
