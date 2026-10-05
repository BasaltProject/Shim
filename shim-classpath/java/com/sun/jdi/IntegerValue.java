/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface IntegerValue extends com.sun.jdi.PrimitiveValue, java.lang.Comparable<com.sun.jdi.IntegerValue> {
  boolean equals(java.lang.Object object);
  int hashCode();
  int value();
}
