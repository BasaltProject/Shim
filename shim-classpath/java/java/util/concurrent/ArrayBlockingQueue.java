/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
  private static final long serialVersionUID = -8179116326529734882L;

  final Object[] items;
  int takeIndex;
  int putIndex;
  int count;

  final ReentrantLock lock;
  private final Condition notEmpty;
  private final Condition notFull;

  public ArrayBlockingQueue(int capacity) {
    this(capacity, false);
  }

  public ArrayBlockingQueue(int capacity, boolean fair) {
    if (capacity <= 0) {
      throw new IllegalArgumentException();
    }
    this.items = new Object[capacity];
    this.lock = new ReentrantLock(fair);
    this.notEmpty = lock.newCondition();
    this.notFull = lock.newCondition();
  }

  public ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
    this(capacity, fair);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      int i = 0;
      for (E e : c) {
        Objects.requireNonNull(e);
        if (i >= capacity) {
          throw new IllegalArgumentException("Exceeds capacity");
        }
        items[i++] = e;
      }
      count = i;
      putIndex = (i == capacity) ? 0 : i;
    } finally {
      lock.unlock();
    }
  }

  public boolean offer(E e) {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      if (count == items.length) {
        return false;
      }
      enqueue(e);
      return true;
    } finally {
      lock.unlock();
    }
  }

  public void put(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
      while (count == items.length) {
        notFull.await();
      }
      enqueue(e);
    } finally {
      lock.unlock();
    }
  }

  public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(e);
    long nanos = unit.toNanos(timeout);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
      while (count == items.length) {
        if (nanos <= 0) {
          return false;
        }
        nanos = notFull.awaitNanos(nanos);
      }
      enqueue(e);
      return true;
    } finally {
      lock.unlock();
    }
  }

  public E poll() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return (count == 0) ? null : dequeue();
    } finally {
      lock.unlock();
    }
  }

  public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
      while (count == 0) {
        notEmpty.await();
      }
      return dequeue();
    } finally {
      lock.unlock();
    }
  }

  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
      while (count == 0) {
        if (nanos <= 0) {
          return null;
        }
        nanos = notEmpty.awaitNanos(nanos);
      }
      return dequeue();
    } finally {
      lock.unlock();
    }
  }

  public E peek() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return (count == 0) ? null : itemAt(takeIndex);
    } finally {
      lock.unlock();
    }
  }

  public int size() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return count;
    } finally {
      lock.unlock();
    }
  }

  public int remainingCapacity() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return items.length - count;
    } finally {
      lock.unlock();
    }
  }

  public int drainTo(Collection<? super E> c) {
    return drainTo(c, Integer.MAX_VALUE);
  }

  public int drainTo(Collection<? super E> c, int maxElements) {
    Objects.requireNonNull(c);
    if (c == this) {
      throw new IllegalArgumentException();
    }
    if (maxElements <= 0) {
      return 0;
    }
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      int n = Math.min(maxElements, count);
      for (int i = 0; i < n; i++) {
        c.add(dequeue());
      }
      return n;
    } finally {
      lock.unlock();
    }
  }

  private void enqueue(E e) {
    items[putIndex] = e;
    if (++putIndex == items.length) {
      putIndex = 0;
    }
    count++;
    notEmpty.signal();
  }

  private E dequeue() {
    @SuppressWarnings("unchecked")
    E e = (E) items[takeIndex];
    items[takeIndex] = null;
    if (++takeIndex == items.length) {
      takeIndex = 0;
    }
    count--;
    notFull.signal();
    return e;
  }

  @SuppressWarnings("unchecked")
  private E itemAt(int i) {
    return (E) items[i];
  }

  public Iterator<E> iterator() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      Object[] copy = new Object[count];
      for (int i = 0, idx = takeIndex; i < count; i++) {
        copy[i] = items[idx];
        if (++idx == items.length) {
          idx = 0;
        }
      }
      return new Itr(copy);
    } finally {
      lock.unlock();
    }
  }

  private class Itr implements Iterator<E> {
    private final Object[] snapshot;
    private int cursor;

    Itr(Object[] snapshot) {
      this.snapshot = snapshot;
    }

    public boolean hasNext() {
      return cursor < snapshot.length;
    }

    @SuppressWarnings("unchecked")
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return (E) snapshot[cursor++];
    }
  }
}
