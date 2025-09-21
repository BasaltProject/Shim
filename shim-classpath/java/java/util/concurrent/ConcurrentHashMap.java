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

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {
  private static final int MAXIMUM_CAPACITY = 1 << 30;
  private static final int DEFAULT_CAPACITY = 16;
  private static final float LOAD_FACTOR = 0.75f;
  private static final int TREEIFY_THRESHOLD = 8;
  private static final int MIN_TREEIFY_CAPACITY = 64;
  static final int HASH_BITS = 0x7fffffff;

  static class Node<K, V> implements Map.Entry<K, V> {
    final int hash;
    final K key;
    volatile V val;
    volatile Node<K, V> next;

    Node(int hash, K key, V val) {
      this.hash = hash;
      this.key = key;
      this.val = val;
    }

    Node(int hash, K key, V val, Node<K, V> next) {
      this(hash, key, val);
      this.next = next;
    }

    public final K getKey() {
      return key;
    }

    public final V getValue() {
      return val;
    }

    public final int hashCode() {
      return key.hashCode() ^ val.hashCode();
    }

    public final String toString() {
      return key + "=" + val;
    }

    public final V setValue(V value) {
      throw new UnsupportedOperationException();
    }

    public final boolean equals(Object o) {
      Object k, v, u;
      Map.Entry<?, ?> e;
      return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null
              && (v = e.getValue()) != null && (k == key || k.equals(key)) && (v == (u = val) || v.equals(u)));
    }
  }

  static final int spread(int h) {
    return (h ^ (h >>> 16)) & HASH_BITS;
  }

  private static final int tableSizeFor(int c) {
    int n = -1 >>> Integer.numberOfLeadingZeros(c - 1);
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
  }

  transient volatile Node<K, V>[] table;
  private transient volatile int sizeCtl;
  private transient volatile long baseCount;
  private final transient Object lock = new Object();

  public ConcurrentHashMap() { }

  public ConcurrentHashMap(int initialCapacity) {
    this(initialCapacity, LOAD_FACTOR, 1);
  }

  public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
    this.sizeCtl = DEFAULT_CAPACITY;
    putAll(m);
  }

  public ConcurrentHashMap(int initialCapacity, float loadFactor) {
    this(initialCapacity, loadFactor, 1);
  }

  public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
    if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0) {
      throw new IllegalArgumentException();
    }
    if (initialCapacity < concurrencyLevel) {
      initialCapacity = concurrencyLevel;
    }
    long size = (long) (1.0 + (long) initialCapacity / loadFactor);
    int cap = (size >= (long) MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : tableSizeFor((int) size);
    this.sizeCtl = cap;
  }

  public int size() {
    long n = baseCount;
    return ((n < 0L) ? 0 : (n > (long) Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) n);
  }

  public boolean isEmpty() {
    return baseCount <= 0L;
  }

  public long mappingCount() {
    long n = baseCount;
    return (n < 0L) ? 0L : n;
  }

  public V get(Object key) {
    Node<K, V>[] tab;
    Node<K, V> e;
    int n, eh;
    K ek;
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 && (e = tab[(n - 1) & h]) != null) {
      for (; e != null; e = e.next) {
        if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
          return e.val;
        }
      }
    }
    return null;
  }

  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  public boolean containsValue(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          V v;
          if ((v = p.val) == value || (v != null && value.equals(v))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public V put(K key, V value) {
    return putVal(key, value, false);
  }

  @SuppressWarnings("unchecked")
  final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) {
      throw new NullPointerException();
    }
    int hash = spread(key.hashCode());
    int binCount = 0;
    V oldVal = null;
    synchronized (lock) {
      Node<K, V>[] tab = table;
      if (tab == null || tab.length == 0) {
        tab = initTable();
      }
      int n = tab.length;
      int i = (n - 1) & hash;
      Node<K, V> f = tab[i];
      if (f == null) {
        tab[i] = new Node<K, V>(hash, key, value);
        binCount = 0;
      } else {
        for (Node<K, V> e = f;; ++binCount) {
          K ek;
          if (e.hash == hash && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
            oldVal = e.val;
            if (!onlyIfAbsent) {
              e.val = value;
            }
            break;
          }
          Node<K, V> pred = e;
          if ((e = e.next) == null) {
            pred.next = new Node<K, V>(hash, key, value);
            break;
          }
        }
        binCount++;
      }
      if (binCount >= TREEIFY_THRESHOLD && oldVal == null && tab.length < MIN_TREEIFY_CAPACITY) {
        tryPresize(tab.length << 1);
      }
    }
    if (oldVal != null) {
      return oldVal;
    }
    addCount(1L);
    return null;
  }

  @SuppressWarnings("unchecked")
  private Node<K, V>[] initTable() {
    Node<K, V>[] tab = table;
    if (tab == null || tab.length == 0) {
      int sc = sizeCtl;
      int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
      tab = (Node<K, V>[]) new Node<?, ?>[n];
      table = tab;
      sizeCtl = n - (n >>> 2);
    }
    return tab;
  }

  private void addCount(long x) {
    synchronized (lock) {
      long s = (baseCount += x);
      if (x > 0) {
        Node<K, V>[] tab;
        int sc;
        while (s >= (long) (sc = sizeCtl) && (tab = table) != null && tab.length < MAXIMUM_CAPACITY) {
          transfer(tab);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void transfer(Node<K, V>[] tab) {
    int n = tab.length;
    Node<K, V>[] nextTab = (Node<K, V>[]) new Node<?, ?>[n << 1];
    for (int i = 0; i < n; ++i) {
      Node<K, V> f = tab[i];
      if (f == null) {
        continue;
      }
      Node<K, V> ln, hn;
      int runBit = f.hash & n;
      Node<K, V> lastRun = f;
      for (Node<K, V> p = f.next; p != null; p = p.next) {
        int b = p.hash & n;
        if (b != runBit) {
          runBit = b;
          lastRun = p;
        }
      }
      if (runBit == 0) {
        ln = lastRun;
        hn = null;
      } else {
        hn = lastRun;
        ln = null;
      }
      for (Node<K, V> p = f; p != lastRun; p = p.next) {
        int ph = p.hash;
        K pk = p.key;
        V pv = p.val;
        if ((ph & n) == 0) {
          ln = new Node<K, V>(ph, pk, pv, ln);
        } else {
          hn = new Node<K, V>(ph, pk, pv, hn);
        }
      }
      nextTab[i] = ln;
      nextTab[i + n] = hn;
    }
    table = nextTab;
    sizeCtl = (n << 1) - (n >>> 1);
  }

  private void tryPresize(int size) {
    int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY : tableSizeFor(size + (size >>> 1) + 1);
    int sc;
    while ((sc = sizeCtl) >= 0) {
      Node<K, V>[] tab = table;
      int n;
      if (tab == null || (n = tab.length) == 0) {
        n = (sc > c) ? sc : c;
        @SuppressWarnings("unchecked")
        Node<K, V>[] nt = (Node<K, V>[]) new Node<?, ?>[n];
        table = nt;
        sizeCtl = n - (n >>> 2);
      } else if (c <= sc || n >= MAXIMUM_CAPACITY) {
        break;
      } else {
        transfer(tab);
      }
    }
  }

  public void putAll(Map<? extends K, ? extends V> m) {
    synchronized (lock) {
      tryPresize(m.size());
    }
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      putVal(e.getKey(), e.getValue(), false);
    }
  }

  public V remove(Object key) {
    return replaceNode(key, null, null);
  }

  final V replaceNode(Object key, V value, Object cv) {
    int hash = spread(key.hashCode());
    V oldVal = null;
    boolean removed = false;
    synchronized (lock) {
      Node<K, V>[] tab = table;
      int n;
      if (tab == null || (n = tab.length) == 0) {
        return null;
      }
      int i = (n - 1) & hash;
      Node<K, V> f = tab[i];
      if (f == null) {
        return null;
      }
      Node<K, V> pred = null;
      for (Node<K, V> e = f; e != null; pred = e, e = e.next) {
        K ek;
        if (e.hash == hash && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
          V ev = e.val;
          if (cv == null || cv == ev || (ev != null && cv.equals(ev))) {
            oldVal = ev;
            if (value != null) {
              e.val = value;
            } else if (pred != null) {
              pred.next = e.next;
              removed = true;
            } else {
              tab[i] = e.next;
              removed = true;
            }
          }
          break;
        }
      }
      if (removed) {
        baseCount -= 1L;
      }
    }
    return oldVal;
  }

  public void clear() {
    synchronized (lock) {
      Node<K, V>[] tab = table;
      long delta = 0L;
      if (tab != null) {
        for (int i = 0; i < tab.length; ++i) {
          for (Node<K, V> p = tab[i]; p != null; p = p.next) {
            --delta;
          }
          tab[i] = null;
        }
      }
      if (delta != 0L) {
        baseCount += delta;
      }
    }
  }

  public KeySetView<K, V> keySet() {
    KeySetView<K, V> ks;
    if ((ks = keySet) != null) {
      return ks;
    }
    return keySet = new KeySetView<K, V>(this, null);
  }

  public Collection<V> values() {
    ValuesView<K, V> vs;
    if ((vs = values) != null) {
      return vs;
    }
    return values = new ValuesView<K, V>(this);
  }

  public Set<Map.Entry<K, V>> entrySet() {
    EntrySetView<K, V> es;
    if ((es = entrySet) != null) {
      return es;
    }
    return entrySet = new EntrySetView<K, V>(this);
  }

  private transient KeySetView<K, V> keySet;
  private transient ValuesView<K, V> values;
  private transient EntrySetView<K, V> entrySet;

  public int hashCode() {
    int h = 0;
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          h += p.key.hashCode() ^ p.val.hashCode();
        }
      }
    }
    return h;
  }

  public String toString() {
    Node<K, V>[] t;
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    boolean first = true;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          if (!first) {
            sb.append(',').append(' ');
          }
          first = false;
          K k = p.key;
          V v = p.val;
          sb.append(k == this ? "(this Map)" : k);
          sb.append('=');
          sb.append(v == this ? "(this Map)" : v);
        }
      }
    }
    return sb.append('}').toString();
  }

  public boolean equals(Object o) {
    if (o != this) {
      if (!(o instanceof Map)) {
        return false;
      }
      Map<?, ?> m = (Map<?, ?>) o;
      Node<K, V>[] t;
      if ((t = table) != null) {
        for (Node<K, V> head : t) {
          for (Node<K, V> p = head; p != null; p = p.next) {
            V val = p.val;
            Object v = m.get(p.key);
            if (v == null || (v != val && !v.equals(val))) {
              return false;
            }
          }
        }
      }
      for (Map.Entry<?, ?> e : m.entrySet()) {
        Object mk, mv, v;
        if ((mk = e.getKey()) == null || (mv = e.getValue()) == null || (v = get(mk)) == null
            || (mv != v && !mv.equals(v))) {
          return false;
        }
      }
    }
    return true;
  }

  public V putIfAbsent(K key, V value) {
    return putVal(key, value, true);
  }

  public boolean remove(Object key, Object value) {
    if (key == null) {
      throw new NullPointerException();
    }
    return value != null && replaceNode(key, null, value) != null;
  }

  public boolean replace(K key, V oldValue, V newValue) {
    if (key == null || oldValue == null || newValue == null) {
      throw new NullPointerException();
    }
    return replaceNode(key, newValue, oldValue) != null;
  }

  public V replace(K key, V value) {
    if (key == null || value == null) {
      throw new NullPointerException();
    }
    return replaceNode(key, value, null);
  }

  public V getOrDefault(Object key, V defaultValue) {
    V v;
    return (v = get(key)) == null ? defaultValue : v;
  }

  public void forEach(BiConsumer<? super K, ? super V> action) {
    if (action == null) {
      throw new NullPointerException();
    }
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          action.accept(p.key, p.val);
        }
      }
    }
  }

  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    if (function == null) {
      throw new NullPointerException();
    }
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          V oldValue = p.val;
          for (K key = p.key;;) {
            V newValue = function.apply(key, oldValue);
            if (newValue == null) {
              throw new NullPointerException();
            }
            if (replaceNode(key, newValue, oldValue) != null || (oldValue = get(key)) == null) {
              break;
            }
          }
        }
      }
    }
  }

  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    if (key == null || mappingFunction == null) {
      throw new NullPointerException();
    }
    V v = get(key);
    if (v != null) {
      return v;
    }
    V computed = mappingFunction.apply(key);
    if (computed == null) {
      return null;
    }
    V prior = putVal(key, computed, true);
    return prior != null ? prior : computed;
  }

  public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    if (key == null || remappingFunction == null) {
      throw new NullPointerException();
    }
    for (;;) {
      V old = get(key);
      if (old == null) {
        return null;
      }
      V nv = remappingFunction.apply(key, old);
      if (nv == null) {
        if (replaceNode(key, null, old) != null) {
          return null;
        }
      } else if (replaceNode(key, nv, old) != null) {
        return nv;
      }
    }
  }

  public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    if (key == null || remappingFunction == null) {
      throw new NullPointerException();
    }
    for (;;) {
      V old = get(key);
      V nv = remappingFunction.apply(key, old);
      if (old == null) {
        if (nv == null) {
          return null;
        }
        if (putVal(key, nv, true) == null) {
          return nv;
        }
      } else if (nv == null) {
        if (replaceNode(key, null, old) != null) {
          return null;
        }
      } else if (replaceNode(key, nv, old) != null) {
        return nv;
      }
    }
  }

  public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    if (key == null || value == null || remappingFunction == null) {
      throw new NullPointerException();
    }
    for (;;) {
      V old = get(key);
      if (old == null) {
        if (putVal(key, value, true) == null) {
          return value;
        }
      } else {
        V nv = remappingFunction.apply(old, value);
        if (nv == null) {
          if (replaceNode(key, null, old) != null) {
            return null;
          }
        } else if (replaceNode(key, nv, old) != null) {
          return nv;
        }
      }
    }
  }

  public boolean contains(Object value) {
    return containsValue(value);
  }

  public Enumeration<K> keys() {
    return new KeyIterator<K, V>(this);
  }

  public Enumeration<V> elements() {
    return new ValueIterator<K, V>(this);
  }

  public static <T> KeySetView<T, Boolean> newKeySet() {
    return new KeySetView<T, Boolean>(new ConcurrentHashMap<T, Boolean>(), Boolean.TRUE);
  }

  public static <T> KeySetView<T, Boolean> newKeySet(int initialCapacity) {
    return new KeySetView<T, Boolean>(new ConcurrentHashMap<T, Boolean>(initialCapacity), Boolean.TRUE);
  }

  public KeySetView<K, V> keySet(V mappedValue) {
    if (mappedValue == null) {
      throw new NullPointerException();
    }
    return new KeySetView<K, V>(this, mappedValue);
  }

  static class BaseIterator<K, V> {
    final ConcurrentHashMap<K, V> map;
    Node<K, V>[] tab;
    Node<K, V> next;
    int index;
    Node<K, V> lastReturned;

    BaseIterator(ConcurrentHashMap<K, V> map) {
      this.map = map;
      this.tab = map.table;
      advance();
    }

    final void advance() {
      Node<K, V> e = next;
      if (e != null) {
        e = e.next;
      }
      while (e == null) {
        if (tab == null || index >= tab.length) {
          next = null;
          return;
        }
        e = tab[index++];
      }
      next = e;
    }

    public final boolean hasNext() {
      return next != null;
    }

    public final boolean hasMoreElements() {
      return next != null;
    }

    final Node<K, V> advanceNode() {
      Node<K, V> p = next;
      if (p == null) {
        throw new NoSuchElementException();
      }
      lastReturned = p;
      advance();
      return p;
    }

    public final void remove() {
      Node<K, V> p = lastReturned;
      if (p == null) {
        throw new IllegalStateException();
      }
      lastReturned = null;
      map.replaceNode(p.key, null, null);
    }
  }

  static final class KeyIterator<K, V> extends BaseIterator<K, V> implements Iterator<K>, Enumeration<K> {
    KeyIterator(ConcurrentHashMap<K, V> map) {
      super(map);
    }

    public final K next() {
      return advanceNode().key;
    }

    public final K nextElement() {
      return next();
    }
  }

  static final class ValueIterator<K, V> extends BaseIterator<K, V> implements Iterator<V>, Enumeration<V> {
    ValueIterator(ConcurrentHashMap<K, V> map) {
      super(map);
    }

    public final V next() {
      return advanceNode().val;
    }

    public final V nextElement() {
      return next();
    }
  }

  static final class EntryIterator<K, V> extends BaseIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    EntryIterator(ConcurrentHashMap<K, V> map) {
      super(map);
    }

    public final Map.Entry<K, V> next() {
      Node<K, V> p = advanceNode();
      return new MapEntry<K, V>(p.key, p.val, map);
    }
  }

  static final class MapEntry<K, V> implements Map.Entry<K, V> {
    final K key;
    V val;
    final ConcurrentHashMap<K, V> map;

    MapEntry(K key, V val, ConcurrentHashMap<K, V> map) {
      this.key = key;
      this.val = val;
      this.map = map;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return val;
    }

    public int hashCode() {
      return key.hashCode() ^ val.hashCode();
    }

    public String toString() {
      return key + "=" + val;
    }

    public boolean equals(Object o) {
      Object k, v;
      Map.Entry<?, ?> e;
      return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null
              && (v = e.getValue()) != null && (k == key || k.equals(key)) && (v == val || v.equals(val)));
    }

    public V setValue(V value) {
      if (value == null) {
        throw new NullPointerException();
      }
      V v = val;
      val = value;
      map.put(key, value);
      return v;
    }
  }

  abstract static class CollectionView<K, V, E> implements Collection<E>, Serializable {
    final ConcurrentHashMap<K, V> map;

    CollectionView(ConcurrentHashMap<K, V> map) {
      this.map = map;
    }

    public ConcurrentHashMap<K, V> getMap() {
      return map;
    }

    public final void clear() {
      map.clear();
    }

    public final int size() {
      return map.size();
    }

    public final boolean isEmpty() {
      return map.isEmpty();
    }

    public abstract Iterator<E> iterator();

    public abstract boolean contains(Object o);

    public abstract boolean remove(Object o);

    public final Object[] toArray() {
      java.util.ArrayList<E> list = new java.util.ArrayList<E>();
      for (Iterator<E> it = iterator(); it.hasNext();) {
        list.add(it.next());
      }
      return list.toArray();
    }

    public final <T> T[] toArray(T[] a) {
      java.util.ArrayList<E> list = new java.util.ArrayList<E>();
      for (Iterator<E> it = iterator(); it.hasNext();) {
        list.add(it.next());
      }
      return list.toArray(a);
    }

    public final String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Iterator<E> it = iterator();
      if (it.hasNext()) {
        for (;;) {
          Object e = it.next();
          sb.append(e == this ? "(this Collection)" : e);
          if (!it.hasNext()) {
            break;
          }
          sb.append(',').append(' ');
        }
      }
      return sb.append(']').toString();
    }

    public final boolean containsAll(Collection<?> c) {
      if (c != this) {
        for (Object e : c) {
          if (e == null || !contains(e)) {
            return false;
          }
        }
      }
      return true;
    }

    public boolean removeAll(Collection<?> c) {
      if (c == null) {
        throw new NullPointerException();
      }
      boolean modified = false;
      for (Iterator<E> it = iterator(); it.hasNext();) {
        if (c.contains(it.next())) {
          it.remove();
          modified = true;
        }
      }
      return modified;
    }

    public final boolean retainAll(Collection<?> c) {
      if (c == null) {
        throw new NullPointerException();
      }
      boolean modified = false;
      for (Iterator<E> it = iterator(); it.hasNext();) {
        if (!c.contains(it.next())) {
          it.remove();
          modified = true;
        }
      }
      return modified;
    }
  }

  public static class KeySetView<K, V> extends CollectionView<K, V, K> implements Set<K>, Serializable {
    private final V value;

    KeySetView(ConcurrentHashMap<K, V> map, V value) {
      super(map);
      this.value = value;
    }

    public V getMappedValue() {
      return value;
    }

    public boolean contains(Object o) {
      return map.containsKey(o);
    }

    public boolean remove(Object o) {
      return map.remove(o) != null;
    }

    public Iterator<K> iterator() {
      return new KeyIterator<K, V>(map);
    }

    public boolean add(K e) {
      V v;
      if ((v = value) == null) {
        throw new UnsupportedOperationException();
      }
      return map.putVal(e, v, true) == null;
    }

    public boolean addAll(Collection<? extends K> c) {
      boolean added = false;
      V v;
      if ((v = value) == null) {
        throw new UnsupportedOperationException();
      }
      for (K e : c) {
        if (map.putVal(e, v, true) == null) {
          added = true;
        }
      }
      return added;
    }

    public int hashCode() {
      int h = 0;
      for (K e : this) {
        h += e.hashCode();
      }
      return h;
    }

    public boolean equals(Object o) {
      Set<?> c;
      return ((o instanceof Set) && ((c = (Set<?>) o) == this || (containsAll(c) && c.containsAll(this))));
    }

    public Spliterator<K> spliterator() {
      return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.CONCURRENT | Spliterator.NONNULL);
    }

    public void forEach(Consumer<? super K> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      for (Iterator<K> it = iterator(); it.hasNext();) {
        action.accept(it.next());
      }
    }
  }

  static final class ValuesView<K, V> extends CollectionView<K, V, V> {
    ValuesView(ConcurrentHashMap<K, V> map) {
      super(map);
    }

    public final boolean contains(Object o) {
      return map.containsValue(o);
    }

    public final boolean remove(Object o) {
      if (o != null) {
        for (Iterator<V> it = iterator(); it.hasNext();) {
          if (o.equals(it.next())) {
            it.remove();
            return true;
          }
        }
      }
      return false;
    }

    public final Iterator<V> iterator() {
      return new ValueIterator<K, V>(map);
    }

    public final boolean add(V e) {
      throw new UnsupportedOperationException();
    }

    public final boolean addAll(Collection<? extends V> c) {
      throw new UnsupportedOperationException();
    }

    public boolean removeIf(java.util.function.Predicate<? super V> filter) {
      if (filter == null) {
        throw new NullPointerException();
      }
      boolean removed = false;
      for (Iterator<V> it = iterator(); it.hasNext();) {
        if (filter.test(it.next())) {
          it.remove();
          removed = true;
        }
      }
      return removed;
    }

    public void forEach(Consumer<? super V> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      for (Iterator<V> it = iterator(); it.hasNext();) {
        action.accept(it.next());
      }
    }
  }

  static final class EntrySetView<K, V> extends CollectionView<K, V, Map.Entry<K, V>> implements Set<Map.Entry<K, V>> {
    EntrySetView(ConcurrentHashMap<K, V> map) {
      super(map);
    }

    public boolean contains(Object o) {
      Object k, v, r;
      Map.Entry<?, ?> e;
      return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null && (r = map.get(k)) != null
              && (v = e.getValue()) != null && (v == r || v.equals(r)));
    }

    public boolean remove(Object o) {
      Object k, v;
      Map.Entry<?, ?> e;
      return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null
              && (v = e.getValue()) != null && map.remove(k, v));
    }

    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator<K, V>(map);
    }

    public boolean add(Map.Entry<K, V> e) {
      return map.putVal(e.getKey(), e.getValue(), false) == null;
    }

    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
      boolean added = false;
      for (Map.Entry<K, V> e : c) {
        if (add(e)) {
          added = true;
        }
      }
      return added;
    }

    public boolean removeIf(java.util.function.Predicate<? super Map.Entry<K, V>> filter) {
      if (filter == null) {
        throw new NullPointerException();
      }
      boolean removed = false;
      for (Iterator<Map.Entry<K, V>> it = iterator(); it.hasNext();) {
        if (filter.test(it.next())) {
          it.remove();
          removed = true;
        }
      }
      return removed;
    }

    public final int hashCode() {
      int h = 0;
      for (Iterator<Map.Entry<K, V>> it = iterator(); it.hasNext();) {
        h += it.next().hashCode();
      }
      return h;
    }

    public final boolean equals(Object o) {
      Set<?> c;
      return ((o instanceof Set) && ((c = (Set<?>) o) == this || (containsAll(c) && c.containsAll(this))));
    }

    public void forEach(Consumer<? super Map.Entry<K, V>> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      for (Iterator<Map.Entry<K, V>> it = iterator(); it.hasNext();) {
        action.accept(it.next());
      }
    }
  }

  public void forEach(long parallelismThreshold, BiConsumer<? super K, ? super V> action) {
    forEach(action);
  }

  public <U> void forEach(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer,
                          Consumer<? super U> action) {
    if (transformer == null || action == null) {
      throw new NullPointerException();
    }
    forEach((k, v) -> {
      U u = transformer.apply(k, v);
      if (u != null) {
        action.accept(u);
      }
    });
  }

  public <U> U search(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> searchFunction) {
    if (searchFunction == null) {
      throw new NullPointerException();
    }
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          U u = searchFunction.apply(p.key, p.val);
          if (u != null) {
            return u;
          }
        }
      }
    }
    return null;
  }

  public <U> U reduce(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer,
                      BiFunction<? super U, ? super U, ? extends U> reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    U r = null;
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          U u = transformer.apply(p.key, p.val);
          if (u != null) {
            r = (r == null) ? u : reducer.apply(r, u);
          }
        }
      }
    }
    return r;
  }

  public double reduceToDouble(long parallelismThreshold, ToDoubleBiFunction<? super K, ? super V> transformer,
                               double basis, DoubleBinaryOperator reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    double r = basis;
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
        }
      }
    }
    return r;
  }

  public long reduceToLong(long parallelismThreshold, ToLongBiFunction<? super K, ? super V> transformer,
                           long basis, LongBinaryOperator reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    long r = basis;
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
        }
      }
    }
    return r;
  }

  public int reduceToInt(long parallelismThreshold, ToIntBiFunction<? super K, ? super V> transformer,
                         int basis, IntBinaryOperator reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    int r = basis;
    Node<K, V>[] t;
    if ((t = table) != null) {
      for (Node<K, V> head : t) {
        for (Node<K, V> p = head; p != null; p = p.next) {
          r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
        }
      }
    }
    return r;
  }

  public void forEachKey(long parallelismThreshold, Consumer<? super K> action) {
    keySet().forEach(action);
  }

  public <U> void forEachKey(long parallelismThreshold, Function<? super K, ? extends U> transformer,
                             Consumer<? super U> action) {
    if (transformer == null || action == null) {
      throw new NullPointerException();
    }
    forEach((k, v) -> {
      U u = transformer.apply(k);
      if (u != null) {
        action.accept(u);
      }
    });
  }

  public <U> U searchKeys(long parallelismThreshold, Function<? super K, ? extends U> searchFunction) {
    if (searchFunction == null) {
      throw new NullPointerException();
    }
    return search(parallelismThreshold, (k, v) -> searchFunction.apply(k));
  }

  public K reduceKeys(long parallelismThreshold, BiFunction<? super K, ? super K, ? extends K> reducer) {
    if (reducer == null) {
      throw new NullPointerException();
    }
    return reduce(parallelismThreshold, (k, v) -> k, reducer);
  }

  public <U> U reduceKeys(long parallelismThreshold, Function<? super K, ? extends U> transformer,
                          BiFunction<? super U, ? super U, ? extends U> reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    return reduce(parallelismThreshold, (k, v) -> transformer.apply(k), reducer);
  }

  public double reduceKeysToDouble(long parallelismThreshold, ToDoubleFunction<? super K> transformer, double basis,
                                   DoubleBinaryOperator reducer) {
    return reduceToDouble(parallelismThreshold, (k, v) -> transformer.applyAsDouble(k), basis, reducer);
  }

  public long reduceKeysToLong(long parallelismThreshold, ToLongFunction<? super K> transformer, long basis,
                               LongBinaryOperator reducer) {
    return reduceToLong(parallelismThreshold, (k, v) -> transformer.applyAsLong(k), basis, reducer);
  }

  public int reduceKeysToInt(long parallelismThreshold, ToIntFunction<? super K> transformer, int basis,
                             IntBinaryOperator reducer) {
    return reduceToInt(parallelismThreshold, (k, v) -> transformer.applyAsInt(k), basis, reducer);
  }

  public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
    values().forEach(action);
  }

  public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer,
                               Consumer<? super U> action) {
    if (transformer == null || action == null) {
      throw new NullPointerException();
    }
    forEach((k, v) -> {
      U u = transformer.apply(v);
      if (u != null) {
        action.accept(u);
      }
    });
  }

  public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
    if (searchFunction == null) {
      throw new NullPointerException();
    }
    return search(parallelismThreshold, (k, v) -> searchFunction.apply(v));
  }

  public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
    if (reducer == null) {
      throw new NullPointerException();
    }
    return reduce(parallelismThreshold, (k, v) -> v, reducer);
  }

  public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer,
                            BiFunction<? super U, ? super U, ? extends U> reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    return reduce(parallelismThreshold, (k, v) -> transformer.apply(v), reducer);
  }

  public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis,
                                     DoubleBinaryOperator reducer) {
    return reduceToDouble(parallelismThreshold, (k, v) -> transformer.applyAsDouble(v), basis, reducer);
  }

  public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super V> transformer, long basis,
                                 LongBinaryOperator reducer) {
    return reduceToLong(parallelismThreshold, (k, v) -> transformer.applyAsLong(v), basis, reducer);
  }

  public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super V> transformer, int basis,
                               IntBinaryOperator reducer) {
    return reduceToInt(parallelismThreshold, (k, v) -> transformer.applyAsInt(v), basis, reducer);
  }

  public void forEachEntry(long parallelismThreshold, Consumer<? super Map.Entry<K, V>> action) {
    entrySet().forEach(action);
  }

  public <U> void forEachEntry(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> transformer,
                               Consumer<? super U> action) {
    if (transformer == null || action == null) {
      throw new NullPointerException();
    }
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      U u = transformer.apply(it.next());
      if (u != null) {
        action.accept(u);
      }
    }
  }

  public <U> U searchEntries(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> searchFunction) {
    if (searchFunction == null) {
      throw new NullPointerException();
    }
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      U u = searchFunction.apply(it.next());
      if (u != null) {
        return u;
      }
    }
    return null;
  }

  public Map.Entry<K, V> reduceEntries(long parallelismThreshold,
                                       BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer) {
    if (reducer == null) {
      throw new NullPointerException();
    }
    Map.Entry<K, V> r = null;
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      Map.Entry<K, V> u = it.next();
      r = (r == null) ? u : reducer.apply(r, u);
    }
    return r;
  }

  public <U> U reduceEntries(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> transformer,
                             BiFunction<? super U, ? super U, ? extends U> reducer) {
    if (transformer == null || reducer == null) {
      throw new NullPointerException();
    }
    U r = null;
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      U u = transformer.apply(it.next());
      if (u != null) {
        r = (r == null) ? u : reducer.apply(r, u);
      }
    }
    return r;
  }

  public double reduceEntriesToDouble(long parallelismThreshold, ToDoubleFunction<Map.Entry<K, V>> transformer,
                                      double basis, DoubleBinaryOperator reducer) {
    double r = basis;
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      r = reducer.applyAsDouble(r, transformer.applyAsDouble(it.next()));
    }
    return r;
  }

  public long reduceEntriesToLong(long parallelismThreshold, ToLongFunction<Map.Entry<K, V>> transformer, long basis,
                                  LongBinaryOperator reducer) {
    long r = basis;
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      r = reducer.applyAsLong(r, transformer.applyAsLong(it.next()));
    }
    return r;
  }

  public int reduceEntriesToInt(long parallelismThreshold, ToIntFunction<Map.Entry<K, V>> transformer, int basis,
                                IntBinaryOperator reducer) {
    int r = basis;
    for (Iterator<Map.Entry<K, V>> it = new EntryIterator<K, V>(this); it.hasNext();) {
      r = reducer.applyAsInt(r, transformer.applyAsInt(it.next()));
    }
    return r;
  }
}
