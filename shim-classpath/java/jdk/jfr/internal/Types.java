/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class Types {
  private static final ConcurrentHashMap<String, Long> IDS = new ConcurrentHashMap<String, Long>();
  private static final AtomicLong NEXT = new AtomicLong(1000);

  private Types() {
  }

  public static long idOf(String name) {
    Long id = IDS.get(name);
    if (id == null) {
      Long fresh = NEXT.getAndIncrement();
      id = IDS.putIfAbsent(name, fresh);
      if (id == null) {
        id = fresh;
      }
    }
    return id;
  }

  public static String typeName(Class<?> c) {
    return c.getName();
  }
}
