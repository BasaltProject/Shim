/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public interface ChronoLocalDateTime<D extends ChronoLocalDate> extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDateTime<?>> {
  D toLocalDate();

  LocalTime toLocalTime();

  @Override
  default int compareTo(ChronoLocalDateTime<?> other) {
    int cmp = toLocalDate().compareTo(other.toLocalDate());
    if (cmp == 0) {
      cmp = toLocalTime().compareTo(other.toLocalTime());
    }
    return cmp;
  }

  default boolean isAfter(ChronoLocalDateTime<?> other) {
    return compareTo(other) > 0;
  }

  default boolean isBefore(ChronoLocalDateTime<?> other) {
    return compareTo(other) < 0;
  }

  default boolean isEqual(ChronoLocalDateTime<?> other) {
    return compareTo(other) == 0;
  }
}
