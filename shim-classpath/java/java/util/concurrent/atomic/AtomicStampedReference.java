/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.atomic;

public class AtomicStampedReference<V> {

  private static final class Pair<T> {
    final T reference;
    final int stamp;

    private Pair(T reference, int stamp) {
      this.reference = reference;
      this.stamp = stamp;
    }

    static <T> Pair<T> of(T reference, int stamp) {
      return new Pair<>(reference, stamp);
    }
  }

  private volatile Pair<V> pair;

  public AtomicStampedReference(V initialRef, int initialStamp) {
    this.pair = Pair.of(initialRef, initialStamp);
  }

  public V getReference() {
    return pair.reference;
  }

  public int getStamp() {
    return pair.stamp;
  }

  public V get(int[] stampHolder) {
    Pair<V> p = pair;
    if (stampHolder != null && stampHolder.length > 0) {
      stampHolder[0] = p.stamp;
    }
    return p.reference;
  }

  public boolean weakCompareAndSet(
      V expectedReference, V newReference, int expectedStamp, int newStamp) {
    return compareAndSet(expectedReference, newReference, expectedStamp, newStamp);
  }

  public synchronized boolean compareAndSet(
      V expectedReference, V newReference, int expectedStamp, int newStamp) {
    Pair<V> current = pair;
    if (expectedReference == current.reference && expectedStamp == current.stamp) {
      if (newReference == current.reference && newStamp == current.stamp) {
        return true;
      }
      pair = Pair.of(newReference, newStamp);
      return true;
    }
    return false;
  }

  public synchronized void set(V newReference, int newStamp) {
    Pair<V> current = pair;
    if (newReference != current.reference || newStamp != current.stamp) {
      pair = Pair.of(newReference, newStamp);
    }
  }

  public synchronized boolean attemptStamp(V expectedReference, int newStamp) {
    Pair<V> current = pair;
    if (expectedReference != current.reference) {
      return false;
    }
    if (newStamp != current.stamp) {
      pair = Pair.of(expectedReference, newStamp);
    }
    return true;
  }
}
