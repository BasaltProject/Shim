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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TreeMap<K, V> extends AbstractMap<K, V>
    implements NavigableMap<K, V>, Cloneable, Serializable {

  private static final boolean RED = false;
  private static final boolean BLACK = true;

  public static final long serialVersionUID = 0x0cc1f63e2d256ae6L;

  private final Comparator<? super K> comparator;
  private transient Entry<K, V> root;
  private transient int size = 0;
  private transient int modCount = 0;

  public TreeMap() {
    this((Comparator<? super K>) null);
  }

  public TreeMap(Comparator<? super K> comparator) {
    this.comparator = comparator;
  }

  public TreeMap(Map<? extends K, ? extends V> m) {
    this((Comparator<? super K>) null);
    putAll(m);
  }

  public TreeMap(SortedMap<K, ? extends V> m) {
    this(m.comparator());
    putAll(m);
  }

  @SuppressWarnings("unchecked")
  final int compare(Object k1, Object k2) {
    return comparator == null
        ? ((Comparable<? super Object>) k1).compareTo(k2)
        : ((Comparator<? super Object>) comparator).compare(k1, k2);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return getEntry(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
      if (Objects.equals(value, e.value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    Entry<K, V> p = getEntry(key);
    return (p == null ? null : p.value);
  }

  @Override
  public Comparator<? super K> comparator() {
    return comparator;
  }

  @Override
  public K firstKey() {
    Entry<K, V> p = getFirstEntry();
    if (p == null) {
      throw new NoSuchElementException();
    }
    return p.key;
  }

  @Override
  public K lastKey() {
    Entry<K, V> p = getLastEntry();
    if (p == null) {
      throw new NoSuchElementException();
    }
    return p.key;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  final Entry<K, V> getEntry(Object key) {
    if (key == null && comparator == null) {
      throw new NullPointerException("key is null");
    }
    Entry<K, V> p = root;
    while (p != null) {
      int cmp = compare(key, p.key);
      if (cmp < 0) {
        p = p.left;
      } else if (cmp > 0) {
        p = p.right;
      } else {
        return p;
      }
    }
    return null;
  }

  final Entry<K, V> getCeilingEntry(K key) {
    if (key == null && comparator == null) {
      throw new NullPointerException("key is null");
    }
    Entry<K, V> p = root;
    while (p != null) {
      int cmp = compare(key, p.key);
      if (cmp < 0) {
        if (p.left != null) {
          p = p.left;
        } else {
          return p;
        }
      } else if (cmp > 0) {
        if (p.right != null) {
          p = p.right;
        } else {
          Entry<K, V> parent = p.parent;
          Entry<K, V> ch = p;
          while (parent != null && ch == parent.right) {
            ch = parent;
            parent = parent.parent;
          }
          return parent;
        }
      } else {
        return p;
      }
    }
    return null;
  }

  final Entry<K, V> getFloorEntry(K key) {
    if (key == null && comparator == null) {
      throw new NullPointerException("key is null");
    }
    Entry<K, V> p = root;
    while (p != null) {
      int cmp = compare(key, p.key);
      if (cmp > 0) {
        if (p.right != null) {
          p = p.right;
        } else {
          return p;
        }
      } else if (cmp < 0) {
        if (p.left != null) {
          p = p.left;
        } else {
          Entry<K, V> parent = p.parent;
          Entry<K, V> ch = p;
          while (parent != null && ch == parent.left) {
            ch = parent;
            parent = parent.parent;
          }
          return parent;
        }
      } else {
        return p;
      }
    }
    return null;
  }

  final Entry<K, V> getHigherEntry(K key) {
    if (key == null && comparator == null) {
      throw new NullPointerException("key is null");
    }
    Entry<K, V> p = root;
    while (p != null) {
      int cmp = compare(key, p.key);
      if (cmp < 0) {
        if (p.left != null) {
          p = p.left;
        } else {
          return p;
        }
      } else {
        if (p.right != null) {
          p = p.right;
        } else {
          Entry<K, V> parent = p.parent;
          Entry<K, V> ch = p;
          while (parent != null && ch == parent.right) {
            ch = parent;
            parent = parent.parent;
          }
          return parent;
        }
      }
    }
    return null;
  }

  final Entry<K, V> getLowerEntry(K key) {
    if (key == null && comparator == null) {
      throw new NullPointerException("key is null");
    }
    Entry<K, V> p = root;
    while (p != null) {
      int cmp = compare(key, p.key);
      if (cmp > 0) {
        if (p.right != null) {
          p = p.right;
        } else {
          return p;
        }
      } else {
        if (p.left != null) {
          p = p.left;
        } else {
          Entry<K, V> parent = p.parent;
          Entry<K, V> ch = p;
          while (parent != null && ch == parent.left) {
            ch = parent;
            parent = parent.parent;
          }
          return parent;
        }
      }
    }
    return null;
  }

  @Override
  public V put(K key, V value) {
    if (key == null && comparator == null) {
      throw new NullPointerException("key is null");
    }
    Entry<K, V> t = root;
    if (t == null) {
      compare(key, key); // type check
      root = new Entry<K, V>(key, value, null);
      size = 1;
      modCount++;
      return null;
    }
    int cmp;
    Entry<K, V> parent;
    do {
      parent = t;
      cmp = compare(key, t.key);
      if (cmp < 0) {
        t = t.left;
      } else if (cmp > 0) {
        t = t.right;
      } else {
        V oldValue = t.value;
        t.value = value;
        return oldValue;
      }
    } while (t != null);

    Entry<K, V> e = new Entry<K, V>(key, value, parent);
    if (cmp < 0) {
      parent.left = e;
    } else {
      parent.right = e;
    }
    fixAfterInsertion(e);
    size++;
    modCount++;
    return null;
  }

  @Override
  public V remove(Object key) {
    Entry<K, V> p = getEntry(key);
    if (p == null) {
      return null;
    }
    V oldValue = p.value;
    deleteEntry(p);
    return oldValue;
  }

  @Override
  public void clear() {
    modCount++;
    size = 0;
    root = null;
  }

  @Override
  public Object clone() {
    TreeMap<K, V> clone;
    try {
      clone = (TreeMap<K, V>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
    clone.root = null;
    clone.size = 0;
    clone.modCount = 0;
    clone.putAll(this);
    return clone;
  }

  @Override
  public Map.Entry<K, V> firstEntry() {
    return exportEntry(getFirstEntry());
  }

  @Override
  public Map.Entry<K, V> lastEntry() {
    return exportEntry(getLastEntry());
  }

  @Override
  public Map.Entry<K, V> pollFirstEntry() {
    Entry<K, V> p = getFirstEntry();
    Map.Entry<K, V> result = exportEntry(p);
    if (p != null) {
      deleteEntry(p);
    }
    return result;
  }

  @Override
  public Map.Entry<K, V> pollLastEntry() {
    Entry<K, V> p = getLastEntry();
    Map.Entry<K, V> result = exportEntry(p);
    if (p != null) {
      deleteEntry(p);
    }
    return result;
  }

  @Override
  public Map.Entry<K, V> lowerEntry(K key) {
    return exportEntry(getLowerEntry(key));
  }

  @Override
  public K lowerKey(K key) {
    Entry<K, V> p = getLowerEntry(key);
    return p == null ? null : p.key;
  }

  @Override
  public Map.Entry<K, V> floorEntry(K key) {
    return exportEntry(getFloorEntry(key));
  }

  @Override
  public K floorKey(K key) {
    Entry<K, V> p = getFloorEntry(key);
    return p == null ? null : p.key;
  }

  @Override
  public Map.Entry<K, V> ceilingEntry(K key) {
    return exportEntry(getCeilingEntry(key));
  }

  @Override
  public K ceilingKey(K key) {
    Entry<K, V> p = getCeilingEntry(key);
    return p == null ? null : p.key;
  }

  @Override
  public Map.Entry<K, V> higherEntry(K key) {
    return exportEntry(getHigherEntry(key));
  }

  @Override
  public K higherKey(K key) {
    Entry<K, V> p = getHigherEntry(key);
    return p == null ? null : p.key;
  }

  @Override
  public Set<K> keySet() {
    return navigableKeySet();
  }

  @Override
  public NavigableSet<K> navigableKeySet() {
    return new KeySet<K>(this);
  }

  @Override
  public NavigableSet<K> descendingKeySet() {
    return descendingMap().navigableKeySet();
  }

  @Override
  public Collection<V> values() {
    return new Values();
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  @Override
  public NavigableMap<K, V> descendingMap() {
    return new SubMap<K, V>(this, true, null, true, true, null, true, true);
  }

  @Override
  public NavigableMap<K, V> subMap(
      K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
    return new SubMap<K, V>(
        this, false, fromKey, fromInclusive, false, toKey, toInclusive, false);
  }

  @Override
  public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
    return new SubMap<K, V>(
        this, true, null, true, false, toKey, inclusive, false);
  }

  @Override
  public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
    return new SubMap<K, V>(
        this, false, fromKey, inclusive, true, null, true, false);
  }

  @Override
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public SortedMap<K, V> headMap(K toKey) {
    return headMap(toKey, false);
  }

  @Override
  public SortedMap<K, V> tailMap(K fromKey) {
    return tailMap(fromKey, true);
  }

  final Entry<K, V> getFirstEntry() {
    Entry<K, V> p = root;
    if (p != null) {
      while (p.left != null) {
        p = p.left;
      }
    }
    return p;
  }

  final Entry<K, V> getLastEntry() {
    Entry<K, V> p = root;
    if (p != null) {
      while (p.right != null) {
        p = p.right;
      }
    }
    return p;
  }

  static <K, V> Entry<K, V> successor(Entry<K, V> t) {
    if (t == null) {
      return null;
    } else if (t.right != null) {
      Entry<K, V> p = t.right;
      while (p.left != null) {
        p = p.left;
      }
      return p;
    } else {
      Entry<K, V> p = t.parent;
      Entry<K, V> ch = t;
      while (p != null && ch == p.right) {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  static <K, V> Entry<K, V> predecessor(Entry<K, V> t) {
    if (t == null) {
      return null;
    } else if (t.left != null) {
      Entry<K, V> p = t.left;
      while (p.right != null) {
        p = p.right;
      }
      return p;
    } else {
      Entry<K, V> p = t.parent;
      Entry<K, V> ch = t;
      while (p != null && ch == p.left) {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  static <K, V> Map.Entry<K, V> exportEntry(Entry<K, V> e) {
    return e == null ? null : new AbstractMap.SimpleImmutableEntry<K, V>(e);
  }

  private static <K, V> boolean colorOf(Entry<K, V> p) {
    return (p == null ? BLACK : p.color);
  }

  private static <K, V> Entry<K, V> parentOf(Entry<K, V> p) {
    return (p == null ? null : p.parent);
  }

  private static <K, V> void setColor(Entry<K, V> p, boolean c) {
    if (p != null) {
      p.color = c;
    }
  }

  private static <K, V> Entry<K, V> leftOf(Entry<K, V> p) {
    return (p == null ? null : p.left);
  }

  private static <K, V> Entry<K, V> rightOf(Entry<K, V> p) {
    return (p == null ? null : p.right);
  }

  private void rotateLeft(Entry<K, V> p) {
    if (p != null) {
      Entry<K, V> r = p.right;
      p.right = r.left;
      if (r.left != null) {
        r.left.parent = p;
      }
      r.parent = p.parent;
      if (p.parent == null) {
        root = r;
      } else if (p.parent.left == p) {
        p.parent.left = r;
      } else {
        p.parent.right = r;
      }
      r.left = p;
      p.parent = r;
    }
  }

  private void rotateRight(Entry<K, V> p) {
    if (p != null) {
      Entry<K, V> l = p.left;
      p.left = l.right;
      if (l.right != null) {
        l.right.parent = p;
      }
      l.parent = p.parent;
      if (p.parent == null) {
        root = l;
      } else if (p.parent.right == p) {
        p.parent.right = l;
      } else {
        p.parent.left = l;
      }
      l.right = p;
      p.parent = l;
    }
  }

  private void fixAfterInsertion(Entry<K, V> x) {
    x.color = RED;

    while (x != null && x != root && x.parent.color == RED) {
      if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
        Entry<K, V> y = rightOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if (x == rightOf(parentOf(x))) {
            x = parentOf(x);
            rotateLeft(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          rotateRight(parentOf(parentOf(x)));
        }
      } else {
        Entry<K, V> y = leftOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if (x == leftOf(parentOf(x))) {
            x = parentOf(x);
            rotateRight(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          rotateLeft(parentOf(parentOf(x)));
        }
      }
    }
    root.color = BLACK;
  }

  void deleteEntry(Entry<K, V> p) {
    modCount++;
    size--;

    if (p.left != null && p.right != null) {
      Entry<K, V> s = successor(p);
      p.key = s.key;
      p.value = s.value;
      p = s;
    }

    Entry<K, V> replacement = (p.left != null ? p.left : p.right);

    if (replacement != null) {
      replacement.parent = p.parent;
      if (p.parent == null) {
        root = replacement;
      } else if (p == p.parent.left) {
        p.parent.left = replacement;
      } else {
        p.parent.right = replacement;
      }

      p.left = p.right = p.parent = null;

      if (p.color == BLACK) {
        fixAfterDeletion(replacement);
      }
    } else if (p.parent == null) {
      root = null;
    } else {
      if (p.color == BLACK) {
        fixAfterDeletion(p);
      }

      if (p.parent != null) {
        if (p == p.parent.left) {
          p.parent.left = null;
        } else if (p == p.parent.right) {
          p.parent.right = null;
        }
        p.parent = null;
      }
    }
  }

  private void fixAfterDeletion(Entry<K, V> x) {
    while (x != root && colorOf(x) == BLACK) {
      if (x == leftOf(parentOf(x))) {
        Entry<K, V> sib = rightOf(parentOf(x));

        if (colorOf(sib) == RED) {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateLeft(parentOf(x));
          sib = rightOf(parentOf(x));
        }

        if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(rightOf(sib)) == BLACK) {
            setColor(leftOf(sib), BLACK);
            setColor(sib, RED);
            rotateRight(sib);
            sib = rightOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(rightOf(sib), BLACK);
          rotateLeft(parentOf(x));
          x = root;
        }
      } else {
        Entry<K, V> sib = leftOf(parentOf(x));

        if (colorOf(sib) == RED) {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateRight(parentOf(x));
          sib = leftOf(parentOf(x));
        }

        if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(leftOf(sib)) == BLACK) {
            setColor(rightOf(sib), BLACK);
            setColor(sib, RED);
            rotateLeft(sib);
            sib = leftOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(leftOf(sib), BLACK);
          rotateRight(parentOf(x));
          x = root;
        }
      }
    }

    setColor(x, BLACK);
  }

  static final class Entry<K, V> implements Map.Entry<K, V> {
    K key;
    V value;
    Entry<K, V> left;
    Entry<K, V> right;
    Entry<K, V> parent;
    boolean color = BLACK;

    Entry(K key, V value, Entry<K, V> parent) {
      this.key = key;
      this.value = value;
      this.parent = parent;
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

  abstract class PrivateEntryIterator<T> implements Iterator<T> {
    Entry<K, V> next;
    Entry<K, V> lastReturned;
    int expectedModCount;

    PrivateEntryIterator(Entry<K, V> first) {
      expectedModCount = modCount;
      lastReturned = null;
      next = first;
    }

    @Override
    public final boolean hasNext() {
      return next != null;
    }

    final Entry<K, V> nextEntry() {
      Entry<K, V> e = next;
      if (e == null) {
        throw new NoSuchElementException();
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      next = successor(e);
      lastReturned = e;
      return e;
    }

    final Entry<K, V> prevEntry() {
      Entry<K, V> e = next;
      if (e == null) {
        throw new NoSuchElementException();
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      next = predecessor(e);
      lastReturned = e;
      return e;
    }

    @Override
    public void remove() {
      if (lastReturned == null) {
        throw new IllegalStateException();
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (lastReturned.left != null && lastReturned.right != null) {
        next = lastReturned;
      }
      deleteEntry(lastReturned);
      expectedModCount = modCount;
      lastReturned = null;
    }
  }

  final class EntryIterator extends PrivateEntryIterator<Map.Entry<K, V>> {
    EntryIterator(Entry<K, V> first) {
      super(first);
    }

    @Override
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }

  final class KeyIterator extends PrivateEntryIterator<K> {
    KeyIterator(Entry<K, V> first) {
      super(first);
    }

    @Override
    public K next() {
      return nextEntry().key;
    }
  }

  final class DescendingKeyIterator extends PrivateEntryIterator<K> {
    DescendingKeyIterator(Entry<K, V> last) {
      super(last);
    }

    @Override
    public K next() {
      return prevEntry().key;
    }
  }

  final class ValueIterator extends PrivateEntryIterator<V> {
    ValueIterator(Entry<K, V> first) {
      super(first);
    }

    @Override
    public V next() {
      return nextEntry().value;
    }
  }

  class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator(getFirstEntry());
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      Entry<K, V> p = getEntry(entry.getKey());
      return p != null && Objects.equals(p.getValue(), entry.getValue());
    }

    @Override
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      Entry<K, V> p = getEntry(entry.getKey());
      if (p != null && Objects.equals(p.getValue(), entry.getValue())) {
        deleteEntry(p);
        return true;
      }
      return false;
    }

    @Override
    public int size() {
      return TreeMap.this.size();
    }

    @Override
    public void clear() {
      TreeMap.this.clear();
    }
  }

  static class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final NavigableMap<E, ?> m;

    KeySet(NavigableMap<E, ?> map) {
      m = map;
    }

    @Override
    public Iterator<E> iterator() {
      return new Iterator<E>() {
        private final Iterator<? extends Map.Entry<E, ?>> it = m.entrySet().iterator();

        @Override
        public boolean hasNext() {
          return it.hasNext();
        }

        @Override
        public E next() {
          return it.next().getKey();
        }

        @Override
        public void remove() {
          it.remove();
        }
      };
    }

    @Override
    public Iterator<E> descendingIterator() {
      return descendingSet().iterator();
    }

    @Override
    public int size() {
      return m.size();
    }

    @Override
    public boolean isEmpty() {
      return m.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return m.containsKey(o);
    }

    @Override
    public void clear() {
      m.clear();
    }

    @Override
    public E lower(E e) {
      return m.lowerKey(e);
    }

    @Override
    public E floor(E e) {
      return m.floorKey(e);
    }

    @Override
    public E ceiling(E e) {
      return m.ceilingKey(e);
    }

    @Override
    public E higher(E e) {
      return m.higherKey(e);
    }

    @Override
    public E first() {
      return m.firstKey();
    }

    @Override
    public E last() {
      return m.lastKey();
    }

    @Override
    public Comparator<? super E> comparator() {
      return m.comparator();
    }

    @Override
    public E pollFirst() {
      Map.Entry<E, ?> e = m.pollFirstEntry();
      return e == null ? null : e.getKey();
    }

    @Override
    public E pollLast() {
      Map.Entry<E, ?> e = m.pollLastEntry();
      return e == null ? null : e.getKey();
    }

    @Override
    public boolean remove(Object o) {
      int oldSize = size();
      m.remove(o);
      return size() != oldSize;
    }

    @Override
    public NavigableSet<E> subSet(
        E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      return new KeySet<E>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      return new KeySet<E>(m.headMap(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      return new KeySet<E>(m.tailMap(fromElement, inclusive));
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
      return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
      return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
      return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<E> descendingSet() {
      return new KeySet<E>(m.descendingMap());
    }
  }

  class Values extends AbstractCollection<V> {
    @Override
    public Iterator<V> iterator() {
      return new ValueIterator(getFirstEntry());
    }

    @Override
    public int size() {
      return TreeMap.this.size();
    }

    @Override
    public boolean contains(Object o) {
      return TreeMap.this.containsValue(o);
    }

    @Override
    public boolean remove(Object o) {
      for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
        if (Objects.equals(o, e.value)) {
          deleteEntry(e);
          return true;
        }
      }
      return false;
    }

    @Override
    public void clear() {
      TreeMap.this.clear();
    }
  }

  static final class SubMap<K, V> extends AbstractMap<K, V>
      implements NavigableMap<K, V>, Serializable {

    private final TreeMap<K, V> m;
    private final boolean fromStart;
    private final K lo;
    private final boolean loInclusive;
    private final boolean toEnd;
    private final K hi;
    private final boolean hiInclusive;
    private final boolean isDescending;

    SubMap(
        TreeMap<K, V> m,
        boolean fromStart, K lo, boolean loInclusive,
        boolean toEnd, K hi, boolean hiInclusive,
        boolean isDescending) {
      if (!fromStart && !toEnd) {
        if (m.compare(lo, hi) > 0) {
          throw new IllegalArgumentException("fromKey > toKey");
        }
      }
      this.m = m;
      this.fromStart = fromStart;
      this.lo = lo;
      this.loInclusive = loInclusive;
      this.toEnd = toEnd;
      this.hi = hi;
      this.hiInclusive = hiInclusive;
      this.isDescending = isDescending;
    }

    boolean tooLow(Object key) {
      if (!fromStart) {
        int c = m.compare(key, lo);
        if (c < 0 || (c == 0 && !loInclusive)) {
          return true;
        }
      }
      return false;
    }

    boolean tooHigh(Object key) {
      if (!toEnd) {
        int c = m.compare(key, hi);
        if (c > 0 || (c == 0 && !hiInclusive)) {
          return true;
        }
      }
      return false;
    }

    boolean inRange(Object key) {
      return !tooLow(key) && !tooHigh(key);
    }

    TreeMap.Entry<K, V> subLowest() {
      TreeMap.Entry<K, V> e = fromStart
          ? m.getFirstEntry()
          : (loInclusive ? m.getCeilingEntry(lo) : m.getHigherEntry(lo));
      return (e == null || tooHigh(e.key)) ? null : e;
    }

    TreeMap.Entry<K, V> subHighest() {
      TreeMap.Entry<K, V> e = toEnd
          ? m.getLastEntry()
          : (hiInclusive ? m.getFloorEntry(hi) : m.getLowerEntry(hi));
      return (e == null || tooLow(e.key)) ? null : e;
    }

    TreeMap.Entry<K, V> subCeiling(K key) {
      if (tooLow(key)) {
        return subLowest();
      }
      TreeMap.Entry<K, V> e = m.getCeilingEntry(key);
      return (e == null || tooHigh(e.key)) ? null : e;
    }

    TreeMap.Entry<K, V> subFloor(K key) {
      if (tooHigh(key)) {
        return subHighest();
      }
      TreeMap.Entry<K, V> e = m.getFloorEntry(key);
      return (e == null || tooLow(e.key)) ? null : e;
    }

    TreeMap.Entry<K, V> subHigher(K key) {
      if (tooLow(key)) {
        return subLowest();
      }
      TreeMap.Entry<K, V> e = m.getHigherEntry(key);
      return (e == null || tooHigh(e.key)) ? null : e;
    }

    TreeMap.Entry<K, V> subLower(K key) {
      if (tooHigh(key)) {
        return subHighest();
      }
      TreeMap.Entry<K, V> e = m.getLowerEntry(key);
      return (e == null || tooLow(e.key)) ? null : e;
    }

    @Override
    public boolean isEmpty() {
      return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
    }

    @Override
    public int size() {
      return (fromStart && toEnd) ? m.size() : entrySet().size();
    }

    @Override
    public boolean containsKey(Object key) {
      return inRange(key) && m.containsKey(key);
    }

    @Override
    public V get(Object key) {
      return !inRange(key) ? null : m.get(key);
    }

    @Override
    public V put(K key, V value) {
      if (!inRange(key)) {
        throw new IllegalArgumentException("key out of range");
      }
      return m.put(key, value);
    }

    @Override
    public V remove(Object key) {
      return !inRange(key) ? null : m.remove(key);
    }

    @Override
    public Comparator<? super K> comparator() {
      Comparator<? super K> comp = m.comparator();
      return isDescending ? Collections.reverseOrder(comp) : comp;
    }

    @Override
    public K firstKey() {
      Map.Entry<K, V> e = firstEntry();
      if (e == null) {
        throw new NoSuchElementException();
      }
      return e.getKey();
    }

    @Override
    public K lastKey() {
      Map.Entry<K, V> e = lastEntry();
      if (e == null) {
        throw new NoSuchElementException();
      }
      return e.getKey();
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
      return exportEntry(isDescending ? subHighest() : subLowest());
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
      return exportEntry(isDescending ? subLowest() : subHighest());
    }

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
      TreeMap.Entry<K, V> e = isDescending ? subHighest() : subLowest();
      Map.Entry<K, V> res = exportEntry(e);
      if (e != null) {
        m.deleteEntry(e);
      }
      return res;
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
      TreeMap.Entry<K, V> e = isDescending ? subLowest() : subHighest();
      Map.Entry<K, V> res = exportEntry(e);
      if (e != null) {
        m.deleteEntry(e);
      }
      return res;
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K key) {
      return exportEntry(isDescending ? subHigher(key) : subLower(key));
    }

    @Override
    public K lowerKey(K key) {
      TreeMap.Entry<K, V> e = isDescending ? subHigher(key) : subLower(key);
      return e == null ? null : e.key;
    }

    @Override
    public Map.Entry<K, V> floorEntry(K key) {
      return exportEntry(isDescending ? subCeiling(key) : subFloor(key));
    }

    @Override
    public K floorKey(K key) {
      TreeMap.Entry<K, V> e = isDescending ? subCeiling(key) : subFloor(key);
      return e == null ? null : e.key;
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
      return exportEntry(isDescending ? subFloor(key) : subCeiling(key));
    }

    @Override
    public K ceilingKey(K key) {
      TreeMap.Entry<K, V> e = isDescending ? subFloor(key) : subCeiling(key);
      return e == null ? null : e.key;
    }

    @Override
    public Map.Entry<K, V> higherEntry(K key) {
      return exportEntry(isDescending ? subLower(key) : subHigher(key));
    }

    @Override
    public K higherKey(K key) {
      TreeMap.Entry<K, V> e = isDescending ? subLower(key) : subHigher(key);
      return e == null ? null : e.key;
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
      return new SubMap<K, V>(
          m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive, !isDescending);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
      return new KeySet<K>(this);
    }

    @Override
    public Set<K> keySet() {
      return navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
      return descendingMap().navigableKeySet();
    }

    boolean inClosedRange(Object key) {
      return (fromStart || m.compare(key, lo) >= 0) && (toEnd || m.compare(hi, key) >= 0);
    }

    boolean inRange(Object key, boolean inclusive) {
      return inclusive ? inRange(key) : inClosedRange(key);
    }

    @Override
    public NavigableMap<K, V> subMap(
        K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      if (!inRange(fromKey, fromInclusive)) {
        throw new IllegalArgumentException("fromKey out of range");
      }
      if (!inRange(toKey, toInclusive)) {
        throw new IllegalArgumentException("toKey out of range");
      }
      if (isDescending) {
        return new SubMap<K, V>(m, false, toKey, toInclusive, false, fromKey, fromInclusive, true);
      }
      return new SubMap<K, V>(
          m, false, fromKey, fromInclusive, false, toKey, toInclusive, false);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      if (!inRange(toKey, inclusive)) {
        throw new IllegalArgumentException("toKey out of range");
      }
      if (isDescending) {
        return new SubMap<K, V>(m, false, toKey, inclusive, toEnd, hi, hiInclusive, true);
      }
      return new SubMap<K, V>(
          m, fromStart, lo, loInclusive, false, toKey, inclusive, false);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      if (!inRange(fromKey, inclusive)) {
        throw new IllegalArgumentException("fromKey out of range");
      }
      if (isDescending) {
        return new SubMap<K, V>(m, fromStart, lo, loInclusive, false, fromKey, inclusive, true);
      }
      return new SubMap<K, V>(
          m, false, fromKey, inclusive, toEnd, hi, hiInclusive, false);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
      return headMap(toKey, false);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
      return tailMap(fromKey, true);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
      return new EntrySetView();
    }

    class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
      @Override
      public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<Map.Entry<K, V>>() {
          private TreeMap.Entry<K, V> current = isDescending ? subHighest() : subLowest();
          private TreeMap.Entry<K, V> lastReturned = null;

          @Override
          public boolean hasNext() {
            return current != null;
          }

          @Override
          public Map.Entry<K, V> next() {
            if (current == null) {
              throw new NoSuchElementException();
            }
            TreeMap.Entry<K, V> ret = current;
            lastReturned = ret;
            if (isDescending) {
              current = predecessor(ret);
              if (current != null && tooLow(current.key)) {
                current = null;
              }
            } else {
              current = successor(ret);
              if (current != null && tooHigh(current.key)) {
                current = null;
              }
            }
            return ret;
          }

          @Override
          public void remove() {
            if (lastReturned == null) {
              throw new IllegalStateException();
            }
            m.deleteEntry(lastReturned);
            lastReturned = null;
          }
        };
      }

      @Override
      public int size() {
        int count = 0;
        Iterator<Map.Entry<K, V>> it = iterator();
        while (it.hasNext()) {
          count++;
          it.next();
        }
        return count;
      }

      @Override
      public boolean isEmpty() {
        return iterator().hasNext() == false;
      }
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(comparator);
    out.writeInt(size);
    for (Map.Entry<K, V> entry : entrySet()) {
      out.writeObject(entry.getKey());
      out.writeObject(entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Comparator<? super K> comp = (Comparator<? super K>) in.readObject();
    try {
      java.lang.reflect.Field f = TreeMap.class.getDeclaredField("comparator");
      f.setAccessible(true);
      f.set(this, comp);
    } catch (Exception e) {
      throw new IOException(e);
    }
    int s = in.readInt();
    for (int i = 0; i < s; i++) {
      K key = (K) in.readObject();
      V value = (V) in.readObject();
      put(key, value);
    }
  }
}
