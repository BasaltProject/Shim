/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

abstract class HmacCore extends MacSpi implements Cloneable {
  private MessageDigest md;
  private byte[] k_ipad;
  private byte[] k_opad;
  private boolean first;
  private final int blockLen;
  private final String algorithm;

  HmacCore(String digestAlgorithm, int blockLength) throws NoSuchAlgorithmException {
    this.algorithm = digestAlgorithm;
    this.md = MessageDigest.getInstance(digestAlgorithm);
    this.blockLen = blockLength;
    this.k_ipad = new byte[blockLen];
    this.k_opad = new byte[blockLen];
    first = true;
  }

  @Override
  protected int engineGetMacLength() {
    return md.getDigestLength();
  }

  @Override
  protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("HMAC does not use parameters");
    }
    if (!(key instanceof SecretKey)) {
      throw new InvalidKeyException("Secret key expected");
    }
    byte[] secret = key.getEncoded();
    if (secret == null) {
      throw new InvalidKeyException("Missing key data");
    }
    if (secret.length > blockLen) {
      byte[] tmp = md.digest(secret);
      java.util.Arrays.fill(secret, (byte) 0);
      secret = tmp;
    }
    for (int i = 0; i < blockLen; i++) {
      byte b = i < secret.length ? secret[i] : 0;
      k_ipad[i] = (byte) (b ^ 0x36);
      k_opad[i] = (byte) (b ^ 0x5c);
    }
    java.util.Arrays.fill(secret, (byte) 0);
    engineReset();
  }

  @Override
  protected void engineUpdate(byte input) {
    if (first) {
      md.update(k_ipad);
      first = false;
    }
    md.update(input);
  }

  @Override
  protected void engineUpdate(byte[] input, int offset, int len) {
    if (first) {
      md.update(k_ipad);
      first = false;
    }
    md.update(input, offset, len);
  }

  @Override
  protected void engineUpdate(ByteBuffer input) {
    if (first) {
      md.update(k_ipad);
      first = false;
    }
    md.update(input);
  }

  @Override
  protected byte[] engineDoFinal() {
    if (first) {
      md.update(k_ipad);
    } else {
      first = true;
    }
    byte[] tmp = md.digest();
    md.update(k_opad);
    md.update(tmp);
    java.util.Arrays.fill(tmp, (byte) 0);
    return md.digest();
  }

  @Override
  protected void engineReset() {
    if (!first) {
      md.reset();
      first = true;
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    HmacCore copy = (HmacCore) super.clone();
    copy.md = (MessageDigest) md.clone();
    copy.k_ipad = k_ipad.clone();
    copy.k_opad = k_opad.clone();
    return copy;
  }

  public static final class HmacMD5 extends HmacCore {
    public HmacMD5() throws NoSuchAlgorithmException {
      super("MD5", 64);
    }
  }

  public static final class HmacSHA1 extends HmacCore {
    public HmacSHA1() throws NoSuchAlgorithmException {
      super("SHA-1", 64);
    }
  }

  public static final class HmacSHA224 extends HmacCore {
    public HmacSHA224() throws NoSuchAlgorithmException {
      super("SHA-224", 64);
    }
  }

  public static final class HmacSHA256 extends HmacCore {
    public HmacSHA256() throws NoSuchAlgorithmException {
      super("SHA-256", 64);
    }
  }

  public static final class HmacSHA384 extends HmacCore {
    public HmacSHA384() throws NoSuchAlgorithmException {
      super("SHA-384", 128);
    }
  }

  public static final class HmacSHA512 extends HmacCore {
    public HmacSHA512() throws NoSuchAlgorithmException {
      super("SHA-512", 128);
    }
  }

  public static final class HmacSHA512_224 extends HmacCore {
    public HmacSHA512_224() throws NoSuchAlgorithmException {
      super("SHA-512/224", 128);
    }
  }

  public static final class HmacSHA512_256 extends HmacCore {
    public HmacSHA512_256() throws NoSuchAlgorithmException {
      super("SHA-512/256", 128);
    }
  }

  public static final class HmacSHA3_224 extends HmacCore {
    public HmacSHA3_224() throws NoSuchAlgorithmException {
      super("SHA3-224", 144);
    }
  }

  public static final class HmacSHA3_256 extends HmacCore {
    public HmacSHA3_256() throws NoSuchAlgorithmException {
      super("SHA3-256", 136);
    }
  }

  public static final class HmacSHA3_384 extends HmacCore {
    public HmacSHA3_384() throws NoSuchAlgorithmException {
      super("SHA3-384", 104);
    }
  }

  public static final class HmacSHA3_512 extends HmacCore {
    public HmacSHA3_512() throws NoSuchAlgorithmException {
      super("SHA3-512", 72);
    }
  }
}
