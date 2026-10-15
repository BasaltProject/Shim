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

public class LongAdder extends Number implements Serializable {
  private static final long serialVersionUID = 7249069246863182397L;

  private final AtomicLong value = new AtomicLong();

  public LongAdder() {}

  public void add(long x) {
    value.addAndGet(x);
  }

  public void increment() {
    add(1L);
  }

  public void decrement() {
    add(-1L);
  }

  public long sum() {
    return value.get();
  }

  public void reset() {
    value.set(0L);
  }

  public long sumThenReset() {
    return value.getAndSet(0L);
  }

  @Override
  public String toString() {
    return Long.toString(sum());
  }

  @Override
  public long longValue() {
    return sum();
  }

  @Override
  public int intValue() {
    return (int) sum();
  }

  @Override
  public float floatValue() {
    return (float) sum();
  }

  @Override
  public double doubleValue() {
    return (double) sum();
  }
}
