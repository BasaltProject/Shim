/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.jca;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.util.List;

public final class GetInstance {
  private GetInstance() {
  }

  public static final class Instance {
    public final Provider provider;
    public final Object impl;

    private Instance(Provider provider, Object impl) {
      this.provider = provider;
      this.impl = impl;
    }
  }

  public static Service getService(String type, String algorithm) throws NoSuchAlgorithmException {
    if (algorithm == null) {
      throw new NoSuchAlgorithmException("null algorithm name");
    }
    for (Provider p : Providers.list()) {
      Service s = p.getService(type, algorithm);
      if (s != null) {
        return s;
      }
    }
    throw new NoSuchAlgorithmException(algorithm + " " + type + " not available");
  }

  public static Service getService(String type, String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    if (provider == null || provider.isEmpty()) {
      throw new IllegalArgumentException("missing provider");
    }
    Provider p = Providers.named(provider);
    if (p == null) {
      throw new NoSuchProviderException("no such provider: " + provider);
    }
    Service s = p.getService(type, algorithm);
    if (s == null) {
      throw new NoSuchAlgorithmException("no such algorithm: " + algorithm + " for provider " + provider);
    }
    return s;
  }

  public static Service getService(String type, String algorithm, Provider provider)
      throws NoSuchAlgorithmException {
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    Service s = provider.getService(type, algorithm);
    if (s == null) {
      throw new NoSuchAlgorithmException("no such algorithm: " + algorithm + " for provider " + provider.getName());
    }
    return s;
  }

  public static List<Provider> providers() {
    return Providers.list();
  }

  public static Instance getInstance(String type, Class<?> clazz, String algorithm) throws NoSuchAlgorithmException {
    if (algorithm == null) {
      throw new NoSuchAlgorithmException("null algorithm name");
    }
    NoSuchAlgorithmException failure = null;
    for (Provider p : Providers.list()) {
      Service s = p.getService(type, algorithm);
      if (s == null) {
        continue;
      }
      try {
        return getInstance(s, clazz);
      } catch (NoSuchAlgorithmException e) {
        failure = e;
      }
    }
    if (failure != null) {
      throw failure;
    }
    throw new NoSuchAlgorithmException(algorithm + " " + type + " not available");
  }

  public static Instance getInstance(String type, Class<?> clazz, String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    return getInstance(getService(type, algorithm, provider), clazz);
  }

  public static Instance getInstance(String type, Class<?> clazz, String algorithm, Provider provider)
      throws NoSuchAlgorithmException {
    return getInstance(getService(type, algorithm, provider), clazz);
  }

  public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param)
      throws NoSuchAlgorithmException {
    NoSuchAlgorithmException failure = null;
    for (Provider p : Providers.list()) {
      Service s = p.getService(type, algorithm);
      if (s == null) {
        continue;
      }
      try {
        return getInstance(s, clazz, param);
      } catch (NoSuchAlgorithmException e) {
        failure = e;
      }
    }
    if (failure != null) {
      throw failure;
    }
    throw new NoSuchAlgorithmException(algorithm + " " + type + " not available");
  }

  public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    return getInstance(getService(type, algorithm, provider), clazz, param);
  }

  public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param, Provider provider)
      throws NoSuchAlgorithmException {
    return getInstance(getService(type, algorithm, provider), clazz, param);
  }

  public static Instance getInstance(Service s, Class<?> clazz) throws NoSuchAlgorithmException {
    Object o = s.newInstance(null);
    checkSuperClass(s, o.getClass(), clazz);
    return new Instance(s.getProvider(), o);
  }

  public static Instance getInstance(Service s, Class<?> clazz, Object param) throws NoSuchAlgorithmException {
    Object o = s.newInstance(param);
    checkSuperClass(s, o.getClass(), clazz);
    return new Instance(s.getProvider(), o);
  }

  public static void checkSuperClass(Service s, Class<?> subClass, Class<?> superClass)
      throws NoSuchAlgorithmException {
    if (superClass == null) {
      return;
    }
    if (!superClass.isAssignableFrom(subClass)) {
      throw new NoSuchAlgorithmException("class configured for " + s.getType() + ": " + s.getClassName()
          + " not a " + s.getType());
    }
  }
}
