/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.security.Provider.Service;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class KeyFactory {
  private final String algorithm;
  private Provider provider;
  private volatile KeyFactorySpi spi;
  private final Object lock = new Object();
  private Iterator<Service> serviceIterator;

  protected KeyFactory(KeyFactorySpi keyFacSpi, Provider provider, String algorithm) {
    this.spi = keyFacSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  private KeyFactory(String algorithm) throws NoSuchAlgorithmException {
    this.algorithm = algorithm;
    List<Service> list = new java.util.ArrayList<Service>();
    Iterator<Service> it = GetInstance.getServices("KeyFactory", algorithm);
    while (it.hasNext()) {
      list.add(it.next());
    }
    serviceIterator = list.iterator();
    if (nextSpi(null) == null) {
      throw new NoSuchAlgorithmException(algorithm + " KeyFactory not available");
    }
  }

  public static KeyFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    return new KeyFactory(algorithm);
  }

  public static KeyFactory getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("KeyFactory", KeyFactorySpi.class, algorithm, provider);
    return new KeyFactory((KeyFactorySpi) instance.impl, instance.provider, algorithm);
  }

  public static KeyFactory getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("KeyFactory", KeyFactorySpi.class, algorithm, provider);
    return new KeyFactory((KeyFactorySpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    synchronized (lock) {
      serviceIterator = null;
      return provider;
    }
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  private KeyFactorySpi nextSpi(KeyFactorySpi oldSpi) {
    synchronized (lock) {
      if ((oldSpi != null) && (oldSpi != spi)) {
        return spi;
      }
      if (serviceIterator == null) {
        return null;
      }
      while (serviceIterator.hasNext()) {
        Service s = serviceIterator.next();
        try {
          Object obj = s.newInstance(null);
          if (!(obj instanceof KeyFactorySpi)) {
            continue;
          }
          KeyFactorySpi spi = (KeyFactorySpi) obj;
          provider = s.getProvider();
          this.spi = spi;
          return spi;
        } catch (NoSuchAlgorithmException e) {
        }
      }
      serviceIterator = null;
      return null;
    }
  }

  public final PublicKey generatePublic(KeySpec keySpec) throws InvalidKeySpecException {
    if (serviceIterator == null) {
      return spi.engineGeneratePublic(keySpec);
    }
    Exception failure = null;
    KeyFactorySpi mySpi = spi;
    do {
      try {
        return mySpi.engineGeneratePublic(keySpec);
      } catch (Exception e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi);
      }
    } while (mySpi != null);
    if (failure instanceof RuntimeException) {
      throw (RuntimeException) failure;
    }
    if (failure instanceof InvalidKeySpecException) {
      throw (InvalidKeySpecException) failure;
    }
    throw new InvalidKeySpecException("Could not generate public key", failure);
  }

  public final PrivateKey generatePrivate(KeySpec keySpec) throws InvalidKeySpecException {
    if (serviceIterator == null) {
      return spi.engineGeneratePrivate(keySpec);
    }
    Exception failure = null;
    KeyFactorySpi mySpi = spi;
    do {
      try {
        return mySpi.engineGeneratePrivate(keySpec);
      } catch (Exception e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi);
      }
    } while (mySpi != null);
    if (failure instanceof RuntimeException) {
      throw (RuntimeException) failure;
    }
    if (failure instanceof InvalidKeySpecException) {
      throw (InvalidKeySpecException) failure;
    }
    throw new InvalidKeySpecException("Could not generate private key", failure);
  }

  public final <T extends KeySpec> T getKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
    if (serviceIterator == null) {
      return spi.engineGetKeySpec(key, keySpec);
    }
    Exception failure = null;
    KeyFactorySpi mySpi = spi;
    do {
      try {
        return mySpi.engineGetKeySpec(key, keySpec);
      } catch (Exception e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi);
      }
    } while (mySpi != null);
    if (failure instanceof RuntimeException) {
      throw (RuntimeException) failure;
    }
    if (failure instanceof InvalidKeySpecException) {
      throw (InvalidKeySpecException) failure;
    }
    throw new InvalidKeySpecException("Could not get key spec", failure);
  }

  public final Key translateKey(Key key) throws InvalidKeyException {
    if (serviceIterator == null) {
      return spi.engineTranslateKey(key);
    }
    Exception failure = null;
    KeyFactorySpi mySpi = spi;
    do {
      try {
        return mySpi.engineTranslateKey(key);
      } catch (Exception e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi);
      }
    } while (mySpi != null);
    if (failure instanceof RuntimeException) {
      throw (RuntimeException) failure;
    }
    if (failure instanceof InvalidKeyException) {
      throw (InvalidKeyException) failure;
    }
    throw new InvalidKeyException("Could not translate key", failure);
  }
}
