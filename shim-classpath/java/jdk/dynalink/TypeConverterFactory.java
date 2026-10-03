/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import jdk.dynalink.linker.ConversionComparator;
import jdk.dynalink.linker.ConversionComparator.Comparison;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.GuardingTypeConverterFactory;
import jdk.dynalink.linker.MethodTypeConversionStrategy;
import jdk.dynalink.linker.support.TypeUtilities;

final class TypeConverterFactory {
  private final GuardingTypeConverterFactory[] factories;
  private final ConversionComparator[] comparators;
  private final MethodTypeConversionStrategy autoConversionStrategy;
  private final ClassValue<ClassValue<MethodHandle>> converterMap = new ClassValue<ClassValue<MethodHandle>>() {
    @Override
    protected ClassValue<MethodHandle> computeValue(final Class<?> sourceType) {
      return new ClassValue<MethodHandle>() {
        @Override
        protected MethodHandle computeValue(Class<?> targetType) {
          return createConverter(sourceType, targetType);
        }
      };
    }
  };

  TypeConverterFactory(Iterable<? extends GuardingDynamicLinker> linkers,
      MethodTypeConversionStrategy autoConversionStrategy) {
    final List<GuardingTypeConverterFactory> factoryList = new ArrayList<>();
    final List<ConversionComparator> comparatorList = new ArrayList<>();
    for (GuardingDynamicLinker linker : linkers) {
      if (linker instanceof GuardingTypeConverterFactory) {
        factoryList.add((GuardingTypeConverterFactory) linker);
      }
      if (linker instanceof ConversionComparator) {
        comparatorList.add((ConversionComparator) linker);
      }
    }
    this.factories = factoryList.toArray(new GuardingTypeConverterFactory[0]);
    this.comparators = comparatorList.toArray(new ConversionComparator[0]);
    this.autoConversionStrategy = autoConversionStrategy;
  }

  MethodHandle asType(MethodHandle handle, MethodType fromType) {
    MethodHandle newHandle = handle;
    final MethodType toType = newHandle.type();
    final int l = toType.parameterCount();
    if (l != fromType.parameterCount()) {
      throw new WrongMethodTypeException("Parameter counts differ: " + handle.type() + " vs. " + fromType);
    }
    for (int i = 0; i < l; ++i) {
      final Class<?> mhType = toType.parameterType(i);
      final Class<?> fromParam = fromType.parameterType(i);
      if (mhType != fromParam && !TypeUtilities.isMethodInvocationConvertible(fromParam, mhType)) {
        final MethodHandle converter = getTypeConverterNull(fromParam, mhType);
        if (converter != null) {
          newHandle = MethodHandles.filterArguments(newHandle, i, converter.asType(MethodType.methodType(mhType,
              fromParam)));
        }
      }
    }
    final Class<?> toRet = newHandle.type().returnType();
    final Class<?> fromRet = fromType.returnType();
    if (toRet != fromRet && toRet != void.class && fromRet != void.class
        && !TypeUtilities.isMethodInvocationConvertible(toRet, fromRet)) {
      final MethodHandle converter = getTypeConverterNull(toRet, fromRet);
      if (converter != null) {
        newHandle = MethodHandles.filterReturnValue(newHandle, converter.asType(MethodType.methodType(fromRet,
            toRet)));
      }
    }
    return autoConversionStrategy != null ? autoConversionStrategy.asType(newHandle, fromType)
        : newHandle.asType(fromType);
  }

  MethodHandle getTypeConverter(Class<?> sourceType, Class<?> targetType) {
    try {
      return converterMap.get(sourceType).get(targetType);
    } catch (NotCastableException e) {
      return null;
    }
  }

  private static final class NotCastableException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  private MethodHandle getTypeConverterNull(Class<?> sourceType, Class<?> targetType) {
    try {
      return converterMap.get(sourceType).get(targetType);
    } catch (NotCastableException e) {
      return null;
    }
  }

  boolean canConvert(Class<?> from, Class<?> to) {
    return canAutoConvert(from, to) || getTypeConverterNull(from, to) != null;
  }

  static boolean canAutoConvert(Class<?> fromType, Class<?> toType) {
    return TypeUtilities.isMethodInvocationConvertible(fromType, toType);
  }

  Comparison compareConversion(Class<?> sourceType, Class<?> targetType1, Class<?> targetType2) {
    for (ConversionComparator comparator : comparators) {
      final Comparison result = comparator.compareConversion(sourceType, targetType1, targetType2);
      if (result != Comparison.INDETERMINATE) {
        return result;
      }
    }
    if (TypeUtilities.isMethodInvocationConvertible(sourceType, targetType1)) {
      if (!TypeUtilities.isMethodInvocationConvertible(sourceType, targetType2)) {
        return Comparison.TYPE_1_BETTER;
      }
    } else if (TypeUtilities.isMethodInvocationConvertible(sourceType, targetType2)) {
      return Comparison.TYPE_2_BETTER;
    }
    return Comparison.INDETERMINATE;
  }

  private MethodHandle createConverter(Class<?> sourceType, Class<?> targetType) {
    final MethodType type = MethodType.methodType(targetType, sourceType);
    final MethodHandle identity = MethodHandles.identity(sourceType);
    MethodHandle last = identity;
    if (!canAutoConvert(sourceType, targetType)) {
      last = null;
    } else {
      last = identity.asType(type);
    }
    for (int i = factories.length; i-- > 0;) {
      final GuardedInvocation next;
      try {
        next = factories[i].convertToType(sourceType, targetType, ThreadLocalLookup::get);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      if (next != null) {
        if (last == null) {
          last = next.getInvocation().asType(type);
          if (next.getGuard() != null) {
            last = MethodHandles.guardWithTest(next.getGuard().asType(MethodType.methodType(boolean.class,
                sourceType)), last, MethodHandles.dropArguments(MethodHandles.throwException(targetType,
                    ClassCastException.class).bindTo(new ClassCastException("Cannot convert " + sourceType + " to "
                        + targetType)), 0, sourceType));
          }
        } else {
          final MethodHandle inv = next.getInvocation().asType(type);
          last = next.getGuard() == null ? inv : MethodHandles.guardWithTest(next.getGuard().asType(
              MethodType.methodType(boolean.class, sourceType)), inv, last);
        }
      }
    }
    if (last == null) {
      throw new NotCastableException();
    }
    return last;
  }

  private static final class WrongMethodTypeException extends java.lang.invoke.WrongMethodTypeException {
    private static final long serialVersionUID = 1L;

    WrongMethodTypeException(String s) {
      super(s);
    }
  }

  static final class ThreadLocalLookup {
    private static final ThreadLocal<SecureLookupSupplier> SUPPLIER = new ThreadLocal<>();

    static Lookup get() {
      final SecureLookupSupplier s = SUPPLIER.get();
      return s == null ? MethodHandles.publicLookup() : s.getLookup();
    }

    static SecureLookupSupplier set(SecureLookupSupplier s) {
      final SecureLookupSupplier prev = SUPPLIER.get();
      SUPPLIER.set(s);
      return prev;
    }
  }
}
