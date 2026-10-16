/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.DateTimeException;
import java.time.temporal.Temporal;

final class ChronoLocalDateOps {
  private ChronoLocalDateOps() { }

  static ChronoLocalDate ensureValid(Chronology chrono, Temporal temporal) {
    ChronoLocalDate other = (ChronoLocalDate) temporal;
    if (!chrono.equals(other.getChronology())) {
      throw new ClassCastException("Chronology mismatch, expected: " + chrono.getId() + ", actual: "
          + other.getChronology().getId());
    }
    return other;
  }
}
