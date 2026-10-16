/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR_OF_ERA;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

abstract class ChronoLocalDateImpl<D extends ChronoLocalDate> implements ChronoLocalDate, Temporal,
    TemporalAdjuster, Serializable {
  private static final long serialVersionUID = 6282433883239719096L;

  @SuppressWarnings("unchecked")
  D self() {
    return (D) this;
  }

  @Override
  public D with(TemporalAdjuster adjuster) {
    return castValid(adjuster.adjustInto(this));
  }

  @Override
  public D plus(TemporalAmount amount) {
    return castValid(amount.addTo(this));
  }

  @Override
  public D minus(TemporalAmount amount) {
    return castValid(amount.subtractFrom(this));
  }

  @SuppressWarnings("unchecked")
  private D castValid(Temporal temporal) {
    return (D) ChronoLocalDateOps.ensureValid(getChronology(), temporal);
  }

  @Override
  @SuppressWarnings("unchecked")
  public D plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      ChronoUnit f = (ChronoUnit) unit;
      switch (f) {
        case DAYS:
          return plusDays(amountToAdd);
        case WEEKS:
          return plusDays(Math.multiplyExact(amountToAdd, 7));
        case MONTHS:
          return plusMonths(amountToAdd);
        case YEARS:
          return plusYears(amountToAdd);
        case DECADES:
          return plusYears(Math.multiplyExact(amountToAdd, 10));
        case CENTURIES:
          return plusYears(Math.multiplyExact(amountToAdd, 100));
        case MILLENNIA:
          return plusYears(Math.multiplyExact(amountToAdd, 1000));
        case ERAS:
          return (D) with(ERA, Math.addExact(getLong(ERA), amountToAdd));
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return castValid(unit.addTo(this, amountToAdd));
  }

  @Override
  public D minus(long amountToSubtract, TemporalUnit unit) {
    if (amountToSubtract == Long.MIN_VALUE) {
      return castValid(plus(Long.MAX_VALUE, unit).plus(1, unit));
    }
    return plus(-amountToSubtract, unit);
  }

  abstract D plusYears(long yearsToAdd);

  abstract D plusMonths(long monthsToAdd);

  abstract D plusDays(long daysToAdd);

  D plusWeeks(long weeksToAdd) {
    return plusDays(Math.multiplyExact(weeksToAdd, 7));
  }

  long daysUntil(ChronoLocalDate end) {
    return end.toEpochDay() - toEpochDay();
  }

  long monthsUntil(ChronoLocalDate end) {
    ValueRange range = getChronology().range(MONTH_OF_YEAR);
    if (range.getMaximum() != 12) {
      throw new IllegalStateException("ChronoLocalDateImpl only supports Chronologies with 12 months per year");
    }
    long packed1 = getLong(PROLEPTIC_MONTH) * 32L + get(DAY_OF_MONTH);
    long packed2 = end.getLong(PROLEPTIC_MONTH) * 32L + end.get(DAY_OF_MONTH);
    return (packed2 - packed1) / 32;
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    Objects.requireNonNull(endExclusive, "endExclusive");
    ChronoLocalDate end = getChronology().date(endExclusive);
    if (unit instanceof ChronoUnit) {
      switch ((ChronoUnit) unit) {
        case DAYS:
          return daysUntil(end);
        case WEEKS:
          return daysUntil(end) / 7;
        case MONTHS:
          return monthsUntil(end);
        case YEARS:
          return monthsUntil(end) / 12;
        case DECADES:
          return monthsUntil(end) / 120;
        case CENTURIES:
          return monthsUntil(end) / 1200;
        case MILLENNIA:
          return monthsUntil(end) / 12000;
        case ERAS:
          return end.getLong(ERA) - getLong(ERA);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    Objects.requireNonNull(unit, "unit");
    return unit.between(this, end);
  }

  @Override
  public java.time.chrono.ChronoLocalDateTime<?> atTime(java.time.LocalTime localTime) {
    return ChronoLocalDateTimeImpl.of(this, localTime);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ChronoLocalDate) {
      return compareTo((ChronoLocalDate) obj) == 0;
    }
    return false;
  }

  @Override
  public int hashCode() {
    long epDay = toEpochDay();
    return getChronology().hashCode() ^ ((int) (epDay ^ (epDay >>> 32)));
  }

  @Override
  public String toString() {
    long yoe = getLong(YEAR_OF_ERA);
    long moy = getLong(MONTH_OF_YEAR);
    long dom = getLong(DAY_OF_MONTH);
    StringBuilder buf = new StringBuilder(30);
    buf.append(getChronology().toString()).append(" ").append(getEra()).append(" ").append(yoe)
        .append(moy < 10 ? "-0" : "-").append(moy).append(dom < 10 ? "-0" : "-").append(dom);
    return buf.toString();
  }
}
