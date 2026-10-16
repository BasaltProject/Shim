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
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentLinkedDeque<E> extends AbstractCollection<E>
    implements Deque<E>, Serializable {
  private static final long serialVersionUID = 876323262645176354L;


  static final class Node<E> {
    volatile E item;
    Node<E> prev;
    Node<E> next;

    Node(E x) {
      item = x;
    }
  }

  private Node<E> head;
  private Node<E> tail;
  private int count;
  private final ReentrantLock lock = new ReentrantLock();

  public ConcurrentLinkedDeque() {
  }

  public ConcurrentLinkedDeque(Collection<? extends E> c) {
    this();
    addAll(c);
  }

  private void linkFirst(E e) {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    lock.lock();
    try {
      Node<E> h = head;
      node.next = h;
      head = node;
      if (tail == null) {
        tail = node;
      } else {
        h.prev = node;
      }
      ++count;
    } finally {
      lock.unlock();
    }
  }

  private void linkLast(E e) {
    Objects.requireNonNull(e);
    Node<E> node = new Node<E>(e);
    lock.lock();
    try {
      Node<E> t = tail;
      node.prev = t;
      tail = node;
      if (head == null) {
        head = node;
      } else {
        t.next = node;
      }
      ++count;
    } finally {
      lock.unlock();
    }
  }

  private E unlinkFirst() {
    lock.lock();
    try {
      Node<E> h = head;
      if (h == null) {
        return null;
      }
      E item = h.item;
      h.item = null;
      Node<E> n = h.next;
      head = n;
      if (n == null) {
        tail = null;
      } else {
        n.prev = null;
      }
      --count;
      return item;
    } finally {
      lock.unlock();
    }
  }

  private E unlinkLast() {
    lock.lock();
    try {
      Node<E> t = tail;
      if (t == null) {
        return null;
      }
      E item = t.item;
      t.item = null;
      Node<E> p = t.prev;
      tail = p;
      if (p == null) {
        head = null;
      } else {
        p.next = null;
      }
      --count;
      return item;
    } finally {
      lock.unlock();
    }
  }

  private void unlink(Node<E> x) {
    Node<E> p = x.prev;
    Node<E> n = x.next;
    if (p == null) {
      head = n;
    } else {
      p.next = n;
      x.prev = null;
    }
    if (n == null) {
      tail = p;
    } else {
      n.prev = p;
      x.next = null;
    }
    x.item = null;
    --count;
  }

  @Override
  public void addFirst(E e) {
    linkFirst(e);
  }

  @Override
  public void addLast(E e) {
    linkLast(e);
  }

  @Override
  public boolean offerFirst(E e) {
    linkFirst(e);
    return true;
  }

  @Override
  public boolean offerLast(E e) {
    linkLast(e);
    return true;
  }

  @Override
  public E peekFirst() {
    lock.lock();
    try {
      return (head == null) ? null : head.item;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public E peekLast() {
    lock.lock();
    try {
      return (tail == null) ? null : tail.item;
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
  public E pollFirst() {
    return unlinkFirst();
  }

  @Override
  public E pollLast() {
    return unlinkLast();
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
  public boolean offer(E e) {
    return offerLast(e);
  }

  @Override
  public boolean add(E e) {
    addLast(e);
    return true;
  }

  @Override
  public E poll() {
    return pollFirst();
  }

  @Override
  public E peek() {
    return peekFirst();
  }

  @Override
  public E remove() {
    return removeFirst();
  }

  @Override
  public E element() {
    return getFirst();
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
  public boolean removeFirstOccurrence(Object o) {
    if (o == null) {
      return false;
    }
    lock.lock();
    try {
      for (Node<E> p = head; p != null; p = p.next) {
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
      for (Node<E> p = tail; p != null; p = p.prev) {
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
  public boolean contains(Object o) {
    if (o == null) {
      return false;
    }
    lock.lock();
    try {
      for (Node<E> p = head; p != null; p = p.next) {
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
  public boolean isEmpty() {
    return peekFirst() == null;
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
  public boolean remove(Object o) {
    return removeFirstOccurrence(o);
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      for (Node<E> p = head; p != null; ) {
        Node<E> n = p.next;
        p.item = null;
        p.prev = null;
        p.next = null;
        p = n;
      }
      head = tail = null;
      count = 0;
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
      for (Node<E> p = head; p != null; p = p.next) {
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
      for (Node<E> p = head; p != null; p = p.next) {
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
      return head;
    }

    @Override
    Node<E> nextNode(Node<E> n) {
      return n.next;
    }
  }

  private class DescendingItr extends AbstractItr {
    @Override
    Node<E> firstNode() {
      return tail;
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
