/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.Objects;
import java.util.Random;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;

public class SecureRandom extends Random {
  private static final long serialVersionUID = 4940670005562187L;

  private Provider provider = null;
  private SecureRandomSpi secureRandomSpi = null;
  private final String threadSafe = null;
  private String algorithm;
  private final boolean threadSafeFlag;
  private final Object lock = new Object();

  private static volatile SecureRandom seedGenerator;

  public SecureRandom() {
    super(0);
    threadSafeFlag = false;
    getDefaultPRNG(false, null);
  }

  public SecureRandom(byte[] seed) {
    super(0);
    threadSafeFlag = false;
    getDefaultPRNG(true, seed);
  }

  private void getDefaultPRNG(boolean setSeed, byte[] seed) {
    Provider.Service prngService = null;
    String prngAlgorithm = null;
    for (Provider p : Providers.list()) {
      for (Provider.Service s : p.getServices()) {
        if (s.getType().equals("SecureRandom")) {
          prngService = s;
          prngAlgorithm = s.getAlgorithm();
          break;
        }
      }
      if (prngService != null) {
        break;
      }
    }
    if (prngService == null) {
      prngAlgorithm = "SHA1PRNG";
      this.secureRandomSpi = new sun.security.provider.SecureRandom();
      this.provider = Providers.named("SUN");
    } else {
      try {
        this.secureRandomSpi = (SecureRandomSpi) prngService.newInstance(null);
        this.provider = prngService.getProvider();
      } catch (NoSuchAlgorithmException nsae) {
        throw new RuntimeException(nsae);
      }
    }
    if (setSeed) {
      this.secureRandomSpi.engineSetSeed(seed);
    }
    this.algorithm = prngAlgorithm;
  }

  protected SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider) {
    this(secureRandomSpi, provider, null);
  }

  private SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider, String algorithm) {
    super(0);
    this.secureRandomSpi = secureRandomSpi;
    this.provider = provider;
    this.algorithm = algorithm;
    this.threadSafeFlag = false;
  }

  public static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm,
        (Object) null);
    return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm);
  }

  public static SecureRandom getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm,
        (Object) null, provider);
    return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm);
  }

  public static SecureRandom getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm,
        (Object) null, provider);
    return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm);
  }

  public static SecureRandom getInstance(String algorithm, SecureRandomParameters params)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    if (params == null) {
      throw new IllegalArgumentException("params cannot be null");
    }
    GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, params);
    return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm);
  }

  public static SecureRandom getInstance(String algorithm, SecureRandomParameters params, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    if (params == null) {
      throw new IllegalArgumentException("params cannot be null");
    }
    GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, params,
        provider);
    return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm);
  }

  public static SecureRandom getInstance(String algorithm, SecureRandomParameters params, Provider provider)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    if (params == null) {
      throw new IllegalArgumentException("params cannot be null");
    }
    GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", SecureRandomSpi.class, algorithm, params,
        provider);
    return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    return provider;
  }

  public String getAlgorithm() {
    return algorithm != null ? algorithm : "unknown";
  }

  @Override
  public String toString() {
    return secureRandomSpi.toString();
  }

  public SecureRandomParameters getParameters() {
    return secureRandomSpi.engineGetParameters();
  }

  public void setSeed(byte[] seed) {
    synchronized (lock) {
      secureRandomSpi.engineSetSeed(seed);
    }
  }

  @Override
  public void setSeed(long seed) {
    if (seed != 0) {
      setSeed(longToByteArray(seed));
    }
  }

  @Override
  public void nextBytes(byte[] bytes) {
    synchronized (lock) {
      secureRandomSpi.engineNextBytes(bytes);
    }
  }

  public void nextBytes(byte[] bytes, SecureRandomParameters params) {
    if (params == null) {
      throw new IllegalArgumentException("params cannot be null");
    }
    synchronized (lock) {
      secureRandomSpi.engineNextBytes(Objects.requireNonNull(bytes), params);
    }
  }

  @Override
  protected final int next(int numBits) {
    int numBytes = (numBits + 7) / 8;
    byte[] b = new byte[numBytes];
    int next = 0;
    nextBytes(b);
    for (int i = 0; i < numBytes; i++) {
      next = (next << 8) + (b[i] & 0xFF);
    }
    return next >>> (numBytes * 8 - numBits);
  }

  public static byte[] getSeed(int numBytes) {
    SecureRandom gen = seedGenerator;
    if (gen == null) {
      gen = new SecureRandom();
      seedGenerator = gen;
    }
    return gen.generateSeed(numBytes);
  }

  public byte[] generateSeed(int numBytes) {
    if (numBytes < 0) {
      throw new IllegalArgumentException("numBytes cannot be negative");
    }
    return secureRandomSpi.engineGenerateSeed(numBytes);
  }

  private static byte[] longToByteArray(long l) {
    byte[] retVal = new byte[8];
    for (int i = 0; i < 8; i++) {
      retVal[i] = (byte) l;
      l >>= 8;
    }
    return retVal;
  }

  public static SecureRandom getInstanceStrong() throws NoSuchAlgorithmException {
    String property = Security.getProperty("securerandom.strongAlgorithms");
    if (property == null || property.isEmpty()) {
      throw new NoSuchAlgorithmException("Null/empty securerandom.strongAlgorithms Security Property");
    }
    String remainder = property;
    while (remainder != null) {
      String algName;
      String prov = null;
      int comma = remainder.indexOf(',');
      String token = comma < 0 ? remainder : remainder.substring(0, comma);
      remainder = comma < 0 ? null : remainder.substring(comma + 1);
      token = token.trim();
      int colon = token.indexOf(':');
      if (colon < 0) {
        algName = token;
      } else {
        algName = token.substring(0, colon).trim();
        prov = token.substring(colon + 1).trim();
      }
      try {
        if (prov == null) {
          return SecureRandom.getInstance(algName);
        }
        return SecureRandom.getInstance(algName, prov);
      } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      }
    }
    throw new NoSuchAlgorithmException("No strong SecureRandom impls available: " + property);
  }

  public void reseed() {
    synchronized (lock) {
      secureRandomSpi.engineReseed(null);
    }
  }

  public void reseed(SecureRandomParameters params) {
    if (params == null) {
      throw new IllegalArgumentException("params cannot be null");
    }
    synchronized (lock) {
      secureRandomSpi.engineReseed(params);
    }
  }
}
