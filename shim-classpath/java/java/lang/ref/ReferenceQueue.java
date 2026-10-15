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

package java.lang.ref;

public class ReferenceQueue<T> {
  private Reference<? extends T> front;

  public synchronized Reference<? extends T> poll() {
    if (front == null) {
      return null;
    }
    Reference<? extends T> r = front;
    if (front == front.jNext) {
      front = null;
    } else {
      front = front.jNext;
    }
    r.jNext = null;
    return r;
  }

  public synchronized Reference<? extends T> remove(long timeout)
      throws IllegalArgumentException, InterruptedException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Negative timeout value");
    }
    if (front != null) {
      return poll();
    }
    if (timeout == 0) {
      while (front == null) {
        wait(50);
      }
      return poll();
    }
    long start = System.currentTimeMillis();
    while (front == null) {
      long remaining = timeout - (System.currentTimeMillis() - start);
      if (remaining <= 0) {
        break;
      }
      wait(Math.min(remaining, 50));
    }
    return poll();
  }

  public Reference<? extends T> remove() throws InterruptedException {
    return remove(0);
  }

  synchronized void add(Reference<? extends T> r) {
    if (front == null) {
      r.jNext = r;
    } else {
      r.jNext = front;
    }
    front = r;
    notifyAll();
  }
}
