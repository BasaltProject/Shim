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

public abstract class AbstractQueue<T> extends AbstractCollection<T> implements Queue<T> {
  
  protected AbstractQueue() {
    super();
  }
  
  @Override
  public boolean add(T element) {
    if (offer(element)) {
      return true;
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public boolean addAll(Collection <? extends T> collection) {
    if (collection == null) {
      throw new NullPointerException();
    }

    for (T element : collection) {
      add(element);
    }

    return true;
  }

  @Override
  public void clear() {
    while (size() > 0) {
      poll();
    }
  }

  @Override
  public T element() {
    T result = peek();
    if (result == null) {
      throw new NoSuchElementException();
    } else {
      return result;
    }
  }

  @Override
  public T remove() {
    T result = poll();
    if (result == null) {
      throw new NoSuchElementException();
    } else {
      return result;
    }
  }
}
