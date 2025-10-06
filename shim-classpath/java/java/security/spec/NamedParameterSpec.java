/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class NamedParameterSpec implements AlgorithmParameterSpec {
  public static final NamedParameterSpec X25519 = new NamedParameterSpec("X25519");
  public static final NamedParameterSpec X448 = new NamedParameterSpec("X448");
  public static final NamedParameterSpec ED25519 = new NamedParameterSpec("Ed25519");
  public static final NamedParameterSpec ED448 = new NamedParameterSpec("Ed448");
  public static final NamedParameterSpec ML_DSA_44 = new NamedParameterSpec("ML-DSA-44");
  public static final NamedParameterSpec ML_DSA_65 = new NamedParameterSpec("ML-DSA-65");
  public static final NamedParameterSpec ML_DSA_87 = new NamedParameterSpec("ML-DSA-87");
  public static final NamedParameterSpec ML_KEM_512 = new NamedParameterSpec("ML-KEM-512");
  public static final NamedParameterSpec ML_KEM_768 = new NamedParameterSpec("ML-KEM-768");
  public static final NamedParameterSpec ML_KEM_1024 = new NamedParameterSpec("ML-KEM-1024");

  private final String name;

  public NamedParameterSpec(String stdName) {
    java.util.Objects.requireNonNull(stdName, "stdName must not be null");
    this.name = stdName;
  }

  public String getName() {
    return name;
  }
}
