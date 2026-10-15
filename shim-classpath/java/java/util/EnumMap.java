/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;

public class EnumMap<K extends Enum<K>, V>
    extends AbstractMap<K, V>
    implements Serializable, Cloneable {

  private static final long serialVersionUID = 4586612460070145105L;

  private static final Object NULL = new Object();

  private final Class<K> keyType;
  private final K[] keyUniverse;
  private transient Object[] vals;
  private transient int size = 0;

  private transient Set<K> keySet;
  private transient Collection<V> values;
  private transient Set<Map.Entry<K, V>> entrySet;

  public EnumMap(Class<K> keyType) {
    this.keyType = Objects.requireNonNull(keyType, "keyType");
    this.keyUniverse = getKeyUniverse(keyType);
    this.vals = new Object[keyUniverse.length];
  }

  public EnumMap(EnumMap<K, ? extends V> m) {
    this.keyType = m.keyType;
    this.keyUniverse = m.keyUniverse;
    this.vals = m.vals.clone();
    this.size = m.size;
  }

  public EnumMap(Map<K, ? extends V> m) {
    if (m instanceof EnumMap) {
      EnumMap<K, ? extends V> em = (EnumMap<K, ? extends V>) m;
      this.keyType = em.keyType;
      this.keyUniverse = em.keyUniverse;
      this.vals = em.vals.clone();
      this.size = em.size;
    } else {
      if (m.isEmpty()) {
        throw new IllegalArgumentException("Specified map is empty");
      }
      this.keyType = m.keySet().iterator().next().getDeclaringClass();
      this.keyUniverse = getKeyUniverse(keyType);
      this.vals = new Object[keyUniverse.length];
      putAll(m);
    }
  }

  private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType) {
    K[] universe = keyType.getEnumConstants();
    if (universe == null) {
      throw new IllegalArgumentException(keyType + " not an enum");
    }
    return universe;
  }

  private Object maskNull(Object value) {
    return (value == null ? NULL : value);
  }

  @SuppressWarnings("unchecked")
  private V unmaskNull(Object value) {
    return (value == NULL ? null : (V) value);
  }

  private boolean isValidKey(Object key) {
    if (key == null) return false;
    Class<?> keyClass = key.getClass();
    return keyClass == keyType || keyClass.getSuperclass() == keyType;
  }

  private void typeCheck(K key) {
    Objects.requireNonNull(key);
    Class<?> keyClass = key.getClass();
    if (keyClass != keyType && keyClass.getSuperclass() != keyType) {
      throw new ClassCastException(keyClass + " != " + keyType);
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean containsValue(Object value) {
    Object mask = maskNull(value);
    for (Object val : vals) {
      if (mask.equals(val)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return isValidKey(key) && vals[((Enum<?>) key).ordinal()] != null;
  }

  @Override
  public V get(Object key) {
    return isValidKey(key) ? unmaskNull(vals[((Enum<?>) key).ordinal()]) : null;
  }

  @Override
  public V put(K key, V value) {
    typeCheck(key);
    int index = key.ordinal();
    Object oldValue = vals[index];
    vals[index] = maskNull(value);
    if (oldValue == null) {
      size++;
    }
    return unmaskNull(oldValue);
  }

  @Override
  public V remove(Object key) {
    if (!isValidKey(key)) return null;
    int index = ((Enum<?>) key).ordinal();
    Object oldValue = vals[index];
    if (oldValue != null) {
      vals[index] = null;
      size--;
      return unmaskNull(oldValue);
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    if (m instanceof EnumMap) {
      EnumMap<? extends K, ? extends V> em = (EnumMap<? extends K, ? extends V>) m;
      if (em.keyType != keyType) {
        if (em.isEmpty()) return;
        throw new ClassCastException(em.keyType + " != " + keyType);
      }
      for (int i = 0; i < keyUniverse.length; i++) {
        Object emVal = em.vals[i];
        if (emVal != null) {
          if (vals[i] == null) {
            size++;
          }
          vals[i] = emVal;
        }
      }
    } else {
      super.putAll(m);
    }
  }

  @Override
  public void clear() {
    Arrays.fill(vals, null);
    size = 0;
  }

  @Override
  public Set<K> keySet() {
    Set<K> ks = keySet;
    if (ks == null) {
      keySet = ks = new KeySet();
    }
    return ks;
  }

  @Override
  public Collection<V> values() {
    Collection<V> vs = values;
    if (vs == null) {
      values = vs = new Values();
    }
    return vs;
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> es = entrySet;
    if (es == null) {
      entrySet = es = new EntrySet();
    }
    return es;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof EnumMap) {
      EnumMap<?, ?> em = (EnumMap<?, ?>) o;
      if (em.keyType != keyType) return size == 0 && em.size == 0;
      return Arrays.equals(vals, em.vals);
    }
    if (!(o instanceof Map)) return false;
    Map<?, ?> m = (Map<?, ?>) o;
    if (size != m.size()) return false;
    for (int i = 0; i < keyUniverse.length; i++) {
      if (vals[i] != null) {
        K key = keyUniverse[i];
        V val = unmaskNull(vals[i]);
        if (val == null) {
          if (!(m.get(key) == null && m.containsKey(key))) return false;
        } else {
          if (!val.equals(m.get(key))) return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (int i = 0; i < keyUniverse.length; i++) {
      if (vals[i] != null) {
        h += keyUniverse[i].hashCode() ^ Objects.hashCode(unmaskNull(vals[i]));
      }
    }
    return h;
  }

  @SuppressWarnings("unchecked")
  @Override
  public EnumMap<K, V> clone() {
    try {
      EnumMap<K, V> result = (EnumMap<K, V>) super.clone();
      result.vals = vals.clone();
      result.keySet = null;
      result.values = null;
      result.entrySet = null;
      return result;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  private class KeySet extends AbstractSet<K> {
    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public boolean contains(Object o) {
      return containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
      int prevSize = size;
      EnumMap.this.remove(o);
      return size != prevSize;
    }

    @Override
    public void clear() {
      EnumMap.this.clear();
    }
  }

  private class Values extends AbstractCollection<V> {
    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public boolean contains(Object o) {
      return containsValue(o);
    }

    @Override
    public boolean remove(Object o) {
      o = maskNull(o);
      for (int i = 0; i < vals.length; i++) {
        if (o.equals(vals[i])) {
          vals[i] = null;
          size--;
          return true;
        }
      }
      return false;
    }

    @Override
    public void clear() {
      EnumMap.this.clear();
    }
  }

  private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      Object key = entry.getKey();
      return isValidKey(key) && Objects.equals(vals[((Enum<?>) key).ordinal()], maskNull(entry.getValue()));
    }

    @Override
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      Object key = entry.getKey();
      if (isValidKey(key)) {
        int index = ((Enum<?>) key).ordinal();
        if (Objects.equals(vals[index], maskNull(entry.getValue()))) {
          vals[index] = null;
          size--;
          return true;
        }
      }
      return false;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public void clear() {
      EnumMap.this.clear();
    }
  }

  private abstract class EnumMapIterator<T> implements Iterator<T> {
    int index = 0;
    int lastReturned = -1;

    @Override
    public boolean hasNext() {
      while (index < vals.length && vals[index] == null) {
        index++;
      }
      return index < vals.length;
    }

    @Override
    public void remove() {
      if (lastReturned < 0) {
        throw new IllegalStateException();
      }
      if (vals[lastReturned] != null) {
        vals[lastReturned] = null;
        size--;
      }
      lastReturned = -1;
    }
  }

  private class KeyIterator extends EnumMapIterator<K> {
    @Override
    public K next() {
      if (!hasNext()) throw new NoSuchElementException();
      lastReturned = index++;
      return keyUniverse[lastReturned];
    }
  }

  private class ValueIterator extends EnumMapIterator<V> {
    @Override
    public V next() {
      if (!hasNext()) throw new NoSuchElementException();
      lastReturned = index++;
      return unmaskNull(vals[lastReturned]);
    }
  }

  private class EntryIterator extends EnumMapIterator<Map.Entry<K, V>> {
    @Override
    public Map.Entry<K, V> next() {
      if (!hasNext()) throw new NoSuchElementException();
      lastReturned = index++;
      return new MapEntry(lastReturned);
    }
  }

  private class MapEntry implements Map.Entry<K, V> {
    private final int index;

    MapEntry(int index) {
      this.index = index;
    }

    @Override
    public K getKey() {
      return keyUniverse[index];
    }

    @Override
    public V getValue() {
      return unmaskNull(vals[index]);
    }

    @Override
    public V setValue(V value) {
      return put(keyUniverse[index], value);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
      return Objects.equals(keyUniverse[index], other.getKey())
          && Objects.equals(unmaskNull(vals[index]), other.getValue());
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(keyUniverse[index]) ^ Objects.hashCode(unmaskNull(vals[index]));
    }

    @Override
    public String toString() {
      return keyUniverse[index] + "=" + unmaskNull(vals[index]);
    }
  }
}
