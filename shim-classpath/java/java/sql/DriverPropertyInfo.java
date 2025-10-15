/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class DriverPropertyInfo {
  public String name;
  public String description = null;
  public boolean required = false;
  public String value = null;
  public String[] choices = null;

  public DriverPropertyInfo(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
