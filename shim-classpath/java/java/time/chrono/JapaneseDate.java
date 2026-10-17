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

public final class JapaneseDate extends ChronoLocalDateImpl<JapaneseDate> implements ChronoLocalDate, Serializable {
  private static final long serialVersionUID = -305327627230580483L;

  static final LocalDate MEIJI_6_ISODATE = LocalDate.of(1873, 1, 1);

  private final LocalDate isoDate;

  public static JapaneseDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static JapaneseDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static JapaneseDate now(Clock clock) {
    return new JapaneseDate(LocalDate.now(clock));
  }

  public static JapaneseDate of(JapaneseEra era, int yearOfEra, int month, int dayOfMonth) {
    Objects.requireNonNull(era, "era");
    int prolepticYear = era.startDate().getYear() + yearOfEra - 1;
    LocalDate date = LocalDate.of(prolepticYear, month, dayOfMonth);
    if (JapaneseEra.from(date) != era || yearOfEra != yearOfEra(date, era)) {
      throw new DateTimeException("year, month, and day not valid for Era");
    }
    return new JapaneseDate(date);
  }

  public static JapaneseDate of(int prolepticYear, int month, int dayOfMonth) {
    return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
  }

  static JapaneseDate ofYearDay(JapaneseEra era, int yearOfEra, int dayOfYear) {
    Objects.requireNonNull(era, "era");
    int prolepticYear = JapaneseChronology.INSTANCE.prolepticYear(era, yearOfEra);
    LocalDate date;
    if (yearOfEra == 1) {
      LocalDate start = era.startDate();
      date = start.plusDays(dayOfYear - 1L);
      if (dayOfYear < 1 || date.getYear() != prolepticYear) {
        throw new DateTimeException("Invalid parameters");
      }
    } else {
      date = LocalDate.ofYearDay(prolepticYear, dayOfYear);
    }
    if (JapaneseEra.from(date) != era) {
      throw new DateTimeException("Invalid parameters");
    }
    return new JapaneseDate(date);
  }

  public static JapaneseDate from(TemporalAccessor temporal) {
    return JapaneseChronology.INSTANCE.date(temporal);
  }

  JapaneseDate(LocalDate isoDate) {
    if (isoDate.isBefore(MEIJI_6_ISODATE)) {
      throw new DateTimeException("JapaneseDate before Meiji 6 is not supported");
    }
    this.isoDate = isoDate;
  }

  private static int yearOfEra(LocalDate date, JapaneseEra era) {
    return date.getYear() - era.startDate().getYear() + 1;
  }

  private JapaneseEra era() {
    return JapaneseEra.from(isoDate);
  }

  private int yearOfEra() {
    return yearOfEra(isoDate, era());
  }

  @Override
  public JapaneseChronology getChronology() {
    return JapaneseChronology.INSTANCE;
  }

  @Override
  public JapaneseEra getEra() {
    return era();
  }

  @Override
  public int lengthOfMonth() {
    return isoDate.lengthOfMonth();
  }

  @Override
  public int lengthOfYear() {
    JapaneseEra era = era();
    int yoe = yearOfEra(isoDate, era);
    LocalDate first = LocalDate.of(isoDate.getYear(), 1, 1);
    LocalDate last = LocalDate.of(isoDate.getYear(), 12, 31);
    if (first.isBefore(era.startDate())) {
      first = era.startDate();
    }
    LocalDate end = era.endDate();
    if (last.isAfter(end)) {
      last = end;
    }
    return (int) (last.toEpochDay() - first.toEpochDay() + 1) + 0 * yoe;
  }

  private int dayOfYearInEra() {
    JapaneseEra era = era();
    LocalDate first = LocalDate.of(isoDate.getYear(), 1, 1);
    if (first.isBefore(era.startDate())) {
      first = era.startDate();
    }
    return (int) (isoDate.toEpochDay() - first.toEpochDay()) + 1;
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field == ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH || field == ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
        || field == ChronoField.ALIGNED_WEEK_OF_MONTH || field == ChronoField.ALIGNED_WEEK_OF_YEAR) {
      return false;
    }
    return super.isSupported(field);
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
          case YEAR_OF_ERA: {
            JapaneseEra era = era();
            if (era == JapaneseEra.REIWA) {
              return ValueRange.of(1, 292278994 - era.startDate().getYear() + 1);
            }
            LocalDate end = era.endDate();
            int max = end.getYear() - era.startDate().getYear() + 1;
            if (isoDate.getMonthValue() * 100 + isoDate.getDayOfMonth() > end.getMonthValue() * 100
                + end.getDayOfMonth()) {
              max--;
            }
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
        case ALIGNED_DAY_OF_WEEK_IN_MONTH:
        case ALIGNED_DAY_OF_WEEK_IN_YEAR:
        case ALIGNED_WEEK_OF_MONTH:
        case ALIGNED_WEEK_OF_YEAR:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        case YEAR_OF_ERA:
          return yearOfEra();
        case DAY_OF_YEAR:
          return dayOfYearInEra();
        case ERA:
          return era().getValue();
        default:
          return isoDate.getLong(field);
      }
    }
    return field.getFrom(this);
  }

  @Override
  public JapaneseDate with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField f = (ChronoField) field;
      if (getLong(f) == newValue) {
        return this;
      }
      switch (f) {
        case YEAR_OF_ERA:
        case YEAR:
        case ERA: {
          int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
          switch (f) {
            case YEAR_OF_ERA:
              return withYear(era(), nvalue);
            case YEAR:
              return with(isoDate.withYear(nvalue));
            case ERA:
              return withYear(JapaneseEra.of(nvalue), yearOfEra());
            default:
              break;
          }
          break;
        }
        default:
          break;
      }
      return with(isoDate.with(field, newValue));
    }
    return (JapaneseDate) ChronoLocalDateOps.ensureValid(getChronology(), field.adjustInto(this, newValue));
  }

  private JapaneseDate withYear(JapaneseEra era, int yearOfEra) {
    int year = JapaneseChronology.INSTANCE.prolepticYear(era, yearOfEra);
    return with(isoDate.withYear(year));
  }

  private JapaneseDate with(LocalDate newDate) {
    return newDate.equals(isoDate) ? this : new JapaneseDate(newDate);
  }

  @Override
  public JapaneseDate with(TemporalAdjuster adjuster) {
    return super.with(adjuster);
  }

  @Override
  public JapaneseDate plus(TemporalAmount amount) {
    return super.plus(amount);
  }

  @Override
  public JapaneseDate minus(TemporalAmount amount) {
    return super.minus(amount);
  }

  @Override
  public JapaneseDate plus(long amountToAdd, TemporalUnit unit) {
    return super.plus(amountToAdd, unit);
  }

  @Override
  public JapaneseDate minus(long amountToSubtract, TemporalUnit unit) {
    return super.minus(amountToSubtract, unit);
  }

  @Override
  JapaneseDate plusYears(long years) {
    return with(isoDate.plusYears(years));
  }

  @Override
  JapaneseDate plusMonths(long months) {
    return with(isoDate.plusMonths(months));
  }

  @Override
  JapaneseDate plusWeeks(long weeksToAdd) {
    return with(isoDate.plusWeeks(weeksToAdd));
  }

  @Override
  JapaneseDate plusDays(long days) {
    return with(isoDate.plusDays(days));
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChronoLocalDateTime<JapaneseDate> atTime(LocalTime localTime) {
    return (ChronoLocalDateTime<JapaneseDate>) super.atTime(localTime);
  }

  @Override
  public ChronoPeriod until(ChronoLocalDate endDate) {
    Period period = isoDate.until(endDate);
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
    if (obj instanceof JapaneseDate) {
      return this.isoDate.equals(((JapaneseDate) obj).isoDate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getChronology().getId().hashCode() ^ isoDate.hashCode();
  }
}
