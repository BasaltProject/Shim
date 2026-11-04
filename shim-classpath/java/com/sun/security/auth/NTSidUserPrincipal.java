/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

public class NTSidUserPrincipal extends NTSid {
  private static final long serialVersionUID = -5573239889517749525L;

  public NTSidUserPrincipal(String name) {
    super(name);
  }

  public String toString() {
    return "NTSidUserPrincipal: " + getName();
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTSidUserPrincipal)) {
      return false;
    }
    return super.equals(o);
  }
}
