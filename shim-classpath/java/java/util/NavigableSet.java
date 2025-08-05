/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public interface NavigableSet<E> extends SortedSet<E> {
  E lower(E e);

  E floor(E e);

  E ceiling(E e);

  E higher(E e);

  E pollFirst();

  E pollLast();

  @Override
  Iterator<E> iterator();

  NavigableSet<E> descendingSet();

  Iterator<E> descendingIterator();

  NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

  NavigableSet<E> headSet(E toElement, boolean inclusive);

  NavigableSet<E> tailSet(E fromElement, boolean inclusive);

  @Override
  SortedSet<E> subSet(E fromElement, E toElement);

  @Override
  SortedSet<E> headSet(E toElement);

  @Override
  SortedSet<E> tailSet(E fromElement);

  @Override
  default E removeFirst() {
    E e = pollFirst();
    if (e == null && isEmpty()) {
      throw new NoSuchElementException();
    }
    return e;
  }

  @Override
  default E removeLast() {
    E e = pollLast();
    if (e == null && isEmpty()) {
      throw new NoSuchElementException();
    }
    return e;
  }

  @Override
  default NavigableSet<E> reversed() {
    return descendingSet();
  }
}
