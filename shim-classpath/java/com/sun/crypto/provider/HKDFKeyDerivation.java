/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.List;
import javax.crypto.KDFParameters;
import javax.crypto.KDFSpi;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;

abstract class HKDFKeyDerivation extends KDFSpi {
  private final String macAlgorithm;
  private final int hashLength;

  protected HKDFKeyDerivation(KDFParameters kdfParameters, String macAlgorithm, int hashLength)
      throws InvalidAlgorithmParameterException {
    super(kdfParameters);
    if (kdfParameters != null) {
      throw new InvalidAlgorithmParameterException(macAlgorithm.replace("Hmac", "HKDF-") + " does not support parameters");
    }
    this.macAlgorithm = macAlgorithm;
    this.hashLength = hashLength;
  }

  @Override
  protected KDFParameters engineGetParameters() {
    return null;
  }

  @Override
  protected SecretKey engineDeriveKey(String alg, AlgorithmParameterSpec derivationSpec)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
    if (alg == null) {
      throw new NullPointerException("the algorithm for the SecretKey return value must not be null");
    }
    if (alg.isEmpty()) {
      throw new NoSuchAlgorithmException("the algorithm for the SecretKey return value must not be empty");
    }
    byte[] bytes = derive(derivationSpec);
    if (derivationSpec instanceof HKDFParameterSpec.Extract) {
      return new SecretKeySpec(bytes, alg);
    }
    if (bytes.length == 0) {
      throw new InvalidAlgorithmParameterException("Cannot derive a key from zero bytes");
    }
    return new SecretKeySpec(bytes, alg);
  }

  @Override
  protected byte[] engineDeriveData(AlgorithmParameterSpec derivationSpec) throws InvalidAlgorithmParameterException {
    return derive(derivationSpec);
  }

  private byte[] derive(AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
    if (spec == null) {
      throw new NullPointerException();
    }
    try {
      if (spec instanceof HKDFParameterSpec.Extract) {
        HKDFParameterSpec.Extract e = (HKDFParameterSpec.Extract) spec;
        return extract(e.ikms(), e.salts());
      }
      if (spec instanceof HKDFParameterSpec.Expand) {
        HKDFParameterSpec.Expand e = (HKDFParameterSpec.Expand) spec;
        return expand(e.prk(), e.info(), e.length());
      }
      if (spec instanceof HKDFParameterSpec.ExtractThenExpand) {
        HKDFParameterSpec.ExtractThenExpand e = (HKDFParameterSpec.ExtractThenExpand) spec;
        SecretKey prk = new SecretKeySpec(extract(e.ikms(), e.salts()), macAlgorithm);
        return expand(prk, e.info(), e.length());
      }
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new InvalidAlgorithmParameterException(e);
    }
    throw new InvalidAlgorithmParameterException("Unsupported or null HKDFParameterSpec");
  }

  private static byte[] concat(List<SecretKey> keys) throws InvalidKeyException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (SecretKey k : keys) {
      byte[] raw = k.getEncoded();
      if (raw == null) {
        throw new InvalidKeyException("Key material is not extractable");
      }
      out.write(raw, 0, raw.length);
    }
    return out.toByteArray();
  }

  private byte[] extract(List<SecretKey> ikms, List<SecretKey> salts) throws InvalidKeyException,
      NoSuchAlgorithmException {
    byte[] salt = concat(salts);
    byte[] ikm = concat(ikms);
    if (salt.length == 0) {
      salt = new byte[hashLength];
    }
    Mac mac = Mac.getInstance(macAlgorithm);
    mac.init(new SecretKeySpec(salt, macAlgorithm));
    return mac.doFinal(ikm);
  }

  private byte[] expand(SecretKey prk, byte[] info, int length) throws InvalidKeyException, NoSuchAlgorithmException,
      InvalidAlgorithmParameterException {
    if (prk == null) {
      throw new InvalidKeyException("a pseudorandom key must be provided");
    }
    byte[] raw = prk.getEncoded();
    if (!"RAW".equalsIgnoreCase(prk.getFormat()) || raw == null) {
      throw new InvalidKeyException("Key material is not extractable");
    }
    if (length > 255 * hashLength) {
      throw new InvalidAlgorithmParameterException("The specified SunJCE provider does not support this input");
    }
    Mac mac = Mac.getInstance(macAlgorithm);
    mac.init(new SecretKeySpec(raw, macAlgorithm));
    byte[] okm = new byte[length];
    byte[] t = new byte[0];
    int pos = 0;
    for (int i = 1; pos < length; i++) {
      mac.update(t);
      mac.update(info);
      mac.update((byte) i);
      t = mac.doFinal();
      int n = Math.min(t.length, length - pos);
      System.arraycopy(t, 0, okm, pos, n);
      pos += n;
    }
    return okm;
  }

  public static final class HKDFSHA256 extends HKDFKeyDerivation {
    public HKDFSHA256(KDFParameters kdfParameters) throws InvalidAlgorithmParameterException {
      super(kdfParameters, "HmacSHA256", 32);
    }
  }

  public static final class HKDFSHA384 extends HKDFKeyDerivation {
    public HKDFSHA384(KDFParameters kdfParameters) throws InvalidAlgorithmParameterException {
      super(kdfParameters, "HmacSHA384", 48);
    }
  }

  public static final class HKDFSHA512 extends HKDFKeyDerivation {
    public HKDFSHA512(KDFParameters kdfParameters) throws InvalidAlgorithmParameterException {
      super(kdfParameters, "HmacSHA512", 64);
    }
  }
}
