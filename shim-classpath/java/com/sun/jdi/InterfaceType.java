/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface InterfaceType extends com.sun.jdi.ReferenceType {
  java.util.List<com.sun.jdi.ClassType> implementors();
  default com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference threadReference, com.sun.jdi.Method method, java.util.List<? extends com.sun.jdi.Value> list, int i) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException {
    throw new UnsupportedOperationException();
  }
  java.util.List<com.sun.jdi.InterfaceType> subinterfaces();
  java.util.List<com.sun.jdi.InterfaceType> superinterfaces();
}
