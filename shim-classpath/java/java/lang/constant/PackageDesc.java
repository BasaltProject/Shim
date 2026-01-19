/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.util.Objects;

public interface PackageDesc {
  boolean equals(Object o);

  int hashCode();

  static PackageDesc of(String name) {
    Objects.requireNonNull(name, "name");
    if (!PackageDescImpl.valid(name, '.', '/')) {
      throw new IllegalArgumentException("Invalid class name: " + name);
    }
    return new PackageDescImpl(name);
  }

  static PackageDesc ofInternalName(String name) {
    Objects.requireNonNull(name, "name");
    if (!PackageDescImpl.valid(name, '/', '.')) {
      throw new IllegalArgumentException("Invalid class name: " + name);
    }
    return new PackageDescImpl(name.replace('/', '.'));
  }

  String internalName();

  String name();
}
