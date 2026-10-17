/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.io.IOException;
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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public abstract class RSASignature extends SignatureSpi {
  private final MessageDigest md;
  private final String digestName;
  private RSAPublicKey publicKey;
  private RSAPrivateKey privateKey;
  private boolean signing;

  RSASignature(String digestName) {
    this.digestName = digestName;
    try {
      this.md = MessageDigest.getInstance(digestName);
    } catch (NoSuchAlgorithmException e) {
      throw new ProviderException(e);
    }
  }

  private byte[] encodeDigestInfo(byte[] digest) throws IOException {
    DerOutputStream inner = new DerOutputStream();
    inner.putDerValue(RSAUtil.digestAlgId(digestName));
    inner.putOctetString(digest);
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, inner);
    return out.toByteArray();
  }

  @Override
  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    RSAPublicKey rsaKey = (RSAPublicKey) RSAKeyFactory.toRSAKey(publicKey);
    this.privateKey = null;
    this.publicKey = rsaKey;
    this.signing = false;
    md.reset();
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
    engineInitSign(privateKey, null);
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
    RSAPrivateKey rsaKey = (RSAPrivateKey) RSAKeyFactory.toRSAKey(privateKey);
    this.privateKey = rsaKey;
    this.publicKey = null;
    this.signing = true;
    md.reset();
  }

  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    md.update(b);
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    md.update(b, off, len);
  }

  @Override
  protected void engineUpdate(ByteBuffer b) {
    md.update(b);
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    byte[] digest = md.digest();
    try {
      byte[] encoded = encodeDigestInfo(digest);
      int k = RSACore.getByteLength(privateKey);
      RSAPadding padding = RSAPadding.getInstance(RSAPadding.PAD_BLOCKTYPE_1, k, null);
      byte[] padded = padding.pad(encoded);
      return RSACore.rsa(padded, privateKey, true);
    } catch (GeneralSecurityException | IOException e) {
      throw new SignatureException("Could not sign data", e);
    }
  }

  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    int k = RSACore.getByteLength(publicKey);
    if (sigBytes.length != k) {
      throw new SignatureException("Bad signature length: got " + sigBytes.length + " but was expecting " + k);
    }
    byte[] digest = md.digest();
    try {
      byte[] encoded = encodeDigestInfo(digest);
      RSAPadding padding = RSAPadding.getInstance(RSAPadding.PAD_BLOCKTYPE_1, k, null);
      byte[] expected = padding.pad(encoded);
      byte[] decrypted = RSACore.rsa(sigBytes, publicKey);
      return MessageDigest.isEqual(expected, decrypted);
    } catch (javax.crypto.BadPaddingException e) {
      return false;
    } catch (GeneralSecurityException | IOException e) {
      throw new SignatureException("Signature verification failed", e);
    }
  }

  @Override
  @Deprecated
  protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    throw new UnsupportedOperationException("setParameter() not supported");
  }

  @Override
  protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    if (params != null) {
      throw new InvalidAlgorithmParameterException("No parameters accepted");
    }
  }

  @Override
  @Deprecated
  protected Object engineGetParameter(String param) throws InvalidParameterException {
    throw new UnsupportedOperationException("getParameter() not supported");
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return null;
  }

  public static final class MD2withRSA extends RSASignature {
    public MD2withRSA() {
      super("MD2");
    }
  }

  public static final class MD5withRSA extends RSASignature {
    public MD5withRSA() {
      super("MD5");
    }
  }

  public static final class SHA1withRSA extends RSASignature {
    public SHA1withRSA() {
      super("SHA-1");
    }
  }

  public static final class SHA224withRSA extends RSASignature {
    public SHA224withRSA() {
      super("SHA-224");
    }
  }

  public static final class SHA256withRSA extends RSASignature {
    public SHA256withRSA() {
      super("SHA-256");
    }
  }

  public static final class SHA384withRSA extends RSASignature {
    public SHA384withRSA() {
      super("SHA-384");
    }
  }

  public static final class SHA512withRSA extends RSASignature {
    public SHA512withRSA() {
      super("SHA-512");
    }
  }

  public static final class SHA512_224withRSA extends RSASignature {
    public SHA512_224withRSA() {
      super("SHA-512/224");
    }
  }

  public static final class SHA512_256withRSA extends RSASignature {
    public SHA512_256withRSA() {
      super("SHA-512/256");
    }
  }

  public static final class SHA3_224withRSA extends RSASignature {
    public SHA3_224withRSA() {
      super("SHA3-224");
    }
  }

  public static final class SHA3_256withRSA extends RSASignature {
    public SHA3_256withRSA() {
      super("SHA3-256");
    }
  }

  public static final class SHA3_384withRSA extends RSASignature {
    public SHA3_384withRSA() {
      super("SHA3-384");
    }
  }

  public static final class SHA3_512withRSA extends RSASignature {
    public SHA3_512withRSA() {
      super("SHA3-512");
    }
  }
}
