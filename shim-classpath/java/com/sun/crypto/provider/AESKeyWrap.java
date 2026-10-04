/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.ByteArrayOutputStream;
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
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

public final class AESKeyWrap extends CipherSpi {
  private static final byte[] kDefaultIv = {
      (byte) 0xa6, (byte) 0xa6, (byte) 0xa6, (byte) 0xa6, (byte) 0xa6, (byte) 0xa6, (byte) 0xa6, (byte) 0xa6};

  private final AESCrypt aes = new AESCrypt();
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private boolean wrapping;
  private boolean initialized;
  private byte[] iv = kDefaultIv;

  public AESKeyWrap() {
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    if (!mode.equalsIgnoreCase("KW")) {
      throw new NoSuchAlgorithmException("Invalid cipher mode: " + mode);
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
    return 8;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    int total = inputLen + buffer.size();
    return wrapping ? total + 8 : Math.max(total - 8, 0);
  }

  @Override
  protected byte[] engineGetIV() {
    return iv.clone();
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return null;
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
    if (key == null || !"AES".equalsIgnoreCase(key.getAlgorithm())) {
      throw new InvalidKeyException("Invalid key algorithm");
    }
    byte[] k = key.getEncoded();
    if (k == null || (k.length != 16 && k.length != 24 && k.length != 32)) {
      throw new InvalidKeyException("Invalid key length: " + (k == null ? 0 : k.length) + " bytes");
    }
    if (params != null) {
      if (!(params instanceof IvParameterSpec) || ((IvParameterSpec) params).getIV().length != 8) {
        throw new InvalidAlgorithmParameterException("Only an IV of 8 bytes is supported");
      }
      iv = ((IvParameterSpec) params).getIV();
    } else {
      iv = kDefaultIv;
    }
    wrapping = opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE;
    aes.init(!wrapping, "AES", k);
    buffer.reset();
    initialized = true;
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("Parameters are not supported");
    }
    engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    if (!initialized) {
      throw new IllegalStateException("Cipher not initialized");
    }
    buffer.write(input, inputOffset, inputLen);
    return new byte[0];
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    engineUpdate(input, inputOffset, inputLen);
    return 0;
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    if (!initialized) {
      throw new IllegalStateException("Cipher not initialized");
    }
    if (inputLen > 0) {
      buffer.write(input, inputOffset, inputLen);
    }
    byte[] data = buffer.toByteArray();
    buffer.reset();
    return wrapping ? wrap(data) : unwrap(data);
  }

  private byte[] wrap(byte[] in) throws IllegalBlockSizeException {
    if (in.length % 8 != 0 || in.length < 16) {
      throw new IllegalBlockSizeException("data should be at least 16 bytes and multiples of 8");
    }
    int n = in.length / 8;
    byte[] a = iv.clone();
    byte[] r = in.clone();
    byte[] b = new byte[16];
    for (int j = 0; j < 6; j++) {
      for (int i = 0; i < n; i++) {
        System.arraycopy(a, 0, b, 0, 8);
        System.arraycopy(r, i * 8, b, 8, 8);
        byte[] enc = new byte[16];
        aes.encryptBlock(b, 0, enc, 0);
        System.arraycopy(enc, 0, b, 0, 16);
        long t = (long) n * j + i + 1;
        System.arraycopy(b, 0, a, 0, 8);
        for (int k = 0; k < 8; k++) {
          a[7 - k] ^= (byte) (t >>> (8 * k));
        }
        System.arraycopy(b, 8, r, i * 8, 8);
      }
    }
    byte[] out = new byte[in.length + 8];
    System.arraycopy(a, 0, out, 0, 8);
    System.arraycopy(r, 0, out, 8, r.length);
    return out;
  }

  private byte[] unwrap(byte[] in) throws IllegalBlockSizeException, BadPaddingException {
    if (in.length % 8 != 0 || in.length < 24) {
      throw new IllegalBlockSizeException("data should be at least 24 bytes and multiples of 8");
    }
    int n = in.length / 8 - 1;
    byte[] a = new byte[8];
    System.arraycopy(in, 0, a, 0, 8);
    byte[] r = new byte[n * 8];
    System.arraycopy(in, 8, r, 0, r.length);
    byte[] b = new byte[16];
    for (int j = 5; j >= 0; j--) {
      for (int i = n - 1; i >= 0; i--) {
        long t = (long) n * j + i + 1;
        System.arraycopy(a, 0, b, 0, 8);
        for (int k = 0; k < 8; k++) {
          b[7 - k] ^= (byte) (t >>> (8 * k));
        }
        System.arraycopy(r, i * 8, b, 8, 8);
        byte[] dec = new byte[16];
        aes.decryptBlock(b, 0, dec, 0);
        System.arraycopy(dec, 0, b, 0, 16);
        System.arraycopy(b, 0, a, 0, 8);
        System.arraycopy(b, 8, r, i * 8, 8);
      }
    }
    if (!java.security.MessageDigest.isEqual(a, iv)) {
      throw new BadPaddingException("Integrity check failed");
    }
    return r;
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
    return wrap(encoded);
  }

  @Override
  protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    try {
      return ConstructKeys.constructKey(unwrap(wrappedKey), wrappedKeyAlgorithm, wrappedKeyType);
    } catch (BadPaddingException | IllegalBlockSizeException e) {
      throw new InvalidKeyException("Unwrap failed", e);
    }
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    return key.getEncoded().length * 8;
  }
}
