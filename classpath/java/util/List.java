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

public interface List<T> extends Collection<T> {
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
}
