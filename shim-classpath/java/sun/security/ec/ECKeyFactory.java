/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class ECKeyFactory extends KeyFactorySpi {
  public ECKeyFactory() {
  }

  private static ECKeyFactory instance;

  public static synchronized ECKeyFactory instance() {
    if (instance == null) {
      instance = new ECKeyFactory();
    }
    return instance;
  }

  public static ECKey toECKey(Key key) throws InvalidKeyException {
    if (key instanceof ECKey) {
      ECKey ecKey = (ECKey) key;
      return ecKey;
    }
    return (ECKey) instance().engineTranslateKey(key);
  }

  @Override
  protected Key engineTranslateKey(Key key) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("Key must not be null");
    }
    String keyAlg = key.getAlgorithm();
    if (!keyAlg.equals("EC")) {
      throw new InvalidKeyException("Not an EC key: " + keyAlg);
    }
    if (key instanceof PublicKey) {
      return implTranslatePublicKey((PublicKey) key);
    } else if (key instanceof PrivateKey) {
      return implTranslatePrivateKey((PrivateKey) key);
    }
    throw new InvalidKeyException("Neither a public nor a private key");
  }

  private PublicKey implTranslatePublicKey(PublicKey key) throws InvalidKeyException {
    if (key instanceof ECPublicKeyImpl) {
      return key;
    }
    if (key instanceof ECPublicKey) {
      ECPublicKey ecKey = (ECPublicKey) key;
      return new ECPublicKeyImpl(ecKey.getW(), ecKey.getParams());
    }
    if ("X.509".equals(key.getFormat())) {
      byte[] encoded = key.getEncoded();
      return new ECPublicKeyImpl(encoded);
    }
    throw new InvalidKeyException("Public keys must be instance of ECPublicKey or have X.509 encoding");
  }

  private PrivateKey implTranslatePrivateKey(PrivateKey key) throws InvalidKeyException {
    if (key instanceof ECPrivateKeyImpl) {
      return key;
    }
    if (key instanceof ECPrivateKey) {
      ECPrivateKey ecKey = (ECPrivateKey) key;
      return new ECPrivateKeyImpl(ecKey.getS(), ecKey.getParams());
    }
    if ("PKCS#8".equals(key.getFormat())) {
      return new ECPrivateKeyImpl(key.getEncoded());
    }
    throw new InvalidKeyException("Private keys must be instance of ECPrivateKey or have PKCS#8 encoding");
  }

  @Override
  protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      return implGeneratePublic(keySpec);
    } catch (InvalidKeySpecException e) {
      throw e;
    } catch (GeneralSecurityException e) {
      throw new InvalidKeySpecException(e);
    }
  }

  @Override
  protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      return implGeneratePrivate(keySpec);
    } catch (InvalidKeySpecException e) {
      throw e;
    } catch (GeneralSecurityException e) {
      throw new InvalidKeySpecException(e);
    }
  }

  private PublicKey implGeneratePublic(KeySpec keySpec) throws GeneralSecurityException {
    if (keySpec instanceof X509EncodedKeySpec) {
      X509EncodedKeySpec x509Spec = (X509EncodedKeySpec) keySpec;
      return new ECPublicKeyImpl(x509Spec.getEncoded());
    } else if (keySpec instanceof ECPublicKeySpec) {
      ECPublicKeySpec ecSpec = (ECPublicKeySpec) keySpec;
      return new ECPublicKeyImpl(ecSpec.getW(), ecSpec.getParams());
    }
    throw new InvalidKeySpecException(keySpec.getClass().getName() + " not supported.");
  }

  private PrivateKey implGeneratePrivate(KeySpec keySpec) throws GeneralSecurityException {
    if (keySpec instanceof PKCS8EncodedKeySpec) {
      PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec) keySpec;
      return new ECPrivateKeyImpl(pkcsSpec.getEncoded());
    } else if (keySpec instanceof ECPrivateKeySpec) {
      ECPrivateKeySpec ecSpec = (ECPrivateKeySpec) keySpec;
      return new ECPrivateKeyImpl(ecSpec.getS(), ecSpec.getParams());
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
    if (key instanceof ECPublicKey) {
      ECPublicKey ecKey = (ECPublicKey) key;
      if (ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
        return (T) new ECPublicKeySpec(ecKey.getW(), ecKey.getParams());
      } else if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
        return (T) new X509EncodedKeySpec(key.getEncoded());
      }
      throw new InvalidKeySpecException("KeySpec must be ECPublicKeySpec or X509EncodedKeySpec for EC public keys");
    } else if (key instanceof ECPrivateKey) {
      if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
        return (T) new PKCS8EncodedKeySpec(key.getEncoded());
      } else if (ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
        ECPrivateKey ecKey = (ECPrivateKey) key;
        return (T) new ECPrivateKeySpec(ecKey.getS(), ecKey.getParams());
      }
      throw new InvalidKeySpecException("KeySpec must be ECPrivateKeySpec or PKCS8EncodedKeySpec for EC private keys");
    }
    throw new InvalidKeySpecException("Neither public nor private key");
  }
}
