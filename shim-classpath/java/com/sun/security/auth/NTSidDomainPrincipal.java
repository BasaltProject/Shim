/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

public class NTSidDomainPrincipal extends NTSid {
  private static final long serialVersionUID = 5247810785821650912L;

  public NTSidDomainPrincipal(String name) {
    super(name);
  }

  public String toString() {
    return "NTSidDomainPrincipal: " + getName();
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTSidDomainPrincipal)) {
      return false;
    }
    return super.equals(o);
  }
}
