/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public final class ARCFOURCipher extends CipherSpi {
  private final int[] state = new int[256];
  private int x;
  private int y;
  private byte[] lastKey;

  public ARCFOURCipher() {
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    if (!"ECB".equalsIgnoreCase(mode)) {
      throw new NoSuchAlgorithmException("Unsupported mode " + mode + ", only ECB mode supported");
    }
  }

  @Override
  protected void engineSetPadding(String padding) throws NoSuchPaddingException {
    if (!"NoPadding".equalsIgnoreCase(padding)) {
      throw new NoSuchPaddingException("Padding must be NoPadding");
    }
  }

  @Override
  protected int engineGetBlockSize() {
    return 0;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    return inputLen;
  }

  @Override
  protected byte[] engineGetIV() {
    return null;
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return null;
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    byte[] encoded = checkKey(key);
    return encoded.length * 8;
  }

  private static byte[] checkKey(Key key) throws InvalidKeyException {
    if (key == null || !(key instanceof SecretKey) || !"RAW".equals(key.getFormat())) {
      throw new InvalidKeyException("Key format must be RAW");
    }
    String algorithm = key.getAlgorithm();
    if (!"RC4".equalsIgnoreCase(algorithm) && !"ARCFOUR".equalsIgnoreCase(algorithm)) {
      throw new InvalidKeyException("Key algorithm must be RC4");
    }
    byte[] encoded = key.getEncoded();
    if (encoded == null || encoded.length < 5 || encoded.length > 128) {
      throw new InvalidKeyException("Key length must be between 40 and 1024 bit");
    }
    return encoded;
  }

  private void reset() {
    byte[] key = lastKey;
    for (int i = 0; i < 256; i++) {
      state[i] = i;
    }
    int j = 0;
    for (int i = 0; i < 256; i++) {
      j = (j + state[i] + (key[i % key.length] & 0xff)) & 0xff;
      int t = state[i];
      state[i] = state[j];
      state[j] = t;
    }
    x = 0;
    y = 0;
  }

  @Override
  protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
    lastKey = checkKey(key);
    reset();
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("Parameters not supported");
    }
    engineInit(opmode, key, random);
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("Parameters not supported");
    }
    engineInit(opmode, key, random);
  }

  private void crypt(byte[] in, int inOffset, int length, byte[] out, int outOffset) {
    for (int i = 0; i < length; i++) {
      x = (x + 1) & 0xff;
      y = (y + state[x]) & 0xff;
      int t = state[x];
      state[x] = state[y];
      state[y] = t;
      out[outOffset + i] = (byte) (in[inOffset + i] ^ state[(state[x] + state[y]) & 0xff]);
    }
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    byte[] out = new byte[inputLen];
    crypt(input, inputOffset, inputLen, out, 0);
    return out;
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    if (output.length - outputOffset < inputLen) {
      throw new ShortBufferException("Output buffer too small");
    }
    crypt(input, inputOffset, inputLen, output, outputOffset);
    return inputLen;
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) {
    byte[] out = engineUpdate(input, inputOffset, inputLen);
    reset();
    return out;
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    int n = engineUpdate(input, inputOffset, inputLen, output, outputOffset);
    reset();
    return n;
  }

  @Override
  protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    byte[] encoded = key.getEncoded();
    if (encoded == null || encoded.length == 0) {
      throw new InvalidKeyException("Cannot get an encoding of the key to be wrapped");
    }
    return engineDoFinal(encoded, 0, encoded.length);
  }

  @Override
  protected Key engineUnwrap(byte[] wrappedKey, String algorithm, int type)
      throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
    if (type == Cipher.SECRET_KEY) {
      return new SecretKeySpec(encoded, algorithm);
    }
    return ConstructKeys.constructKey(encoded, algorithm, type);
  }
}
