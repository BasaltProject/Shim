/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class KeyGenerator {
  private Provider provider;
  private volatile KeyGeneratorSpi spi;
  private final String algorithm;
  private final Object lock = new Object();
  private Iterator<Service> serviceIterator;
  private int initType;
  private int initKeySize;
  private AlgorithmParameterSpec initParams;
  private SecureRandom initRandom;

  private static final int I_NONE = 1;
  private static final int I_RANDOM = 2;
  private static final int I_PARAMS = 3;
  private static final int I_SIZE = 4;

  protected KeyGenerator(KeyGeneratorSpi keyGenSpi, Provider provider, String algorithm) {
    this.spi = keyGenSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  private KeyGenerator(String algorithm) throws NoSuchAlgorithmException {
    this.algorithm = algorithm;
    List<Service> list = new ArrayList<Service>();
    Iterator<Service> it = GetInstance.getServices("KeyGenerator", algorithm);
    while (it.hasNext()) {
      list.add(it.next());
    }
    serviceIterator = list.iterator();
    initType = I_NONE;
    if (nextSpi(null, false) == null) {
      throw new NoSuchAlgorithmException(algorithm + " KeyGenerator not available");
    }
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public static final KeyGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    return new KeyGenerator(algorithm);
  }

  public static final KeyGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException,
      NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("KeyGenerator", KeyGeneratorSpi.class, algorithm,
        provider);
    return new KeyGenerator((KeyGeneratorSpi) instance.impl, instance.provider, algorithm);
  }

  public static final KeyGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("KeyGenerator", KeyGeneratorSpi.class, algorithm,
        provider);
    return new KeyGenerator((KeyGeneratorSpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    synchronized (lock) {
      disableFailover();
      return provider;
    }
  }

  private KeyGeneratorSpi nextSpi(KeyGeneratorSpi oldSpi, boolean reinit) {
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
          Object inst = s.newInstance(null);
          if (!(inst instanceof KeyGeneratorSpi)) {
            continue;
          }
          KeyGeneratorSpi spi = (KeyGeneratorSpi) inst;
          if (reinit) {
            if (initType == I_SIZE) {
              spi.engineInit(initKeySize, initRandom);
            } else if (initType == I_PARAMS) {
              spi.engineInit(initParams, initRandom);
            } else if (initType == I_RANDOM) {
              spi.engineInit(initRandom);
            } else if (initType != I_NONE) {
              throw new AssertionError("KeyGenerator initType: " + initType);
            }
          }
          provider = s.getProvider();
          this.spi = spi;
          return spi;
        } catch (Exception e) {
        }
      }
      disableFailover();
      return null;
    }
  }

  void disableFailover() {
    serviceIterator = null;
    initType = 0;
    initParams = null;
    initRandom = null;
  }

  public final void init(SecureRandom random) {
    if (serviceIterator == null) {
      spi.engineInit(random);
      return;
    }
    RuntimeException failure = null;
    KeyGeneratorSpi mySpi = spi;
    do {
      try {
        mySpi.engineInit(random);
        initType = I_RANDOM;
        initKeySize = 0;
        initParams = null;
        initRandom = random;
        return;
      } catch (RuntimeException e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi, false);
      }
    } while (mySpi != null);
    throw failure;
  }

  public final void init(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    init(params, new SecureRandom());
  }

  public final void init(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (serviceIterator == null) {
      spi.engineInit(params, random);
      return;
    }
    Exception failure = null;
    KeyGeneratorSpi mySpi = spi;
    do {
      try {
        mySpi.engineInit(params, random);
        initType = I_PARAMS;
        initKeySize = 0;
        initParams = params;
        initRandom = random;
        return;
      } catch (Exception e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi, false);
      }
    } while (mySpi != null);
    if (failure instanceof RuntimeException) {
      throw (RuntimeException) failure;
    }
    throw (InvalidAlgorithmParameterException) failure;
  }

  public final void init(int keysize) {
    init(keysize, new SecureRandom());
  }

  public final void init(int keysize, SecureRandom random) {
    if (serviceIterator == null) {
      spi.engineInit(keysize, random);
      return;
    }
    RuntimeException failure = null;
    KeyGeneratorSpi mySpi = spi;
    do {
      try {
        mySpi.engineInit(keysize, random);
        initType = I_SIZE;
        initKeySize = keysize;
        initParams = null;
        initRandom = random;
        return;
      } catch (RuntimeException e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi, false);
      }
    } while (mySpi != null);
    throw failure;
  }

  public final SecretKey generateKey() {
    if (serviceIterator == null) {
      return spi.engineGenerateKey();
    }
    RuntimeException failure = null;
    KeyGeneratorSpi mySpi = spi;
    do {
      try {
        return mySpi.engineGenerateKey();
      } catch (RuntimeException e) {
        if (failure == null) {
          failure = e;
        }
        mySpi = nextSpi(mySpi, true);
      }
    } while (mySpi != null);
    throw failure;
  }
}
