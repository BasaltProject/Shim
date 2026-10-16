/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.Serializable;
import java.security.cert.CertPath;
import java.util.Objects;

public final class CodeSigner implements Serializable {
  private static final long serialVersionUID = 6819288105675287527L;

  private final CertPath signerCertPath;
  private final Timestamp timestamp;

  public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
    this.signerCertPath = Objects.requireNonNull(signerCertPath, "signerCertPath");
    this.timestamp = timestamp;
  }

  public CertPath getSignerCertPath() {
    return signerCertPath;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(signerCertPath, timestamp);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof CodeSigner) {
      CodeSigner other = (CodeSigner) obj;
      return Objects.equals(signerCertPath, other.signerCertPath)
          && Objects.equals(timestamp, other.timestamp);
    }
    return false;
  }

  @Override
  public String toString() {
    return "CodeSigner [signerCertPath=" + signerCertPath + ", timestamp=" + timestamp + "]";
  }
}
