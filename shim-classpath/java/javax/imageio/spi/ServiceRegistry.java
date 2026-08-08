/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceRegistry {
  private final Map<Class<?>, List<Object>> categories = new LinkedHashMap<>();

  public interface Filter {
    boolean filter(Object provider);
  }

  public ServiceRegistry(Iterator<Class<?>> categories) {
    if (categories == null) {
      throw new IllegalArgumentException("categories == null!");
    }
    while (categories.hasNext()) {
      Class<?> category = categories.next();
      this.categories.put(category, new ArrayList<>());
    }
  }

  public static <T> Iterator<T> lookupProviders(Class<T> providerClass, ClassLoader loader) {
    if (providerClass == null) {
      throw new IllegalArgumentException("providerClass == null!");
    }
    return java.util.ServiceLoader.load(providerClass, loader).iterator();
  }

  public static <T> Iterator<T> lookupProviders(Class<T> providerClass) {
    if (providerClass == null) {
      throw new IllegalArgumentException("providerClass == null!");
    }
    return java.util.ServiceLoader.load(providerClass).iterator();
  }

  public Iterator<Class<?>> getCategories() {
    return new ArrayList<Class<?>>(categories.keySet()).iterator();
  }

  public void registerServiceProvider(Object provider) {
    registerAll(provider, null);
  }

  private boolean registerAll(Object provider, Class<?> only) {
    if (provider == null) {
      throw new IllegalArgumentException("provider == null!");
    }
    boolean any = false;
    for (Map.Entry<Class<?>, List<Object>> e : categories.entrySet()) {
      if ((only == null || only == e.getKey()) && e.getKey().isAssignableFrom(provider.getClass())) {
        if (!e.getValue().contains(provider)) {
          e.getValue().add(provider);
          if (provider instanceof RegisterableService) {
            ((RegisterableService) provider).onRegistration(this, e.getKey());
          }
          any = true;
        }
      }
    }
    return any;
  }

  public void registerServiceProviders(Iterator<?> providers) {
    if (providers == null) {
      throw new IllegalArgumentException("provider == null!");
    }
    while (providers.hasNext()) {
      registerServiceProvider(providers.next());
    }
  }

  public <T> boolean registerServiceProvider(T provider, Class<T> category) {
    if (provider == null) {
      throw new IllegalArgumentException("provider == null!");
    }
    if (category == null) {
      throw new IllegalArgumentException("category == null!");
    }
    if (!category.isAssignableFrom(provider.getClass())) {
      throw new ClassCastException();
    }
    List<Object> list = categories.get(category);
    if (list == null) {
      throw new IllegalArgumentException("category unknown!");
    }
    if (list.contains(provider)) {
      return false;
    }
    list.add(provider);
    if (provider instanceof RegisterableService) {
      ((RegisterableService) provider).onRegistration(this, category);
    }
    return true;
  }

  public <T> boolean deregisterServiceProvider(T provider, Class<T> category) {
    if (provider == null) {
      throw new IllegalArgumentException("provider == null!");
    }
    List<Object> list = categories.get(category);
    if (list == null) {
      throw new IllegalArgumentException("category unknown!");
    }
    boolean removed = list.remove(provider);
    if (removed && provider instanceof RegisterableService) {
      ((RegisterableService) provider).onDeregistration(this, category);
    }
    return removed;
  }

  public void deregisterServiceProvider(Object provider) {
    if (provider == null) {
      throw new IllegalArgumentException("provider == null!");
    }
    for (Map.Entry<Class<?>, List<Object>> e : categories.entrySet()) {
      if (e.getValue().remove(provider) && provider instanceof RegisterableService) {
        ((RegisterableService) provider).onDeregistration(this, e.getKey());
      }
    }
  }

  public boolean contains(Object provider) {
    if (provider == null) {
      throw new IllegalArgumentException("provider == null!");
    }
    for (List<Object> list : categories.values()) {
      if (list.contains(provider)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <T> Iterator<T> getServiceProviders(Class<T> category, boolean useOrdering) {
    List<Object> list = categories.get(category);
    if (list == null) {
      throw new IllegalArgumentException("category unknown!");
    }
    return (Iterator<T>) new ArrayList<Object>(list).iterator();
  }

  @SuppressWarnings("unchecked")
  public <T> Iterator<T> getServiceProviders(Class<T> category, Filter filter, boolean useOrdering) {
    List<Object> list = categories.get(category);
    if (list == null) {
      throw new IllegalArgumentException("category unknown!");
    }
    List<Object> out = new ArrayList<>();
    for (Object o : list) {
      if (filter.filter(o)) {
        out.add(o);
      }
    }
    return (Iterator<T>) out.iterator();
  }

  @SuppressWarnings("unchecked")
  public <T> T getServiceProviderByClass(Class<T> providerClass) {
    if (providerClass == null) {
      throw new IllegalArgumentException("providerClass == null!");
    }
    for (List<Object> list : categories.values()) {
      for (Object o : list) {
        if (o.getClass() == providerClass) {
          return (T) o;
        }
      }
    }
    return null;
  }

  public <T> boolean setOrdering(Class<T> category, T firstProvider, T secondProvider) {
    return true;
  }

  public <T> boolean unsetOrdering(Class<T> category, T firstProvider, T secondProvider) {
    return true;
  }

  public void deregisterAll(Class<?> category) {
    List<Object> list = categories.get(category);
    if (list == null) {
      throw new IllegalArgumentException("category unknown!");
    }
    list.clear();
  }

  public void deregisterAll() {
    for (List<Object> list : categories.values()) {
      list.clear();
    }
  }

  public void finalize() throws Throwable {
    deregisterAll();
    super.finalize();
  }
}
