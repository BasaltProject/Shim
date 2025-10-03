/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityBlockingQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, Serializable {
  private static final long serialVersionUID = 5595510919245408276L;


  private static final int DEFAULT_INITIAL_CAPACITY = 11;
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private Object[] queue;
  private int size;
  private final Comparator<? super E> comparator;
  private final ReentrantLock lock;
  private final Condition notEmpty;

  public PriorityBlockingQueue() {
    this(DEFAULT_INITIAL_CAPACITY, null);
  }

  public PriorityBlockingQueue(int initialCapacity) {
    this(initialCapacity, null);
  }

  public PriorityBlockingQueue(int initialCapacity, Comparator<? super E> comparator) {
    if (initialCapacity < 1) {
      throw new IllegalArgumentException();
    }
    this.lock = new ReentrantLock();
    this.notEmpty = lock.newCondition();
    this.comparator = comparator;
    this.queue = new Object[Math.max(1, initialCapacity)];
  }

  @SuppressWarnings("unchecked")
  public PriorityBlockingQueue(Collection<? extends E> c) {
    this.lock = new ReentrantLock();
    this.notEmpty = lock.newCondition();
    if (c instanceof SortedSet<?>) {
      SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
      this.comparator = (Comparator<? super E>) ss.comparator();
      initElementsFromCollection(ss);
    } else if (c instanceof PriorityBlockingQueue<?>) {
      PriorityBlockingQueue<? extends E> pq = (PriorityBlockingQueue<? extends E>) c;
      this.comparator = (Comparator<? super E>) pq.comparator();
      initFromPriorityBlockingQueue(pq);
    } else if (c instanceof PriorityQueue<?>) {
      PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
      this.comparator = (Comparator<? super E>) pq.comparator();
      initElementsFromCollection(pq);
      heapify();
    } else {
      this.comparator = null;
      initElementsFromCollection(c);
      heapify();
    }
  }

  private void initElementsFromCollection(Collection<? extends E> c) {
    Object[] a = c.toArray();
    if (a.getClass() != Object[].class) {
      a = Arrays.copyOf(a, a.length, Object[].class);
    }
    for (Object o : a) {
      if (o == null) {
        throw new NullPointerException();
      }
    }
    this.queue = a;
    this.size = a.length;
  }

  private void initFromPriorityBlockingQueue(PriorityBlockingQueue<? extends E> pq) {
    pq.lock.lock();
    try {
      this.queue = Arrays.copyOf(pq.queue, pq.size, Object[].class);
      this.size = pq.size;
    } finally {
      pq.lock.unlock();
    }
  }

  private void tryGrow(int oldCap) {
    int newCap = oldCap + ((oldCap < 64) ? (oldCap + 2) : (oldCap >> 1));
    if (newCap - MAX_ARRAY_SIZE > 0) {
      int minCap = oldCap + 1;
      if (minCap < 0) {
        throw new OutOfMemoryError();
      }
      newCap = (minCap > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
    if (newCap > oldCap) {
      queue = Arrays.copyOf(queue, newCap);
    }
  }

  @SuppressWarnings("unchecked")
  private void siftUp(int k, E x) {
    if (comparator != null) {
      siftUpUsingComparator(k, x);
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
  private void siftUpUsingComparator(int k, E x) {
    while (k > 0) {
      int parent = (k - 1) >>> 1;
      Object e = queue[parent];
      if (comparator.compare(x, (E) e) >= 0) {
        break;
      }
      queue[k] = e;
      k = parent;
    }
    queue[k] = x;
  }

  @SuppressWarnings("unchecked")
  private void siftDown(int k, E x) {
    if (comparator != null) {
      siftDownUsingComparator(k, x);
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
        child = right;
        c = queue[child];
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
  private void siftDownUsingComparator(int k, E x) {
    int half = size >>> 1;
    while (k < half) {
      int child = (k << 1) + 1;
      Object c = queue[child];
      int right = child + 1;
      if (right < size && comparator.compare((E) c, (E) queue[right]) > 0) {
        child = right;
        c = queue[child];
      }
      if (comparator.compare(x, (E) c) <= 0) {
        break;
      }
      queue[k] = c;
      k = child;
    }
    queue[k] = x;
  }

  @SuppressWarnings("unchecked")
  private void heapify() {
    for (int i = (size >>> 1) - 1; i >= 0; i--) {
      siftDown(i, (E) queue[i]);
    }
  }

  @SuppressWarnings("unchecked")
  private E dequeue() {
    int n = size - 1;
    if (n < 0) {
      return null;
    }
    E result = (E) queue[0];
    E x = (E) queue[n];
    queue[n] = null;
    size = n;
    if (n > 0) {
      siftDown(0, x);
    }
    return result;
  }

  @Override
  public boolean add(E e) {
    return offer(e);
  }

  @Override
  public boolean offer(E e) {
    Objects.requireNonNull(e);
    lock.lock();
    try {
      int n = size;
      if (n >= queue.length) {
        tryGrow(n);
      }
      siftUp(n, e);
      size = n + 1;
      notEmpty.signal();
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void put(E e) {
    offer(e);
  }

  @Override
  public boolean offer(E e, long timeout, TimeUnit unit) {
    return offer(e);
  }

  @Override
  public E poll() {
    lock.lock();
    try {
      return dequeue();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E take() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      E result;
      while ((result = dequeue()) == null) {
        notEmpty.await();
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      E result;
      while ((result = dequeue()) == null) {
        if (nanos <= 0) {
          return null;
        }
        nanos = notEmpty.awaitNanos(nanos);
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public E peek() {
    lock.lock();
    try {
      return (size == 0) ? null : (E) queue[0];
    } finally {
      lock.unlock();
    }
  }

  public Comparator<? super E> comparator() {
    return comparator;
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return size;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int remainingCapacity() {
    return Integer.MAX_VALUE;
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

  @SuppressWarnings("unchecked")
  private void removeAt(int i) {
    int n = size - 1;
    if (n == i) {
      queue[i] = null;
      size = n;
    } else {
      E moved = (E) queue[n];
      queue[n] = null;
      size = n;
      siftDown(i, moved);
      if (queue[i] == moved) {
        siftUp(i, moved);
      }
    }
  }

  @Override
  public boolean remove(Object o) {
    lock.lock();
    try {
      int i = indexOf(o);
      if (i == -1) {
        return false;
      }
      removeAt(i);
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean contains(Object o) {
    lock.lock();
    try {
      return indexOf(o) != -1;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object[] toArray() {
    lock.lock();
    try {
      return Arrays.copyOf(queue, size);
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    lock.lock();
    try {
      if (a.length < size) {
        return (T[]) Arrays.copyOf(queue, size, a.getClass());
      }
      System.arraycopy(queue, 0, a, 0, size);
      if (a.length > size) {
        a[size] = null;
      }
      return a;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      for (int i = 0; i < size; i++) {
        queue[i] = null;
      }
      size = 0;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int drainTo(Collection<? super E> c) {
    return drainTo(c, Integer.MAX_VALUE);
  }

  @Override
  public int drainTo(Collection<? super E> c, int maxElements) {
    Objects.requireNonNull(c);
    if (c == this) {
      throw new IllegalArgumentException();
    }
    if (maxElements <= 0) {
      return 0;
    }
    lock.lock();
    try {
      int n = Math.min(size, maxElements);
      for (int i = 0; i < n; i++) {
        c.add(dequeue());
      }
      return n;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new Itr(toArray());
  }

  private class Itr implements Iterator<E> {
    final Object[] array;
    int cursor;
    int lastRet = -1;

    Itr(Object[] array) {
      this.array = array;
    }

    @Override
    public boolean hasNext() {
      return cursor < array.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E next() {
      if (cursor >= array.length) {
        throw new NoSuchElementException();
      }
      lastRet = cursor;
      return (E) array[cursor++];
    }

    @Override
    public void remove() {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      Object target = array[lastRet];
      lastRet = -1;
      PriorityBlockingQueue.this.remove(target);
    }
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.NONNULL);
  }
}
