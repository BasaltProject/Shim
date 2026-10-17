/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;

public class OAEPParameterSpec implements AlgorithmParameterSpec {
  private final String mdName;
  private final String mgfName;
  private final AlgorithmParameterSpec mgfSpec;
  private final PSource pSrc;

  public static final OAEPParameterSpec DEFAULT = new OAEPParameterSpec();

  private OAEPParameterSpec() {
    mdName = "SHA-1";
    mgfName = "MGF1";
    mgfSpec = MGF1ParameterSpec.SHA1;
    pSrc = PSource.PSpecified.DEFAULT;
  }

  public OAEPParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec mgfSpec, PSource pSrc) {
    if (mdName == null) {
      throw new NullPointerException("digest algorithm is null");
    }
    if (mgfName == null) {
      throw new NullPointerException("mask generation function algorithm is null");
    }
    if (pSrc == null) {
      throw new NullPointerException("source of the encoding input is null");
    }
    this.mdName = mdName;
    this.mgfName = mgfName;
    this.mgfSpec = mgfSpec;
    this.pSrc = pSrc;
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

  public PSource getPSource() {
    return pSrc;
  }
}
