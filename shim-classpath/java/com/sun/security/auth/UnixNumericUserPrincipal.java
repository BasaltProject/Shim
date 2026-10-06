/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public class UnixNumericUserPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = -4329764253802397821L;

  private String name;

  public UnixNumericUserPrincipal(String name) {
    if (name == null) {
      throw new NullPointerException("invalid null input: name");
    }
    this.name = name;
  }

  public UnixNumericUserPrincipal(long name) {
    this.name = Long.toString(name);
  }

  public String getName() {
    return name;
  }

  public long longValue() {
    return Long.parseLong(name);
  }

  public String toString() {
    return "UnixNumericUserPrincipal: " + name;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnixNumericUserPrincipal)) {
      return false;
    }
    UnixNumericUserPrincipal that = (UnixNumericUserPrincipal) o;
    return this.getName().equals(that.getName());
  }

  public int hashCode() {
    return name.hashCode();
  }
}
