/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentSkipListMap<K, V> extends AbstractMap<K, V>
    implements ConcurrentNavigableMap<K, V>, Cloneable, Serializable {

  private static final long serialVersionUID = -8627078645895051609L;
  private static final int MAX_LEVEL = 16;

  private final Comparator<? super K> comparator;
  private transient Node<K, V> head;
  private transient int level;
  private final AtomicInteger size = new AtomicInteger(0);
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Random random = new Random();

  public ConcurrentSkipListMap() {
    this((Comparator<? super K>) null);
  }

  public ConcurrentSkipListMap(Comparator<? super K> comparator) {
    this.comparator = comparator;
    initialize();
  }

  public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
    this((Comparator<? super K>) null);
    putAll(m);
  }

  public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
    this(m.comparator());
    putAll(m);
  }

  private void initialize() {
    this.head = new Node<K, V>(null, null, MAX_LEVEL);
    this.level = 1;
    this.size.set(0);
  }

  @SuppressWarnings("unchecked")
  final int compare(Object k1, Object k2) {
    if (k1 == null || k2 == null) {
      throw new NullPointerException();
    }
    return comparator == null
        ? ((Comparable<? super Object>) k1).compareTo(k2)
        : ((Comparator<? super Object>) comparator).compare(k1, k2);
  }

  private int randomLevel() {
    int lvl = 1;
    while ((random.nextInt() & 1) == 0 && lvl < MAX_LEVEL) {
      lvl++;
    }
    return lvl;
  }

  @Override
  public Comparator<? super K> comparator() {
    return comparator;
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean isEmpty() {
    return size.get() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    Objects.requireNonNull(value);
    lock.readLock().lock();
    try {
      Node<K, V> curr = head.forward[0];
      while (curr != null) {
        if (value.equals(curr.value)) {
          return true;
        }
        curr = curr.forward[0];
      }
      return false;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public V get(Object key) {
    Objects.requireNonNull(key);
    lock.readLock().lock();
    try {
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
      }
      curr = curr.forward[0];
      if (curr != null && compare(key, curr.key) == 0) {
        return curr.value;
      }
      return null;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public V put(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    lock.writeLock().lock();
    try {
      @SuppressWarnings("unchecked")
      Node<K, V>[] update = (Node<K, V>[]) new Node[MAX_LEVEL];
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
        update[i] = curr;
      }
      Node<K, V> next = curr.forward[0];
      if (next != null && compare(key, next.key) == 0) {
        V oldVal = next.value;
        next.value = value;
        return oldVal;
      }

      int lvl = randomLevel();
      if (lvl > level) {
        for (int i = level; i < lvl; i++) {
          update[i] = head;
        }
        level = lvl;
      }

      Node<K, V> newNode = new Node<K, V>(key, value, lvl);
      for (int i = 0; i < lvl; i++) {
        newNode.forward[i] = update[i].forward[i];
        update[i].forward[i] = newNode;
      }

      newNode.prev = update[0];
      if (newNode.forward[0] != null) {
        newNode.forward[0].prev = newNode;
      }

      size.incrementAndGet();
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public V putIfAbsent(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    lock.writeLock().lock();
    try {
      @SuppressWarnings("unchecked")
      Node<K, V>[] update = (Node<K, V>[]) new Node[MAX_LEVEL];
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
        update[i] = curr;
      }
      Node<K, V> next = curr.forward[0];
      if (next != null && compare(key, next.key) == 0) {
        return next.value;
      }

      int lvl = randomLevel();
      if (lvl > level) {
        for (int i = level; i < lvl; i++) {
          update[i] = head;
        }
        level = lvl;
      }

      Node<K, V> newNode = new Node<K, V>(key, value, lvl);
      for (int i = 0; i < lvl; i++) {
        newNode.forward[i] = update[i].forward[i];
        update[i].forward[i] = newNode;
      }

      newNode.prev = update[0];
      if (newNode.forward[0] != null) {
        newNode.forward[0].prev = newNode;
      }

      size.incrementAndGet();
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public V remove(Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    lock.writeLock().lock();
    try {
      @SuppressWarnings("unchecked")
      Node<K, V>[] update = (Node<K, V>[]) new Node[MAX_LEVEL];
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
        update[i] = curr;
      }
      Node<K, V> target = curr.forward[0];
      if (target != null && compare(key, target.key) == 0) {
        for (int i = 0; i < level; i++) {
          if (update[i].forward[i] != target) {
            break;
          }
          update[i].forward[i] = target.forward[i];
        }
        if (target.forward[0] != null) {
          target.forward[0].prev = target.prev;
        }
        while (level > 1 && head.forward[level - 1] == null) {
          level--;
        }
        size.decrementAndGet();
        return target.value;
      }
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean remove(Object key, Object value) {
    if (key == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      return false;
    }
    lock.writeLock().lock();
    try {
      @SuppressWarnings("unchecked")
      Node<K, V>[] update = (Node<K, V>[]) new Node[MAX_LEVEL];
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
        update[i] = curr;
      }
      Node<K, V> target = curr.forward[0];
      if (target != null && compare(key, target.key) == 0 && value.equals(target.value)) {
        for (int i = 0; i < level; i++) {
          if (update[i].forward[i] != target) {
            break;
          }
          update[i].forward[i] = target.forward[i];
        }
        if (target.forward[0] != null) {
          target.forward[0].prev = target.prev;
        }
        while (level > 1 && head.forward[level - 1] == null) {
          level--;
        }
        size.decrementAndGet();
        return true;
      }
      return false;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(oldValue);
    Objects.requireNonNull(newValue);
    lock.writeLock().lock();
    try {
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
      }
      curr = curr.forward[0];
      if (curr != null && compare(key, curr.key) == 0 && oldValue.equals(curr.value)) {
        curr.value = newValue;
        return true;
      }
      return false;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public V replace(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    lock.writeLock().lock();
    try {
      Node<K, V> curr = head;
      for (int i = level - 1; i >= 0; i--) {
        while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
          curr = curr.forward[i];
        }
      }
      curr = curr.forward[0];
      if (curr != null && compare(key, curr.key) == 0) {
        V old = curr.value;
        curr.value = value;
        return old;
      }
      return null;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void clear() {
    lock.writeLock().lock();
    try {
      initialize();
    } finally {
      lock.writeLock().unlock();
    }
  }

  Node<K, V> findFirstNode() {
    return head.forward[0];
  }

  Node<K, V> findLastNode() {
    Node<K, V> curr = head;
    for (int i = level - 1; i >= 0; i--) {
      while (curr.forward[i] != null) {
        curr = curr.forward[i];
      }
    }
    return (curr == head) ? null : curr;
  }

  Node<K, V> findCeilingNode(K key) {
    Objects.requireNonNull(key);
    Node<K, V> curr = head;
    for (int i = level - 1; i >= 0; i--) {
      while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
        curr = curr.forward[i];
      }
    }
    return curr.forward[0];
  }

  Node<K, V> findFloorNode(K key) {
    Objects.requireNonNull(key);
    Node<K, V> curr = head;
    for (int i = level - 1; i >= 0; i--) {
      while (curr.forward[i] != null && compare(key, curr.forward[i].key) >= 0) {
        curr = curr.forward[i];
      }
    }
    return (curr == head) ? null : curr;
  }

  Node<K, V> findHigherNode(K key) {
    Objects.requireNonNull(key);
    Node<K, V> curr = head;
    for (int i = level - 1; i >= 0; i--) {
      while (curr.forward[i] != null && compare(key, curr.forward[i].key) >= 0) {
        curr = curr.forward[i];
      }
    }
    return curr.forward[0];
  }

  Node<K, V> findLowerNode(K key) {
    Objects.requireNonNull(key);
    Node<K, V> curr = head;
    for (int i = level - 1; i >= 0; i--) {
      while (curr.forward[i] != null && compare(key, curr.forward[i].key) > 0) {
        curr = curr.forward[i];
      }
    }
    return (curr == head) ? null : curr;
  }

  static <K, V> Map.Entry<K, V> exportEntry(Node<K, V> n) {
    return n == null ? null : new AbstractMap.SimpleImmutableEntry<K, V>(n.key, n.value);
  }

  @Override
  public K firstKey() {
    lock.readLock().lock();
    try {
      Node<K, V> n = findFirstNode();
      if (n == null) {
        throw new NoSuchElementException();
      }
      return n.key;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public K lastKey() {
    lock.readLock().lock();
    try {
      Node<K, V> n = findLastNode();
      if (n == null) {
        throw new NoSuchElementException();
      }
      return n.key;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> firstEntry() {
    lock.readLock().lock();
    try {
      return exportEntry(findFirstNode());
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> lastEntry() {
    lock.readLock().lock();
    try {
      return exportEntry(findLastNode());
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> pollFirstEntry() {
    lock.writeLock().lock();
    try {
      Node<K, V> n = findFirstNode();
      if (n == null) {
        return null;
      }
      Map.Entry<K, V> res = exportEntry(n);
      remove(n.key);
      return res;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> pollLastEntry() {
    lock.writeLock().lock();
    try {
      Node<K, V> n = findLastNode();
      if (n == null) {
        return null;
      }
      Map.Entry<K, V> res = exportEntry(n);
      remove(n.key);
      return res;
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> lowerEntry(K key) {
    lock.readLock().lock();
    try {
      return exportEntry(findLowerNode(key));
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public K lowerKey(K key) {
    lock.readLock().lock();
    try {
      Node<K, V> n = findLowerNode(key);
      return n == null ? null : n.key;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> floorEntry(K key) {
    lock.readLock().lock();
    try {
      return exportEntry(findFloorNode(key));
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public K floorKey(K key) {
    lock.readLock().lock();
    try {
      Node<K, V> n = findFloorNode(key);
      return n == null ? null : n.key;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> ceilingEntry(K key) {
    lock.readLock().lock();
    try {
      return exportEntry(findCeilingNode(key));
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public K ceilingKey(K key) {
    lock.readLock().lock();
    try {
      Node<K, V> n = findCeilingNode(key);
      return n == null ? null : n.key;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map.Entry<K, V> higherEntry(K key) {
    lock.readLock().lock();
    try {
      return exportEntry(findHigherNode(key));
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public K higherKey(K key) {
    lock.readLock().lock();
    try {
      Node<K, V> n = findHigherNode(key);
      return n == null ? null : n.key;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public NavigableSet<K> navigableKeySet() {
    return new KeySet<K>(this);
  }

  @Override
  public NavigableSet<K> keySet() {
    return navigableKeySet();
  }

  @Override
  public NavigableSet<K> descendingKeySet() {
    return descendingMap().navigableKeySet();
  }

  @Override
  public ConcurrentNavigableMap<K, V> descendingMap() {
    return new ConcurrentSubMap<K, V>(this, true, null, true, true, null, true, true);
  }

  @Override
  public ConcurrentNavigableMap<K, V> subMap(
      K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
    return new ConcurrentSubMap<K, V>(
        this, false, fromKey, fromInclusive, false, toKey, toInclusive, false);
  }

  @Override
  public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
    return new ConcurrentSubMap<K, V>(
        this, true, null, true, false, toKey, inclusive, false);
  }

  @Override
  public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
    return new ConcurrentSubMap<K, V>(
        this, false, fromKey, inclusive, true, null, true, false);
  }

  @Override
  public ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  @Override
  public ConcurrentNavigableMap<K, V> headMap(K toKey) {
    return headMap(toKey, false);
  }

  @Override
  public ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
    return tailMap(fromKey, true);
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  @Override
  public Collection<V> values() {
    return new Values();
  }

  @Override
  public ConcurrentSkipListMap<K, V> clone() {
    ConcurrentSkipListMap<K, V> clone;
    try {
      clone = (ConcurrentSkipListMap<K, V>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
    clone.initialize();
    clone.putAll(this);
    return clone;
  }

  static final class Node<K, V> {
    final K key;
    volatile V value;
    final Node<K, V>[] forward;
    Node<K, V> prev;

    @SuppressWarnings("unchecked")
    Node(K key, V value, int level) {
      this.key = key;
      this.value = value;
      this.forward = (Node<K, V>[]) new Node[level];
    }
  }

  static final class IterSpliterator<T> implements Spliterator<T> {
    private final Iterator<T> it;
    private final long est;
    private final int characteristics;
    private final Comparator<? super T> comparator;

    IterSpliterator(Iterator<T> it, long est, int characteristics, Comparator<? super T> comparator) {
      this.it = it;
      this.est = est;
      this.characteristics = characteristics;
      this.comparator = comparator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      if (it.hasNext()) {
        action.accept(it.next());
        return true;
      }
      return false;
    }

    @Override
    public Spliterator<T> trySplit() {
      return null;
    }

    @Override
    public long estimateSize() {
      return est;
    }

    @Override
    public int characteristics() {
      return characteristics;
    }

    @Override
    public Comparator<? super T> getComparator() {
      if ((characteristics & Spliterator.SORTED) == 0) {
        throw new IllegalStateException();
      }
      return comparator;
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <K, V> Comparator<Map.Entry<K, V>> entryNaturalOrder() {
    return (Comparator) Map.Entry.comparingByKey();
  }

  static final int kKeyCharacteristics = Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED
      | Spliterator.NONNULL | Spliterator.CONCURRENT;
  static final int kValueCharacteristics = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT;

  class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
      Comparator<? super K> cmp = comparator();
      return new IterSpliterator<Map.Entry<K, V>>(iterator(), size(), kKeyCharacteristics,
          cmp != null ? Map.Entry.<K, V>comparingByKey(cmp) : entryNaturalOrder());
    }
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new Iterator<Map.Entry<K, V>>() {
        private Node<K, V> current = head.forward[0];
        private Node<K, V> lastReturned = null;

        @Override
        public boolean hasNext() {
          return current != null;
        }

        @Override
        public Map.Entry<K, V> next() {
          if (current == null) {
            throw new NoSuchElementException();
          }
          lastReturned = current;
          current = current.forward[0];
          return new AbstractMap.SimpleImmutableEntry<K, V>(
              lastReturned.key, lastReturned.value);
        }

        @Override
        public void remove() {
          if (lastReturned == null) {
            throw new IllegalStateException();
          }
          ConcurrentSkipListMap.this.remove(lastReturned.key);
          lastReturned = null;
        }
      };
    }

    @Override
    public int size() {
      return ConcurrentSkipListMap.this.size();
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      V val = get(e.getKey());
      return val != null && val.equals(e.getValue());
    }

    @Override
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      return ConcurrentSkipListMap.this.remove(e.getKey(), e.getValue());
    }

    @Override
    public void clear() {
      ConcurrentSkipListMap.this.clear();
    }
  }

  class Values extends AbstractCollection<V> {
    @Override
    public Spliterator<V> spliterator() {
      return new IterSpliterator<V>(iterator(), size(), kValueCharacteristics, null);
    }

    @Override
    public Iterator<V> iterator() {
      return new Iterator<V>() {
        private Node<K, V> current = head.forward[0];
        private Node<K, V> lastReturned = null;

        @Override
        public boolean hasNext() {
          return current != null;
        }

        @Override
        public V next() {
          if (current == null) {
            throw new NoSuchElementException();
          }
          lastReturned = current;
          current = current.forward[0];
          return lastReturned.value;
        }

        @Override
        public void remove() {
          if (lastReturned == null) {
            throw new IllegalStateException();
          }
          ConcurrentSkipListMap.this.remove(lastReturned.key);
          lastReturned = null;
        }
      };
    }

    @Override
    public int size() {
      return ConcurrentSkipListMap.this.size();
    }

    @Override
    public void clear() {
      ConcurrentSkipListMap.this.clear();
    }
  }

  static class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final ConcurrentNavigableMap<E, ?> m;

    KeySet(ConcurrentNavigableMap<E, ?> m) {
      this.m = m;
    }

    @Override
    public Spliterator<E> spliterator() {
      return new IterSpliterator<E>(iterator(), size(), kKeyCharacteristics, m.comparator());
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
    public boolean remove(Object o) {
      return m.remove(o) != null;
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

  static final class ConcurrentSubMap<K, V> extends AbstractMap<K, V>
      implements ConcurrentNavigableMap<K, V>, Serializable {

    private final ConcurrentSkipListMap<K, V> m;
    private final boolean fromStart;
    private final K lo;
    private final boolean loInclusive;
    private final boolean toEnd;
    private final K hi;
    private final boolean hiInclusive;
    private final boolean isDescending;

    ConcurrentSubMap(
        ConcurrentSkipListMap<K, V> m,
        boolean fromStart, K lo, boolean loInclusive,
        boolean toEnd, K hi, boolean hiInclusive,
        boolean isDescending) {
      if (!fromStart && !toEnd) {
        if (m.compare(lo, hi) > 0) {
          throw new IllegalArgumentException("inconsistent range");
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

    Node<K, V> subLowest() {
      Node<K, V> n = fromStart
          ? m.findFirstNode()
          : (loInclusive ? m.findCeilingNode(lo) : m.findHigherNode(lo));
      return (n == null || tooHigh(n.key)) ? null : n;
    }

    Node<K, V> subHighest() {
      Node<K, V> n = toEnd
          ? m.findLastNode()
          : (hiInclusive ? m.findFloorNode(hi) : m.findLowerNode(hi));
      return (n == null || tooLow(n.key)) ? null : n;
    }

    Node<K, V> subCeiling(K key) {
      if (tooLow(key)) {
        return subLowest();
      }
      Node<K, V> n = m.findCeilingNode(key);
      return (n == null || tooHigh(n.key)) ? null : n;
    }

    Node<K, V> subFloor(K key) {
      if (tooHigh(key)) {
        return subHighest();
      }
      Node<K, V> n = m.findFloorNode(key);
      return (n == null || tooLow(n.key)) ? null : n;
    }

    Node<K, V> subHigher(K key) {
      if (tooLow(key)) {
        return subLowest();
      }
      Node<K, V> n = m.findHigherNode(key);
      return (n == null || tooHigh(n.key)) ? null : n;
    }

    Node<K, V> subLower(K key) {
      if (tooHigh(key)) {
        return subHighest();
      }
      Node<K, V> n = m.findLowerNode(key);
      return (n == null || tooLow(n.key)) ? null : n;
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
    public V putIfAbsent(K key, V value) {
      if (!inRange(key)) {
        throw new IllegalArgumentException("key out of range");
      }
      return m.putIfAbsent(key, value);
    }

    @Override
    public V remove(Object key) {
      return !inRange(key) ? null : m.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
      return inRange(key) && m.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
      return inRange(key) && m.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
      return !inRange(key) ? null : m.replace(key, value);
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
      m.lock.readLock().lock();
      try {
        return exportEntry(isDescending ? subHighest() : subLowest());
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
      m.lock.readLock().lock();
      try {
        return exportEntry(isDescending ? subLowest() : subHighest());
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
      m.lock.writeLock().lock();
      try {
        Node<K, V> n = isDescending ? subHighest() : subLowest();
        if (n == null) {
          return null;
        }
        Map.Entry<K, V> res = exportEntry(n);
        m.remove(n.key);
        return res;
      } finally {
        m.lock.writeLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
      m.lock.writeLock().lock();
      try {
        Node<K, V> n = isDescending ? subLowest() : subHighest();
        if (n == null) {
          return null;
        }
        Map.Entry<K, V> res = exportEntry(n);
        m.remove(n.key);
        return res;
      } finally {
        m.lock.writeLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K key) {
      m.lock.readLock().lock();
      try {
        return exportEntry(isDescending ? subHigher(key) : subLower(key));
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public K lowerKey(K key) {
      m.lock.readLock().lock();
      try {
        Node<K, V> n = isDescending ? subHigher(key) : subLower(key);
        return n == null ? null : n.key;
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> floorEntry(K key) {
      m.lock.readLock().lock();
      try {
        return exportEntry(isDescending ? subCeiling(key) : subFloor(key));
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public K floorKey(K key) {
      m.lock.readLock().lock();
      try {
        Node<K, V> n = isDescending ? subCeiling(key) : subFloor(key);
        return n == null ? null : n.key;
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
      m.lock.readLock().lock();
      try {
        return exportEntry(isDescending ? subFloor(key) : subCeiling(key));
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public K ceilingKey(K key) {
      m.lock.readLock().lock();
      try {
        Node<K, V> n = isDescending ? subFloor(key) : subCeiling(key);
        return n == null ? null : n.key;
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public Map.Entry<K, V> higherEntry(K key) {
      m.lock.readLock().lock();
      try {
        return exportEntry(isDescending ? subLower(key) : subHigher(key));
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public K higherKey(K key) {
      m.lock.readLock().lock();
      try {
        Node<K, V> n = isDescending ? subLower(key) : subHigher(key);
        return n == null ? null : n.key;
      } finally {
        m.lock.readLock().unlock();
      }
    }

    @Override
    public ConcurrentNavigableMap<K, V> descendingMap() {
      return new ConcurrentSubMap<K, V>(
          m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive, !isDescending);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
      return new KeySet<K>(this);
    }

    @Override
    public NavigableSet<K> keySet() {
      return navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
      return descendingMap().navigableKeySet();
    }

    private ConcurrentNavigableMap<K, V> newSubMap(
        boolean hasFrom, K fromKey, boolean fromInclusive, boolean hasTo, K toKey, boolean toInclusive) {
      if (isDescending) {
        boolean h = hasFrom;
        hasFrom = hasTo;
        hasTo = h;
        K k = fromKey;
        fromKey = toKey;
        toKey = k;
        boolean i = fromInclusive;
        fromInclusive = toInclusive;
        toInclusive = i;
      }
      boolean newFromStart = fromStart;
      K newLo = lo;
      boolean newLoInclusive = loInclusive;
      boolean newToEnd = toEnd;
      K newHi = hi;
      boolean newHiInclusive = hiInclusive;
      if (hasFrom) {
        if (!fromStart) {
          int c = m.compare(fromKey, lo);
          if (c < 0 || (c == 0 && !loInclusive && fromInclusive)) {
            throw new IllegalArgumentException("inconsistent range");
          }
        }
        if (!toEnd) {
          int c = m.compare(fromKey, hi);
          if (c > 0 || (c == 0 && !hiInclusive && fromInclusive)) {
            throw new IllegalArgumentException("inconsistent range");
          }
        }
        newFromStart = false;
        newLo = fromKey;
        newLoInclusive = fromInclusive;
      }
      if (hasTo) {
        if (!fromStart) {
          int c = m.compare(toKey, lo);
          if (c < 0 || (c == 0 && !loInclusive && toInclusive)) {
            throw new IllegalArgumentException("inconsistent range");
          }
        }
        if (!toEnd) {
          int c = m.compare(toKey, hi);
          if (c > 0 || (c == 0 && !hiInclusive && toInclusive)) {
            throw new IllegalArgumentException("inconsistent range");
          }
        }
        newToEnd = false;
        newHi = toKey;
        newHiInclusive = toInclusive;
      }
      return new ConcurrentSubMap<K, V>(
          m, newFromStart, newLo, newLoInclusive, newToEnd, newHi, newHiInclusive, isDescending);
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(
        K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      if (fromKey == null || toKey == null) {
        throw new NullPointerException();
      }
      return newSubMap(true, fromKey, fromInclusive, true, toKey, toInclusive);
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      if (toKey == null) {
        throw new NullPointerException();
      }
      return newSubMap(false, null, false, true, toKey, inclusive);
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      if (fromKey == null) {
        throw new NullPointerException();
      }
      return newSubMap(true, fromKey, inclusive, false, null, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K toKey) {
      return headMap(toKey, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
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
          private Node<K, V> current = isDescending ? subHighest() : subLowest();
          private Node<K, V> lastReturned = null;

          @Override
          public boolean hasNext() {
            return current != null;
          }

          @Override
          public Map.Entry<K, V> next() {
            if (current == null) {
              throw new NoSuchElementException();
            }
            Node<K, V> ret = current;
            lastReturned = ret;
            if (isDescending) {
              current = ret.prev;
              if (current != null && (current == m.head || tooLow(current.key))) {
                current = null;
              }
            } else {
              current = ret.forward[0];
              if (current != null && tooHigh(current.key)) {
                current = null;
              }
            }
            return new AbstractMap.SimpleImmutableEntry<K, V>(ret.key, ret.value);
          }

          @Override
          public void remove() {
            if (lastReturned == null) {
              throw new IllegalStateException();
            }
            m.remove(lastReturned.key);
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
    lock.readLock().lock();
    try {
      out.defaultWriteObject();
      out.writeObject(comparator);
      out.writeInt(size.get());
      for (Map.Entry<K, V> entry : entrySet()) {
        out.writeObject(entry.getKey());
        out.writeObject(entry.getValue());
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Comparator<? super K> comp = (Comparator<? super K>) in.readObject();
    try {
      java.lang.reflect.Field f = ConcurrentSkipListMap.class.getDeclaredField("comparator");
      f.setAccessible(true);
      f.set(this, comp);
    } catch (Exception e) {
      throw new IOException(e);
    }
    initialize();
    int s = in.readInt();
    for (int i = 0; i < s; i++) {
      K key = (K) in.readObject();
      V value = (V) in.readObject();
      put(key, value);
    }
  }
}
