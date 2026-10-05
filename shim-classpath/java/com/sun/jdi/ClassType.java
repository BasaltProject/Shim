/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ClassType extends com.sun.jdi.ReferenceType {
  int INVOKE_SINGLE_THREADED = 1;

  java.util.List<com.sun.jdi.InterfaceType> allInterfaces();
  com.sun.jdi.Method concreteMethodByName(java.lang.String string, java.lang.String string2);
  java.util.List<com.sun.jdi.InterfaceType> interfaces();
  com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference threadReference, com.sun.jdi.Method method, java.util.List<? extends com.sun.jdi.Value> list, int i) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException;
  boolean isEnum();
  com.sun.jdi.ObjectReference newInstance(com.sun.jdi.ThreadReference threadReference, com.sun.jdi.Method method, java.util.List<? extends com.sun.jdi.Value> list, int i) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException;
  void setValue(com.sun.jdi.Field field, com.sun.jdi.Value value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;
  java.util.List<com.sun.jdi.ClassType> subclasses();
  com.sun.jdi.ClassType superclass();
}
