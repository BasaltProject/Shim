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
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import sun.misc.Unsafe;

public class AtomicLongArray implements Serializable {
  private static final long serialVersionUID = -2308431214976778248L;

  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long arrayOffset = unsafe.arrayBaseOffset(long[].class);
  private static final long arrayScale = unsafe.arrayIndexScale(long[].class);

  private final long[] array;

  public AtomicLongArray(int length) {
    this.array = new long[length];
  }

  public AtomicLongArray(long[] array) {
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

  public long get(int i) {
    return unsafe.getLongVolatile(array, offset(i));
  }

  public void set(int i, long newValue) {
    unsafe.putLongVolatile(array, offset(i), newValue);
  }

  public void lazySet(int i, long newValue) {
    unsafe.putOrderedLong(array, offset(i), newValue);
  }

  public long getAndSet(int i, long newValue) {
    long off = offset(i);
    while (true) {
      long current = unsafe.getLongVolatile(array, off);
      if (unsafe.compareAndSwapLong(array, off, current, newValue)) {
        return current;
      }
    }
  }

  public boolean compareAndSet(int i, long expectedValue, long newValue) {
    return unsafe.compareAndSwapLong(array, offset(i), expectedValue, newValue);
  }

  public boolean weakCompareAndSet(int i, long expectedValue, long newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(int i, long expectedValue, long newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(int i, long expectedValue, long newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(int i, long expectedValue, long newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(int i, long expectedValue, long newValue) {
    return compareAndSet(i, expectedValue, newValue);
  }

  public long compareAndExchange(int i, long expectedValue, long newValue) {
    long off = offset(i);
    while (true) {
      long witness = unsafe.getLongVolatile(array, off);
      if (witness == expectedValue) {
        if (unsafe.compareAndSwapLong(array, off, witness, newValue)) {
          return witness;
        }
      } else {
        return witness;
      }
    }
  }

  public long compareAndExchangeAcquire(int i, long expectedValue, long newValue) {
    return compareAndExchange(i, expectedValue, newValue);
  }

  public long compareAndExchangeRelease(int i, long expectedValue, long newValue) {
    return compareAndExchange(i, expectedValue, newValue);
  }

  public long getPlain(int i) {
    return get(i);
  }

  public void setPlain(int i, long newValue) {
    set(i, newValue);
  }

  public long getOpaque(int i) {
    return get(i);
  }

  public void setOpaque(int i, long newValue) {
    set(i, newValue);
  }

  public long getAcquire(int i) {
    return get(i);
  }

  public void setRelease(int i, long newValue) {
    set(i, newValue);
  }

  public long getAndAdd(int i, long delta) {
    long off = offset(i);
    while (true) {
      long current = unsafe.getLongVolatile(array, off);
      long next = current + delta;
      if (unsafe.compareAndSwapLong(array, off, current, next)) {
        return current;
      }
    }
  }

  public long addAndGet(int i, long delta) {
    return getAndAdd(i, delta) + delta;
  }

  public long getAndIncrement(int i) {
    return getAndAdd(i, 1L);
  }

  public long incrementAndGet(int i) {
    return getAndAdd(i, 1L) + 1L;
  }

  public long getAndDecrement(int i) {
    return getAndAdd(i, -1L);
  }

  public long decrementAndGet(int i) {
    return getAndAdd(i, -1L) - 1L;
  }

  public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
    Objects.requireNonNull(updateFunction);
    long off = offset(i);
    while (true) {
      long prev = unsafe.getLongVolatile(array, off);
      long next = updateFunction.applyAsLong(prev);
      if (unsafe.compareAndSwapLong(array, off, prev, next)) {
        return prev;
      }
    }
  }

  public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
    Objects.requireNonNull(updateFunction);
    long off = offset(i);
    while (true) {
      long prev = unsafe.getLongVolatile(array, off);
      long next = updateFunction.applyAsLong(prev);
      if (unsafe.compareAndSwapLong(array, off, prev, next)) {
        return next;
      }
    }
  }

  public final long getAndAccumulate(int i, long x, LongBinaryOperator accumulatorFunction) {
    Objects.requireNonNull(accumulatorFunction);
    long off = offset(i);
    while (true) {
      long prev = unsafe.getLongVolatile(array, off);
      long next = accumulatorFunction.applyAsLong(prev, x);
      if (unsafe.compareAndSwapLong(array, off, prev, next)) {
        return prev;
      }
    }
  }

  public final long accumulateAndGet(int i, long x, LongBinaryOperator accumulatorFunction) {
    Objects.requireNonNull(accumulatorFunction);
    long off = offset(i);
    while (true) {
      long prev = unsafe.getLongVolatile(array, off);
      long next = accumulatorFunction.applyAsLong(prev, x);
      if (unsafe.compareAndSwapLong(array, off, prev, next)) {
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
