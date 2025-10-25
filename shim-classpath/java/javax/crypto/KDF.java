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
import java.security.spec.AlgorithmParameterSpec;
import java.util.Iterator;
import java.util.Objects;
import sun.security.jca.GetInstance;

public final class KDF {
  private final String algorithm;
  private final KDFParameters kdfParameters;
  private final KDFSpi spi;
  private final Provider provider;

  private KDF(String algorithm, KDFParameters kdfParameters, KDFSpi spi, Provider provider) {
    this.algorithm = algorithm;
    this.kdfParameters = kdfParameters;
    this.spi = spi;
    this.provider = provider;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public KDFParameters getParameters() {
    return kdfParameters;
  }

  public String getProviderName() {
    return provider.getName();
  }

  public static KDF getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    try {
      return getInstance(algorithm, (KDFParameters) null);
    } catch (InvalidAlgorithmParameterException e) {
      throw new NoSuchAlgorithmException("No implementation found for " + algorithm, e);
    }
  }

  public static KDF getInstance(String algorithm, String provider) throws NoSuchAlgorithmException,
      NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Objects.requireNonNull(provider, "null provider name");
    try {
      return getInstance(algorithm, (KDFParameters) null, provider);
    } catch (InvalidAlgorithmParameterException e) {
      throw new NoSuchAlgorithmException("No implementation found for " + algorithm, e);
    }
  }

  public static KDF getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Objects.requireNonNull(provider, "null provider");
    try {
      return getInstance(algorithm, (KDFParameters) null, provider);
    } catch (InvalidAlgorithmParameterException e) {
      throw new NoSuchAlgorithmException("No implementation found for " + algorithm, e);
    }
  }

  public static KDF getInstance(String algorithm, KDFParameters kdfParameters)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Iterator<Provider.Service> services = GetInstance.getServices("KDF", algorithm);
    return fromServices(algorithm, kdfParameters, services);
  }

  public static KDF getInstance(String algorithm, KDFParameters kdfParameters, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Objects.requireNonNull(provider, "null provider name");
    return fromServices(algorithm, kdfParameters, GetInstance.getServices("KDF", algorithm, provider));
  }

  public static KDF getInstance(String algorithm, KDFParameters kdfParameters, Provider provider)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    Objects.requireNonNull(provider, "null provider");
    return fromServices(algorithm, kdfParameters, GetInstance.getServices("KDF", algorithm, provider));
  }

  private static KDF fromServices(String algorithm, KDFParameters kdfParameters, Iterator<Provider.Service> services)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    Exception failure = null;
    while (services.hasNext()) {
      Provider.Service s = services.next();
      try {
        Object impl = s.newInstance(kdfParameters);
        if (impl instanceof KDFSpi) {
          return new KDF(algorithm, kdfParameters, (KDFSpi) impl, s.getProvider());
        }
      } catch (NoSuchAlgorithmException e) {
        failure = e;
        if (e.getCause() instanceof InvalidAlgorithmParameterException) {
          throw (InvalidAlgorithmParameterException) e.getCause();
        }
      }
    }
    NoSuchAlgorithmException e = new NoSuchAlgorithmException("No available provider supports the specified algorithm");
    if (failure != null) {
      e.initCause(failure);
    }
    throw e;
  }

  public SecretKey deriveKey(String alg, AlgorithmParameterSpec derivationSpec)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
    Objects.requireNonNull(alg, "the algorithm for the SecretKey return value must not be null");
    if (alg.isEmpty()) {
      throw new NoSuchAlgorithmException("the algorithm for the SecretKey return value must not be empty");
    }
    return spi.engineDeriveKey(alg, derivationSpec);
  }

  public byte[] deriveData(AlgorithmParameterSpec derivationSpec) throws InvalidAlgorithmParameterException {
    return spi.engineDeriveData(derivationSpec);
  }
}
