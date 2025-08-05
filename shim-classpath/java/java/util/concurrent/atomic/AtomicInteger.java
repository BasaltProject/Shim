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
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import sun.misc.Unsafe;

public class AtomicInteger extends Number implements java.io.Serializable {
  private static final long serialVersionUID = 6214790243416807050L;
  
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long valueOffset;
  
  static {
    try {
      Field<?> f = AtomicInteger.class.getDeclaredField("value");
      valueOffset = unsafe.objectFieldOffset(f);
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }
  
  private volatile int value;
  
  public AtomicInteger() {
    this(0);
  }
  
  public AtomicInteger(int initialValue) {
    this.value = initialValue;
  }
  
  public int get() {
    return value;
  }
  
  public void set(int newValue) {
    this.value = newValue;
  }
  
  public void lazySet(int newValue) {
    unsafe.putOrderedInt(this, valueOffset, newValue);
  }
  
  public boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
  }
  
  public boolean weakCompareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
  }
  
  public int getAndSet(int newValue) {
    while (true) {
      int current = value;
      if (compareAndSet(current, newValue)) {
        return current;
      }
    }
  }
  
  public int getAndAdd(int delta) {
    while (true) {
      int current = value;
      int next = current + delta;
      if (compareAndSet(current, next)) {
        return current;
      }
    }
  }
  
  public int getAndIncrement() {
    return getAndAdd(1);
  }
  
  public int getAndDecrement() {
    return getAndAdd(-1);
  }
  
  public int addAndGet(int delta) {
    while (true) {
      int current = value;
      int next = current + delta;
      if (compareAndSet(current, next)) {
        return next;
      }
    }
  }
  
  public int incrementAndGet() {
    return addAndGet(1);
  }
  
  public int decrementAndGet() {
    return addAndGet(-1);
  }
  
  public final int getAndUpdate(IntUnaryOperator updateFunction) {
    while (true) {
      int prev = get();
      int next = updateFunction.applyAsInt(prev);
      if (compareAndSet(prev, next)) {
        return prev;
      }
    }
  }

  public final int updateAndGet(IntUnaryOperator updateFunction) {
    while (true) {
      int prev = get();
      int next = updateFunction.applyAsInt(prev);
      if (compareAndSet(prev, next)) {
        return next;
      }
    }
  }

  public final int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
    while (true) {
      int prev = get();
      int next = accumulatorFunction.applyAsInt(prev, x);
      if (compareAndSet(prev, next)) {
        return prev;
      }
    }
  }

  public final int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
    while (true) {
      int prev = get();
      int next = accumulatorFunction.applyAsInt(prev, x);
      if (compareAndSet(prev, next)) {
        return next;
      }
    }
  }

  public int getPlain() {
    return value;
  }

  public void setPlain(int newValue) {
    value = newValue;
  }

  public int getOpaque() {
    return value;
  }

  public void setOpaque(int newValue) {
    value = newValue;
  }

  public int getAcquire() {
    return value;
  }

  public void setRelease(int newValue) {
    lazySet(newValue);
  }

  public final int compareAndExchange(int expectedValue, int newValue) {
    while (true) {
      int cur = get();
      if (cur != expectedValue) {
        return cur;
      }
      if (compareAndSet(cur, newValue)) {
        return expectedValue;
      }
    }
  }

  public final int compareAndExchangeAcquire(int expectedValue, int newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public final int compareAndExchangeRelease(int expectedValue, int newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(int expectedValue, int newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(int expectedValue, int newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(int expectedValue, int newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(int expectedValue, int newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  @Override
  public String toString() {
    return Integer.toString(get());
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
    return value;
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
