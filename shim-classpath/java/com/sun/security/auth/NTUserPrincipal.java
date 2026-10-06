/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public class NTUserPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = -8737649811939033735L;

  private String name;

  public NTUserPrincipal(String name) {
    if (name == null) {
      throw new NullPointerException("invalid null input: name");
    }
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return "NTUserPrincipal: " + name;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTUserPrincipal)) {
      return false;
    }
    NTUserPrincipal that = (NTUserPrincipal) o;
    return name.equals(that.getName());
  }

  public int hashCode() {
    return this.getName().hashCode();
  }
}
