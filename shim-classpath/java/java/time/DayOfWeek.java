/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Objects;

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

  public static DayOfWeek from(TemporalAccessor temporal) {
    if (temporal instanceof DayOfWeek) {
      return (DayOfWeek) temporal;
    }
    try {
      return of(temporal.get(DAY_OF_WEEK));
    } catch (DateTimeException ex) {
      throw new DateTimeException("Unable to obtain DayOfWeek from TemporalAccessor: " + temporal + " of type "
          + temporal.getClass().getName(), ex);
    }
  }

  public int getValue() {
    return ordinal() + 1;
  }

  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    String name = name();
    String full = name.charAt(0) + name.substring(1).toLowerCase();
    switch (style) {
      case FULL:
      case FULL_STANDALONE:
        return full;
      case SHORT:
      case SHORT_STANDALONE:
        return full.substring(0, 3);
      default:
        return full.substring(0, 1);
    }
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field instanceof java.time.temporal.ChronoField) {
      return field == DAY_OF_WEEK;
    }
    return field != null && field.isSupportedBy(this);
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field == DAY_OF_WEEK) {
      return field.range();
    }
    return TemporalAccessor.super.range(field);
  }

  @Override
  public int get(TemporalField field) {
    if (field == DAY_OF_WEEK) {
      return getValue();
    }
    return TemporalAccessor.super.get(field);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field == DAY_OF_WEEK) {
      return getValue();
    } else if (field instanceof java.time.temporal.ChronoField) {
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }
    return field.getFrom(this);
  }

  public DayOfWeek plus(long days) {
    int amount = (int) (days % 7);
    return ENUMS[(ordinal() + (amount + 7)) % 7];
  }

  public DayOfWeek minus(long days) {
    return plus(-(days % 7));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> R query(TemporalQuery<R> query) {
    if (query == TemporalQueries.precision()) {
      return (R) ChronoUnit.DAYS;
    }
    return TemporalAccessor.super.query(query);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(DAY_OF_WEEK, getValue());
  }
}
