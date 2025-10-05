/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

final class PBKDF2KeyImpl implements PBEKey {
  private static final long serialVersionUID = -2234868909660948157L;

  private final char[] passwd;
  private final byte[] salt;
  private final int iterCount;
  private final byte[] key;
  private final String prfAlgo;
  private final String algName;

  PBKDF2KeyImpl(char[] passwd, byte[] salt, int iterCount, int keyLength, String prfAlgo, String algName)
      throws InvalidKeySpecException {
    this.passwd = passwd.clone();
    this.salt = salt.clone();
    this.iterCount = iterCount;
    this.prfAlgo = prfAlgo;
    this.algName = algName;
    byte[] pwdBytes = new String(passwd).getBytes(StandardCharsets.UTF_8);
    try {
      this.key = derive(pwdBytes, this.salt, iterCount, keyLength);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new InvalidKeySpecException("Cannot derive key", e);
    } finally {
      java.util.Arrays.fill(pwdBytes, (byte) 0);
    }
  }

  private byte[] derive(byte[] pwd, byte[] salt, int iterations, int keyLengthBits) throws NoSuchAlgorithmException,
      InvalidKeyException {
    Mac prf = Mac.getInstance(prfAlgo);
    int hlen = prf.getMacLength();
    prf.init(new SecretKeySpec(pwd.length == 0 ? new byte[1] : pwd, "RAW"));
    if (pwd.length == 0) {
      prf.init(new EmptyKey());
    }
    int dkLen = keyLengthBits / 8;
    int blocks = (dkLen + hlen - 1) / hlen;
    byte[] dk = new byte[dkLen];
    byte[] u = new byte[hlen];
    byte[] t = new byte[hlen];
    for (int i = 1; i <= blocks; i++) {
      prf.update(salt);
      prf.update(new byte[] {(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i});
      u = prf.doFinal();
      System.arraycopy(u, 0, t, 0, hlen);
      for (int c = 1; c < iterations; c++) {
        u = prf.doFinal(u);
        for (int j = 0; j < hlen; j++) {
          t[j] ^= u[j];
        }
      }
      int n = Math.min(hlen, dkLen - (i - 1) * hlen);
      System.arraycopy(t, 0, dk, (i - 1) * hlen, n);
    }
    return dk;
  }

  private static final class EmptyKey implements javax.crypto.SecretKey {
    private static final long serialVersionUID = 1L;

    @Override
    public String getAlgorithm() {
      return "RAW";
    }

    @Override
    public String getFormat() {
      return "RAW";
    }

    @Override
    public byte[] getEncoded() {
      return new byte[0];
    }
  }

  @Override
  public byte[] getEncoded() {
    return key.clone();
  }

  @Override
  public String getAlgorithm() {
    return algName;
  }

  @Override
  public int getIterationCount() {
    return iterCount;
  }

  @Override
  public char[] getPassword() {
    return passwd.clone();
  }

  @Override
  public byte[] getSalt() {
    return salt.clone();
  }

  @Override
  public String getFormat() {
    return "RAW";
  }

  @Override
  public int hashCode() {
    int retval = 0;
    for (int i = 1; i < this.key.length; i++) {
      retval += this.key[i] * i;
    }
    return retval ^ getAlgorithm().toLowerCase(java.util.Locale.ENGLISH).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SecretKey)) {
      return false;
    }
    SecretKey that = (SecretKey) obj;
    if (!(that.getAlgorithm().equalsIgnoreCase(getAlgorithm()))) {
      return false;
    }
    if (!(that.getFormat().equalsIgnoreCase("RAW"))) {
      return false;
    }
    return java.security.MessageDigest.isEqual(key, that.getEncoded());
  }
}
