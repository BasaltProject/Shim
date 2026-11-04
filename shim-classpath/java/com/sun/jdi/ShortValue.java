/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ShortValue extends com.sun.jdi.PrimitiveValue, java.lang.Comparable<com.sun.jdi.ShortValue> {
  boolean equals(java.lang.Object object);
  int hashCode();
  short value();
}
