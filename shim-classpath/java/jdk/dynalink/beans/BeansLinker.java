/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.beans;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;

public class BeansLinker implements GuardingDynamicLinker {
  private final ClassValue<TypeBasedGuardingDynamicLinker> linkers;

  public BeansLinker() {
    this(null);
  }

  public BeansLinker(final MissingMemberHandlerFactory missingMemberHandlerFactory) {
    linkers = new ClassValue<TypeBasedGuardingDynamicLinker>() {
      @Override
      protected TypeBasedGuardingDynamicLinker computeValue(Class<?> clazz) {
        return new BeanLinker(clazz, missingMemberHandlerFactory);
      }
    };
  }

  public TypeBasedGuardingDynamicLinker getLinkerForClass(Class<?> clazz) {
    return linkers.get(clazz);
  }

  public static Set<String> getReadableStaticPropertyNames(Class<?> clazz) {
    return new java.util.HashSet<>(ClassInfo.of(clazz).readableStaticProperties());
  }

  public static Set<String> getReadableInstancePropertyNames(Class<?> clazz) {
    return new java.util.HashSet<>(ClassInfo.of(clazz).readableInstanceProperties());
  }

  public static Set<String> getWritableStaticPropertyNames(Class<?> clazz) {
    return new java.util.HashSet<>(ClassInfo.of(clazz).writableStaticProperties());
  }

  public static Set<String> getWritableInstancePropertyNames(Class<?> clazz) {
    return new java.util.HashSet<>(ClassInfo.of(clazz).writableInstanceProperties());
  }

  public static Set<String> getStaticMethodNames(Class<?> clazz) {
    return new java.util.HashSet<>(ClassInfo.of(clazz).staticMethods.keySet());
  }

  public static Set<String> getInstanceMethodNames(Class<?> clazz) {
    return new java.util.HashSet<>(ClassInfo.of(clazz).instanceMethods.keySet());
  }

  public static Object getConstructorMethod(Class<?> clazz, String signature) {
    Objects.requireNonNull(signature);
    final List<Constructor<?>> matching = new ArrayList<>();
    final String wanted = signature.replace(" ", "");
    for (Constructor<?> c : ClassInfo.of(clazz).constructors) {
      if (BeanLinker.signatureOf(c.getParameterTypes()).equals(wanted)) {
        matching.add(c);
      }
    }
    return matching.isEmpty() ? null : new JavaMethods.ConstructorGroup(clazz.getName(), matching);
  }

  public static boolean isDynamicMethod(Object obj) {
    return obj instanceof JavaMethods.MethodGroup;
  }

  public static boolean isDynamicConstructor(Object obj) {
    return obj instanceof JavaMethods.ConstructorGroup;
  }

  @Override
  public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices linkerServices)
      throws Exception {
    final Object receiver = request.getReceiver();
    if (receiver == null) {
      return null;
    }
    return getLinkerForClass(receiver.getClass()).getGuardedInvocation(request, linkerServices);
  }
}
