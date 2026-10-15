/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Objects;

public final class LocalDate implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {
  public static final LocalDate MIN = LocalDate.of(-999999999, 1, 1);
  public static final LocalDate MAX = LocalDate.of(999999999, 12, 31);
  public static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);

  private static final int[] DAYS_TO_MONTH = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
  private static final int[] DAYS_TO_MONTH_LEAP = {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

  private final int year;
  private final short month;
  private final short day;

  private LocalDate(int year, int month, int day) {
    this.year = year;
    this.month = (short) month;
    this.day = (short) day;
  }

  public static LocalDate now() {
    return now(Clock.systemDefaultZone());
  }

  public static LocalDate now(ZoneId zone) {
    return now(Clock.system(zone));
  }

  public static LocalDate now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    Instant now = clock.instant();
    ZoneOffset offset = clock.getZone().getRules().getOffset(now);
    long epochSec = now.getEpochSecond() + offset.getTotalSeconds();
    long epochDay = Math.floorDiv(epochSec, 86400L);
    return ofEpochDay(epochDay);
  }

  public static LocalDate of(int year, Month month, int dayOfMonth) {
    Objects.requireNonNull(month, "month");
    return of(year, month.getValue(), dayOfMonth);
  }

  public static LocalDate of(int year, int month, int dayOfMonth) {
    ChronoField.YEAR.checkValidValue(year);
    ChronoField.MONTH_OF_YEAR.checkValidValue(month);
    ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
    int maxDays = lengthOfMonth(year, month);
    if (dayOfMonth > maxDays) {
      if (dayOfMonth == 29) {
        throw new DateTimeException("Invalid date 'February 29' as '" + year + "' is not a leap year");
      } else {
        throw new DateTimeException("Invalid date '" + Month.of(month).name() + " " + dayOfMonth + "'");
      }
    }
    return new LocalDate(year, month, dayOfMonth);
  }

  public static LocalDate ofYearDay(int year, int dayOfYear) {
    ChronoField.YEAR.checkValidValue(year);
    ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
    boolean leap = isLeapYear(year);
    if (dayOfYear == 366 && !leap) {
      throw new DateTimeException("Invalid date 'DayOfYear 366' as '" + year + "' is not a leap year");
    }
    int[] table = leap ? DAYS_TO_MONTH_LEAP : DAYS_TO_MONTH;
    int m = 1;
    for (int i = 11; i >= 0; --i) {
      if (dayOfYear > table[i]) {
        m = i + 1;
        break;
      }
    }
    int d = dayOfYear - table[m - 1];
    return new LocalDate(year, m, d);
  }

  public static LocalDate ofEpochDay(long epochDay) {
    long zeroDay = epochDay + 719468L;
    long era = (zeroDay >= 0 ? zeroDay : zeroDay - 146096L) / 146097L;
    long doe = zeroDay - era * 146097L;
    long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365L;
    long y = yoe + era * 400L;
    long doy = doe - (365L * yoe + yoe / 4L - yoe / 100L);
    long mp = (5L * doy + 2L) / 153L;
    int d = (int) (doy - (153L * mp + 2L) / 5L + 1L);
    int m = (int) (mp < 10L ? mp + 3L : mp - 9L);
    int year = (int) (m <= 2 ? y + 1L : y);
    return LocalDate.of(year, m, d);
  }

  public static LocalDate from(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    if (temporal instanceof LocalDate) {
      return (LocalDate) temporal;
    }
    if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
      return ofEpochDay(temporal.getLong(ChronoField.EPOCH_DAY));
    }
    int year = temporal.get(ChronoField.YEAR);
    int month = temporal.get(ChronoField.MONTH_OF_YEAR);
    int day = temporal.get(ChronoField.DAY_OF_MONTH);
    return of(year, month, day);
  }

  public static boolean isLeapYear(long year) {
    return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
  }

  public boolean isLeapYear() {
    return isLeapYear(year);
  }

  public static int lengthOfMonth(int year, int month) {
    switch (month) {
      case 2:
        return isLeapYear(year) ? 29 : 28;
      case 4:
      case 6:
      case 9:
      case 11:
        return 30;
      default:
        return 31;
    }
  }

  public int lengthOfMonth() {
    return lengthOfMonth(year, month);
  }

  public int lengthOfYear() {
    return isLeapYear() ? 366 : 365;
  }

  public int getYear() {
    return year;
  }

  public int getMonthValue() {
    return month;
  }

  public Month getMonth() {
    return Month.of(month);
  }

  public int getDayOfMonth() {
    return day;
  }

  public int getDayOfYear() {
    int[] table = isLeapYear() ? DAYS_TO_MONTH_LEAP : DAYS_TO_MONTH;
    return table[month - 1] + day;
  }

  public DayOfWeek getDayOfWeek() {
    int dow0 = (int) Math.floorMod(toEpochDay() + 3, 7L);
    return DayOfWeek.of(dow0 + 1);
  }

  public long toEpochDay() {
    long y = year;
    long m = month;
    long total = 365L * y;
    if (y >= 0) {
      total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
    } else {
      total -= y / -4 - y / -100 + y / -400;
    }
    total += ((367 * m - 362) / 12);
    total += day - 1;
    if (m > 2) {
      total--;
      if (!isLeapYear(y)) {
        total--;
      }
    }
    return total - 719528L;
  }

  @Override
  public boolean isSupported(TemporalField field) {
    if (field instanceof ChronoField) {
      return field.isDateBased();
    }
    return field != null && field.isSupportedBy(this);
  }

  @Override
  public boolean isSupported(TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      return unit.isDateBased();
    }
    return unit != null && unit.isSupportedBy(this);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field instanceof ChronoField) {
      switch ((ChronoField) field) {
        case DAY_OF_WEEK:
          return getDayOfWeek().getValue();
        case ALIGNED_DAY_OF_WEEK_IN_MONTH:
          return ((day - 1) % 7) + 1;
        case ALIGNED_DAY_OF_WEEK_IN_YEAR:
          return ((getDayOfYear() - 1) % 7) + 1;
        case DAY_OF_MONTH:
          return day;
        case DAY_OF_YEAR:
          return getDayOfYear();
        case EPOCH_DAY:
          return toEpochDay();
        case ALIGNED_WEEK_OF_MONTH:
          return ((day - 1) / 7) + 1;
        case ALIGNED_WEEK_OF_YEAR:
          return ((getDayOfYear() - 1) / 7) + 1;
        case MONTH_OF_YEAR:
          return month;
        case PROLEPTIC_MONTH:
          return ((long) year) * 12L + (month - 1);
        case YEAR_OF_ERA:
          return (year >= 1 ? year : 1 - year);
        case YEAR:
          return year;
        case ERA:
          return (year >= 1 ? 1 : 0);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return field.getFrom(this);
  }

  @Override
  public LocalDate with(TemporalField field, long newValue) {
    if (field instanceof ChronoField) {
      ChronoField cf = (ChronoField) field;
      cf.checkValidValue(newValue);
      switch (cf) {
        case DAY_OF_WEEK:
          return plusDays(newValue - getDayOfWeek().getValue());
        case DAY_OF_MONTH:
          return withDayOfMonth((int) newValue);
        case DAY_OF_YEAR:
          return withDayOfYear((int) newValue);
        case EPOCH_DAY:
          return LocalDate.ofEpochDay(newValue);
        case MONTH_OF_YEAR:
          return withMonth((int) newValue);
        case PROLEPTIC_MONTH:
          return plusMonths(newValue - getLong(ChronoField.PROLEPTIC_MONTH));
        case YEAR_OF_ERA:
          return withYear((int) (year >= 1 ? newValue : 1 - newValue));
        case YEAR:
          return withYear((int) newValue);
        case ERA:
          return (getLong(ChronoField.ERA) == newValue ? this : withYear(1 - year));
        default:
          throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
      }
    }
    return (LocalDate) field.adjustInto(this, newValue);
  }

  public LocalDate withYear(int year) {
    if (this.year == year) {
      return this;
    }
    ChronoField.YEAR.checkValidValue(year);
    int d = Math.min(day, lengthOfMonth(year, month));
    return new LocalDate(year, month, d);
  }

  public LocalDate withMonth(int month) {
    if (this.month == month) {
      return this;
    }
    ChronoField.MONTH_OF_YEAR.checkValidValue(month);
    int d = Math.min(day, lengthOfMonth(year, month));
    return new LocalDate(year, month, d);
  }

  public LocalDate withDayOfMonth(int dayOfMonth) {
    if (this.day == dayOfMonth) {
      return this;
    }
    return of(year, month, dayOfMonth);
  }

  public LocalDate withDayOfYear(int dayOfYear) {
    if (this.getDayOfYear() == dayOfYear) {
      return this;
    }
    return ofYearDay(year, dayOfYear);
  }

  @Override
  public LocalDate plus(TemporalAmount amount) {
    return (LocalDate) amount.addTo(this);
  }

  @Override
  public LocalDate plus(long amountToAdd, TemporalUnit unit) {
    if (unit instanceof ChronoUnit) {
      ChronoUnit f = (ChronoUnit) unit;
      switch (f) {
        case DAYS:
          return plusDays(amountToAdd);
        case WEEKS:
          return plusWeeks(amountToAdd);
        case MONTHS:
          return plusMonths(amountToAdd);
        case YEARS:
          return plusYears(amountToAdd);
        case DECADES:
          return plusYears(Math.multiplyExact(amountToAdd, 10L));
        case CENTURIES:
          return plusYears(Math.multiplyExact(amountToAdd, 100L));
        case MILLENNIA:
          return plusYears(Math.multiplyExact(amountToAdd, 1000L));
        case ERAS:
          return with(ChronoField.ERA, Math.addExact(getLong(ChronoField.ERA), amountToAdd));
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return (LocalDate) unit.addTo(this, amountToAdd);
  }

  public LocalDate plusYears(long yearsToAdd) {
    if (yearsToAdd == 0) {
      return this;
    }
    int newYear = ChronoField.YEAR.checkValidIntValue(year + yearsToAdd);
    int d = Math.min(day, lengthOfMonth(newYear, month));
    return new LocalDate(newYear, month, d);
  }

  public LocalDate plusMonths(long monthsToAdd) {
    if (monthsToAdd == 0) {
      return this;
    }
    long monthCount = ((long) year) * 12L + (month - 1);
    long calcMonths = monthCount + monthsToAdd;
    int newYear = ChronoField.YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12L));
    int newMonth = (int) Math.floorMod(calcMonths, 12L) + 1;
    int d = Math.min(day, lengthOfMonth(newYear, newMonth));
    return new LocalDate(newYear, newMonth, d);
  }

  public LocalDate plusWeeks(long weeksToAdd) {
    return plusDays(Math.multiplyExact(weeksToAdd, 7L));
  }

  public LocalDate plusDays(long daysToAdd) {
    if (daysToAdd == 0) {
      return this;
    }
    long mjDay = Math.addExact(toEpochDay(), daysToAdd);
    return LocalDate.ofEpochDay(mjDay);
  }

  @Override
  public LocalDate minus(TemporalAmount amount) {
    return (LocalDate) amount.subtractFrom(this);
  }

  @Override
  public LocalDate minus(long amountToSubtract, TemporalUnit unit) {
    return (amountToSubtract == Long.MIN_VALUE)
        ? plus(Long.MAX_VALUE, unit).plus(1, unit)
        : plus(-amountToSubtract, unit);
  }

  public LocalDate minusYears(long yearsToSubtract) {
    return (yearsToSubtract == Long.MIN_VALUE)
        ? plusYears(Long.MAX_VALUE).plusYears(1)
        : plusYears(-yearsToSubtract);
  }

  public LocalDate minusMonths(long monthsToSubtract) {
    return (monthsToSubtract == Long.MIN_VALUE)
        ? plusMonths(Long.MAX_VALUE).plusMonths(1)
        : plusMonths(-monthsToSubtract);
  }

  public LocalDate minusWeeks(long weeksToSubtract) {
    return (weeksToSubtract == Long.MIN_VALUE)
        ? plusWeeks(Long.MAX_VALUE).plusWeeks(1)
        : plusWeeks(-weeksToSubtract);
  }

  public LocalDate minusDays(long daysToSubtract) {
    return (daysToSubtract == Long.MIN_VALUE)
        ? plusDays(Long.MAX_VALUE).plusDays(1)
        : plusDays(-daysToSubtract);
  }

  @Override
  public Temporal adjustInto(Temporal temporal) {
    return temporal.with(ChronoField.EPOCH_DAY, toEpochDay());
  }

  @Override
  public long until(Temporal endExclusive, TemporalUnit unit) {
    LocalDate end = LocalDate.from(endExclusive);
    if (unit instanceof ChronoUnit) {
      switch ((ChronoUnit) unit) {
        case DAYS:
          return daysUntil(end);
        case WEEKS:
          return daysUntil(end) / 7L;
        case MONTHS:
          return monthsUntil(end);
        case YEARS:
          return monthsUntil(end) / 12L;
        case DECADES:
          return monthsUntil(end) / 120L;
        case CENTURIES:
          return monthsUntil(end) / 1200L;
        case MILLENNIA:
          return monthsUntil(end) / 12000L;
        case ERAS:
          return end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA);
        default:
          throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
      }
    }
    return unit.between(this, end);
  }

  private long daysUntil(LocalDate end) {
    return end.toEpochDay() - toEpochDay();
  }

  private long monthsUntil(LocalDate end) {
    long packed1 = getLong(ChronoField.PROLEPTIC_MONTH) * 32L + getDayOfMonth();
    long packed2 = end.getLong(ChronoField.PROLEPTIC_MONTH) * 32L + end.getDayOfMonth();
    return (packed2 - packed1) / 32L;
  }

  public LocalDateTime atTime(LocalTime time) {
    return LocalDateTime.of(this, time);
  }

  public LocalDateTime atTime(int hour, int minute) {
    return atTime(LocalTime.of(hour, minute));
  }

  public LocalDateTime atTime(int hour, int minute, int second) {
    return atTime(LocalTime.of(hour, minute, second));
  }

  public LocalDateTime atTime(int hour, int minute, int second, int nanoOfSecond) {
    return atTime(LocalTime.of(hour, minute, second, nanoOfSecond));
  }

  public LocalDateTime atStartOfDay() {
    return LocalDateTime.of(this, LocalTime.MIDNIGHT);
  }

  @Override
  public IsoChronology getChronology() {
    return IsoChronology.INSTANCE;
  }

  @Override
  public int compareTo(ChronoLocalDate other) {
    if (other instanceof LocalDate) {
      return compareTo((LocalDate) other);
    }
    return ChronoLocalDate.super.compareTo(other);
  }

  public int compareTo(LocalDate other) {
    int cmp = year - other.year;
    if (cmp == 0) {
      cmp = month - other.month;
      if (cmp == 0) {
        cmp = day - other.day;
      }
    }
    return cmp;
  }

  @Override
  public boolean isAfter(ChronoLocalDate other) {
    if (other instanceof LocalDate) {
      return compareTo((LocalDate) other) > 0;
    }
    return ChronoLocalDate.super.isAfter(other);
  }

  @Override
  public boolean isBefore(ChronoLocalDate other) {
    if (other instanceof LocalDate) {
      return compareTo((LocalDate) other) < 0;
    }
    return ChronoLocalDate.super.isBefore(other);
  }

  @Override
  public boolean isEqual(ChronoLocalDate other) {
    if (other instanceof LocalDate) {
      return compareTo((LocalDate) other) == 0;
    }
    return ChronoLocalDate.super.isEqual(other);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LocalDate) {
      LocalDate other = (LocalDate) obj;
      return year == other.year && month == other.month && day == other.day;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int yearValue = year;
    int monthValue = month;
    int dayValue = day;
    return (yearValue & 0xFFFFF800) ^ ((yearValue << 11) + (monthValue << 6) + (dayValue));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(10);
    if (year < 0) {
      sb.append('-');
      appendZeroPadded(sb, -year, 4);
    } else if (year > 9999) {
      sb.append('+');
      sb.append(year);
    } else {
      appendZeroPadded(sb, year, 4);
    }
    sb.append('-');
    appendZeroPadded(sb, month, 2);
    sb.append('-');
    appendZeroPadded(sb, day, 2);
    return sb.toString();
  }

  public String format(DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.format(this);
  }

  public static LocalDate parse(CharSequence text) {
    return parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
    Objects.requireNonNull(formatter, "formatter");
    return formatter.parse(text, LocalDate::from);
  }

  private static void appendZeroPadded(StringBuilder sb, int value, int digits) {
    String s = Integer.toString(value);
    for (int i = s.length(); i < digits; ++i) {
      sb.append('0');
    }
    sb.append(s);
  }
}
