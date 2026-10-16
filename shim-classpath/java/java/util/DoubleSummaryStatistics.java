/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class DoubleSummaryStatistics implements java.util.function.DoubleConsumer {
  private long count;
  private double sum;
  private double sumCompensation;
  private double simpleSum;
  private double min = Double.POSITIVE_INFINITY;
  private double max = Double.NEGATIVE_INFINITY;

  public DoubleSummaryStatistics() { }

  public DoubleSummaryStatistics(long count, double min, double max, double sum) throws IllegalArgumentException {
    if (count < 0L) {
      throw new IllegalArgumentException("Negative count value");
    }
    if (count > 0L) {
      if (min > max) {
        throw new IllegalArgumentException("Minimum greater than maximum");
      }
      long ncount = count;
      double nmin = min;
      double nmax = max;
      double nsum = sum;
      if (Double.isNaN(nmin) || Double.isNaN(nmax) || Double.isNaN(nsum)) {
        nmin = Double.NaN;
        nmax = Double.NaN;
        nsum = Double.NaN;
      }
      this.count = ncount;
      this.sum = nsum;
      this.simpleSum = nsum;
      this.sumCompensation = 0.0d;
      this.min = nmin;
      this.max = nmax;
    }
  }

  @Override
  public void accept(double value) {
    ++count;
    simpleSum += value;
    sumWithCompensation(value);
    min = Math.min(min, value);
    max = Math.max(max, value);
  }

  public void combine(DoubleSummaryStatistics other) {
    count += other.count;
    simpleSum += other.simpleSum;
    sumWithCompensation(other.sum);
    sumWithCompensation(-other.sumCompensation);
    min = Math.min(min, other.min);
    max = Math.max(max, other.max);
  }

  private void sumWithCompensation(double value) {
    double tmp = value - sumCompensation;
    double velvel = sum + tmp;
    sumCompensation = (velvel - sum) - tmp;
    sum = velvel;
  }

  public final long getCount() {
    return count;
  }

  public final double getSum() {
    double tmp = sum - sumCompensation;
    if (Double.isNaN(tmp) && Double.isInfinite(simpleSum)) {
      return simpleSum;
    } else {
      return tmp;
    }
  }

  public final double getMin() {
    return min;
  }

  public final double getMax() {
    return max;
  }

  public final double getAverage() {
    return getCount() > 0 ? getSum() / getCount() : 0.0d;
  }

  @Override
  public String toString() {
    return String.format("%s{count=%d, sum=%f, min=%f, average=%f, max=%f}",
                         this.getClass().getSimpleName(), getCount(), getSum(), getMin(), getAverage(), getMax());
  }
}
