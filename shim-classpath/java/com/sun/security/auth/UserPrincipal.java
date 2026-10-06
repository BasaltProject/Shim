/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public final class UserPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = 892106070870210969L;

  private final String name;

  public UserPrincipal(String name) {
    if (name == null) {
      throw new NullPointerException("null name is illegal");
    }
    this.name = name;
  }

  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof UserPrincipal) {
      return name.equals(((UserPrincipal) object).getName());
    }
    return false;
  }

  public String toString() {
    return name;
  }

  public int hashCode() {
    return name.hashCode();
  }

  public String getName() {
    return name;
  }
}
