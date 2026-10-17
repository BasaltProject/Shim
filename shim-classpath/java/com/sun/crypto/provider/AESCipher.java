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
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

abstract class AESCipher extends CipherSpi {
  private final BlockCipherCore core;
  private final int fixedKeySize;

  protected AESCipher(int keySize) {
    core = new BlockCipherCore(new AESCrypt(), "AES");
    fixedKeySize = keySize;
  }

  protected AESCipher(int keySize, String mode, String padding) {
    this(keySize);
    try {
      core.setMode(mode);
      core.setPadding(padding);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new ProviderException("Internal Error", e);
    }
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    core.setMode(mode);
  }

  @Override
  protected void engineSetPadding(String paddingScheme) throws NoSuchPaddingException {
    core.setPadding(paddingScheme);
  }

  @Override
  protected int engineGetBlockSize() {
    return 16;
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
    return core.getParameters("AES");
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
    if (fixedKeySize != -1 && fixedKeySize != keyBytes.length) {
      throw new InvalidKeyException("The key must be " + fixedKeySize + " bytes");
    }
    core.init(opmode, keyBytes, key.getAlgorithm(), params, random);
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    AlgorithmParameterSpec spec = null;
    if (params != null) {
      try {
        spec = params.getParameterSpec(IvParameterSpec.class);
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
    byte[] encoded = key.getEncoded();
    if (!AESCrypt.isKeySizeValid(encoded.length)) {
      throw new InvalidKeyException("Invalid key length: " + encoded.length + " bytes");
    }
    return Math.multiplyExact(encoded.length, 8);
  }

  public static final class General extends AESCipher {
    public General() {
      super(-1);
    }
  }

  public static final class AES_CTR_NoPadding extends AESCipher {
    public AES_CTR_NoPadding() {
      super(-1, "CTR", "NoPadding");
    }
  }

  public static final class AES128_ECB_NoPadding extends AESCipher {
    public AES128_ECB_NoPadding() {
      super(16, "ECB", "NoPadding");
    }
  }

  public static final class AES128_CBC_NoPadding extends AESCipher {
    public AES128_CBC_NoPadding() {
      super(16, "CBC", "NoPadding");
    }
  }

  public static final class AES128_OFB_NoPadding extends AESCipher {
    public AES128_OFB_NoPadding() {
      super(16, "OFB", "NoPadding");
    }
  }

  public static final class AES128_CFB_NoPadding extends AESCipher {
    public AES128_CFB_NoPadding() {
      super(16, "CFB", "NoPadding");
    }
  }

  public static final class AES192_ECB_NoPadding extends AESCipher {
    public AES192_ECB_NoPadding() {
      super(24, "ECB", "NoPadding");
    }
  }

  public static final class AES192_CBC_NoPadding extends AESCipher {
    public AES192_CBC_NoPadding() {
      super(24, "CBC", "NoPadding");
    }
  }

  public static final class AES192_OFB_NoPadding extends AESCipher {
    public AES192_OFB_NoPadding() {
      super(24, "OFB", "NoPadding");
    }
  }

  public static final class AES192_CFB_NoPadding extends AESCipher {
    public AES192_CFB_NoPadding() {
      super(24, "CFB", "NoPadding");
    }
  }

  public static final class AES256_ECB_NoPadding extends AESCipher {
    public AES256_ECB_NoPadding() {
      super(32, "ECB", "NoPadding");
    }
  }

  public static final class AES256_CBC_NoPadding extends AESCipher {
    public AES256_CBC_NoPadding() {
      super(32, "CBC", "NoPadding");
    }
  }

  public static final class AES256_OFB_NoPadding extends AESCipher {
    public AES256_OFB_NoPadding() {
      super(32, "OFB", "NoPadding");
    }
  }

  public static final class AES256_CFB_NoPadding extends AESCipher {
    public AES256_CFB_NoPadding() {
      super(32, "CFB", "NoPadding");
    }
  }
}
