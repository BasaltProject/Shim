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
import java.util.function.DoubleBinaryOperator;

public class DoubleAccumulator extends Number implements Serializable {
  private static final long serialVersionUID = 7249069246863182397L;

  private final DoubleBinaryOperator function;
  private final double identity;
  private final AtomicLong bits;

  public DoubleAccumulator(DoubleBinaryOperator accumulatorFunction, double identity) {
    this.function = Objects.requireNonNull(accumulatorFunction);
    this.identity = identity;
    this.bits = new AtomicLong(Double.doubleToRawLongBits(identity));
  }

  public void accumulate(double x) {
    while (true) {
      long curBits = bits.get();
      double cur = Double.longBitsToDouble(curBits);
      double next = function.applyAsDouble(cur, x);
      long nextBits = Double.doubleToRawLongBits(next);
      if (bits.compareAndSet(curBits, nextBits)) {
        return;
      }
    }
  }

  public double get() {
    return Double.longBitsToDouble(bits.get());
  }

  public void reset() {
    bits.set(Double.doubleToRawLongBits(identity));
  }

  public double getThenReset() {
    long curBits = bits.getAndSet(Double.doubleToRawLongBits(identity));
    return Double.longBitsToDouble(curBits);
  }

  @Override
  public String toString() {
    return Double.toString(get());
  }

  @Override
  public double doubleValue() {
    return get();
  }

  @Override
  public long longValue() {
    return (long) get();
  }

  @Override
  public int intValue() {
    return (int) get();
  }

  @Override
  public float floatValue() {
    return (float) get();
  }

  private static class SerializationProxy implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;
    private final double value;
    private final DoubleBinaryOperator function;
    private final long identity;

    SerializationProxy(DoubleAccumulator a) {
      value = a.get();
      function = a.function;
      identity = Double.doubleToRawLongBits(a.identity);
    }

    private Object readResolve() {
      DoubleAccumulator a = new DoubleAccumulator(function, Double.longBitsToDouble(identity));
      a.bits.set(Double.doubleToRawLongBits(value));
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
