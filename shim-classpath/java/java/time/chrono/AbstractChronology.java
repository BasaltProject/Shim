/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.format.ResolverStyle;
import java.time.temporal.TemporalField;
import java.util.Map;

public abstract class AbstractChronology implements Chronology {
  protected AbstractChronology() { }

  @Override
  public ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
    throw new UnsupportedOperationException("resolveDate");
  }

  @Override
  public int compareTo(Chronology other) {
    return getId().compareTo(other.getId());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof AbstractChronology) {
      return compareTo((AbstractChronology) obj) == 0;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() ^ getId().hashCode();
  }

  @Override
  public String toString() {
    return getId();
  }
}
