/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.Duration;

public interface TemporalUnit {
  Duration getDuration();

  boolean isDurationEstimated();

  boolean isDateBased();

  boolean isTimeBased();

  default boolean isSupportedBy(Temporal temporal) {
    if (temporal instanceof java.time.LocalTime) {
      return isTimeBased();
    }
    if (temporal instanceof java.time.chrono.ChronoLocalDate) {
      return isDateBased();
    }
    if (temporal instanceof java.time.chrono.ChronoLocalDateTime
        || temporal instanceof java.time.chrono.ChronoZonedDateTime) {
      return true;
    }
    try {
      temporal.plus(1, this);
      return true;
    } catch (UnsupportedTemporalTypeException ex) {
      return false;
    } catch (RuntimeException ex) {
      try {
        temporal.plus(-1, this);
        return true;
      } catch (RuntimeException ex2) {
        return false;
      }
    }
  }

  <R extends Temporal> R addTo(R temporal, long amount);

  long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive);

  @Override
  String toString();
}
