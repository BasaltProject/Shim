/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface Location extends com.sun.jdi.Mirror, java.lang.Comparable<com.sun.jdi.Location> {
  long codeIndex();
  com.sun.jdi.ReferenceType declaringType();
  boolean equals(java.lang.Object object);
  int hashCode();
  int lineNumber();
  int lineNumber(java.lang.String string);
  com.sun.jdi.Method method();
  java.lang.String sourceName() throws com.sun.jdi.AbsentInformationException;
  java.lang.String sourceName(java.lang.String string) throws com.sun.jdi.AbsentInformationException;
  java.lang.String sourcePath() throws com.sun.jdi.AbsentInformationException;
  java.lang.String sourcePath(java.lang.String string) throws com.sun.jdi.AbsentInformationException;
}
