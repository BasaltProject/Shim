/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.atomic;

public class AtomicMarkableReference<V> {

  private static final class Pair<T> {
    final T reference;
    final boolean mark;

    private Pair(T reference, boolean mark) {
      this.reference = reference;
      this.mark = mark;
    }

    static <T> Pair<T> of(T reference, boolean mark) {
      return new Pair<>(reference, mark);
    }
  }

  private volatile Pair<V> pair;

  public AtomicMarkableReference(V initialRef, boolean initialMark) {
    this.pair = Pair.of(initialRef, initialMark);
  }

  public V getReference() {
    return pair.reference;
  }

  public boolean isMarked() {
    return pair.mark;
  }

  public V get(boolean[] markHolder) {
    Pair<V> p = pair;
    if (markHolder != null && markHolder.length > 0) {
      markHolder[0] = p.mark;
    }
    return p.reference;
  }

  public boolean weakCompareAndSet(
      V expectedReference, V newReference, boolean expectedMark, boolean newMark) {
    return compareAndSet(expectedReference, newReference, expectedMark, newMark);
  }

  public synchronized boolean compareAndSet(
      V expectedReference, V newReference, boolean expectedMark, boolean newMark) {
    Pair<V> current = pair;
    if (expectedReference == current.reference && expectedMark == current.mark) {
      if (newReference == current.reference && newMark == current.mark) {
        return true;
      }
      pair = Pair.of(newReference, newMark);
      return true;
    }
    return false;
  }

  public synchronized void set(V newReference, boolean newMark) {
    Pair<V> current = pair;
    if (newReference != current.reference || newMark != current.mark) {
      pair = Pair.of(newReference, newMark);
    }
  }

  public synchronized boolean attemptMark(V expectedReference, boolean newMark) {
    Pair<V> current = pair;
    if (expectedReference != current.reference) {
      return false;
    }
    if (newMark != current.mark) {
      pair = Pair.of(expectedReference, newMark);
    }
    return true;
  }
}
