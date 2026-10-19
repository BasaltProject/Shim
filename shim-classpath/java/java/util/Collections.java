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

  private static final int SHUFFLE_THRESHOLD = 5;
  private static final int REVERSE_THRESHOLD = 18;

  public static void shuffle(List<?> list, Random rnd) {
    shuffle(list, (java.util.random.RandomGenerator) rnd);
  }

  public static void shuffle(List<?> list) {
    shuffle(list, new Random());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void shuffle(List<?> list, java.util.random.RandomGenerator rnd) {
    int size = list.size();
    if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
      for (int i = size; i > 1; i--) {
        swap(list, i - 1, rnd.nextInt(i));
      }
    } else {
      Object[] array = list.toArray();
      for (int i = size; i > 1; i--) {
        Object tmp = array[i - 1];
        int j = rnd.nextInt(i);
        array[i - 1] = array[j];
        array[j] = tmp;
      }
      ListIterator it = list.listIterator();
      for (Object e : array) {
        it.next();
        it.set(e);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void swap(List<?> list, int i, int j) {
    final List l = list;
    l.set(i, l.set(j, l.get(i)));
  }

  public static <T extends Comparable<? super T>> void sort(List<T> list) {
    list.sort(null);
  }

  public static <T> void sort(List<T> list, Comparator<? super T> c) {
    list.sort(c);
  }

  public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
    int low = 0;
    int high = list.size() - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      Comparable<? super T> midVal = list.get(mid);
      int cmp = midVal.compareTo(key);
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  @SuppressWarnings("unchecked")
  public static <T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c) {
    if (c == null) {
      return binarySearch((List<? extends Comparable<? super T>>) list, key);
    }
    int low = 0;
    int high = list.size() - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      T midVal = list.get(mid);
      int cmp = c.compare(midVal, key);
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void reverse(List<?> list) {
    int size = list.size();
    if (size < REVERSE_THRESHOLD || list instanceof RandomAccess) {
      for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
        swap(list, i, j);
      }
    } else {
      ListIterator fwd = list.listIterator();
      ListIterator rev = list.listIterator(size);
      for (int i = 0, mid = list.size() >> 1; i < mid; i++) {
        Object tmp = fwd.next();
        fwd.set(rev.previous());
        rev.set(tmp);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> void fill(List<? super T> list, T obj) {
    int size = list.size();
    if (size < 25 || list instanceof RandomAccess) {
      for (int i = 0; i < size; i++) {
        list.set(i, obj);
      }
    } else {
      ListIterator<? super T> itr = list.listIterator();
      for (int i = 0; i < size; i++) {
        itr.next();
        itr.set(obj);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> void copy(List<? super T> dest, List<? extends T> src) {
    int srcSize = src.size();
    if (srcSize > dest.size()) {
      throw new IndexOutOfBoundsException("Source does not fit in dest");
    }
    if (srcSize < 10 || (src instanceof RandomAccess && dest instanceof RandomAccess)) {
      for (int i = 0; i < srcSize; i++) {
        dest.set(i, src.get(i));
      }
    } else {
      ListIterator<? super T> di = dest.listIterator();
      ListIterator<? extends T> si = src.listIterator();
      for (int i = 0; i < srcSize; i++) {
        di.next();
        di.set(si.next());
      }
    }
  }

  public static <T extends Object & Comparable<? super T>> T min(Collection<? extends T> coll) {
    Iterator<? extends T> i = coll.iterator();
    T candidate = i.next();
    while (i.hasNext()) {
      T next = i.next();
      if (next.compareTo(candidate) < 0) {
        candidate = next;
      }
    }
    return candidate;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> T min(Collection<? extends T> coll, Comparator<? super T> comp) {
    if (comp == null) {
      return (T) min((Collection) coll);
    }
    Iterator<? extends T> i = coll.iterator();
    T candidate = i.next();
    while (i.hasNext()) {
      T next = i.next();
      if (comp.compare(next, candidate) < 0) {
        candidate = next;
      }
    }
    return candidate;
  }

  public static <T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll) {
    Iterator<? extends T> i = coll.iterator();
    T candidate = i.next();
    while (i.hasNext()) {
      T next = i.next();
      if (next.compareTo(candidate) > 0) {
        candidate = next;
      }
    }
    return candidate;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> T max(Collection<? extends T> coll, Comparator<? super T> comp) {
    if (comp == null) {
      return (T) max((Collection) coll);
    }
    Iterator<? extends T> i = coll.iterator();
    T candidate = i.next();
    while (i.hasNext()) {
      T next = i.next();
      if (comp.compare(next, candidate) > 0) {
        candidate = next;
      }
    }
    return candidate;
  }

  public static void rotate(List<?> list, int distance) {
    rotateList(list, distance);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void rotateList(List list, int distance) {
    int size = list.size();
    if (size == 0) {
      return;
    }
    distance = distance % size;
    if (distance < 0) {
      distance += size;
    }
    if (distance == 0) {
      return;
    }
    for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
      Object displaced = list.get(cycleStart);
      int i = cycleStart;
      do {
        i += distance;
        if (i >= size) {
          i -= size;
        }
        displaced = list.set(i, displaced);
        nMoved++;
      } while (i != cycleStart);
    }
  }

  public static <T> boolean replaceAll(List<T> list, T oldVal, T newVal) {
    boolean result = false;
    int size = list.size();
    if (size < 11 || list instanceof RandomAccess) {
      if (oldVal == null) {
        for (int i = 0; i < size; i++) {
          if (list.get(i) == null) {
            list.set(i, newVal);
            result = true;
          }
        }
      } else {
        for (int i = 0; i < size; i++) {
          if (oldVal.equals(list.get(i))) {
            list.set(i, newVal);
            result = true;
          }
        }
      }
    } else {
      ListIterator<T> itr = list.listIterator();
      if (oldVal == null) {
        for (int i = 0; i < size; i++) {
          if (itr.next() == null) {
            itr.set(newVal);
            result = true;
          }
        }
      } else {
        for (int i = 0; i < size; i++) {
          if (oldVal.equals(itr.next())) {
            itr.set(newVal);
            result = true;
          }
        }
      }
    }
    return result;
  }

  public static int indexOfSubList(List<?> source, List<?> target) {
    int sourceSize = source.size();
    int targetSize = target.size();
    int maxCandidate = sourceSize - targetSize;
    nextCand:
    for (int candidate = 0; candidate <= maxCandidate; candidate++) {
      for (int i = 0, j = candidate; i < targetSize; i++, j++) {
        if (!Objects.equals(target.get(i), source.get(j))) {
          continue nextCand;
        }
      }
      return candidate;
    }
    return -1;
  }

  public static int lastIndexOfSubList(List<?> source, List<?> target) {
    int sourceSize = source.size();
    int targetSize = target.size();
    int maxCandidate = sourceSize - targetSize;
    nextCand:
    for (int candidate = maxCandidate; candidate >= 0; candidate--) {
      for (int i = 0, j = candidate; i < targetSize; i++, j++) {
        if (!Objects.equals(target.get(i), source.get(j))) {
          continue nextCand;
        }
      }
      return candidate;
    }
    return -1;
  }

  public static int frequency(Collection<?> c, Object o) {
    int result = 0;
    if (o == null) {
      for (Object e : c) {
        if (e == null) {
          result++;
        }
      }
    } else {
      for (Object e : c) {
        if (o.equals(e)) {
          result++;
        }
      }
    }
    return result;
  }

  public static boolean disjoint(Collection<?> c1, Collection<?> c2) {
    Collection<?> contains = c2;
    Collection<?> iterate = c1;
    if (c1 instanceof Set) {
      iterate = c2;
      contains = c1;
    } else if (!(c2 instanceof Set)) {
      int c1size = c1.size();
      int c2size = c2.size();
      if (c1size == 0 || c2size == 0) {
        return true;
      }
      if (c1size > c2size) {
        iterate = c2;
        contains = c1;
      }
    }
    for (Object e : iterate) {
      if (contains.contains(e)) {
        return false;
      }
    }
    return true;
  }

  @SafeVarargs
  public static <T> boolean addAll(Collection<? super T> c, T... elements) {
    boolean result = false;
    for (T element : elements) {
      result |= c.add(element);
    }
    return result;
  }

  public static <T> List<T> nCopies(int n, T o) {
    if (n < 0) {
      throw new IllegalArgumentException("List length = " + n);
    }
    return new CopiesList<T>(n, o);
  }

  private static class CopiesList<E> extends AbstractList<E> implements RandomAccess, Serializable {
    final int n;
    final E element;

    CopiesList(int n, E e) {
      this.n = n;
      element = e;
    }

    public int size() {
      return n;
    }

    public boolean contains(Object obj) {
      return n != 0 && Objects.equals(obj, element);
    }

    public int indexOf(Object o) {
      return contains(o) ? 0 : -1;
    }

    public int lastIndexOf(Object o) {
      return contains(o) ? n - 1 : -1;
    }

    public Object[] toArray() {
      final Object[] a = new Object[n];
      if (element != null) {
        Arrays.fill(a, 0, n, element);
      }
      return a;
    }

    public E get(int index) {
      Objects.checkIndex(index, n);
      return element;
    }

    public List<E> subList(int fromIndex, int toIndex) {
      if (fromIndex < 0) {
        throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
      }
      if (toIndex > n) {
        throw new IndexOutOfBoundsException("toIndex = " + toIndex);
      }
      if (fromIndex > toIndex) {
        throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
      }
      return new CopiesList<E>(toIndex - fromIndex, element);
    }
  }

  public static final List EMPTY_LIST = new EmptyList<Object>();

  private static class EmptyList<E> extends AbstractList<E> implements RandomAccess, Serializable {
    public Iterator<E> iterator() {
      return emptyIterator();
    }

    public ListIterator<E> listIterator() {
      return emptyListIterator();
    }

    public int size() {
      return 0;
    }

    public boolean isEmpty() {
      return true;
    }

    public void clear() { }

    public boolean contains(Object obj) {
      return false;
    }

    public boolean containsAll(Collection<?> c) {
      return c.isEmpty();
    }

    public Object[] toArray() {
      return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
      if (a.length > 0) {
        a[0] = null;
      }
      return a;
    }

    public E get(int index) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }

    public boolean equals(Object o) {
      return (o instanceof List) && ((List<?>) o).isEmpty();
    }

    public int hashCode() {
      return 1;
    }

    public boolean removeIf(java.util.function.Predicate<? super E> filter) {
      Objects.requireNonNull(filter);
      return false;
    }

    public void replaceAll(java.util.function.UnaryOperator<E> operator) {
      Objects.requireNonNull(operator);
    }

    public void sort(Comparator<? super E> c) { }

    public void forEach(java.util.function.Consumer<? super E> action) {
      Objects.requireNonNull(action);
    }
  }

  public static final <E> List<E> emptyList() {
    return EMPTY_LIST;
  }

  @SuppressWarnings("rawtypes")
  public static final Map EMPTY_MAP = new UnmodifiableMap<Object, Object>(new HashMap<Object, Object>(0));

  @SuppressWarnings("rawtypes")
  public static final Set EMPTY_SET = new UnmodifiableSet<Object>(new HashSet<Object>(0));

  @SuppressWarnings("unchecked")
  public static final <K,V> Map<K,V> emptyMap() {
    return (Map<K, V>) EMPTY_MAP;
  }

  @SuppressWarnings("unchecked")
  public static final <T> Set<T> emptySet() {
    return (Set<T>) EMPTY_SET;
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
  
  public static <T> ArrayList<T> list(Enumeration<T> e) {
    ArrayList<T> result = new ArrayList<T>();
    while (e.hasMoreElements()) {
      result.add(e.nextElement());
    }
    return result;
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

  public static <E> Collection<E> checkedCollection(Collection<E> c, Class<E> type) {
    return new CheckedViews.CheckedCollection<E>(c, type);
  }

  public static <E> Queue<E> checkedQueue(Queue<E> queue, Class<E> type) {
    return new CheckedViews.CheckedQueue<E>(queue, type);
  }

  public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
    return new CheckedViews.CheckedSet<E>(s, type);
  }

  public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s, Class<E> type) {
    return new CheckedViews.CheckedSortedSet<E>(s, type);
  }

  public static <E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> s, Class<E> type) {
    return new CheckedViews.CheckedNavigableSet<E>(s, type);
  }

  public static <E> List<E> checkedList(List<E> list, Class<E> type) {
    return list instanceof RandomAccess ? new CheckedViews.CheckedRandomAccessList<E>(list, type)
                                        : new CheckedViews.CheckedList<E>(list, type);
  }

  public static <K, V> Map<K, V> checkedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
    return new CheckedViews.CheckedMap<K, V>(m, keyType, valueType);
  }

  public static <K, V> SortedMap<K, V> checkedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType) {
    return new CheckedViews.CheckedSortedMap<K, V>(m, keyType, valueType);
  }

  public static <K, V> NavigableMap<K, V> checkedNavigableMap(NavigableMap<K, V> m, Class<K> keyType,
                                                              Class<V> valueType) {
    return new CheckedViews.CheckedNavigableMap<K, V>(m, keyType, valueType);
  }

  public static <T> Collection<T> synchronizedCollection(Collection<T> c) {
    return new SyncViews.SynchronizedCollection<T>(c);
  }

  static <T> Collection<T> synchronizedCollection(Collection<T> c, Object mutex) {
    return new SyncViews.SynchronizedCollection<T>(c, mutex);
  }

  static <T> Set<T> synchronizedSet(Set<T> s, Object mutex) {
    return new SyncViews.SynchronizedSet<T>(s, mutex);
  }

  public static <T> Set<T> synchronizedSet(Set<T> s) {
    return new SyncViews.SynchronizedSet<T>(s);
  }

  public static <T> SortedSet<T> synchronizedSortedSet(SortedSet<T> s) {
    return new SyncViews.SynchronizedSortedSet<T>(s);
  }

  public static <T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s) {
    return new SyncViews.SynchronizedNavigableSet<T>(s);
  }

  public static <T> List<T> synchronizedList(List<T> list) {
    return list instanceof RandomAccess ? new SyncViews.SynchronizedRandomAccessList<T>(list)
                                        : new SyncViews.SynchronizedList<T>(list);
  }

  public static <K, V> Map<K, V> synchronizedMap(Map<K, V> m) {
    return new SyncViews.SynchronizedMap<K, V>(m);
  }

  public static <K, V> SortedMap<K, V> synchronizedSortedMap(SortedMap<K, V> m) {
    return new SyncViews.SynchronizedSortedMap<K, V>(m);
  }

  public static <K, V> NavigableMap<K, V> synchronizedNavigableMap(NavigableMap<K, V> m) {
    return new SyncViews.SynchronizedNavigableMap<K, V>(m);
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

  static class UnmodifiableList<T> implements List<T>, java.io.Serializable {

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
  
    public List<T> subList(int fromIndex, int toIndex) {
      return new UnmodifiableList<T>(inner.subList(fromIndex, toIndex));
    }

    public boolean removeIf(java.util.function.Predicate<? super T> filter) {
      throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    public void replaceAll(java.util.function.UnaryOperator<T> operator) {
      throw new UnsupportedOperationException();
    }

    public void sort(Comparator<? super T> c) {
      throw new UnsupportedOperationException();
    }
}

  public static <K,V> Map<K,V> unmodifiableMap(Map<K,V> m) {
	  return new UnmodifiableMap<K, V>(m);
  }

  static class UnmodifiableEntrySet<K, V> extends UnmodifiableSet<Map.Entry<K, V>> {
    UnmodifiableEntrySet(Set<Map.Entry<K, V>> s) {
      super(s);
    }

    private static <K, V> Map.Entry<K, V> wrap(Map.Entry<? extends K, ? extends V> e) {
      return new UnmodifiableMapEntry<K, V>(e);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      final Iterator<Map.Entry<K, V>> i = super.iterator();
      return new Iterator<Map.Entry<K, V>>() {
        public boolean hasNext() {
          return i.hasNext();
        }

        public Map.Entry<K, V> next() {
          return wrap(i.next());
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public Object[] toArray() {
      Object[] a = super.toArray();
      for (int i = 0; i < a.length; i++) {
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> e = (Map.Entry<K, V>) a[i];
        a[i] = wrap(e);
      }
      return a;
    }

    @Override
    public <T> T[] toArray(T[] a) {
      Object[] arr = super.toArray(a.length == 0 ? a : Arrays.copyOf(a, 0));
      for (int i = 0; i < arr.length; i++) {
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> e = (Map.Entry<K, V>) arr[i];
        arr[i] = wrap(e);
      }
      if (arr.length > a.length) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) arr;
        return result;
      }
      System.arraycopy(arr, 0, a, 0, arr.length);
      if (a.length > arr.length) {
        a[arr.length] = null;
      }
      return a;
    }
  }

  static class UnmodifiableMapEntry<K, V> implements Map.Entry<K, V> {
    private final Map.Entry<? extends K, ? extends V> e;

    UnmodifiableMapEntry(Map.Entry<? extends K, ? extends V> e) {
      this.e = Objects.requireNonNull(e);
    }

    public K getKey() {
      return e.getKey();
    }

    public V getValue() {
      return e.getValue();
    }

    public V setValue(V value) {
      throw new UnsupportedOperationException();
    }

    public int hashCode() {
      return e.hashCode();
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> t = (Map.Entry<?, ?>) o;
      return Objects.equals(e.getKey(), t.getKey()) && Objects.equals(e.getValue(), t.getValue());
    }

    public String toString() {
      return e.toString();
    }
  }

  static class UnmodifiableMap<K, V> implements Map<K, V>, java.io.Serializable {
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
      return new UnmodifiableEntrySet<K, V>(inner.entrySet());
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
  
    public V putIfAbsent(K key, V value) {
      throw new UnsupportedOperationException();
    }

    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    public boolean replace(K key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
    }

    public V replace(K key, V value) {
      throw new UnsupportedOperationException();
    }

    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
      throw new UnsupportedOperationException();
    }

    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V,
        ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    public void replaceAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
      throw new UnsupportedOperationException();
    }

    public V getOrDefault(Object k, V defaultValue) {
      return inner.getOrDefault(k, defaultValue);
    }

    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
      inner.forEach(action);
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

    @Override
    public int nextIndex() {
      return innerListIterator.nextIndex();
    }

    @Override
    public int previousIndex() {
      return innerListIterator.previousIndex();
    }
  }
  
  static class UnmodifiableCollection<T> implements Collection<T>, java.io.Serializable {
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
  
    @Override
    public boolean removeIf(java.util.function.Predicate<? super T> filter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }
}
  
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> collection) {
    return new UnmodifiableCollection<T>((Collection<T>) collection);
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
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement,
        boolean toInclusive) { return this; }
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
