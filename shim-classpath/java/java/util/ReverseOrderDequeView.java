/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

final class ReverseOrderDequeView<E> implements Deque<E> {
  final Deque<E> base;

  private ReverseOrderDequeView(Deque<E> deque) {
    base = deque;
  }

  public static <T> Deque<T> of(Deque<T> deque) {
    if (deque instanceof ReverseOrderDequeView) {
      return ((ReverseOrderDequeView<T>) deque).base;
    } else {
      return new ReverseOrderDequeView<>(deque);
    }
  }

  public Iterator<E> iterator() {
    return base.descendingIterator();
  }

  public Iterator<E> descendingIterator() {
    return base.iterator();
  }

  public Deque<E> reversed() {
    return base;
  }

  public void addFirst(E e) {
    base.addLast(e);
  }

  public void addLast(E e) {
    base.addFirst(e);
  }

  public boolean offerFirst(E e) {
    return base.offerLast(e);
  }

  public boolean offerLast(E e) {
    return base.offerFirst(e);
  }

  public E removeFirst() {
    return base.removeLast();
  }

  public E removeLast() {
    return base.removeFirst();
  }

  public E pollFirst() {
    return base.pollLast();
  }

  public E pollLast() {
    return base.pollFirst();
  }

  public E getFirst() {
    return base.getLast();
  }

  public E getLast() {
    return base.getFirst();
  }

  public E peekFirst() {
    return base.peekLast();
  }

  public E peekLast() {
    return base.peekFirst();
  }

  public boolean removeFirstOccurrence(Object o) {
    return base.removeLastOccurrence(o);
  }

  public boolean removeLastOccurrence(Object o) {
    return base.removeFirstOccurrence(o);
  }

  public boolean add(E e) {
    base.addFirst(e);
    return true;
  }

  public boolean offer(E e) {
    return base.offerFirst(e);
  }

  public E remove() {
    return base.removeLast();
  }

  public E poll() {
    return base.pollLast();
  }

  public E element() {
    return base.getLast();
  }

  public E peek() {
    return base.peekLast();
  }

  public void push(E e) {
    base.addLast(e);
  }

  public E pop() {
    return base.removeLast();
  }

  public boolean remove(Object o) {
    return base.removeLastOccurrence(o);
  }

  public boolean contains(Object o) {
    return base.contains(o);
  }

  public int size() {
    return base.size();
  }

  public boolean isEmpty() {
    return base.isEmpty();
  }

  public void clear() {
    base.clear();
  }

  public boolean containsAll(Collection<?> c) {
    return base.containsAll(c);
  }

  public boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    for (E e : c) {
      base.addFirst(e);
      modified = true;
    }
    return modified;
  }

  public boolean removeAll(Collection<?> c) {
    return base.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return base.retainAll(c);
  }

  public boolean removeIf(Predicate<? super E> filter) {
    return base.removeIf(filter);
  }

  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    for (Iterator<E> it = iterator(); it.hasNext();) {
      action.accept(it.next());
    }
  }

  public Object[] toArray() {
    Object[] a = base.toArray();
    for (int i = 0, j = a.length - 1; i < j; i++, j--) {
      Object t = a[i];
      a[i] = a[j];
      a[j] = t;
    }
    return a;
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    Object[] reversed = toArray();
    if (a.length < reversed.length) {
      a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), reversed.length);
    }
    System.arraycopy(reversed, 0, a, 0, reversed.length);
    if (a.length > reversed.length) {
      a[reversed.length] = null;
    }
    return a;
  }

  public String toString() {
    Iterator<E> it = iterator();
    if (!it.hasNext()) {
      return "[]";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (;;) {
      E e = it.next();
      sb.append(e == this ? "(this Collection)" : e);
      if (!it.hasNext()) {
        return sb.append(']').toString();
      }
      sb.append(',').append(' ');
    }
  }
}
