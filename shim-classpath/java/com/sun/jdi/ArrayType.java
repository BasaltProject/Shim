/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ArrayType extends com.sun.jdi.ReferenceType {
  java.lang.String componentSignature();
  com.sun.jdi.Type componentType() throws com.sun.jdi.ClassNotLoadedException;
  java.lang.String componentTypeName();
  com.sun.jdi.ArrayReference newInstance(int i);
}
