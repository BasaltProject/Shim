/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface FloatValue extends com.sun.jdi.PrimitiveValue, java.lang.Comparable<com.sun.jdi.FloatValue> {
  boolean equals(java.lang.Object object);
  int hashCode();
  float value();
}
