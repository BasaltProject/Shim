/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

abstract class PBES2Core extends CipherSpi {
  private static final int kDefaultSaltLength = 20;
  private static final int kDefaultCount = 4096;

  private final String algorithm;
  private final String prf;
  private final int keyLength;
  private final Cipher cipher;
  private byte[] salt;
  private int iterCount;
  private byte[] iv;

  PBES2Core(String algorithm, String prf, int keyLength) {
    this.algorithm = algorithm;
    this.prf = prf;
    this.keyLength = keyLength;
    try {
      this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    if (mode == null || !mode.equalsIgnoreCase("CBC")) {
      throw new NoSuchAlgorithmException("Invalid cipher mode: " + mode);
    }
  }

  @Override
  protected void engineSetPadding(String paddingScheme) throws NoSuchPaddingException {
    if (paddingScheme == null || !paddingScheme.equalsIgnoreCase("PKCS5Padding")) {
      throw new NoSuchPaddingException("Invalid padding scheme: " + paddingScheme);
    }
  }

  @Override
  protected int engineGetBlockSize() {
    return 16;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    return cipher.getOutputSize(inputLen);
  }

  @Override
  protected byte[] engineGetIV() {
    return iv == null ? null : iv.clone();
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    if (salt == null) {
      return null;
    }
    try {
      AlgorithmParameters params = AlgorithmParameters.getInstance(algorithm, "SunJCE");
      params.init(new PBEParameterSpec(salt.clone(), iterCount, new IvParameterSpec(iv.clone())));
      return params;
    } catch (NoSuchAlgorithmException | java.security.NoSuchProviderException | InvalidParameterSpecException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
    try {
      engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
    } catch (InvalidAlgorithmParameterException e) {
      InvalidKeyException ike = new InvalidKeyException("requires PBE parameters");
      ike.initCause(e);
      throw ike;
    }
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    if (key == null || !key.getAlgorithm().regionMatches(true, 0, "PBE", 0, 3)) {
      throw new InvalidKeyException("Missing password");
    }
    byte[] passwordBytes = key.getEncoded();
    if (passwordBytes == null) {
      throw new InvalidKeyException("Missing password");
    }
    boolean encrypt = opmode == Cipher.ENCRYPT_MODE || opmode == Cipher.WRAP_MODE;
    byte[] newSalt;
    int newCount;
    byte[] newIv = null;
    if (params == null) {
      if (!encrypt) {
        throw new InvalidAlgorithmParameterException("Parameters missing");
      }
      newSalt = new byte[kDefaultSaltLength];
      (random != null ? random : new SecureRandom()).nextBytes(newSalt);
      newCount = kDefaultCount;
    } else {
      if (!(params instanceof PBEParameterSpec)) {
        throw new InvalidAlgorithmParameterException("Wrong parameter type: PBE expected");
      }
      PBEParameterSpec pbe = (PBEParameterSpec) params;
      newSalt = pbe.getSalt().clone();
      newCount = pbe.getIterationCount();
      AlgorithmParameterSpec inner = pbe.getParameterSpec();
      if (inner instanceof IvParameterSpec) {
        newIv = ((IvParameterSpec) inner).getIV().clone();
      } else if (inner != null || !encrypt) {
        throw new InvalidAlgorithmParameterException("Wrong parameter type: IvParameterSpec expected");
      }
    }
    if (newIv == null) {
      newIv = new byte[16];
      (random != null ? random : new SecureRandom()).nextBytes(newIv);
    }
    SecretKey derived;
    try {
      SecretKeyFactory kdf = SecretKeyFactory.getInstance("PBKDF2With" + prf);
      PBEKeySpec spec = new PBEKeySpec(new String(passwordBytes, StandardCharsets.UTF_8).toCharArray(), newSalt,
          newCount, keyLength * 8);
      derived = new SecretKeySpec(kdf.generateSecret(spec).getEncoded(), "AES");
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new InvalidKeyException("Cannot derive the encryption key", e);
    }
    cipher.init(opmode, derived, new IvParameterSpec(newIv), random);
    this.salt = newSalt;
    this.iterCount = newCount;
    this.iv = newIv;
  }

  @Override
  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    PBEParameterSpec spec = null;
    if (params != null) {
      try {
        spec = params.getParameterSpec(PBEParameterSpec.class);
      } catch (InvalidParameterSpecException e) {
        throw new InvalidAlgorithmParameterException("Wrong parameter type: PBE expected");
      }
    }
    engineInit(opmode, key, spec, random);
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    return cipher.update(input, inputOffset, inputLen);
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    return cipher.update(input, inputOffset, inputLen, output, outputOffset);
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
      throws IllegalBlockSizeException, BadPaddingException {
    return cipher.doFinal(input, inputOffset, inputLen);
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    return cipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
  }

  @Override
  protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    return cipher.wrap(key);
  }

  @Override
  protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    return cipher.unwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
  }

  @Override
  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    return keyLength * 8;
  }

  public static final class HmacSHA1AndAES_128 extends PBES2Core {
    public HmacSHA1AndAES_128() {
      super("PBEWithHmacSHA1AndAES_128", "HmacSHA1", 16);
    }
  }

  public static final class HmacSHA1AndAES_256 extends PBES2Core {
    public HmacSHA1AndAES_256() {
      super("PBEWithHmacSHA1AndAES_256", "HmacSHA1", 32);
    }
  }

  public static final class HmacSHA224AndAES_128 extends PBES2Core {
    public HmacSHA224AndAES_128() {
      super("PBEWithHmacSHA224AndAES_128", "HmacSHA224", 16);
    }
  }

  public static final class HmacSHA224AndAES_256 extends PBES2Core {
    public HmacSHA224AndAES_256() {
      super("PBEWithHmacSHA224AndAES_256", "HmacSHA224", 32);
    }
  }

  public static final class HmacSHA256AndAES_128 extends PBES2Core {
    public HmacSHA256AndAES_128() {
      super("PBEWithHmacSHA256AndAES_128", "HmacSHA256", 16);
    }
  }

  public static final class HmacSHA256AndAES_256 extends PBES2Core {
    public HmacSHA256AndAES_256() {
      super("PBEWithHmacSHA256AndAES_256", "HmacSHA256", 32);
    }
  }

  public static final class HmacSHA384AndAES_128 extends PBES2Core {
    public HmacSHA384AndAES_128() {
      super("PBEWithHmacSHA384AndAES_128", "HmacSHA384", 16);
    }
  }

  public static final class HmacSHA384AndAES_256 extends PBES2Core {
    public HmacSHA384AndAES_256() {
      super("PBEWithHmacSHA384AndAES_256", "HmacSHA384", 32);
    }
  }

  public static final class HmacSHA512AndAES_128 extends PBES2Core {
    public HmacSHA512AndAES_128() {
      super("PBEWithHmacSHA512AndAES_128", "HmacSHA512", 16);
    }
  }

  public static final class HmacSHA512AndAES_256 extends PBES2Core {
    public HmacSHA512AndAES_256() {
      super("PBEWithHmacSHA512AndAES_256", "HmacSHA512", 32);
    }
  }

  public static final class HmacSHA512_224AndAES_128 extends PBES2Core {
    public HmacSHA512_224AndAES_128() {
      super("PBEWithHmacSHA512/224AndAES_128", "HmacSHA512/224", 16);
    }
  }

  public static final class HmacSHA512_224AndAES_256 extends PBES2Core {
    public HmacSHA512_224AndAES_256() {
      super("PBEWithHmacSHA512/224AndAES_256", "HmacSHA512/224", 32);
    }
  }

  public static final class HmacSHA512_256AndAES_128 extends PBES2Core {
    public HmacSHA512_256AndAES_128() {
      super("PBEWithHmacSHA512/256AndAES_128", "HmacSHA512/256", 16);
    }
  }

  public static final class HmacSHA512_256AndAES_256 extends PBES2Core {
    public HmacSHA512_256AndAES_256() {
      super("PBEWithHmacSHA512/256AndAES_256", "HmacSHA512/256", 32);
    }
  }
}
