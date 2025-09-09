/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import sun.misc.Unsafe;

public class AtomicIntegerArray implements Serializable {
  private static final long serialVersionUID = 2862133569453604235L;

  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long arrayOffset = unsafe.arrayBaseOffset(int[].class);
  private static final long arrayScale = unsafe.arrayIndexScale(int[].class);

  private final int[] array;

  public AtomicIntegerArray(int length) {
    this.array = new int[length];
  }

  public AtomicIntegerArray(int[] array) {
    this.array = array.clone();
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

  public int get(int i) {
    return unsafe.getIntVolatile(array, offset(i));
  }

  public void set(int i, int newValue) {
    unsafe.putIntVolatile(array, offset(i), newValue);
  }

  public void lazySet(int i, int newValue) {
    unsafe.putOrderedInt(array, offset(i), newValue);
  }

  public int getAndSet(int i, int newValue) {
    long off = offset(i);
    while (true) {
      int current = unsafe.getIntVolatile(array, off);
      if (unsafe.compareAndSwapInt(array, off, current, newValue)) {
        return current;
      }
    }
  }

  public boolean compareAndSet(int i, int expectedValue, int newValue) {
    return unsafe.compareAndSwapInt(array, offset(i), expectedValue, newValue);
  }

  public boolean weakCompareAndSet(int i, int expectedValue, int newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(int i, int expectedValue, int newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(int i, int expectedValue, int newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(int i, int expectedValue, int newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(int i, int expectedValue, int newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public int compareAndExchange(int i, int expectedValue, int newValue) {
    long off = offset(i);
    while (true) {
      int witness = unsafe.getIntVolatile(array, off);
      if (witness == expectedValue) {
        if (unsafe.compareAndSwapInt(array, off, witness, newValue)) {
          return witness;
        }
      } else {
        return witness;
      }
    }
  }

  public int compareAndExchangeAcquire(int i, int expectedValue, int newValue) {
    return compareAndExchange(i, expectedValue, newValue);
  }

  public int compareAndExchangeRelease(int i, int expectedValue, int newValue) {
    return compareAndExchange(i, expectedValue, newValue);
  }

  public int getPlain(int i) {
    return get(i);
  }

  public void setPlain(int i, int newValue) {
    set(i, newValue);
  }

  public int getOpaque(int i) {
    return get(i);
  }

  public void setOpaque(int i, int newValue) {
    set(i, newValue);
  }

  public int getAcquire(int i) {
    return get(i);
  }

  public void setRelease(int i, int newValue) {
    set(i, newValue);
  }

  public int getAndAdd(int i, int delta) {
    long off = offset(i);
    while (true) {
      int current = unsafe.getIntVolatile(array, off);
      int next = current + delta;
      if (unsafe.compareAndSwapInt(array, off, current, next)) {
        return current;
      }
    }
  }

  public int addAndGet(int i, int delta) {
    return getAndAdd(i, delta) + delta;
  }

  public int getAndIncrement(int i) {
    return getAndAdd(i, 1);
  }

  public int incrementAndGet(int i) {
    return getAndAdd(i, 1) + 1;
  }

  public int getAndDecrement(int i) {
    return getAndAdd(i, -1);
  }

  public int decrementAndGet(int i) {
    return getAndAdd(i, -1) - 1;
  }

  public final int getAndUpdate(int i, IntUnaryOperator updateFunction) {
    Objects.requireNonNull(updateFunction);
    long off = offset(i);
    while (true) {
      int prev = unsafe.getIntVolatile(array, off);
      int next = updateFunction.applyAsInt(prev);
      if (unsafe.compareAndSwapInt(array, off, prev, next)) {
        return prev;
      }
    }
  }

  public final int updateAndGet(int i, IntUnaryOperator updateFunction) {
    Objects.requireNonNull(updateFunction);
    long off = offset(i);
    while (true) {
      int prev = unsafe.getIntVolatile(array, off);
      int next = updateFunction.applyAsInt(prev);
      if (unsafe.compareAndSwapInt(array, off, prev, next)) {
        return next;
      }
    }
  }

  public final int getAndAccumulate(int i, int x, IntBinaryOperator accumulatorFunction) {
    Objects.requireNonNull(accumulatorFunction);
    long off = offset(i);
    while (true) {
      int prev = unsafe.getIntVolatile(array, off);
      int next = accumulatorFunction.applyAsInt(prev, x);
      if (unsafe.compareAndSwapInt(array, off, prev, next)) {
        return prev;
      }
    }
  }

  public final int accumulateAndGet(int i, int x, IntBinaryOperator accumulatorFunction) {
    Objects.requireNonNull(accumulatorFunction);
    long off = offset(i);
    while (true) {
      int prev = unsafe.getIntVolatile(array, off);
      int next = accumulatorFunction.applyAsInt(prev, x);
      if (unsafe.compareAndSwapInt(array, off, prev, next)) {
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
