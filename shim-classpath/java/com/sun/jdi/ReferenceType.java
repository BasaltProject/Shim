/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ReferenceType extends com.sun.jdi.Type, java.lang.Comparable<com.sun.jdi.ReferenceType>, com.sun.jdi.Accessible {
  java.util.List<com.sun.jdi.Field> allFields();
  java.util.List<com.sun.jdi.Location> allLineLocations() throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.Location> allLineLocations(java.lang.String string, java.lang.String string2) throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.Method> allMethods();
  java.util.List<java.lang.String> availableStrata();
  com.sun.jdi.ClassLoaderReference classLoader();
  com.sun.jdi.ClassObjectReference classObject();
  byte[] constantPool();
  int constantPoolCount();
  java.lang.String defaultStratum();
  boolean equals(java.lang.Object object);
  boolean failedToInitialize();
  com.sun.jdi.Field fieldByName(java.lang.String string);
  java.util.List<com.sun.jdi.Field> fields();
  java.lang.String genericSignature();
  com.sun.jdi.Value getValue(com.sun.jdi.Field field);
  java.util.Map<com.sun.jdi.Field, com.sun.jdi.Value> getValues(java.util.List<? extends com.sun.jdi.Field> list);
  int hashCode();
  java.util.List<com.sun.jdi.ObjectReference> instances(long l);
  boolean isAbstract();
  boolean isFinal();
  boolean isInitialized();
  boolean isPrepared();
  boolean isStatic();
  boolean isVerified();
  java.util.List<com.sun.jdi.Location> locationsOfLine(java.lang.String string, java.lang.String string2, int i) throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.Location> locationsOfLine(int i) throws com.sun.jdi.AbsentInformationException;
  int majorVersion();
  java.util.List<com.sun.jdi.Method> methods();
  java.util.List<com.sun.jdi.Method> methodsByName(java.lang.String string, java.lang.String string2);
  java.util.List<com.sun.jdi.Method> methodsByName(java.lang.String string);
  int minorVersion();
  default com.sun.jdi.ModuleReference module() {
    throw new UnsupportedOperationException();
  }
  java.lang.String name();
  java.util.List<com.sun.jdi.ReferenceType> nestedTypes();
  java.lang.String sourceDebugExtension() throws com.sun.jdi.AbsentInformationException;
  java.lang.String sourceName() throws com.sun.jdi.AbsentInformationException;
  java.util.List<java.lang.String> sourceNames(java.lang.String string) throws com.sun.jdi.AbsentInformationException;
  java.util.List<java.lang.String> sourcePaths(java.lang.String string) throws com.sun.jdi.AbsentInformationException;
  java.util.List<com.sun.jdi.Field> visibleFields();
  java.util.List<com.sun.jdi.Method> visibleMethods();
}
