/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.util.Objects;

public class Exchanger<V> {
  private final Object lock = new Object();
  private Node currentSlot;

  private static class Node {
    final Thread thread;
    final Object item;
    Object match;
    boolean done;

    Node(Thread thread, Object item) {
      this.thread = thread;
      this.item = item;
    }
  }

  public Exchanger() {
  }

  @SuppressWarnings("unchecked")
  public V exchange(V x) throws InterruptedException {
    try {
      return exchange(x, 0L, null);
    } catch (TimeoutException toe) {
      throw new Error(toe);
    }
  }

  @SuppressWarnings("unchecked")
  public V exchange(V x, long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException {
    boolean timed = unit != null;
    long nanos = timed ? unit.toNanos(timeout) : 0L;
    long deadline = timed ? System.nanoTime() + nanos : 0L;

    synchronized (lock) {
      if (currentSlot == null) {
        Node myNode = new Node(Thread.currentThread(), x);
        currentSlot = myNode;

        while (!myNode.done) {
          if (Thread.interrupted()) {
            if (currentSlot == myNode) {
              currentSlot = null;
            }
            throw new InterruptedException();
          }

          if (timed) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0L) {
              if (currentSlot == myNode) {
                currentSlot = null;
              }
              throw new TimeoutException();
            }
            long millis = remaining / 1_000_000L;
            int remNanos = (int) (remaining % 1_000_000L);
            lock.wait(millis, remNanos);
          } else {
            lock.wait();
          }
        }

        return (V) myNode.match;
      } else {
        Node partner = currentSlot;
        currentSlot = null;
        partner.match = x;
        partner.done = true;
        lock.notifyAll();
        return (V) partner.item;
      }
    }
  }
}
