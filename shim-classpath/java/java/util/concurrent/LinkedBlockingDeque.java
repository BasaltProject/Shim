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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LinkedBlockingDeque<E> extends AbstractQueue<E>
    implements BlockingDeque<E>, Serializable {

  static final class Node<E> {
    E item;
    Node<E> prev;
    Node<E> next;

    Node(E x) {
      item = x;
    }
  }

  private final int capacity;
  private int count;
  private Node<E> first;
  private Node<E> last;

  private final ReentrantLock lock = new ReentrantLock();
  private final Condition notEmpty = lock.newCondition();
  private final Condition notFull = lock.newCondition();

  public LinkedBlockingDeque() {
    this(Integer.MAX_VALUE);
  }

  public LinkedBlockingDeque(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException();
    }
    this.capacity = capacity;
  }

  public LinkedBlockingDeque(Collection<? extends E> c) {
    this(Integer.MAX_VALUE);
    addAll(c);
  }

  private boolean linkFirst(Node<E> node) {
    if (count >= capacity) {
      return false;
    }
    Node<E> f = first;
    node.next = f;
    first = node;
    if (last == null) {
      last = node;
    } else {
      f.prev = node;
    }
    ++count;
    notEmpty.signal();
    return true;
  }

  private boolean linkLast(Node<E> node) {
    if (count >= capacity) {
      return false;
    }
    Node<E> l = last;
    node.prev = l;
    last = node;
    if (first == null) {
      first = node;
    } else {
      l.next = node;
    }
    ++count;
    notEmpty.signal();
    return true;
  }

  private E unlinkFirst() {
    Node<E> f = first;
    if (f == null) {
      return null;
    }
    Node<E> n = f.next;
    E item = f.item;
    f.item = null;
    f.next = f;
    first = n;
    if (n == null) {
      last = null;
    } else {
      n.prev = null;
    }
    --count;
    notFull.signal();
    return item;
  }

  private E unlinkLast() {
    Node<E> l = last;
    if (l == null) {
      return null;
    }
    Node<E> p = l.prev;
    E item = l.item;
    l.item = null;
    l.prev = l;
    last = p;
    if (p == null) {
      first = null;
    } else {
      p.next = null;
    }
    --count;
    notFull.signal();
    return item;
  }

  private void unlink(Node<E> x) {
    Node<E> p = x.prev;
    Node<E> n = x.next;
    if (p == null) {
      unlinkFirst();
    } else if (n == null) {
      unlinkLast();
    } else {
      p.next = n;
      n.prev = p;
      x.item = null;
      --count;
      notFull.signal();
    }
  }

  @Override
  public void addFirst(E e) {
    if (!offerFirst(e)) {
      throw new IllegalStateException("Deque full");
    }
  }

  @Override
  public void addLast(E e) {
    if (!offerLast(e)) {
      throw new IllegalStateException("Deque full");
    }
  }

  @Override
  public boolean offerFirst(E e) {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    lock.lock();
    try {
      return linkFirst(node);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offerLast(E e) {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    lock.lock();
    try {
      return linkLast(node);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void putFirst(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    lock.lock();
    try {
      while (!linkFirst(node)) {
        notFull.await();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void putLast(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    lock.lock();
    try {
      while (!linkLast(node)) {
        notFull.await();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      while (!linkFirst(node)) {
        if (nanos <= 0) {
          return false;
        }
        nanos = notFull.awaitNanos(nanos);
      }
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      while (!linkLast(node)) {
        if (nanos <= 0) {
          return false;
        }
        nanos = notFull.awaitNanos(nanos);
      }
      return true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E removeFirst() {
    E x = pollFirst();
    if (x == null) {
      throw new NoSuchElementException();
    }
    return x;
  }

  @Override
  public E removeLast() {
    E x = pollLast();
    if (x == null) {
      throw new NoSuchElementException();
    }
    return x;
  }

  @Override
  public E pollFirst() {
    lock.lock();
    try {
      return unlinkFirst();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E pollLast() {
    lock.lock();
    try {
      return unlinkLast();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E takeFirst() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      E x;
      while ((x = unlinkFirst()) == null) {
        notEmpty.await();
      }
      return x;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E takeLast() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      E x;
      while ((x = unlinkLast()) == null) {
        notEmpty.await();
      }
      return x;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      E x;
      while ((x = unlinkFirst()) == null) {
        if (nanos <= 0) {
          return null;
        }
        nanos = notEmpty.awaitNanos(nanos);
      }
      return x;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lockInterruptibly();
    try {
      E x;
      while ((x = unlinkLast()) == null) {
        if (nanos <= 0) {
          return null;
        }
        nanos = notEmpty.awaitNanos(nanos);
      }
      return x;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E getFirst() {
    E x = peekFirst();
    if (x == null) {
      throw new NoSuchElementException();
    }
    return x;
  }

  @Override
  public E getLast() {
    E x = peekLast();
    if (x == null) {
      throw new NoSuchElementException();
    }
    return x;
  }

  @Override
  public E peekFirst() {
    lock.lock();
    try {
      return first == null ? null : first.item;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E peekLast() {
    lock.lock();
    try {
      return last == null ? null : last.item;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean removeFirstOccurrence(Object o) {
    if (o == null) {
      return false;
    }
    lock.lock();
    try {
      for (Node<E> p = first; p != null; p = p.next) {
        if (o.equals(p.item)) {
          unlink(p);
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean removeLastOccurrence(Object o) {
    if (o == null) {
      return false;
    }
    lock.lock();
    try {
      for (Node<E> p = last; p != null; p = p.prev) {
        if (o.equals(p.item)) {
          unlink(p);
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean add(E e) {
    addLast(e);
    return true;
  }

  @Override
  public boolean offer(E e) {
    return offerLast(e);
  }

  @Override
  public void put(E e) throws InterruptedException {
    putLast(e);
  }

  @Override
  public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    return offerLast(e, timeout, unit);
  }

  @Override
  public E remove() {
    return removeFirst();
  }

  @Override
  public E poll() {
    return pollFirst();
  }

  @Override
  public E take() throws InterruptedException {
    return takeFirst();
  }

  @Override
  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    return pollFirst(timeout, unit);
  }

  @Override
  public E element() {
    return getFirst();
  }

  @Override
  public E peek() {
    return peekFirst();
  }

  @Override
  public int remainingCapacity() {
    lock.lock();
    try {
      return capacity - count;
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
      int n = Math.min(maxElements, count);
      for (int i = 0; i < n; i++) {
        c.add(unlinkFirst());
      }
      return n;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void push(E e) {
    addFirst(e);
  }

  @Override
  public E pop() {
    return removeFirst();
  }

  @Override
  public boolean remove(Object o) {
    return removeFirstOccurrence(o);
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return count;
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
      for (Node<E> p = first; p != null; p = p.next) {
        if (o.equals(p.item)) {
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object[] toArray() {
    lock.lock();
    try {
      Object[] a = new Object[count];
      int k = 0;
      for (Node<E> p = first; p != null; p = p.next) {
        a[k++] = p.item;
      }
      return a;
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    lock.lock();
    try {
      if (a.length < count) {
        a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), count);
      }
      int k = 0;
      for (Node<E> p = first; p != null; p = p.next) {
        a[k++] = (T) p.item;
      }
      if (a.length > count) {
        a[count] = null;
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
      for (Node<E> f = first; f != null; ) {
        Node<E> n = f.next;
        f.item = null;
        f.prev = null;
        f.next = null;
        f = n;
      }
      first = last = null;
      count = 0;
      notFull.signalAll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new Itr();
  }

  @Override
  public Iterator<E> descendingIterator() {
    return new DescendingItr();
  }

  private abstract class AbstractItr implements Iterator<E> {
    Node<E> next;
    E nextItem;
    private Node<E> lastRet;

    abstract Node<E> firstNode();
    abstract Node<E> nextNode(Node<E> n);

    AbstractItr() {
      lock.lock();
      try {
        next = firstNode();
        nextItem = (next == null) ? null : next.item;
      } finally {
        lock.unlock();
      }
    }

    private Node<E> succ(Node<E> n) {
      while (true) {
        Node<E> s = nextNode(n);
        if (s == null) {
          return null;
        } else if (s.item != null) {
          return s;
        } else if (s == n) {
          return firstNode();
        } else {
          n = s;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public E next() {
      if (next == null) {
        throw new NoSuchElementException();
      }
      lastRet = next;
      E x = nextItem;
      lock.lock();
      try {
        next = succ(next);
        nextItem = (next == null) ? null : next.item;
      } finally {
        lock.unlock();
      }
      return x;
    }

    @Override
    public void remove() {
      Node<E> n = lastRet;
      if (n == null) {
        throw new IllegalStateException();
      }
      lastRet = null;
      lock.lock();
      try {
        if (n.item != null) {
          unlink(n);
        }
      } finally {
        lock.unlock();
      }
    }
  }

  private class Itr extends AbstractItr {
    @Override
    Node<E> firstNode() {
      return first;
    }

    @Override
    Node<E> nextNode(Node<E> n) {
      return n.next;
    }
  }

  private class DescendingItr extends AbstractItr {
    @Override
    Node<E> firstNode() {
      return last;
    }

    @Override
    Node<E> nextNode(Node<E> n) {
      return n.prev;
    }
  }

  @Override
  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT);
  }
}
