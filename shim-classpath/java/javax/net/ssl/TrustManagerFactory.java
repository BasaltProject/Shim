/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class TrustManagerFactory {
  private final Provider provider;
  private final TrustManagerFactorySpi factorySpi;
  private final String algorithm;

  public static final String getDefaultAlgorithm() {
    String type = Security.getProperty("ssl.TrustManagerFactory.algorithm");
    if (type == null) {
      type = "SunX509";
    }
    return type;
  }

  protected TrustManagerFactory(TrustManagerFactorySpi factorySpi, Provider provider, String algorithm) {
    this.factorySpi = factorySpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public static final TrustManagerFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("TrustManagerFactory", TrustManagerFactorySpi.class,
        algorithm);
    return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm);
  }

  public static final TrustManagerFactory getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("TrustManagerFactory", TrustManagerFactorySpi.class,
        algorithm, provider);
    return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm);
  }

  public static final TrustManagerFactory getInstance(String algorithm, Provider provider)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("TrustManagerFactory", TrustManagerFactorySpi.class,
        algorithm, provider);
    return new TrustManagerFactory((TrustManagerFactorySpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final void init(KeyStore ks) throws KeyStoreException {
    factorySpi.engineInit(ks);
  }

  public final void init(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
    factorySpi.engineInit(spec);
  }

  public final TrustManager[] getTrustManagers() {
    return factorySpi.engineGetTrustManagers();
  }
}
