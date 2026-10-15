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

package java.util.concurrent;

import java.util.Map;

public interface ConcurrentMap<K,V> extends Map<K,V> {
  public V putIfAbsent(K key, V value);

  public boolean remove(Object key, Object value);

  public V replace(K key, V value);

  public boolean replace(K key, V oldValue, V newValue);

  default V getOrDefault(Object key, V defaultValue) {
    V v;
    return ((v = get(key)) != null) ? v : defaultValue;
  }

  default void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
    java.util.Objects.requireNonNull(action);
    for (Map.Entry<K, V> entry : entrySet()) {
      K k;
      V v;
      try {
        k = entry.getKey();
        v = entry.getValue();
      } catch (IllegalStateException ise) {
        continue;
      }
      action.accept(k, v);
    }
  }

  default void replaceAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
    java.util.Objects.requireNonNull(function);
    forEach((k, v) -> {
      while (!replace(k, v, function.apply(k, v))) {
        if ((v = get(k)) == null) {
          break;
        }
      }
    });
  }

  default V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
    java.util.Objects.requireNonNull(mappingFunction);
    V v, newValue;
    return ((v = get(key)) == null &&
            (newValue = mappingFunction.apply(key)) != null &&
            (v = putIfAbsent(key, newValue)) == null) ? newValue : v;
  }

  default V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    V oldValue;
    while ((oldValue = get(key)) != null) {
      V newValue = remappingFunction.apply(key, oldValue);
      if (newValue != null) {
        if (replace(key, oldValue, newValue)) {
          return newValue;
        }
      } else if (remove(key, oldValue)) {
        return null;
      }
    }
    return oldValue;
  }

  default V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    while (true) {
      V oldValue = get(key);
      while (true) {
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
          if (oldValue != null) {
            if (replace(key, oldValue, newValue)) {
              return newValue;
            }
            break;
          } else {
            if ((oldValue = putIfAbsent(key, newValue)) == null) {
              return newValue;
            }
          }
        } else {
          if (oldValue == null || remove(key, oldValue)) {
            return null;
          }
          break;
        }
      }
    }
  }

  default V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    java.util.Objects.requireNonNull(value);
    while (true) {
      V oldValue = get(key);
      while (oldValue != null) {
        V newValue = remappingFunction.apply(oldValue, value);
        if (newValue != null) {
          if (replace(key, oldValue, newValue)) {
            return newValue;
          }
        } else if (remove(key, oldValue)) {
          return null;
        }
        oldValue = get(key);
      }
      if ((oldValue = putIfAbsent(key, value)) == null) {
        return value;
      }
    }
  }
}
