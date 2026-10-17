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
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;

public final class RC2Cipher extends CipherSpi {
  private final RC2Crypt crypt = new RC2Crypt();
  private final BlockCipherCore core = new BlockCipherCore(crypt, "RC2");

  public RC2Cipher() {
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    core.setMode(mode);
  }

  @Override
  protected void engineSetPadding(String padding) throws NoSuchPaddingException {
    core.setPadding(padding);
  }

  @Override
  protected int engineGetBlockSize() {
    return 8;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    return core.getOutputSize(inputLen);
  }

  @Override
  protected byte[] engineGetIV() {
    return core.getIV();
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return core.getParameters("RC2");
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
    byte[] keyBytes = CipherKeys.rawKeyBytes(key);
    crypt.setEffectiveBits(0);
    if (params instanceof RC2ParameterSpec) {
      RC2ParameterSpec rc2 = (RC2ParameterSpec) params;
      crypt.setEffectiveBits(rc2.getEffectiveKeyBits());
      params = rc2.getIV() == null ? null : new IvParameterSpec(rc2.getIV());
    }
    core.init(opmode, keyBytes, key.getAlgorithm(), params, random);
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    AlgorithmParameterSpec spec = null;
    if (params != null) {
      try {
        spec = params.getParameterSpec(RC2ParameterSpec.class);
      } catch (InvalidParameterSpecException ipse) {
        throw new InvalidAlgorithmParameterException("Wrong parameter type: IV expected");
      }
    }
    engineInit(opmode, key, spec, random);
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    return core.update(input, inputOffset, inputLen);
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    return core.update(input, inputOffset, inputLen, output, outputOffset);
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    return core.doFinal(input, inputOffset, inputLen);
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    return core.doFinal(input, inputOffset, inputLen, output, outputOffset);
  }

  @Override
  protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    return core.wrap(key);
  }

  @Override
  protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    return core.unwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    return CipherKeys.rawKeyBytes(key).length * 8;
  }
}
