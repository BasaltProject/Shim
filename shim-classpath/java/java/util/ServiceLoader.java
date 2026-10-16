/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ServiceLoader<S> implements Iterable<S> {
  private static final String PREFIX = "META-INF/services/";

  private final Class<S> service;
  private final ClassLoader loader;
  private final ModuleLayer layer;
  private final List<Provider<S>> providers = new ArrayList<Provider<S>>();
  private boolean loaded = false;

  public interface Provider<S> extends Supplier<S> {
    Class<? extends S> type();

    @Override
    S get();
  }

  private static final class ProviderImpl<S> implements Provider<S> {
    private final Class<S> service;
    private final String className;
    private final ClassLoader loader;
    private Class<? extends S> clazz;
    private S instance;

    ProviderImpl(Class<S> service, String className, ClassLoader loader) {
      this.service = service;
      this.className = className;
      this.loader = loader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Class<? extends S> type() {
      if (clazz == null) {
        try {
          Class<?> c = Class.forName(className, false, loader);
          if (!service.isAssignableFrom(c)) {
            throw new ServiceConfigurationError(
                "Service provider " + className + " is not a subtype of " + service.getName());
          }
          clazz = (Class<? extends S>) c;
        } catch (ClassNotFoundException e) {
          throw new ServiceConfigurationError("Provider " + className + " not found", e);
        } catch (Throwable t) {
          throw new ServiceConfigurationError("Error loading provider " + className, t);
        }
      }
      return clazz;
    }

    @Override
    public synchronized S get() {
      if (instance == null) {
        Class<? extends S> c = type();
        try {
          Constructor<? extends S> ctor = c.getDeclaredConstructor();
          ctor.setAccessible(true);
          instance = ctor.newInstance();
        } catch (Throwable t) {
          throw new ServiceConfigurationError("Provider " + className + " could not be instantiated", t);
        }
      }
      return instance;
    }
  }

  private ServiceLoader(Class<S> service, ClassLoader loader, ModuleLayer layer) {
    this.service = Objects.requireNonNull(service, "service");
    this.loader = loader != null ? loader : ClassLoader.getSystemClassLoader();
    this.layer = layer;
  }

  public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
    return new ServiceLoader<S>(service, loader, null);
  }

  public static <S> ServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
  }

  public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    ClassLoader prev = null;
    while (cl != null) {
      prev = cl;
      cl = cl.getParent();
    }
    return ServiceLoader.load(service, prev);
  }

  public static <S> ServiceLoader<S> load(ModuleLayer layer, Class<S> service) {
    return new ServiceLoader<S>(service, null, layer);
  }

  public synchronized void reload() {
    providers.clear();
    loaded = false;
  }

  private synchronized void loadProviders() {
    if (loaded) {
      return;
    }
    loaded = true;

    String fullName = PREFIX + service.getName();
    try {
      Enumeration<URL> configs = loader.getResources(fullName);
      Set<String> names = new LinkedHashSet<String>();
      while (configs != null && configs.hasMoreElements()) {
        URL u = configs.nextElement();
        parseConfigFile(u, names);
      }
      for (String name : names) {
        providers.add(new ProviderImpl<S>(service, name, loader));
      }
    } catch (IOException e) {
      throw new ServiceConfigurationError("Error reading configuration files for " + service.getName(), e);
    }
  }

  private void parseConfigFile(URL u, Set<String> names) {
    try (InputStream is = u.openStream();
         BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = r.readLine()) != null) {
        int comment = line.indexOf('#');
        if (comment >= 0) {
          line = line.substring(0, comment);
        }
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        for (int i = 0; i < line.length(); i++) {
          char cp = line.charAt(i);
          if (Character.isWhitespace(cp) || cp == '#') {
            throw new ServiceConfigurationError("Illegal configuration line: " + line);
          }
        }
        names.add(line);
      }
    } catch (IOException e) {
      throw new ServiceConfigurationError("Error reading configuration file: " + u, e);
    }
  }

  @Override
  public Iterator<S> iterator() {
    loadProviders();
    return new Iterator<S>() {
      private final Iterator<Provider<S>> it = providers.iterator();

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public S next() {
        return it.next().get();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public Stream<Provider<S>> stream() {
    loadProviders();
    return StreamSupport.stream(
        Spliterators.spliterator(providers, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE),
        false);
  }

  public Optional<S> findFirst() {
    Iterator<S> it = iterator();
    if (it.hasNext()) {
      return Optional.of(it.next());
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return "java.util.ServiceLoader[" + service.getName() + "]";
  }
}
