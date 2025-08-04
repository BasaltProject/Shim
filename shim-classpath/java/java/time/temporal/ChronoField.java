/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

public enum ChronoField implements TemporalField {
  NANO_OF_SECOND("NanoOfSecond", ChronoUnit.NANOS, ChronoUnit.SECONDS),
  NANO_OF_DAY("NanoOfDay", ChronoUnit.NANOS, ChronoUnit.DAYS),
  MICRO_OF_SECOND("MicroOfSecond", ChronoUnit.MICROS, ChronoUnit.SECONDS),
  MICRO_OF_DAY("MicroOfDay", ChronoUnit.MICROS, ChronoUnit.DAYS),
  MILLI_OF_SECOND("MilliOfSecond", ChronoUnit.MILLIS, ChronoUnit.SECONDS),
  MILLI_OF_DAY("MilliOfDay", ChronoUnit.MILLIS, ChronoUnit.DAYS),
  SECOND_OF_MINUTE("SecondOfMinute", ChronoUnit.SECONDS, ChronoUnit.MINUTES),
  SECOND_OF_DAY("SecondOfDay", ChronoUnit.SECONDS, ChronoUnit.DAYS),
  MINUTE_OF_HOUR("MinuteOfHour", ChronoUnit.MINUTES, ChronoUnit.HOURS),
  MINUTE_OF_DAY("MinuteOfDay", ChronoUnit.MINUTES, ChronoUnit.DAYS),
  HOUR_OF_AMPM("HourOfAmPm", ChronoUnit.HOURS, ChronoUnit.HALF_DAYS),
  CLOCK_HOUR_OF_AMPM("ClockHourOfAmPm", ChronoUnit.HOURS, ChronoUnit.HALF_DAYS),
  HOUR_OF_DAY("HourOfDay", ChronoUnit.HOURS, ChronoUnit.DAYS),
  CLOCK_HOUR_OF_DAY("ClockHourOfDay", ChronoUnit.HOURS, ChronoUnit.DAYS),
  AMPM_OF_DAY("AmPmOfDay", ChronoUnit.HALF_DAYS, ChronoUnit.DAYS),
  DAY_OF_WEEK("DayOfWeek", ChronoUnit.DAYS, ChronoUnit.WEEKS),
  ALIGNED_DAY_OF_WEEK_IN_MONTH("AlignedDayOfWeekInMonth", ChronoUnit.DAYS, ChronoUnit.WEEKS),
  ALIGNED_DAY_OF_WEEK_IN_YEAR("AlignedDayOfWeekInYear", ChronoUnit.DAYS, ChronoUnit.WEEKS),
  DAY_OF_MONTH("DayOfMonth", ChronoUnit.DAYS, ChronoUnit.MONTHS),
  DAY_OF_YEAR("DayOfYear", ChronoUnit.DAYS, ChronoUnit.YEARS),
  EPOCH_DAY("EpochDay", ChronoUnit.DAYS, ChronoUnit.FOREVER),
  ALIGNED_WEEK_OF_MONTH("AlignedWeekOfMonth", ChronoUnit.WEEKS, ChronoUnit.MONTHS),
  ALIGNED_WEEK_OF_YEAR("AlignedWeekOfYear", ChronoUnit.WEEKS, ChronoUnit.YEARS),
  MONTH_OF_YEAR("MonthOfYear", ChronoUnit.MONTHS, ChronoUnit.YEARS),
  PROLEPTIC_MONTH("ProlepticMonth", ChronoUnit.MONTHS, ChronoUnit.FOREVER),
  YEAR_OF_ERA("YearOfEra", ChronoUnit.YEARS, ChronoUnit.FOREVER),
  YEAR("Year", ChronoUnit.YEARS, ChronoUnit.FOREVER),
  ERA("Era", ChronoUnit.ERAS, ChronoUnit.FOREVER),
  INSTANT_SECONDS("InstantSeconds", ChronoUnit.SECONDS, ChronoUnit.FOREVER),
  OFFSET_SECONDS("OffsetSeconds", ChronoUnit.SECONDS, ChronoUnit.FOREVER);

  private final String name;
  private final TemporalUnit baseUnit;
  private final TemporalUnit rangeUnit;

  private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit) {
    this.name = name;
    this.baseUnit = baseUnit;
    this.rangeUnit = rangeUnit;
  }

  @Override
  public TemporalUnit getBaseUnit() {
    return baseUnit;
  }

  @Override
  public TemporalUnit getRangeUnit() {
    return rangeUnit;
  }

  @Override
  public boolean isDateBased() {
    return compareTo(DAY_OF_WEEK) >= 0 && compareTo(ERA) <= 0;
  }

  @Override
  public boolean isTimeBased() {
    return compareTo(DAY_OF_WEEK) < 0;
  }

  @Override
  public boolean isSupportedBy(TemporalAccessor temporal) {
    return temporal.isSupported(this);
  }

  @Override
  public long getFrom(TemporalAccessor temporal) {
    return temporal.getLong(this);
  }

  @Override
  public <R extends Temporal> R adjustInto(R temporal, long newValue) {
    return (R) temporal.with(this, newValue);
  }

  public long checkValidValue(long value) {
    return value;
  }

  public int checkValidIntValue(long value) {
    return (int) value;
  }

  @Override
  public String toString() {
    return name;
  }
}
