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

import java.util.function.UnaryOperator;
import java.util.function.Predicate;
import java.util.function.IntFunction;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.BiFunction;
import java.util.function.BiConsumer;
import java.io.Serializable;
import shim.Data;

public final class Collections {

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
    private static final long serialVersionUID = 2739099268398711800L;

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
    private static final long serialVersionUID = 8842843931221139166L;

    private Object readResolve() {
      return EMPTY_LIST;
    }

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
  public static final Map EMPTY_MAP = new EmptyMap<Object, Object>();

  @SuppressWarnings("rawtypes")
  public static final Set EMPTY_SET = new EmptySet<Object>();

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

  @SuppressWarnings("unchecked")
  public static <T> Comparator<T> reverseOrder() {
    return (Comparator<T>) ReverseComparator.REVERSE_ORDER;
  }

  @SuppressWarnings("unchecked")
  public static <T> Comparator<T> reverseOrder(Comparator<T> cmp) {
    if (cmp == null) {
      return (Comparator<T>) ReverseComparator.REVERSE_ORDER;
    } else if (cmp == ReverseComparator.REVERSE_ORDER) {
      return (Comparator<T>) Comparator.naturalOrder();
    } else if (cmp == Comparator.naturalOrder()) {
      return (Comparator<T>) ReverseComparator.REVERSE_ORDER;
    } else if (cmp instanceof ReverseComparator2) {
      return ((ReverseComparator2<T>) cmp).cmp;
    } else {
      return new ReverseComparator2<T>(cmp);
    }
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
    return new CheckedCollection<E>(c, type);
  }

  public static <E> Queue<E> checkedQueue(Queue<E> queue, Class<E> type) {
    return new CheckedQueue<E>(queue, type);
  }

  public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
    return new CheckedSet<E>(s, type);
  }

  public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s, Class<E> type) {
    return new CheckedSortedSet<E>(s, type);
  }

  public static <E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> s, Class<E> type) {
    return new CheckedNavigableSet<E>(s, type);
  }

  public static <E> List<E> checkedList(List<E> list, Class<E> type) {
    return list instanceof RandomAccess ? new CheckedRandomAccessList<E>(list, type)
                                        : new CheckedList<E>(list, type);
  }

  public static <K, V> Map<K, V> checkedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
    return new CheckedMap<K, V>(m, keyType, valueType);
  }

  public static <K, V> SortedMap<K, V> checkedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType) {
    return new CheckedSortedMap<K, V>(m, keyType, valueType);
  }

  public static <K, V> NavigableMap<K, V> checkedNavigableMap(NavigableMap<K, V> m, Class<K> keyType,
                                                              Class<V> valueType) {
    return new CheckedNavigableMap<K, V>(m, keyType, valueType);
  }

  public static <T> Collection<T> synchronizedCollection(Collection<T> c) {
    return new SynchronizedCollection<T>(c);
  }

  static <T> Collection<T> synchronizedCollection(Collection<T> c, Object mutex) {
    return new SynchronizedCollection<T>(c, mutex);
  }

  static <T> Set<T> synchronizedSet(Set<T> s, Object mutex) {
    return new SynchronizedSet<T>(s, mutex);
  }

  public static <T> Set<T> synchronizedSet(Set<T> s) {
    return new SynchronizedSet<T>(s);
  }

  public static <T> SortedSet<T> synchronizedSortedSet(SortedSet<T> s) {
    return new SynchronizedSortedSet<T>(s);
  }

  public static <T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s) {
    return new SynchronizedNavigableSet<T>(s);
  }

  public static <T> List<T> synchronizedList(List<T> list) {
    return list instanceof RandomAccess ? new SynchronizedRandomAccessList<T>(list)
                                        : new SynchronizedList<T>(list);
  }

  public static <K, V> Map<K, V> synchronizedMap(Map<K, V> m) {
    return new SynchronizedMap<K, V>(m);
  }

  public static <K, V> SortedMap<K, V> synchronizedSortedMap(SortedMap<K, V> m) {
    return new SynchronizedSortedMap<K, V>(m);
  }

  public static <K, V> NavigableMap<K, V> synchronizedNavigableMap(NavigableMap<K, V> m) {
    return new SynchronizedNavigableMap<K, V>(m);
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

  @SuppressWarnings("unchecked")
  public static <T> List<T> unmodifiableList(List<? extends T> l)  {
    List<T> list = (List<T>) l;
    return list instanceof RandomAccess ? new UnmodifiableRandomAccessList<T>(list) : new UnmodifiableList<T>(list);
  }

  static class UnmodifiableRandomAccessList<T> extends UnmodifiableList<T> implements RandomAccess {
    private static final long serialVersionUID = -2542308836966382001L;

    UnmodifiableRandomAccessList(List<T> list) {
      super(list);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
      return new UnmodifiableRandomAccessList<T>(list.subList(fromIndex, toIndex));
    }

    private Object writeReplace() {
      return new UnmodifiableList<T>(list);
    }
  }

  static class UnmodifiableList<T> extends UnmodifiableCollection<T> implements List<T> {
    private static final long serialVersionUID = -283967356065247728L;


    final List<T> list;

    UnmodifiableList(List<T> l) {
      super(l);
      this.list = l;
    }

    public boolean equals(Object other) {
      return other == this || list.equals(other);
    }

    public int hashCode() {
      return list.hashCode();
    }

    public String toString() {
      return list.toString();
    }

    public T get(int index) {
      return list.get(index);
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
      return new UnmodifiableIterator<T>(list.iterator());
    }

    public int indexOf(Object value) {
      return list.indexOf(value);
    }

    public int lastIndexOf(Object value) {
      return list.lastIndexOf(value);
    }

    public boolean isEmpty() {
      return list.isEmpty();
    }

    public ListIterator<T> listIterator(int index) {
      return new UnmodifiableListIterator<T>(list.listIterator(index));
    }

    public ListIterator<T> listIterator() {
      return new UnmodifiableListIterator<T>(list.listIterator());
    }

    public int size() {
      return list.size();
    }

    public boolean contains(Object element) {
      return list.contains(element);
    }

    public boolean addAll(Collection<? extends T> collection) {
      throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
      return list.toArray();
    }

    public <S> S[] toArray(S[] array) {
      return list.toArray(array);
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
      return list.containsAll(c);
    }
  
    public List<T> subList(int fromIndex, int toIndex) {
      return new UnmodifiableList<T>(list.subList(fromIndex, toIndex));
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

  @SuppressWarnings("unchecked")
  public static <K,V> Map<K,V> unmodifiableMap(Map<? extends K, ? extends V> m) {
	  return new UnmodifiableMap<K, V>((Map<K, V>) m);
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
    private static final long serialVersionUID = -1034234728574286014L;

	  final Map<K, V> m;

	  UnmodifiableMap(Map<K, V> m) {
	    this.m = m;
	  }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
      return m.containsKey(key);
    }

    public boolean containsValue(Object value) {
      return m.containsValue(value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
      return new UnmodifiableEntrySet<K, V>(m.entrySet());
    }

    public V get(Object key) {
      return m.get(key);
    }

    public boolean isEmpty() {
      return m.isEmpty();
    }

    public Set<K> keySet() {
      return unmodifiableSet(m.keySet());
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
      return m.size();
    }

    public Collection<V> values() {
      return unmodifiableCollection(m.values());
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
      return m.getOrDefault(k, defaultValue);
    }

    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
      m.forEach(action);
    }
}

  @SuppressWarnings("unchecked")
  public static <K,V> SortedMap<K,V> unmodifiableSortedMap(SortedMap<K, ? extends V> m) {
    return new UnmodifiableSortedMap<K, V>((SortedMap<K, V>) m);
  }

  static class UnmodifiableSortedMap<K, V> extends UnmodifiableMap<K, V> implements SortedMap<K, V> {
    private static final long serialVersionUID = -8806743815996713206L;

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
    private static final long serialVersionUID = 1820017752578914078L;

    final Collection<T> c;
    
    UnmodifiableCollection(Collection<T> c) {
      this.c = c;
    }
    
    @Override
    public Iterator<T> iterator() {
      return new UnmodifiableIterator<T>(c.iterator());
    }

    @Override
    public int size() {
      return c.size();
    }

    @Override
    public boolean isEmpty() {
      return c.isEmpty();
    }

    @Override
    public boolean contains(Object element) {
      return c.contains(element);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return this.c.containsAll(c);
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
      return c.toArray();
    }

    @Override
    public <S> S[] toArray(S[] array) {
      return c.toArray(array);
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || c.equals(o);
    }

    @Override
    public int hashCode() {
      return c.hashCode();
    }

    @Override
    public String toString() {
      return c.toString();
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
    private static final long serialVersionUID = -9215047833775013803L;

    UnmodifiableSet(Set<T> inner) {
      super(inner);
    }  
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Set<T> unmodifiableSet(Set<? extends T> hs) {
    return new UnmodifiableSet<T>((Set<T>) hs);
  }
  
  static boolean eq(Object o1, Object o2) {
    return o1 == null ? o2 == null : o1.equals(o2);
  }

  private static Iterator<?> singletonIterator(final Object e) {
    return new Iterator<Object>() {
      private boolean hasNext = true;

      public boolean hasNext() {
        return hasNext;
      }

      public Object next() {
        if (hasNext) {
          hasNext = false;
          return e;
        }
        throw new NoSuchElementException();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void forEachRemaining(java.util.function.Consumer<? super Object> action) {
        Objects.requireNonNull(action);
        if (hasNext) {
          hasNext = false;
          action.accept(e);
        }
      }
    };
  }

  private static class ReverseComparator implements Comparator<Comparable<Object>>, java.io.Serializable {
    private static final long serialVersionUID = 7207038068494060240L;

    static final ReverseComparator REVERSE_ORDER = new ReverseComparator();

    public int compare(Comparable<Object> c1, Comparable<Object> c2) {
      return c2.compareTo(c1);
    }

    private Object readResolve() {
      return Collections.reverseOrder();
    }

    @Override
    public Comparator<Comparable<Object>> reversed() {
      return Comparator.naturalOrder();
    }
  }

  private static class ReverseComparator2<T> implements Comparator<T>, java.io.Serializable {
    private static final long serialVersionUID = 4374092139857L;

    final Comparator<T> cmp;

    ReverseComparator2(Comparator<T> cmp) {
      assert cmp != null;
      this.cmp = cmp;
    }

    public int compare(T t1, T t2) {
      return cmp.compare(t2, t1);
    }

    public boolean equals(Object o) {
      return (o == this) || (o instanceof ReverseComparator2 && cmp.equals(((ReverseComparator2<?>) o).cmp));
    }

    public int hashCode() {
      return cmp.hashCode() ^ Integer.MIN_VALUE;
    }

    @Override
    public Comparator<T> reversed() {
      return cmp;
    }
  }

  private static class SingletonSet<E> extends AbstractSet<E> implements java.io.Serializable {
    private static final long serialVersionUID = 3193687207550431679L;

    private final E element;

    SingletonSet(E e) {
      element = e;
    }

    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
      return (Iterator<E>) singletonIterator(element);
    }

    public int size() {
      return 1;
    }

    public boolean contains(Object o) {
      return eq(o, element);
    }

    @Override
    public void forEach(java.util.function.Consumer<? super E> action) {
      action.accept(element);
    }

    @Override
    public boolean removeIf(java.util.function.Predicate<? super E> filter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
      return element == null ? 0 : element.hashCode();
    }
  }

  private static class SingletonList<E> extends AbstractList<E> implements RandomAccess, java.io.Serializable {
    private static final long serialVersionUID = 3093736618740652951L;

    private final E element;

    SingletonList(E obj) {
      element = obj;
    }

    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
      return (Iterator<E>) singletonIterator(element);
    }

    public int size() {
      return 1;
    }

    public boolean contains(Object obj) {
      return eq(obj, element);
    }

    public E get(int index) {
      if (index != 0) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
      }
      return element;
    }

    @Override
    public void forEach(java.util.function.Consumer<? super E> action) {
      action.accept(element);
    }

    @Override
    public boolean removeIf(java.util.function.Predicate<? super E> filter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(java.util.function.UnaryOperator<E> operator) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super E> c) {
    }

    @Override
    public int hashCode() {
      return 31 + (element == null ? 0 : element.hashCode());
    }
  }

  private static class SingletonMap<K, V> extends AbstractMap<K, V> implements java.io.Serializable {
    private static final long serialVersionUID = -6979724477215052911L;

    private final K k;
    private final V v;

    SingletonMap(K key, V value) {
      k = key;
      v = value;
    }

    public int size() {
      return 1;
    }

    public boolean isEmpty() {
      return false;
    }

    public boolean containsKey(Object key) {
      return eq(key, k);
    }

    public boolean containsValue(Object value) {
      return eq(value, v);
    }

    public V get(Object key) {
      return (eq(key, k) ? v : null);
    }

    private transient Set<K> keySet;
    private transient Set<Map.Entry<K, V>> entrySet;
    private transient Collection<V> values;

    public Set<K> keySet() {
      if (keySet == null) {
        keySet = singleton(k);
      }
      return keySet;
    }

    public Set<Map.Entry<K, V>> entrySet() {
      if (entrySet == null) {
        entrySet = Collections.<Map.Entry<K, V>>singleton(new SimpleImmutableEntry<K, V>(k, v));
      }
      return entrySet;
    }

    public Collection<V> values() {
      if (values == null) {
        values = singleton(v);
      }
      return values;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
      return eq(key, k) ? v : defaultValue;
    }

    @Override
    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
      action.accept(k, v);
    }

    @Override
    public void replaceAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
      return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
    }
  }

  private static class EmptySet<E> extends AbstractSet<E> implements java.io.Serializable {
    private static final long serialVersionUID = 1582296315990362920L;

    public Iterator<E> iterator() {
      return emptyIterator();
    }

    public int size() {
      return 0;
    }

    public boolean isEmpty() {
      return true;
    }

    public void clear() {
    }

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

    @Override
    public void forEach(java.util.function.Consumer<? super E> action) {
      Objects.requireNonNull(action);
    }

    @Override
    public boolean removeIf(java.util.function.Predicate<? super E> filter) {
      Objects.requireNonNull(filter);
      return false;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    private Object readResolve() {
      return EMPTY_SET;
    }
  }

  private static class EmptyMap<K, V> extends AbstractMap<K, V> implements java.io.Serializable {
    private static final long serialVersionUID = 6428348081105594320L;

    public int size() {
      return 0;
    }

    public boolean isEmpty() {
      return true;
    }

    public void clear() {
    }

    public boolean containsKey(Object key) {
      return false;
    }

    public boolean containsValue(Object value) {
      return false;
    }

    public V get(Object key) {
      return null;
    }

    public Set<K> keySet() {
      return emptySet();
    }

    public Collection<V> values() {
      return emptySet();
    }

    public Set<Map.Entry<K, V>> entrySet() {
      return emptySet();
    }

    public boolean equals(Object o) {
      return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
    }

    public int hashCode() {
      return 0;
    }

    @Override
    public V getOrDefault(Object k, V defaultValue) {
      return defaultValue;
    }

    @Override
    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
      Objects.requireNonNull(action);
    }

    @Override
    public void replaceAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
      Objects.requireNonNull(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
    }

    private Object readResolve() {
      return EMPTY_MAP;
    }
  }
  public static <T> Set<T> singleton(T o) {
    return new SingletonSet<T>(o);
  }

  public static <T> List<T> singletonList(T o) {
    return new SingletonList<T>(o);
  }

  public static <K, V> Map<K, V> singletonMap(K key, V value) {
    return new SingletonMap<K, V>(key, value);
  }

  public static <T> SortedSet<T> unmodifiableSortedSet(SortedSet<T> s) {
    return new UnmodifiableSortedSet<T>(s);
  }

  static class UnmodifiableSortedSet<T> extends UnmodifiableSet<T>
      implements SortedSet<T>, Serializable {
    private static final long serialVersionUID = -4929149591599911165L;

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
    private static final long serialVersionUID = -6027448201786391929L;

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
    private static final long serialVersionUID = -4858195264774772197L;

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
    private static final long serialVersionUID = -6060065079711684830L;

    private SequencedCollection<? extends T> sc() {
      return (SequencedCollection<? extends T>) c;
    }

    @SuppressWarnings("unchecked")
    UnmodifiableSequencedCollection(SequencedCollection<? extends T> sc) {
      super((Collection<T>) sc);
      Objects.requireNonNull(sc);
    }

    @Override
    public SequencedCollection<T> reversed() {
      return new UnmodifiableSequencedCollection<T>(sc().reversed());
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
      return sc().getFirst();
    }

    @Override
    public T getLast() {
      return sc().getLast();
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
    private static final long serialVersionUID = -2153469532349793522L;

    private SequencedSet<? extends T> ss() {
      return (SequencedSet<? extends T>) c;
    }

    UnmodifiableSequencedSet(SequencedSet<? extends T> ss) {
      super(ss);
      Objects.requireNonNull(ss);
    }

    @Override
    public SequencedSet<T> reversed() {
      return new UnmodifiableSequencedSet<T>(ss().reversed());
    }
  }

  public static <K, V> SequencedMap<K, V> unmodifiableSequencedMap(SequencedMap<? extends K, ? extends V> m) {
    return new UnmodifiableSequencedMap<K, V>(m);
  }

  static class UnmodifiableSequencedMap<K, V> extends UnmodifiableMap<K, V>
      implements SequencedMap<K, V>, Serializable {
    private static final long serialVersionUID = -8171676257373950636L;

    private SequencedMap<? extends K, ? extends V> sm() {
      return (SequencedMap<? extends K, ? extends V>) m;
    }

    @SuppressWarnings("unchecked")
    UnmodifiableSequencedMap(SequencedMap<? extends K, ? extends V> sm) {
      super((Map<K, V>) sm);
      Objects.requireNonNull(sm);
    }

    @Override
    public SequencedMap<K, V> reversed() {
      return new UnmodifiableSequencedMap<K, V>(sm().reversed());
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
      Map.Entry<? extends K, ? extends V> e = sm().firstEntry();
      return e == null ? null : Map.entry(e.getKey(), e.getValue());
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
      Map.Entry<? extends K, ? extends V> e = sm().lastEntry();
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
      return unmodifiableSequencedSet(sm().sequencedKeySet());
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
      return unmodifiableSequencedCollection(sm().sequencedValues());
    }
  }

  public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
    return new SetFromMap<E>(map);
  }

  static class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable {
    private static final long serialVersionUID = 2454657854757543876L;

    final Map<E, Boolean> m;
    transient Set<E> s;

    SetFromMap(Map<E, Boolean> map) {
      this(map, true);
    }

    SetFromMap(Map<E, Boolean> map, boolean checkEmpty) {
      if (checkEmpty && !map.isEmpty()) {
        throw new IllegalArgumentException("Map is not empty");
      }
      this.m = Objects.requireNonNull(map);
      this.s = map.keySet();
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
      stream.defaultReadObject();
      s = m instanceof SequencedMap ? ((SequencedMap<E, Boolean>) m).sequencedKeySet() : m.keySet();
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
    return new SequencedSetFromMap<E>(map);
  }

  static class SequencedSetFromMap<E> extends SetFromMap<E> implements SequencedSet<E> {
    private static final long serialVersionUID = -3943479744841433802L;

    SequencedSetFromMap(SequencedMap<E, Boolean> map) {
      super(map);
      this.s = map.sequencedKeySet();
    }

    private SequencedMap<E, Boolean> sm() {
      return (SequencedMap<E, Boolean>) m;
    }

    private SequencedSet<E> ss() {
      return (SequencedSet<E>) s;
    }

    @Override
    public SequencedSet<E> reversed() {
      return new SequencedSetFromMap<E>(sm().reversed(), false);
    }

    private SequencedSetFromMap(SequencedMap<E, Boolean> map, boolean unused) {
      super(map, false);
      this.s = map.sequencedKeySet();
    }

    @Override
    public void addFirst(E e) {
      sm().putFirst(e, Boolean.TRUE);
    }

    @Override
    public void addLast(E e) {
      sm().putLast(e, Boolean.TRUE);
    }

    @Override
    public E getFirst() {
      return ss().getFirst();
    }

    @Override
    public E getLast() {
      return ss().getLast();
    }

    @Override
    public E removeFirst() {
      Map.Entry<E, Boolean> e = sm().pollFirstEntry();
      if (e == null) {
        throw new NoSuchElementException();
      }
      return e.getKey();
    }

    @Override
    public E removeLast() {
      Map.Entry<E, Boolean> e = sm().pollLastEntry();
      if (e == null) {
        throw new NoSuchElementException();
      }
      return e.getKey();
    }
  }

  public static <T> Queue<T> asLifoQueue(Deque<T> deque) {
    return new AsLIFOQueue<T>(deque);
  }

  static class AsLIFOQueue<T> extends AbstractQueue<T> implements Queue<T>, Serializable {
    private static final long serialVersionUID = 1802017725587941708L;

    private final Deque<T> q;

    AsLIFOQueue(Deque<T> deque) {
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


  static class SynchronizedCollection<E> implements Collection<E>, Serializable {
    private static final long serialVersionUID = 3053995032091335093L;

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
    private static final long serialVersionUID = 487447009682186044L;

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
    private static final long serialVersionUID = 8695801310862127406L;

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
    private static final long serialVersionUID = -5505529816273629798L;

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
    private static final long serialVersionUID = -7754090372962971524L;

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
    private static final long serialVersionUID = 1530674583602358482L;

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
    private static final long serialVersionUID = 1978198479659022715L;

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
    private static final long serialVersionUID = -8798146769416483793L;

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
    private static final long serialVersionUID = 699392247599746807L;

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


  static class CheckedCollection<E> implements Collection<E>, Serializable {
    private static final long serialVersionUID = 1578914078182001775L;

    final Collection<E> c;
    final Class<E> type;
    E[] zeroLengthElementArray;

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
    private static final long serialVersionUID = 1433151992604707767L;

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
    private static final long serialVersionUID = 4694047833775013803L;

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
    private static final long serialVersionUID = 1599911165492914959L;

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
    private static final long serialVersionUID = -5429120189805438922L;

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
    private static final long serialVersionUID = 65247728283967356L;

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
    private static final long serialVersionUID = 1638200125423088369L;

    CheckedRandomAccessList(List<E> list, Class<E> type) {
      super(list, type);
    }

    public List<E> subList(int fromIndex, int toIndex) {
      return new CheckedRandomAccessList<E>(list.subList(fromIndex, toIndex), type);
    }
  }

  static class CheckedMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 5742860141034234728L;

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
    private static final long serialVersionUID = 1599671320688067438L;

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
    private static final long serialVersionUID = -4852462692372534096L;

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
