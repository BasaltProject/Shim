/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Map<K, V> {
  public boolean isEmpty();

  public int size();

  public boolean containsKey(Object obj);

  public boolean containsValue(Object obj);

  public V get(Object key);

  public V put(K key, V value);

  public void putAll(Map<? extends K,? extends V> elts);

  public V remove(Object key);

  public void clear();

  public Set<Entry<K, V>> entrySet();

  public Set<K> keySet();

  public Collection<V> values();

  public boolean equals(Object other);

  public int hashCode();

  default V getOrDefault(Object key, V defaultValue) {
    V v;
    return (((v = get(key)) != null) || containsKey(key)) ? v : defaultValue;
  }

  default void forEach(BiConsumer<? super K, ? super V> action) {
    Objects.requireNonNull(action);
    for (Entry<K, V> entry : entrySet()) {
      action.accept(entry.getKey(), entry.getValue());
    }
  }

  default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    for (Entry<K, V> entry : entrySet()) {
      entry.setValue(function.apply(entry.getKey(), entry.getValue()));
    }
  }

  default V putIfAbsent(K key, V value) {
    V v = get(key);
    if (v == null) {
      v = put(key, value);
    }
    return v;
  }

  default boolean remove(Object key, Object value) {
    Object curValue = get(key);
    if (!Objects.equals(curValue, value) || (curValue == null && !containsKey(key))) {
      return false;
    }
    remove(key);
    return true;
  }

  default boolean replace(K key, V oldValue, V newValue) {
    Object curValue = get(key);
    if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
      return false;
    }
    put(key, newValue);
    return true;
  }

  default V replace(K key, V value) {
    V curValue;
    if (((curValue = get(key)) != null) || containsKey(key)) {
      curValue = put(key, value);
    }
    return curValue;
  }

  default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    Objects.requireNonNull(mappingFunction);
    V v;
    if ((v = get(key)) == null) {
      V newValue;
      if ((newValue = mappingFunction.apply(key)) != null) {
        put(key, newValue);
        return newValue;
      }
    }
    return v;
  }

  default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    V oldValue;
    if ((oldValue = get(key)) != null) {
      V newValue = remappingFunction.apply(key, oldValue);
      if (newValue != null) {
        put(key, newValue);
        return newValue;
      } else {
        remove(key);
        return null;
      }
    } else {
      return null;
    }
  }

  default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    V oldValue = get(key);
    V newValue = remappingFunction.apply(key, oldValue);
    if (newValue == null) {
      if (oldValue != null || containsKey(key)) {
        remove(key);
      }
      return null;
    } else {
      put(key, newValue);
      return newValue;
    }
  }

  default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    Objects.requireNonNull(value);
    V oldValue = get(key);
    V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
    if (newValue == null) {
      remove(key);
    } else {
      put(key, newValue);
    }
    return newValue;
  }

  static <K, V> Entry<K, V> entry(K k, V v) {
    return new ImmutableCollections.MapEntry<K, V>(k, v);
  }

  static <K, V> Map<K, V> of() {
    return ImmutableCollections.emptyMap();
  }

  static <K, V> Map<K, V> of(K k1, V v1) {
    return new ImmutableCollections.MapN<K, V>(new Object[] { k1, v1 });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
    return new ImmutableCollections.MapN<K, V>(new Object[] { k1, v1, k2, v2 });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
    return new ImmutableCollections.MapN<K, V>(new Object[] { k1, v1, k2, v2, k3, v3 });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    return new ImmutableCollections.MapN<K, V>(new Object[] { k1, v1, k2, v2, k3, v3, k4, v4 });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    return new ImmutableCollections.MapN<K, V>(new Object[] { k1, v1, k2, v2, k3, v3, k4, v4, k5, v5 });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                             K k6, V v6) {
    return new ImmutableCollections.MapN<K, V>(new Object[] {
      k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6
    });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                             K k6, V v6, K k7, V v7) {
    return new ImmutableCollections.MapN<K, V>(new Object[] {
      k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7
    });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                             K k6, V v6, K k7, V v7, K k8, V v8) {
    return new ImmutableCollections.MapN<K, V>(new Object[] {
      k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8
    });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                             K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
    return new ImmutableCollections.MapN<K, V>(new Object[] {
      k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9
    });
  }

  static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                             K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
    return new ImmutableCollections.MapN<K, V>(new Object[] {
      k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10
    });
  }

  static <K, V> Map<K, V> ofEntries(Entry<? extends K, ? extends V>... entries) {
    if (entries.length == 0) {
      return ImmutableCollections.emptyMap();
    }
    Object[] table = new Object[entries.length << 1];
    for (int i = 0; i < entries.length; ++i) {
      Entry<? extends K, ? extends V> e = Objects.requireNonNull(entries[i]);
      table[i << 1] = e.getKey();
      table[(i << 1) + 1] = e.getValue();
    }
    return new ImmutableCollections.MapN<K, V>(table);
  }

  @SuppressWarnings("unchecked")
  static <K, V> Map<K, V> copyOf(Map<? extends K, ? extends V> map) {
    if (map instanceof ImmutableCollections.MapN) {
      return (Map<K, V>) map;
    }
    Set<? extends Entry<? extends K, ? extends V>> entries = map.entrySet();
    Entry<? extends K, ? extends V>[] array = entries.toArray(new Entry[entries.size()]);
    return ofEntries(array);
  }

  public interface Entry<K, V> {
    boolean equals(Object o);

    int hashCode();

    public K getKey();

    public V getValue();

    public V setValue(V value);

    static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K, V>> comparingByKey() {
      return (c1, c2) -> c1.getKey().compareTo(c2.getKey());
    }

    static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValue() {
      return (c1, c2) -> c1.getValue().compareTo(c2.getValue());
    }

    static <K, V> Comparator<Map.Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
      Objects.requireNonNull(cmp);
      return (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
    }

    static <K, V> Comparator<Map.Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
      Objects.requireNonNull(cmp);
      return (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
    }

    static <K, V> Map.Entry<K, V> copyOf(Map.Entry<? extends K, ? extends V> e) {
      Objects.requireNonNull(e);
      return entry(e.getKey(), e.getValue());
    }
  }
}
