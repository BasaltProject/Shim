/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface Field extends com.sun.jdi.TypeComponent, java.lang.Comparable<com.sun.jdi.Field> {
  boolean equals(java.lang.Object object);
  int hashCode();
  boolean isEnumConstant();
  boolean isTransient();
  boolean isVolatile();
  com.sun.jdi.Type type() throws com.sun.jdi.ClassNotLoadedException;
  java.lang.String typeName();
}
