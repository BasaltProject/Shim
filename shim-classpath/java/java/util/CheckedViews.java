/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

final class CheckedViews {
  private CheckedViews() { }

  static class CheckedCollection<E> implements Collection<E>, Serializable {
    final Collection<E> c;
    final Class<E> type;

    CheckedCollection(Collection<E> c, Class<E> type) {
      this.c = Objects.requireNonNull(c, "c");
      this.type = Objects.requireNonNull(type, "type");
    }

    @SuppressWarnings("unchecked")
    E typeCheck(Object o) {
      if (o != null && !type.isInstance(o)) {
        throw new ClassCastException("Attempt to insert " + o.getClass() + " element into collection with element type "
                                     + type);
      }
      return (E) o;
    }

    public int size() {
      return c.size();
    }

    public boolean isEmpty() {
      return c.isEmpty();
    }

    public boolean contains(Object o) {
      return c.contains(o);
    }

    public Object[] toArray() {
      return c.toArray();
    }

    public <T> T[] toArray(T[] a) {
      return c.toArray(a);
    }

    public String toString() {
      return c.toString();
    }

    public boolean remove(Object o) {
      return c.remove(o);
    }

    public void clear() {
      c.clear();
    }

    public boolean containsAll(Collection<?> coll) {
      return c.containsAll(coll);
    }

    public boolean removeAll(Collection<?> coll) {
      return c.removeAll(coll);
    }

    public boolean retainAll(Collection<?> coll) {
      return c.retainAll(coll);
    }

    public Iterator<E> iterator() {
      final Iterator<E> it = c.iterator();
      return new Iterator<E>() {
        public boolean hasNext() {
          return it.hasNext();
        }

        public E next() {
          return it.next();
        }

        public void remove() {
          it.remove();
        }
      };
    }

    public boolean add(E e) {
      return c.add(typeCheck(e));
    }

    Collection<E> checkedCopyOf(Collection<? extends E> coll) {
      Object[] a = coll.toArray();
      List<E> copy = new ArrayList<E>(a.length);
      for (Object o : a) {
        copy.add(typeCheck(o));
      }
      return copy;
    }

    public boolean addAll(Collection<? extends E> coll) {
      return c.addAll(checkedCopyOf(coll));
    }

    public boolean removeIf(java.util.function.Predicate<? super E> filter) {
      return c.removeIf(filter);
    }

    public void forEach(java.util.function.Consumer<? super E> action) {
      c.forEach(action);
    }
  }

  static class CheckedQueue<E> extends CheckedCollection<E> implements Queue<E> {
    final Queue<E> queue;

    CheckedQueue(Queue<E> queue, Class<E> elementType) {
      super(queue, elementType);
      this.queue = queue;
    }

    public E element() {
      return queue.element();
    }

    public boolean equals(Object o) {
      return o == this || c.equals(o);
    }

    public int hashCode() {
      return c.hashCode();
    }

    public E peek() {
      return queue.peek();
    }

    public E poll() {
      return queue.poll();
    }

    public E remove() {
      return queue.remove();
    }

    public boolean offer(E e) {
      return queue.offer(typeCheck(e));
    }
  }

  static class CheckedSet<E> extends CheckedCollection<E> implements Set<E> {
    CheckedSet(Set<E> s, Class<E> elementType) {
      super(s, elementType);
    }

    public boolean equals(Object o) {
      return o == this || c.equals(o);
    }

    public int hashCode() {
      return c.hashCode();
    }
  }

  static class CheckedSortedSet<E> extends CheckedSet<E> implements SortedSet<E> {
    private final SortedSet<E> ss;

    CheckedSortedSet(SortedSet<E> s, Class<E> type) {
      super(s, type);
      ss = s;
    }

    public Comparator<? super E> comparator() {
      return ss.comparator();
    }

    public E first() {
      return ss.first();
    }

    public E last() {
      return ss.last();
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
      return new CheckedSortedSet<E>(ss.subSet(fromElement, toElement), type);
    }

    public SortedSet<E> headSet(E toElement) {
      return new CheckedSortedSet<E>(ss.headSet(toElement), type);
    }

    public SortedSet<E> tailSet(E fromElement) {
      return new CheckedSortedSet<E>(ss.tailSet(fromElement), type);
    }
  }

  static class CheckedNavigableSet<E> extends CheckedSortedSet<E> implements NavigableSet<E> {
    private final NavigableSet<E> ns;

    CheckedNavigableSet(NavigableSet<E> s, Class<E> type) {
      super(s, type);
      ns = s;
    }

    public E lower(E e) {
      return ns.lower(e);
    }

    public E floor(E e) {
      return ns.floor(e);
    }

    public E ceiling(E e) {
      return ns.ceiling(e);
    }

    public E higher(E e) {
      return ns.higher(e);
    }

    public E pollFirst() {
      return ns.pollFirst();
    }

    public E pollLast() {
      return ns.pollLast();
    }

    public NavigableSet<E> descendingSet() {
      return new CheckedNavigableSet<E>(ns.descendingSet(), type);
    }

    public Iterator<E> descendingIterator() {
      return descendingSet().iterator();
    }

    public NavigableSet<E> subSet(E fromElement, E toElement) {
      return subSet(fromElement, true, toElement, false);
    }

    public NavigableSet<E> headSet(E toElement) {
      return headSet(toElement, false);
    }

    public NavigableSet<E> tailSet(E fromElement) {
      return tailSet(fromElement, true);
    }

    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      return new CheckedNavigableSet<E>(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), type);
    }

    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      return new CheckedNavigableSet<E>(ns.headSet(toElement, inclusive), type);
    }

    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      return new CheckedNavigableSet<E>(ns.tailSet(fromElement, inclusive), type);
    }
  }

  static class CheckedList<E> extends CheckedCollection<E> implements List<E> {
    final List<E> list;

    CheckedList(List<E> list, Class<E> type) {
      super(list, type);
      this.list = list;
    }

    public boolean equals(Object o) {
      return o == this || list.equals(o);
    }

    public int hashCode() {
      return list.hashCode();
    }

    public E get(int index) {
      return list.get(index);
    }

    public E remove(int index) {
      return list.remove(index);
    }

    public int indexOf(Object o) {
      return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
      return list.lastIndexOf(o);
    }

    public E set(int index, E element) {
      return list.set(index, typeCheck(element));
    }

    public void add(int index, E element) {
      list.add(index, typeCheck(element));
    }

    public boolean addAll(int index, Collection<? extends E> c) {
      return list.addAll(index, checkedCopyOf(c));
    }

    public ListIterator<E> listIterator() {
      return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) {
      final ListIterator<E> i = list.listIterator(index);
      return new ListIterator<E>() {
        public boolean hasNext() {
          return i.hasNext();
        }

        public E next() {
          return i.next();
        }

        public boolean hasPrevious() {
          return i.hasPrevious();
        }

        public E previous() {
          return i.previous();
        }

        public int nextIndex() {
          return i.nextIndex();
        }

        public int previousIndex() {
          return i.previousIndex();
        }

        public void remove() {
          i.remove();
        }

        public void set(E e) {
          i.set(typeCheck(e));
        }

        public void add(E e) {
          i.add(typeCheck(e));
        }
      };
    }

    public List<E> subList(int fromIndex, int toIndex) {
      return new CheckedList<E>(list.subList(fromIndex, toIndex), type);
    }

    public void replaceAll(UnaryOperator<E> operator) {
      Objects.requireNonNull(operator);
      list.replaceAll(e -> typeCheck(operator.apply(e)));
    }

    public void sort(Comparator<? super E> c) {
      list.sort(c);
    }
  }

  static class CheckedRandomAccessList<E> extends CheckedList<E> implements RandomAccess {
    CheckedRandomAccessList(List<E> list, Class<E> type) {
      super(list, type);
    }

    public List<E> subList(int fromIndex, int toIndex) {
      return new CheckedRandomAccessList<E>(list.subList(fromIndex, toIndex), type);
    }
  }

  static class CheckedMap<K, V> implements Map<K, V>, Serializable {
    private final Map<K, V> m;
    final Class<K> keyType;
    final Class<V> valueType;
    private transient Set<Map.Entry<K, V>> entrySet;

    CheckedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
      this.m = Objects.requireNonNull(m);
      this.keyType = Objects.requireNonNull(keyType);
      this.valueType = Objects.requireNonNull(valueType);
    }

    private void typeCheck(Object key, Object value) {
      if (key != null && !keyType.isInstance(key)) {
        throw new ClassCastException("Attempt to insert " + key.getClass() + " key into map with key type " + keyType);
      }
      if (value != null && !valueType.isInstance(value)) {
        throw new ClassCastException("Attempt to insert " + value.getClass() + " value into map with value type "
                                     + valueType);
      }
    }

    public int size() {
      return m.size();
    }

    public boolean isEmpty() {
      return m.isEmpty();
    }

    public boolean containsKey(Object key) {
      return m.containsKey(key);
    }

    public boolean containsValue(Object v) {
      return m.containsValue(v);
    }

    public V get(Object key) {
      return m.get(key);
    }

    public V remove(Object key) {
      return m.remove(key);
    }

    public void clear() {
      m.clear();
    }

    public Set<K> keySet() {
      return m.keySet();
    }

    public Collection<V> values() {
      return m.values();
    }

    public boolean equals(Object o) {
      return o == this || m.equals(o);
    }

    public int hashCode() {
      return m.hashCode();
    }

    public String toString() {
      return m.toString();
    }

    public V put(K key, V value) {
      typeCheck(key, value);
      return m.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public void putAll(Map<? extends K, ? extends V> t) {
      Object[] entries = t.entrySet().toArray();
      List<Map.Entry<K, V>> checked = new ArrayList<Map.Entry<K, V>>(entries.length);
      for (Object o : entries) {
        Map.Entry<K, V> e = (Map.Entry<K, V>) o;
        typeCheck(e.getKey(), e.getValue());
        checked.add(new AbstractMap.SimpleImmutableEntry<K, V>(e.getKey(), e.getValue()));
      }
      for (Map.Entry<K, V> e : checked) {
        m.put(e.getKey(), e.getValue());
      }
    }

    public Set<Map.Entry<K, V>> entrySet() {
      if (entrySet == null) {
        entrySet = new CheckedEntrySet<K, V>(m.entrySet(), valueType);
      }
      return entrySet;
    }

    public V putIfAbsent(K key, V value) {
      typeCheck(key, value);
      return m.putIfAbsent(key, value);
    }

    public V replace(K key, V value) {
      typeCheck(key, value);
      return m.replace(key, value);
    }

    public boolean replace(K key, V oldValue, V newValue) {
      typeCheck(key, newValue);
      return m.replace(key, oldValue, newValue);
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      return m.merge(key, value, (v1, v2) -> {
        V newValue = remappingFunction.apply(v1, v2);
        typeCheck(null, newValue);
        return newValue;
      });
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      return m.computeIfAbsent(key, k -> {
        V value = mappingFunction.apply(k);
        typeCheck(k, value);
        return value;
      });
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      return m.computeIfPresent(key, (k, v) -> {
        V newValue = remappingFunction.apply(k, v);
        typeCheck(k, newValue);
        return newValue;
      });
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      return m.compute(key, (k, v) -> {
        V newValue = remappingFunction.apply(k, v);
        typeCheck(k, newValue);
        return newValue;
      });
    }
  }

  static class CheckedEntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {
    private final Set<Map.Entry<K, V>> s;
    private final Class<V> valueType;

    CheckedEntrySet(Set<Map.Entry<K, V>> s, Class<V> valueType) {
      this.s = s;
      this.valueType = valueType;
    }

    public int size() {
      return s.size();
    }

    public boolean contains(Object o) {
      return s.contains(o);
    }

    public boolean remove(Object o) {
      return s.remove(o);
    }

    public void clear() {
      s.clear();
    }

    public Iterator<Map.Entry<K, V>> iterator() {
      final Iterator<Map.Entry<K, V>> i = s.iterator();
      return new Iterator<Map.Entry<K, V>>() {
        public boolean hasNext() {
          return i.hasNext();
        }

        public void remove() {
          i.remove();
        }

        public Map.Entry<K, V> next() {
          final Map.Entry<K, V> e = i.next();
          return new Map.Entry<K, V>() {
            public K getKey() {
              return e.getKey();
            }

            public V getValue() {
              return e.getValue();
            }

            public V setValue(V value) {
              if (value != null && !valueType.isInstance(value)) {
                throw new ClassCastException("Attempt to insert " + value.getClass()
                    + " value into map with value type "
                                             + valueType);
              }
              return e.setValue(value);
            }

            public boolean equals(Object o) {
              return o == this || e.equals(o);
            }

            public int hashCode() {
              return e.hashCode();
            }

            public String toString() {
              return e.toString();
            }
          };
        }
      };
    }
  }

  static class CheckedSortedMap<K, V> extends CheckedMap<K, V> implements SortedMap<K, V> {
    private final SortedMap<K, V> sm;

    CheckedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType) {
      super(m, keyType, valueType);
      sm = m;
    }

    public Comparator<? super K> comparator() {
      return sm.comparator();
    }

    public K firstKey() {
      return sm.firstKey();
    }

    public K lastKey() {
      return sm.lastKey();
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return new CheckedSortedMap<K, V>(sm.subMap(fromKey, toKey), keyType, valueType);
    }

    public SortedMap<K, V> headMap(K toKey) {
      return new CheckedSortedMap<K, V>(sm.headMap(toKey), keyType, valueType);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
      return new CheckedSortedMap<K, V>(sm.tailMap(fromKey), keyType, valueType);
    }
  }

  static class CheckedNavigableMap<K, V> extends CheckedSortedMap<K, V> implements NavigableMap<K, V> {
    private final NavigableMap<K, V> nm;

    CheckedNavigableMap(NavigableMap<K, V> m, Class<K> keyType, Class<V> valueType) {
      super(m, keyType, valueType);
      nm = m;
    }

    public Map.Entry<K, V> lowerEntry(K key) {
      return nm.lowerEntry(key);
    }

    public K lowerKey(K key) {
      return nm.lowerKey(key);
    }

    public Map.Entry<K, V> floorEntry(K key) {
      return nm.floorEntry(key);
    }

    public K floorKey(K key) {
      return nm.floorKey(key);
    }

    public Map.Entry<K, V> ceilingEntry(K key) {
      return nm.ceilingEntry(key);
    }

    public K ceilingKey(K key) {
      return nm.ceilingKey(key);
    }

    public Map.Entry<K, V> higherEntry(K key) {
      return nm.higherEntry(key);
    }

    public K higherKey(K key) {
      return nm.higherKey(key);
    }

    public Map.Entry<K, V> firstEntry() {
      return nm.firstEntry();
    }

    public Map.Entry<K, V> lastEntry() {
      return nm.lastEntry();
    }

    public Map.Entry<K, V> pollFirstEntry() {
      return nm.pollFirstEntry();
    }

    public Map.Entry<K, V> pollLastEntry() {
      return nm.pollLastEntry();
    }

    public NavigableMap<K, V> descendingMap() {
      return new CheckedNavigableMap<K, V>(nm.descendingMap(), keyType, valueType);
    }

    public NavigableSet<K> navigableKeySet() {
      return nm.navigableKeySet();
    }

    public NavigableSet<K> descendingKeySet() {
      return nm.descendingKeySet();
    }

    public NavigableMap<K, V> subMap(K fromKey, K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

    public NavigableMap<K, V> headMap(K toKey) {
      return headMap(toKey, false);
    }

    public NavigableMap<K, V> tailMap(K fromKey) {
      return tailMap(fromKey, true);
    }

    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      return new CheckedNavigableMap<K, V>(nm.subMap(fromKey, fromInclusive, toKey, toInclusive), keyType, valueType);
    }

    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      return new CheckedNavigableMap<K, V>(nm.headMap(toKey, inclusive), keyType, valueType);
    }

    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      return new CheckedNavigableMap<K, V>(nm.tailMap(fromKey, inclusive), keyType, valueType);
    }
  }
}
