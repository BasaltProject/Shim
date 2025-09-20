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

public abstract class AbstractMap<K, V> implements Map<K, V> {
  protected AbstractMap() {}

  @Override
  public abstract Set<Entry<K, V>> entrySet();

  @Override
  public int size() {
    return entrySet().size();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    Iterator<Entry<K, V>> it = entrySet().iterator();
    while (it.hasNext()) {
      Entry<K, V> e = it.next();
      if (Objects.equals(key, e.getKey())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    Iterator<Entry<K, V>> it = entrySet().iterator();
    while (it.hasNext()) {
      Entry<K, V> e = it.next();
      if (Objects.equals(value, e.getValue())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    Iterator<Entry<K, V>> it = entrySet().iterator();
    while (it.hasNext()) {
      Entry<K, V> e = it.next();
      if (Objects.equals(key, e.getKey())) {
        return e.getValue();
      }
    }
    return null;
  }

  @Override
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public V remove(Object key) {
    Iterator<Entry<K, V>> it = entrySet().iterator();
    while (it.hasNext()) {
      Entry<K, V> e = it.next();
      if (Objects.equals(key, e.getKey())) {
        V val = e.getValue();
        it.remove();
        return val;
      }
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    entrySet().clear();
  }

  @Override
  public Set<K> keySet() {
    return new AbstractSet<K>() {
      @Override
      public Iterator<K> iterator() {
        return new Iterator<K>() {
          private final Iterator<Entry<K, V>> it = entrySet().iterator();

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
      public int size() {
        return AbstractMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return AbstractMap.this.isEmpty();
      }

      @Override
      public void clear() {
        AbstractMap.this.clear();
      }

      @Override
      public boolean contains(Object k) {
        return AbstractMap.this.containsKey(k);
      }
    };
  }

  @Override
  public Collection<V> values() {
    return new AbstractCollection<V>() {
      @Override
      public Iterator<V> iterator() {
        return new Iterator<V>() {
          private final Iterator<Entry<K, V>> it = entrySet().iterator();

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
      public int size() {
        return AbstractMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return AbstractMap.this.isEmpty();
      }

      @Override
      public void clear() {
        AbstractMap.this.clear();
      }

      @Override
      public boolean contains(Object v) {
        return AbstractMap.this.containsValue(v);
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Map)) {
      return false;
    }
    Map<?, ?> m = (Map<?, ?>) o;
    if (m.size() != size()) {
      return false;
    }
    try {
      for (Entry<K, V> e : entrySet()) {
        K key = e.getKey();
        V value = e.getValue();
        if (value == null) {
          if (!(m.get(key) == null && m.containsKey(key))) {
            return false;
          }
        } else {
          if (!value.equals(m.get(key))) {
            return false;
          }
        }
      }
    } catch (ClassCastException | NullPointerException unused) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (Entry<K, V> entry : entrySet()) {
      h += entry.hashCode();
    }
    return h;
  }

  @Override
  public String toString() {
    Iterator<Entry<K, V>> i = entrySet().iterator();
    if (!i.hasNext()) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (;;) {
      Entry<K, V> e = i.next();
      K key = e.getKey();
      V value = e.getValue();
      sb.append(key == this ? "(this Map)" : key);
      sb.append('=');
      sb.append(value == this ? "(this Map)" : value);
      if (!i.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',').append(' ');
    }
  }

  public static class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {
    private static final long serialVersionUID = -8499721149061103585L;
    private final K key;
    private V value;

    public SimpleEntry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public SimpleEntry(Entry<? extends K, ? extends V> entry) {
      this.key = entry.getKey();
      this.value = entry.getValue();
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public V setValue(V value) {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }

  public static class SimpleImmutableEntry<K, V> implements Entry<K, V>, java.io.Serializable {
    private static final long serialVersionUID = 7138329143926046721L;
    private final K key;
    private final V value;

    public SimpleImmutableEntry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public SimpleImmutableEntry(Entry<? extends K, ? extends V> entry) {
      this.key = entry.getKey();
      this.value = entry.getValue();
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public V setValue(V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }
}
