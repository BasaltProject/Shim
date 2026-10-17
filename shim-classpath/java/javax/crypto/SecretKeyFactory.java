/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class SecretKeyFactory {
  private Provider provider;
  private final String algorithm;
  private volatile SecretKeyFactorySpi spi;
  private final Object lock = new Object();
  private Iterator<Service> serviceIterator;

  protected SecretKeyFactory(SecretKeyFactorySpi keyFacSpi, Provider provider, String algorithm) {
    this.spi = keyFacSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  private SecretKeyFactory(String algorithm) throws NoSuchAlgorithmException {
    this.algorithm = algorithm;
    List<Service> list = new ArrayList<Service>();
    Iterator<Service> it = GetInstance.getServices("SecretKeyFactory", algorithm);
    while (it.hasNext()) {
      list.add(it.next());
    }
    serviceIterator = list.iterator();
    if (nextSpi(null) == null) {
      throw new NoSuchAlgorithmException(algorithm + " SecretKeyFactory not available");
    }
  }

  public static final SecretKeyFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    return new SecretKeyFactory(algorithm);
  }

  public static final SecretKeyFactory getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("SecretKeyFactory", SecretKeyFactorySpi.class,
        algorithm, provider);
    return new SecretKeyFactory((SecretKeyFactorySpi) instance.impl, instance.provider, algorithm);
  }

  public static final SecretKeyFactory getInstance(String algorithm, Provider provider)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("SecretKeyFactory", SecretKeyFactorySpi.class,
        algorithm, provider);
    return new SecretKeyFactory((SecretKeyFactorySpi) instance.impl, instance.provider, algorithm);
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

  private SecretKeyFactorySpi nextSpi(SecretKeyFactorySpi oldSpi) {
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
          if (!(obj instanceof SecretKeyFactorySpi)) {
            continue;
          }
          SecretKeyFactorySpi spi = (SecretKeyFactorySpi) obj;
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

  public final SecretKey generateSecret(KeySpec keySpec) throws InvalidKeySpecException {
    if (serviceIterator == null) {
      return spi.engineGenerateSecret(keySpec);
    }
    Exception failure = null;
    SecretKeyFactorySpi mySpi = spi;
    do {
      try {
        return mySpi.engineGenerateSecret(keySpec);
      } catch (Exception e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi);
      }
    } while (mySpi != null);
    if (failure instanceof InvalidKeySpecException) {
      throw (InvalidKeySpecException) failure;
    }
    throw new InvalidKeySpecException("Could not generate secret key", failure);
  }

  public final KeySpec getKeySpec(SecretKey key, Class<?> keySpec) throws InvalidKeySpecException {
    if (serviceIterator == null) {
      return spi.engineGetKeySpec(key, keySpec);
    }
    Exception failure = null;
    SecretKeyFactorySpi mySpi = spi;
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
    if (failure instanceof InvalidKeySpecException) {
      throw (InvalidKeySpecException) failure;
    }
    throw new InvalidKeySpecException("Could not get key spec", failure);
  }

  public final SecretKey translateKey(SecretKey key) throws InvalidKeyException {
    if (serviceIterator == null) {
      return spi.engineTranslateKey(key);
    }
    Exception failure = null;
    SecretKeyFactorySpi mySpi = spi;
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
    if (failure instanceof InvalidKeyException) {
      throw (InvalidKeyException) failure;
    }
    throw new InvalidKeyException("Could not translate key", failure);
  }
}
