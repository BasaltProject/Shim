/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

final class PackageDescImpl implements PackageDesc {
  private final String name;

  PackageDescImpl(String name) {
    this.name = name;
  }

  static boolean valid(String name, char separator, char other) {
    if (name.isEmpty()) {
      return true;
    }
    if (name.indexOf(other) >= 0 || name.indexOf(';') >= 0 || name.indexOf('[') >= 0) {
      return false;
    }
    return name.charAt(0) != separator && name.charAt(name.length() - 1) != separator
        && !name.contains("" + separator + separator);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String internalName() {
    return name.replace('.', '/');
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PackageDescImpl && name.equals(((PackageDescImpl) o).name);
  }

  @Override
  public int hashCode() {
    return internalName().hashCode();
  }

  @Override
  public String toString() {
    return "PackageDesc[" + name + "]";
  }
}
