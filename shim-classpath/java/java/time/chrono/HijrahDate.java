/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
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

public final class HijrahDate extends ChronoLocalDateImpl<HijrahDate> implements ChronoLocalDate, Serializable {
  private static final long serialVersionUID = -5207853542612002020L;

  private final transient HijrahChronology chrono;
  private final int prolepticYear;
  private final int monthOfYear;
  private final int dayOfMonth;

  static HijrahDate of(HijrahChronology chrono, int prolepticYear, int monthOfYear, int dayOfMonth) {
    return new HijrahDate(chrono, prolepticYear, monthOfYear, dayOfMonth);
  }

  static HijrahDate ofEpochDay(HijrahChronology chrono, long epochDay) {
    return new HijrahDate(chrono, epochDay);
  }

  public static HijrahDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static HijrahDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static HijrahDate now(Clock clock) {
    return HijrahChronology.INSTANCE.dateNow(clock);
  }

  public static HijrahDate of(int prolepticYear, int month, int dayOfMonth) {
    return HijrahChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
  }

  public static HijrahDate from(TemporalAccessor temporal) {
    return HijrahChronology.INSTANCE.date(temporal);
  }

  private HijrahDate(HijrahChronology chrono, int prolepticYear, int monthOfYear, int dayOfMonth) {
    chrono.getEpochDay(prolepticYear, monthOfYear, dayOfMonth);
    this.chrono = chrono;
    this.prolepticYear = prolepticYear;
    this.monthOfYear = monthOfYear;
    this.dayOfMonth = dayOfMonth;
  }

  private HijrahDate(HijrahChronology chrono, long epochDay) {
    int[] dateInfo = chrono.getHijrahDateInfo(epochDay);
    this.chrono = chrono;
    this.prolepticYear = dateInfo[0];
    this.monthOfYear = dateInfo[1];
    this.dayOfMonth = dateInfo[2];
  }

  private Object readResolve() {
    return new HijrahDate(HijrahChronology.INSTANCE, prolepticYear, monthOfYear, dayOfMonth);
  }

  @Override
  public HijrahChronology getChronology() {
    return chrono;
  }

  @Override
  public HijrahEra getEra() {
    return HijrahEra.AH;
  }

  @Override
  public int lengthOfMonth() {
    return chrono.getMonthLength(prolepticYear, monthOfYear);
  }

  @Override
  public int lengthOfYear() {
    return chrono.getYearLength(prolepticYear);
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field instanceof ChronoField) {
      if (isSupported(field)) {
        ChronoField f = (ChronoField) field;
        switch (f) {
          case DAY_OF_MONTH:
            return ValueRange.of(1, lengthOfMonth());
          case DAY_OF_YEAR:
            return ValueRange.of(1, lengthOfYear());
          case ALIGNED_WEEK_OF_MONTH:
            return ValueRange.of(1, 5);
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
        case DAY_OF_WEEK:
          return getDayOfWeek();
        case ALIGNED_DAY_OF_WEEK_IN_MONTH:
          return ((dayOfMonth - 1) % 7) + 1;
        case ALIGNED_DAY_OF_WEEK_IN_YEAR:
          return ((getDayOfYear() - 1) % 7) + 1;
        case DAY_OF_MONTH:
          return this.dayOfMonth;
        case DAY_OF_YEAR:
          return this.getDayOfYear();
        case EPOCH_DAY:
          return toEpochDay();
        case ALIGNED_WEEK_OF_MONTH:
          return ((dayOfMonth - 1) / 7) + 1;
        case ALIGNED_WEEK_OF_YEAR:
          return ((getDayOfYear() - 1) / 7) + 1;
        case MONTH_OF_YEAR:
          return monthOfYear;
        case PROLEPTIC_MONTH:
          return getProlepticMonth();
        case YEAR_OF_ERA:
          return prolepticYear;
        case YEAR:
          return prolepticYear;
        case ERA:
          return getEraValue();
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return field.getFrom(this);
  }

  private int getDayOfWeek() {
    int dow0 = (int) Math.floorMod(toEpochDay() + 3, 7);
    return dow0 + 1;
  }

  private int getDayOfYear() {
    return chrono.getDayOfYear(prolepticYear, monthOfYear) + dayOfMonth;
  }

  private long getProlepticMonth() {
    return prolepticYear * 12L + monthOfYear - 1;
  }

  private int getEraValue() {
    return (prolepticYear > 1 ? 1 : 0);
  }

  @Override
  public boolean isLeapYear() {
    return chrono.isLeapYear(prolepticYear);
  }

  @Override
  public HijrahDate with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      chrono.range(f).checkValidValue(newValue, f);
      int nvalue = (int) newValue;
      switch (f) {
        case DAY_OF_WEEK:
          return plusDays(newValue - getDayOfWeek());
        case ALIGNED_DAY_OF_WEEK_IN_MONTH:
          return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH));
        case ALIGNED_DAY_OF_WEEK_IN_YEAR:
          return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
        case DAY_OF_MONTH:
          return resolvePreviousValid(prolepticYear, monthOfYear, nvalue);
        case DAY_OF_YEAR:
          return plusDays(Math.min(nvalue, lengthOfYear()) - getDayOfYear());
        case EPOCH_DAY:
          return new HijrahDate(chrono, newValue);
        case ALIGNED_WEEK_OF_MONTH:
          return plusDays((newValue - getLong(ChronoField.ALIGNED_WEEK_OF_MONTH)) * 7);
        case ALIGNED_WEEK_OF_YEAR:
          return plusDays((newValue - getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)) * 7);
        case MONTH_OF_YEAR:
          return resolvePreviousValid(prolepticYear, nvalue, dayOfMonth);
        case PROLEPTIC_MONTH:
          return plusMonths(newValue - getProlepticMonth());
        case YEAR_OF_ERA:
          return resolvePreviousValid(prolepticYear >= 1 ? nvalue : 1 - nvalue, monthOfYear, dayOfMonth);
        case YEAR:
          return resolvePreviousValid(nvalue, monthOfYear, dayOfMonth);
        case ERA:
          return resolvePreviousValid(1 - prolepticYear, monthOfYear, dayOfMonth);
        default:
          break;
      }
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }
    return (HijrahDate) ChronoLocalDateOps.ensureValid(getChronology(), field.adjustInto(this, newValue));
  }

  private HijrahDate resolvePreviousValid(int prolepticYear, int month, int day) {
    int monthDays = chrono.getMonthLengthForResolve(prolepticYear, month);
    if (day > monthDays) {
      day = monthDays;
    }
    return HijrahDate.of(chrono, prolepticYear, month, day);
  }

  public HijrahDate withVariant(HijrahChronology chronology) {
    if (chronology == chrono) {
      return this;
    }
    int monthDays = chronology.getDayOfYear(prolepticYear, monthOfYear);
    return HijrahDate.of(chronology, prolepticYear, monthOfYear, (dayOfMonth > monthDays) ? monthDays
        : dayOfMonth);
  }

  @Override
  public HijrahDate with(TemporalAdjuster adjuster) {
    return super.with(adjuster);
  }

  @Override
  public HijrahDate plus(TemporalAmount amount) {
    return super.plus(amount);
  }

  @Override
  public HijrahDate minus(TemporalAmount amount) {
    return super.minus(amount);
  }

  @Override
  public HijrahDate plus(long amountToAdd, TemporalUnit unit) {
    return super.plus(amountToAdd, unit);
  }

  @Override
  public HijrahDate minus(long amountToSubtract, TemporalUnit unit) {
    return super.minus(amountToSubtract, unit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<HijrahDate> atTime(LocalTime localTime) {
    return (ChronoLocalDateTime<HijrahDate>) super.atTime(localTime);
  }

  @Override
  public ChronoPeriod until(ChronoLocalDate endDate) {
    HijrahDate end = getChronology().date(endDate);
    long totalMonths = (end.prolepticYear - this.prolepticYear) * 12L + (end.monthOfYear - this.monthOfYear);
    int days = end.dayOfMonth - this.dayOfMonth;
    if (totalMonths > 0 && days < 0) {
      totalMonths--;
      HijrahDate calcDate = this.plusMonths(totalMonths);
      days = (int) (end.toEpochDay() - calcDate.toEpochDay());
    } else if (totalMonths < 0 && days > 0) {
      totalMonths++;
      days -= end.lengthOfMonth();
    }
    long years = totalMonths / 12;
    int months = (int) (totalMonths % 12);
    return getChronology().period(Math.toIntExact(years), months, days);
  }

  @Override
  public long toEpochDay() {
    return chrono.getEpochDay(prolepticYear, monthOfYear, dayOfMonth);
  }

  @Override
  HijrahDate plusYears(long years) {
    if (years == 0) {
      return this;
    }
    int newYear = Math.addExact(this.prolepticYear, (int) years);
    return resolvePreviousValid(newYear, monthOfYear, dayOfMonth);
  }

  @Override
  HijrahDate plusMonths(long monthsToAdd) {
    if (monthsToAdd == 0) {
      return this;
    }
    int newMonth = this.monthOfYear - 1;
    newMonth = newMonth + (int) monthsToAdd;
    int years = newMonth / 12;
    newMonth = newMonth % 12;
    while (newMonth < 0) {
      newMonth += 12;
      years = Math.subtractExact(years, 1);
    }
    int newYear = Math.addExact(this.prolepticYear, years);
    if (newYear < HijrahChronology.MIN_YEAR || newYear > HijrahChronology.MAX_YEAR) {
      throw new DateTimeException("Invalid Hijrah year: " + newYear);
    }
    return resolvePreviousValid(newYear, newMonth + 1, dayOfMonth);
  }

  @Override
  HijrahDate plusWeeks(long weeksToAdd) {
    return super.plusWeeks(weeksToAdd);
  }

  @Override
  HijrahDate plusDays(long days) {
    return new HijrahDate(chrono, toEpochDay() + days);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof HijrahDate) {
      HijrahDate otherDate = (HijrahDate) obj;
      return this.prolepticYear == otherDate.prolepticYear && this.monthOfYear == otherDate.monthOfYear
          && this.dayOfMonth == otherDate.dayOfMonth && getChronology().equals(otherDate.getChronology());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int yearValue = prolepticYear;
    int monthValue = monthOfYear;
    int dayValue = dayOfMonth;
    return getChronology().getId().hashCode() ^ (yearValue & 0xFFFFF800) ^ ((yearValue << 11) + (monthValue << 6)
        + (dayValue));
  }
}
