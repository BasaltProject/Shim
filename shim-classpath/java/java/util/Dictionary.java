/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public abstract class Dictionary<K, V> {
  public Dictionary() { }

  public abstract int size();

  public abstract boolean isEmpty();

  public abstract Enumeration<K> keys();

  public abstract Enumeration<V> elements();

  public abstract V get(Object key);

  public abstract V put(K key, V value);

  public abstract V remove(Object key);
}
