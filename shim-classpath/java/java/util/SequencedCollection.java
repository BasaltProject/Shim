/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public interface SequencedCollection<E> extends Collection<E> {
  SequencedCollection<E> reversed();

  default void addFirst(E element) {
    throw new UnsupportedOperationException();
  }

  default void addLast(E element) {
    throw new UnsupportedOperationException();
  }

  default E getFirst() {
    return iterator().next();
  }

  default E getLast() {
    return reversed().iterator().next();
  }

  default E removeFirst() {
    Iterator<E> it = iterator();
    E element = it.next();
    it.remove();
    return element;
  }

  default E removeLast() {
    Iterator<E> it = reversed().iterator();
    E element = it.next();
    it.remove();
    return element;
  }
}
