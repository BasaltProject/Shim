/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

public final class ThaiBuddhistDate extends ChronoLocalDateImpl<ThaiBuddhistDate> implements ChronoLocalDate, Serializable {
  private static final long serialVersionUID = -8722293800195731463L;

  public static ThaiBuddhistDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static ThaiBuddhistDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static ThaiBuddhistDate now(Clock clock) {
    return new ThaiBuddhistDate(LocalDate.now(clock));
  }

  public static ThaiBuddhistDate of(int prolepticYear, int month, int dayOfMonth) {
    return ThaiBuddhistChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
  }

  public static ThaiBuddhistDate from(TemporalAccessor temporal) {
    return ThaiBuddhistChronology.INSTANCE.date(temporal);
  }

  private final LocalDate isoDate;

  ThaiBuddhistDate(LocalDate isoDate) {
    Objects.requireNonNull(isoDate, "isoDate");
    this.isoDate = isoDate;
  }

  int yearsDifference() {
    return -543;
  }

  boolean validatesEra() {
    return true;
  }

  ThaiBuddhistDate create(LocalDate isoDate) {
    return new ThaiBuddhistDate(isoDate);
  }

  @Override
  public ThaiBuddhistChronology getChronology() {
    return ThaiBuddhistChronology.INSTANCE;
  }

  @Override
  public ThaiBuddhistEra getEra() {
    return (prolepticYear() >= 1 ? ThaiBuddhistEra.BE : ThaiBuddhistEra.BEFORE_BE);
  }

  @Override
  public ThaiBuddhistDate with(TemporalAdjuster adjuster) {
    return super.with(adjuster);
  }

  @Override
  public ThaiBuddhistDate plus(TemporalAmount amount) {
    return super.plus(amount);
  }

  @Override
  public ThaiBuddhistDate plus(long amountToAdd, TemporalUnit unit) {
    return super.plus(amountToAdd, unit);
  }

  @Override
  public ThaiBuddhistDate minus(TemporalAmount amount) {
    return super.minus(amount);
  }

  @Override
  public ThaiBuddhistDate minus(long amountToSubtract, TemporalUnit unit) {
    return super.minus(amountToSubtract, unit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<ThaiBuddhistDate> atTime(LocalTime localTime) {
    return (ChronoLocalDateTime<ThaiBuddhistDate>) super.atTime(localTime);
  }

  @Override
  public int lengthOfMonth() {
    return isoDate.lengthOfMonth();
  }

  int prolepticYear() {
    return isoDate.getYear() - yearsDifference();
  }

  private long prolepticMonth() {
    return prolepticYear() * 12L + isoDate.getMonthValue() - 1;
  }

  ThaiBuddhistDate withIso(LocalDate date) {
    return (date == isoDate || date.equals(isoDate)) ? self() : create(date);
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field instanceof ChronoField) {
      if (isSupported(field)) {
        ChronoField f = (ChronoField) field;
        switch (f) {
          case DAY_OF_MONTH:
          case DAY_OF_YEAR:
          case ALIGNED_WEEK_OF_MONTH:
            return isoDate.range(field);
          case YEAR_OF_ERA: {
            ValueRange range = YEAR.range();
            long max = (prolepticYear() <= 0 ? -range.getMinimum() + 1 + yearsDifference()
                : range.getMaximum() - yearsDifference());
            return ValueRange.of(1, max);
          }
          default:
            return getChronology().range(f);
        }
      }
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }
    return field.rangeRefinedBy(this);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case PROLEPTIC_MONTH:
          return prolepticMonth();
        case YEAR_OF_ERA: {
          int py = prolepticYear();
          return py >= 1 ? py : 1 - py;
        }
        case YEAR:
          return prolepticYear();
        case ERA:
          return prolepticYear() >= 1 ? 1 : 0;
        default:
          return isoDate.getLong(field);
      }
    }
    return field.getFrom(this);
  }

  @Override
  public ThaiBuddhistDate with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      if (getLong(f) == newValue) {
        return self();
      }
      switch (f) {
        case PROLEPTIC_MONTH:
          getChronology().range(f).checkValidValue(newValue, f);
          return plusMonths(newValue - prolepticMonth());
        case ERA:
          if (validatesEra()) {
            getChronology().range(f).checkValidIntValue(newValue, f);
          }
          return withIso(isoDate.withYear((1 - prolepticYear()) + yearsDifference()));
        case YEAR_OF_ERA:
        case YEAR: {
          int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
          switch (f) {
            case YEAR_OF_ERA:
              return withIso(isoDate.withYear(prolepticYear() >= 1 ? nvalue + yearsDifference()
                  : (1 - nvalue) + yearsDifference()));
            default:
              return withIso(isoDate.withYear(nvalue + yearsDifference()));
          }
        }
        default:
          break;
      }
      return withIso(isoDate.with(field, newValue));
    }
    return castField(field.adjustInto(this, newValue));
  }

  @SuppressWarnings("unchecked")
  private ThaiBuddhistDate castField(java.time.temporal.Temporal t) {
    return (ThaiBuddhistDate) ChronoLocalDateOps.ensureValid(getChronology(), t);
  }

  @Override
  ThaiBuddhistDate plusYears(long years) {
    return withIso(isoDate.plusYears(years));
  }

  @Override
  ThaiBuddhistDate plusMonths(long months) {
    return withIso(isoDate.plusMonths(months));
  }

  @Override
  ThaiBuddhistDate plusWeeks(long weeks) {
    return withIso(isoDate.plusWeeks(weeks));
  }

  @Override
  ThaiBuddhistDate plusDays(long days) {
    return withIso(isoDate.plusDays(days));
  }

  @Override
  public ChronoPeriod until(ChronoLocalDate endDateExclusive) {
    LocalDate end = LocalDate.ofEpochDay(getChronology().date(endDateExclusive).toEpochDay());
    Period period = isoDate.until(end);
    return getChronology().period(period.getYears(), period.getMonths(), period.getDays());
  }

  @Override
  public long toEpochDay() {
    return isoDate.toEpochDay();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj.getClass() == getClass()) {
      return isoDate.equals(((ThaiBuddhistDate) obj).isoDate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getChronology().getId().hashCode() ^ isoDate.hashCode();
  }
}
