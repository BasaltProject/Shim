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

public final class MinguoDate extends ChronoLocalDateImpl<MinguoDate> implements ChronoLocalDate, Serializable {
  private static final long serialVersionUID = 1300372329181994526L;

  public static MinguoDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static MinguoDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static MinguoDate now(Clock clock) {
    return new MinguoDate(LocalDate.now(clock));
  }

  public static MinguoDate of(int prolepticYear, int month, int dayOfMonth) {
    return MinguoChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
  }

  public static MinguoDate from(TemporalAccessor temporal) {
    return MinguoChronology.INSTANCE.date(temporal);
  }

  private final LocalDate isoDate;

  MinguoDate(LocalDate isoDate) {
    Objects.requireNonNull(isoDate, "isoDate");
    this.isoDate = isoDate;
  }

  boolean validatesEra() {
    return false;
  }

  int yearsDifference() {
    return 1911;
  }

  MinguoDate create(LocalDate isoDate) {
    return new MinguoDate(isoDate);
  }

  @Override
  public MinguoChronology getChronology() {
    return MinguoChronology.INSTANCE;
  }

  @Override
  public MinguoEra getEra() {
    return (prolepticYear() >= 1 ? MinguoEra.ROC : MinguoEra.BEFORE_ROC);
  }

  @Override
  public MinguoDate with(TemporalAdjuster adjuster) {
    return super.with(adjuster);
  }

  @Override
  public MinguoDate plus(TemporalAmount amount) {
    return super.plus(amount);
  }

  @Override
  public MinguoDate plus(long amountToAdd, TemporalUnit unit) {
    return super.plus(amountToAdd, unit);
  }

  @Override
  public MinguoDate minus(TemporalAmount amount) {
    return super.minus(amount);
  }

  @Override
  public MinguoDate minus(long amountToSubtract, TemporalUnit unit) {
    return super.minus(amountToSubtract, unit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<MinguoDate> atTime(LocalTime localTime) {
    return (ChronoLocalDateTime<MinguoDate>) super.atTime(localTime);
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

  MinguoDate withIso(LocalDate date) {
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
  public MinguoDate with(TemporalField field, long newValue) {
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
  private MinguoDate castField(java.time.temporal.Temporal t) {
    return (MinguoDate) ChronoLocalDateOps.ensureValid(getChronology(), t);
  }

  @Override
  MinguoDate plusYears(long years) {
    return withIso(isoDate.plusYears(years));
  }

  @Override
  MinguoDate plusMonths(long months) {
    return withIso(isoDate.plusMonths(months));
  }

  @Override
  MinguoDate plusWeeks(long weeks) {
    return withIso(isoDate.plusWeeks(weeks));
  }

  @Override
  MinguoDate plusDays(long days) {
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
      return isoDate.equals(((MinguoDate) obj).isoDate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getChronology().getId().hashCode() ^ isoDate.hashCode();
  }
}
