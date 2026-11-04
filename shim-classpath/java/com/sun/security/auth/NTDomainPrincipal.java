/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public class NTDomainPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = -4408637351440771220L;

  private String name;

  public NTDomainPrincipal(String name) {
    if (name == null) {
      throw new NullPointerException("invalid null input: name");
    }
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return "NTDomainPrincipal: " + name;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTDomainPrincipal)) {
      return false;
    }
    NTDomainPrincipal that = (NTDomainPrincipal) o;
    return name.equals(that.getName());
  }

  public int hashCode() {
    return this.getName().hashCode();
  }
}
