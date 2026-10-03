/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import jdk.dynalink.beans.BeansLinker;
import jdk.dynalink.linker.GuardedInvocationTransformer;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.GuardingDynamicLinkerExporter;
import jdk.dynalink.linker.MethodHandleTransformer;
import jdk.dynalink.linker.MethodTypeConversionStrategy;
import jdk.dynalink.linker.support.CompositeGuardingDynamicLinker;
import jdk.dynalink.linker.support.CompositeTypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.DefaultInternalObjectFilter;

public final class DynamicLinkerFactory {
  public static final int DEFAULT_UNSTABLE_RELINK_THRESHOLD = 8;

  private boolean classLoaderExplicitlySet = false;
  private ClassLoader classLoader;

  private List<? extends GuardingDynamicLinker> prioritizedLinkers;
  private List<? extends GuardingDynamicLinker> fallbackLinkers;
  private int unstableRelinkThreshold = DEFAULT_UNSTABLE_RELINK_THRESHOLD;
  private GuardedInvocationTransformer prelinkTransformer;
  private MethodTypeConversionStrategy autoConversionStrategy;
  private MethodHandleTransformer internalObjectsFilter;
  private boolean syncOnRelink = false;

  private List<ServiceConfigurationError> autoLoadingErrors = Collections.emptyList();

  public DynamicLinkerFactory() {
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    classLoaderExplicitlySet = true;
  }

  public void setPrioritizedLinkers(List<? extends GuardingDynamicLinker> prioritizedLinkers) {
    this.prioritizedLinkers = prioritizedLinkers == null ? null : new ArrayList<>(prioritizedLinkers);
  }

  public void setPrioritizedLinkers(GuardingDynamicLinker... prioritizedLinkers) {
    setPrioritizedLinkers(prioritizedLinkers == null ? null : Arrays.asList(prioritizedLinkers));
  }

  public void setPrioritizedLinker(GuardingDynamicLinker prioritizedLinker) {
    if (prioritizedLinker == null) {
      throw new IllegalArgumentException("prioritizedLinker == null");
    }
    this.prioritizedLinkers = Collections.singletonList(prioritizedLinker);
  }

  public void setFallbackLinkers(List<? extends GuardingDynamicLinker> fallbackLinkers) {
    this.fallbackLinkers = fallbackLinkers == null ? null : new ArrayList<>(fallbackLinkers);
  }

  public void setFallbackLinkers(GuardingDynamicLinker... fallbackLinkers) {
    setFallbackLinkers(fallbackLinkers == null ? null : Arrays.asList(fallbackLinkers));
  }

  public void setUnstableRelinkThreshold(int unstableRelinkThreshold) {
    this.unstableRelinkThreshold = Math.max(0, unstableRelinkThreshold);
  }

  public void setPrelinkTransformer(GuardedInvocationTransformer prelinkTransformer) {
    this.prelinkTransformer = prelinkTransformer;
  }

  public void setAutoConversionStrategy(MethodTypeConversionStrategy autoConversionStrategy) {
    this.autoConversionStrategy = autoConversionStrategy;
  }

  public void setInternalObjectsFilter(MethodHandleTransformer internalObjectsFilter) {
    this.internalObjectsFilter = internalObjectsFilter;
  }

  public void setSyncOnRelink(boolean syncOnRelink) {
    this.syncOnRelink = syncOnRelink;
  }

  public List<ServiceConfigurationError> getAutoLoadingErrors() {
    return Collections.unmodifiableList(autoLoadingErrors);
  }

  public DynamicLinker createLinker() {
    final List<GuardingDynamicLinker> prioritizedLinkers = this.prioritizedLinkers == null
        ? Collections.<GuardingDynamicLinker>emptyList() : new ArrayList<GuardingDynamicLinker>(this.prioritizedLinkers);
    final List<GuardingDynamicLinker> fallbackLinkers = this.fallbackLinkers == null
        ? Collections.<GuardingDynamicLinker>singletonList(new BeansLinker())
        : new ArrayList<GuardingDynamicLinker>(this.fallbackLinkers);
    final Set<Class<? extends GuardingDynamicLinker>> knownLinkerClasses = new HashSet<>();
    addClasses(knownLinkerClasses, prioritizedLinkers);
    addClasses(knownLinkerClasses, fallbackLinkers);
    final ClassLoader effectiveClassLoader = classLoaderExplicitlySet ? classLoader
        : Thread.currentThread().getContextClassLoader();
    final List<GuardingDynamicLinker> discovered = new LinkedList<>();
    final List<ServiceConfigurationError> errors = new LinkedList<>();
    try {
      final ServiceLoader<GuardingDynamicLinkerExporter> loader = ServiceLoader.load(
          GuardingDynamicLinkerExporter.class, effectiveClassLoader);
      for (java.util.Iterator<GuardingDynamicLinkerExporter> it = loader.iterator(); it.hasNext();) {
        try {
          for (GuardingDynamicLinker linker : it.next().get()) {
            if (!knownLinkerClasses.contains(linker.getClass())) {
              discovered.add(linker);
            }
          }
        } catch (ServiceConfigurationError e) {
          errors.add(e);
        }
      }
    } catch (ServiceConfigurationError e) {
      errors.add(e);
    }
    autoLoadingErrors = errors;
    final List<GuardingDynamicLinker> linkers = new ArrayList<>(prioritizedLinkers.size() + discovered.size()
        + fallbackLinkers.size());
    linkers.addAll(prioritizedLinkers);
    linkers.addAll(discovered);
    linkers.addAll(fallbackLinkers);
    final List<GuardingDynamicLinker> optimized = CompositeTypeBasedGuardingDynamicLinker.optimize(linkers);
    final GuardingDynamicLinker composite;
    switch (linkers.size()) {
      case 0:
        composite = (r, s) -> null;
        break;
      case 1:
        composite = optimized.get(0);
        break;
      default:
        composite = new CompositeGuardingDynamicLinker(optimized);
        break;
    }
    final List<GuardingDynamicLinker> typeConverters = new LinkedList<>(linkers);
    final LinkerServicesImpl linkerServices = new LinkerServicesImpl(new TypeConverterFactory(typeConverters,
        autoConversionStrategy), composite, internalObjectsFilter);
    return new DynamicLinker(linkerServices, prelinkTransformer, syncOnRelink, unstableRelinkThreshold);
  }

  private static void addClasses(Set<Class<? extends GuardingDynamicLinker>> knownLinkerClasses,
      List<? extends GuardingDynamicLinker> linkers) {
    for (GuardingDynamicLinker linker : linkers) {
      knownLinkerClasses.add(linker.getClass());
    }
  }
}
