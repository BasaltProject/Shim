/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class AlgorithmParameterGenerator {
  private Provider provider;
  private AlgorithmParameterGeneratorSpi paramGenSpi;
  private String algorithm;

  protected AlgorithmParameterGenerator(AlgorithmParameterGeneratorSpi paramGenSpi, Provider provider,
      String algorithm) {
    this.paramGenSpi = paramGenSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public static AlgorithmParameterGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("AlgorithmParameterGenerator",
        AlgorithmParameterGeneratorSpi.class, algorithm);
    return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) instance.impl, instance.provider,
        algorithm);
  }

  public static AlgorithmParameterGenerator getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("AlgorithmParameterGenerator",
        AlgorithmParameterGeneratorSpi.class, algorithm, provider);
    return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) instance.impl, instance.provider,
        algorithm);
  }

  public static AlgorithmParameterGenerator getInstance(String algorithm, Provider provider)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("AlgorithmParameterGenerator",
        AlgorithmParameterGeneratorSpi.class, algorithm, provider);
    return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) instance.impl, instance.provider,
        algorithm);
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final void init(int size) {
    paramGenSpi.engineInit(size, new SecureRandom());
  }

  public final void init(int size, SecureRandom random) {
    paramGenSpi.engineInit(size, random);
  }

  public final void init(AlgorithmParameterSpec genParamSpec) throws InvalidAlgorithmParameterException {
    paramGenSpi.engineInit(genParamSpec, new SecureRandom());
  }

  public final void init(AlgorithmParameterSpec genParamSpec, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    paramGenSpi.engineInit(genParamSpec, random);
  }

  public final AlgorithmParameters generateParameters() {
    return paramGenSpi.engineGenerateParameters();
  }
}
