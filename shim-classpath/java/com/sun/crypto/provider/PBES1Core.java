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
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

abstract class PBES1Core extends CipherSpi {
  private static final int kDefaultCount = 1024;

  private final String algorithm;
  private final String transformation;
  private final String keyAlgorithm;
  private final int keyBytes;
  private final int rc2Bits;
  private final boolean pkcs12;
  private final boolean tripleDes;
  private final boolean stream;
  private Cipher cipher;
  private byte[] salt;
  private int iterationCount;
  private byte[] iv;

  PBES1Core(String algorithm, String transformation, String keyAlgorithm, int keyBytes, int rc2Bits, boolean pkcs12,
      boolean tripleDes) {
    this.algorithm = algorithm;
    this.transformation = transformation;
    this.keyAlgorithm = keyAlgorithm;
    this.keyBytes = keyBytes;
    this.rc2Bits = rc2Bits;
    this.pkcs12 = pkcs12;
    this.tripleDes = tripleDes;
    this.stream = transformation.startsWith("ARCFOUR");
  }

  @Override
  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
    String expected = stream ? "ECB" : "CBC";
    if (mode == null || !mode.equalsIgnoreCase(expected)) {
      throw new NoSuchAlgorithmException("Invalid cipher mode: " + mode);
    }
  }

  @Override
  protected void engineSetPadding(String padding) throws NoSuchPaddingException {
    String expected = stream ? "NoPadding" : "PKCS5Padding";
    if (padding == null || !padding.equalsIgnoreCase(expected)) {
      throw new NoSuchPaddingException("Invalid padding scheme: " + padding);
    }
  }

  @Override
  protected int engineGetBlockSize() {
    return stream ? 0 : 8;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    return cipher == null ? inputLen + (stream ? 0 : 8) : cipher.getOutputSize(inputLen);
  }

  @Override
  protected byte[] engineGetIV() {
    return iv == null ? null : iv.clone();
  }

  private int defaultSaltLength() {
    return pkcs12 ? 20 : 8;
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    if (salt == null) {
      salt = new byte[defaultSaltLength()];
      new SecureRandom().nextBytes(salt);
      iterationCount = kDefaultCount;
    }
    try {
      AlgorithmParameters params = AlgorithmParameters.getInstance(algorithm, "SunJCE");
      params.init(new PBEParameterSpec(salt.clone(), iterationCount));
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
    if (key == null || key.getEncoded() == null || !key.getAlgorithm().regionMatches(true, 0, "PBE", 0, 3)) {
      throw new InvalidKeyException("Missing password");
    }
    boolean decrypting = opmode == Cipher.DECRYPT_MODE || opmode == Cipher.UNWRAP_MODE;
    byte[] newSalt;
    int newCount;
    if (params == null) {
      if (decrypting && !pkcs12) {
        throw new InvalidAlgorithmParameterException("Parameters missing");
      }
      if (!decrypting && !pkcs12 && salt != null) {
        newSalt = salt.clone();
        newCount = iterationCount;
      } else {
        newSalt = new byte[defaultSaltLength()];
        (random != null ? random : new SecureRandom()).nextBytes(newSalt);
        newCount = kDefaultCount;
      }
    } else {
      if (!(params instanceof PBEParameterSpec)) {
        throw new InvalidAlgorithmParameterException(pkcs12 ? "PBEParameterSpec type required"
            : "Wrong parameter type: PBE expected");
      }
      PBEParameterSpec pbe = (PBEParameterSpec) params;
      newSalt = pbe.getSalt().clone();
      newCount = pbe.getIterationCount();
    }
    if (pkcs12 ? newSalt.length < 8 : newSalt.length != 8) {
      throw new InvalidAlgorithmParameterException(pkcs12 ? "Salt must be at least 8 bytes long"
          : "Salt must be 8 bytes long");
    }
    if (newCount <= 0) {
      throw new InvalidAlgorithmParameterException("IterationCount must be a positive number");
    }
    byte[] password = key.getEncoded();
    byte[] derivedKey;
    byte[] derivedIv = null;
    if (pkcs12) {
      byte[] bmp = new byte[(password.length + 1) * 2];
      for (int i = 0; i < password.length; i++) {
        bmp[2 * i + 1] = (byte) (password[i] & 0x7f);
      }
      derivedKey = pkcs12Derive(bmp, newSalt, newCount, 1, keyBytes);
      if (!stream) {
        derivedIv = pkcs12Derive(bmp, newSalt, newCount, 2, 8);
      }
    } else if (tripleDes) {
      byte[] adjusted = newSalt.clone();
      boolean same = true;
      for (int i = 0; i < 4; i++) {
        if (adjusted[i] != adjusted[i + 4]) {
          same = false;
        }
      }
      if (same) {
        byte[] original = adjusted.clone();
        for (int i = 0; i < 4; i++) {
          adjusted[i] = original[3 - i];
        }
        boolean stillSame = true;
        for (int i = 0; i < 4; i++) {
          if (adjusted[i] != adjusted[i + 4]) {
            stillSame = false;
          }
        }
        if (stillSame && !decrypting) {
          throw new InvalidAlgorithmParameterException("Weak salts cannot be used for encryption");
        }
      }
      byte[] all = new byte[32];
      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (int half = 0; half < 2; half++) {
          md.update(adjusted, half * 4, 4);
          md.update(password);
          byte[] result = md.digest();
          for (int j = 1; j < newCount; j++) {
            md.update(result);
            md.update(password);
            result = md.digest();
          }
          System.arraycopy(result, 0, all, half * 16, 16);
        }
      } catch (NoSuchAlgorithmException e) {
        throw new InvalidKeyException("MD5 not available", e);
      }
      derivedKey = Arrays.copyOf(all, 24);
      derivedIv = Arrays.copyOfRange(all, 24, 32);
    } else {
      byte[] result;
      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password);
        md.update(newSalt);
        result = md.digest();
        for (int j = 1; j < newCount; j++) {
          result = md.digest(result);
        }
      } catch (NoSuchAlgorithmException e) {
        throw new InvalidKeyException("MD5 not available", e);
      }
      derivedKey = Arrays.copyOf(result, 8);
      derivedIv = Arrays.copyOfRange(result, 8, 16);
    }
    try {
      cipher = Cipher.getInstance(transformation);
      SecretKeySpec keySpec = new SecretKeySpec(derivedKey, keyAlgorithm);
      if (stream) {
        cipher.init(opmode, keySpec);
      } else if (rc2Bits > 0) {
        cipher.init(opmode, keySpec, new RC2ParameterSpec(rc2Bits, derivedIv));
      } else {
        cipher.init(opmode, keySpec, new IvParameterSpec(derivedIv));
      }
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new InvalidKeyException("Cannot initialize the cipher", e);
    }
    this.salt = newSalt;
    this.iterationCount = newCount;
    this.iv = derivedIv;
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

  static byte[] pkcs12Derive(byte[] password, byte[] salt, int iterations, int id, int length) {
    final int u = 20;
    final int v = 64;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] diversifier = new byte[v];
      Arrays.fill(diversifier, (byte) id);
      byte[] s = new byte[v * ((salt.length + v - 1) / v)];
      for (int i = 0; i < s.length; i++) {
        s[i] = salt[i % salt.length];
      }
      byte[] p = password.length == 0 ? new byte[0] : new byte[v * ((password.length + v - 1) / v)];
      for (int i = 0; i < p.length; i++) {
        p[i] = password[i % password.length];
      }
      byte[] input = new byte[s.length + p.length];
      System.arraycopy(s, 0, input, 0, s.length);
      System.arraycopy(p, 0, input, s.length, p.length);
      int blocks = (length + u - 1) / u;
      byte[] derived = new byte[length];
      for (int i = 1; i <= blocks; i++) {
        md.update(diversifier);
        md.update(input);
        byte[] a = md.digest();
        for (int j = 1; j < iterations; j++) {
          a = md.digest(a);
        }
        System.arraycopy(a, 0, derived, (i - 1) * u, Math.min(u, length - (i - 1) * u));
        if (i == blocks) {
          break;
        }
        byte[] b = new byte[v];
        for (int j = 0; j < v; j++) {
          b[j] = a[j % u];
        }
        BigInteger bPlusOne = new BigInteger(1, b).add(BigInteger.ONE);
        for (int j = 0; j < input.length / v; j++) {
          byte[] block = Arrays.copyOfRange(input, j * v, (j + 1) * v);
          byte[] sum = new BigInteger(1, block).add(bPlusOne).toByteArray();
          byte[] fixed = new byte[v];
          int copy = Math.min(v, sum.length);
          System.arraycopy(sum, sum.length - copy, fixed, v - copy, copy);
          System.arraycopy(fixed, 0, input, j * v, v);
        }
      }
      return derived;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
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
    return 0;
  }

  public static final class PBEWithMD5AndDES extends PBES1Core {
    public PBEWithMD5AndDES() {
      super("PBEWithMD5AndDES", "DES/CBC/PKCS5Padding", "DES", 8, 0, false, false);
    }
  }

  public static final class PBEWithMD5AndTripleDES extends PBES1Core {
    public PBEWithMD5AndTripleDES() {
      super("PBEWithMD5AndTripleDES", "DESede/CBC/PKCS5Padding", "DESede", 24, 0, false, true);
    }
  }

  public static final class PBEWithSHA1AndDESede extends PBES1Core {
    public PBEWithSHA1AndDESede() {
      super("PBEWithSHA1AndDESede", "DESede/CBC/PKCS5Padding", "DESede", 24, 0, true, false);
    }
  }

  public static final class PBEWithSHA1AndRC2_40 extends PBES1Core {
    public PBEWithSHA1AndRC2_40() {
      super("PBEWithSHA1AndRC2_40", "RC2/CBC/PKCS5Padding", "RC2", 5, 40, true, false);
    }
  }

  public static final class PBEWithSHA1AndRC2_128 extends PBES1Core {
    public PBEWithSHA1AndRC2_128() {
      super("PBEWithSHA1AndRC2_128", "RC2/CBC/PKCS5Padding", "RC2", 16, 128, true, false);
    }
  }

  public static final class PBEWithSHA1AndRC4_40 extends PBES1Core {
    public PBEWithSHA1AndRC4_40() {
      super("PBEWithSHA1AndRC4_40", "ARCFOUR/ECB/NoPadding", "ARCFOUR", 5, 0, true, false);
    }
  }

  public static final class PBEWithSHA1AndRC4_128 extends PBES1Core {
    public PBEWithSHA1AndRC4_128() {
      super("PBEWithSHA1AndRC4_128", "ARCFOUR/ECB/NoPadding", "ARCFOUR", 16, 0, true, false);
    }
  }
}
