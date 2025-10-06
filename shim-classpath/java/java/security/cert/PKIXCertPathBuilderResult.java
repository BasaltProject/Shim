/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.PublicKey;

public class PKIXCertPathBuilderResult extends PKIXCertPathValidatorResult implements CertPathBuilderResult {
  private final CertPath certPath;

  public PKIXCertPathBuilderResult(CertPath certPath, TrustAnchor trustAnchor, PolicyNode policyTree,
      PublicKey subjectPublicKey) {
    super(trustAnchor, policyTree, subjectPublicKey);
    if (certPath == null) {
      throw new NullPointerException("certPath must be non-null");
    }
    this.certPath = certPath;
  }

  @Override
  public CertPath getCertPath() {
    return certPath;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PKIXCertPathBuilderResult: [\n");
    sb.append("  Certification Path: " + certPath + "\n");
    sb.append("  Trust Anchor: " + getTrustAnchor().toString() + "\n");
    sb.append("  Policy Tree: " + String.valueOf(getPolicyTree()) + "\n");
    sb.append("  Subject Public Key: " + getPublicKey() + "\n");
    sb.append("]");
    return sb.toString();
  }
}
