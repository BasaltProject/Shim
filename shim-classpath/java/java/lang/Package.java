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
}
