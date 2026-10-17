/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class CertPathBuilder {
  private static final String CPB_TYPE = "certpathbuilder.type";
  private final CertPathBuilderSpi builderSpi;
  private final Provider provider;
  private final String algorithm;

  protected CertPathBuilder(CertPathBuilderSpi builderSpi, Provider provider, String algorithm) {
    this.builderSpi = builderSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public static CertPathBuilder getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("CertPathBuilder", CertPathBuilderSpi.class, algorithm);
    return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm);
  }

  public static CertPathBuilder getInstance(String algorithm, String provider) throws NoSuchAlgorithmException,
      NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("CertPathBuilder", CertPathBuilderSpi.class, algorithm,
        provider);
    return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm);
  }

  public static CertPathBuilder getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("CertPathBuilder", CertPathBuilderSpi.class, algorithm,
        provider);
    return new CertPathBuilder((CertPathBuilderSpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public final CertPathBuilderResult build(CertPathParameters params) throws CertPathBuilderException,
      InvalidAlgorithmParameterException {
    return builderSpi.engineBuild(params);
  }

  public static final String getDefaultType() {
    String cpbtype = Security.getProperty(CPB_TYPE);
    return (cpbtype == null) ? "PKIX" : cpbtype;
  }

  public final CertPathChecker getRevocationChecker() {
    return builderSpi.engineGetRevocationChecker();
  }
}
