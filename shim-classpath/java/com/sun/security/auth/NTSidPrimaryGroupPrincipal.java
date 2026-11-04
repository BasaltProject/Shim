/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

public class NTSidPrimaryGroupPrincipal extends NTSid {
  private static final long serialVersionUID = 8011978367305190527L;

  public NTSidPrimaryGroupPrincipal(String name) {
    super(name);
  }

  public String toString() {
    return "NTSidPrimaryGroupPrincipal: " + getName();
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTSidPrimaryGroupPrincipal)) {
      return false;
    }
    return super.equals(o);
  }
}
