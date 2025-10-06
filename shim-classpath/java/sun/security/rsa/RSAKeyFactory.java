/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class RSAKeyFactory extends KeyFactorySpi {
  private static final int kMinModLen = 512;
  private static final int kMaxModLen = 16384;
  private static final int kMaxExpLen = 64;

  private final String type;

  RSAKeyFactory(String type) {
    this.type = type;
  }

  public static final class Legacy extends RSAKeyFactory {
    public Legacy() {
      super("RSA");
    }
  }

  public static final class PSS extends RSAKeyFactory {
    public PSS() {
      super("RSASSA-PSS");
    }
  }

  static AlgorithmParameterSpec checkParams(String type, AlgorithmParameterSpec params) throws InvalidKeyException {
    if (type.equals("RSA")) {
      if (params != null) {
        throw new InvalidKeyException(new ProviderException("null params expected for RSA"));
      }
      return null;
    }
    if (params != null && !(params instanceof PSSParameterSpec)) {
      throw new InvalidKeyException("Params must be PSSParameterSpec");
    }
    return params;
  }

  public static void checkRSAProviderKeyLengths(int modulusLen, BigInteger exponent) throws InvalidKeyException {
    checkKeyLengths(((modulusLen + 7) & ~7), exponent, kMinModLen, Integer.MAX_VALUE);
  }

  public static void checkKeyLengths(int modulusLen, BigInteger exponent, int minModulusLen, int maxModulusLen)
      throws InvalidKeyException {
    if ((minModulusLen > 0) && (modulusLen < (minModulusLen))) {
      throw new InvalidKeyException("RSA keys must be at least " + minModulusLen + " bits long");
    }
    int maxLen = Math.min(maxModulusLen, kMaxModLen);
    if (modulusLen > maxLen) {
      throw new InvalidKeyException("RSA keys must be no longer than " + maxLen + " bits");
    }
    if (exponent != null && modulusLen > 3072 && exponent.bitLength() > kMaxExpLen) {
      throw new InvalidKeyException("RSA exponents can be no longer than " + kMaxExpLen + " bits "
          + "if modulus is greater than 3072 bits");
    }
  }

  static byte[] pkcs8(String type, AlgorithmParameterSpec params, byte[] keySeq) throws IOException {
    DerValue alg;
    if (type.equals("RSASSA-PSS")) {
      DerValue p = params == null ? null : new DerValue(RSAUtil.encodePssParams((PSSParameterSpec) params));
      alg = RSAUtil.algId(RSAUtil.kOidPss, p);
    } else {
      alg = RSAUtil.algId(RSAUtil.kOidRsa, DerValue.nullValue());
    }
    DerOutputStream inner = new DerOutputStream();
    inner.putInteger(0);
    inner.putDerValue(alg);
    inner.putOctetString(keySeq);
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, inner);
    return out.toByteArray();
  }

  public static RSAKey toRSAKey(Key key) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("Key must not be null");
    }
    if (key instanceof RSAPrivateKeyImpl || key instanceof RSAPrivateCrtKeyImpl
        || key instanceof RSAPublicKeyImpl) {
      return (RSAKey) key;
    }
    return RSAKeyFactory.toRSAKeyViaFactory(key);
  }

  private static RSAKey toRSAKeyViaFactory(Key key) throws InvalidKeyException {
    try {
      String alg = key.getAlgorithm();
      if (!alg.equals("RSA") && !alg.equals("RSASSA-PSS")) {
        throw new InvalidKeyException("Expected a RSA key, but got " + alg);
      }
      RSAKeyFactory factory = new RSAKeyFactory(alg);
      return (RSAKey) factory.engineTranslateKey(key);
    } catch (RuntimeException e) {
      throw new InvalidKeyException("Invalid key", e);
    }
  }

  @Override
  protected Key engineTranslateKey(Key key) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("Key must not be null");
    }
    String keyAlg = key.getAlgorithm();
    if (!keyAlg.equals("RSA") && !keyAlg.equals("RSASSA-PSS")) {
      throw new InvalidKeyException("Expected a " + type + " key, but got " + keyAlg);
    }
    if (key instanceof RSAPublicKeyImpl || key instanceof RSAPrivateKeyImpl || key instanceof RSAPrivateCrtKeyImpl) {
      return key;
    }
    if (key instanceof PublicKey) {
      return translatePublicKey((PublicKey) key);
    } else if (key instanceof PrivateKey) {
      return translatePrivateKey((PrivateKey) key);
    }
    throw new InvalidKeyException("Neither a public nor a private key");
  }

  private PublicKey translatePublicKey(PublicKey key) throws InvalidKeyException {
    if (key instanceof RSAPublicKey) {
      RSAPublicKey rsaKey = (RSAPublicKey) key;
      return new RSAPublicKeyImpl(type, rsaKey.getParams(), rsaKey.getModulus(), rsaKey.getPublicExponent());
    }
    if ("X.509".equals(key.getFormat())) {
      byte[] encoded = key.getEncoded();
      return new RSAPublicKeyImpl(type, encoded);
    }
    throw new InvalidKeyException("Public keys must be instance of RSAPublicKey or have X.509 encoding");
  }

  private PrivateKey translatePrivateKey(PrivateKey key) throws InvalidKeyException {
    if (key instanceof RSAPrivateCrtKey) {
      RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) key;
      return new RSAPrivateCrtKeyImpl(type, rsaKey.getParams(), rsaKey.getModulus(), rsaKey.getPublicExponent(),
          rsaKey.getPrivateExponent(), rsaKey.getPrimeP(), rsaKey.getPrimeQ(), rsaKey.getPrimeExponentP(),
          rsaKey.getPrimeExponentQ(), rsaKey.getCrtCoefficient());
    } else if (key instanceof RSAPrivateKey) {
      RSAPrivateKey rsaKey = (RSAPrivateKey) key;
      return new RSAPrivateKeyImpl(type, rsaKey.getParams(), rsaKey.getModulus(), rsaKey.getPrivateExponent());
    }
    if ("PKCS#8".equals(key.getFormat())) {
      byte[] encoded = key.getEncoded();
      return RSAPrivateCrtKeyImpl.newKey(type, null, encoded);
    }
    throw new InvalidKeyException("Private keys must be instance of RSAPrivate(Crt)Key or have PKCS#8 encoding");
  }

  @Override
  protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      return generatePublic(keySpec);
    } catch (InvalidKeySpecException e) {
      throw e;
    } catch (InvalidKeyException | ProviderException e) {
      throw new InvalidKeySpecException(e);
    }
  }

  @Override
  protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      return generatePrivate(keySpec);
    } catch (InvalidKeySpecException e) {
      throw e;
    } catch (InvalidKeyException | ProviderException e) {
      throw new InvalidKeySpecException(e);
    }
  }

  private PublicKey generatePublic(KeySpec keySpec) throws InvalidKeySpecException, InvalidKeyException {
    if (keySpec == null) {
      throw new InvalidKeySpecException("keySpec must not be null");
    }
    if (keySpec instanceof X509EncodedKeySpec) {
      X509EncodedKeySpec x509Spec = (X509EncodedKeySpec) keySpec;
      return new RSAPublicKeyImpl(type, x509Spec.getEncoded());
    }
    if (keySpec instanceof RSAPublicKeySpec) {
      RSAPublicKeySpec rsaSpec = (RSAPublicKeySpec) keySpec;
      return new RSAPublicKeyImpl(type, rsaSpec.getParams(), rsaSpec.getModulus(), rsaSpec.getPublicExponent());
    }
    throw new InvalidKeySpecException(keySpec.getClass().getName() + " not supported.");
  }

  private PrivateKey generatePrivate(KeySpec keySpec) throws InvalidKeySpecException, InvalidKeyException {
    if (keySpec == null) {
      throw new InvalidKeySpecException("keySpec must not be null");
    }
    if (keySpec instanceof PKCS8EncodedKeySpec) {
      PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec) keySpec;
      return RSAPrivateCrtKeyImpl.newKey(type, null, pkcsSpec.getEncoded());
    }
    if (keySpec instanceof RSAPrivateCrtKeySpec) {
      RSAPrivateCrtKeySpec rsaSpec = (RSAPrivateCrtKeySpec) keySpec;
      return new RSAPrivateCrtKeyImpl(type, rsaSpec.getParams(), rsaSpec.getModulus(), rsaSpec.getPublicExponent(),
          rsaSpec.getPrivateExponent(), rsaSpec.getPrimeP(), rsaSpec.getPrimeQ(), rsaSpec.getPrimeExponentP(),
          rsaSpec.getPrimeExponentQ(), rsaSpec.getCrtCoefficient());
    }
    if (keySpec instanceof RSAPrivateKeySpec) {
      RSAPrivateKeySpec rsaSpec = (RSAPrivateKeySpec) keySpec;
      return new RSAPrivateKeyImpl(type, rsaSpec.getParams(), rsaSpec.getModulus(), rsaSpec.getPrivateExponent());
    }
    throw new InvalidKeySpecException(keySpec.getClass().getName() + " not supported.");
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
    try {
      key = engineTranslateKey(key);
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException(e);
    }
    if (key instanceof RSAPublicKey) {
      RSAPublicKey rsaKey = (RSAPublicKey) key;
      if (RSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
        return (T) new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent(), rsaKey.getParams());
      } else if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
        return (T) new X509EncodedKeySpec(key.getEncoded());
      }
      throw new InvalidKeySpecException("KeySpec must be RSAPublicKeySpec or X509EncodedKeySpec for RSA public keys");
    } else if (key instanceof RSAPrivateKey) {
      if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
        return (T) new PKCS8EncodedKeySpec(key.getEncoded());
      } else if (RSAPrivateCrtKeySpec.class.isAssignableFrom(keySpec)) {
        if (key instanceof RSAPrivateCrtKey) {
          RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey) key;
          return (T) new RSAPrivateCrtKeySpec(crtKey.getModulus(), crtKey.getPublicExponent(),
              crtKey.getPrivateExponent(), crtKey.getPrimeP(), crtKey.getPrimeQ(), crtKey.getPrimeExponentP(),
              crtKey.getPrimeExponentQ(), crtKey.getCrtCoefficient(), crtKey.getParams());
        }
        throw new InvalidKeySpecException("RSAPrivateCrtKeySpec can only be used with CRT keys");
      } else if (RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
        RSAPrivateKey rsaKey = (RSAPrivateKey) key;
        return (T) new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent(), rsaKey.getParams());
      }
      throw new InvalidKeySpecException(
          "KeySpec must be RSAPrivate(Crt)KeySpec or PKCS8EncodedKeySpec for RSA private keys");
    }
    throw new InvalidKeySpecException("Neither public nor private key");
  }
}
