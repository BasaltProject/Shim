/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

abstract class HmacPBE extends MacSpi implements Cloneable {
  private Mac delegate;
  private final String macName;
  private final String digest;
  private final int digestLen;
  private final int blockLen;
  private final boolean pkcs12;

  HmacPBE(String digest, String macName, int digestLen, int blockLen, boolean pkcs12) throws NoSuchAlgorithmException {
    this.digest = digest;
    this.macName = macName;
    this.digestLen = digestLen;
    this.blockLen = blockLen;
    this.pkcs12 = pkcs12;
    this.delegate = Mac.getInstance(macName);
  }

  @Override
  protected int engineGetMacLength() {
    return digestLen;
  }

  @Override
  protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    if (!(key instanceof SecretKey)) {
      throw new InvalidKeyException("Secret key expected");
    }
    if (!(params instanceof PBEParameterSpec)) {
      throw new InvalidAlgorithmParameterException("PBEParameterSpec required for salt and iteration count");
    }
    byte[] salt = ((PBEParameterSpec) params).getSalt();
    int iterations = ((PBEParameterSpec) params).getIterationCount();
    byte[] password = key.getEncoded();
    if (password == null) {
      throw new InvalidKeyException("Missing key data");
    }
    byte[] derived;
    if (pkcs12) {
      char[] chars = new String(password, StandardCharsets.UTF_8).toCharArray();
      byte[] bmp = new byte[(chars.length + 1) * 2];
      for (int i = 0; i < chars.length; i++) {
        bmp[2 * i] = (byte) (chars[i] >> 8);
        bmp[2 * i + 1] = (byte) chars[i];
      }
      derived = derive(bmp, salt, iterations);
    } else {
      byte[] low = new byte[password.length];
      for (int i = 0; i < low.length; i++) {
        low[i] = (byte) (password[i] & 0x7f);
      }
      derived = pbkdf2(low, salt, iterations);
    }
    delegate.init(new SecretKeySpec(derived, macName));
    Arrays.fill(derived, (byte) 0);
    Arrays.fill(password, (byte) 0);
  }

  private byte[] pbkdf2(byte[] password, byte[] salt, int iterations) throws InvalidKeyException {
    try {
      Mac prf = Mac.getInstance(macName);
      prf.init(new SecretKeySpec(password.length == 0 ? new byte[1] : password, macName));
      prf.update(salt);
      prf.update(new byte[] {0, 0, 0, 1});
      byte[] u = prf.doFinal();
      byte[] t = u.clone();
      for (int i = 1; i < iterations; i++) {
        u = prf.doFinal(u);
        for (int j = 0; j < t.length; j++) {
          t[j] ^= u[j];
        }
      }
      // parity: PBMAC1Core derives block-length bits, not bytes
      return Arrays.copyOf(t, blockLen / 8);
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidKeyException(e);
    }
  }

  private byte[] derive(byte[] password, byte[] salt, int iterations) throws InvalidKeyException {
    try {
      MessageDigest md = MessageDigest.getInstance(digest);
      byte[] diversifier = new byte[blockLen];
      Arrays.fill(diversifier, (byte) 3);
      byte[] s = new byte[salt.length == 0 ? 0 : blockLen * ((salt.length + blockLen - 1) / blockLen)];
      for (int i = 0; i < s.length; i++) {
        s[i] = salt[i % salt.length];
      }
      byte[] p = new byte[password.length == 0 ? 0 : blockLen * ((password.length + blockLen - 1) / blockLen)];
      for (int i = 0; i < p.length; i++) {
        p[i] = password[i % password.length];
      }
      byte[] input = new byte[s.length + p.length];
      System.arraycopy(s, 0, input, 0, s.length);
      System.arraycopy(p, 0, input, s.length, p.length);
      md.update(diversifier);
      md.update(input);
      byte[] a = md.digest();
      for (int j = 1; j < iterations; j++) {
        a = md.digest(a);
      }
      return Arrays.copyOf(a, digestLen);
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidKeyException(e);
    }
  }

  @Override
  protected void engineUpdate(byte input) {
    delegate.update(input);
  }

  @Override
  protected void engineUpdate(byte[] input, int offset, int len) {
    delegate.update(input, offset, len);
  }

  @Override
  protected void engineUpdate(ByteBuffer input) {
    delegate.update(input);
  }

  @Override
  protected byte[] engineDoFinal() {
    return delegate.doFinal();
  }

  @Override
  protected void engineReset() {
    delegate.reset();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    HmacPBE copy = (HmacPBE) super.clone();
    copy.delegate = (Mac) delegate.clone();
    return copy;
  }

  public static final class HmacPBESHA1 extends HmacPBE {
    public HmacPBESHA1() throws NoSuchAlgorithmException {
      super("SHA-1", "HmacSHA1", 20, 64, true);
    }
  }

  public static final class PBEWithHmacSHA1 extends HmacPBE {
    public PBEWithHmacSHA1() throws NoSuchAlgorithmException {
      super("SHA-1", "HmacSHA1", 20, 64, false);
    }
  }

  public static final class HmacPBESHA224 extends HmacPBE {
    public HmacPBESHA224() throws NoSuchAlgorithmException {
      super("SHA-224", "HmacSHA224", 28, 64, true);
    }
  }

  public static final class PBEWithHmacSHA224 extends HmacPBE {
    public PBEWithHmacSHA224() throws NoSuchAlgorithmException {
      super("SHA-224", "HmacSHA224", 28, 64, false);
    }
  }

  public static final class HmacPBESHA256 extends HmacPBE {
    public HmacPBESHA256() throws NoSuchAlgorithmException {
      super("SHA-256", "HmacSHA256", 32, 64, true);
    }
  }

  public static final class PBEWithHmacSHA256 extends HmacPBE {
    public PBEWithHmacSHA256() throws NoSuchAlgorithmException {
      super("SHA-256", "HmacSHA256", 32, 64, false);
    }
  }

  public static final class HmacPBESHA384 extends HmacPBE {
    public HmacPBESHA384() throws NoSuchAlgorithmException {
      super("SHA-384", "HmacSHA384", 48, 128, true);
    }
  }

  public static final class PBEWithHmacSHA384 extends HmacPBE {
    public PBEWithHmacSHA384() throws NoSuchAlgorithmException {
      super("SHA-384", "HmacSHA384", 48, 128, false);
    }
  }

  public static final class HmacPBESHA512 extends HmacPBE {
    public HmacPBESHA512() throws NoSuchAlgorithmException {
      super("SHA-512", "HmacSHA512", 64, 128, true);
    }
  }

  public static final class PBEWithHmacSHA512 extends HmacPBE {
    public PBEWithHmacSHA512() throws NoSuchAlgorithmException {
      super("SHA-512", "HmacSHA512", 64, 128, false);
    }
  }

  public static final class HmacPBESHA512_224 extends HmacPBE {
    public HmacPBESHA512_224() throws NoSuchAlgorithmException {
      super("SHA-512/224", "HmacSHA512/224", 28, 128, true);
    }
  }

  public static final class PBEWithHmacSHA512_224 extends HmacPBE {
    public PBEWithHmacSHA512_224() throws NoSuchAlgorithmException {
      super("SHA-512/224", "HmacSHA512/224", 28, 128, false);
    }
  }

  public static final class HmacPBESHA512_256 extends HmacPBE {
    public HmacPBESHA512_256() throws NoSuchAlgorithmException {
      super("SHA-512/256", "HmacSHA512/256", 32, 128, true);
    }
  }

  public static final class PBEWithHmacSHA512_256 extends HmacPBE {
    public PBEWithHmacSHA512_256() throws NoSuchAlgorithmException {
      super("SHA-512/256", "HmacSHA512/256", 32, 128, false);
    }
  }
}
