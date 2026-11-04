/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ClassLoaderReference extends com.sun.jdi.ObjectReference {
  java.util.List<com.sun.jdi.ReferenceType> definedClasses();
  java.util.List<com.sun.jdi.ReferenceType> visibleClasses();
}
