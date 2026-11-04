/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public class UnixNumericGroupPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = 3941535899328403223L;

  private String name;
  private boolean primaryGroup;

  public UnixNumericGroupPrincipal(String name, boolean primaryGroup) {
    if (name == null) {
      throw new NullPointerException("invalid null input: name");
    }
    this.name = name;
    this.primaryGroup = primaryGroup;
  }

  public UnixNumericGroupPrincipal(long name, boolean primaryGroup) {
    this.name = Long.toString(name);
    this.primaryGroup = primaryGroup;
  }

  public String getName() {
    return name;
  }

  public long longValue() {
    return Long.parseLong(name);
  }

  public boolean isPrimaryGroup() {
    return primaryGroup;
  }

  public String toString() {
    return primaryGroup ? "UnixNumericGroupPrincipal [Primary Group]: " + name
        : "UnixNumericGroupPrincipal [Supplementary Group]: " + name;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnixNumericGroupPrincipal)) {
      return false;
    }
    UnixNumericGroupPrincipal that = (UnixNumericGroupPrincipal) o;
    return this.getName().equals(that.getName()) && this.isPrimaryGroup() == that.isPrimaryGroup();
  }

  public int hashCode() {
    return toString().hashCode();
  }
}
