/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;

abstract class ChaCha20Cipher extends CipherSpi {
  private static final int kTagLength = 16;

  private final boolean aead;
  private int opmode;
  private int[] state;
  private int counter;
  private byte[] block = new byte[64];
  private int blockPos = 64;
  private byte[] key;
  private byte[] nonce;
  private byte[] lastKey;
  private byte[] lastNonce;
  private boolean initialized;
  private ByteArrayOutputStream aad = new ByteArrayOutputStream();
  private ByteArrayOutputStream data = new ByteArrayOutputStream();
  private boolean dataStarted;
  private long aadLength;
  private long dataLength;

  ChaCha20Cipher(boolean aead) {
    this.aead = aead;
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    if (!mode.equalsIgnoreCase("None")) {
      throw new NoSuchAlgorithmException("Mode must be None");
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
    return 0;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    if (!aead) {
      return inputLen;
    }
    int total = inputLen + data.size();
    if (opmode == Cipher.ENCRYPT_MODE) {
      return total + kTagLength;
    }
    return Math.max(total - kTagLength, 0);
  }

  @Override
  protected byte[] engineGetIV() {
    return nonce == null ? null : nonce.clone();
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    if (nonce == null || !aead) {
      return null;
    }
    try {
      AlgorithmParameters p = AlgorithmParameters.getInstance("ChaCha20-Poly1305");
      p.init(new IvParameterSpec(nonce));
      return p;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private int counter0 = 1;

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
    if (opmode != Cipher.ENCRYPT_MODE && opmode != Cipher.DECRYPT_MODE && opmode != Cipher.WRAP_MODE
        && opmode != Cipher.UNWRAP_MODE) {
      throw new java.security.InvalidParameterException("Unsupported operation mode: " + opmode);
    }
    if (key == null || !key.getAlgorithm().equalsIgnoreCase("ChaCha20") || !"RAW".equals(key.getFormat())) {
      throw new InvalidKeyException("Key must be a 256-bit ChaCha20 secret key");
    }
    byte[] k = key.getEncoded();
    if (k == null || k.length != 32) {
      throw new InvalidKeyException("Key length must be 256 bits");
    }
    boolean encrypting = opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE;
    byte[] n;
    int c = 1;
    if (params == null) {
      if (!encrypting) {
        throw new InvalidKeyException("Default parameter generation disallowed in DECRYPT and UNWRAP modes");
      }
      n = new byte[12];
      (random == null ? new SecureRandom() : random).nextBytes(n);
    } else if (aead && params instanceof IvParameterSpec) {
      n = ((IvParameterSpec) params).getIV();
      if (n.length != 12) {
        throw new InvalidAlgorithmParameterException("ChaCha20-Poly1305 nonce must be 12 bytes in length");
      }
    } else if (!aead && params instanceof ChaCha20ParameterSpec) {
      n = ((ChaCha20ParameterSpec) params).getNonce();
      c = ((ChaCha20ParameterSpec) params).getCounter();
    } else {
      throw new InvalidAlgorithmParameterException("ChaCha20 algorithm requires ChaCha20ParameterSpec");
    }
    if (encrypting && lastKey != null && MessageDigest.isEqual(lastKey, k) && MessageDigest.isEqual(lastNonce, n)) {
      throw new InvalidKeyException("Matching key and nonce from previous initialization");
    }
    this.opmode = opmode;
    this.key = k;
    this.nonce = n;
    this.counter0 = c;
    if (encrypting) {
      lastKey = k.clone();
      lastNonce = n.clone();
    }
    state = new int[16];
    state[0] = 0x61707865;
    state[1] = 0x3320646e;
    state[2] = 0x79622d32;
    state[3] = 0x6b206574;
    for (int i = 0; i < 8; i++) {
      state[4 + i] = le32(k, i * 4);
    }
    for (int i = 0; i < 3; i++) {
      state[13 + i] = le32(n, i * 4);
    }
    counter = aead ? 1 : c;
    blockPos = 64;
    aad.reset();
    data.reset();
    dataStarted = false;
    aadLength = 0;
    dataLength = 0;
    initialized = true;
    if (aead) {
      polyKey = keyBlock(0);
    }
  }

  private byte[] polyKey;

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    AlgorithmParameterSpec spec = null;
    if (params != null) {
      try {
        spec = aead ? params.getParameterSpec(IvParameterSpec.class) : params.getParameterSpec(ChaCha20ParameterSpec.class);
      } catch (InvalidParameterSpecException e) {
        throw new InvalidAlgorithmParameterException(e);
      }
    }
    engineInit(opmode, key, spec, random);
  }

  private static int le32(byte[] b, int o) {
    return (b[o] & 0xff) | ((b[o + 1] & 0xff) << 8) | ((b[o + 2] & 0xff) << 16) | ((b[o + 3] & 0xff) << 24);
  }

  private static void quarter(int[] x, int a, int b, int c, int d) {
    x[a] += x[b];
    x[d] = Integer.rotateLeft(x[d] ^ x[a], 16);
    x[c] += x[d];
    x[b] = Integer.rotateLeft(x[b] ^ x[c], 12);
    x[a] += x[b];
    x[d] = Integer.rotateLeft(x[d] ^ x[a], 8);
    x[c] += x[d];
    x[b] = Integer.rotateLeft(x[b] ^ x[c], 7);
  }

  private byte[] keyBlock(int blockCounter) {
    int[] in = state.clone();
    in[12] = blockCounter;
    int[] x = in.clone();
    for (int i = 0; i < 10; i++) {
      quarter(x, 0, 4, 8, 12);
      quarter(x, 1, 5, 9, 13);
      quarter(x, 2, 6, 10, 14);
      quarter(x, 3, 7, 11, 15);
      quarter(x, 0, 5, 10, 15);
      quarter(x, 1, 6, 11, 12);
      quarter(x, 2, 7, 8, 13);
      quarter(x, 3, 4, 9, 14);
    }
    byte[] out = new byte[64];
    for (int i = 0; i < 16; i++) {
      int v = x[i] + in[i];
      out[i * 4] = (byte) v;
      out[i * 4 + 1] = (byte) (v >>> 8);
      out[i * 4 + 2] = (byte) (v >>> 16);
      out[i * 4 + 3] = (byte) (v >>> 24);
    }
    return out;
  }

  private byte[] crypt(byte[] in, int off, int len) {
    byte[] out = new byte[len];
    for (int i = 0; i < len; i++) {
      if (blockPos == 64) {
        block = keyBlock(counter++);
        blockPos = 0;
      }
      out[i] = (byte) (in[off + i] ^ block[blockPos++]);
    }
    return out;
  }

  private void checkInit() {
    if (!initialized) {
      throw new IllegalStateException("Cipher not initialized");
    }
  }

  @Override
  protected void engineUpdateAAD(byte[] src, int offset, int len) {
    checkInit();
    if (!aead) {
      throw new IllegalStateException("AAD is not supported for ChaCha20");
    }
    if (dataStarted) {
      throw new IllegalStateException("Update has been called; no more AAD data");
    }
    aad.write(src, offset, len);
    aadLength += len;
  }

  @Override
  protected void engineUpdateAAD(java.nio.ByteBuffer src) {
    byte[] b = new byte[src.remaining()];
    src.get(b);
    engineUpdateAAD(b, 0, b.length);
  }

  private static final byte[] kEmpty = new byte[0];

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    checkInit();
    if (inputLen == 0) {
      return kEmpty;
    }
    dataStarted = true;
    if (!aead) {
      return crypt(input, inputOffset, inputLen);
    }
    if (opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE) {
      byte[] ct = crypt(input, inputOffset, inputLen);
      data.write(ct, 0, ct.length);
      dataLength += ct.length;
      return ct;
    }
    data.write(input, inputOffset, inputLen);
    return kEmpty;
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    byte[] r = engineUpdate(input, inputOffset, inputLen);
    if (output.length - outputOffset < r.length) {
      throw new ShortBufferException("Output buffer too small");
    }
    System.arraycopy(r, 0, output, outputOffset, r.length);
    return r.length;
  }

  private byte[] tag() {
    byte[] a = aad.toByteArray();
    byte[] c = dataForTag;
    ByteArrayOutputStream m = new ByteArrayOutputStream();
    m.write(a, 0, a.length);
    for (int i = a.length % 16; i != 0 && i < 16; i++) {
      m.write(0);
    }
    m.write(c, 0, c.length);
    for (int i = c.length % 16; i != 0 && i < 16; i++) {
      m.write(0);
    }
    for (int i = 0; i < 8; i++) {
      m.write((int) ((long) a.length >>> (8 * i)));
    }
    for (int i = 0; i < 8; i++) {
      m.write((int) ((long) c.length >>> (8 * i)));
    }
    return poly1305(polyKey, m.toByteArray());
  }

  private byte[] dataForTag;

  private static byte[] poly1305(byte[] key, byte[] msg) {
    byte[] rb = new byte[16];
    for (int i = 0; i < 16; i++) {
      rb[15 - i] = key[i];
    }
    BigInteger r = new BigInteger(1, rb).and(new BigInteger("0ffffffc0ffffffc0ffffffc0fffffff", 16));
    byte[] sb = new byte[16];
    for (int i = 0; i < 16; i++) {
      sb[15 - i] = key[16 + i];
    }
    BigInteger s = new BigInteger(1, sb);
    BigInteger p = BigInteger.ONE.shiftLeft(130).subtract(BigInteger.valueOf(5));
    BigInteger acc = BigInteger.ZERO;
    for (int i = 0; i < msg.length; i += 16) {
      int n = Math.min(16, msg.length - i);
      byte[] chunk = new byte[n + 1];
      for (int j = 0; j < n; j++) {
        chunk[n - j] = msg[i + j];
      }
      chunk[0] = 1;
      acc = acc.add(new BigInteger(1, chunk)).multiply(r).mod(p);
    }
    acc = acc.add(s);
    byte[] out = new byte[16];
    for (int i = 0; i < 16; i++) {
      out[i] = (byte) acc.shiftRight(8 * i).intValue();
    }
    return out;
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    checkInit();
    try {
      if (!aead) {
        byte[] r = crypt(input == null ? kEmpty : input, inputOffset, input == null ? 0 : inputLen);
        reset();
        return r;
      }
      boolean encrypting = opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE;
      if (encrypting) {
        byte[] ct = inputLen == 0 ? kEmpty : crypt(input, inputOffset, inputLen);
        data.write(ct, 0, ct.length);
        dataForTag = data.toByteArray();
        byte[] t = tag();
        byte[] out = new byte[ct.length + kTagLength];
        System.arraycopy(ct, 0, out, 0, ct.length);
        System.arraycopy(t, 0, out, ct.length, kTagLength);
        reset();
        return out;
      }
      if (inputLen > 0) {
        data.write(input, inputOffset, inputLen);
      }
      byte[] all = data.toByteArray();
      if (all.length < kTagLength) {
        throw new AEADBadTagException("Input data too short to contain an expected tag length of 16bytes");
      }
      byte[] ct = new byte[all.length - kTagLength];
      System.arraycopy(all, 0, ct, 0, ct.length);
      dataForTag = ct;
      byte[] expected = tag();
      byte[] got = new byte[kTagLength];
      System.arraycopy(all, ct.length, got, 0, kTagLength);
      if (!MessageDigest.isEqual(expected, got)) {
        throw new AEADBadTagException("Tag mismatch");
      }
      byte[] pt = crypt(ct, 0, ct.length);
      reset();
      return pt;
    } catch (AEADBadTagException e) {
      reset();
      throw e;
    }
  }

  private void reset() {
    aad.reset();
    data.reset();
    dataStarted = false;
    aadLength = 0;
    dataLength = 0;
    counter = aead ? 1 : counter0;
    blockPos = 64;
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    byte[] r = engineDoFinal(input, inputOffset, inputLen);
    if (output.length - outputOffset < r.length) {
      throw new ShortBufferException("Output buffer too small");
    }
    System.arraycopy(r, 0, output, outputOffset, r.length);
    return r.length;
  }

  @Override
  protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    byte[] encoded = key.getEncoded();
    if (encoded == null) {
      throw new InvalidKeyException("Cannot get an encoding of the key to be wrapped");
    }
    try {
      return engineDoFinal(encoded, 0, encoded.length);
    } catch (BadPaddingException e) {
      throw new InvalidKeyException(e);
    }
  }

  @Override
  protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    try {
      byte[] raw = engineDoFinal(wrappedKey, 0, wrappedKey.length);
      return ConstructKeys.constructKey(raw, wrappedKeyAlgorithm, wrappedKeyType);
    } catch (BadPaddingException | IllegalBlockSizeException e) {
      throw new InvalidKeyException(e);
    }
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    return 256;
  }

  public static final class ChaCha20Only extends ChaCha20Cipher {
    public ChaCha20Only() {
      super(false);
    }
  }

  public static final class ChaCha20Poly1305 extends ChaCha20Cipher {
    public ChaCha20Poly1305() {
      super(true);
    }
  }
}
