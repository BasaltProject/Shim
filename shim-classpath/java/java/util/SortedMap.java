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

package java.util;

public interface SortedMap<K, V> extends Map<K, V>, SequencedMap<K, V> {
  public Comparator<? super K> comparator();

  public K firstKey();

  public K lastKey();

  public SortedMap<K, V> headMap(K toKey);

  public SortedMap<K, V> tailMap(K fromKey);

  public SortedMap<K, V> subMap(K fromKey, K toKey);

  @Override
  default SortedMap<K, V> reversed() {
    throw new UnsupportedOperationException();
  }
}
