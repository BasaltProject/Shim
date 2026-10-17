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

public class CertPathValidator {
  private static final String CPV_TYPE = "certpathvalidator.type";
  private final CertPathValidatorSpi validatorSpi;
  private final Provider provider;
  private final String algorithm;

  protected CertPathValidator(CertPathValidatorSpi validatorSpi, Provider provider, String algorithm) {
    this.validatorSpi = validatorSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public static CertPathValidator getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("CertPathValidator", CertPathValidatorSpi.class,
        algorithm);
    return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm);
  }

  public static CertPathValidator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException,
      NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("CertPathValidator", CertPathValidatorSpi.class,
        algorithm, provider);
    return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm);
  }

  public static CertPathValidator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("CertPathValidator", CertPathValidatorSpi.class,
        algorithm, provider);
    return new CertPathValidator((CertPathValidatorSpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public final CertPathValidatorResult validate(CertPath certPath, CertPathParameters params)
      throws CertPathValidatorException, InvalidAlgorithmParameterException {
    return validatorSpi.engineValidate(certPath, params);
  }

  public static final String getDefaultType() {
    String cpvtype = Security.getProperty(CPV_TYPE);
    return (cpvtype == null) ? "PKIX" : cpvtype;
  }

  public final CertPathChecker getRevocationChecker() {
    return validatorSpi.engineGetRevocationChecker();
  }
}
