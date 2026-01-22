/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

import java.util.Objects;

public class VMOption {
  private final String name;
  private String value;
  private boolean writeable;
  private Origin origin;

  public enum Origin {
    DEFAULT,
    VM_CREATION,
    ENVIRON_VAR,
    CONFIG_FILE,
    MANAGEMENT,
    ERGONOMIC,
    ATTACH_ON_DEMAND,
    OTHER
  }

  public VMOption(String name, String value, boolean writeable, Origin origin) {
    this.name = name;
    this.value = value;
    this.writeable = writeable;
    this.origin = origin;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public Origin getOrigin() {
    return origin;
  }

  public boolean isWriteable() {
    return writeable;
  }

  @Override
  public String toString() {
    return "VM option: " + getName() + " value: " + value + " " + " origin: " + origin + " " + (writeable
        ? "(read-write)" : "(read-only)");
  }
}
