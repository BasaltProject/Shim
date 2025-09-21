/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;

public class PriorityQueue<E> extends AbstractQueue<E> implements Serializable {
  private static final long serialVersionUID = -7720805057305804111L;

  private static final int DEFAULT_INITIAL_CAPACITY = 11;

  private transient Object[] queue;
  private int size;
  private final Comparator<? super E> comparator;
  private transient int modCount;

  public PriorityQueue() {
    this(DEFAULT_INITIAL_CAPACITY, null);
  }

  public PriorityQueue(int initialCapacity) {
    this(initialCapacity, null);
  }

  public PriorityQueue(Comparator<? super E> comparator) {
    this(DEFAULT_INITIAL_CAPACITY, comparator);
  }

  public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
    if (initialCapacity < 1) {
      throw new IllegalArgumentException();
    }
    this.queue = new Object[initialCapacity];
    this.comparator = comparator;
  }

  @SuppressWarnings("unchecked")
  public PriorityQueue(Collection<? extends E> c) {
    if (c instanceof SortedSet<?>) {
      SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
      this.comparator = (Comparator<? super E>) ss.comparator();
      initElementsFromCollection(ss);
    } else if (c instanceof PriorityQueue<?>) {
      PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
      this.comparator = (Comparator<? super E>) pq.comparator();
      initFromPriorityQueue(pq);
    } else {
      this.comparator = null;
      initFromCollection(c);
    }
  }

  @SuppressWarnings("unchecked")
  public PriorityQueue(PriorityQueue<? extends E> c) {
    this.comparator = (Comparator<? super E>) c.comparator();
    initFromPriorityQueue(c);
  }

  @SuppressWarnings("unchecked")
  public PriorityQueue(SortedSet<? extends E> c) {
    this.comparator = (Comparator<? super E>) c.comparator();
    initElementsFromCollection(c);
  }

  private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
    if (c.getClass() == PriorityQueue.class) {
      this.queue = new Object[c.size()];
      System.arraycopy(c.queue, 0, this.queue, 0, c.size());
      this.size = c.size();
    } else {
      initFromCollection(c);
    }
  }

  private void initElementsFromCollection(Collection<? extends E> c) {
    Object[] a = c.toArray();
    if (c.getClass() != ArrayList.class) {
      a = Arrays.copyOf(a, a.length, Object[].class);
    }
    int len = a.length;
    if (len == 1 || this.comparator != null) {
      for (Object e : a) {
        if (e == null) {
          throw new NullPointerException();
        }
      }
    }
    this.queue = a;
    this.size = a.length;
  }

  private void initFromCollection(Collection<? extends E> c) {
    initElementsFromCollection(c);
    heapify();
  }

  private void grow(int minCapacity) {
    int oldCapacity = queue.length;
    int newCapacity = oldCapacity + ((oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1));
    if (newCapacity < 0) {
      newCapacity = Integer.MAX_VALUE - 8;
    }
    if (newCapacity < minCapacity) {
      newCapacity = minCapacity;
    }
    queue = Arrays.copyOf(queue, newCapacity);
  }

  @Override
  public boolean add(E e) {
    return offer(e);
  }

  @Override
  public boolean offer(E e) {
    if (e == null) {
      throw new NullPointerException();
    }
    modCount++;
    int i = size;
    if (i >= queue.length) {
      grow(i + 1);
    }
    siftUp(i, e);
    size = i + 1;
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E peek() {
    return (size == 0) ? null : (E) queue[0];
  }

  @Override
  @SuppressWarnings("unchecked")
  public E poll() {
    if (size == 0) {
      return null;
    }
    int s = --size;
    modCount++;
    E result = (E) queue[0];
    E x = (E) queue[s];
    queue[s] = null;
    if (s != 0) {
      siftDown(0, x);
    }
    return result;
  }

  private int indexOf(Object o) {
    if (o != null) {
      for (int i = 0; i < size; i++) {
        if (o.equals(queue[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public boolean remove(Object o) {
    int i = indexOf(o);
    if (i == -1) {
      return false;
    } else {
      removeAt(i);
      return true;
    }
  }

  @SuppressWarnings("unchecked")
  private E removeAt(int i) {
    modCount++;
    int s = --size;
    if (s == i) {
      queue[i] = null;
    } else {
      E moved = (E) queue[s];
      queue[s] = null;
      siftDown(i, moved);
      if (queue[i] == moved) {
        siftUp(i, moved);
        if (queue[i] != moved) {
          return moved;
        }
      }
    }
    return null;
  }

  @Override
  public boolean contains(Object o) {
    return indexOf(o) != -1;
  }

  @Override
  public Object[] toArray() {
    return Arrays.copyOf(queue, size);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    int size = this.size;
    if (a.length < size) {
      return (T[]) Arrays.copyOf(queue, size, a.getClass());
    }
    System.arraycopy(queue, 0, a, 0, size);
    if (a.length > size) {
      a[size] = null;
    }
    return a;
  }

  @Override
  public Iterator<E> iterator() {
    return new Itr();
  }

  private final class Itr implements Iterator<E> {
    private int cursor = 0;
    private int lastRet = -1;
    private int expectedModCount = modCount;

    @Override
    public boolean hasNext() {
      return cursor < size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      if (expectedModCount != modCount) {
        throw new ConcurrentModificationException();
      }
      if (cursor >= size) {
        throw new NoSuchElementException();
      }
      lastRet = cursor;
      return (E) queue[cursor++];
    }

    @Override
    public void remove() {
      if (expectedModCount != modCount) {
        throw new ConcurrentModificationException();
      }
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      E moved = removeAt(lastRet);
      lastRet = -1;
      if (moved == null) {
        cursor--;
      } else {
        cursor--;
      }
      expectedModCount = modCount;
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void clear() {
    modCount++;
    for (int i = 0; i < size; i++) {
      queue[i] = null;
    }
    size = 0;
  }

  public Comparator<? super E> comparator() {
    return comparator;
  }

  private void siftUp(int k, E x) {
    if (comparator != null) {
      siftUpUsingComparator(k, x, comparator);
    } else {
      siftUpComparable(k, x);
    }
  }

  @SuppressWarnings("unchecked")
  private void siftUpComparable(int k, E x) {
    Comparable<? super E> key = (Comparable<? super E>) x;
    while (k > 0) {
      int parent = (k - 1) >>> 1;
      Object e = queue[parent];
      if (key.compareTo((E) e) >= 0) {
        break;
      }
      queue[k] = e;
      k = parent;
    }
    queue[k] = key;
  }

  @SuppressWarnings("unchecked")
  private void siftUpUsingComparator(int k, E x, Comparator<? super E> cmp) {
    while (k > 0) {
      int parent = (k - 1) >>> 1;
      Object e = queue[parent];
      if (cmp.compare(x, (E) e) >= 0) {
        break;
      }
      queue[k] = e;
      k = parent;
    }
    queue[k] = x;
  }

  private void siftDown(int k, E x) {
    if (comparator != null) {
      siftDownUsingComparator(k, x, comparator);
    } else {
      siftDownComparable(k, x);
    }
  }

  @SuppressWarnings("unchecked")
  private void siftDownComparable(int k, E x) {
    Comparable<? super E> key = (Comparable<? super E>) x;
    int half = size >>> 1;
    while (k < half) {
      int child = (k << 1) + 1;
      Object c = queue[child];
      int right = child + 1;
      if (right < size && ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0) {
        c = queue[child = right];
      }
      if (key.compareTo((E) c) <= 0) {
        break;
      }
      queue[k] = c;
      k = child;
    }
    queue[k] = key;
  }

  @SuppressWarnings("unchecked")
  private void siftDownUsingComparator(int k, E x, Comparator<? super E> cmp) {
    int half = size >>> 1;
    while (k < half) {
      int child = (k << 1) + 1;
      Object c = queue[child];
      int right = child + 1;
      if (right < size && cmp.compare((E) c, (E) queue[right]) > 0) {
        c = queue[child = right];
      }
      if (cmp.compare(x, (E) c) <= 0) {
        break;
      }
      queue[k] = c;
      k = child;
    }
    queue[k] = x;
  }

  @SuppressWarnings("unchecked")
  private void heapify() {
    int half = (size >>> 1) - 1;
    for (int i = half; i >= 0; i--) {
      siftDown(i, (E) queue[i]);
    }
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.NONNULL);
  }

  public boolean removeIf(java.util.function.Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    return bulkRemove(filter);
  }

  public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return bulkRemove(e -> c.contains(e));
  }

  public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return bulkRemove(e -> !c.contains(e));
  }

  @SuppressWarnings("unchecked")
  private boolean bulkRemove(java.util.function.Predicate<? super E> filter) {
    final int expectedModCount = ++modCount;
    final Object[] es = queue;
    final int end = size;
    int i;
    for (i = 0; i < end && !filter.test((E) es[i]); i++) {
      ;
    }
    if (i >= end) {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      return false;
    }
    final int beg = i;
    final boolean[] deathRow = new boolean[end - beg];
    deathRow[0] = true;
    for (i = beg + 1; i < end; i++) {
      if (filter.test((E) es[i])) {
        deathRow[i - beg] = true;
      }
    }
    if (modCount != expectedModCount) {
      throw new ConcurrentModificationException();
    }
    int w = beg;
    for (i = beg; i < end; i++) {
      if (!deathRow[i - beg]) {
        es[w++] = es[i];
      }
    }
    for (i = size = w; i < end; i++) {
      es[i] = null;
    }
    heapify();
    return true;
  }
}
