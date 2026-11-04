/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;
import jdk.dynalink.linker.ConversionComparator.Comparison;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.MethodHandleTransformer;

final class LinkerServicesImpl implements LinkerServices {
  private static final ThreadLocal<LinkRequest> threadInvokeLinkRequest = new ThreadLocal<>();

  private final TypeConverterFactory typeConverterFactory;
  private final GuardingDynamicLinker topLevelLinker;
  private final MethodHandleTransformer internalObjectsFilter;

  LinkerServicesImpl(TypeConverterFactory typeConverterFactory, GuardingDynamicLinker topLevelLinker,
      MethodHandleTransformer internalObjectsFilter) {
    this.typeConverterFactory = typeConverterFactory;
    this.topLevelLinker = topLevelLinker;
    this.internalObjectsFilter = internalObjectsFilter;
  }

  @Override
  public boolean canConvert(Class<?> from, Class<?> to) {
    return typeConverterFactory.canConvert(from, to);
  }

  @Override
  public MethodHandle asType(MethodHandle handle, MethodType fromType) {
    return typeConverterFactory.asType(handle, fromType);
  }

  @Override
  public MethodHandle getTypeConverter(Class<?> sourceType, Class<?> targetType) {
    return typeConverterFactory.getTypeConverter(sourceType, targetType);
  }

  @Override
  public Comparison compareConversion(Class<?> sourceType, Class<?> targetType1, Class<?> targetType2) {
    return typeConverterFactory.compareConversion(sourceType, targetType1, targetType2);
  }

  @Override
  public MethodHandle filterInternalObjects(MethodHandle target) {
    return internalObjectsFilter != null ? internalObjectsFilter.transform(target) : target;
  }

  @Override
  public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest) throws Exception {
    final LinkRequest prevLinkRequest = threadInvokeLinkRequest.get();
    threadInvokeLinkRequest.set(linkRequest);
    try {
      return topLevelLinker.getGuardedInvocation(linkRequest, this);
    } finally {
      threadInvokeLinkRequest.set(prevLinkRequest);
    }
  }

  @Override
  public <T> T getWithLookup(Supplier<T> operation, SecureLookupSupplier lookupSupplier) {
    final SecureLookupSupplier prev = TypeConverterFactory.ThreadLocalLookup.set(lookupSupplier);
    try {
      return operation.get();
    } finally {
      TypeConverterFactory.ThreadLocalLookup.set(prev);
    }
  }
}
