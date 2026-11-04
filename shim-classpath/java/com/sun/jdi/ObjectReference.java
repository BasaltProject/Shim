/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ObjectReference extends com.sun.jdi.Value {
  int INVOKE_SINGLE_THREADED = 1;
  int INVOKE_NONVIRTUAL = 2;

  void disableCollection();
  void enableCollection();
  int entryCount() throws com.sun.jdi.IncompatibleThreadStateException;
  boolean equals(java.lang.Object object);
  com.sun.jdi.Value getValue(com.sun.jdi.Field field);
  java.util.Map<com.sun.jdi.Field, com.sun.jdi.Value> getValues(java.util.List<? extends com.sun.jdi.Field> list);
  int hashCode();
  com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference threadReference, com.sun.jdi.Method method, java.util.List<? extends com.sun.jdi.Value> list, int i) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException;
  boolean isCollected();
  com.sun.jdi.ThreadReference owningThread() throws com.sun.jdi.IncompatibleThreadStateException;
  com.sun.jdi.ReferenceType referenceType();
  java.util.List<com.sun.jdi.ObjectReference> referringObjects(long l);
  void setValue(com.sun.jdi.Field field, com.sun.jdi.Value value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;
  long uniqueID();
  java.util.List<com.sun.jdi.ThreadReference> waitingThreads() throws com.sun.jdi.IncompatibleThreadStateException;
}
