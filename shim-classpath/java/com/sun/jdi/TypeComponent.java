/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface TypeComponent extends com.sun.jdi.Mirror, com.sun.jdi.Accessible {
  com.sun.jdi.ReferenceType declaringType();
  java.lang.String genericSignature();
  boolean isFinal();
  boolean isStatic();
  boolean isSynthetic();
  java.lang.String name();
  java.lang.String signature();
}
