/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.FOREVER;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WeekFields implements Serializable {
  private static final long serialVersionUID = -1177360819670808121L;

  private static final ConcurrentMap<String, WeekFields> CACHE = new ConcurrentHashMap<String, WeekFields>(4, 0.75f, 2);

  public static final WeekFields ISO = new WeekFields(DayOfWeek.MONDAY, 4);
  public static final WeekFields SUNDAY_START = WeekFields.of(DayOfWeek.SUNDAY, 1);
  public static final TemporalUnit WEEK_BASED_YEARS = IsoFields.WEEK_BASED_YEARS;

  private static final String kMonday4 = " AD AN AT AX BE BG CH CZ DE DK EE ES FI FJ FO FR GB GF GG GI GP GR HU IE IM"
      + " IS IT JE LI LT LU MC MQ NL NO PL RE RU SE SJ SK SM VA ";
  private static final String kMonday1 = " AL AM AR AU AZ BA BM BN BY CL CM CN CR CY EC GE HR KG KZ LB LK LV MD ME MK"
      + " MN MY NZ RO RS SI TJ TM TR UA UY UZ VN XK ZA ";
  private static final String kSaturday1 = " AE AF BH DJ DZ EG IQ IR JO KW LY OM QA SD SY ";

  private final DayOfWeek firstDayOfWeek;
  private final int minimalDays;
  private final transient TemporalField dayOfWeek = ComputedDayOfField.ofDayOfWeekField(this);
  private final transient TemporalField weekOfMonth = ComputedDayOfField.ofWeekOfMonthField(this);
  private final transient TemporalField weekOfYear = ComputedDayOfField.ofWeekOfYearField(this);
  private final transient TemporalField weekOfWeekBasedYear = ComputedDayOfField.ofWeekOfWeekBasedYearField(this);
  private final transient TemporalField weekBasedYear = ComputedDayOfField.ofWeekBasedYearField(this);

  public static WeekFields of(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    String country = locale.getCountry();
    String key = " " + country + " ";
    DayOfWeek first = DayOfWeek.SUNDAY;
    int min = 1;
    if (country.length() > 0) {
      if (kMonday4.contains(key)) {
        first = DayOfWeek.MONDAY;
        min = 4;
      } else if (kMonday1.contains(key)) {
        first = DayOfWeek.MONDAY;
      } else if (kSaturday1.contains(key)) {
        first = DayOfWeek.SATURDAY;
      }
    }
    return WeekFields.of(first, min);
  }

  public static WeekFields of(DayOfWeek firstDayOfWeek, int minimalDaysInFirstWeek) {
    String key = firstDayOfWeek.toString() + minimalDaysInFirstWeek;
    WeekFields rules = CACHE.get(key);
    if (rules == null) {
      rules = new WeekFields(firstDayOfWeek, minimalDaysInFirstWeek);
      CACHE.putIfAbsent(key, rules);
      rules = CACHE.get(key);
    }
    return rules;
  }

  private WeekFields(DayOfWeek firstDayOfWeek, int minimalDaysInFirstWeek) {
    Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek");
    if (minimalDaysInFirstWeek < 1 || minimalDaysInFirstWeek > 7) {
      throw new IllegalArgumentException("Minimal number of days is invalid");
    }
    this.firstDayOfWeek = firstDayOfWeek;
    this.minimalDays = minimalDaysInFirstWeek;
  }

  public DayOfWeek getFirstDayOfWeek() {
    return firstDayOfWeek;
  }

  public int getMinimalDaysInFirstWeek() {
    return minimalDays;
  }

  public TemporalField dayOfWeek() {
    return dayOfWeek;
  }

  public TemporalField weekOfMonth() {
    return weekOfMonth;
  }

  public TemporalField weekOfYear() {
    return weekOfYear;
  }

  public TemporalField weekOfWeekBasedYear() {
    return weekOfWeekBasedYear;
  }

  public TemporalField weekBasedYear() {
    return weekBasedYear;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object instanceof WeekFields) {
      return hashCode() == object.hashCode();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return firstDayOfWeek.ordinal() * 7 + minimalDays;
  }

  @Override
  public String toString() {
    return "WeekFields[" + firstDayOfWeek + ',' + minimalDays + ']';
  }

  static class ComputedDayOfField implements TemporalField {
    static ComputedDayOfField ofDayOfWeekField(WeekFields weekDef) {
      return new ComputedDayOfField("DayOfWeek", weekDef, DAYS, WEEKS, DAY_OF_WEEK_RANGE);
    }

    static ComputedDayOfField ofWeekOfMonthField(WeekFields weekDef) {
      return new ComputedDayOfField("WeekOfMonth", weekDef, WEEKS, MONTHS, WEEK_OF_MONTH_RANGE);
    }

    static ComputedDayOfField ofWeekOfYearField(WeekFields weekDef) {
      return new ComputedDayOfField("WeekOfYear", weekDef, WEEKS, YEARS, WEEK_OF_YEAR_RANGE);
    }

    static ComputedDayOfField ofWeekOfWeekBasedYearField(WeekFields weekDef) {
      return new ComputedDayOfField("WeekOfWeekBasedYear", weekDef, WEEKS, IsoFields.WEEK_BASED_YEARS,
          WEEK_OF_WEEK_BASED_YEAR_RANGE);
    }

    static ComputedDayOfField ofWeekBasedYearField(WeekFields weekDef) {
      return new ComputedDayOfField("WeekBasedYear", weekDef, IsoFields.WEEK_BASED_YEARS, FOREVER,
          ChronoField.YEAR.range());
    }

    private static final ValueRange DAY_OF_WEEK_RANGE = ValueRange.of(1, 7);
    private static final ValueRange WEEK_OF_MONTH_RANGE = ValueRange.of(0, 1, 4, 6);
    private static final ValueRange WEEK_OF_YEAR_RANGE = ValueRange.of(0, 1, 52, 54);
    private static final ValueRange WEEK_OF_WEEK_BASED_YEAR_RANGE = ValueRange.of(1, 52, 53);

    private final String name;
    private final WeekFields weekDef;
    private final TemporalUnit baseUnit;
    private final TemporalUnit rangeUnit;
    private final ValueRange range;

    private ComputedDayOfField(String name, WeekFields weekDef, TemporalUnit baseUnit, TemporalUnit rangeUnit,
        ValueRange range) {
      this.name = name;
      this.weekDef = weekDef;
      this.baseUnit = baseUnit;
      this.rangeUnit = rangeUnit;
      this.range = range;
    }

    @Override
    public long getFrom(TemporalAccessor temporal) {
      if (rangeUnit == WEEKS) {
        return localizedDayOfWeek(temporal);
      } else if (rangeUnit == MONTHS) {
        return localizedWeekOfMonth(temporal);
      } else if (rangeUnit == YEARS) {
        return localizedWeekOfYear(temporal);
      } else if (rangeUnit == IsoFields.WEEK_BASED_YEARS) {
        return localizedWeekOfWeekBasedYear(temporal);
      } else if (rangeUnit == FOREVER) {
        return localizedWeekBasedYear(temporal);
      } else {
        throw new IllegalStateException("unreachable, rangeUnit: " + rangeUnit + ", this: " + this);
      }
    }

    private int localizedDayOfWeek(TemporalAccessor temporal) {
      int sow = weekDef.getFirstDayOfWeek().getValue();
      int isoDow = temporal.get(DAY_OF_WEEK);
      return Math.floorMod(isoDow - sow, 7) + 1;
    }

    private int localizedDayOfWeek(int isoDow) {
      int sow = weekDef.getFirstDayOfWeek().getValue();
      return Math.floorMod(isoDow - sow, 7) + 1;
    }

    private long localizedWeekOfMonth(TemporalAccessor temporal) {
      int dow = localizedDayOfWeek(temporal);
      int dom = temporal.get(DAY_OF_MONTH);
      int offset = startOfWeekOffset(dom, dow);
      return computeWeek(offset, dom);
    }

    private long localizedWeekOfYear(TemporalAccessor temporal) {
      int dow = localizedDayOfWeek(temporal);
      int doy = temporal.get(DAY_OF_YEAR);
      int offset = startOfWeekOffset(doy, dow);
      return computeWeek(offset, doy);
    }

    private int localizedWeekBasedYear(TemporalAccessor temporal) {
      int dow = localizedDayOfWeek(temporal);
      int year = temporal.get(YEAR);
      int doy = temporal.get(DAY_OF_YEAR);
      int offset = startOfWeekOffset(doy, dow);
      int week = computeWeek(offset, doy);
      if (week == 0) {
        return year - 1;
      } else {
        ValueRange dayRange = temporal.range(DAY_OF_YEAR);
        int yearLen = (int) dayRange.getMaximum();
        int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
        if (week >= newYearWeek) {
          return year + 1;
        }
      }
      return year;
    }

    private int localizedWeekOfWeekBasedYear(TemporalAccessor temporal) {
      int dow = localizedDayOfWeek(temporal);
      int doy = temporal.get(DAY_OF_YEAR);
      int offset = startOfWeekOffset(doy, dow);
      int week = computeWeek(offset, doy);
      if (week == 0) {
        ChronoLocalDate date = Chronology.from(temporal).date(temporal);
        date = date.minus(doy, DAYS);
        return localizedWeekOfWeekBasedYear(date);
      } else if (week > 50) {
        ValueRange dayRange = temporal.range(DAY_OF_YEAR);
        int yearLen = (int) dayRange.getMaximum();
        int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
        if (week >= newYearWeek) {
          week = week - newYearWeek + 1;
        }
      }
      return week;
    }

    private int startOfWeekOffset(int day, int dow) {
      int weekStart = Math.floorMod(day - dow, 7);
      int offset = -weekStart;
      if (weekStart + 1 > weekDef.getMinimalDaysInFirstWeek()) {
        offset = 7 - weekStart;
      }
      return offset;
    }

    private int computeWeek(int offset, int day) {
      return ((7 + offset + (day - 1)) / 7);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R adjustInto(R temporal, long newValue) {
      int newVal = range.checkValidIntValue(newValue, this);
      int currentVal = temporal.get(this);
      if (newVal == currentVal) {
        return temporal;
      }
      if (rangeUnit == FOREVER) {
        int idow = temporal.get(weekDef.dayOfWeek);
        int wowby = temporal.get(weekDef.weekOfWeekBasedYear);
        return (R) ofWeekBasedYear(Chronology.from(temporal), (int) newValue, wowby, idow);
      } else {
        return (R) temporal.plus(newVal - currentVal, baseUnit);
      }
    }

    @Override
    public TemporalAccessor resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal,
        ResolverStyle resolverStyle) {
      final long value = fieldValues.get(this).longValue();
      final int newValue = Math.toIntExact(value);
      if (rangeUnit == WEEKS) {
        final int checkedValue = range.checkValidIntValue(value, this);
        final int startDow = weekDef.getFirstDayOfWeek().getValue();
        long isoDow = Math.floorMod((startDow - 1) + (checkedValue - 1), 7) + 1;
        fieldValues.remove(this);
        fieldValues.put(DAY_OF_WEEK, isoDow);
        return null;
      }
      if (fieldValues.containsKey(DAY_OF_WEEK) == false) {
        return null;
      }
      int isoDow = DAY_OF_WEEK.checkValidIntValue(fieldValues.get(DAY_OF_WEEK));
      int dow = localizedDayOfWeek(isoDow);
      Chronology chrono = Chronology.from(partialTemporal);
      if (fieldValues.containsKey(YEAR)) {
        int year = YEAR.checkValidIntValue(fieldValues.get(YEAR));
        if (rangeUnit == MONTHS && fieldValues.containsKey(MONTH_OF_YEAR)) {
          long month = fieldValues.get(MONTH_OF_YEAR);
          return resolveWoM(fieldValues, chrono, year, month, newValue, dow, resolverStyle);
        }
        if (rangeUnit == YEARS) {
          return resolveWoY(fieldValues, chrono, year, newValue, dow, resolverStyle);
        }
      } else if ((rangeUnit == IsoFields.WEEK_BASED_YEARS || rangeUnit == FOREVER)
          && fieldValues.containsKey(weekDef.weekBasedYear) && fieldValues.containsKey(weekDef.weekOfWeekBasedYear)) {
        return resolveWBY(fieldValues, chrono, dow, resolverStyle);
      }
      return null;
    }

    private ChronoLocalDate resolveWoM(Map<TemporalField, Long> fieldValues, Chronology chrono, int year, long month,
        long wom, int localDow, ResolverStyle resolverStyle) {
      ChronoLocalDate date = chrono.date(year, 1, 1);
      if (resolverStyle == ResolverStyle.LENIENT) {
        date = date.plus(Math.subtractExact(month, 1), MONTHS);
        long weeks = Math.subtractExact(wom, localizedWeekOfMonth(date));
        int days = localDow - localizedDayOfWeek(date);
        date = date.plus(Math.addExact(Math.multiplyExact(weeks, 7), days), DAYS);
      } else {
        int monthValid = MONTH_OF_YEAR.checkValidIntValue(month);
        date = date.plus(monthValid - 1, MONTHS);
        int womInt = range.checkValidIntValue(wom, this);
        int weeks = (int) (womInt - localizedWeekOfMonth(date));
        int days = localDow - localizedDayOfWeek(date);
        date = date.plus(weeks * 7 + days, DAYS);
        if (resolverStyle == ResolverStyle.STRICT && date.getLong(MONTH_OF_YEAR) != month) {
          throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
        }
      }
      fieldValues.remove(this);
      fieldValues.remove(YEAR);
      fieldValues.remove(MONTH_OF_YEAR);
      fieldValues.remove(DAY_OF_WEEK);
      return date;
    }

    private ChronoLocalDate resolveWoY(Map<TemporalField, Long> fieldValues, Chronology chrono, int year, long woy,
        int localDow, ResolverStyle resolverStyle) {
      ChronoLocalDate date = chrono.date(year, 1, 1);
      if (resolverStyle == ResolverStyle.LENIENT) {
        long weeks = Math.subtractExact(woy, localizedWeekOfYear(date));
        int days = localDow - localizedDayOfWeek(date);
        date = date.plus(Math.addExact(Math.multiplyExact(weeks, 7), days), DAYS);
      } else {
        int womInt = range.checkValidIntValue(woy, this);
        int weeks = (int) (womInt - localizedWeekOfYear(date));
        int days = localDow - localizedDayOfWeek(date);
        date = date.plus(weeks * 7 + days, DAYS);
        if (resolverStyle == ResolverStyle.STRICT && date.getLong(YEAR) != year) {
          throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
        }
      }
      fieldValues.remove(this);
      fieldValues.remove(YEAR);
      fieldValues.remove(DAY_OF_WEEK);
      return date;
    }

    private ChronoLocalDate resolveWBY(Map<TemporalField, Long> fieldValues, Chronology chrono, int localDow,
        ResolverStyle resolverStyle) {
      int yowby = weekDef.weekBasedYear.range().checkValidIntValue(fieldValues.get(weekDef.weekBasedYear),
          weekDef.weekBasedYear);
      ChronoLocalDate date;
      if (resolverStyle == ResolverStyle.LENIENT) {
        date = ofWeekBasedYear(chrono, yowby, 1, localDow);
        long wowby = fieldValues.get(weekDef.weekOfWeekBasedYear);
        long weeks = Math.subtractExact(wowby, 1);
        date = date.plus(weeks, WEEKS);
      } else {
        int wowby = weekDef.weekOfWeekBasedYear.range().checkValidIntValue(
            fieldValues.get(weekDef.weekOfWeekBasedYear), weekDef.weekOfWeekBasedYear);
        date = ofWeekBasedYear(chrono, yowby, wowby, localDow);
        if (resolverStyle == ResolverStyle.STRICT && localizedWeekBasedYear(date) != yowby) {
          throw new DateTimeException("Strict mode rejected resolved date as it is in a different week-based-year");
        }
      }
      fieldValues.remove(this);
      fieldValues.remove(weekDef.weekBasedYear);
      fieldValues.remove(weekDef.weekOfWeekBasedYear);
      fieldValues.remove(DAY_OF_WEEK);
      return date;
    }

    private ChronoLocalDate ofWeekBasedYear(Chronology chrono, int yowby, int wowby, int dow) {
      ChronoLocalDate date = chrono.date(yowby, 1, 1);
      int ldow = localizedDayOfWeek(date);
      int offset = startOfWeekOffset(1, ldow);
      int yearLen = date.lengthOfYear();
      int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
      wowby = Math.min(wowby, newYearWeek - 1);
      int days = -offset + (dow - 1) + (wowby - 1) * 7;
      return date.plus(days, DAYS);
    }

    @Override
    public String getDisplayName(Locale locale) {
      Objects.requireNonNull(locale, "locale");
      if (rangeUnit == YEARS) {
        return "week";
      }
      return name;
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
      return true;
    }

    @Override
    public boolean isTimeBased() {
      return false;
    }

    @Override
    public ValueRange range() {
      return range;
    }

    @Override
    public boolean isSupportedBy(TemporalAccessor temporal) {
      if (temporal.isSupported(DAY_OF_WEEK)) {
        if (rangeUnit == WEEKS) {
          return true;
        } else if (rangeUnit == MONTHS) {
          return temporal.isSupported(DAY_OF_MONTH);
        } else if (rangeUnit == YEARS) {
          return temporal.isSupported(DAY_OF_YEAR);
        } else if (rangeUnit == IsoFields.WEEK_BASED_YEARS) {
          return temporal.isSupported(DAY_OF_YEAR);
        } else if (rangeUnit == FOREVER) {
          return temporal.isSupported(YEAR);
        }
      }
      return false;
    }

    @Override
    public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
      if (rangeUnit == WEEKS) {
        return range;
      } else if (rangeUnit == MONTHS) {
        return rangeByWeek(temporal, DAY_OF_MONTH);
      } else if (rangeUnit == YEARS) {
        return rangeByWeek(temporal, DAY_OF_YEAR);
      } else if (rangeUnit == IsoFields.WEEK_BASED_YEARS) {
        return rangeWeekOfWeekBasedYear(temporal);
      } else if (rangeUnit == FOREVER) {
        return YEAR.range();
      } else {
        throw new IllegalStateException("unreachable, rangeUnit: " + rangeUnit + ", this: " + this);
      }
    }

    private ValueRange rangeByWeek(TemporalAccessor temporal, TemporalField field) {
      int dow = localizedDayOfWeek(temporal);
      int offset = startOfWeekOffset(temporal.get(field), dow);
      ValueRange fieldRange = temporal.range(field);
      return ValueRange.of(computeWeek(offset, (int) fieldRange.getMinimum()),
          computeWeek(offset, (int) fieldRange.getMaximum()));
    }

    private ValueRange rangeWeekOfWeekBasedYear(TemporalAccessor temporal) {
      if (!temporal.isSupported(DAY_OF_YEAR)) {
        return WEEK_OF_YEAR_RANGE;
      }
      int dow = localizedDayOfWeek(temporal);
      int doy = temporal.get(DAY_OF_YEAR);
      int offset = startOfWeekOffset(doy, dow);
      int week = computeWeek(offset, doy);
      if (week == 0) {
        ChronoLocalDate date = Chronology.from(temporal).date(temporal);
        date = date.minus(doy + 7, DAYS);
        return rangeWeekOfWeekBasedYear(date);
      }
      int yearLen = (int) temporal.range(DAY_OF_YEAR).getMaximum();
      int newYearWeek = computeWeek(offset, yearLen + weekDef.getMinimalDaysInFirstWeek());
      if (week >= newYearWeek) {
        ChronoLocalDate date = Chronology.from(temporal).date(temporal);
        date = date.plus(yearLen - doy + 1 + 7, DAYS);
        return rangeWeekOfWeekBasedYear(date);
      }
      return ValueRange.of(1, newYearWeek - 1);
    }

    @Override
    public String toString() {
      return name + "[" + weekDef.toString() + "]";
    }
  }
}
