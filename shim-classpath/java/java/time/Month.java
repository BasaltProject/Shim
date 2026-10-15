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

public enum Month implements TemporalAccessor, TemporalAdjuster {
  JANUARY,
  FEBRUARY,
  MARCH,
  APRIL,
  MAY,
  JUNE,
  JULY,
  AUGUST,
  SEPTEMBER,
  OCTOBER,
  NOVEMBER,
  DECEMBER;

  private static final Month[] ENUMS = Month.values();

  public static Month of(int month) {
    if (month < 1 || month > 12) {
      throw new DateTimeException("Invalid value for Month: " + month);
    }
    return ENUMS[month - 1];
  }

  public int getValue() {
    return ordinal() + 1;
  }

  public Month plus(long months) {
    int amount = (int) (months % 12);
    return ENUMS[(ordinal() + (amount + 12)) % 12];
  }

  public Month minus(long months) {
    return plus(-(months % 12));
  }

  public int length(boolean leapYear) {
    switch (this) {
    case FEBRUARY:
      return leapYear ? 29 : 28;
    case APRIL:
    case JUNE:
    case SEPTEMBER:
    case NOVEMBER:
      return 30;
    default:
      return 31;
    }
  }

  public int minLength() {
    return length(false);
  }

  public int maxLength() {
    return length(true);
  }

  public int firstDayOfYear(boolean leapYear) {
    int leap = leapYear ? 1 : 0;
    switch (this) {
    case JANUARY: return 1;
    case FEBRUARY: return 32;
    case MARCH: return 60 + leap;
    case APRIL: return 91 + leap;
    case MAY: return 121 + leap;
    case JUNE: return 152 + leap;
    case JULY: return 182 + leap;
    case AUGUST: return 213 + leap;
    case SEPTEMBER: return 244 + leap;
    case OCTOBER: return 274 + leap;
    case NOVEMBER: return 305 + leap;
    case DECEMBER: return 335 + leap;
    default: throw new AssertionError();
    }
  }

  @Override
  public boolean isSupported(TemporalField field) {
    return field == ChronoField.MONTH_OF_YEAR;
  }

  @Override
  public long getLong(TemporalField field) {
    if (field == ChronoField.MONTH_OF_YEAR) {
      return getValue();
    }
    throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.MONTH_OF_YEAR, getValue());
  }
}
