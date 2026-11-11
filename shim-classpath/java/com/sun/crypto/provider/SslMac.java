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
import java.util.Arrays;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

abstract class SslMac extends MacSpi implements Cloneable {
  private MessageDigest md;
  private final int padLen;
  private byte[] secret;
  private boolean started;

  SslMac(String digest, int padLen) throws NoSuchAlgorithmException {
    this.md = MessageDigest.getInstance(digest);
    this.padLen = padLen;
  }

  @Override
  protected int engineGetMacLength() {
    return md.getDigestLength();
  }

  @Override
  protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("SslMac does not use parameters");
    }
    if (!(key instanceof SecretKey)) {
      throw new InvalidKeyException("Secret key expected");
    }
    byte[] k = key.getEncoded();
    if (k == null || k.length == 0) {
      throw new InvalidKeyException("Missing key data");
    }
    secret = k;
    engineReset();
  }

  private void begin() {
    if (!started) {
      byte[] pad = new byte[padLen];
      Arrays.fill(pad, (byte) 0x36);
      md.update(secret);
      md.update(pad);
      started = true;
    }
  }

  @Override
  protected void engineUpdate(byte input) {
    begin();
    md.update(input);
  }

  @Override
  protected void engineUpdate(byte[] input, int offset, int len) {
    begin();
    md.update(input, offset, len);
  }

  @Override
  protected void engineUpdate(ByteBuffer input) {
    begin();
    md.update(input);
  }

  @Override
  protected byte[] engineDoFinal() {
    begin();
    byte[] inner = md.digest();
    byte[] pad = new byte[padLen];
    Arrays.fill(pad, (byte) 0x5c);
    md.update(secret);
    md.update(pad);
    md.update(inner);
    byte[] out = md.digest();
    started = false;
    return out;
  }

  @Override
  protected void engineReset() {
    md.reset();
    started = false;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    SslMac copy = (SslMac) super.clone();
    copy.md = (MessageDigest) md.clone();
    return copy;
  }

  public static final class SslMacMD5 extends SslMac {
    public SslMacMD5() throws NoSuchAlgorithmException {
      super("MD5", 48);
    }
  }

  public static final class SslMacSHA1 extends SslMac {
    public SslMacSHA1() throws NoSuchAlgorithmException {
      super("SHA-1", 40);
    }
  }
}
