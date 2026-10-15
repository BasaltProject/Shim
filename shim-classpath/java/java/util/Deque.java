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

public interface Deque<T> extends Queue<T>, SequencedCollection<T> {
  public boolean offerFirst(T e);
  public void push(T e);
  public void addFirst(T element);
  public boolean offerLast(T e);
  public void addLast(T element);
  public T peekFirst();
  public T getFirst();
  public T peekLast();
  public T getLast();
  public T pollFirst();
  public T removeFirst();
  public T pop();
  public T pollLast();
  public T removeLast();
  public Iterator<T> descendingIterator();
  public boolean removeLastOccurrence(Object o);
  public boolean removeFirstOccurrence(Object o);

  @Override
  default SequencedCollection<T> reversed() {
    return new SequencedCollection<T>() {
      @Override
      public SequencedCollection<T> reversed() {
        return Deque.this;
      }

      @Override
      public int size() {
        return Deque.this.size();
      }

      @Override
      public boolean isEmpty() {
        return Deque.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
        return Deque.this.contains(o);
      }

      @Override
      public Iterator<T> iterator() {
        return Deque.this.descendingIterator();
      }

      @Override
      public Object[] toArray() {
        Object[] a = new Object[size()];
        int i = 0;
        for (T e : this) {
          a[i++] = e;
        }
        return a;
      }

      @Override
      @SuppressWarnings("unchecked")
      public <S> S[] toArray(S[] array) {
        if (array.length < size()) {
          array = (S[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        }
        int i = 0;
        for (T e : this) {
          array[i++] = (S) e;
        }
        if (array.length > size()) {
          array[size()] = null;
        }
        return array;
      }

      @Override
      public boolean add(T element) {
        Deque.this.addFirst(element);
        return true;
      }

      @Override
      public boolean remove(Object o) {
        return Deque.this.removeLastOccurrence(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
          if (!contains(o)) return false;
        }
        return true;
      }

      @Override
      public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T e : c) {
          changed |= add(e);
        }
        return changed;
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
          changed |= remove(o);
        }
        return changed;
      }

      @Override
      public void clear() {
        Deque.this.clear();
      }

      @Override
      public void addFirst(T element) {
        Deque.this.addLast(element);
      }

      @Override
      public void addLast(T element) {
        Deque.this.addFirst(element);
      }

      @Override
      public T getFirst() {
        return Deque.this.getLast();
      }

      @Override
      public T getLast() {
        return Deque.this.getFirst();
      }

      @Override
      public T removeFirst() {
        return Deque.this.removeLast();
      }

      @Override
      public T removeLast() {
        return Deque.this.removeFirst();
      }
    };
  }
}
