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
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import sun.misc.Unsafe;

public class AtomicReference<T> implements java.io.Serializable {
  private static final long serialVersionUID = -1848883965231344442L;
  
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final long valueOffset;
  
  static {
    try {
      Field<?> f = AtomicReference.class.getDeclaredField("value");
      valueOffset = unsafe.objectFieldOffset(f);
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }
  
  private volatile T value;
  
  public AtomicReference() {
    this(null);
  }
  
  public AtomicReference(T initialValue) {
    this.value = initialValue;
  }
  
  public T get() {
    return value;
  }
  
  public void set(T newValue) {
    value = newValue;
  }
  
  public void lazySet(T newValue) {
    unsafe.putOrderedObject(this, valueOffset, newValue);
  }
  
  public boolean compareAndSet(T expect, T update) {
    return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
  }
  
  public boolean weakCompareAndSet(T expect, T update) {
    return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
  }
  
  public final T getAndSet(T newValue) {
    while (true) {
      T current = value;
      if (compareAndSet(current, newValue)) {
        return current;
      }
    }
  }
  
  public final T getAndUpdate(UnaryOperator<T> updateFunction) {
    while (true) {
      T prev = get();
      T next = updateFunction.apply(prev);
      if (compareAndSet(prev, next)) {
        return prev;
      }
    }
  }

  public final T updateAndGet(UnaryOperator<T> updateFunction) {
    while (true) {
      T prev = get();
      T next = updateFunction.apply(prev);
      if (compareAndSet(prev, next)) {
        return next;
      }
    }
  }

  public final T getAndAccumulate(T x, BinaryOperator<T> accumulatorFunction) {
    while (true) {
      T prev = get();
      T next = accumulatorFunction.apply(prev, x);
      if (compareAndSet(prev, next)) {
        return prev;
      }
    }
  }

  public final T accumulateAndGet(T x, BinaryOperator<T> accumulatorFunction) {
    while (true) {
      T prev = get();
      T next = accumulatorFunction.apply(prev, x);
      if (compareAndSet(prev, next)) {
        return next;
      }
    }
  }

  public T getPlain() {
    return value;
  }

  public void setPlain(T newValue) {
    value = newValue;
  }

  public T getOpaque() {
    return value;
  }

  public void setOpaque(T newValue) {
    value = newValue;
  }

  public T getAcquire() {
    return value;
  }

  public void setRelease(T newValue) {
    lazySet(newValue);
  }

  public final T compareAndExchange(T expectedValue, T newValue) {
    while (true) {
      T cur = get();
      if (cur != expectedValue) {
        return cur;
      }
      if (compareAndSet(cur, newValue)) {
        return expectedValue;
      }
    }
  }

  public final T compareAndExchangeAcquire(T expectedValue, T newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public final T compareAndExchangeRelease(T expectedValue, T newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(T expectedValue, T newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(T expectedValue, T newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(T expectedValue, T newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(T expectedValue, T newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
