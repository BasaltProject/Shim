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

import java.io.Serializable;
import shim.Data;

public class Collections {

  private Collections() { }

  public static void shuffle(List list, Random random) {
    Object[] array = Data.toArray(list, new Object[list.size()]);
    for (int i = 0; i < array.length; ++i) {
      int j = random.nextInt(array.length);
      Object tmp = array[i];
      array[i] = array[j];
      array[j] = tmp;
    }
 
    list.clear();
    for (int i = 0; i < array.length; ++i) {
      list.add(array[i]);
    }
  }

  public static void shuffle(List list) {
    shuffle(list, new Random());
  }

  public static void sort(List list) {
    sort(list, new Comparator() {
        public int compare(Object a, Object b) {
          return ((Comparable) a).compareTo(b);
        }
      });
  }

  private final static int SORT_SIZE_THRESHOLD = 16;

  public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
    if (comparator == null) {
      sort(list);
      return;
    }
    int size = list.size();
    introSort(list, comparator, 0, size, size);
    insertionSort(list, comparator);
  }

  private static <T > void introSort(List<T> list,
    Comparator<? super T> comparator, int begin, int end, int limit)
  {
    while (end - begin > SORT_SIZE_THRESHOLD) {
      if (limit == 0) {
        heapSort(list, comparator, begin, end);
        return;
      }
      limit >>= 1;

      // median of three
      T a = list.get(begin);
      T b = list.get(begin + (end - begin) / 2 + 1);
      T c = list.get(end - 1);
      T median;
      if (comparator.compare(a, b) < 0) {
        median = comparator.compare(b, c) < 0 ?
          b : (comparator.compare(a, c) < 0 ? c : a);
      } else {
        median = comparator.compare(b, c) > 0 ?
          b : (comparator.compare(a, c) > 0 ? c : a);
      }

      // partition
      int pivot, i = begin, j = end;
      for (;;) {
        while (comparator.compare(list.get(i), median) < 0) {
          ++i;
        }
        --j;
        while (comparator.compare(median, list.get(j)) < 0) {
          --j;
        }
        if (i >= j) {
          pivot = i;
          break;
        }
        T swap = list.get(i);
        list.set(i, list.get(j));
        list.set(j, swap);
        ++i;
      }

      introSort(list, comparator, pivot, end, limit);
      end = pivot;
    }
  }

  private static <T> void heapSort(List<T> list, Comparator<? super T> comparator,
    int begin, int end)
  {
    int count = end - begin;
    for (int i = count / 2 - 1; i >= 0; --i) {
      siftDown(list, comparator, i, count, begin);
    }
    for (int i = count - 1; i > 0; --i) {
      // swap begin and begin + i
      T swap = list.get(begin + i);
      list.set(begin + i, list.get(begin));
      list.set(begin, swap);

      siftDown(list, comparator, 0, i, begin);
    }
  }

  private static <T> void siftDown(List<T> list, Comparator<? super T> comparator,
    int i, int count, int offset)
  {
    T value = list.get(offset + i);
    while (i < count / 2) {
      int child = 2 * i + 1;
      if (child + 1 < count &&
          comparator.compare(list.get(child), list.get(child + 1)) < 0) {
        ++child;
      }
      if (comparator.compare(value, list.get(child)) >= 0) {
        break;
      }
      list.set(offset + i, list.get(offset + child));
      i = child;
    }
    list.set(offset + i, value);
  }

  private static <T> void insertionSort(List<T> list,
    Comparator<? super T> comparator)
  {
    int size = list.size();
    for (int j = 1; j < size; ++j) {
      T t = list.get(j);
      int i = j - 1;
      while (i >= 0 && comparator.compare(list.get(i), t) > 0) {
        list.set(i + 1, list.get(i));
        --i;
      }
      list.set(i + 1, t);
    }
  }

  public static <T> int binarySearch(List<T> list, T needle) {
    int left = -1, right = list.size();
    while (left + 1 < right) {
      int middle = (left + right) >> 1;
      int result = ((Comparable)needle).compareTo(list.get(middle));
      if (result < 0) {
        right = middle;
      } else if (result > 0) {
        left = middle;
      } else {
        return middle;
      }
    }
    return -1 - right;
  }

  public static <T> void reverse(List<T> list) {
    int ascending = 0, descending = list.size() - 1;
    while (ascending < descending) {
      T tmp = list.get(ascending);
      list.set(ascending++, list.get(descending));
      list.set(descending--, tmp);
    }
  }

  public static final List EMPTY_LIST
    = new UnmodifiableList<Object>(new ArrayList<Object>(0));

  public static final <E> List<E> emptyList() {
    return EMPTY_LIST;
  }

  public static final <K,V> Map<K,V> emptyMap() {
    return (Map<K, V>) new UnmodifiableMap<Object, Object>(
      new HashMap<Object, Object>(0));
  }

  public static final <T> Set<T> emptySet() {
    return (Set<T>) new UnmodifiableSet<Object>(
      new HashSet<Object>(0));
  }

  private static final Iterator<Object> EMPTY_ITERATOR = new Iterator<Object>() {
    @Override public boolean hasNext() { return false; }
    @Override public Object next() { throw new NoSuchElementException(); }
    @Override public void remove() { throw new IllegalStateException(); }
  };

  private static final ListIterator<Object> EMPTY_LIST_ITERATOR = new ListIterator<Object>() {
    @Override public boolean hasNext() { return false; }
    @Override public Object next() { throw new NoSuchElementException(); }
    @Override public boolean hasPrevious() { return false; }
    @Override public Object previous() { throw new NoSuchElementException(); }
    @Override public int nextIndex() { return 0; }
    @Override public int previousIndex() { return -1; }
    @Override public void remove() { throw new IllegalStateException(); }
    @Override public void set(Object e) { throw new IllegalStateException(); }
    @Override public void add(Object e) { throw new UnsupportedOperationException(); }
  };

  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> emptyIterator() {
    return (Iterator<T>) EMPTY_ITERATOR;
  }

  @SuppressWarnings("unchecked")
  public static <T> ListIterator<T> emptyListIterator() {
    return (ListIterator<T>) EMPTY_LIST_ITERATOR;
  }

  private static final Enumeration<Object> EMPTY_ENUMERATION = new Enumeration<Object>() {
    public boolean hasMoreElements() { return false; }
    public Object nextElement() { throw new NoSuchElementException(); }
  };

  @SuppressWarnings("unchecked")
  public static <T> Enumeration<T> emptyEnumeration() {
    return (Enumeration<T>) EMPTY_ENUMERATION;
  }
  
  public static <T> Enumeration<T> enumeration(Collection<T> c) {
    return new IteratorEnumeration<T> (c.iterator());
  }

  public static <T> Comparator<T> reverseOrder() {
    return (c1, c2) -> ((Comparable) c2).compareTo(c1);
  }

  public static <T> Comparator<T> reverseOrder(Comparator<T> cmp) {
    return new ReverseComparator<T>(cmp);
  }
  
  static class IteratorEnumeration<T> implements Enumeration<T> {
    private final Iterator<T> it;

    public IteratorEnumeration(Iterator<T> it) {
      this.it = it;
    }

    public T nextElement() {
      return it.next();
    }

    public boolean hasMoreElements() {
      return it.hasNext();
    }
  }

  static class SynchronizedCollection<T> implements Collection<T> {
    protected final Object lock;
    protected final Collection<T> collection;

    public SynchronizedCollection(Object lock, Collection<T> collection) {
      this.lock = lock;
      this.collection = collection;
    }

    public int size() {
      synchronized (lock) { return collection.size(); }
    }

    public boolean isEmpty() {
      return size() == 0;
    }

    public boolean contains(Object e) {
      synchronized (lock) { return collection.contains(e); }
    }

    public boolean add(T e) {
      synchronized (lock) { return collection.add(e); }
    }

    public boolean addAll(Collection<? extends T> collection) {
      synchronized (lock) { return this.collection.addAll(collection); }
    }

    public boolean remove(Object e) {
      synchronized (lock) { return collection.remove((T)e); }
    }

    public Object[] toArray() {
      return toArray(new Object[size()]);      
    }

    public <T> T[] toArray(T[] array) {
      synchronized (lock) { return collection.toArray(array); }
    }

    public void clear() {
      synchronized (lock) { collection.clear(); }
    }

    public Iterator<T> iterator() {
      return new SynchronizedIterator<T>(lock, collection.iterator());
    }

    public boolean containsAll(Collection<?> c) {
      synchronized (lock) { return collection.containsAll(c); }
    }

    public boolean removeAll(Collection<?> c) {
      synchronized (lock) { return collection.removeAll(c); }
    }
  }
  
  static class SynchronizedMap<K,V> implements Map<K,V> {
    protected final Object lock;
    protected final Map<K,V> map;

    SynchronizedMap(Map<K,V> map) {
      this.map = map;
      this.lock = this;
    }

    SynchronizedMap(Object lock, Map<K,V> map) {
      this.lock = lock;
      this.map = map;
    }
    
    public void clear() {
      synchronized (lock) { map.clear(); }
    }
    public boolean containsKey(Object key) {
      synchronized (lock) { return map.containsKey(key); }
    }
    public boolean containsValue(Object value) {
      synchronized (lock) { return map.containsValue(value); }
    }
    public Set<java.util.Map.Entry<K, V>> entrySet() {
      synchronized (lock) { return new SynchronizedSet<java.util.Map.Entry<K, V>>(lock, map.entrySet()); }
    }
    public V get(Object key) {
      synchronized (lock) { return map.get(key); }
    }
    public boolean isEmpty() {
      synchronized (lock) { return map.isEmpty(); }
    }
    public Set<K> keySet() {
      synchronized (lock) { return new SynchronizedSet<K>(lock, map.keySet()); }
    }
    public V put(K key, V value) {
      synchronized (lock) { return map.put(key, value); }
    }
    public void putAll(Map<? extends K, ? extends V> elts) {
      synchronized (lock) { map.putAll(elts); }
    }
    public V remove(Object key) {
      synchronized (lock) { return map.remove(key); }
    }
    public int size() {
      synchronized (lock) { return map.size(); }
    }
    public Collection<V> values() {
      synchronized (lock) { return new SynchronizedCollection<V>(lock, map.values()); }
    }
  }
  
  public static <K,V> Map<K,V> synchronizedMap(Map<K,V> map) {
    return new SynchronizedMap<K, V> (map); 
  }
  
  static class SynchronizedSet<T>
    extends SynchronizedCollection<T>
    implements Set<T>
  {
    public SynchronizedSet(Object lock, Set<T> set) {
      super(lock, set);
    }
  }

  public static <V> Set<V> synchronizedSet(Set<V> set) {
    return new SynchronizedSet<V> (set, set);
  }
  
  static class SynchronizedList<T>
    extends SynchronizedCollection<T>
    implements List<T>
  {
    private final List<T> list;
    
    public SynchronizedList(List<T> list) {
      super(list, list);
      
      this.list = list;
    }

    public boolean equals(Object other) {
      if (other == this) return true;
      synchronized (lock) {
        return list.equals(other);
      }
    }

    public int hashCode() {
      synchronized (lock) {
        return list.hashCode();
      }
    }

    @Override
    public T get(int index) {
      synchronized (lock) {
        return list.get(index);
      }
    }

    @Override
    public T set(int index, T value) {
      synchronized (lock) {
        return list.set(index, value);
      }
    }

    @Override
    public T remove(int index) {
      synchronized (lock) {
        return list.remove(index);
      }
    }

    @Override
    public void add(int index, T element) {
      synchronized (lock) {
        list.add(index, element);
      }
    }

    @Override
    public boolean addAll(int startIndex, Collection<? extends T> c) {
      synchronized (lock) {
        return list.addAll(startIndex, c);
      }
    }

    @Override
    public int indexOf(Object value) {
      synchronized (lock) {
        return list.indexOf(value);
      }
    }

    @Override
    public int lastIndexOf(Object value) {
      synchronized (lock) {
        return list.lastIndexOf(value);
      }
    }

    @Override
    public ListIterator<T> listIterator(int index) {
      // as described in the javadocs, user should be synchronized on list before calling
      return list.listIterator(index);
    }

    @Override
    public ListIterator<T> listIterator() {
      // as described in the javadocs, user should be synchronized on list before calling
      return list.listIterator();
    }
  }
  
  static class RandomAccessSynchronizedList<T>
    extends SynchronizedList<T>
    implements RandomAccess
  {
    public RandomAccessSynchronizedList(List<T> list) {
      super(list);
    }
  }
  
  public static <T> List<T> synchronizedList(List<T> list) {
    List<T> result;
    if (list instanceof RandomAccess) {
      result = new RandomAccessSynchronizedList<T>(list);
    } else {
      result = new SynchronizedList<T>(list);
    }
    
    return result;
  }

  static class SynchronizedIterator<T> implements Iterator<T> {
    private final Object lock;
    private final Iterator<T> it;

    public SynchronizedIterator(Object lock, Iterator<T> it) {
      this.lock = lock;
      this.it = it;
    }

    public T next() {
      synchronized (lock) { return it.next(); }
    }

    public boolean hasNext() {
      synchronized (lock) { return it.hasNext(); }
    }

    public void remove() {
      synchronized (lock) { it.remove(); }
    }
  }

  static class ArrayListIterator<T> implements ListIterator<T> {
    private final List<T> list;
    private int toRemove = -1;
    private int index;

    public ArrayListIterator(List<T> list) {
      this(list, 0);
    }

    public ArrayListIterator(List<T> list, int index) {
      this.list = list;
      this.index = index - 1;
    }

    public boolean hasPrevious() {
      return index >= 0;
    }

    public T previous() {
      if (hasPrevious()) {
        toRemove = index;
        return list.get(index--);
      } else {
        throw new NoSuchElementException();
      }
    }

    public T next() {
      if (hasNext()) {
        toRemove = ++index;
        return list.get(index);
      } else {
        throw new NoSuchElementException();
      }
    }

    public boolean hasNext() {
      return index + 1 < list.size();
    }

    public void remove() {
      if (toRemove != -1) {
        list.remove(toRemove);
        index = toRemove - 1;
        toRemove = -1;
      } else {
        throw new IllegalStateException();
      }
    }

    public void set(T e) {
      if (toRemove != -1) {
        list.set(toRemove, e);
      } else {
        throw new IllegalStateException();
      }
    }

    public void add(T e) {
      list.add(++index, e);
      toRemove = -1;
    }

    public int nextIndex() {
      return index + 1;
    }

    public int previousIndex() {
      return index;
    }
  }

  public static <T> List<T> unmodifiableList(List<T> list)  {
    return new UnmodifiableList<T>(list);
  }

  static class UnmodifiableList<T> implements List<T> {

    private List<T> inner;

    UnmodifiableList(List<T> l) {
      this.inner = l;
    }

    public boolean equals(Object other) {
      return other == this || inner.equals(other);
    }

    public int hashCode() {
      return inner.hashCode();
    }

    public String toString() {
      return inner.toString();
    }

    public T get(int index) {
      return inner.get(index);
    }

    public T set(int index, T value) {
      throw new UnsupportedOperationException();
    }

    public T remove(int index) {
      throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    public boolean add(T element) {
      throw new UnsupportedOperationException();
    }

    public void add(int index, T element) {
      throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator() {
      return new UnmodifiableIterator<T>(inner.iterator());
    }

    public int indexOf(Object value) {
      return inner.indexOf(value);
    }

    public int lastIndexOf(Object value) {
      return inner.lastIndexOf(value);
    }

    public boolean isEmpty() {
      return inner.isEmpty();
    }

    public ListIterator<T> listIterator(int index) {
      return new UnmodifiableListIterator<T>(inner.listIterator(index));
    }

    public ListIterator<T> listIterator() {
      return new UnmodifiableListIterator<T>(inner.listIterator());
    }

    public int size() {
      return inner.size();
    }

    public boolean contains(Object element) {
      return inner.contains(element);
    }

    public boolean addAll(Collection<? extends T> collection) {
      throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
      return inner.toArray();
    }

    public <S> S[] toArray(S[] array) {
      return inner.toArray(array);
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    public boolean addAll(int startIndex, Collection<? extends T> c) {
      throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
      return inner.containsAll(c);
    }
  }

  public static <K,V> Map<K,V> unmodifiableMap(Map<K,V> m) {
	  return new UnmodifiableMap<K, V>(m);
  }

  static class UnmodifiableMap<K, V> implements Map<K, V> {
	  private Map<K, V> inner;

	  UnmodifiableMap(Map<K, V> m) {
	    this.inner = m;
	  }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
      return inner.containsKey(key);
    }

    public boolean containsValue(Object value) {
      return inner.containsValue(value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
      return unmodifiableSet(inner.entrySet());
    }

    public V get(Object key) {
      return inner.get(key);
    }

    public boolean isEmpty() {
      return inner.isEmpty();
    }

    public Set<K> keySet() {
      return unmodifiableSet(inner.keySet());
    }

    public V put(K key, V value) {
      throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> t) {
      throw new UnsupportedOperationException();
    }

    public V remove(Object key) {
      throw new UnsupportedOperationException();
    }

    public int size() {
      return inner.size();
    }

    public Collection<V> values() {
      return unmodifiableCollection(inner.values());
    }

    public boolean equals(Object o) {
      return o == this || inner.equals(o);
    }

    public int hashCode() {
      return inner.hashCode();
    }

    public String toString() {
      return inner.toString();
    }
  }

  public static <K,V> SortedMap<K,V> unmodifiableSortedMap(SortedMap<K,V> m) {
    return new UnmodifiableSortedMap<K, V>(m);
  }

  static class UnmodifiableSortedMap<K, V> extends UnmodifiableMap<K, V> implements SortedMap<K, V> {
    private final SortedMap<K, V> sm;

    UnmodifiableSortedMap(SortedMap<K, V> m) {
      super(m);
      this.sm = m;
    }

    public Comparator<? super K> comparator() {
      return sm.comparator();
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return new UnmodifiableSortedMap<K, V>(sm.subMap(fromKey, toKey));
    }

    public SortedMap<K, V> headMap(K toKey) {
      return new UnmodifiableSortedMap<K, V>(sm.headMap(toKey));
    }

    public SortedMap<K, V> tailMap(K fromKey) {
      return new UnmodifiableSortedMap<K, V>(sm.tailMap(fromKey));
    }

    public K firstKey() {
      return sm.firstKey();
    }

    public K lastKey() {
      return sm.lastKey();
    }

    public Map.Entry<K, V> firstEntry() {
      Map.Entry<K, V> e = sm.firstEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }

    public Map.Entry<K, V> lastEntry() {
      Map.Entry<K, V> e = sm.lastEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }

    public Map.Entry<K, V> pollFirstEntry() {
      throw new UnsupportedOperationException();
    }

    public Map.Entry<K, V> pollLastEntry() {
      throw new UnsupportedOperationException();
    }

    public V putFirst(K key, V value) {
      throw new UnsupportedOperationException();
    }

    public V putLast(K key, V value) {
      throw new UnsupportedOperationException();
    }

    public SequencedSet<K> sequencedKeySet() {
      return unmodifiableSequencedSet(sm.sequencedKeySet());
    }

    public SequencedCollection<V> sequencedValues() {
      return unmodifiableSequencedCollection(sm.sequencedValues());
    }

    @Override
    public SortedMap<K, V> reversed() {
      return new UnmodifiableSortedMap<K, V>(sm.reversed());
    }
  }
  
  static class UnmodifiableIterator<T> implements Iterator<T> {
    private final Iterator<T> inner;
    
    UnmodifiableIterator(Iterator<T> inner) {
      this.inner = inner;
    }
    
    @Override
    public T next() {
      return inner.next();
    }

    @Override
    public boolean hasNext() {
      return inner.hasNext();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  
  static class UnmodifiableListIterator<T> extends UnmodifiableIterator<T> 
                                                   implements ListIterator<T> {
    private final ListIterator<T> innerListIterator;
    
    UnmodifiableListIterator(ListIterator<T> listIterator) {
      super(listIterator);
      
      this.innerListIterator = listIterator;
    }

    @Override
    public boolean hasPrevious() {
      return innerListIterator.hasPrevious();
    }

    @Override
    public T previous() {
      return innerListIterator.previous();
    }
  }
  
  static class UnmodifiableCollection<T> implements Collection<T> {
    private final Collection<T> inner;
    
    UnmodifiableCollection(Collection<T> inner) {
      this.inner = inner;
    }
    
    @Override
    public Iterator<T> iterator() {
      return new UnmodifiableIterator<T>(inner.iterator());
    }

    @Override
    public int size() {
      return inner.size();
    }

    @Override
    public boolean isEmpty() {
      return inner.isEmpty();
    }

    @Override
    public boolean contains(Object element) {
      return inner.contains(element);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return inner.containsAll(c);
    }

    @Override
    public boolean add(T element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
      return inner.toArray();
    }

    @Override
    public <S> S[] toArray(S[] array) {
      return inner.toArray(array);
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || inner.equals(o);
    }

    @Override
    public int hashCode() {
      return inner.hashCode();
    }

    @Override
    public String toString() {
      return inner.toString();
    }
  }
  
  public static <T> UnmodifiableCollection<T> unmodifiableCollection(Collection<T> collection) {
    return new UnmodifiableCollection<T>(collection);
  }

  static class UnmodifiableSet<T> extends UnmodifiableCollection<T> 
                                  implements Set<T> {
    UnmodifiableSet(Set<T> inner) {
      super(inner);
    }  
  }
  
  public static <T> Set<T> unmodifiableSet(Set<T> hs) {
    return new UnmodifiableSet<T>(hs);
  }
  
  private static final class ReverseComparator<T> implements Comparator<T> {

    Comparator<T> cmp;
    
    public ReverseComparator(Comparator<T> cmp) {
      this.cmp = cmp;
    }
    
    public int compare(T o1, T o2) {
      return - cmp.compare(o1, o2);
    }
  }

  public static <T> List<T> singletonList(T o) {
    ArrayList<T> list = new ArrayList<T>(1);
    list.add(o);
    return new UnmodifiableList(list);
  }

  public static <T> Set<T> singleton(T o) {
    Set<T> set = new HashSet<T>(1);
    set.add(o);
    return unmodifiableSet(set);
  }

  public static <K, V> Map<K, V> singletonMap(K key, V value) {
    Map<K, V> map = new HashMap<K, V>(1);
    map.put(key, value);
    return unmodifiableMap(map);
  }

  public static <T> SortedSet<T> unmodifiableSortedSet(SortedSet<T> s) {
    return new UnmodifiableSortedSet<T>(s);
  }

  static class UnmodifiableSortedSet<T> extends UnmodifiableSet<T>
      implements SortedSet<T>, Serializable {
    private final SortedSet<T> ss;

    UnmodifiableSortedSet(SortedSet<T> s) {
      super(s);
      this.ss = Objects.requireNonNull(s);
    }

    public Comparator<? super T> comparator() { return ss.comparator(); }
    public SortedSet<T> subSet(T fromElement, T toElement) {
      return new UnmodifiableSortedSet<T>(ss.subSet(fromElement, toElement));
    }
    public SortedSet<T> headSet(T toElement) {
      return new UnmodifiableSortedSet<T>(ss.headSet(toElement));
    }
    public SortedSet<T> tailSet(T fromElement) {
      return new UnmodifiableSortedSet<T>(ss.tailSet(fromElement));
    }
    public T first() { return ss.first(); }
    public T last() { return ss.last(); }
    @Override
    public SortedSet<T> reversed() {
      return new UnmodifiableSortedSet<T>(ss.reversed());
    }
  }

  public static <T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s) {
    return new UnmodifiableNavigableSet<T>(s);
  }

  static class UnmodifiableNavigableSet<T> extends UnmodifiableSortedSet<T>
      implements NavigableSet<T>, Serializable {
    private final NavigableSet<T> ns;

    UnmodifiableNavigableSet(NavigableSet<T> s) {
      super(s);
      this.ns = Objects.requireNonNull(s);
    }

    public T lower(T e) { return ns.lower(e); }
    public T floor(T e) { return ns.floor(e); }
    public T ceiling(T e) { return ns.ceiling(e); }
    public T higher(T e) { return ns.higher(e); }
    public T pollFirst() { throw new UnsupportedOperationException(); }
    public T pollLast() { throw new UnsupportedOperationException(); }
    public NavigableSet<T> descendingSet() {
      return new UnmodifiableNavigableSet<T>(ns.descendingSet());
    }
    public Iterator<T> descendingIterator() {
      return new UnmodifiableIterator<T>(ns.descendingIterator());
    }
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
      return new UnmodifiableNavigableSet<T>(ns.subSet(fromElement, fromInclusive, toElement, toInclusive));
    }
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
      return new UnmodifiableNavigableSet<T>(ns.headSet(toElement, inclusive));
    }
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
      return new UnmodifiableNavigableSet<T>(ns.tailSet(fromElement, inclusive));
    }
    @Override
    public NavigableSet<T> reversed() {
      return descendingSet();
    }
  }

  public static <K, V> NavigableMap<K, V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> m) {
    return new UnmodifiableNavigableMap<K, V>(m);
  }

  static class UnmodifiableNavigableMap<K, V> extends UnmodifiableSortedMap<K, V>
      implements NavigableMap<K, V>, Serializable {
    private final NavigableMap<K, ? extends V> nm;

    @SuppressWarnings("unchecked")
    UnmodifiableNavigableMap(NavigableMap<K, ? extends V> m) {
      super((SortedMap<K, V>) m);
      this.nm = Objects.requireNonNull(m);
    }

    public Entry<K, V> lowerEntry(K key) {
      Entry<K, ? extends V> e = nm.lowerEntry(key);
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }
    public K lowerKey(K key) { return nm.lowerKey(key); }
    public Entry<K, V> floorEntry(K key) {
      Entry<K, ? extends V> e = nm.floorEntry(key);
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }
    public K floorKey(K key) { return nm.floorKey(key); }
    public Entry<K, V> ceilingEntry(K key) {
      Entry<K, ? extends V> e = nm.ceilingEntry(key);
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }
    public K ceilingKey(K key) { return nm.ceilingKey(key); }
    public Entry<K, V> higherEntry(K key) {
      Entry<K, ? extends V> e = nm.higherEntry(key);
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }
    public K higherKey(K key) { return nm.higherKey(key); }
    @Override
    public Entry<K, V> firstEntry() {
      Entry<K, ? extends V> e = nm.firstEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }
    @Override
    public Entry<K, V> lastEntry() {
      Entry<K, ? extends V> e = nm.lastEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }
    @Override
    public Entry<K, V> pollFirstEntry() { throw new UnsupportedOperationException(); }
    @Override
    public Entry<K, V> pollLastEntry() { throw new UnsupportedOperationException(); }
    public NavigableMap<K, V> descendingMap() {
      return new UnmodifiableNavigableMap<K, V>(nm.descendingMap());
    }
    public NavigableSet<K> navigableKeySet() {
      return unmodifiableNavigableSet(nm.navigableKeySet());
    }
    public NavigableSet<K> descendingKeySet() {
      return unmodifiableNavigableSet(nm.descendingKeySet());
    }
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      return new UnmodifiableNavigableMap<K, V>(nm.subMap(fromKey, fromInclusive, toKey, toInclusive));
    }
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      return new UnmodifiableNavigableMap<K, V>(nm.headMap(toKey, inclusive));
    }
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      return new UnmodifiableNavigableMap<K, V>(nm.tailMap(fromKey, inclusive));
    }
    @Override
    public NavigableMap<K, V> reversed() {
      return descendingMap();
    }
  }

  public static <T> SequencedCollection<T> unmodifiableSequencedCollection(SequencedCollection<? extends T> c) {
    return new UnmodifiableSequencedCollection<T>(c);
  }

  static class UnmodifiableSequencedCollection<T> extends UnmodifiableCollection<T>
      implements SequencedCollection<T>, Serializable {
    private final SequencedCollection<? extends T> sc;

    @SuppressWarnings("unchecked")
    UnmodifiableSequencedCollection(SequencedCollection<? extends T> sc) {
      super((Collection<T>) sc);
      this.sc = Objects.requireNonNull(sc);
    }

    @Override
    public SequencedCollection<T> reversed() {
      return new UnmodifiableSequencedCollection<T>(sc.reversed());
    }

    @Override
    public void addFirst(T element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(T element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public T getFirst() {
      return sc.getFirst();
    }

    @Override
    public T getLast() {
      return sc.getLast();
    }

    @Override
    public T removeFirst() {
      throw new UnsupportedOperationException();
    }

    @Override
    public T removeLast() {
      throw new UnsupportedOperationException();
    }
  }

  public static <T> SequencedSet<T> unmodifiableSequencedSet(SequencedSet<? extends T> s) {
    return new UnmodifiableSequencedSet<T>(s);
  }

  static class UnmodifiableSequencedSet<T> extends UnmodifiableSequencedCollection<T>
      implements SequencedSet<T>, Serializable {
    private final SequencedSet<? extends T> ss;

    UnmodifiableSequencedSet(SequencedSet<? extends T> ss) {
      super(ss);
      this.ss = Objects.requireNonNull(ss);
    }

    @Override
    public SequencedSet<T> reversed() {
      return new UnmodifiableSequencedSet<T>(ss.reversed());
    }
  }

  public static <K, V> SequencedMap<K, V> unmodifiableSequencedMap(SequencedMap<? extends K, ? extends V> m) {
    return new UnmodifiableSequencedMap<K, V>(m);
  }

  static class UnmodifiableSequencedMap<K, V> extends UnmodifiableMap<K, V>
      implements SequencedMap<K, V>, Serializable {
    private final SequencedMap<? extends K, ? extends V> sm;

    @SuppressWarnings("unchecked")
    UnmodifiableSequencedMap(SequencedMap<? extends K, ? extends V> sm) {
      super((Map<K, V>) sm);
      this.sm = Objects.requireNonNull(sm);
    }

    @Override
    public SequencedMap<K, V> reversed() {
      return new UnmodifiableSequencedMap<K, V>(sm.reversed());
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
      Map.Entry<? extends K, ? extends V> e = sm.firstEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
      Map.Entry<? extends K, ? extends V> e = sm.lastEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
      throw new UnsupportedOperationException();
    }

    @Override
    public V putFirst(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V putLast(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SequencedSet<K> sequencedKeySet() {
      return unmodifiableSequencedSet(sm.sequencedKeySet());
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
      return unmodifiableSequencedCollection(sm.sequencedValues());
    }
  }

  public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
    return new SetFromMap<E>(map);
  }

  static class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable {
    private final Map<E, Boolean> m;
    private transient Set<E> s;

    SetFromMap(Map<E, Boolean> map) {
      if (!map.isEmpty()) {
        throw new IllegalArgumentException("Map is not empty");
      }
      this.m = Objects.requireNonNull(map);
      this.s = map.keySet();
    }

    @Override
    public void clear() { m.clear(); }
    @Override
    public int size() { return m.size(); }
    @Override
    public boolean isEmpty() { return m.isEmpty(); }
    @Override
    public boolean contains(Object o) { return m.containsKey(o); }
    @Override
    public boolean remove(Object o) { return m.remove(o) != null; }
    @Override
    public boolean add(E e) { return m.put(e, Boolean.TRUE) == null; }
    @Override
    public Iterator<E> iterator() { return s.iterator(); }
    @Override
    public Object[] toArray() { return s.toArray(); }
    @Override
    public <T> T[] toArray(T[] a) { return s.toArray(a); }
    @Override
    public String toString() { return s.toString(); }
    @Override
    public int hashCode() { return s.hashCode(); }
    @Override
    public boolean equals(Object o) { return o == this || s.equals(o); }
    @Override
    public boolean containsAll(Collection<?> c) { return s.containsAll(c); }
    @Override
    public boolean removeAll(Collection<?> c) { return s.removeAll(c); }
    @Override
    public boolean retainAll(Collection<?> c) { return s.retainAll(c); }
  }

  public static <E> SequencedSet<E> newSequencedSetFromMap(SequencedMap<E, Boolean> map) {
    return new SetFromSequencedMap<E>(map);
  }

  static class SetFromSequencedMap<E> extends AbstractSet<E>
      implements SequencedSet<E>, Serializable {
    private final SequencedMap<E, Boolean> m;
    private transient SequencedSet<E> s;

    SetFromSequencedMap(SequencedMap<E, Boolean> map) {
      this(map, true);
    }

    SetFromSequencedMap(SequencedMap<E, Boolean> map, boolean checkEmpty) {
      if (checkEmpty && !map.isEmpty()) {
        throw new IllegalArgumentException("Map is not empty");
      }
      this.m = Objects.requireNonNull(map);
      this.s = map.sequencedKeySet();
    }

    @Override
    public void clear() { m.clear(); }
    @Override
    public int size() { return m.size(); }
    @Override
    public boolean isEmpty() { return m.isEmpty(); }
    @Override
    public boolean contains(Object o) { return m.containsKey(o); }
    @Override
    public boolean remove(Object o) { return m.remove(o) != null; }
    @Override
    public boolean add(E e) { return m.put(e, Boolean.TRUE) == null; }
    @Override
    public Iterator<E> iterator() { return s.iterator(); }
    @Override
    public Object[] toArray() { return s.toArray(); }
    @Override
    public <T> T[] toArray(T[] a) { return s.toArray(a); }
    @Override
    public String toString() { return s.toString(); }
    @Override
    public int hashCode() { return s.hashCode(); }
    @Override
    public boolean equals(Object o) { return o == this || s.equals(o); }
    @Override
    public boolean containsAll(Collection<?> c) { return s.containsAll(c); }
    @Override
    public boolean removeAll(Collection<?> c) { return s.removeAll(c); }
    @Override
    public boolean retainAll(Collection<?> c) { return s.retainAll(c); }

    @Override
    public SequencedSet<E> reversed() {
      return new SetFromSequencedMap<E>(m.reversed(), false);
    }

    @Override
    public void addFirst(E e) {
      m.putFirst(e, Boolean.TRUE);
    }

    @Override
    public void addLast(E e) {
      m.putLast(e, Boolean.TRUE);
    }

    @Override
    public E getFirst() {
      return s.getFirst();
    }

    @Override
    public E getLast() {
      return s.getLast();
    }

    @Override
    public E removeFirst() {
      Map.Entry<E, Boolean> e = m.pollFirstEntry();
      if (e == null) {
        throw new NoSuchElementException();
      }
      return e.getKey();
    }

    @Override
    public E removeLast() {
      Map.Entry<E, Boolean> e = m.pollLastEntry();
      if (e == null) {
        throw new NoSuchElementException();
      }
      return e.getKey();
    }
  }

  public static <T> Queue<T> asLifoQueue(Deque<T> deque) {
    return new AsLifoQueue<T>(deque);
  }

  static class AsLifoQueue<T> extends AbstractQueue<T> implements Queue<T>, Serializable {
    private final Deque<T> q;

    AsLifoQueue(Deque<T> deque) {
      this.q = Objects.requireNonNull(deque);
    }

    @Override
    public boolean add(T e) { q.addFirst(e); return true; }
    @Override
    public boolean offer(T e) { return q.offerFirst(e); }
    @Override
    public T poll() { return q.pollFirst(); }
    @Override
    public T remove() { return q.removeFirst(); }
    @Override
    public T peek() { return q.peekFirst(); }
    @Override
    public T element() { return q.getFirst(); }
    @Override
    public void clear() { q.clear(); }
    @Override
    public int size() { return q.size(); }
    @Override
    public boolean isEmpty() { return q.isEmpty(); }
    @Override
    public boolean contains(Object o) { return q.contains(o); }
    @Override
    public boolean remove(Object o) { return q.remove(o); }
    @Override
    public Iterator<T> iterator() { return q.iterator(); }
    @Override
    public Object[] toArray() { return q.toArray(); }
    @Override
    public <E> E[] toArray(E[] a) { return q.toArray(a); }
    @Override
    public String toString() { return q.toString(); }
  }

  private static final EmptyNavigableSet<?> EMPTY_NAVIGABLE_SET = new EmptyNavigableSet<Object>();

  @SuppressWarnings("unchecked")
  public static <E> SortedSet<E> emptySortedSet() {
    return (SortedSet<E>) EMPTY_NAVIGABLE_SET;
  }

  @SuppressWarnings("unchecked")
  public static <E> NavigableSet<E> emptyNavigableSet() {
    return (NavigableSet<E>) EMPTY_NAVIGABLE_SET;
  }

  static class EmptyNavigableSet<E> extends AbstractSet<E> implements NavigableSet<E>, Serializable {
    @Override
    public Iterator<E> iterator() { return emptyIterator(); }
    @Override
    public int size() { return 0; }
    @Override
    public boolean isEmpty() { return true; }
    @Override
    public boolean contains(Object o) { return false; }
    @Override
    public Comparator<? super E> comparator() { return null; }
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) { return this; }
    @Override
    public SortedSet<E> headSet(E toElement) { return this; }
    @Override
    public SortedSet<E> tailSet(E fromElement) { return this; }
    @Override
    public E first() { throw new NoSuchElementException(); }
    @Override
    public E last() { throw new NoSuchElementException(); }
    @Override
    public E lower(E e) { return null; }
    @Override
    public E floor(E e) { return null; }
    @Override
    public E ceiling(E e) { return null; }
    @Override
    public E higher(E e) { return null; }
    @Override
    public E pollFirst() { return null; }
    @Override
    public E pollLast() { return null; }
    @Override
    public NavigableSet<E> descendingSet() { return this; }
    @Override
    public Iterator<E> descendingIterator() { return emptyIterator(); }
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) { return this; }
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) { return this; }
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) { return this; }
    @Override
    public NavigableSet<E> reversed() { return this; }
  }

  private static final EmptyNavigableMap<?, ?> EMPTY_NAVIGABLE_MAP = new EmptyNavigableMap<Object, Object>();

  @SuppressWarnings("unchecked")
  public static <K, V> SortedMap<K, V> emptySortedMap() {
    return (SortedMap<K, V>) EMPTY_NAVIGABLE_MAP;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> NavigableMap<K, V> emptyNavigableMap() {
    return (NavigableMap<K, V>) EMPTY_NAVIGABLE_MAP;
  }

  static class EmptyNavigableMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
    @Override
    public int size() { return 0; }
    @Override
    public boolean isEmpty() { return true; }
    @Override
    public boolean containsKey(Object key) { return false; }
    @Override
    public boolean containsValue(Object value) { return false; }
    @Override
    public V get(Object key) { return null; }
    @Override
    public Set<Entry<K, V>> entrySet() { return emptySet(); }
    @Override
    public NavigableSet<K> keySet() { return emptyNavigableSet(); }
    @Override
    public Collection<V> values() { return emptySet(); }
    @Override
    public Comparator<? super K> comparator() { return null; }
    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) { return this; }
    @Override
    public SortedMap<K, V> headMap(K toKey) { return this; }
    @Override
    public SortedMap<K, V> tailMap(K fromKey) { return this; }
    @Override
    public K firstKey() { throw new NoSuchElementException(); }
    @Override
    public K lastKey() { throw new NoSuchElementException(); }
    @Override
    public Entry<K, V> lowerEntry(K key) { return null; }
    @Override
    public K lowerKey(K key) { return null; }
    @Override
    public Entry<K, V> floorEntry(K key) { return null; }
    @Override
    public K floorKey(K key) { return null; }
    @Override
    public Entry<K, V> ceilingEntry(K key) { return null; }
    @Override
    public K ceilingKey(K key) { return null; }
    @Override
    public Entry<K, V> higherEntry(K key) { return null; }
    @Override
    public K higherKey(K key) { return null; }
    @Override
    public Entry<K, V> firstEntry() { return null; }
    @Override
    public Entry<K, V> lastEntry() { return null; }
    @Override
    public Entry<K, V> pollFirstEntry() { return null; }
    @Override
    public Entry<K, V> pollLastEntry() { return null; }
    @Override
    public NavigableMap<K, V> descendingMap() { return this; }
    @Override
    public NavigableSet<K> navigableKeySet() { return emptyNavigableSet(); }
    @Override
    public NavigableSet<K> descendingKeySet() { return emptyNavigableSet(); }
    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) { return this; }
    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) { return this; }
    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) { return this; }
    @Override
    public NavigableMap<K, V> reversed() { return this; }
  }
}
