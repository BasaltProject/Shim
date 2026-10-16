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
import java.util.Date;
import java.util.Objects;

public final class Timestamp implements Serializable {
  private static final long serialVersionUID = -5502683707821851294L;

  private final Date timestamp;
  private final CertPath signerCertPath;

  public Timestamp(Date timestamp, CertPath signerCertPath) {
    this.timestamp = new Date(Objects.requireNonNull(timestamp, "timestamp").getTime());
    this.signerCertPath = Objects.requireNonNull(signerCertPath, "signerCertPath");
  }

  public Date getTimestamp() {
    return new Date(timestamp.getTime());
  }

  public CertPath getSignerCertPath() {
    return signerCertPath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, signerCertPath);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof Timestamp) {
      Timestamp other = (Timestamp) obj;
      return Objects.equals(timestamp, other.timestamp)
          && Objects.equals(signerCertPath, other.signerCertPath);
    }
    return false;
  }

  @Override
  public String toString() {
    return "Timestamp [timestamp=" + timestamp + ", signerCertPath=" + signerCertPath + "]";
  }
}
