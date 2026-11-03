/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class DSAKeyFactory extends KeyFactorySpi {
  public DSAKeyFactory() {
  }

  @Override
  protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      if (keySpec instanceof DSAPublicKeySpec) {
        DSAPublicKeySpec s = (DSAPublicKeySpec) keySpec;
        return new DSAKeys.Pub(s.getY(), s.getP(), s.getQ(), s.getG());
      } else if (keySpec instanceof X509EncodedKeySpec) {
        return new DSAKeys.Pub(((X509EncodedKeySpec) keySpec).getEncoded());
      }
      throw new InvalidKeySpecException("Only DSAPublicKeySpec and X509EncodedKeySpec supported for DSA public keys");
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException(e.getMessage(), e);
    }
  }

  @Override
  protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      if (keySpec instanceof DSAPrivateKeySpec) {
        DSAPrivateKeySpec s = (DSAPrivateKeySpec) keySpec;
        return new DSAKeys.Priv(s.getX(), s.getP(), s.getQ(), s.getG());
      } else if (keySpec instanceof PKCS8EncodedKeySpec) {
        return new DSAKeys.Priv(((PKCS8EncodedKeySpec) keySpec).getEncoded());
      }
      throw new InvalidKeySpecException("Only DSAPrivateKeySpec and PKCS8EncodedKeySpec supported for DSA private keys");
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException(e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
    try {
      if (key instanceof DSAPublicKey) {
        DSAPublicKey k = (DSAPublicKey) key;
        if (DSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
          DSAParams p = k.getParams();
          return (T) new DSAPublicKeySpec(k.getY(), p.getP(), p.getQ(), p.getG());
        } else if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
          return (T) new X509EncodedKeySpec(key.getEncoded());
        }
        throw new InvalidKeySpecException("Inappropriate key specification");
      } else if (key instanceof DSAPrivateKey) {
        DSAPrivateKey k = (DSAPrivateKey) key;
        if (DSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
          DSAParams p = k.getParams();
          return (T) new DSAPrivateKeySpec(k.getX(), p.getP(), p.getQ(), p.getG());
        } else if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
          return (T) new PKCS8EncodedKeySpec(key.getEncoded());
        }
        throw new InvalidKeySpecException("Inappropriate key specification");
      }
      throw new InvalidKeySpecException("Inappropriate key type");
    } catch (NullPointerException e) {
      throw new InvalidKeySpecException(e);
    }
  }

  @Override
  protected Key engineTranslateKey(Key key) throws InvalidKeyException {
    try {
      if (key instanceof DSAPublicKey) {
        DSAPublicKey k = (DSAPublicKey) key;
        DSAParams p = k.getParams();
        return new DSAKeys.Pub(k.getY(), p.getP(), p.getQ(), p.getG());
      } else if (key instanceof DSAPrivateKey) {
        DSAPrivateKey k = (DSAPrivateKey) key;
        DSAParams p = k.getParams();
        return new DSAKeys.Priv(k.getX(), p.getP(), p.getQ(), p.getG());
      }
    } catch (RuntimeException e) {
      throw new InvalidKeyException("Cannot translate key: " + e.getMessage());
    }
    throw new InvalidKeyException("Key must be a DSA key");
  }
}
