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

public interface ListIterator<E> extends Iterator<E> {
  public boolean hasPrevious();

  public E previous();

  default int nextIndex() {
    throw new UnsupportedOperationException();
  }

  default int previousIndex() {
    throw new UnsupportedOperationException();
  }

  default void remove() {
    throw new UnsupportedOperationException();
  }

  default void set(E e) {
    throw new UnsupportedOperationException();
  }

  default void add(E e) {
    throw new UnsupportedOperationException();
  }
}
