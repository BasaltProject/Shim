/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class GregorianCalendar extends Calendar {
  private static final long serialVersionUID = -8125100834729963327L;

  public static final int BC = 0;
  static final int BCE = 0;
  public static final int AD = 1;
  static final int CE = 1;

  private static final int EPOCH_YEAR = 1970;
  private static final long ONE_SECOND = 1000;
  private static final long ONE_MINUTE = 60 * ONE_SECOND;
  private static final long ONE_HOUR = 60 * ONE_MINUTE;
  private static final long ONE_DAY = 24 * ONE_HOUR;
  private static final long ONE_WEEK = 7 * ONE_DAY;
  private static final long EPOCH_OFFSET = 719163L;

  private static final int[] MIN_VALUES = {BCE, 1, JANUARY, 1, 0, 1, 1, SUNDAY, 1, AM, 0, 0, 0, 0, 0,
      -13 * (int) ONE_HOUR, 0};
  private static final int[] LEAST_MAX_VALUES = {CE, 292269054, DECEMBER, 52, 4, 28, 365, SATURDAY, 4, PM, 11, 23, 59,
      59, 999, 14 * (int) ONE_HOUR, 20 * (int) ONE_MINUTE};
  private static final int[] MAX_VALUES = {CE, 292278994, DECEMBER, 53, 6, 31, 366, SATURDAY, 6, PM, 11, 23, 59, 59,
      999, 14 * (int) ONE_HOUR, 2 * (int) ONE_HOUR};

  private static final int[] kCumulative = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
  private static final int[] kMonthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  private long gregorianCutover = -12219292800000L;
  private transient long gregorianCutoverDate = 577736;
  private transient int gregorianCutoverYear = 1582;
  private transient int gregorianCutoverYearJulian = 1582;
  private transient int[] originalFields;
  private transient int[] zoneOffsets;

  public GregorianCalendar() {
    this(TimeZone.getDefaultRef(), Locale.getDefault());
    setZoneShared(true);
  }

  public GregorianCalendar(TimeZone zone) {
    this(zone, Locale.getDefault());
  }

  public GregorianCalendar(Locale aLocale) {
    this(TimeZone.getDefaultRef(), aLocale);
    setZoneShared(true);
  }

  public GregorianCalendar(TimeZone zone, Locale aLocale) {
    super(zone, aLocale);
    setTimeInMillis(System.currentTimeMillis());
  }

  public GregorianCalendar(int year, int month, int dayOfMonth) {
    this(year, month, dayOfMonth, 0, 0, 0, 0);
  }

  public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
    this(year, month, dayOfMonth, hourOfDay, minute, 0, 0);
  }

  public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
    this(year, month, dayOfMonth, hourOfDay, minute, second, 0);
  }

  GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millis) {
    super(TimeZone.getDefaultRef(), Locale.getDefault());
    setZoneShared(true);
    this.set(YEAR, year);
    this.set(MONTH, month);
    this.set(DAY_OF_MONTH, dayOfMonth);
    if (hourOfDay >= 12 && hourOfDay <= 23) {
      this.internalSet(AM_PM, PM);
      this.internalSet(HOUR, hourOfDay - 12);
    } else {
      this.internalSet(HOUR, hourOfDay);
    }
    setFieldsComputed(HOUR_MASK | AM_PM_MASK);
    set(HOUR_OF_DAY, hourOfDay);
    set(MINUTE, minute);
    set(SECOND, second);
    internalSet(MILLISECOND, millis);
  }

  public void setGregorianChange(Date date) {
    long cutoverTime = date.getTime();
    if (cutoverTime == gregorianCutover) {
      return;
    }
    complete();
    setGregorianChange(cutoverTime);
  }

  private void setGregorianChange(long cutoverTime) {
    gregorianCutover = cutoverTime;
    gregorianCutoverDate = Math.floorDiv(cutoverTime, ONE_DAY) + EPOCH_OFFSET;
    if (cutoverTime == Long.MAX_VALUE) {
      gregorianCutoverDate++;
    }
    long[] d = new long[3];
    toGregorian(gregorianCutoverDate, d);
    gregorianCutoverYear = (int) d[0];
    toJulian(gregorianCutoverDate - 1, d);
    gregorianCutoverYearJulian = (int) d[0];
    if (time < gregorianCutover) {
      setUnnormalized();
    }
  }

  public final Date getGregorianChange() {
    return new Date(gregorianCutover);
  }

  public boolean isLeapYear(int year) {
    if ((year & 3) != 0) {
      return false;
    }
    if (year > gregorianCutoverYear) {
      return (year % 100 != 0) || (year % 400 == 0);
    }
    if (year < gregorianCutoverYearJulian) {
      return true;
    }
    boolean gregorian;
    if (gregorianCutoverYear == gregorianCutoverYearJulian) {
      gregorian = year >= gregorianCutoverYear;
    } else {
      gregorian = year == gregorianCutoverYear;
    }
    return gregorian ? (year % 100 != 0) || (year % 400 == 0) : true;
  }

  private static boolean gregLeap(long year) {
    return (year & 3) == 0 && (year % 100 != 0 || year % 400 == 0);
  }

  private static boolean julLeap(long year) {
    return Math.floorMod(year, 4L) == 0;
  }

  private static long gregorianFixed(long y, int m, int d) {
    long days = 365L * (y - 1) + Math.floorDiv(y - 1, 4L) - Math.floorDiv(y - 1, 100L) + Math.floorDiv(y - 1, 400L);
    return days + kCumulative[m - 1] + (m > 2 && gregLeap(y) ? 1 : 0) + d;
  }

  private static long julianFixed(long y, int m, int d) {
    long days = 365L * (y - 1) + Math.floorDiv(y - 1, 4L);
    return days + kCumulative[m - 1] + (m > 2 && julLeap(y) ? 1 : 0) + d - 2;
  }

  private static void toGregorian(long fixed, long[] out) {
    LocalDate date = LocalDate.ofEpochDay(fixed - EPOCH_OFFSET);
    out[0] = date.getYear();
    out[1] = date.getMonthValue();
    out[2] = date.getDayOfMonth();
  }

  private static void toJulian(long fixed, long[] out) {
    long n = fixed + 1;
    long cycle = Math.floorDiv(n, 1461L);
    long rem = Math.floorMod(n, 1461L);
    int idx = rem < 1095 ? (int) (rem / 365) : 3;
    long y = cycle * 4 + idx + 1;
    long doy0 = idx == 3 ? rem - 1095 : rem - idx * 365L;
    boolean leap = julLeap(y);
    int month = 11;
    for (int m = 0; m < 12; m++) {
      int next = m < 11 ? kCumulative[m + 1] : 365;
      if (leap && m >= 1) {
        next += 1;
      }
      if (doy0 < next) {
        month = m;
        break;
      }
    }
    int start = kCumulative[month] + (leap && month >= 2 ? 1 : 0);
    out[0] = y;
    out[1] = month + 1;
    out[2] = (int) (doy0 - start + 1);
  }

  private boolean isGregorianFixed(long fixed) {
    return fixed >= gregorianCutoverDate;
  }

  private void fromFixed(long fixed, long[] out) {
    if (isGregorianFixed(fixed)) {
      toGregorian(fixed, out);
    } else {
      toJulian(fixed, out);
    }
  }

  private long toFixed(long year, int month1, int day) {
    if (year > gregorianCutoverYear) {
      return gregorianFixed(year, month1, day);
    }
    if (year < gregorianCutoverYearJulian) {
      return julianFixed(year, month1, day);
    }
    long g = gregorianFixed(year, month1, day);
    if (g >= gregorianCutoverDate) {
      return g;
    }
    return julianFixed(year, month1, day);
  }

  private int daysInMonth(long year, int month0, boolean gregorian) {
    if (month0 == 1) {
      return (gregorian ? gregLeap(year) : julLeap(year)) ? 29 : 28;
    }
    return kMonthDays[month0];
  }

  private boolean yearIsGregorian(long year) {
    if (year > gregorianCutoverYear) {
      return true;
    }
    if (year < gregorianCutoverYearJulian) {
      return false;
    }
    return year == gregorianCutoverYear && year == gregorianCutoverYearJulian ? false : year >= gregorianCutoverYear;
  }

  private int regularMonthLength(int month0, int year) {
    boolean gregorian = year > gregorianCutoverYear || (year == gregorianCutoverYear
        && gregorianCutoverYear != gregorianCutoverYearJulian);
    if (year < gregorianCutoverYearJulian) {
      gregorian = false;
    }
    return daysInMonth(year, month0, gregorian);
  }

  private static final String[] kUnused = {};

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GregorianCalendar && super.equals(obj)
        && gregorianCutover == ((GregorianCalendar) obj).gregorianCutover;
  }

  private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
    stream.defaultReadObject();
    setGregorianChange(gregorianCutover);
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ (int) gregorianCutoverDate;
  }

  @Override
  public Object clone() {
    GregorianCalendar other = (GregorianCalendar) super.clone();
    other.originalFields = null;
    other.zoneOffsets = null;
    return other;
  }

  private static int firstDayOnOrBefore(long fixedDate, int dayOfWeek) {
    return 0;
  }

  private static long dayOfWeekDateOnOrBefore(long fixedDate, int dayOfWeek) {
    long fd = fixedDate - (dayOfWeek - 1);
    return fixedDate - Math.floorMod(fd, 7L);
  }

  private int dayOfWeekOf(long fixedDate) {
    return (int) Math.floorMod(fixedDate, 7L) + 1;
  }

  private int[] zoneOffsetsFor(long utc) {
    TimeZone tz = getZone();
    int[] o = new int[2];
    if (tz instanceof ZoneInfoTimeZone) {
      ((ZoneInfoTimeZone) tz).getOffsets(utc, o);
    } else {
      int total = tz.getOffset(utc);
      o[0] = tz.getRawOffset();
      o[1] = total - o[0];
    }
    return o;
  }

  @Override
  protected void computeFields() {
    int mask;
    if (isPartiallyNormalized()) {
      mask = getSetStateFields();
      int fieldMask = ~mask & ALL_FIELDS;
      if (fieldMask != 0) {
        mask |= computeFields(fieldMask, mask & (ZONE_OFFSET_MASK | DST_OFFSET_MASK));
      }
    } else {
      mask = ALL_FIELDS;
      computeFields(mask, 0);
    }
    setFieldsComputed(mask);
  }

  private int computeFields(int fieldMask, int tzMask) {
    int[] offs = zoneOffsetsFor(time);
    int zoneOffset;
    if (tzMask != (ZONE_OFFSET_MASK | DST_OFFSET_MASK)) {
      zoneOffsets = offs;
      zoneOffset = offs[0] + offs[1];
    } else {
      zoneOffsets = new int[2];
    }
    if (tzMask != 0) {
      if (isFieldSet(tzMask, ZONE_OFFSET)) {
        offs[0] = internalGet(ZONE_OFFSET);
      }
      if (isFieldSet(tzMask, DST_OFFSET)) {
        offs[1] = internalGet(DST_OFFSET);
      }
    }
    zoneOffset = offs[0] + offs[1];
    long fixedDate = Math.floorDiv(time + zoneOffset, ONE_DAY) + EPOCH_OFFSET;
    int timeOfDay = (int) Math.floorMod(time + zoneOffset, ONE_DAY);

    long[] ymd = new long[3];
    fromFixed(fixedDate, ymd);
    boolean gregorian = isGregorianFixed(fixedDate);
    long year = ymd[0];
    int month = (int) ymd[1] - 1;
    int dayOfMonth = (int) ymd[2];

    int mask = ERA_MASK | YEAR_MASK | MONTH_MASK | DAY_OF_MONTH_MASK | DAY_OF_WEEK_MASK | HOUR_OF_DAY_MASK
        | AM_PM_MASK | HOUR_MASK | MINUTE_MASK | SECOND_MASK | MILLISECOND_MASK | ZONE_OFFSET_MASK | DST_OFFSET_MASK;
    mask |= fieldMask;

    int era = CE;
    int normalized = (int) year;
    if (year <= 0) {
      era = BCE;
      normalized = (int) (1 - year);
    }
    internalSet(ERA, era);
    internalSet(YEAR, normalized);
    internalSet(MONTH, month);
    internalSet(DAY_OF_MONTH, dayOfMonth);
    int dow = dayOfWeekOf(fixedDate);
    internalSet(DAY_OF_WEEK, dow);
    internalSet(HOUR_OF_DAY, timeOfDay / (int) ONE_HOUR);
    internalSet(AM_PM, (timeOfDay / (int) ONE_HOUR) / 12);
    internalSet(HOUR, (timeOfDay / (int) ONE_HOUR) % 12);
    internalSet(MINUTE, (timeOfDay / (int) ONE_MINUTE) % 60);
    internalSet(SECOND, (timeOfDay / (int) ONE_SECOND) % 60);
    internalSet(MILLISECOND, timeOfDay % 1000);
    if (tzMask == 0 || !isFieldSet(tzMask, ZONE_OFFSET)) {
      internalSet(ZONE_OFFSET, offs[0]);
    }
    if (tzMask == 0 || !isFieldSet(tzMask, DST_OFFSET)) {
      internalSet(DST_OFFSET, offs[1]);
    }

    long fixedDateJan1 = yearStart(year, gregorian);
    int dayOfYear = (int) (fixedDate - fixedDateJan1) + 1;
    long fixedDateMonth1 = fixedDate - dayOfMonth + 1;
    if (!gregorian && false) {
      fixedDateMonth1 = fixedDate - dayOfMonth + 1;
    }
    if (isGregorianFixed(fixedDate) && year == gregorianCutoverYear && month == ymd[1] - 1) {
      long m1 = gregorianFixed(year, month + 1, 1);
      if (m1 < gregorianCutoverDate) {
        fixedDateMonth1 = gregorianCutoverDate;
      }
    }
    internalSet(DAY_OF_YEAR, dayOfYear);
    int adjustedDom = dayOfMonth;
    if (gregorian && fixedDateMonth1 == gregorianCutoverDate && gregorianFixed(year, month + 1,
        1) < gregorianCutoverDate) {
      long[] jd = new long[3];
      toJulian(gregorianCutoverDate - 1, jd);
      adjustedDom = (int) (fixedDate - gregorianCutoverDate) + (int) jd[2] + 1;
    }
    internalSet(DAY_OF_WEEK_IN_MONTH, (adjustedDom - 1) / 7 + 1);

    int weekOfYear = getWeekNumber(fixedDateJan1, fixedDate);
    if (weekOfYear == 0) {
      long fixedDec31 = fixedDateJan1 - 1;
      long prevJan1;
      boolean prevGregorian = isGregorianFixed(fixedDec31);
      if (prevGregorian) {
        prevJan1 = yearStart(year - 1, true);
      } else {
        prevJan1 = julianFixed(year - 1, 1, 1);
      }
      weekOfYear = getWeekNumber(prevJan1, fixedDec31);
    } else {
      if (year > gregorianCutoverYear || year < gregorianCutoverYearJulian - 1) {
        if (weekOfYear >= 52) {
          boolean leap = gregorian ? gregLeap(year) : julLeap(year);
          long nextJan1 = fixedDateJan1 + (leap ? 366 : 365);
          long nextJan1st = dayOfWeekDateOnOrBefore(nextJan1 + 6, getFirstDayOfWeek());
          int ndays = (int) (nextJan1st - nextJan1);
          if (ndays >= getMinimalDaysInFirstWeek() && fixedDate >= (nextJan1st - 7)) {
            weekOfYear = 1;
          }
        }
      } else {
        long nextJan1;
        if (year == gregorianCutoverYear) {
          nextJan1 = gregorianFixed(year + 1, 1, 1);
        } else {
          nextJan1 = julianFixed(year + 1, 1, 1);
          if (nextJan1 >= gregorianCutoverDate) {
            nextJan1 = gregorianCutoverDate > nextJan1 ? nextJan1 : gregorianFixed(year + 1, 1, 1);
          }
        }
        long nextJan1st = dayOfWeekDateOnOrBefore(nextJan1 + 6, getFirstDayOfWeek());
        int ndays = (int) (nextJan1st - nextJan1);
        if (ndays >= getMinimalDaysInFirstWeek() && fixedDate >= (nextJan1st - 7)) {
          weekOfYear = 1;
        }
      }
    }
    internalSet(WEEK_OF_YEAR, weekOfYear);
    internalSet(WEEK_OF_MONTH, getWeekNumber(fixedDateMonth1, fixedDate));
    return mask;
  }

  private long yearStart(long year, boolean gregorianDate) {
    if (gregorianDate) {
      if (year == gregorianCutoverYear) {
        long jan1 = gregorianFixed(year, 1, 1);
        if (jan1 < gregorianCutoverDate) {
          return year == gregorianCutoverYearJulian ? julianFixed(year, 1, 1) : gregorianCutoverDate;
        }
        return jan1;
      }
      return gregorianFixed(year, 1, 1);
    }
    return julianFixed(year, 1, 1);
  }

  private int getWeekNumber(long fixedDay1, long fixedDate) {
    long fixedDay1st = dayOfWeekDateOnOrBefore(fixedDay1 + 6, getFirstDayOfWeek());
    int ndays = (int) (fixedDay1st - fixedDay1);
    if (ndays >= getMinimalDaysInFirstWeek()) {
      fixedDay1st -= 7;
    }
    int normalizedDayOfPeriod = (int) (fixedDate - fixedDay1st);
    if (normalizedDayOfPeriod >= 0) {
      return normalizedDayOfPeriod / 7 + 1;
    }
    return Math.floorDiv(normalizedDayOfPeriod, 7) + 1;
  }

  @Override
  protected void computeTime() {
    if (!isLenient()) {
      if (originalFields == null) {
        originalFields = new int[FIELD_COUNT];
      }
      for (int field = 0; field < FIELD_COUNT; field++) {
        int value = internalGet(field);
        if (isExternallySet(field)) {
          if (value < getMinimum(field) || value > getMaximum(field)) {
            throw new IllegalArgumentException(getFieldName(field));
          }
        }
        originalFields[field] = value;
      }
    }
    int fieldMask = selectFields();
    int year = isSet(YEAR) ? internalGet(YEAR) : EPOCH_YEAR;
    int era = internalGetEra();
    if (era == BCE) {
      year = 1 - year;
    } else if (era != CE) {
      throw new IllegalArgumentException("Invalid era");
    }
    if (year <= 0 && !isSet(ERA)) {
      fieldMask |= ERA_MASK;
      setFieldsComputed(ERA_MASK);
    }
    long timeOfDay = 0;
    if (isFieldSet(fieldMask, HOUR_OF_DAY)) {
      timeOfDay += (long) internalGet(HOUR_OF_DAY);
    } else {
      timeOfDay += internalGet(HOUR);
      if (isFieldSet(fieldMask, AM_PM)) {
        timeOfDay += 12 * internalGet(AM_PM);
      }
    }
    timeOfDay *= 60;
    timeOfDay += internalGet(MINUTE);
    timeOfDay *= 60;
    timeOfDay += internalGet(SECOND);
    timeOfDay *= 1000;
    timeOfDay += internalGet(MILLISECOND);
    long fixedDate = Math.floorDiv(timeOfDay, ONE_DAY);
    timeOfDay = Math.floorMod(timeOfDay, ONE_DAY);
    long dateFixed;
    if (year > gregorianCutoverYear && year > gregorianCutoverYearJulian) {
      dateFixed = calculateFixedDate(year, fieldMask, true);
    } else if (year < gregorianCutoverYear && year < gregorianCutoverYearJulian) {
      dateFixed = calculateFixedDate(year, fieldMask, false);
    } else {
      dateFixed = calculateFixedDate(year, fieldMask, true);
      if (dateFixed < gregorianCutoverDate) {
        dateFixed = calculateFixedDate(year, fieldMask, false);
      }
    }
    fixedDate += dateFixed;
    long millis = (fixedDate - EPOCH_OFFSET) * ONE_DAY + timeOfDay;
    TimeZone tz = getZone();
    int[] offs = new int[2];
    int tzMask = fieldMask & (ZONE_OFFSET_MASK | DST_OFFSET_MASK);
    if (tzMask != (ZONE_OFFSET_MASK | DST_OFFSET_MASK)) {
      if (tz instanceof ZoneInfoTimeZone) {
        ((ZoneInfoTimeZone) tz).getOffsetsByWall(millis, offs);
      } else {
        int gmtOffset = isFieldSet(fieldMask, ZONE_OFFSET) ? internalGet(ZONE_OFFSET) : tz.getRawOffset();
        tz.getOffsets(millis - gmtOffset, offs);
      }
    }
    if (tzMask != 0) {
      if (isFieldSet(tzMask, ZONE_OFFSET)) {
        offs[0] = internalGet(ZONE_OFFSET);
      }
      if (isFieldSet(tzMask, DST_OFFSET)) {
        offs[1] = internalGet(DST_OFFSET);
      }
    }
    millis -= offs[0] + offs[1];
    time = millis;
    int mask = computeFields(fieldMask | getSetStateFields(), tzMask);
    if (!isLenient()) {
      for (int field = 0; field < FIELD_COUNT; field++) {
        if (!isExternallySet(field)) {
          continue;
        }
        if (originalFields[field] != internalGet(field)) {
          String s = originalFields[field] + " -> " + internalGet(field);
          System.arraycopy(originalFields, 0, fields, 0, fields.length);
          throw new IllegalArgumentException(getFieldName(field) + ": " + s);
        }
      }
    }
    setFieldsNormalized(mask);
  }

  private int internalGetEra() {
    return isSet(ERA) ? internalGet(ERA) : CE;
  }

  private long calculateFixedDate(int year, int fieldMask, boolean greg) {
    int month = JANUARY;
    if (isFieldSet(fieldMask, MONTH)) {
      month = internalGet(MONTH);
      if (month > DECEMBER) {
        year += month / 12;
        month %= 12;
      } else if (month < JANUARY) {
        year += Math.floorDiv(month, 12);
        month = Math.floorMod(month, 12);
      }
    }
    long fixedDate = greg ? gregorianFixed(year, month + 1, 1) : julianFixed(year, month + 1, 1);
    if (isFieldSet(fieldMask, MONTH)) {
      if (isFieldSet(fieldMask, DAY_OF_MONTH)) {
        if (isSet(DAY_OF_MONTH)) {
          fixedDate += internalGet(DAY_OF_MONTH);
          fixedDate--;
        }
      } else {
        if (isFieldSet(fieldMask, WEEK_OF_MONTH)) {
          long firstDayOfWeek = dayOfWeekDateOnOrBefore(fixedDate + 6, getFirstDayOfWeek());
          if ((firstDayOfWeek - fixedDate) >= getMinimalDaysInFirstWeek()) {
            firstDayOfWeek -= 7;
          }
          if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
            firstDayOfWeek = dayOfWeekDateOnOrBefore(firstDayOfWeek + 6, internalGet(DAY_OF_WEEK));
          }
          fixedDate = firstDayOfWeek + 7 * (internalGet(WEEK_OF_MONTH) - 1);
        } else {
          int dayOfWeek;
          if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
            dayOfWeek = internalGet(DAY_OF_WEEK);
          } else {
            dayOfWeek = getFirstDayOfWeek();
          }
          int dowim;
          if (isFieldSet(fieldMask, DAY_OF_WEEK_IN_MONTH)) {
            dowim = internalGet(DAY_OF_WEEK_IN_MONTH);
          } else {
            dowim = 1;
          }
          if (dowim >= 0) {
            fixedDate = dayOfWeekDateOnOrBefore(fixedDate + (7 * dowim) - 1, dayOfWeek);
          } else {
            int lastDate = monthLengthActual(month, year) + (7 * (dowim + 1));
            fixedDate = dayOfWeekDateOnOrBefore(fixedDate + lastDate - 1, dayOfWeek);
          }
        }
      }
    } else {
      if (year == gregorianCutoverYear && fixedDate < gregorianCutoverDate
          && gregorianCutoverYear != gregorianCutoverYearJulian) {
        fixedDate = gregorianCutoverDate;
      }
      if (isFieldSet(fieldMask, DAY_OF_YEAR)) {
        fixedDate += internalGet(DAY_OF_YEAR);
        fixedDate--;
      } else {
        long firstDayOfWeek = dayOfWeekDateOnOrBefore(fixedDate + 6, getFirstDayOfWeek());
        if ((firstDayOfWeek - fixedDate) >= getMinimalDaysInFirstWeek()) {
          firstDayOfWeek -= 7;
        }
        if (isFieldSet(fieldMask, DAY_OF_WEEK)) {
          int dayOfWeek = internalGet(DAY_OF_WEEK);
          if (dayOfWeek != getFirstDayOfWeek()) {
            firstDayOfWeek = dayOfWeekDateOnOrBefore(firstDayOfWeek + 6, dayOfWeek);
          }
        }
        fixedDate = firstDayOfWeek + 7 * ((long) internalGet(WEEK_OF_YEAR) - 1);
      }
    }
    return fixedDate;
  }

  private int monthLengthActual(int month0, int year) {
    long first = toFixed(year, month0 + 1, 1);
    long next = month0 == 11 ? toFixed(year + 1, 1, 1) : toFixed(year, month0 + 2, 1);
    return (int) (next - first);
  }

  private long currentFixedDate() {
    return Math.floorDiv(time + internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET), ONE_DAY) + EPOCH_OFFSET;
  }

  @Override
  public void add(int field, int amount) {
    if (amount == 0) {
      return;
    }
    if (field < 0 || field >= ZONE_OFFSET) {
      throw new IllegalArgumentException();
    }
    complete();
    if (field == YEAR) {
      int year = internalGet(YEAR);
      if (internalGetEra() == CE) {
        year += amount;
        if (year > 0) {
          set(YEAR, year);
        } else {
          set(YEAR, 1 - year);
          set(ERA, BCE);
        }
      } else {
        year -= amount;
        if (year > 0) {
          set(YEAR, year);
        } else {
          set(YEAR, 1 - year);
          set(ERA, CE);
        }
      }
      pinDayOfMonth();
    } else if (field == MONTH) {
      int month = internalGet(MONTH) + amount;
      int year = internalGet(YEAR);
      int yAmount;
      if (month >= 0) {
        yAmount = month / 12;
      } else {
        yAmount = (month + 1) / 12 - 1;
      }
      if (yAmount != 0) {
        if (internalGetEra() == CE) {
          year += yAmount;
          if (year > 0) {
            set(YEAR, year);
          } else {
            set(YEAR, 1 - year);
            set(ERA, BCE);
          }
        } else {
          year -= yAmount;
          if (year > 0) {
            set(YEAR, year);
          } else {
            set(YEAR, 1 - year);
            set(ERA, CE);
          }
        }
      }
      if (month >= 0) {
        set(MONTH, month % 12);
      } else {
        month %= 12;
        if (month < 0) {
          month += 12;
        }
        set(MONTH, JANUARY + month);
      }
      pinDayOfMonth();
    } else if (field == ERA) {
      int era = internalGet(ERA) + amount;
      if (era < 0) {
        era = 0;
      }
      if (era > 1) {
        era = 1;
      }
      set(ERA, era);
    } else {
      long delta = amount;
      long timeOfDay = 0;
      switch (field) {
        case HOUR:
        case HOUR_OF_DAY:
          delta *= 60 * 60 * 1000;
          break;
        case MINUTE:
          delta *= 60 * 1000;
          break;
        case SECOND:
          delta *= 1000;
          break;
        case MILLISECOND:
          break;
        case WEEK_OF_YEAR:
        case WEEK_OF_MONTH:
        case DAY_OF_WEEK_IN_MONTH:
          delta *= 7;
          break;
        case DAY_OF_MONTH:
        case DAY_OF_YEAR:
        case DAY_OF_WEEK:
          break;
        case AM_PM:
          delta = amount / 2;
          timeOfDay = 12 * (amount % 2);
          break;
        default:
          break;
      }
      if (field >= HOUR) {
        setTimeInMillis(time + delta);
        return;
      }
      long fd = currentFixedDate();
      timeOfDay += internalGet(HOUR_OF_DAY);
      timeOfDay *= 60;
      timeOfDay += internalGet(MINUTE);
      timeOfDay *= 60;
      timeOfDay += internalGet(SECOND);
      timeOfDay *= 1000;
      timeOfDay += internalGet(MILLISECOND);
      if (timeOfDay >= ONE_DAY) {
        fd++;
        timeOfDay -= ONE_DAY;
      } else if (timeOfDay < 0) {
        fd--;
        timeOfDay += ONE_DAY;
      }
      fd += delta;
      int zoneOffset = internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET);
      setTimeInMillis((fd - EPOCH_OFFSET) * ONE_DAY + timeOfDay - zoneOffset);
      zoneOffset -= internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET);
      if (zoneOffset != 0) {
        setTimeInMillis(time + zoneOffset);
        long fd2 = currentFixedDate();
        if (fd2 != fd) {
          setTimeInMillis(time - zoneOffset);
        }
      }
    }
  }

  private void pinDayOfMonth() {
    int year = internalGet(YEAR);
    int era = internalGetEra();
    int calYear = era == BCE ? 1 - year : year;
    int monthLen = monthLengthActual(internalGet(MONTH), calYear);
    int dom = internalGet(DAY_OF_MONTH);
    if (dom > monthLen) {
      set(DAY_OF_MONTH, monthLen);
    }
  }

  @Override
  public void roll(int field, boolean up) {
    roll(field, up ? +1 : -1);
  }

  @Override
  public void roll(int field, int amount) {
    if (amount == 0) {
      return;
    }
    if (field < 0 || field >= ZONE_OFFSET) {
      throw new IllegalArgumentException();
    }
    complete();
    int min = getMinimum(field);
    int max = getMaximum(field);
    switch (field) {
      case AM_PM:
      case ERA:
      case MINUTE:
      case SECOND:
      case MILLISECOND:
        break;
      case YEAR:
        min = getActualMinimum(field);
        max = getActualMaximum(field);
        break;
      case HOUR:
      case HOUR_OF_DAY:
        break;
      case MONTH: {
        int mon = (internalGet(MONTH) + amount) % 12;
        if (mon < 0) {
          mon += 12;
        }
        set(MONTH, mon);
        pinDayOfMonth();
        return;
      }
      case WEEK_OF_YEAR: {
        int y = internalGetEra() == BCE ? 1 - internalGet(YEAR) : internalGet(YEAR);
        max = getActualMaximum(WEEK_OF_YEAR);
        set(DAY_OF_WEEK, internalGet(DAY_OF_WEEK));
        int woy = internalGet(WEEK_OF_YEAR);
        int value = woy + amount;
        if (y != gregorianCutoverYear && y != gregorianCutoverYearJulian) {
          int weekYear = getWeekYear();
          if (weekYear == y) {
            if (value > min && value < max) {
              set(WEEK_OF_YEAR, value);
              return;
            }
            long fd = currentFixedDate();
            long day1 = fd - (7L * (woy - min));
            long[] ymd = new long[3];
            fromFixed(day1, ymd);
            if (ymd[0] != y) {
              min++;
            }
            fd += 7L * (max - internalGet(WEEK_OF_YEAR));
            fromFixed(fd, ymd);
            if (ymd[0] != y) {
              max--;
            }
          } else {
            if (weekYear > y) {
              if (amount < 0) {
                amount++;
              }
              woy = max;
            } else {
              if (amount > 0) {
                amount -= woy;
              }
              woy = min;
            }
          }
          set(field, getRolledValue(woy, amount, min, max));
          return;
        }
        add(DAY_OF_MONTH, (getRolledValue(woy, amount, min, max) - woy) * 7);
        return;
      }
      case WEEK_OF_MONTH: {
        int dow = internalGet(DAY_OF_WEEK) - getFirstDayOfWeek();
        if (dow < 0) {
          dow += 7;
        }
        long fd = currentFixedDate();
        int dom = internalGet(DAY_OF_MONTH);
        long month1 = fd - dom + 1;
        int year = internalGetEra() == BCE ? 1 - internalGet(YEAR) : internalGet(YEAR);
        int monthLength = monthLengthActual(internalGet(MONTH), year);
        if (isGregorianFixed(fd) && year == gregorianCutoverYear && toFixedJulianStartBefore(year,
            internalGet(MONTH))) {
          month1 = gregorianCutoverDate;
          monthLength = (int) (toFixed(year, internalGet(MONTH) + 2 > 12 ? 1 : internalGet(MONTH) + 2,
              1) - month1);
        }
        long monthDay1st = dayOfWeekDateOnOrBefore(month1 + 6, getFirstDayOfWeek());
        if ((int) (monthDay1st - month1) >= getMinimalDaysInFirstWeek()) {
          monthDay1st -= 7;
        }
        max = getActualMaximum(field);
        int value = getRolledValue(internalGet(field), amount, 1, max) - 1;
        long nfd = monthDay1st + value * 7L + dow;
        if (nfd < month1) {
          nfd = month1;
        } else if (nfd >= (month1 + monthLength)) {
          nfd = month1 + monthLength - 1;
        }
        long[] ymd = new long[3];
        fromFixed(nfd, ymd);
        set(DAY_OF_MONTH, (int) ymd[2]);
        return;
      }
      case DAY_OF_MONTH: {
        max = getActualMaximum(DAY_OF_MONTH);
        break;
      }
      case DAY_OF_YEAR: {
        max = getActualMaximum(DAY_OF_YEAR);
        break;
      }
      case DAY_OF_WEEK: {
        int dow = internalGet(DAY_OF_WEEK);
        int first = getFirstDayOfWeek();
        int offset = Math.floorMod(dow - first, 7);
        int newOffset = Math.floorMod(offset + amount, 7);
        add(DAY_OF_MONTH, newOffset - offset);
        return;
      }
      case DAY_OF_WEEK_IN_MONTH: {
        int dom = internalGet(DAY_OF_MONTH);
        int len = monthLengthActual(internalGet(MONTH), internalGetEra() == BCE ? 1 - internalGet(YEAR)
            : internalGet(YEAR));
        int firstDay = ((dom - 1) % 7) + 1;
        int count = (len - firstDay) / 7 + 1;
        int n = (dom - 1) / 7 + 1;
        int newN = getRolledValue(n, amount, 1, count);
        set(DAY_OF_MONTH, firstDay + (newN - 1) * 7);
        return;
      }
      default:
        break;
    }
    int newValue = getRolledValue(internalGet(field), amount, min, max);
    set(field, newValue);
  }

  private boolean toFixedJulianStartBefore(int year, int month0) {
    return gregorianFixed(year, month0 + 1, 1) < gregorianCutoverDate;
  }

  private static int getRolledValue(int value, int amount, int min, int max) {
    int range = max - min + 1;
    int n = value - min + amount;
    n %= range;
    if (n < 0) {
      n += range;
    }
    return n + min;
  }

  @Override
  public int getMinimum(int field) {
    return MIN_VALUES[field];
  }

  @Override
  public int getMaximum(int field) {
    switch (field) {
      case MONTH:
      case DAY_OF_MONTH:
      case DAY_OF_YEAR:
      case WEEK_OF_YEAR:
      case WEEK_OF_MONTH:
      case DAY_OF_WEEK_IN_MONTH:
      case YEAR: {
        if (gregorianCutoverYear > 200) {
          break;
        }
        break;
      }
      default:
        break;
    }
    return MAX_VALUES[field];
  }

  @Override
  public int getGreatestMinimum(int field) {
    return MIN_VALUES[field];
  }

  @Override
  public int getLeastMaximum(int field) {
    if (gregorianCutover == -12219292800000L) {
      switch (field) {
        case WEEK_OF_YEAR:
          return 50;
        case WEEK_OF_MONTH:
          return 3;
        case DAY_OF_YEAR:
          return 355;
        case DAY_OF_WEEK_IN_MONTH:
          return 3;
        default:
          break;
      }
    }
    return LEAST_MAX_VALUES[field];
  }

  @Override
  public int getActualMinimum(int field) {
    if (field == DAY_OF_MONTH) {
      complete();
      int year = internalGetEra() == BCE ? 1 - internalGet(YEAR) : internalGet(YEAR);
      if (year == gregorianCutoverYear || year == gregorianCutoverYearJulian) {
        long[] ymd = new long[3];
        long first = toFixed(year, internalGet(MONTH) + 1, 1);
        fromFixed(first, ymd);
        long gc = gregorianCutoverDate;
        long[] g = new long[3];
        fromFixed(gc, g);
        if (g[0] == year && g[1] - 1 == internalGet(MONTH) && first < gc && false) {
          return (int) g[2];
        }
        return (int) ymd[2];
      }
    }
    return getMinimum(field);
  }

  @Override
  public int getActualMaximum(int field) {
    final int fieldsForFixedMax = ERA_MASK | DAY_OF_WEEK_MASK | HOUR_MASK | AM_PM_MASK | HOUR_OF_DAY_MASK
        | MINUTE_MASK | SECOND_MASK | MILLISECOND_MASK | ZONE_OFFSET_MASK | DST_OFFSET_MASK;
    if ((fieldsForFixedMax & (1 << field)) != 0) {
      return getMaximum(field);
    }
    complete();
    int year = internalGetEra() == BCE ? 1 - internalGet(YEAR) : internalGet(YEAR);
    int month = internalGet(MONTH);
    switch (field) {
      case MONTH:
        return DECEMBER;
      case DAY_OF_MONTH: {
        long[] last = new long[3];
        fromFixed(toFixed(year, month + 1, 1) + monthLengthActual(month, year) - 1, last);
        return (int) last[2];
      }
      case DAY_OF_YEAR: {
        long start = toFixed(year, 1, 1);
        long end = toFixed(year + 1, 1, 1);
        if (year == gregorianCutoverYear && gregorianCutoverYear != gregorianCutoverYearJulian) {
          start = gregorianCutoverDate < start ? start : start;
        }
        return (int) (yearEndFixed(year) - yearStart(year, isGregorianFixed(currentFixedDate()))) + 1;
      }
      case WEEK_OF_YEAR: {
        GregorianCalendar probe = (GregorianCalendar) clone();
        probe.setLenient(true);
        int lastDay = (int) (yearEndFixed(year) - yearStart(year, isGregorianFixed(currentFixedDate()))) + 1;
        probe.set(DAY_OF_YEAR, lastDay);
        int week = probe.get(WEEK_OF_YEAR);
        if (week == 1) {
          probe.set(DAY_OF_YEAR, lastDay - 7);
          week = probe.get(WEEK_OF_YEAR);
        }
        return week;
      }
      case WEEK_OF_MONTH: {
        GregorianCalendar probe = (GregorianCalendar) clone();
        probe.setLenient(true);
        probe.set(DAY_OF_MONTH, monthLengthActual(month, year));
        return probe.get(WEEK_OF_MONTH);
      }
      case DAY_OF_WEEK_IN_MONTH: {
        int len = monthLengthActual(month, year);
        return (len + 6) / 7;
      }
      case YEAR:
        return internalGetEra() == BCE ? 292269055 : 292278994;
      default:
        break;
    }
    throw new ArrayIndexOutOfBoundsException(field);
  }

  private long yearEndFixed(long year) {
    long next = toFixed(year + 1, 1, 1);
    return next - 1;
  }

  @Override
  public boolean isWeekDateSupported() {
    return true;
  }

  @Override
  public int getWeekYear() {
    int year = get(YEAR);
    if (internalGetEra() == BCE) {
      year = 1 - year;
    }
    int weekOfYear = internalGet(WEEK_OF_YEAR);
    int month = internalGet(MONTH);
    if (month == JANUARY) {
      if (weekOfYear >= 52) {
        --year;
      }
    } else if (month == DECEMBER) {
      if (weekOfYear == 1) {
        ++year;
      }
    }
    return year;
  }

  @Override
  public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
    if (dayOfWeek < SUNDAY || dayOfWeek > SATURDAY) {
      throw new IllegalArgumentException("invalid dayOfWeek: " + dayOfWeek);
    }
    GregorianCalendar gc = (GregorianCalendar) clone();
    gc.setLenient(true);
    int era = gc.get(ERA);
    gc.clear();
    gc.setTimeZone(TimeZone.getTimeZone("UTC"));
    gc.set(ERA, era);
    gc.set(YEAR, weekYear);
    gc.set(WEEK_OF_YEAR, 1);
    gc.set(DAY_OF_WEEK, getFirstDayOfWeek());
    int days = dayOfWeek - getFirstDayOfWeek();
    if (days < 0) {
      days += 7;
    }
    days += 7 * (weekOfYear - 1);
    if (days != 0) {
      gc.add(DAY_OF_YEAR, days);
    } else {
      gc.complete();
    }
    if (!isLenient() && (gc.getWeekYear() != weekYear || gc.internalGet(WEEK_OF_YEAR) != weekOfYear
        || gc.internalGet(DAY_OF_WEEK) != dayOfWeek)) {
      throw new IllegalArgumentException();
    }
    set(ERA, gc.internalGet(ERA));
    set(YEAR, gc.internalGet(YEAR));
    set(MONTH, gc.internalGet(MONTH));
    set(DAY_OF_MONTH, gc.internalGet(DAY_OF_MONTH));
    internalSet(WEEK_OF_YEAR, weekOfYear);
    complete();
  }

  @Override
  public int getWeeksInWeekYear() {
    GregorianCalendar gc = (GregorianCalendar) clone();
    gc.setWeekDate(getWeekYear(), 2, getFirstDayOfWeek());
    int weekYear = gc.getWeekYear();
    GregorianCalendar probe = (GregorianCalendar) gc.clone();
    probe.setLenient(true);
    probe.add(WEEK_OF_YEAR, 50);
    int count = 2;
    for (;;) {
      GregorianCalendar next = (GregorianCalendar) gc.clone();
      next.setLenient(true);
      next.add(DAY_OF_MONTH, 7 * count);
      if (next.getWeekYear() != weekYear) {
        return count + 1;
      }
      count++;
      if (count > 54) {
        return count;
      }
    }
  }

  @Override
  public String getCalendarType() {
    return "gregory";
  }

  public ZonedDateTime toZonedDateTime() {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getTimeInMillis()), getTimeZone().toZoneId());
  }

  public static GregorianCalendar from(ZonedDateTime zdt) {
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zdt.getZone()));
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    cal.setFirstDayOfWeek(MONDAY);
    cal.setMinimalDaysInFirstWeek(4);
    try {
      cal.setTimeInMillis(Math.addExact(Math.multiplyExact(zdt.toEpochSecond(), 1000),
          zdt.get(java.time.temporal.ChronoField.MILLI_OF_SECOND)));
    } catch (ArithmeticException ex) {
      throw new IllegalArgumentException(ex);
    }
    return cal;
  }
}
