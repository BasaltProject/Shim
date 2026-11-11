/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import sun.security.rsa.RSACore;
import sun.security.rsa.RSAKeyFactory;
import sun.security.rsa.RSAPadding;

/** MD5andSHA1withRSA: raw MD5||SHA-1 concatenation under PKCS#1 type 1 padding, as TLS 1.0/1.1 use. */
public final class RSASignature extends SignatureSpi {
  private final MessageDigest md5;
  private final MessageDigest sha;
  private RSAPublicKey publicKey;
  private RSAPrivateKey privateKey;

  public RSASignature() {
    try {
      md5 = MessageDigest.getInstance("MD5");
      sha = MessageDigest.getInstance("SHA1");
    } catch (GeneralSecurityException e) {
      throw new java.security.ProviderException(e);
    }
  }

  private void reset() {
    md5.reset();
    sha.reset();
  }

  @Override
  protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
    publicKey = (RSAPublicKey) RSAKeyFactory.toRSAKey(key);
    privateKey = null;
    reset();
  }

  @Override
  protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
    engineInitSign(key, null);
  }

  @Override
  protected void engineInitSign(PrivateKey key, SecureRandom random) throws InvalidKeyException {
    privateKey = (RSAPrivateKey) RSAKeyFactory.toRSAKey(key);
    publicKey = null;
    reset();
  }

  @Override
  protected void engineUpdate(byte b) {
    md5.update(b);
    sha.update(b);
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) {
    md5.update(b, off, len);
    sha.update(b, off, len);
  }

  private byte[] digest() {
    ByteArrayOutputStream o = new ByteArrayOutputStream(36);
    o.writeBytes(md5.digest());
    o.writeBytes(sha.digest());
    return o.toByteArray();
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    try {
      int k = RSACore.getByteLength(privateKey);
      byte[] padded = RSAPadding.getInstance(RSAPadding.PAD_BLOCKTYPE_1, k, null).pad(digest());
      return RSACore.rsa(padded, privateKey, true);
    } catch (GeneralSecurityException e) {
      throw new SignatureException("Could not sign data", e);
    }
  }

  @Override
  protected boolean engineVerify(byte[] sig) throws SignatureException {
    int k = RSACore.getByteLength(publicKey);
    if (sig.length != k) {
      throw new SignatureException("Bad signature length: got " + sig.length + " but was expecting " + k);
    }
    try {
      byte[] expected = RSAPadding.getInstance(RSAPadding.PAD_BLOCKTYPE_1, k, null).pad(digest());
      return MessageDigest.isEqual(expected, RSACore.rsa(sig, publicKey));
    } catch (javax.crypto.BadPaddingException e) {
      return false;
    } catch (GeneralSecurityException e) {
      throw new SignatureException("Signature verification failed", e);
    }
  }

  @Override
  @Deprecated
  protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    throw new InvalidParameterException("Parameters not supported");
  }

  @Override
  @Deprecated
  protected Object engineGetParameter(String param) throws InvalidParameterException {
    throw new InvalidParameterException("Parameters not supported");
  }
}
