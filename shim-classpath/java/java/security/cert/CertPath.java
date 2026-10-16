/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public abstract class CertPath implements Serializable {
  private static final long serialVersionUID = 6068470306641917886L;

  private final String type;

  protected CertPath(String type) {
    this.type = Objects.requireNonNull(type, "type");
  }

  public String getType() {
    return type;
  }

  public abstract List<? extends Certificate> getCertificates();

  public Iterator<? extends Certificate> iterator() {
    return getCertificates().iterator();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other instanceof CertPath) {
      CertPath cp = (CertPath) other;
      return type.equals(cp.type) && getCertificates().equals(cp.getCertificates());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return type.hashCode() * 31 + getCertificates().hashCode();
  }

  @Override
  public String toString() {
    return type + " Cert Path: " + getCertificates();
  }
}
