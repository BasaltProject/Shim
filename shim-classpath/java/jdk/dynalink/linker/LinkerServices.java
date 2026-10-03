/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

public interface LinkerServices {
  java.lang.invoke.MethodHandle asType(java.lang.invoke.MethodHandle methodHandle, java.lang.invoke.MethodType methodType);
  default java.lang.invoke.MethodHandle asTypeLosslessReturn(java.lang.invoke.MethodHandle methodHandle, java.lang.invoke.MethodType methodType) {
    throw new UnsupportedOperationException();
  }
  boolean canConvert(java.lang.Class<?> p0, java.lang.Class<?> p1);
  jdk.dynalink.linker.ConversionComparator.Comparison compareConversion(java.lang.Class<?> p0, java.lang.Class<?> p1, java.lang.Class<?> p2);
  java.lang.invoke.MethodHandle filterInternalObjects(java.lang.invoke.MethodHandle methodHandle);
  jdk.dynalink.linker.GuardedInvocation getGuardedInvocation(jdk.dynalink.linker.LinkRequest linkRequest) throws java.lang.Exception;
  java.lang.invoke.MethodHandle getTypeConverter(java.lang.Class<?> p0, java.lang.Class<?> p1);
  <T> T getWithLookup(java.util.function.Supplier<T> supplier, jdk.dynalink.SecureLookupSupplier secureLookupSupplier);
}
