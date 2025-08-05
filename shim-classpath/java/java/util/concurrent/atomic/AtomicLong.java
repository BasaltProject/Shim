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

import java.lang.reflect.Field;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import sun.misc.Unsafe;

public class AtomicLong extends Number implements java.io.Serializable {
  private static final long serialVersionUID = 1927816293512124184L;
  
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long valueOffset;
  
  static {
    try {
      Field<?> f = AtomicLong.class.getDeclaredField("value");
      valueOffset = unsafe.objectFieldOffset(f);
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }
  
  private volatile long value;
  
  public AtomicLong() {
    this(0);
  }
  
  public AtomicLong(long initialValue) {
    this.value = initialValue;
  }
  
  public long get() {
    return value;
  }
  
  public void set(long newValue) {
    this.value = newValue;
  }
  
  public void lazySet(long newValue) {
    unsafe.putOrderedLong(this, valueOffset, newValue);
  }
  
  public boolean compareAndSet(long expect, long update) {
    return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
  }
  
  public boolean weakCompareAndSet(long expect, long update) {
    return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
  }
  
  public long getAndSet(long newValue) {
    while (true) {
      long current = value;
      if (compareAndSet(current, newValue)) {
        return current;
      }
    }
  }
  
  public long getAndAdd(long delta) {
    while (true) {
      long current = value;
      long next = current + delta;
      if (compareAndSet(current, next)) {
        return current;
      }
    }
  }
  
  public long getAndIncrement() {
    return getAndAdd(1);
  }
  
  public long getAndDecrement() {
    return getAndAdd(-1);
  }
  
  public long addAndGet(long delta) {
    while (true) {
      long current = value;
      long next = current + delta;
      if (compareAndSet(current, next)) {
        return next;
      }
    }
  }
  
  public long incrementAndGet() {
    return addAndGet(1);
  }
  
  public long decrementAndGet() {
    return addAndGet(-1);
  }
  
  public final long getAndUpdate(LongUnaryOperator updateFunction) {
    while (true) {
      long prev = get();
      long next = updateFunction.applyAsLong(prev);
      if (compareAndSet(prev, next)) {
        return prev;
      }
    }
  }

  public final long updateAndGet(LongUnaryOperator updateFunction) {
    while (true) {
      long prev = get();
      long next = updateFunction.applyAsLong(prev);
      if (compareAndSet(prev, next)) {
        return next;
      }
    }
  }

  public final long getAndAccumulate(long x, LongBinaryOperator accumulatorFunction) {
    while (true) {
      long prev = get();
      long next = accumulatorFunction.applyAsLong(prev, x);
      if (compareAndSet(prev, next)) {
        return prev;
      }
    }
  }

  public final long accumulateAndGet(long x, LongBinaryOperator accumulatorFunction) {
    while (true) {
      long prev = get();
      long next = accumulatorFunction.applyAsLong(prev, x);
      if (compareAndSet(prev, next)) {
        return next;
      }
    }
  }

  public long getPlain() {
    return value;
  }

  public void setPlain(long newValue) {
    value = newValue;
  }

  public long getOpaque() {
    return value;
  }

  public void setOpaque(long newValue) {
    value = newValue;
  }

  public long getAcquire() {
    return value;
  }

  public void setRelease(long newValue) {
    lazySet(newValue);
  }

  public final long compareAndExchange(long expectedValue, long newValue) {
    while (true) {
      long cur = get();
      if (cur != expectedValue) {
        return cur;
      }
      if (compareAndSet(cur, newValue)) {
        return expectedValue;
      }
    }
  }

  public final long compareAndExchangeAcquire(long expectedValue, long newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public final long compareAndExchangeRelease(long expectedValue, long newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(long expectedValue, long newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(long expectedValue, long newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(long expectedValue, long newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(long expectedValue, long newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  @Override
  public String toString() {
    return Long.toString(get());
  }

  @Override
  public byte byteValue() {
    return (byte)value;
  }

  @Override
  public short shortValue() {
    return (short)value;
  }

  @Override
  public int intValue() {
    return (int)value;
  }

  @Override
  public long longValue() {
    return value;
  }

  @Override
  public float floatValue() {
    return value;
  }

  @Override
  public double doubleValue() {
    return value;
  }
}
