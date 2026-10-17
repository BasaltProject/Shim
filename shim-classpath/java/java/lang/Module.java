/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.io.InputStream;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Module implements java.lang.reflect.AnnotatedElement {
  private final String name;
  private final ClassLoader loader;
  private final ModuleDescriptor descriptor;
  private final ModuleLayer layer;
  private final Set<String> packages;

  public static final Module JAVA_BASE;

  static {
    Set<String> basePackages = new HashSet<String>(java.util.Arrays.asList(
        "java.lang", "java.lang.annotation", "java.lang.invoke", "java.lang.module",
        "java.lang.ref", "java.lang.reflect", "java.io", "java.net", "java.nio",
        "java.security", "java.text", "java.util", "java.util.concurrent",
        "java.util.function", "java.util.regex", "java.util.stream", "java.util.zip"
    ));
    ModuleDescriptor desc = ModuleDescriptor.newModule("java.base")
        .packages(basePackages)
        .build();
    ModuleLayer bootLayer = new ModuleLayer(Collections.<Module>emptySet());
    JAVA_BASE = new Module("java.base", null, desc, bootLayer, basePackages);
    bootLayer.setModules(Collections.singleton(JAVA_BASE));
    ModuleLayer.setBoot(bootLayer);
  }

  Module(ClassLoader loader) {
    this(null, loader, null, null, Collections.<String>emptySet());
  }

  Module(String name, ClassLoader loader, ModuleDescriptor descriptor, ModuleLayer layer, Set<String> packages) {
    this.name = name;
    this.loader = loader;
    this.descriptor = descriptor;
    this.layer = layer;
    this.packages = packages != null ? Collections.unmodifiableSet(new HashSet<String>(packages)) : Collections.<String>emptySet();
  }

  public boolean isNamed() {
    return name != null;
  }

  public String getName() {
    return name;
  }

  public ClassLoader getClassLoader() {
    return loader;
  }

  public ModuleDescriptor getDescriptor() {
    return descriptor;
  }

  public ModuleLayer getLayer() {
    return layer;
  }

  public boolean canRead(Module other) {
    Objects.requireNonNull(other);
    return true;
  }

  public Module addReads(Module other) {
    Objects.requireNonNull(other);
    return this;
  }

  public boolean isExported(String pn, Module other) {
    Objects.requireNonNull(pn);
    Objects.requireNonNull(other);
    return true;
  }

  public boolean isExported(String pn) {
    Objects.requireNonNull(pn);
    return true;
  }

  public Module addExports(String pn, Module ordinary) {
    Objects.requireNonNull(pn);
    Objects.requireNonNull(ordinary);
    return this;
  }

  public boolean isOpen(String pn, Module other) {
    Objects.requireNonNull(pn);
    Objects.requireNonNull(other);
    return true;
  }

  public boolean isOpen(String pn) {
    Objects.requireNonNull(pn);
    return true;
  }

  public Module addOpens(String pn, Module other) {
    Objects.requireNonNull(pn);
    Objects.requireNonNull(other);
    return this;
  }

  public boolean canUse(Class<?> service) {
    Objects.requireNonNull(service);
    return true;
  }

  public Module addUses(Class<?> service) {
    Objects.requireNonNull(service);
    return this;
  }

  public Set<String> getPackages() {
    return packages;
  }

  public InputStream getResourceAsStream(String name) throws IOException {
    Objects.requireNonNull(name);
    if (loader != null) {
      return loader.getResourceAsStream(name);
    }
    return ClassLoader.getSystemResourceAsStream(name);
  }

  @Override
  public String toString() {
    if (name == null) {
      return "unnamed module @" + Integer.toHexString(hashCode());
    }
    return "module " + name;
  }

  @Override
  public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    return null;
  }

  @Override
  public java.lang.annotation.Annotation[] getAnnotations() {
    return new java.lang.annotation.Annotation[0];
  }

  @Override
  public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
    return new java.lang.annotation.Annotation[0];
  }
}
