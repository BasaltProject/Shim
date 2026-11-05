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

public class LongAccumulator extends Striped64 implements Serializable {
  private static final long serialVersionUID = 7249069246863182397L;

  private final LongBinaryOperator function;
  private final long identity;
  private final AtomicLong value;

  public LongAccumulator(LongBinaryOperator accumulatorFunction, long identity) {
    this.function = Objects.requireNonNull(accumulatorFunction);
    this.identity = identity;
    this.value = new AtomicLong(identity);
  }

  public void accumulate(long x) {
    while (true) {
      long cur = value.get();
      long next = function.applyAsLong(cur, x);
      if (value.compareAndSet(cur, next)) {
        return;
      }
    }
  }

  public long get() {
    return value.get();
  }

  public void reset() {
    value.set(identity);
  }

  public long getThenReset() {
    return value.getAndSet(identity);
  }

  @Override
  public String toString() {
    return Long.toString(get());
  }

  @Override
  public long longValue() {
    return get();
  }

  @Override
  public int intValue() {
    return (int) get();
  }

  @Override
  public float floatValue() {
    return (float) get();
  }

  @Override
  public double doubleValue() {
    return (double) get();
  }

  private static class SerializationProxy implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;
    private final long value;
    private final LongBinaryOperator function;
    private final long identity;

    SerializationProxy(LongAccumulator a) {
      value = a.get();
      function = a.function;
      identity = a.identity;
    }

    private Object readResolve() {
      LongAccumulator a = new LongAccumulator(function, identity);
      a.value.set(value);
      return a;
    }
  }

  private Object writeReplace() {
    return new SerializationProxy(this);
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }
}
