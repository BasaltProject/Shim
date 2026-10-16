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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, Serializable {
  private static final long serialVersionUID = -3223113410248163686L;


  static final class Waiter<E> {
    final boolean isData;
    volatile E item;
    final Condition condition;
    volatile boolean done;

    Waiter(boolean isData, E item, ReentrantLock lock) {
      this.isData = isData;
      this.item = item;
      this.condition = lock.newCondition();
    }
  }

  private final ReentrantLock lock;
  private final LinkedList<Waiter<E>> waiters = new LinkedList<Waiter<E>>();

  public SynchronousQueue() {
    this(false);
  }

  public SynchronousQueue(boolean fair) {
    this.lock = new ReentrantLock(fair);
  }

  @Override
  public void put(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    lock.lockInterruptibly();
    try {
      if (!waiters.isEmpty() && !waiters.peekFirst().isData) {
        Waiter<E> consumer = waiters.removeFirst();
        consumer.item = e;
        consumer.done = true;
        consumer.condition.signal();
        return;
      }
      Waiter<E> node = new Waiter<E>(true, e, lock);
      waiters.addLast(node);
      try {
        while (!node.done) {
          node.condition.await();
        }
      } catch (InterruptedException ie) {
        if (!node.done) {
          waiters.remove(node);
        }
        throw ie;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(e);
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      if (!waiters.isEmpty() && !waiters.peekFirst().isData) {
        Waiter<E> consumer = waiters.removeFirst();
        consumer.item = e;
        consumer.done = true;
        consumer.condition.signal();
        return true;
      }
      if (nanos <= 0) {
        return false;
      }
      Waiter<E> node = new Waiter<E>(true, e, lock);
      waiters.addLast(node);
      try {
        while (!node.done) {
          if (nanos <= 0) {
            waiters.remove(node);
            return false;
          }
          nanos = node.condition.awaitNanos(nanos);
        }
        return true;
      } catch (InterruptedException ie) {
        if (!node.done) {
          waiters.remove(node);
        }
        throw ie;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offer(E e) {
    Objects.requireNonNull(e);
    lock.lock();
    try {
      if (!waiters.isEmpty() && !waiters.peekFirst().isData) {
        Waiter<E> consumer = waiters.removeFirst();
        consumer.item = e;
        consumer.done = true;
        consumer.condition.signal();
        return true;
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E take() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      if (!waiters.isEmpty() && waiters.peekFirst().isData) {
        Waiter<E> producer = waiters.removeFirst();
        E item = producer.item;
        producer.done = true;
        producer.condition.signal();
        return item;
      }
      Waiter<E> node = new Waiter<E>(false, null, lock);
      waiters.addLast(node);
      try {
        while (!node.done) {
          node.condition.await();
        }
        return node.item;
      } catch (InterruptedException ie) {
        if (!node.done) {
          waiters.remove(node);
        }
        throw ie;
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
      if (!waiters.isEmpty() && waiters.peekFirst().isData) {
        Waiter<E> producer = waiters.removeFirst();
        E item = producer.item;
        producer.done = true;
        producer.condition.signal();
        return item;
      }
      if (nanos <= 0) {
        return null;
      }
      Waiter<E> node = new Waiter<E>(false, null, lock);
      waiters.addLast(node);
      try {
        while (!node.done) {
          if (nanos <= 0) {
            waiters.remove(node);
            return null;
          }
          nanos = node.condition.awaitNanos(nanos);
        }
        return node.item;
      } catch (InterruptedException ie) {
        if (!node.done) {
          waiters.remove(node);
        }
        throw ie;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E poll() {
    lock.lock();
    try {
      if (!waiters.isEmpty() && waiters.peekFirst().isData) {
        Waiter<E> producer = waiters.removeFirst();
        E item = producer.item;
        producer.done = true;
        producer.condition.signal();
        return item;
      }
      return null;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public int remainingCapacity() {
    return 0;
  }

  @Override
  public void clear() {
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public E peek() {
    return null;
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.emptyIterator();
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.emptySpliterator();
  }

  @Override
  public Object[] toArray() {
    return new Object[0];
  }

  @Override
  public <T> T[] toArray(T[] a) {
    if (a.length > 0) {
      a[0] = null;
    }
    return a;
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
    int n = 0;
    for (E e; n < maxElements && (e = poll()) != null; ) {
      c.add(e);
      n++;
    }
    return n;
  }
}
