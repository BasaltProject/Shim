/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker.support;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jdk.dynalink.linker.MethodHandleTransformer;

public class DefaultInternalObjectFilter implements MethodHandleTransformer {
  private static final MethodHandle FILTER_VARARGS = new Lookup(MethodHandles.lookup()).findStatic(
      DefaultInternalObjectFilter.class, "filterVarArgs",
      MethodType.methodType(Object[].class, MethodHandle.class, Object[].class));

  private final MethodHandle parameterFilter;
  private final MethodHandle returnFilter;
  private final MethodHandle varArgFilter;

  public DefaultInternalObjectFilter(MethodHandle parameterFilter, MethodHandle returnFilter) {
    this.parameterFilter = checkHandle(parameterFilter, "parameterFilter");
    this.returnFilter = checkHandle(returnFilter, "returnFilter");
    this.varArgFilter = parameterFilter == null ? null : FILTER_VARARGS.bindTo(parameterFilter);
  }

  @Override
  public MethodHandle transform(MethodHandle target) {
    assert target != null;
    MethodHandle[] filters = null;
    final MethodType type = target.type();
    boolean hasFilters = false;
    final int paramCount = type.parameterCount();
    final Class<?> lastParam = paramCount == 0 ? null : type.parameterType(paramCount - 1);
    final boolean isVarArg = target.isVarargsCollector();
    for (int i = 0; i < paramCount; ++i) {
      final Class<?> paramType = type.parameterType(i);
      MethodHandle filter = null;
      if (parameterFilter != null && paramType == Object.class) {
        filter = parameterFilter;
      } else if (varArgFilter != null && i == paramCount - 1 && isVarArg && lastParam == Object[].class) {
        filter = varArgFilter;
      }
      if (filter != null) {
        if (filters == null) {
          filters = new MethodHandle[paramCount];
        }
        filters[i] = filter;
        hasFilters = true;
      }
    }
    MethodHandle filtered = target;
    if (hasFilters) {
      filtered = MethodHandles.filterArguments(filtered, 0, filters);
    }
    if (returnFilter != null && type.returnType() == Object.class) {
      filtered = MethodHandles.filterReturnValue(filtered, returnFilter);
    }
    return filtered;
  }

  @SuppressWarnings("unused")
  private static Object[] filterVarArgs(MethodHandle filter, Object[] args) throws Throwable {
    Object[] newArgs = null;
    for (int i = 0; i < args.length; ++i) {
      final Object arg = args[i];
      final Object newArg = filter.invokeExact(arg);
      if (arg != newArg) {
        if (newArgs == null) {
          newArgs = args.clone();
        }
        newArgs[i] = newArg;
      }
    }
    return newArgs == null ? args : newArgs;
  }

  private static MethodHandle checkHandle(MethodHandle handle, String handleKind) {
    if (handle != null) {
      final MethodType objectObjectType = MethodType.methodType(Object.class, Object.class);
      if (!handle.type().equals(objectObjectType)) {
        throw new IllegalArgumentException("Method type for " + handleKind + " must be " + objectObjectType);
      }
    }
    return handle;
  }
}
