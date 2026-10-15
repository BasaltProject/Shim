/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public interface SequencedMap<K, V> extends Map<K, V> {
  SequencedMap<K, V> reversed();

  default Map.Entry<K, V> firstEntry() {
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    return it.hasNext() ? it.next() : null;
  }

  default Map.Entry<K, V> lastEntry() {
    Iterator<Map.Entry<K, V>> it = reversed().entrySet().iterator();
    return it.hasNext() ? it.next() : null;
  }

  default Map.Entry<K, V> pollFirstEntry() {
    Iterator<Map.Entry<K, V>> it = entrySet().iterator();
    if (!it.hasNext()) {
      return null;
    }
    Map.Entry<K, V> entry = it.next();
    Map.Entry<K, V> copy = Map.entry(entry.getKey(), entry.getValue());
    it.remove();
    return copy;
  }

  default Map.Entry<K, V> pollLastEntry() {
    Iterator<Map.Entry<K, V>> it = reversed().entrySet().iterator();
    if (!it.hasNext()) {
      return null;
    }
    Map.Entry<K, V> entry = it.next();
    Map.Entry<K, V> copy = Map.entry(entry.getKey(), entry.getValue());
    it.remove();
    return copy;
  }

  default V putFirst(K key, V value) {
    throw new UnsupportedOperationException();
  }

  default V putLast(K key, V value) {
    throw new UnsupportedOperationException();
  }

  default SequencedSet<K> sequencedKeySet() {
    return new KeySetView<K, V>(this);
  }

  default SequencedCollection<V> sequencedValues() {
    return new ValuesView<K, V>(this);
  }

  default SequencedSet<Map.Entry<K, V>> sequencedEntrySet() {
    return new EntrySetView<K, V>(this);
  }

  static class KeySetView<K, V> extends AbstractSet<K> implements SequencedSet<K> {
    private final SequencedMap<K, V> map;

    KeySetView(SequencedMap<K, V> map) {
      this.map = map;
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean contains(Object o) {
      return map.containsKey(o);
    }

    @Override
    public Iterator<K> iterator() {
      final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
      return new Iterator<K>() {
        @Override
        public boolean hasNext() {
          return it.hasNext();
        }

        @Override
        public K next() {
          return it.next().getKey();
        }

        @Override
        public void remove() {
          it.remove();
        }
      };
    }

    @Override
    public SequencedSet<K> reversed() {
      return new KeySetView<K, V>(map.reversed());
    }
  }

  static class ValuesView<K, V> extends AbstractCollection<V> implements SequencedCollection<V> {
    private final SequencedMap<K, V> map;

    ValuesView(SequencedMap<K, V> map) {
      this.map = map;
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public Iterator<V> iterator() {
      final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
      return new Iterator<V>() {
        @Override
        public boolean hasNext() {
          return it.hasNext();
        }

        @Override
        public V next() {
          return it.next().getValue();
        }

        @Override
        public void remove() {
          it.remove();
        }
      };
    }

    @Override
    public SequencedCollection<V> reversed() {
      return new ValuesView<K, V>(map.reversed());
    }
  }

  static class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> implements SequencedSet<Map.Entry<K, V>> {
    private final SequencedMap<K, V> map;

    EntrySetView(SequencedMap<K, V> map) {
      this.map = map;
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return map.entrySet().iterator();
    }

    @Override
    public SequencedSet<Map.Entry<K, V>> reversed() {
      return new EntrySetView<K, V>(map.reversed());
    }
  }
}
