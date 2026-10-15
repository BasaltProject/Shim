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

package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import sun.misc.Unsafe;

public class AtomicReferenceArray<E> implements Serializable {
  private static final long serialVersionUID = -6209656149925076980L;

  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long arrayOffset = unsafe.arrayBaseOffset(Object[].class);
  private static final long arrayScale = unsafe.arrayIndexScale(Object[].class);

  private final Object[] array;

  public AtomicReferenceArray(int length) {
    this.array = new Object[length];
  }

  public AtomicReferenceArray(E[] array) {
    this.array = Arrays.copyOf(array, array.length, Object[].class);
  }

  public int length() {
    return array.length;
  }

  private long offset(int i) {
    if (i < 0 || i >= array.length) {
      throw new IndexOutOfBoundsException("index " + i);
    }
    return arrayOffset + ((long) i * arrayScale);
  }

  @SuppressWarnings("unchecked")
  public E get(int i) {
    return (E) unsafe.getObjectVolatile(array, offset(i));
  }

  public void set(int i, E newValue) {
    unsafe.putObjectVolatile(array, offset(i), newValue);
  }

  public void lazySet(int i, E newValue) {
    unsafe.putOrderedObject(array, offset(i), newValue);
  }

  @SuppressWarnings("unchecked")
  public E getAndSet(int i, E newValue) {
    long off = offset(i);
    while (true) {
      E current = (E) unsafe.getObjectVolatile(array, off);
      if (unsafe.compareAndSwapObject(array, off, current, newValue)) {
        return current;
      }
    }
  }

  public boolean compareAndSet(int i, E expectedValue, E newValue) {
    return unsafe.compareAndSwapObject(array, offset(i), expectedValue, newValue);
  }

  public boolean weakCompareAndSet(int i, E expectedValue, E newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(int i, E expectedValue, E newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(int i, E expectedValue, E newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(int i, E expectedValue, E newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(int i, E expectedValue, E newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  @SuppressWarnings("unchecked")
  public E compareAndExchange(int i, E expectedValue, E newValue) {
    long off = offset(i);
    while (true) {
      E witness = (E) unsafe.getObjectVolatile(array, off);
      if (witness == expectedValue || (witness != null && witness.equals(expectedValue))) {
        if (unsafe.compareAndSwapObject(array, off, witness, newValue)) {
          return witness;
        }
      } else {
        return witness;
      }
    }
  }

  public E compareAndExchangeAcquire(int i, E expectedValue, E newValue) {
    return compareAndExchange(i, expectedValue, newValue);
  }

  public E compareAndExchangeRelease(int i, E expectedValue, E newValue) {
    return compareAndExchange(i, expectedValue, newValue);
  }

  public E getPlain(int i) {
    return get(i);
  }

  public void setPlain(int i, E newValue) {
    set(i, newValue);
  }

  public E getOpaque(int i) {
    return get(i);
  }

  public void setOpaque(int i, E newValue) {
    set(i, newValue);
  }

  public E getAcquire(int i) {
    return get(i);
  }

  public void setRelease(int i, E newValue) {
    set(i, newValue);
  }

  public final E getAndUpdate(int i, UnaryOperator<E> updateFunction) {
    Objects.requireNonNull(updateFunction);
    long off = offset(i);
    while (true) {
      @SuppressWarnings("unchecked")
      E prev = (E) unsafe.getObjectVolatile(array, off);
      E next = updateFunction.apply(prev);
      if (unsafe.compareAndSwapObject(array, off, prev, next)) {
        return prev;
      }
    }
  }

  public final E updateAndGet(int i, UnaryOperator<E> updateFunction) {
    Objects.requireNonNull(updateFunction);
    long off = offset(i);
    while (true) {
      @SuppressWarnings("unchecked")
      E prev = (E) unsafe.getObjectVolatile(array, off);
      E next = updateFunction.apply(prev);
      if (unsafe.compareAndSwapObject(array, off, prev, next)) {
        return next;
      }
    }
  }

  public final E getAndAccumulate(int i, E x, BinaryOperator<E> accumulatorFunction) {
    Objects.requireNonNull(accumulatorFunction);
    long off = offset(i);
    while (true) {
      @SuppressWarnings("unchecked")
      E prev = (E) unsafe.getObjectVolatile(array, off);
      E next = accumulatorFunction.apply(prev, x);
      if (unsafe.compareAndSwapObject(array, off, prev, next)) {
        return prev;
      }
    }
  }

  public final E accumulateAndGet(int i, E x, BinaryOperator<E> accumulatorFunction) {
    Objects.requireNonNull(accumulatorFunction);
    long off = offset(i);
    while (true) {
      @SuppressWarnings("unchecked")
      E prev = (E) unsafe.getObjectVolatile(array, off);
      E next = accumulatorFunction.apply(prev, x);
      if (unsafe.compareAndSwapObject(array, off, prev, next)) {
        return next;
      }
    }
  }

  @Override
  public String toString() {
    int max = array.length - 1;
    if (max == -1) return "[]";
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(get(i));
      if (i == max) return b.append(']').toString();
      b.append(',').append(' ');
    }
  }
}
