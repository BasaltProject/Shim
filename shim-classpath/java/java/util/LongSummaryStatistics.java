/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class LongSummaryStatistics implements java.util.function.LongConsumer, java.util.function.IntConsumer {
  private long count;
  private long sum;
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;

  public LongSummaryStatistics() { }

  public LongSummaryStatistics(long count, long min, long max, long sum) throws IllegalArgumentException {
    if (count < 0L) {
      throw new IllegalArgumentException("Negative count value");
    }
    if (count > 0L) {
      if (min > max) {
        throw new IllegalArgumentException("Minimum greater than maximum");
      }
      this.count = count;
      this.sum = sum;
      this.min = min;
      this.max = max;
    }
  }

  @Override
  public void accept(int value) {
    accept((long) value);
  }

  @Override
  public void accept(long value) {
    ++count;
    sum += value;
    min = Math.min(min, value);
    max = Math.max(max, value);
  }

  public void combine(LongSummaryStatistics other) {
    count += other.count;
    sum += other.sum;
    min = Math.min(min, other.min);
    max = Math.max(max, other.max);
  }

  public final long getCount() {
    return count;
  }

  public final long getSum() {
    return sum;
  }

  public final long getMin() {
    return min;
  }

  public final long getMax() {
    return max;
  }

  public final double getAverage() {
    return getCount() > 0 ? (double) getSum() / getCount() : 0.0d;
  }

  @Override
  public String toString() {
    return String.format("%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
                         this.getClass().getSimpleName(), getCount(), getSum(), getMin(), getAverage(), getMax());
  }
}
