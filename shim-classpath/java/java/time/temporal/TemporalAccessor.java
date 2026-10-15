/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.DateTimeException;

public interface TemporalAccessor {
  boolean isSupported(TemporalField field);

  default long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }
    return field.getFrom(this);
  }

  default int get(TemporalField field) {
    long value = getLong(field);
    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
      throw new DateTimeException("Field " + field + " value out of int range: " + value);
    }
    return (int) value;
  }

  default <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.zoneId()
        || query == TemporalQueries.precision()) {
      return null;
    }
    return query.queryFrom(this);
  }
}
