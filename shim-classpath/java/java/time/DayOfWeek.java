/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;

public enum DayOfWeek implements TemporalAccessor, TemporalAdjuster {
  MONDAY,
  TUESDAY,
  WEDNESDAY,
  THURSDAY,
  FRIDAY,
  SATURDAY,
  SUNDAY;

  private static final DayOfWeek[] ENUMS = DayOfWeek.values();

  public static DayOfWeek of(int dayOfWeek) {
    if (dayOfWeek < 1 || dayOfWeek > 7) {
      throw new DateTimeException("Invalid value for DayOfWeek: " + dayOfWeek);
    }
    return ENUMS[dayOfWeek - 1];
  }

  public int getValue() {
    return ordinal() + 1;
  }

  public DayOfWeek plus(long days) {
    int amount = (int) (days % 7);
    return ENUMS[(ordinal() + (amount + 7)) % 7];
  }

  public DayOfWeek minus(long days) {
    return plus(-(days % 7));
  }

  @Override
  public boolean isSupported(TemporalField field) {
    return field == ChronoField.DAY_OF_WEEK;
  }

  @Override
  public long getLong(TemporalField field) {
    if (field == ChronoField.DAY_OF_WEEK) {
      return getValue();
    }
    throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.DAY_OF_WEEK, getValue());
  }
}
