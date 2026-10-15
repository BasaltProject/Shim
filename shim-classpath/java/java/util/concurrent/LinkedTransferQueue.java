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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LinkedTransferQueue<E> extends AbstractQueue<E>
    implements TransferQueue<E>, Serializable {

  private static final long serialVersionUID = -3223113410248163686L;

  static final class Node<E> {
    final boolean isData;
    final boolean isTransfer;
    volatile E item;
    final Condition condition;
    volatile boolean matched;
    volatile boolean cancelled;
    Node<E> prev;
    Node<E> next;

    Node(boolean isData, boolean isTransfer, E item, ReentrantLock lock) {
      this.isData = isData;
      this.isTransfer = isTransfer;
      this.item = item;
      this.condition = (isTransfer || !isData) ? lock.newCondition() : null;
    }
  }

  private final ReentrantLock lock = new ReentrantLock();
  private Node<E> head;
  private Node<E> tail;
  private int dataCount;
  private int waiterCount;

  public LinkedTransferQueue() {
  }

  public LinkedTransferQueue(Collection<? extends E> c) {
    this();
    addAll(c);
  }

  private void enqueue(Node<E> node) {
    if (tail == null) {
      head = tail = node;
    } else {
      tail.next = node;
      node.prev = tail;
      tail = node;
    }
    if (node.isData) {
      dataCount++;
    } else {
      waiterCount++;
    }
  }

  private void unlink(Node<E> node) {
    if (node.prev != null) {
      node.prev.next = node.next;
    } else {
      head = node.next;
    }
    if (node.next != null) {
      node.next.prev = node.prev;
    } else {
      tail = node.prev;
    }
    node.prev = null;
    node.next = null;
    if (node.isData) {
      dataCount--;
    } else {
      waiterCount--;
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
  public boolean offer(E e) {
    Objects.requireNonNull(e);
    lock.lock();
    try {
      if (waiterCount > 0) {
        Node<E> waiter = head;
        unlink(waiter);
        waiter.item = e;
        waiter.matched = true;
        waiter.condition.signal();
        return true;
      }
      Node<E> node = new Node<E>(true, false, e, lock);
      enqueue(node);
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean add(E e) {
    return offer(e);
  }

  @Override
  public boolean tryTransfer(E e) {
    Objects.requireNonNull(e);
    lock.lock();
    try {
      if (waiterCount > 0) {
        Node<E> waiter = head;
        unlink(waiter);
        waiter.item = e;
        waiter.matched = true;
        waiter.condition.signal();
        return true;
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void transfer(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    lock.lockInterruptibly();
    try {
      if (waiterCount > 0) {
        Node<E> waiter = head;
        unlink(waiter);
        waiter.item = e;
        waiter.matched = true;
        waiter.condition.signal();
        return;
      }
      Node<E> node = new Node<E>(true, true, e, lock);
      enqueue(node);
      try {
        while (!node.matched && !node.cancelled) {
          node.condition.await();
        }
      } catch (InterruptedException ie) {
        if (!node.matched) {
          unlink(node);
          node.cancelled = true;
          throw ie;
        }
        Thread.currentThread().interrupt();
        return;
      }
      if (node.cancelled && !node.matched) {
        throw new InterruptedException();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(e);
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      if (waiterCount > 0) {
        Node<E> waiter = head;
        unlink(waiter);
        waiter.item = e;
        waiter.matched = true;
        waiter.condition.signal();
        return true;
      }
      if (nanos <= 0) {
        return false;
      }
      Node<E> node = new Node<E>(true, true, e, lock);
      enqueue(node);
      try {
        while (!node.matched && !node.cancelled) {
          if (nanos <= 0) {
            if (!node.matched) {
              unlink(node);
              node.cancelled = true;
            }
            return node.matched;
          }
          nanos = node.condition.awaitNanos(nanos);
        }
        return node.matched;
      } catch (InterruptedException ie) {
        if (!node.matched) {
          unlink(node);
          node.cancelled = true;
          throw ie;
        }
        Thread.currentThread().interrupt();
        return true;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E take() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      if (dataCount > 0) {
        Node<E> node = head;
        unlink(node);
        E item = node.item;
        if (node.isTransfer) {
          node.matched = true;
          node.condition.signal();
        }
        return item;
      }
      Node<E> waiter = new Node<E>(false, false, null, lock);
      enqueue(waiter);
      try {
        while (!waiter.matched && !waiter.cancelled) {
          waiter.condition.await();
        }
      } catch (InterruptedException ie) {
        if (!waiter.matched) {
          unlink(waiter);
          waiter.cancelled = true;
          throw ie;
        }
        Thread.currentThread().interrupt();
        return waiter.item;
      }
      return waiter.item;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      if (dataCount > 0) {
        Node<E> node = head;
        unlink(node);
        E item = node.item;
        if (node.isTransfer) {
          node.matched = true;
          node.condition.signal();
        }
        return item;
      }
      if (nanos <= 0) {
        return null;
      }
      Node<E> waiter = new Node<E>(false, false, null, lock);
      enqueue(waiter);
      try {
        while (!waiter.matched && !waiter.cancelled) {
          if (nanos <= 0) {
            if (!waiter.matched) {
              unlink(waiter);
              waiter.cancelled = true;
            }
            return waiter.item;
          }
          nanos = waiter.condition.awaitNanos(nanos);
        }
        return waiter.item;
      } catch (InterruptedException ie) {
        if (!waiter.matched) {
          unlink(waiter);
          waiter.cancelled = true;
          throw ie;
        }
        Thread.currentThread().interrupt();
        return waiter.item;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E poll() {
    lock.lock();
    try {
      if (dataCount > 0) {
        Node<E> node = head;
        unlink(node);
        E item = node.item;
        if (node.isTransfer) {
          node.matched = true;
          node.condition.signal();
        }
        return item;
      }
      return null;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E peek() {
    lock.lock();
    try {
      if (dataCount > 0) {
        return head.item;
      }
      return null;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean hasWaitingConsumer() {
    lock.lock();
    try {
      return waiterCount > 0;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int getWaitingConsumerCount() {
    lock.lock();
    try {
      return waiterCount;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return dataCount;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    lock.lock();
    try {
      return dataCount == 0;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int remainingCapacity() {
    return Integer.MAX_VALUE;
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
      while (dataCount > 0 && n < maxElements) {
        Node<E> node = head;
        unlink(node);
        c.add(node.item);
        if (node.isTransfer) {
          node.matched = true;
          node.condition.signal();
        }
        n++;
      }
      return n;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean remove(Object o) {
    if (o == null) {
      return false;
    }
    lock.lock();
    try {
      for (Node<E> p = head; p != null; p = p.next) {
        if (p.isData && o.equals(p.item)) {
          unlink(p);
          if (p.isTransfer) {
            p.cancelled = true;
            p.condition.signal();
          }
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean contains(Object o) {
    if (o == null) {
      return false;
    }
    lock.lock();
    try {
      for (Node<E> p = head; p != null; p = p.next) {
        if (p.isData && o.equals(p.item)) {
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      while (dataCount > 0) {
        Node<E> node = head;
        unlink(node);
        if (node.isTransfer) {
          node.cancelled = true;
          node.condition.signal();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Iterator<E> iterator() {
    lock.lock();
    try {
      final ArrayList<E> list = new ArrayList<E>(dataCount);
      for (Node<E> p = head; p != null; p = p.next) {
        if (p.isData) {
          list.add(p.item);
        }
      }
      return new Iterator<E>() {
        private int cursor = 0;
        private E lastRet = null;

        @Override
        public boolean hasNext() {
          return cursor < list.size();
        }

        @Override
        public E next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          lastRet = list.get(cursor++);
          return lastRet;
        }

        @Override
        public void remove() {
          if (lastRet == null) {
            throw new IllegalStateException();
          }
          LinkedTransferQueue.this.remove(lastRet);
          lastRet = null;
        }
      };
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object[] toArray() {
    lock.lock();
    try {
      Object[] a = new Object[dataCount];
      int i = 0;
      for (Node<E> p = head; p != null; p = p.next) {
        if (p.isData) {
          a[i++] = p.item;
        }
      }
      return a;
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    Objects.requireNonNull(a);
    lock.lock();
    try {
      int size = dataCount;
      if (a.length < size) {
        a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
      }
      int i = 0;
      for (Node<E> p = head; p != null; p = p.next) {
        if (p.isData) {
          a[i++] = (T) p.item;
        }
      }
      if (a.length > size) {
        a[size] = null;
      }
      return a;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    lock.lock();
    try {
      for (Node<E> p = head; p != null; p = p.next) {
        if (p.isData) {
          action.accept(p.item);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    boolean removed = false;
    lock.lock();
    try {
      Node<E> p = head;
      while (p != null) {
        Node<E> next = p.next;
        if (p.isData && filter.test(p.item)) {
          unlink(p);
          if (p.isTransfer) {
            p.cancelled = true;
            p.condition.signal();
          }
          removed = true;
        }
        p = next;
      }
      return removed;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT);
  }
}
