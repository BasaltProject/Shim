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

import java.util.function.UnaryOperator;

public interface List<T> extends Collection<T>, SequencedCollection<T> {
  public T get(int index);

  public T set(int index, T value);

  public T remove(int index);

  public boolean add(T element);

  public void add(int index, T element);

  public boolean addAll(int startIndex, Collection<? extends T> c);

  public int indexOf(Object value);

  public int lastIndexOf(Object value);

  public boolean isEmpty();

  public ListIterator<T> listIterator(int index);

  public ListIterator<T> listIterator();

  default List<T> subList(int fromIndex, int toIndex) {
    Objects.checkFromToIndex(fromIndex, toIndex, size());
    int len = toIndex - fromIndex;
    ArrayList<T> sub = new ArrayList<T>(len);
    for (int i = fromIndex; i < toIndex; ++i) {
      sub.add(get(i));
    }
    return sub;
  }

  default void sort(Comparator<? super T> c) {
    Object[] a = toArray();
    Arrays.sort(a, (Comparator) c);
    ListIterator<T> i = listIterator();
    for (Object e : a) {
      i.next();
      i.set((T) e);
    }
  }

  default void replaceAll(UnaryOperator<T> operator) {
    Objects.requireNonNull(operator);
    ListIterator<T> li = listIterator();
    while (li.hasNext()) {
      li.set(operator.apply(li.next()));
    }
  }

  @Override
  default void addFirst(T element) {
    add(0, element);
  }

  @Override
  default void addLast(T element) {
    add(element);
  }

  @Override
  default T getFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return get(0);
  }

  @Override
  default T getLast() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return get(size() - 1);
  }

  @Override
  default T removeFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return remove(0);
  }

  @Override
  default T removeLast() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return remove(size() - 1);
  }

  @Override
  default List<T> reversed() {
    return new ImmutableCollections.ReverseOrderListView<T>(this);
  }

  static <E> List<E> of() {
    return ImmutableCollections.emptyList();
  }

  static <E> List<E> of(E e1) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1 });
  }

  static <E> List<E> of(E e1, E e2) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2 });
  }

  static <E> List<E> of(E e1, E e2, E e3) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4, E e5) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4, e5 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4, e5, e6 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7, e8 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7, e8, e9 });
  }

  static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
    return new ImmutableCollections.ListN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10 });
  }

  static <E> List<E> of(E... elements) {
    if (elements.length == 0) {
      return ImmutableCollections.emptyList();
    }
    return new ImmutableCollections.ListN<E>(elements);
  }

  @SuppressWarnings("unchecked")
  static <E> List<E> copyOf(Collection<? extends E> coll) {
    if (coll instanceof ImmutableCollections.ListN) {
      return (List<E>) coll;
    }
    return of((E[]) coll.toArray());
  }
}
