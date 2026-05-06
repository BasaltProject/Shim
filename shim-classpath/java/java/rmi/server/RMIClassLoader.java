/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class RMIClassLoader {
  private static final RMIClassLoaderSpi defaultProvider = new DefaultProvider();
  private static final RMIClassLoaderSpi provider = loadProvider();

  private RMIClassLoader() {
  }

  private static RMIClassLoaderSpi loadProvider() {
    String name = System.getProperty("java.rmi.server.RMIClassLoaderSpi");
    if (name != null && !name.equals("default")) {
      try {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        return (RMIClassLoaderSpi) Class.forName(name, true, cl).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new java.rmi.server.RMIClassLoader.ProviderError("Could not load " + name, e);
      }
    }
    return defaultProvider;
  }

  private static final class ProviderError extends Error {
    private static final long serialVersionUID = 1L;

    ProviderError(String m, Throwable t) {
      super(m, t);
    }
  }

  @Deprecated
  public static Class<?> loadClass(String name) throws MalformedURLException, ClassNotFoundException {
    return loadClass((String) null, name);
  }

  public static Class<?> loadClass(URL codebase, String name) throws MalformedURLException, ClassNotFoundException {
    return provider.loadClass(codebase != null ? codebase.toString() : null, name, null);
  }

  public static Class<?> loadClass(String codebase, String name) throws MalformedURLException, ClassNotFoundException {
    return provider.loadClass(codebase, name, null);
  }

  public static Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader)
      throws MalformedURLException, ClassNotFoundException {
    return provider.loadClass(codebase, name, defaultLoader);
  }

  public static Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
      throws ClassNotFoundException, MalformedURLException {
    return provider.loadProxyClass(codebase, interfaces, defaultLoader);
  }

  public static ClassLoader getClassLoader(String codebase) throws MalformedURLException {
    return provider.getClassLoader(codebase);
  }

  public static String getClassAnnotation(Class<?> cl) {
    return provider.getClassAnnotation(cl);
  }

  public static RMIClassLoaderSpi getDefaultProviderInstance() {
    return defaultProvider;
  }

  @Deprecated
  public static Object getSecurityContext(ClassLoader loader) {
    return null;
  }

  private static final class DefaultProvider extends RMIClassLoaderSpi {
    private static final boolean useCodebaseOnly = Boolean.parseBoolean(
        System.getProperty("java.rmi.server.useCodebaseOnly", "true"));

    private ClassLoader defaultOr(ClassLoader defaultLoader) {
      if (defaultLoader != null) {
        return defaultLoader;
      }
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      return cl != null ? cl : ClassLoader.getSystemClassLoader();
    }

    public Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
      ClassLoader loader = defaultOr(defaultLoader);
      try {
        return Class.forName(name, false, loader);
      } catch (ClassNotFoundException e) {
        String cb = codebase != null && !useCodebaseOnly ? codebase : System.getProperty("java.rmi.server.codebase");
        if (cb != null && !cb.isEmpty() && !useCodebaseOnly) {
          return Class.forName(name, false, getClassLoader(cb));
        }
        throw e;
      }
    }

    public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException {
      ClassLoader loader = defaultOr(defaultLoader);
      Class<?>[] classes = new Class<?>[interfaces.length];
      for (int i = 0; i < interfaces.length; i++) {
        classes[i] = loadClass(codebase, interfaces[i], loader);
      }
      try {
        return java.lang.reflect.Proxy.getProxyClass(loader, classes);
      } catch (IllegalArgumentException e) {
        throw new ClassNotFoundException(e.getMessage(), e);
      }
    }

    public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
      List<URL> urls = new ArrayList<URL>();
      if (codebase != null) {
        for (String s : codebase.trim().split("\\s+")) {
          if (!s.isEmpty()) {
            urls.add(new URL(s));
          }
        }
      }
      return new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }

    public String getClassAnnotation(Class<?> cl) {
      while (cl.isArray()) {
        cl = cl.getComponentType();
      }
      if (cl.isPrimitive()) {
        return null;
      }
      ClassLoader loader = cl.getClassLoader();
      if (loader == null || loader == ClassLoader.getSystemClassLoader().getParent()) {
        return null;
      }
      if (loader instanceof URLClassLoader && loader != ClassLoader.getSystemClassLoader()) {
        URL[] urls = ((URLClassLoader) loader).getURLs();
        StringBuilder b = new StringBuilder();
        for (URL u : urls) {
          if (b.length() > 0) {
            b.append(' ');
          }
          b.append(u);
        }
        return b.length() == 0 ? null : b.toString();
      }
      return System.getProperty("java.rmi.server.codebase");
    }
  }
}
