/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ArrayReference extends com.sun.jdi.ObjectReference {
  com.sun.jdi.Value getValue(int i);
  java.util.List<com.sun.jdi.Value> getValues();
  java.util.List<com.sun.jdi.Value> getValues(int i, int i2);
  int length();
  void setValue(int i, com.sun.jdi.Value value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;
  void setValues(int i, java.util.List<? extends com.sun.jdi.Value> list, int i2, int i3) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;
  void setValues(java.util.List<? extends com.sun.jdi.Value> list) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;
}
