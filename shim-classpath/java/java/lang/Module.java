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
  private volatile ModuleDescriptor descriptor;
  private final ModuleLayer layer;
  private volatile Set<String> packages;
  private boolean platform;
  private static volatile boolean platformLoaded;
  private static boolean platformLoading;
  private static java.util.Map<String, Module> packageOwners;
  private java.lang.module.ResolvedModule resolved;
  private final Set<Module> extraReads = new HashSet<Module>();
  private final java.util.Map<String, Set<Module>> extraExports = new java.util.HashMap<String, Set<Module>>();
  private final java.util.Map<String, Set<Module>> extraOpens = new java.util.HashMap<String, Set<Module>>();

  void define(java.lang.module.ResolvedModule r) {
    this.resolved = r;
  }

  private boolean defined() {
    return resolved != null || platform;
  }

  private void ensure() {
    if (platform && !platformLoaded) {
      loadPlatform();
    }
  }

  static synchronized void loadPlatform() {
    if (platformLoaded || platformLoading) {
      return;
    }
    platformLoading = true;
    ModuleLayer boot = ModuleLayer.boot();
    java.util.Map<String, Module> owners = new java.util.HashMap<String, Module>();
    Set<Module> all = new HashSet<Module>();
    for (String line : PlatformData.get().split("\n")) {
      if (line.isEmpty()) {
        continue;
      }
      String[] f = line.split("\\|", -1);
      ModuleDescriptor.Builder b = ModuleDescriptor.newModule(f[0]);
      for (String r : f[1].split(",")) {
        if (r.isEmpty()) {
          continue;
        }
        char c = r.charAt(0);
        if (c == '+') {
          b.requires(Set.of(ModuleDescriptor.Requires.Modifier.TRANSITIVE), r.substring(1));
        } else if (c == '?') {
          b.requires(Set.of(ModuleDescriptor.Requires.Modifier.STATIC), r.substring(1));
        } else {
          b.requires(r);
        }
      }
      for (String e : f[2].split(",")) {
        if (e.isEmpty()) {
          continue;
        }
        int gt = e.indexOf('>');
        if (gt < 0) {
          b.exports(e);
        } else {
          b.exports(e.substring(0, gt), Set.of(e.substring(gt + 1).split("\\+")));
        }
      }
      Set<String> pk = new HashSet<String>();
      for (String q : f[3].split(",")) {
        if (!q.isEmpty()) {
          pk.add(q);
        }
      }
      if (f[0].equals("jdk.unsupported")) {
        b.opens("sun.misc");
        b.opens("sun.reflect");
      }
      b.packages(pk);
      ModuleDescriptor d = b.build();
      Module m = f[0].equals("java.base") ? JAVA_BASE : new Module(f[0], null, d, boot, pk);
      if (m == JAVA_BASE) {
        m.descriptor = d;
        m.packages = Collections.unmodifiableSet(pk);
      } else {
        m.platform = true;
      }
      for (String q : pk) {
        owners.put(q, m);
      }
      all.add(m);
    }
    packageOwners = owners;
    boot.setModules(all);
    platformLoaded = true;
  }

  static boolean isBootPackage(String pn) {
    loadPlatform();
    return packageOwners != null && packageOwners.containsKey(pn);
  }

  static Module ownerOfBootPackage(String pn) {
    loadPlatform();
    Module m = packageOwners == null ? null : packageOwners.get(pn);
    return m != null ? m : JAVA_BASE;
  }

  private static final class PlatformData {
    static String get() {
      return shim.module.PlatformModules.data();
    }
  }

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
    JAVA_BASE.platform = true;
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
    ensure();
    return descriptor;
  }

  public ModuleLayer getLayer() {
    return layer;
  }

  public boolean canRead(Module other) {
    ensure();
    Objects.requireNonNull(other);
    if (!defined() || other == this || !other.isNamed()) {
      return true;
    }
    if (extraReads.contains(other)) {
      return true;
    }
    for (java.lang.module.ResolvedModule r : resolved.reads()) {
      if (r.name().equals(other.name) && (other.resolved == null || other.resolved.equals(r))) {
        return true;
      }
    }
    return other.resolved == null;
  }

  public Module addReads(Module other) {
    Objects.requireNonNull(other);
    if (defined() && other.isNamed()) {
      extraReads.add(other);
    }
    return this;
  }

  private boolean exportedTo(java.lang.module.ModuleDescriptor d, String pn, Module other, boolean opens) {
    if (d.isOpen() || opens) {
      if (d.isOpen()) {
        return true;
      }
      for (java.lang.module.ModuleDescriptor.Opens o : d.opens()) {
        if (o.source().equals(pn) && (!o.isQualified() || (other != null && o.targets().contains(other.name)))) {
          return true;
        }
      }
      return false;
    }
    for (java.lang.module.ModuleDescriptor.Exports e : d.exports()) {
      if (e.source().equals(pn) && (!e.isQualified() || (other != null && e.targets().contains(other.name)))) {
        return true;
      }
    }
    return false;
  }

  private static boolean addedFor(java.util.Map<String, Set<Module>> m, String pn, Module other) {
    Set<Module> to = m.get(pn);
    return to != null && (to.contains(ALL_UNNAMED_MODULE) || (other != null && to.contains(other)));
  }

  private static final Module ALL_UNNAMED_MODULE = new Module(null);

  public boolean isExported(String pn, Module other) {
    ensure();
    Objects.requireNonNull(pn);
    Objects.requireNonNull(other);
    if (!defined() || other == this) {
      return !defined() || packages.contains(pn);
    }
    if (!packages.contains(pn)) {
      return false;
    }
    return exportedTo(descriptor, pn, other, false) || addedFor(extraExports, pn, other)
        || addedFor(extraOpens, pn, other) || exportedTo(descriptor, pn, other, true)
        || commandLineExport(pn, other);
  }

  private boolean commandLineExport(String pn, Module other) {
    String prop = System.getProperty("shim.add-exports");
    if (prop == null) {
      return false;
    }
    String key = name + "/" + pn + "=";
    for (String e : prop.split(",")) {
      if (e.startsWith(key)) {
        for (String target : e.substring(key.length()).split(",")) {
          if (target.equals("ALL-UNNAMED") ? !other.isNamed() : target.equals(other.getName())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isExported(String pn) {
    ensure();
    Objects.requireNonNull(pn);
    if (!defined()) {
      return true;
    }
    if (!packages.contains(pn)) {
      return false;
    }
    if (descriptor.isOpen()) {
      return true;
    }
    for (java.lang.module.ModuleDescriptor.Exports e : descriptor.exports()) {
      if (e.source().equals(pn) && !e.isQualified()) {
        return true;
      }
    }
    for (java.lang.module.ModuleDescriptor.Opens o : descriptor.opens()) {
      if (o.source().equals(pn) && !o.isQualified()) {
        return true;
      }
    }
    return addedFor(extraExports, pn, null) || addedFor(extraOpens, pn, null);
  }

  public Module addExports(String pn, Module ordinary) {
    Objects.requireNonNull(pn);
    Objects.requireNonNull(ordinary);
    if (defined() && !packages.contains(pn)) {
      throw new IllegalArgumentException("package " + pn + " not in contents");
    }
    if (defined()) {
      extraExports.computeIfAbsent(pn, k -> new HashSet<Module>()).add(ordinary);
    }
    return this;
  }

  public boolean isOpen(String pn, Module other) {
    ensure();
    Objects.requireNonNull(pn);
    Objects.requireNonNull(other);
    if (!defined() || other == this) {
      return !defined() || packages.contains(pn);
    }
    return packages.contains(pn) && (exportedTo(descriptor, pn, other, true) || addedFor(extraOpens, pn, other));
  }

  public boolean isOpen(String pn) {
    ensure();
    Objects.requireNonNull(pn);
    if (!defined()) {
      return true;
    }
    if (!packages.contains(pn)) {
      return false;
    }
    if (descriptor.isOpen()) {
      return true;
    }
    for (java.lang.module.ModuleDescriptor.Opens o : descriptor.opens()) {
      if (o.source().equals(pn) && !o.isQualified()) {
        return true;
      }
    }
    return addedFor(extraOpens, pn, null);
  }

  public Module addOpens(String pn, Module other) {
    Objects.requireNonNull(pn);
    Objects.requireNonNull(other);
    if (defined() && !packages.contains(pn)) {
      throw new IllegalArgumentException("package " + pn + " not in contents");
    }
    if (defined()) {
      extraOpens.computeIfAbsent(pn, k -> new HashSet<Module>()).add(other);
    }
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
    ensure();
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

  public boolean isNativeAccessEnabled() {
    return isNamed() && loader == null;
  }
}
