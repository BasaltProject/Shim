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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class DESedeWrapCipher extends CipherSpi {
  private static final byte[] kFixedIv = {
      (byte) 0x4a, (byte) 0xdd, (byte) 0xa2, (byte) 0x2c, (byte) 0x79, (byte) 0xe8, (byte) 0x21, (byte) 0x05};

  private SecretKey key;
  private int opmode;
  private byte[] iv;
  private SecureRandom random;

  @Override
  protected void engineSetMode(String mode) throws java.security.NoSuchAlgorithmException {
    if (!mode.equalsIgnoreCase("CBC")) {
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
    return opmode == Cipher.WRAP_MODE ? 8 + ((inputLen + 8 + 7) / 8) * 8 : inputLen - 16;
  }

  @Override
  protected byte[] engineGetIV() {
    return iv == null ? null : iv.clone();
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return null;
  }

  @Override
  protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
    init(opmode, key, null, random);
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    byte[] given = null;
    if (params != null) {
      if (!(params instanceof IvParameterSpec)) {
        throw new InvalidAlgorithmParameterException("Wrong parameter type: IV expected");
      }
      given = ((IvParameterSpec) params).getIV();
      if (given.length != 8) {
        throw new InvalidAlgorithmParameterException("Wrong IV length: must be 8 bytes long");
      }
    }
    init(opmode, key, given, random);
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("Cipher parameters not supported");
    }
    init(opmode, key, null, random);
  }

  private void init(int opmode, Key key, byte[] given, SecureRandom random) throws InvalidKeyException {
    if (opmode != Cipher.WRAP_MODE && opmode != Cipher.UNWRAP_MODE) {
      throw new UnsupportedOperationException("This cipher can only be used for key wrapping and unwrapping");
    }
    if (!(key instanceof SecretKey) || !key.getAlgorithm().equalsIgnoreCase("DESede")
        || key.getEncoded() == null || key.getEncoded().length != 24) {
      throw new InvalidKeyException("Invalid key length: " + (key == null || key.getEncoded() == null ? 0
          : key.getEncoded().length) + " bytes");
    }
    this.opmode = opmode;
    this.key = (SecretKey) key;
    this.random = random != null ? random : new SecureRandom();
    this.iv = null;
    if (given != null) {
      this.iv = given;
    }
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    throw new IllegalStateException("Cipher has not been initialized for encryption or decryption");
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    throw new IllegalStateException("Cipher has not been initialized for encryption or decryption");
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    throw new IllegalStateException("Cipher has not been initialized for encryption or decryption");
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    throw new IllegalStateException("Cipher has not been initialized for encryption or decryption");
  }

  private byte[] cbc(int mode, byte[] ivBytes, byte[] data) throws java.security.GeneralSecurityException {
    Cipher c = Cipher.getInstance("DESede/CBC/NoPadding");
    c.init(mode, new SecretKeySpec(key.getEncoded(), "DESede"), new IvParameterSpec(ivBytes));
    return c.doFinal(data);
  }

  private static byte[] reverse(byte[] in) {
    byte[] out = new byte[in.length];
    for (int i = 0; i < in.length; i++) {
      out[i] = in[in.length - 1 - i];
    }
    return out;
  }

  @Override
  protected byte[] engineWrap(Key toWrap) throws IllegalBlockSizeException, InvalidKeyException {
    if (opmode != Cipher.WRAP_MODE) {
      throw new IllegalStateException("Cipher not initialized for wrapping keys");
    }
    byte[] cek = toWrap.getEncoded();
    if (cek == null || cek.length == 0) {
      throw new InvalidKeyException("Cannot get an encoding of key to be wrapped");
    }
    try {
      byte[] icv = Arrays.copyOf(MessageDigest.getInstance("SHA-1").digest(cek), 8);
      byte[] wkcks = Arrays.copyOf(cek, cek.length + 8);
      System.arraycopy(icv, 0, wkcks, cek.length, 8);
      if (wkcks.length % 8 != 0) {
        throw new IllegalBlockSizeException("Key length must be a multiple of 8 bytes");
      }
      byte[] ivBytes = iv != null ? iv : new byte[8];
      if (iv == null) {
        random.nextBytes(ivBytes);
      }
      byte[] temp1 = cbc(Cipher.ENCRYPT_MODE, ivBytes, wkcks);
      byte[] temp2 = new byte[8 + temp1.length];
      System.arraycopy(ivBytes, 0, temp2, 0, 8);
      System.arraycopy(temp1, 0, temp2, 8, temp1.length);
      return cbc(Cipher.ENCRYPT_MODE, kFixedIv, reverse(temp2));
    } catch (java.security.GeneralSecurityException e) {
      if (e instanceof IllegalBlockSizeException) {
        throw (IllegalBlockSizeException) e;
      }
      throw new InvalidKeyException(e.getMessage(), e);
    }
  }

  @Override
  protected Key engineUnwrap(byte[] wrapped, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    if (opmode != Cipher.UNWRAP_MODE) {
      throw new IllegalStateException("Cipher not initialized for unwrapping keys");
    }
    if (wrapped.length == 0 || wrapped.length % 8 != 0 || wrapped.length < 24) {
      throw new InvalidKeyException("The wrapped key is not padded correctly");
    }
    try {
      byte[] temp2 = reverse(cbc(Cipher.DECRYPT_MODE, kFixedIv, wrapped));
      byte[] ivBytes = Arrays.copyOf(temp2, 8);
      byte[] wkcks = cbc(Cipher.DECRYPT_MODE, ivBytes, Arrays.copyOfRange(temp2, 8, temp2.length));
      byte[] cek = Arrays.copyOf(wkcks, wkcks.length - 8);
      byte[] icv = Arrays.copyOfRange(wkcks, wkcks.length - 8, wkcks.length);
      if (!MessageDigest.isEqual(icv, Arrays.copyOf(MessageDigest.getInstance("SHA-1").digest(cek), 8))) {
        throw new InvalidKeyException("Checksum comparison failed");
      }
      return ConstructKeys.constructKey(cek, wrappedKeyAlgorithm, wrappedKeyType);
    } catch (java.security.GeneralSecurityException e) {
      if (e instanceof InvalidKeyException) {
        throw (InvalidKeyException) e;
      }
      throw new InvalidKeyException(e.getMessage(), e);
    }
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    return 168;
  }
}
