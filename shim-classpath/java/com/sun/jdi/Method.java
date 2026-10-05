/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface Method extends com.sun.jdi.TypeComponent, com.sun.jdi.Locatable, java.lang.Comparable<com.sun.jdi.Method> {
  java.util.List<com.sun.jdi.Location> allLineLocations() throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.Location> allLineLocations(java.lang.String string, java.lang.String string2) throws com.sun.jdi.AbsentInformationException;
  java.util.List<java.lang.String> argumentTypeNames();
  java.util.List<com.sun.jdi.Type> argumentTypes() throws com.sun.jdi.ClassNotLoadedException;
  java.util.List<com.sun.jdi.LocalVariable> arguments() throws com.sun.jdi.AbsentInformationException;
  byte[] bytecodes();
  boolean equals(java.lang.Object object);
  int hashCode();
  boolean isAbstract();
  boolean isBridge();
  boolean isConstructor();
  default boolean isDefault() {
    throw new UnsupportedOperationException();
  }
  boolean isNative();
  boolean isObsolete();
  boolean isStaticInitializer();
  boolean isSynchronized();
  boolean isVarArgs();
  com.sun.jdi.Location location();
  com.sun.jdi.Location locationOfCodeIndex(long l);
  java.util.List<com.sun.jdi.Location> locationsOfLine(java.lang.String string, java.lang.String string2, int i) throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.Location> locationsOfLine(int i) throws com.sun.jdi.AbsentInformationException;
  com.sun.jdi.Type returnType() throws com.sun.jdi.ClassNotLoadedException;
  java.lang.String returnTypeName();
  java.util.List<com.sun.jdi.LocalVariable> variables() throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.LocalVariable> variablesByName(java.lang.String string) throws com.sun.jdi.AbsentInformationException;
}
