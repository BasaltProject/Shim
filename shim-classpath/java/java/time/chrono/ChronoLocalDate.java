/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public interface ChronoLocalDate extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDate> {
  long toEpochDay();

  Chronology getChronology();

  default boolean isLeapYear() {
    return false;
  }

  default int lengthOfYear() {
    return isLeapYear() ? 366 : 365;
  }

  @Override
  default int compareTo(ChronoLocalDate other) {
    int cmp = Long.compare(toEpochDay(), other.toEpochDay());
    if (cmp == 0) {
      cmp = getChronology().compareTo(other.getChronology());
    }
    return cmp;
  }

  default boolean isAfter(ChronoLocalDate other) {
    return this.toEpochDay() > other.toEpochDay();
  }

  default boolean isBefore(ChronoLocalDate other) {
    return this.toEpochDay() < other.toEpochDay();
  }

  default boolean isEqual(ChronoLocalDate other) {
    return this.toEpochDay() == other.toEpochDay();
  }
}
