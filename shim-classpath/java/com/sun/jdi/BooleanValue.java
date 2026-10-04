/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface BooleanValue extends com.sun.jdi.PrimitiveValue {
  boolean equals(java.lang.Object object);
  int hashCode();
  boolean value();
}
