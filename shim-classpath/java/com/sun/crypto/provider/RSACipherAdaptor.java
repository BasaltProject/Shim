/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

public final class RSACipherAdaptor extends SignatureSpi {
  private final RSACipher cipher = new RSACipher();
  private java.io.ByteArrayOutputStream verifyBuf;

  public RSACipherAdaptor() {
    try {
      cipher.engineSetMode("ECB");
      cipher.engineSetPadding("PKCS1Padding");
    } catch (GeneralSecurityException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  @Deprecated
  protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    throw new UnsupportedOperationException("setParameter() not supported");
  }

  @Override
  @Deprecated
  protected Object engineGetParameter(String param) throws InvalidParameterException {
    throw new UnsupportedOperationException("getParameter() not supported");
  }

  @Override
  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    cipher.engineInit(Cipher.DECRYPT_MODE, publicKey, (SecureRandom) null);
    if (verifyBuf == null) {
      verifyBuf = new java.io.ByteArrayOutputStream(128);
    } else {
      verifyBuf.reset();
    }
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
    cipher.engineInit(Cipher.ENCRYPT_MODE, privateKey, (SecureRandom) null);
    verifyBuf = null;
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
    cipher.engineInit(Cipher.ENCRYPT_MODE, privateKey, random);
    verifyBuf = null;
  }

  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    engineUpdate(new byte[] {b}, 0, 1);
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    if (verifyBuf != null) {
      verifyBuf.write(b, off, len);
    } else {
      cipher.engineUpdate(b, off, len);
    }
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    try {
      return cipher.engineDoFinal(null, 0, 0);
    } catch (GeneralSecurityException e) {
      throw new SignatureException("doFinal() failed", e);
    }
  }

  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    try {
      byte[] out = cipher.engineDoFinal(sigBytes, 0, sigBytes.length);
      byte[] data = verifyBuf.toByteArray();
      verifyBuf.reset();
      return java.security.MessageDigest.isEqual(out, data);
    } catch (BadPaddingException e) {
      return false;
    } catch (GeneralSecurityException e) {
      throw new SignatureException("doFinal() failed", e);
    }
  }
}
