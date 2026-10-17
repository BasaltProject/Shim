/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.StringTokenizer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

public final class DOMImplementationRegistry {
  public static final String PROPERTY = "org.w3c.dom.DOMImplementationSourceList";
  private static final String kDefaultSource = "shim.xml.dom.DomImplementationSource";

  private final List<DOMImplementationSource> sources;

  private DOMImplementationRegistry(List<DOMImplementationSource> srcs) {
    sources = srcs;
  }

  public static DOMImplementationRegistry newInstance() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, ClassCastException {
    List<DOMImplementationSource> sources = new ArrayList<DOMImplementationSource>();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
    }
    String p = System.getProperty(PROPERTY);
    if (p != null) {
      StringTokenizer st = new StringTokenizer(p);
      while (st.hasMoreTokens()) {
        String sourceName = st.nextToken();
        sources.add(instantiate(sourceName, loader));
      }
    }
    try {
      for (DOMImplementationSource source : ServiceLoader.load(DOMImplementationSource.class, loader)) {
        sources.add(source);
      }
    } catch (Throwable t) {
    }
    if (sources.isEmpty()) {
      sources.add(instantiate(kDefaultSource, loader));
    }
    return new DOMImplementationRegistry(sources);
  }

  private static DOMImplementationSource instantiate(String name, ClassLoader loader)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    try {
      return (DOMImplementationSource) Class.forName(name, true, loader).getDeclaredConstructor().newInstance();
    } catch (java.lang.reflect.InvocationTargetException | NoSuchMethodException e) {
      throw new InstantiationException(name);
    }
  }

  public DOMImplementation getDOMImplementation(final String features) {
    int size = sources.size();
    String name = null;
    for (int i = 0; i < size; i++) {
      DOMImplementationSource source = sources.get(i);
      DOMImplementation impl = source.getDOMImplementation(features);
      if (impl != null) {
        return impl;
      }
    }
    return null;
  }

  public DOMImplementationList getDOMImplementationList(final String features) {
    final List<DOMImplementation> implementations = new ArrayList<DOMImplementation>();
    for (DOMImplementationSource source : sources) {
      DOMImplementationList impls = source.getDOMImplementationList(features);
      for (int j = 0; j < impls.getLength(); j++) {
        implementations.add(impls.item(j));
      }
    }
    return new DOMImplementationList() {
      public DOMImplementation item(final int index) {
        if (index >= 0 && index < implementations.size()) {
          try {
            return implementations.get(index);
          } catch (IndexOutOfBoundsException e) {
            return null;
          }
        }
        return null;
      }

      public int getLength() {
        return implementations.size();
      }
    };
  }

  public synchronized void addSource(final DOMImplementationSource s) {
    if (s == null) {
      throw new NullPointerException();
    }
    if (!sources.contains(s)) {
      sources.add(s);
    }
  }
}
