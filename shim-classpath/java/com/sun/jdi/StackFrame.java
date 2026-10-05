/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface StackFrame extends com.sun.jdi.Mirror, com.sun.jdi.Locatable {
  java.util.List<com.sun.jdi.Value> getArgumentValues();
  com.sun.jdi.Value getValue(com.sun.jdi.LocalVariable localVariable);
  java.util.Map<com.sun.jdi.LocalVariable, com.sun.jdi.Value> getValues(java.util.List<? extends com.sun.jdi.LocalVariable> list);
  com.sun.jdi.Location location();
  void setValue(com.sun.jdi.LocalVariable localVariable, com.sun.jdi.Value value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;
  com.sun.jdi.ObjectReference thisObject();
  com.sun.jdi.ThreadReference thread();
  com.sun.jdi.LocalVariable visibleVariableByName(java.lang.String string) throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.LocalVariable> visibleVariables() throws com.sun.jdi.AbsentInformationException;
}
