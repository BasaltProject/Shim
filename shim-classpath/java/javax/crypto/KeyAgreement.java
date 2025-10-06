/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class KeyAgreement {
  private Provider provider;
  private KeyAgreementSpi spi;
  private final String algorithm;
  private Service firstService;
  private Iterator<Service> serviceIterator;
  private final Object lock;

  protected KeyAgreement(KeyAgreementSpi keyAgreeSpi, Provider provider, String algorithm) {
    this.spi = keyAgreeSpi;
    this.provider = provider;
    this.algorithm = algorithm;
    lock = null;
  }

  private KeyAgreement(Service s, Iterator<Service> t, String algorithm) {
    firstService = s;
    serviceIterator = t;
    this.algorithm = algorithm;
    lock = new Object();
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public static final KeyAgreement getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Iterator<Service> t = GetInstance.getServices("KeyAgreement", algorithm);
    if (t.hasNext()) {
      return new KeyAgreement(t.next(), t, algorithm);
    }
    throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available");
  }

  public static final KeyAgreement getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("KeyAgreement", KeyAgreementSpi.class, algorithm,
        provider);
    return new KeyAgreement((KeyAgreementSpi) instance.impl, instance.provider, algorithm);
  }

  public static final KeyAgreement getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("KeyAgreement", KeyAgreementSpi.class, algorithm,
        provider);
    return new KeyAgreement((KeyAgreementSpi) instance.impl, instance.provider, algorithm);
  }

  void chooseFirstProvider() {
    if (spi != null) {
      return;
    }
    synchronized (lock) {
      if (spi != null) {
        return;
      }
      Exception lastException = null;
      while ((firstService != null) || serviceIterator.hasNext()) {
        Service s;
        if (firstService != null) {
          s = firstService;
          firstService = null;
        } else {
          s = serviceIterator.next();
        }
        try {
          Object obj = s.newInstance(null);
          if (!(obj instanceof KeyAgreementSpi)) {
            continue;
          }
          spi = (KeyAgreementSpi) obj;
          provider = s.getProvider();
          firstService = null;
          serviceIterator = null;
          return;
        } catch (Exception e) {
          lastException = e;
        }
      }
      ProviderException e = new ProviderException("Could not construct KeyAgreementSpi instance");
      if (lastException != null) {
        e.initCause(lastException);
      }
      throw e;
    }
  }

  private static final int I_NO_PARAMS = 1;
  private static final int I_PARAMS = 2;

  private void implInit(KeyAgreementSpi spi, int type, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    if (type == I_NO_PARAMS) {
      spi.engineInit(key, random);
    } else {
      spi.engineInit(key, params, random);
    }
  }

  private void chooseProvider(int type, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    synchronized (lock) {
      if (spi != null) {
        implInit(spi, type, key, params, random);
        return;
      }
      Exception lastException = null;
      while ((firstService != null) || serviceIterator.hasNext()) {
        Service s;
        if (firstService != null) {
          s = firstService;
          firstService = null;
        } else {
          s = serviceIterator.next();
        }
        if (!s.supportsParameter(key)) {
          continue;
        }
        try {
          KeyAgreementSpi spi = (KeyAgreementSpi) s.newInstance(null);
          implInit(spi, type, key, params, random);
          provider = s.getProvider();
          this.spi = spi;
          firstService = null;
          serviceIterator = null;
          return;
        } catch (Exception e) {
          if (lastException == null) {
            lastException = e;
          }
        }
      }
      if (lastException instanceof InvalidKeyException) {
        throw (InvalidKeyException) lastException;
      }
      if (lastException instanceof InvalidAlgorithmParameterException) {
        throw (InvalidAlgorithmParameterException) lastException;
      }
      if (lastException instanceof RuntimeException) {
        throw (RuntimeException) lastException;
      }
      String kName = (key != null) ? key.getClass().getName() : "(null)";
      throw new InvalidKeyException("No installed provider supports this key: " + kName, lastException);
    }
  }

  public final Provider getProvider() {
    chooseFirstProvider();
    return this.provider;
  }

  public final void init(Key key) throws InvalidKeyException {
    init(key, new SecureRandom());
  }

  public final void init(Key key, SecureRandom random) throws InvalidKeyException {
    if (spi != null) {
      spi.engineInit(key, random);
    } else {
      try {
        chooseProvider(I_NO_PARAMS, key, null, random);
      } catch (InvalidAlgorithmParameterException e) {
        throw new InvalidKeyException(e);
      }
    }
  }

  public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    init(key, params, new SecureRandom());
  }

  public final void init(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    if (spi != null) {
      spi.engineInit(key, params, random);
    } else {
      chooseProvider(I_PARAMS, key, params, random);
    }
  }

  public final Key doPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
    chooseFirstProvider();
    return spi.engineDoPhase(key, lastPhase);
  }

  public final byte[] generateSecret() throws IllegalStateException {
    chooseFirstProvider();
    return spi.engineGenerateSecret();
  }

  public final int generateSecret(byte[] sharedSecret, int offset) throws IllegalStateException,
      ShortBufferException {
    chooseFirstProvider();
    return spi.engineGenerateSecret(sharedSecret, offset);
  }

  public final SecretKey generateSecret(String algorithm) throws IllegalStateException, NoSuchAlgorithmException,
      InvalidKeyException {
    chooseFirstProvider();
    return spi.engineGenerateSecret(algorithm);
  }
}
