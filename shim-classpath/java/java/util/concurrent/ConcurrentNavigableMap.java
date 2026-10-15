/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.NavigableMap;
import java.util.NavigableSet;

public interface ConcurrentNavigableMap<K, V>
    extends ConcurrentMap<K, V>, NavigableMap<K, V> {

  @Override
  ConcurrentNavigableMap<K, V> subMap(
      K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

  @Override
  ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive);

  @Override
  ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive);

  @Override
  default ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  default ConcurrentNavigableMap<K, V> headMap(K toKey) {
    return headMap(toKey, false);
  }

  @Override
  default ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  ConcurrentNavigableMap<K, V> descendingMap();

  @Override
  NavigableSet<K> navigableKeySet();

  @Override
  NavigableSet<K> keySet();

  @Override
  NavigableSet<K> descendingKeySet();
}
