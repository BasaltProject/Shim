/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class AlgorithmParameters {
  private Provider provider;
  private AlgorithmParametersSpi paramSpi;
  private String algorithm;
  private boolean initialized = false;

  protected AlgorithmParameters(AlgorithmParametersSpi paramSpi, Provider provider, String algorithm) {
    this.paramSpi = paramSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm() {
    return this.algorithm;
  }

  public static AlgorithmParameters getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("AlgorithmParameters", AlgorithmParametersSpi.class,
        algorithm);
    return new AlgorithmParameters((AlgorithmParametersSpi) instance.impl, instance.provider, algorithm);
  }

  public static AlgorithmParameters getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("AlgorithmParameters", AlgorithmParametersSpi.class,
        algorithm, provider);
    return new AlgorithmParameters((AlgorithmParametersSpi) instance.impl, instance.provider, algorithm);
  }

  public static AlgorithmParameters getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("AlgorithmParameters", AlgorithmParametersSpi.class,
        algorithm, provider);
    return new AlgorithmParameters((AlgorithmParametersSpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final void init(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (this.initialized) {
      throw new InvalidParameterSpecException("already initialized");
    }
    paramSpi.engineInit(paramSpec);
    this.initialized = true;
  }

  public final void init(byte[] params) throws IOException {
    if (this.initialized) {
      throw new IOException("already initialized");
    }
    paramSpi.engineInit(params);
    this.initialized = true;
  }

  public final void init(byte[] params, String format) throws IOException {
    if (this.initialized) {
      throw new IOException("already initialized");
    }
    paramSpi.engineInit(params, format);
    this.initialized = true;
  }

  public final <T extends AlgorithmParameterSpec> T getParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (this.initialized == false) {
      throw new InvalidParameterSpecException("not initialized");
    }
    return paramSpi.engineGetParameterSpec(paramSpec);
  }

  public final byte[] getEncoded() throws IOException {
    if (this.initialized == false) {
      throw new IOException("not initialized");
    }
    return paramSpi.engineGetEncoded();
  }

  public final byte[] getEncoded(String format) throws IOException {
    if (this.initialized == false) {
      throw new IOException("not initialized");
    }
    return paramSpi.engineGetEncoded(format);
  }

  public final String toString() {
    if (this.initialized == false) {
      return null;
    }
    return paramSpi.engineToString();
  }
}
