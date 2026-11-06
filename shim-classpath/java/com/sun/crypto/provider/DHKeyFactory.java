/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;

public final class DHKeyFactory extends KeyFactorySpi {
  public DHKeyFactory() {
  }

  private static InvalidKeySpecException bad() {
    return new InvalidKeySpecException("Inappropriate key specification");
  }

  @Override
  protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      if (keySpec instanceof DHPublicKeySpec) {
        DHPublicKeySpec s = (DHPublicKeySpec) keySpec;
        return new DHPublicKey(s.getY(), s.getP(), s.getG());
      }
      if (keySpec instanceof X509EncodedKeySpec) {
        return DHPublicKey.decode(((X509EncodedKeySpec) keySpec).getEncoded());
      }
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException("Inappropriate key specification", e);
    }
    throw bad();
  }

  @Override
  protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      if (keySpec instanceof DHPrivateKeySpec) {
        DHPrivateKeySpec s = (DHPrivateKeySpec) keySpec;
        return new DHPrivateKey(s.getX(), s.getP(), s.getG());
      }
      if (keySpec instanceof PKCS8EncodedKeySpec) {
        return DHPrivateKey.decode(((PKCS8EncodedKeySpec) keySpec).getEncoded());
      }
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException("Inappropriate key specification", e);
    }
    throw bad();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
    if (key instanceof javax.crypto.interfaces.DHPublicKey) {
      javax.crypto.interfaces.DHPublicKey k = (javax.crypto.interfaces.DHPublicKey) key;
      if (keySpec.isAssignableFrom(DHPublicKeySpec.class)) {
        DHParameterSpec ps = k.getParams();
        return (T) new DHPublicKeySpec(k.getY(), ps.getP(), ps.getG());
      }
      if (keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
        return (T) new X509EncodedKeySpec(key.getEncoded());
      }
      throw bad();
    }
    if (key instanceof javax.crypto.interfaces.DHPrivateKey) {
      javax.crypto.interfaces.DHPrivateKey k = (javax.crypto.interfaces.DHPrivateKey) key;
      if (keySpec.isAssignableFrom(DHPrivateKeySpec.class)) {
        DHParameterSpec ps = k.getParams();
        return (T) new DHPrivateKeySpec(k.getX(), ps.getP(), ps.getG());
      }
      if (keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class)) {
        return (T) new PKCS8EncodedKeySpec(key.getEncoded());
      }
      throw bad();
    }
    throw new InvalidKeySpecException("Inappropriate key type");
  }

  @Override
  protected Key engineTranslateKey(Key key) throws InvalidKeyException {
    try {
      if (key instanceof javax.crypto.interfaces.DHPublicKey) {
        if (key instanceof DHPublicKey) {
          return key;
        }
        javax.crypto.interfaces.DHPublicKey k = (javax.crypto.interfaces.DHPublicKey) key;
        DHParameterSpec ps = k.getParams();
        return engineGeneratePublic(new DHPublicKeySpec(k.getY(), ps.getP(), ps.getG()));
      }
      if (key instanceof javax.crypto.interfaces.DHPrivateKey) {
        if (key instanceof DHPrivateKey) {
          return key;
        }
        javax.crypto.interfaces.DHPrivateKey k = (javax.crypto.interfaces.DHPrivateKey) key;
        DHParameterSpec ps = k.getParams();
        return engineGeneratePrivate(new DHPrivateKeySpec(k.getX(), ps.getP(), ps.getG()));
      }
    } catch (InvalidKeySpecException e) {
      throw new InvalidKeyException("Cannot translate key", e);
    }
    throw new InvalidKeyException("Wrong algorithm type");
  }
}
