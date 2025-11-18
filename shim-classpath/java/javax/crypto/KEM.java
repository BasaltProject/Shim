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
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import sun.security.jca.GetInstance;

public final class KEM {
  private final String algorithm;
  private final List<Provider.Service> services;

  private KEM(String algorithm, List<Provider.Service> services) {
    this.algorithm = algorithm;
    this.services = services;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public static final class Encapsulated {
    private final SecretKey key;
    private final byte[] encapsulation;
    private final byte[] params;

    public Encapsulated(SecretKey key, byte[] encapsulation, byte[] params) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(encapsulation);
      this.key = key;
      this.encapsulation = encapsulation.clone();
      this.params = params == null ? null : params.clone();
    }

    public SecretKey key() {
      return key;
    }

    public byte[] encapsulation() {
      return encapsulation.clone();
    }

    public byte[] params() {
      return params == null ? null : params.clone();
    }
  }

  public static final class Encapsulator {
    private final KEMSpi.EncapsulatorSpi spi;
    private final Provider provider;

    private Encapsulator(KEMSpi.EncapsulatorSpi spi, Provider provider) {
      this.spi = spi;
      this.provider = provider;
    }

    public Encapsulated encapsulate() {
      return encapsulate(0, secretSize(), "Generic");
    }

    public Encapsulated encapsulate(int from, int to, String algorithm) {
      Objects.checkFromToIndex(from, to, secretSize());
      Objects.requireNonNull(algorithm, "null algorithm");
      return spi.engineEncapsulate(from, to, algorithm);
    }

    public int secretSize() {
      return spi.engineSecretSize();
    }

    public int encapsulationSize() {
      return spi.engineEncapsulationSize();
    }

    public String providerName() {
      return provider.getName();
    }
  }

  public static final class Decapsulator {
    private final KEMSpi.DecapsulatorSpi spi;
    private final Provider provider;

    private Decapsulator(KEMSpi.DecapsulatorSpi spi, Provider provider) {
      this.spi = spi;
      this.provider = provider;
    }

    public SecretKey decapsulate(byte[] encapsulation) throws DecapsulateException {
      Objects.requireNonNull(encapsulation, "null encapsulation");
      return decapsulate(encapsulation, 0, secretSize(), "Generic");
    }

    public SecretKey decapsulate(byte[] encapsulation, int from, int to, String algorithm)
        throws DecapsulateException {
      Objects.requireNonNull(encapsulation, "null encapsulation");
      Objects.requireNonNull(algorithm, "null algorithm");
      Objects.checkFromToIndex(from, to, secretSize());
      return spi.engineDecapsulate(encapsulation, from, to, algorithm);
    }

    public int secretSize() {
      return spi.engineSecretSize();
    }

    public int encapsulationSize() {
      return spi.engineEncapsulationSize();
    }

    public String providerName() {
      return provider.getName();
    }
  }

  public static KEM getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    return of(algorithm, GetInstance.getServices("KEM", algorithm));
  }

  public static KEM getInstance(String algorithm, String provider) throws NoSuchAlgorithmException,
      NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    if (provider == null || provider.isEmpty()) {
      throw new IllegalArgumentException("missing provider");
    }
    return of(algorithm, GetInstance.getServices("KEM", algorithm, provider));
  }

  public static KEM getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    return of(algorithm, GetInstance.getServices("KEM", algorithm, provider));
  }

  private static KEM of(String algorithm, Iterator<Provider.Service> it) throws NoSuchAlgorithmException {
    List<Provider.Service> list = new ArrayList<Provider.Service>();
    while (it.hasNext()) {
      list.add(it.next());
    }
    if (list.isEmpty()) {
      throw new NoSuchAlgorithmException(algorithm + " KEM not available");
    }
    return new KEM(algorithm, list);
  }

  public Encapsulator newEncapsulator(PublicKey publicKey) throws InvalidKeyException {
    return newEncapsulator(publicKey, (SecureRandom) null);
  }

  public Encapsulator newEncapsulator(PublicKey publicKey, SecureRandom secureRandom) throws InvalidKeyException {
    try {
      return newEncapsulator(publicKey, null, secureRandom);
    } catch (InvalidAlgorithmParameterException e) {
      throw new InvalidKeyException(e);
    }
  }

  public Encapsulator newEncapsulator(PublicKey publicKey, AlgorithmParameterSpec spec, SecureRandom secureRandom)
      throws InvalidAlgorithmParameterException, InvalidKeyException {
    Exception last = null;
    for (Provider.Service s : services) {
      if (publicKey != null && !s.supportsParameter(publicKey)) {
        continue;
      }
      try {
        KEMSpi spi = (KEMSpi) s.newInstance(null);
        return new Encapsulator(spi.engineNewEncapsulator(publicKey, spec,
            secureRandom == null ? new SecureRandom() : secureRandom), s.getProvider());
      } catch (NoSuchAlgorithmException e) {
        last = e;
      } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
        last = e;
      }
    }
    fail(last, publicKey);
    return null;
  }

  public Decapsulator newDecapsulator(PrivateKey privateKey) throws InvalidKeyException {
    try {
      return newDecapsulator(privateKey, null);
    } catch (InvalidAlgorithmParameterException e) {
      throw new InvalidKeyException(e);
    }
  }

  public Decapsulator newDecapsulator(PrivateKey privateKey, AlgorithmParameterSpec spec)
      throws InvalidAlgorithmParameterException, InvalidKeyException {
    Exception last = null;
    for (Provider.Service s : services) {
      if (privateKey != null && !s.supportsParameter(privateKey)) {
        continue;
      }
      try {
        KEMSpi spi = (KEMSpi) s.newInstance(null);
        return new Decapsulator(spi.engineNewDecapsulator(privateKey, spec), s.getProvider());
      } catch (NoSuchAlgorithmException e) {
        last = e;
      } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
        last = e;
      }
    }
    fail(last, privateKey);
    return null;
  }

  private static void fail(Exception last, Key key) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    if (last instanceof InvalidAlgorithmParameterException) {
      throw (InvalidAlgorithmParameterException) last;
    }
    if (last instanceof InvalidKeyException) {
      throw (InvalidKeyException) last;
    }
    throw new InvalidKeyException("No installed provider supports this key: "
        + (key == null ? "(null)" : key.getClass().getName()));
  }
}
