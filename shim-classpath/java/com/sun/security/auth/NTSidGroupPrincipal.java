/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

public class NTSidGroupPrincipal extends NTSid {
  private static final long serialVersionUID = -1373347438636198229L;

  public NTSidGroupPrincipal(String name) {
    super(name);
  }

  public String toString() {
    return "NTSidGroupPrincipal: " + getName();
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTSidGroupPrincipal)) {
      return false;
    }
    return super.equals(o);
  }
}
