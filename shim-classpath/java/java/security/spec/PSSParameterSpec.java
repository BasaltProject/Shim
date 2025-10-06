/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

public class PSSParameterSpec implements AlgorithmParameterSpec {
  private final String mdName;
  private final String mgfName;
  private final AlgorithmParameterSpec mgfSpec;
  private final int saltLen;
  private final int trailerField;

  public static final int TRAILER_FIELD_BC = 1;

  public static final PSSParameterSpec DEFAULT = new PSSParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, 20,
      TRAILER_FIELD_BC);

  private PSSParameterSpec() {
    this.mdName = "SHA-1";
    this.mgfName = "MGF1";
    this.mgfSpec = MGF1ParameterSpec.SHA1;
    this.saltLen = 20;
    this.trailerField = TRAILER_FIELD_BC;
  }

  public PSSParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec mgfSpec, int saltLen,
      int trailerField) {
    if (mdName == null) {
      throw new NullPointerException("digest algorithm is null");
    }
    if (mgfName == null) {
      throw new NullPointerException("mask generation function algorithm is null");
    }
    if (saltLen < 0) {
      throw new IllegalArgumentException("negative saltLen value: " + saltLen);
    }
    if (trailerField < 0) {
      throw new IllegalArgumentException("negative trailerField: " + trailerField);
    }
    this.mdName = mdName;
    this.mgfName = mgfName;
    this.mgfSpec = mgfSpec;
    this.saltLen = saltLen;
    this.trailerField = trailerField;
  }

  public PSSParameterSpec(int saltLen) {
    if (saltLen < 0) {
      throw new IllegalArgumentException("negative saltLen value: " + saltLen);
    }
    this.mdName = "SHA-1";
    this.mgfName = "MGF1";
    this.mgfSpec = MGF1ParameterSpec.SHA1;
    this.saltLen = saltLen;
    this.trailerField = TRAILER_FIELD_BC;
  }

  public String getDigestAlgorithm() {
    return mdName;
  }

  public String getMGFAlgorithm() {
    return mgfName;
  }

  public AlgorithmParameterSpec getMGFParameters() {
    return mgfSpec;
  }

  public int getSaltLength() {
    return saltLen;
  }

  public int getTrailerField() {
    return trailerField;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PSSParameterSpec[");
    sb.append("hashAlgorithm=").append(mdName);
    sb.append(", maskGenAlgorithm=").append(mgfSpec == null ? mgfName : mgfName + "(" + mgfSpec + ")");
    sb.append(", saltLength=").append(saltLen);
    sb.append(", trailerField=").append(trailerField);
    sb.append("]");
    return sb.toString();
  }
}
