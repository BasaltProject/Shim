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

public class DoubleAdder extends Number implements Serializable {
  private static final long serialVersionUID = 7249069246863182397L;

  private final AtomicLong bits = new AtomicLong();

  public DoubleAdder() {}

  public void add(double x) {
    while (true) {
      long curBits = bits.get();
      double cur = Double.longBitsToDouble(curBits);
      double next = cur + x;
      long nextBits = Double.doubleToRawLongBits(next);
      if (bits.compareAndSet(curBits, nextBits)) {
        return;
      }
    }
  }

  public double sum() {
    return Double.longBitsToDouble(bits.get());
  }

  public void reset() {
    bits.set(0L);
  }

  public double sumThenReset() {
    long oldBits = bits.getAndSet(0L);
    return Double.longBitsToDouble(oldBits);
  }

  @Override
  public String toString() {
    return Double.toString(sum());
  }

  @Override
  public double doubleValue() {
    return sum();
  }

  @Override
  public long longValue() {
    return (long) sum();
  }

  @Override
  public int intValue() {
    return (int) sum();
  }

  @Override
  public float floatValue() {
    return (float) sum();
  }
}
