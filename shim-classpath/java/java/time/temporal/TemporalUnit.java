/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
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
    if (temporal instanceof TemporalAccessor) {
      return temporal.isSupported(this);
    }
    return false;
  }

  <R extends Temporal> R addTo(R temporal, long amount);

  long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive);
}
