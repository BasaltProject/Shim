/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

final class SyncViews {
  private SyncViews() { }

  static class SynchronizedCollection<E> implements Collection<E>, Serializable {
    final Collection<E> c;
    final Object mutex;

    SynchronizedCollection(Collection<E> c) {
      this.c = Objects.requireNonNull(c);
      mutex = this;
    }

    SynchronizedCollection(Collection<E> c, Object mutex) {
      this.c = Objects.requireNonNull(c);
      this.mutex = Objects.requireNonNull(mutex);
    }

    public int size() {
      synchronized (mutex) { return c.size(); }
    }

    public boolean isEmpty() {
      synchronized (mutex) { return c.isEmpty(); }
    }

    public boolean contains(Object o) {
      synchronized (mutex) { return c.contains(o); }
    }

    public Object[] toArray() {
      synchronized (mutex) { return c.toArray(); }
    }

    public <T> T[] toArray(T[] a) {
      synchronized (mutex) { return c.toArray(a); }
    }

    public <T> T[] toArray(IntFunction<T[]> f) {
      synchronized (mutex) { return c.toArray(f); }
    }

    public Iterator<E> iterator() {
      return c.iterator();
    }

    public boolean add(E e) {
      synchronized (mutex) { return c.add(e); }
    }

    public boolean remove(Object o) {
      synchronized (mutex) { return c.remove(o); }
    }

    public boolean containsAll(Collection<?> coll) {
      synchronized (mutex) { return c.containsAll(coll); }
    }

    public boolean addAll(Collection<? extends E> coll) {
      synchronized (mutex) { return c.addAll(coll); }
    }

    public boolean removeAll(Collection<?> coll) {
      synchronized (mutex) { return c.removeAll(coll); }
    }

    public boolean retainAll(Collection<?> coll) {
      synchronized (mutex) { return c.retainAll(coll); }
    }

    public void clear() {
      synchronized (mutex) { c.clear(); }
    }

    public String toString() {
      synchronized (mutex) { return c.toString(); }
    }

    public void forEach(Consumer<? super E> consumer) {
      synchronized (mutex) { c.forEach(consumer); }
    }

    public boolean removeIf(Predicate<? super E> filter) {
      synchronized (mutex) { return c.removeIf(filter); }
    }

    public Spliterator<E> spliterator() {
      return c.spliterator();
    }

    public java.util.stream.Stream<E> stream() {
      return c.stream();
    }

    public java.util.stream.Stream<E> parallelStream() {
      return c.parallelStream();
    }
  }

  static class SynchronizedSet<E> extends SynchronizedCollection<E> implements Set<E> {
    SynchronizedSet(Set<E> s) {
      super(s);
    }

    SynchronizedSet(Set<E> s, Object mutex) {
      super(s, mutex);
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      synchronized (mutex) { return c.equals(o); }
    }

    public int hashCode() {
      synchronized (mutex) { return c.hashCode(); }
    }
  }

  static class SynchronizedSortedSet<E> extends SynchronizedSet<E> implements SortedSet<E> {
    private final SortedSet<E> ss;

    SynchronizedSortedSet(SortedSet<E> s) {
      super(s);
      ss = s;
    }

    SynchronizedSortedSet(SortedSet<E> s, Object mutex) {
      super(s, mutex);
      ss = s;
    }

    public Comparator<? super E> comparator() {
      synchronized (mutex) { return ss.comparator(); }
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
      synchronized (mutex) { return new SynchronizedSortedSet<E>(ss.subSet(fromElement, toElement), mutex); }
    }

    public SortedSet<E> headSet(E toElement) {
      synchronized (mutex) { return new SynchronizedSortedSet<E>(ss.headSet(toElement), mutex); }
    }

    public SortedSet<E> tailSet(E fromElement) {
      synchronized (mutex) { return new SynchronizedSortedSet<E>(ss.tailSet(fromElement), mutex); }
    }

    public E first() {
      synchronized (mutex) { return ss.first(); }
    }

    public E last() {
      synchronized (mutex) { return ss.last(); }
    }
  }

  static class SynchronizedNavigableSet<E> extends SynchronizedSortedSet<E> implements NavigableSet<E> {
    private final NavigableSet<E> ns;

    SynchronizedNavigableSet(NavigableSet<E> s) {
      super(s);
      ns = s;
    }

    SynchronizedNavigableSet(NavigableSet<E> s, Object mutex) {
      super(s, mutex);
      ns = s;
    }

    public E lower(E e) {
      synchronized (mutex) { return ns.lower(e); }
    }

    public E floor(E e) {
      synchronized (mutex) { return ns.floor(e); }
    }

    public E ceiling(E e) {
      synchronized (mutex) { return ns.ceiling(e); }
    }

    public E higher(E e) {
      synchronized (mutex) { return ns.higher(e); }
    }

    public E pollFirst() {
      synchronized (mutex) { return ns.pollFirst(); }
    }

    public E pollLast() {
      synchronized (mutex) { return ns.pollLast(); }
    }

    public NavigableSet<E> descendingSet() {
      synchronized (mutex) { return new SynchronizedNavigableSet<E>(ns.descendingSet(), mutex); }
    }

    public Iterator<E> descendingIterator() {
      synchronized (mutex) { return descendingSet().iterator(); }
    }

    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      synchronized (mutex) {
        return new SynchronizedNavigableSet<E>(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), mutex);
      }
    }

    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      synchronized (mutex) { return new SynchronizedNavigableSet<E>(ns.headSet(toElement, inclusive), mutex); }
    }

    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      synchronized (mutex) { return new SynchronizedNavigableSet<E>(ns.tailSet(fromElement, inclusive), mutex); }
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
      return subSet(fromElement, true, toElement, false);
    }

    public SortedSet<E> headSet(E toElement) {
      return headSet(toElement, false);
    }

    public SortedSet<E> tailSet(E fromElement) {
      return tailSet(fromElement, true);
    }
  }

  static class SynchronizedList<E> extends SynchronizedCollection<E> implements List<E> {
    final List<E> list;

    SynchronizedList(List<E> list) {
      super(list);
      this.list = list;
    }

    SynchronizedList(List<E> list, Object mutex) {
      super(list, mutex);
      this.list = list;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      synchronized (mutex) { return list.equals(o); }
    }

    public int hashCode() {
      synchronized (mutex) { return list.hashCode(); }
    }

    public E get(int index) {
      synchronized (mutex) { return list.get(index); }
    }

    public E set(int index, E element) {
      synchronized (mutex) { return list.set(index, element); }
    }

    public void add(int index, E element) {
      synchronized (mutex) { list.add(index, element); }
    }

    public E remove(int index) {
      synchronized (mutex) { return list.remove(index); }
    }

    public int indexOf(Object o) {
      synchronized (mutex) { return list.indexOf(o); }
    }

    public int lastIndexOf(Object o) {
      synchronized (mutex) { return list.lastIndexOf(o); }
    }

    public boolean addAll(int index, Collection<? extends E> c) {
      synchronized (mutex) { return list.addAll(index, c); }
    }

    public ListIterator<E> listIterator() {
      return list.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
      return list.listIterator(index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
      synchronized (mutex) { return new SynchronizedList<E>(list.subList(fromIndex, toIndex), mutex); }
    }

    public void replaceAll(UnaryOperator<E> operator) {
      synchronized (mutex) { list.replaceAll(operator); }
    }

    public void sort(Comparator<? super E> c) {
      synchronized (mutex) { list.sort(c); }
    }

    public E getFirst() {
      synchronized (mutex) { return list.getFirst(); }
    }

    public E getLast() {
      synchronized (mutex) { return list.getLast(); }
    }

    public void addFirst(E element) {
      synchronized (mutex) { list.addFirst(element); }
    }

    public void addLast(E element) {
      synchronized (mutex) { list.addLast(element); }
    }

    public E removeFirst() {
      synchronized (mutex) { return list.removeFirst(); }
    }

    public E removeLast() {
      synchronized (mutex) { return list.removeLast(); }
    }

    public List<E> reversed() {
      synchronized (mutex) { return new SynchronizedList<E>(list.reversed(), mutex); }
    }
  }

  static class SynchronizedRandomAccessList<E> extends SynchronizedList<E> implements RandomAccess {
    SynchronizedRandomAccessList(List<E> list) {
      super(list);
    }

    SynchronizedRandomAccessList(List<E> list, Object mutex) {
      super(list, mutex);
    }

    public List<E> subList(int fromIndex, int toIndex) {
      synchronized (mutex) {
        return new SynchronizedRandomAccessList<E>(list.subList(fromIndex, toIndex), mutex);
      }
    }
  }

  static class SynchronizedMap<K, V> implements Map<K, V>, Serializable {
    private final Map<K, V> m;
    final Object mutex;

    private transient Set<K> keySet;
    private transient Set<Map.Entry<K, V>> entrySet;
    private transient Collection<V> values;

    SynchronizedMap(Map<K, V> m) {
      this.m = Objects.requireNonNull(m);
      mutex = this;
    }

    SynchronizedMap(Map<K, V> m, Object mutex) {
      this.m = m;
      this.mutex = mutex;
    }

    public int size() {
      synchronized (mutex) { return m.size(); }
    }

    public boolean isEmpty() {
      synchronized (mutex) { return m.isEmpty(); }
    }

    public boolean containsKey(Object key) {
      synchronized (mutex) { return m.containsKey(key); }
    }

    public boolean containsValue(Object value) {
      synchronized (mutex) { return m.containsValue(value); }
    }

    public V get(Object key) {
      synchronized (mutex) { return m.get(key); }
    }

    public V put(K key, V value) {
      synchronized (mutex) { return m.put(key, value); }
    }

    public V remove(Object key) {
      synchronized (mutex) { return m.remove(key); }
    }

    public void putAll(Map<? extends K, ? extends V> map) {
      synchronized (mutex) { m.putAll(map); }
    }

    public void clear() {
      synchronized (mutex) { m.clear(); }
    }

    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = new SynchronizedSet<K>(m.keySet(), mutex);
        }
        return keySet;
      }
    }

    public Set<Map.Entry<K, V>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = new SynchronizedSet<Map.Entry<K, V>>(m.entrySet(), mutex);
        }
        return entrySet;
      }
    }

    public Collection<V> values() {
      synchronized (mutex) {
        if (values == null) {
          values = new SynchronizedCollection<V>(m.values(), mutex);
        }
        return values;
      }
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      synchronized (mutex) { return m.equals(o); }
    }

    public int hashCode() {
      synchronized (mutex) { return m.hashCode(); }
    }

    public String toString() {
      synchronized (mutex) { return m.toString(); }
    }

    public V getOrDefault(Object k, V defaultValue) {
      synchronized (mutex) { return m.getOrDefault(k, defaultValue); }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
      synchronized (mutex) { m.forEach(action); }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      synchronized (mutex) { m.replaceAll(function); }
    }

    public V putIfAbsent(K key, V value) {
      synchronized (mutex) { return m.putIfAbsent(key, value); }
    }

    public boolean remove(Object key, Object value) {
      synchronized (mutex) { return m.remove(key, value); }
    }

    public boolean replace(K key, V oldValue, V newValue) {
      synchronized (mutex) { return m.replace(key, oldValue, newValue); }
    }

    public V replace(K key, V value) {
      synchronized (mutex) { return m.replace(key, value); }
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      synchronized (mutex) { return m.computeIfAbsent(key, mappingFunction); }
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      synchronized (mutex) { return m.computeIfPresent(key, remappingFunction); }
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      synchronized (mutex) { return m.compute(key, remappingFunction); }
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      synchronized (mutex) { return m.merge(key, value, remappingFunction); }
    }
  }

  static class SynchronizedSortedMap<K, V> extends SynchronizedMap<K, V> implements SortedMap<K, V> {
    private final SortedMap<K, V> sm;

    SynchronizedSortedMap(SortedMap<K, V> m) {
      super(m);
      sm = m;
    }

    SynchronizedSortedMap(SortedMap<K, V> m, Object mutex) {
      super(m, mutex);
      sm = m;
    }

    public Comparator<? super K> comparator() {
      synchronized (mutex) { return sm.comparator(); }
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      synchronized (mutex) { return new SynchronizedSortedMap<K, V>(sm.subMap(fromKey, toKey), mutex); }
    }

    public SortedMap<K, V> headMap(K toKey) {
      synchronized (mutex) { return new SynchronizedSortedMap<K, V>(sm.headMap(toKey), mutex); }
    }

    public SortedMap<K, V> tailMap(K fromKey) {
      synchronized (mutex) { return new SynchronizedSortedMap<K, V>(sm.tailMap(fromKey), mutex); }
    }

    public K firstKey() {
      synchronized (mutex) { return sm.firstKey(); }
    }

    public K lastKey() {
      synchronized (mutex) { return sm.lastKey(); }
    }
  }

  static class SynchronizedNavigableMap<K, V> extends SynchronizedSortedMap<K, V> implements NavigableMap<K, V> {
    private final NavigableMap<K, V> nm;

    SynchronizedNavigableMap(NavigableMap<K, V> m) {
      super(m);
      nm = m;
    }

    SynchronizedNavigableMap(NavigableMap<K, V> m, Object mutex) {
      super(m, mutex);
      nm = m;
    }

    public Map.Entry<K, V> lowerEntry(K key) {
      synchronized (mutex) { return nm.lowerEntry(key); }
    }

    public K lowerKey(K key) {
      synchronized (mutex) { return nm.lowerKey(key); }
    }

    public Map.Entry<K, V> floorEntry(K key) {
      synchronized (mutex) { return nm.floorEntry(key); }
    }

    public K floorKey(K key) {
      synchronized (mutex) { return nm.floorKey(key); }
    }

    public Map.Entry<K, V> ceilingEntry(K key) {
      synchronized (mutex) { return nm.ceilingEntry(key); }
    }

    public K ceilingKey(K key) {
      synchronized (mutex) { return nm.ceilingKey(key); }
    }

    public Map.Entry<K, V> higherEntry(K key) {
      synchronized (mutex) { return nm.higherEntry(key); }
    }

    public K higherKey(K key) {
      synchronized (mutex) { return nm.higherKey(key); }
    }

    public Map.Entry<K, V> firstEntry() {
      synchronized (mutex) { return nm.firstEntry(); }
    }

    public Map.Entry<K, V> lastEntry() {
      synchronized (mutex) { return nm.lastEntry(); }
    }

    public Map.Entry<K, V> pollFirstEntry() {
      synchronized (mutex) { return nm.pollFirstEntry(); }
    }

    public Map.Entry<K, V> pollLastEntry() {
      synchronized (mutex) { return nm.pollLastEntry(); }
    }

    public NavigableMap<K, V> descendingMap() {
      synchronized (mutex) { return new SynchronizedNavigableMap<K, V>(nm.descendingMap(), mutex); }
    }

    public NavigableSet<K> navigableKeySet() {
      synchronized (mutex) { return new SynchronizedNavigableSet<K>(nm.navigableKeySet(), mutex); }
    }

    public NavigableSet<K> descendingKeySet() {
      synchronized (mutex) { return new SynchronizedNavigableSet<K>(nm.descendingKeySet(), mutex); }
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

    public SortedMap<K, V> headMap(K toKey) {
      return headMap(toKey, false);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
      return tailMap(fromKey, true);
    }

    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      synchronized (mutex) {
        return new SynchronizedNavigableMap<K, V>(nm.subMap(fromKey, fromInclusive, toKey, toInclusive), mutex);
      }
    }

    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      synchronized (mutex) { return new SynchronizedNavigableMap<K, V>(nm.headMap(toKey, inclusive), mutex); }
    }

    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      synchronized (mutex) { return new SynchronizedNavigableMap<K, V>(nm.tailMap(fromKey, inclusive), mutex); }
    }
  }
}
