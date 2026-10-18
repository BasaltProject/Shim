/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;


import java.net.URL;

public class Package implements java.lang.reflect.AnnotatedElement {
  private final String name;
  private final String implementationTitle;
  private final String implementationVendor;
  private final String implementationVersion;
  private final String specificationTitle;
  private final String specificationVendor;
  private final String specificationVersion;
  private final URL sealed;
  private final ClassLoader loader;

  Package(String name,
          String implementationTitle,
          String implementationVendor,
          String implementationVersion,
          String specificationTitle,
          String specificationVendor,
          String specificationVersion,
          URL sealed,
          ClassLoader loader)
  {
    this.name                  = name;
    this.implementationTitle   = implementationTitle;
    this.implementationVendor  = implementationVendor;
    this.implementationVersion = implementationVersion;
    this.specificationTitle    = specificationTitle;
    this.specificationVendor   = specificationVendor;
    this.specificationVersion  = specificationVersion;
    this.sealed                = sealed;
    this.loader                = loader;
  }

  public String getName() {
    return name;
  }

  public String getImplementationTitle() {
    return implementationTitle;
  }

  public String getImplementationVendor() {
    return implementationVendor;
  }

  public String getImplementationVersion() {
    return implementationVersion;
  }

  public String getSpecificationTitle() {
    return specificationTitle;
  }

  public String getSpecificationVendor() {
    return specificationVendor;
  }

  public String getSpecificationVersion() {
    return specificationVersion;
  }

  public boolean isSealed() {
    return sealed != null;
  }

  public boolean isSealed(URL url) {
    return sealed.equals(url);
  }

  private java.lang.annotation.Annotation[] packageAnnotations() {
    try {
      return Class.forName(name + ".package-info", false, loader).getDeclaredAnnotations();
    } catch (ClassNotFoundException | LinkageError e) {
      return new java.lang.annotation.Annotation[0];
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationClass) {
    java.util.Objects.requireNonNull(annotationClass);
    for (java.lang.annotation.Annotation a : packageAnnotations()) {
      if (a.annotationType() == annotationClass) {
        return (A) a;
      }
    }
    return null;
  }

  @Override
  public java.lang.annotation.Annotation[] getAnnotations() {
    return packageAnnotations();
  }

  @Override
  public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
    return packageAnnotations();
  }

  @Deprecated(since = "9")
  public static Package getPackage(String name) {
    java.util.Objects.requireNonNull(name);
    for (ClassLoader l = ClassLoader.getSystemClassLoader(); l != null; l = l.getParent()) {
      Package p = l.getDefinedPackage(name);
      if (p != null) {
        return p;
      }
    }
    ClassLoader boot = shim.SystemClassLoader.bootLoader();
    return boot == null ? null : boot.getDefinedPackage(name);
  }

  @Deprecated(since = "9")
  public static Package[] getPackages() {
    java.util.Map<String, Package> all = new java.util.LinkedHashMap<String, Package>();
    for (ClassLoader l = shim.SystemClassLoader.bootLoader(); l != null; l = null) {
      for (Package p : l.getPackages()) {
        all.putIfAbsent(p.getName(), p);
      }
    }
    for (ClassLoader l = ClassLoader.getSystemClassLoader(); l != null; l = l.getParent()) {
      for (Package p : l.getDefinedPackages()) {
        all.putIfAbsent(p.getName(), p);
      }
    }
    return all.values().toArray(new Package[0]);
  }

  public boolean isCompatibleWith(String desired) throws NumberFormatException {
    if (specificationVersion == null || specificationVersion.length() < 1) {
      throw new NumberFormatException("Empty version string");
    }
    String[] sa = specificationVersion.split("\\.", -1);
    int[] si = new int[sa.length];
    for (int i = 0; i < sa.length; i++) {
      si[i] = Integer.parseInt(sa[i]);
      if (si[i] < 0) {
        throw new NumberFormatException("For input string: \"" + si[i] + "\"");
      }
    }
    String[] da = desired.split("\\.", -1);
    int[] di = new int[da.length];
    for (int i = 0; i < da.length; i++) {
      di[i] = Integer.parseInt(da[i]);
      if (di[i] < 0) {
        throw new NumberFormatException("For input string: \"" + di[i] + "\"");
      }
    }
    int len = Math.max(di.length, si.length);
    for (int i = 0; i < len; i++) {
      int d = i < di.length ? di[i] : 0;
      int s = i < si.length ? si[i] : 0;
      if (s < d) {
        return false;
      }
      if (s > d) {
        return true;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    String spec = specificationTitle != null && !specificationTitle.isEmpty() ? ", " + specificationTitle : "";
    String ver = specificationVersion != null && !specificationVersion.isEmpty()
        ? ", version " + specificationVersion : "";
    return "package " + name + spec + ver;
  }
}
