/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

public final class RSAPSSSignature extends SignatureSpi {
  private PSSParameterSpec sigParams;
  private MessageDigest md;
  private RSAPublicKey publicKey;
  private RSAPrivateKey privateKey;
  private SecureRandom random;
  private boolean digestReset = true;

  public RSAPSSSignature() {
  }

  private void ensureInit() throws SignatureException {
    if (this.sigParams == null) {
      throw new SignatureException("Parameters required for RSASSA-PSS signatures");
    }
  }

  private void setDigest(String name) throws InvalidAlgorithmParameterException {
    try {
      md = MessageDigest.getInstance(name);
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidAlgorithmParameterException("Unsupported digest: " + name);
    }
  }

  private void checkKeyParams(AlgorithmParameterSpec keyParams) throws InvalidKeyException {
    if (keyParams == null) {
      return;
    }
    PSSParameterSpec kp = (PSSParameterSpec) keyParams;
    if (sigParams != null) {
      if (!kp.getDigestAlgorithm().equalsIgnoreCase(sigParams.getDigestAlgorithm())
          || kp.getSaltLength() != sigParams.getSaltLength()) {
        throw new InvalidKeyException("Signature parameters does not match key parameters");
      }
    } else {
      try {
        sigParams = kp;
        setDigest(kp.getDigestAlgorithm());
      } catch (InvalidAlgorithmParameterException e) {
        throw new InvalidKeyException("Invalid key parameters", e);
      }
    }
  }

  private void checkKeySize(RSAKey key) throws InvalidKeyException {
    if (sigParams != null) {
      int emLen = (key.getModulus().bitLength() - 1 + 7) >> 3;
      int hLen = md.getDigestLength();
      int min = hLen + sigParams.getSaltLength() + 2;
      if (emLen < min) {
        throw new InvalidKeyException("Key is too short, need min " + min + " bytes");
      }
    }
  }

  @Override
  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    if (!(publicKey instanceof RSAPublicKey)) {
      throw new InvalidKeyException("key must be RSAPublicKey");
    }
    RSAPublicKey rsaKey = (RSAPublicKey) RSAKeyFactory.toRSAKey(publicKey);
    checkKeyParams(rsaKey.getParams());
    checkKeySize(rsaKey);
    this.publicKey = rsaKey;
    this.privateKey = null;
    resetDigest();
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
    engineInitSign(privateKey, null);
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
    if (!(privateKey instanceof RSAPrivateKey)) {
      throw new InvalidKeyException("key must be RSAPrivateKey");
    }
    RSAPrivateKey rsaKey = (RSAPrivateKey) RSAKeyFactory.toRSAKey(privateKey);
    checkKeyParams(rsaKey.getParams());
    checkKeySize(rsaKey);
    this.privateKey = rsaKey;
    this.publicKey = null;
    this.random = random;
    resetDigest();
  }

  private void resetDigest() {
    if (md != null) {
      md.reset();
    }
  }

  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    ensureInit();
    md.update(b);
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    ensureInit();
    md.update(b, off, len);
  }

  @Override
  protected void engineUpdate(ByteBuffer b) {
    try {
      ensureInit();
    } catch (SignatureException e) {
      throw new RuntimeException(e.getMessage());
    }
    md.update(b);
  }

  private byte[] mgf1(MessageDigest mgfMd, byte[] seed, int len) {
    byte[] out = new byte[len];
    int hLen = mgfMd.getDigestLength();
    int produced = 0;
    int counter = 0;
    while (produced < len) {
      mgfMd.update(seed);
      mgfMd.update(new byte[] {(byte) (counter >>> 24), (byte) (counter >>> 16), (byte) (counter >>> 8),
        (byte) counter});
      byte[] h = mgfMd.digest();
      int n = Math.min(hLen, len - produced);
      System.arraycopy(h, 0, out, produced, n);
      produced += n;
      counter++;
    }
    return out;
  }

  private MessageDigest mgfDigest() throws NoSuchAlgorithmException {
    AlgorithmParameterSpec mgfSpec = sigParams.getMGFParameters();
    String name = mgfSpec instanceof MGF1ParameterSpec ? ((MGF1ParameterSpec) mgfSpec).getDigestAlgorithm()
        : "SHA-1";
    return MessageDigest.getInstance(name);
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    ensureInit();
    try {
      int modBits = privateKey.getModulus().bitLength();
      int emBits = modBits - 1;
      int emLen = (emBits + 7) >> 3;
      byte[] mHash = md.digest();
      int hLen = mHash.length;
      int sLen = sigParams.getSaltLength();
      byte[] salt = new byte[sLen];
      (random == null ? new SecureRandom() : random).nextBytes(salt);
      byte[] mPrime = new byte[8 + hLen + sLen];
      System.arraycopy(mHash, 0, mPrime, 8, hLen);
      System.arraycopy(salt, 0, mPrime, 8 + hLen, sLen);
      MessageDigest hash = MessageDigest.getInstance(sigParams.getDigestAlgorithm());
      byte[] h = hash.digest(mPrime);
      byte[] db = new byte[emLen - hLen - 1];
      db[db.length - sLen - 1] = 1;
      System.arraycopy(salt, 0, db, db.length - sLen, sLen);
      byte[] dbMask = mgf1(mgfDigest(), h, db.length);
      byte[] em = new byte[emLen];
      for (int i = 0; i < db.length; i++) {
        em[i] = (byte) (db[i] ^ dbMask[i]);
      }
      System.arraycopy(h, 0, em, db.length, hLen);
      em[emLen - 1] = (byte) 0xbc;
      int zeroBits = 8 * emLen - emBits;
      em[0] &= (byte) (0xff >>> zeroBits);
      int k = RSACore.getByteLength(privateKey);
      byte[] padded = new byte[k];
      System.arraycopy(em, 0, padded, k - emLen, emLen);
      return RSACore.rsa(padded, privateKey, true);
    } catch (GeneralSecurityException e) {
      throw new SignatureException("Could not sign data", e);
    }
  }

  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    ensureInit();
    int k = RSACore.getByteLength(publicKey);
    if (sigBytes.length != k) {
      throw new SignatureException("Signature length not correct: got " + sigBytes.length + " but was expecting "
          + k);
    }
    try {
      int modBits = publicKey.getModulus().bitLength();
      int emBits = modBits - 1;
      int emLen = (emBits + 7) >> 3;
      byte[] mHash = md.digest();
      int hLen = mHash.length;
      int sLen = sigParams.getSaltLength();
      byte[] decrypted;
      try {
        decrypted = RSACore.rsa(sigBytes, publicKey);
      } catch (javax.crypto.BadPaddingException e) {
        return false;
      }
      byte[] em = java.util.Arrays.copyOfRange(decrypted, k - emLen, k);
      for (int i = 0; i < k - emLen; i++) {
        if (decrypted[i] != 0) {
          return false;
        }
      }
      if (em[emLen - 1] != (byte) 0xbc) {
        return false;
      }
      int zeroBits = 8 * emLen - emBits;
      if ((em[0] & (0xff << (8 - zeroBits)) & 0xff) != 0) {
        return false;
      }
      byte[] maskedDb = java.util.Arrays.copyOfRange(em, 0, emLen - hLen - 1);
      byte[] h = java.util.Arrays.copyOfRange(em, emLen - hLen - 1, emLen - 1);
      byte[] dbMask = mgf1(mgfDigest(), h, maskedDb.length);
      byte[] db = new byte[maskedDb.length];
      for (int i = 0; i < db.length; i++) {
        db[i] = (byte) (maskedDb[i] ^ dbMask[i]);
      }
      db[0] &= (byte) (0xff >>> zeroBits);
      int psLen = db.length - sLen - 1;
      if (psLen < 0) {
        return false;
      }
      for (int i = 0; i < psLen; i++) {
        if (db[i] != 0) {
          return false;
        }
      }
      if (db[psLen] != 1) {
        return false;
      }
      byte[] mPrime = new byte[8 + hLen + sLen];
      System.arraycopy(mHash, 0, mPrime, 8, hLen);
      System.arraycopy(db, db.length - sLen, mPrime, 8 + hLen, sLen);
      MessageDigest hash = MessageDigest.getInstance(sigParams.getDigestAlgorithm());
      byte[] h2 = hash.digest(mPrime);
      return MessageDigest.isEqual(h, h2);
    } catch (GeneralSecurityException e) {
      throw new SignatureException("Signature verification failed", e);
    }
  }

  @Override
  protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    if (!(params instanceof PSSParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Parameters must be of type PSSParameterSpec");
    }
    PSSParameterSpec spec = (PSSParameterSpec) params;
    if (this.sigParams != null && !isSame(this.sigParams, spec)) {
      throw new InvalidAlgorithmParameterException("Signature parameters already set and cannot be changed");
    }
    if (spec.getTrailerField() != 1) {
      throw new InvalidAlgorithmParameterException("Only supports TrailerFieldBC(1)");
    }
    if (!spec.getMGFAlgorithm().equalsIgnoreCase("MGF1")) {
      throw new InvalidAlgorithmParameterException("Only supports MGF1");
    }
    AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
    if (!(mgfSpec instanceof MGF1ParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Only MGF1ParameterSpec is supported");
    }
    setDigest(spec.getDigestAlgorithm());
    if (privateKey != null) {
      try {
        checkKeyParams(privateKey.getParams());
        checkKeySize(privateKey);
      } catch (InvalidKeyException e) {
        throw new InvalidAlgorithmParameterException(e.getMessage());
      }
    }
    this.sigParams = spec;
  }

  private static boolean isSame(PSSParameterSpec a, PSSParameterSpec b) {
    return a.getDigestAlgorithm().equalsIgnoreCase(b.getDigestAlgorithm()) && a.getSaltLength() == b.getSaltLength();
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
  protected AlgorithmParameters engineGetParameters() {
    AlgorithmParameters ap = null;
    if (this.sigParams != null) {
      try {
        ap = AlgorithmParameters.getInstance("RSASSA-PSS");
        ap.init(this.sigParams);
      } catch (GeneralSecurityException gse) {
        throw new ProviderException(gse.getMessage());
      }
    }
    return ap;
  }
}
