/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class FactoryFinder {
  private FactoryFinder() {
  }

  public static <T> T find(Class<T> type, String defaultImpl, ClassLoader loader) {
    String name = null;
    try {
      name = System.getProperty(type.getName());
    } catch (SecurityException ignored) {
    }
    if (name != null && !name.isEmpty()) {
      return instantiate(type, name, loader);
    }
    ClassLoader serviceLoader = loader != null ? loader : Thread.currentThread().getContextClassLoader();
    try {
      Iterator<T> it = ServiceLoader.load(type, serviceLoader).iterator();
      if (it.hasNext()) {
        return it.next();
      }
    } catch (Throwable ignored) {
    }
    return instantiate(type, defaultImpl, FactoryFinder.class.getClassLoader());
  }

  public static <T> T instantiate(Class<T> type, String name, ClassLoader loader) {
    try {
      Class<?> impl;
      if (loader == null) {
        impl = Class.forName(name);
      } else {
        impl = Class.forName(name, true, loader);
      }
      return type.cast(impl.getDeclaredConstructor().newInstance());
    } catch (ClassNotFoundException e) {
      throw new FactoryError("Provider " + name + " not found", e);
    } catch (ClassCastException e) {
      throw new FactoryError("Provider " + name + " not found", e);
    } catch (ReflectiveOperationException e) {
      throw new FactoryError("Provider " + name + " could not be instantiated: " + e, e);
    }
  }

  public static final class FactoryError extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FactoryError(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
