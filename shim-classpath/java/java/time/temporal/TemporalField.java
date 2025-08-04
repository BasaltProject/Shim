/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

public interface TemporalField {
  TemporalUnit getBaseUnit();

  TemporalUnit getRangeUnit();

  boolean isDateBased();

  boolean isTimeBased();

  boolean isSupportedBy(TemporalAccessor temporal);

  long getFrom(TemporalAccessor temporal);

  <R extends Temporal> R adjustInto(R temporal, long newValue);
}
