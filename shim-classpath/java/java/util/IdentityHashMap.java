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

public class IdentityHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, java.io.Serializable, Cloneable {
  private static final long serialVersionUID = 8188218128353913216L;
  private static final java.io.ObjectStreamField[] serialPersistentFields = {
    new java.io.ObjectStreamField("size", Integer.TYPE)
  };

  private transient HelperHashMap<K, V> map;

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    java.io.ObjectOutputStream.PutField fields = s.putFields();
    fields.put("size", map.size());
    s.writeFields();
    s.writeInt(map.size() * 2);
    for (Entry<K, V> e : map.entrySet()) {
      s.writeObject(e.getKey());
      s.writeObject(e.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    int size = s.readFields().get("size", 0);
    if (size < 0) {
      throw new java.io.StreamCorruptedException("Illegal mappings count: " + size);
    }
    int length = s.readInt();
    map = new HelperHashMap(size, new MyHelper());
    for (int i = 0; i < size; i++) {
      K key = (K) s.readObject();
      V value = (V) s.readObject();
      map.put(key, value);
    }
  }

  public IdentityHashMap(Map<? extends K, ? extends V> m) {
    this(Math.max(m.size(), 0));
    putAll(m);
  }

  public IdentityHashMap(int capacity) {
    map = new HelperHashMap(capacity, new MyHelper());
  }

  public IdentityHashMap() {
    this(0);
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public int size() {
    return map.size();
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public V get(Object key) {
    return map.get(key);
  }

  public V put(K key, V value) {
    return map.put(key, value);
  }

  public void putAll(Map<? extends K,? extends V> elts) {
    map.putAll(elts);
  }

  public V remove(Object key) {
    return map.remove(key);
  }

  public void clear() {
    map.clear();
  }

  public Set<Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public Collection<V> values() {
    return map.values();
  }

  private static class MyHelper<K, V>
    extends HelperHashMap.MyHelper<K, V>
  {
    public int hash(K a) {
      return (a == null ? 0 : System.identityHashCode(a));
    }

    public boolean equal(K a, K b) {
      return a == b;
    }    
  }
}
