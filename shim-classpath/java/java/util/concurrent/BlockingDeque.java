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

package java.util.concurrent;

import java.util.Deque;
import java.util.Iterator;

public interface BlockingDeque<T> extends Deque<T>, BlockingQueue<T> {
  void addFirst(T e);

  void addLast(T e);

  boolean offerFirst(T e);

  boolean offerLast(T e);

  void putFirst(T e) throws InterruptedException;

  void putLast(T e) throws InterruptedException;

  boolean offerFirst(T e, long timeout, TimeUnit unit) throws InterruptedException;

  boolean offerLast(T e, long timeout, TimeUnit unit) throws InterruptedException;

  T takeFirst() throws InterruptedException;

  T takeLast() throws InterruptedException;

  T pollFirst(long timeout, TimeUnit unit) throws InterruptedException;

  T pollLast(long timeout, TimeUnit unit) throws InterruptedException;

  boolean removeFirstOccurrence(Object o);

  boolean removeLastOccurrence(Object o);

  @Override
  boolean add(T e);

  @Override
  boolean offer(T e);

  @Override
  void put(T e) throws InterruptedException;

  @Override
  boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException;

  @Override
  T remove();

  @Override
  T poll();

  @Override
  T take() throws InterruptedException;

  @Override
  T poll(long timeout, TimeUnit unit) throws InterruptedException;

  @Override
  T element();

  @Override
  T peek();

  @Override
  boolean remove(Object o);

  @Override
  boolean contains(Object o);

  @Override
  int size();

  @Override
  Iterator<T> iterator();

  @Override
  void push(T e);
}

