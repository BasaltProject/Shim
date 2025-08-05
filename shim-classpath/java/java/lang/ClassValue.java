/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ClassValue<T> {
  private static final Object NULL_SENTINEL = new Object();
  private final ConcurrentHashMap<Class<?>, Object> map = new ConcurrentHashMap<Class<?>, Object>();

  protected ClassValue() {
  }

  protected abstract T computeValue(Class<?> type);

  @SuppressWarnings("unchecked")
  public T get(Class<?> type) {
    Objects.requireNonNull(type, "type");
    Object v = map.get(type);
    if (v == null) {
      T computed = computeValue(type);
      Object toStore = computed == null ? NULL_SENTINEL : computed;
      Object existing = map.putIfAbsent(type, toStore);
      v = (existing != null) ? existing : toStore;
    }
    return (v == NULL_SENTINEL) ? null : (T) v;
  }

  public void remove(Class<?> type) {
    Objects.requireNonNull(type, "type");
    map.remove(type);
  }
}
