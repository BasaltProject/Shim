/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class CallSiteDescriptor extends SecureLookupSupplier {
  private final Operation operation;
  private final MethodType methodType;

  public CallSiteDescriptor(Lookup lookup, Operation operation, MethodType methodType) {
    super(lookup);
    this.operation = Objects.requireNonNull(operation, "name");
    this.methodType = Objects.requireNonNull(methodType, "methodType");
  }

  public final Operation getOperation() {
    return operation;
  }

  public final MethodType getMethodType() {
    return methodType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof CallSiteDescriptor) {
      return equals((CallSiteDescriptor) obj);
    }
    return false;
  }

  private boolean equals(CallSiteDescriptor csd) {
    return operation.equals(csd.operation) && methodType.equals(csd.methodType)
        && lookupsEqual(getLookupPrivileged(), csd.getLookupPrivileged());
  }

  @Override
  public int hashCode() {
    return operation.hashCode() + 31 * methodType.hashCode() + 31 * 31 * lookupHashCode(getLookupPrivileged());
  }

  private static boolean lookupsEqual(Lookup l1, Lookup l2) {
    if (l1 == l2) {
      return true;
    }
    return l1.lookupClass() == l2.lookupClass() && l1.lookupModes() == l2.lookupModes();
  }

  private static int lookupHashCode(Lookup l) {
    return l.lookupClass().hashCode() + 31 * l.lookupModes();
  }

  @Override
  public String toString() {
    final String mt = methodType.toString();
    final String l = getLookupPrivileged().toString();
    return operation.toString() + mt + "@" + l;
  }

  public final CallSiteDescriptor changeMethodType(MethodType newMethodType) {
    return changeMethodTypeInternal(newMethodType);
  }

  protected CallSiteDescriptor changeMethodTypeInternal(MethodType newMethodType) {
    return new CallSiteDescriptor(getLookupPrivileged(), operation, newMethodType);
  }

  public final CallSiteDescriptor changeOperation(Operation newOperation) {
    return changeOperationInternal(newOperation);
  }

  protected CallSiteDescriptor changeOperationInternal(Operation newOperation) {
    return new CallSiteDescriptor(getLookupPrivileged(), newOperation, methodType);
  }
}
