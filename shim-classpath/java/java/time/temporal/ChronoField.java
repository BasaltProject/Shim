/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public enum ChronoField implements TemporalField {
  NANO_OF_SECOND("NanoOfSecond", ChronoUnit.NANOS, ChronoUnit.SECONDS, ValueRange.of(0, 999_999_999)),
  NANO_OF_DAY("NanoOfDay", ChronoUnit.NANOS, ChronoUnit.DAYS, ValueRange.of(0, 86400L * 1000_000_000L - 1)),
  MICRO_OF_SECOND("MicroOfSecond", ChronoUnit.MICROS, ChronoUnit.SECONDS, ValueRange.of(0, 999_999)),
  MICRO_OF_DAY("MicroOfDay", ChronoUnit.MICROS, ChronoUnit.DAYS, ValueRange.of(0, 86400L * 1000_000L - 1)),
  MILLI_OF_SECOND("MilliOfSecond", ChronoUnit.MILLIS, ChronoUnit.SECONDS, ValueRange.of(0, 999)),
  MILLI_OF_DAY("MilliOfDay", ChronoUnit.MILLIS, ChronoUnit.DAYS, ValueRange.of(0, 86400L * 1000L - 1)),
  SECOND_OF_MINUTE("SecondOfMinute", ChronoUnit.SECONDS, ChronoUnit.MINUTES, ValueRange.of(0, 59), "second"),
  SECOND_OF_DAY("SecondOfDay", ChronoUnit.SECONDS, ChronoUnit.DAYS, ValueRange.of(0, 86400L - 1)),
  MINUTE_OF_HOUR("MinuteOfHour", ChronoUnit.MINUTES, ChronoUnit.HOURS, ValueRange.of(0, 59), "minute"),
  MINUTE_OF_DAY("MinuteOfDay", ChronoUnit.MINUTES, ChronoUnit.DAYS, ValueRange.of(0, (24 * 60) - 1)),
  HOUR_OF_AMPM("HourOfAmPm", ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ValueRange.of(0, 11)),
  CLOCK_HOUR_OF_AMPM("ClockHourOfAmPm", ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ValueRange.of(1, 12)),
  HOUR_OF_DAY("HourOfDay", ChronoUnit.HOURS, ChronoUnit.DAYS, ValueRange.of(0, 23), "hour"),
  CLOCK_HOUR_OF_DAY("ClockHourOfDay", ChronoUnit.HOURS, ChronoUnit.DAYS, ValueRange.of(1, 24)),
  AMPM_OF_DAY("AmPmOfDay", ChronoUnit.HALF_DAYS, ChronoUnit.DAYS, ValueRange.of(0, 1), "AM/PM"),
  DAY_OF_WEEK("DayOfWeek", ChronoUnit.DAYS, ChronoUnit.WEEKS, ValueRange.of(1, 7), "day of the week"),
  ALIGNED_DAY_OF_WEEK_IN_MONTH("AlignedDayOfWeekInMonth", ChronoUnit.DAYS, ChronoUnit.WEEKS, ValueRange.of(1, 7)),
  ALIGNED_DAY_OF_WEEK_IN_YEAR("AlignedDayOfWeekInYear", ChronoUnit.DAYS, ChronoUnit.WEEKS, ValueRange.of(1, 7)),
  DAY_OF_MONTH("DayOfMonth", ChronoUnit.DAYS, ChronoUnit.MONTHS, ValueRange.of(1, 28, 31)),
  DAY_OF_YEAR("DayOfYear", ChronoUnit.DAYS, ChronoUnit.YEARS, ValueRange.of(1, 365, 366)),
  EPOCH_DAY("EpochDay", ChronoUnit.DAYS, ChronoUnit.FOREVER, ValueRange.of(-365243219162L, 365241780471L)),
  ALIGNED_WEEK_OF_MONTH("AlignedWeekOfMonth", ChronoUnit.WEEKS, ChronoUnit.MONTHS, ValueRange.of(1, 4, 5)),
  ALIGNED_WEEK_OF_YEAR("AlignedWeekOfYear", ChronoUnit.WEEKS, ChronoUnit.YEARS, ValueRange.of(1, 53)),
  MONTH_OF_YEAR("MonthOfYear", ChronoUnit.MONTHS, ChronoUnit.YEARS, ValueRange.of(1, 12), "month"),
  PROLEPTIC_MONTH("ProlepticMonth", ChronoUnit.MONTHS, ChronoUnit.FOREVER, ValueRange.of(-999_999_999L * 12L,
      999_999_999L * 12L + 11)),
  YEAR_OF_ERA("YearOfEra", ChronoUnit.YEARS, ChronoUnit.FOREVER, ValueRange.of(1, 999_999_999, 1_000_000_000)),
  YEAR("Year", ChronoUnit.YEARS, ChronoUnit.FOREVER, ValueRange.of(-999_999_999, 999_999_999), "year"),
  ERA("Era", ChronoUnit.ERAS, ChronoUnit.FOREVER, ValueRange.of(0, 1), "era"),
  INSTANT_SECONDS("InstantSeconds", ChronoUnit.SECONDS, ChronoUnit.FOREVER, ValueRange.of(-31557014167219200L,
      31556889864403199L)),
  OFFSET_SECONDS("OffsetSeconds", ChronoUnit.SECONDS, ChronoUnit.FOREVER, ValueRange.of(-18 * 3600, 18 * 3600));

  private final String name;
  private final TemporalUnit baseUnit;
  private final TemporalUnit rangeUnit;
  private final ValueRange range;
  private final String displayNameKey;

  private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range) {
    this(name, baseUnit, rangeUnit, range, null);
  }

  private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range,
      String displayNameKey) {
    this.name = name;
    this.baseUnit = baseUnit;
    this.rangeUnit = rangeUnit;
    this.range = range;
    this.displayNameKey = displayNameKey;
  }

  @Override
  public String getDisplayName(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    return displayNameKey != null ? displayNameKey : name;
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
  public ValueRange range() {
    return range;
  }

  @Override
  public boolean isDateBased() {
    return ordinal() >= DAY_OF_WEEK.ordinal() && ordinal() <= ERA.ordinal();
  }

  @Override
  public boolean isTimeBased() {
    return ordinal() < DAY_OF_WEEK.ordinal();
  }

  public long checkValidValue(long value) {
    return range().checkValidValue(value, this);
  }

  public int checkValidIntValue(long value) {
    return range().checkValidIntValue(value, this);
  }

  @Override
  public boolean isSupportedBy(TemporalAccessor temporal) {
    return temporal.isSupported(this);
  }

  @Override
  public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
    return temporal.range(this);
  }

  @Override
  public long getFrom(TemporalAccessor temporal) {
    return temporal.getLong(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Temporal> R adjustInto(R temporal, long newValue) {
    return (R) temporal.with(this, newValue);
  }

  @Override
  public TemporalAccessor resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal,
      ResolverStyle resolverStyle) {
    return null;
  }

  @Override
  public String toString() {
    return name;
  }
}
