/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public class UnixPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = -2951667807323493631L;

  private String name;

  public UnixPrincipal(String name) {
    if (name == null) {
      throw new NullPointerException("invalid null input: name");
    }
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return "UnixPrincipal: " + name;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnixPrincipal)) {
      return false;
    }
    UnixPrincipal that = (UnixPrincipal) o;
    return name.equals(that.getName());
  }

  public int hashCode() {
    return this.getName().hashCode();
  }
}
