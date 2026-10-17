/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
    implements BlockingQueue<E> {

  private final transient ReentrantLock lock = new ReentrantLock();
  private final PriorityQueue<E> q = new PriorityQueue<E>();
  private final Condition available = lock.newCondition();

  public DelayQueue() {
  }

  public DelayQueue(Collection<? extends E> c) {
    this();
    addAll(c);
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
      q.offer(e);
      if (q.peek() == e) {
        available.signal();
      }
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
  public E remove() {
    return super.remove();
  }

  @Override
  public E poll() {
    lock.lock();
    try {
      E first = q.peek();
      if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
        return null;
      } else {
        return q.poll();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E take() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      for (;;) {
        E first = q.peek();
        if (first == null) {
          available.await();
        } else {
          long delay = first.getDelay(TimeUnit.NANOSECONDS);
          if (delay <= 0) {
            return q.poll();
          }
          available.awaitNanos(delay);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      for (;;) {
        E first = q.peek();
        if (first == null) {
          if (nanos <= 0) {
            return null;
          }
          nanos = available.awaitNanos(nanos);
        } else {
          long delay = first.getDelay(TimeUnit.NANOSECONDS);
          if (delay <= 0) {
            return q.poll();
          }
          if (nanos <= 0) {
            return null;
          }
          long waitTime = (nanos < delay) ? nanos : delay;
          long timeLeft = available.awaitNanos(waitTime);
          nanos -= (waitTime - timeLeft);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E peek() {
    lock.lock();
    try {
      return q.peek();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return q.size();
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
      int n = 0;
      for (E first; n < maxElements && (first = q.peek()) != null
          && first.getDelay(TimeUnit.NANOSECONDS) <= 0; ) {
        c.add(first);
        q.poll();
        n++;
      }
      return n;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      q.clear();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int remainingCapacity() {
    return Integer.MAX_VALUE;
  }

  @Override
  public Object[] toArray() {
    lock.lock();
    try {
      return q.toArray();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public <T> T[] toArray(T[] a) {
    lock.lock();
    try {
      return q.toArray(a);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean remove(Object o) {
    lock.lock();
    try {
      return q.remove(o);
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
        throw new java.util.NoSuchElementException();
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
      DelayQueue.this.remove(target);
    }
  }
}
