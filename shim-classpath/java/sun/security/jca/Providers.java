/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.jca;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Providers {
  private static final CopyOnWriteArrayList<Provider> kProviders = new CopyOnWriteArrayList<Provider>();
  private static final String[] kBuiltIn = {
    "sun.security.provider.Sun",
    "sun.security.rsa.SunRsaSign",
    "sun.security.ec.SunEC",
    "sun.security.ssl.SunJSSE",
    "com.sun.crypto.provider.SunJCE",
  };

  static {
    for (String className : kBuiltIn) {
      try {
        kProviders.add((Provider) Class.forName(className).getConstructor().newInstance());
      } catch (ReflectiveOperationException | LinkageError e) {
        // provider absent from this build
      }
    }
  }

  private Providers() {
  }

  public static List<Provider> list() {
    return kProviders;
  }

  public static Provider[] array() {
    return kProviders.toArray(new Provider[0]);
  }

  public static synchronized int insert(Provider provider, int position) {
    for (Provider p : kProviders) {
      if (p.getName().equals(provider.getName())) {
        return -1;
      }
    }
    int size = kProviders.size();
    if (position < 1 || position > size) {
      kProviders.add(provider);
      return kProviders.size();
    }
    kProviders.add(position - 1, provider);
    return position;
  }

  public static synchronized void remove(String name) {
    for (Provider p : new ArrayList<Provider>(kProviders)) {
      if (p.getName().equals(name)) {
        kProviders.remove(p);
        return;
      }
    }
  }

  public static Provider named(String name) {
    for (Provider p : kProviders) {
      if (p.getName().equals(name)) {
        return p;
      }
    }
    return null;
  }
}
