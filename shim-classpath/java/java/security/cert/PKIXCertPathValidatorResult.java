/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.PublicKey;

public class PKIXCertPathValidatorResult implements CertPathValidatorResult {
  private final TrustAnchor trustAnchor;
  private final PolicyNode policyTree;
  private final PublicKey subjectPublicKey;

  public PKIXCertPathValidatorResult(TrustAnchor trustAnchor, PolicyNode policyTree, PublicKey subjectPublicKey) {
    if (subjectPublicKey == null) {
      throw new NullPointerException("subjectPublicKey must be non-null");
    }
    if (trustAnchor == null) {
      throw new NullPointerException("trustAnchor must be non-null");
    }
    this.trustAnchor = trustAnchor;
    this.policyTree = policyTree;
    this.subjectPublicKey = subjectPublicKey;
  }

  public TrustAnchor getTrustAnchor() {
    return trustAnchor;
  }

  public PolicyNode getPolicyTree() {
    return policyTree;
  }

  public PublicKey getPublicKey() {
    return subjectPublicKey;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString(), e);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PKIXCertPathValidatorResult: [\n");
    sb.append("  Trust Anchor: " + trustAnchor.toString() + "\n");
    sb.append("  Policy Tree: " + String.valueOf(policyTree) + "\n");
    sb.append("  Subject Public Key: " + subjectPublicKey + "\n");
    sb.append("]");
    return sb.toString();
  }
}
