/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface LocalVariable extends com.sun.jdi.Mirror, java.lang.Comparable<com.sun.jdi.LocalVariable> {
  boolean equals(java.lang.Object object);
  java.lang.String genericSignature();
  int hashCode();
  boolean isArgument();
  boolean isVisible(com.sun.jdi.StackFrame stackFrame);
  java.lang.String name();
  java.lang.String signature();
  com.sun.jdi.Type type() throws com.sun.jdi.ClassNotLoadedException;
  java.lang.String typeName();
}
