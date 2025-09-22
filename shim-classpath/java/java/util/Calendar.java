/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.WeekFields;

public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {
  private static final long serialVersionUID = -1807547505821590642L;

  public static final int ERA = 0;
  public static final int YEAR = 1;
  public static final int MONTH = 2;
  public static final int WEEK_OF_YEAR = 3;
  public static final int WEEK_OF_MONTH = 4;
  public static final int DATE = 5;
  public static final int DAY_OF_MONTH = 5;
  public static final int DAY_OF_YEAR = 6;
  public static final int DAY_OF_WEEK = 7;
  public static final int DAY_OF_WEEK_IN_MONTH = 8;
  public static final int AM_PM = 9;
  public static final int HOUR = 10;
  public static final int HOUR_OF_DAY = 11;
  public static final int MINUTE = 12;
  public static final int SECOND = 13;
  public static final int MILLISECOND = 14;
  public static final int ZONE_OFFSET = 15;
  public static final int DST_OFFSET = 16;
  public static final int FIELD_COUNT = 17;

  public static final int SUNDAY = 1;
  public static final int MONDAY = 2;
  public static final int TUESDAY = 3;
  public static final int WEDNESDAY = 4;
  public static final int THURSDAY = 5;
  public static final int FRIDAY = 6;
  public static final int SATURDAY = 7;

  public static final int JANUARY = 0;
  public static final int FEBRUARY = 1;
  public static final int MARCH = 2;
  public static final int APRIL = 3;
  public static final int MAY = 4;
  public static final int JUNE = 5;
  public static final int JULY = 6;
  public static final int AUGUST = 7;
  public static final int SEPTEMBER = 8;
  public static final int OCTOBER = 9;
  public static final int NOVEMBER = 10;
  public static final int DECEMBER = 11;
  public static final int UNDECIMBER = 12;

  public static final int AM = 0;
  public static final int PM = 1;

  public static final int ALL_STYLES = 0;
  static final int STANDALONE_MASK = 0x8000;
  public static final int SHORT = 1;
  public static final int LONG = 2;
  public static final int NARROW_FORMAT = 4;
  public static final int NARROW_STANDALONE = NARROW_FORMAT | STANDALONE_MASK;
  public static final int SHORT_FORMAT = 1;
  public static final int LONG_FORMAT = 2;
  public static final int SHORT_STANDALONE = SHORT | STANDALONE_MASK;
  public static final int LONG_STANDALONE = LONG | STANDALONE_MASK;

  protected int[] fields;
  protected boolean[] isSet;
  private transient int[] stamp;
  protected long time;
  protected boolean isTimeSet;
  protected boolean areFieldsSet;
  transient boolean areAllFieldsSet;
  private boolean lenient = true;
  private TimeZone zone;
  private transient boolean sharedZone = false;
  private int firstDayOfWeek;
  private int minimalDaysInFirstWeek;

  static final int ERA_MASK = (1 << ERA);
  static final int YEAR_MASK = (1 << YEAR);
  static final int MONTH_MASK = (1 << MONTH);
  static final int WEEK_OF_YEAR_MASK = (1 << WEEK_OF_YEAR);
  static final int WEEK_OF_MONTH_MASK = (1 << WEEK_OF_MONTH);
  static final int DAY_OF_MONTH_MASK = (1 << DAY_OF_MONTH);
  static final int DAY_OF_YEAR_MASK = (1 << DAY_OF_YEAR);
  static final int DAY_OF_WEEK_MASK = (1 << DAY_OF_WEEK);
  static final int DAY_OF_WEEK_IN_MONTH_MASK = (1 << DAY_OF_WEEK_IN_MONTH);
  static final int AM_PM_MASK = (1 << AM_PM);
  static final int HOUR_MASK = (1 << HOUR);
  static final int HOUR_OF_DAY_MASK = (1 << HOUR_OF_DAY);
  static final int MINUTE_MASK = (1 << MINUTE);
  static final int SECOND_MASK = (1 << SECOND);
  static final int MILLISECOND_MASK = (1 << MILLISECOND);
  static final int ZONE_OFFSET_MASK = (1 << ZONE_OFFSET);
  static final int DST_OFFSET_MASK = (1 << DST_OFFSET);

  static final int UNSET = 0;
  static final int COMPUTED = 1;
  static final int MINIMUM_USER_STAMP = 2;
  static final int ALL_FIELDS = (1 << FIELD_COUNT) - 1;
  private transient int nextStamp = MINIMUM_USER_STAMP;

  private static final String[] kFieldNames = {"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
      "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE",
      "SECOND", "MILLISECOND", "ZONE_OFFSET", "DST_OFFSET"};

  protected Calendar() {
    this(TimeZone.getDefaultRef(), Locale.getDefault());
    sharedZone = true;
  }

  protected Calendar(TimeZone zone, Locale aLocale) {
    fields = new int[FIELD_COUNT];
    stamp = new int[FIELD_COUNT];
    isSet = new boolean[FIELD_COUNT];
    this.zone = zone;
    setWeekCountData(aLocale);
  }

  private void setWeekCountData(Locale desiredLocale) {
    WeekFields wf = WeekFields.of(desiredLocale);
    firstDayOfWeek = wf.getFirstDayOfWeek().getValue() % 7 + 1;
    minimalDaysInFirstWeek = wf.getMinimalDaysInFirstWeek();
  }

  public static Calendar getInstance() {
    return new GregorianCalendar(TimeZone.getDefaultRef(), Locale.getDefault());
  }

  public static Calendar getInstance(TimeZone zone) {
    return new GregorianCalendar(zone, Locale.getDefault());
  }

  public static Calendar getInstance(Locale aLocale) {
    return new GregorianCalendar(TimeZone.getDefaultRef(), aLocale);
  }

  public static Calendar getInstance(TimeZone zone, Locale aLocale) {
    return new GregorianCalendar(zone, aLocale);
  }

  public static synchronized Locale[] getAvailableLocales() {
    return new Locale[] {Locale.ROOT, Locale.US, Locale.ENGLISH};
  }

  protected abstract void computeTime();

  protected abstract void computeFields();

  public final Date getTime() {
    return new Date(getTimeInMillis());
  }

  public final void setTime(Date date) {
    setTimeInMillis(date.getTime());
  }

  public long getTimeInMillis() {
    if (!isTimeSet) {
      updateTime();
    }
    return time;
  }

  public void setTimeInMillis(long millis) {
    if (time == millis && isTimeSet && areFieldsSet && areAllFieldsSet) {
      return;
    }
    time = millis;
    isTimeSet = true;
    areFieldsSet = false;
    computeFields();
    areAllFieldsSet = areFieldsSet = true;
  }

  public int get(int field) {
    complete();
    return internalGet(field);
  }

  protected final int internalGet(int field) {
    return fields[field];
  }

  final int internalGet(int field, int defaultValue) {
    return isSet[field] ? fields[field] : defaultValue;
  }

  public void set(int field, int value) {
    if (areFieldsSet && !areAllFieldsSet) {
      computeFields();
    }
    internalSet(field, value);
    isTimeSet = false;
    areFieldsSet = false;
    isSet[field] = true;
    stamp[field] = nextStamp++;
    if (nextStamp == Integer.MAX_VALUE) {
      adjustStamp();
    }
  }

  final void internalSet(int field, int value) {
    fields[field] = value;
  }

  public final void set(int year, int month, int date) {
    set(YEAR, year);
    set(MONTH, month);
    set(DATE, date);
  }

  public final void set(int year, int month, int date, int hourOfDay, int minute) {
    set(YEAR, year);
    set(MONTH, month);
    set(DATE, date);
    set(HOUR_OF_DAY, hourOfDay);
    set(MINUTE, minute);
  }

  public final void set(int year, int month, int date, int hourOfDay, int minute, int second) {
    set(YEAR, year);
    set(MONTH, month);
    set(DATE, date);
    set(HOUR_OF_DAY, hourOfDay);
    set(MINUTE, minute);
    set(SECOND, second);
  }

  public final void clear() {
    for (int i = 0; i < fields.length;) {
      stamp[i] = fields[i] = 0;
      isSet[i++] = false;
    }
    areAllFieldsSet = areFieldsSet = false;
    isTimeSet = false;
  }

  public final void clear(int field) {
    fields[field] = 0;
    stamp[field] = UNSET;
    isSet[field] = false;
    areAllFieldsSet = areFieldsSet = false;
    isTimeSet = false;
  }

  public final boolean isSet(int field) {
    return stamp[field] != UNSET;
  }

  protected void complete() {
    if (!isTimeSet) {
      updateTime();
    }
    if (!areFieldsSet || !areAllFieldsSet) {
      computeFields();
      areAllFieldsSet = areFieldsSet = true;
    }
  }

  final boolean isExternallySet(int field) {
    return stamp[field] >= MINIMUM_USER_STAMP;
  }

  final int getSetStateFields() {
    int mask = 0;
    for (int i = 0; i < fields.length; i++) {
      if (stamp[i] != UNSET) {
        mask |= 1 << i;
      }
    }
    return mask;
  }

  final void setFieldsComputed(int fieldMask) {
    if (fieldMask == ALL_FIELDS) {
      for (int i = 0; i < fields.length; i++) {
        stamp[i] = COMPUTED;
        isSet[i] = true;
      }
      areFieldsSet = areAllFieldsSet = true;
    } else {
      for (int i = 0; i < fields.length; i++) {
        if ((fieldMask & 1) == 1) {
          stamp[i] = COMPUTED;
          isSet[i] = true;
        } else {
          if (areAllFieldsSet && !isSet[i]) {
            areAllFieldsSet = false;
          }
        }
        fieldMask >>>= 1;
      }
    }
  }

  final void setFieldsNormalized(int fieldMask) {
    if (fieldMask != ALL_FIELDS) {
      for (int i = 0; i < fields.length; i++) {
        if ((fieldMask & 1) == 0) {
          stamp[i] = fields[i] = 0;
          isSet[i] = false;
        }
        fieldMask >>= 1;
      }
    }
    areFieldsSet = true;
    areAllFieldsSet = false;
  }

  final boolean isPartiallyNormalized() {
    return areFieldsSet && !areAllFieldsSet;
  }

  final boolean isFullyNormalized() {
    return areFieldsSet && areAllFieldsSet;
  }

  final void setUnnormalized() {
    areFieldsSet = areAllFieldsSet = false;
  }

  static boolean isFieldSet(int fieldMask, int field) {
    return (fieldMask & (1 << field)) != 0;
  }

  final int selectFields() {
    int fieldMask = YEAR_MASK;
    if (stamp[ERA] != UNSET) {
      fieldMask |= ERA_MASK;
    }
    int dowStamp = stamp[DAY_OF_WEEK];
    int monthStamp = stamp[MONTH];
    int domStamp = stamp[DAY_OF_MONTH];
    int womStamp = aggregateStamp(stamp[WEEK_OF_MONTH], dowStamp);
    int dowimStamp = aggregateStamp(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
    int doyStamp = stamp[DAY_OF_YEAR];
    int woyStamp = aggregateStamp(stamp[WEEK_OF_YEAR], dowStamp);
    int bestStamp = domStamp;
    if (womStamp > bestStamp) {
      bestStamp = womStamp;
    }
    if (dowimStamp > bestStamp) {
      bestStamp = dowimStamp;
    }
    if (doyStamp > bestStamp) {
      bestStamp = doyStamp;
    }
    if (woyStamp > bestStamp) {
      bestStamp = woyStamp;
    }
    if (bestStamp == UNSET) {
      womStamp = stamp[WEEK_OF_MONTH];
      dowimStamp = Math.max(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
      woyStamp = stamp[WEEK_OF_YEAR];
      bestStamp = Math.max(Math.max(womStamp, dowimStamp), woyStamp);
      if (bestStamp == UNSET) {
        bestStamp = domStamp = monthStamp;
      }
    }
    if (bestStamp == domStamp || (bestStamp == womStamp && stamp[WEEK_OF_MONTH] >= stamp[WEEK_OF_YEAR])
        || (bestStamp == dowimStamp && stamp[DAY_OF_WEEK_IN_MONTH] >= stamp[WEEK_OF_YEAR])) {
      fieldMask |= MONTH_MASK;
      if (bestStamp == domStamp) {
        fieldMask |= DAY_OF_MONTH_MASK;
      } else {
        if (dowStamp != UNSET) {
          fieldMask |= DAY_OF_WEEK_MASK;
        }
        if (womStamp == dowimStamp) {
          if (stamp[WEEK_OF_MONTH] >= stamp[DAY_OF_WEEK_IN_MONTH]) {
            fieldMask |= WEEK_OF_MONTH_MASK;
          } else {
            fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
          }
        } else {
          if (bestStamp == womStamp) {
            fieldMask |= WEEK_OF_MONTH_MASK;
          } else {
            if (stamp[DAY_OF_WEEK_IN_MONTH] != UNSET) {
              fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
            }
          }
        }
      }
    } else {
      if (bestStamp == doyStamp) {
        fieldMask |= DAY_OF_YEAR_MASK;
      } else {
        if (dowStamp != UNSET) {
          fieldMask |= DAY_OF_WEEK_MASK;
        }
        fieldMask |= WEEK_OF_YEAR_MASK;
      }
    }
    int hourOfDayStamp = stamp[HOUR_OF_DAY];
    int hourStamp = aggregateStamp(stamp[HOUR], stamp[AM_PM]);
    bestStamp = (hourStamp > hourOfDayStamp) ? hourStamp : hourOfDayStamp;
    if (bestStamp == UNSET) {
      bestStamp = Math.max(stamp[HOUR], stamp[AM_PM]);
    }
    if (bestStamp == UNSET || bestStamp == hourOfDayStamp) {
      fieldMask |= HOUR_OF_DAY_MASK;
    } else {
      fieldMask |= HOUR_MASK;
      if (stamp[AM_PM] != UNSET) {
        fieldMask |= AM_PM_MASK;
      }
    }
    if (stamp[MINUTE] != UNSET) {
      fieldMask |= MINUTE_MASK;
    }
    if (stamp[SECOND] != UNSET) {
      fieldMask |= SECOND_MASK;
    }
    if (stamp[MILLISECOND] != UNSET) {
      fieldMask |= MILLISECOND_MASK;
    }
    if (stamp[ZONE_OFFSET] >= MINIMUM_USER_STAMP) {
      fieldMask |= ZONE_OFFSET_MASK;
    }
    if (stamp[DST_OFFSET] >= MINIMUM_USER_STAMP) {
      fieldMask |= DST_OFFSET_MASK;
    }
    return fieldMask;
  }

  private static int aggregateStamp(int stamp_a, int stamp_b) {
    if (stamp_a == UNSET || stamp_b == UNSET) {
      return UNSET;
    }
    return (stamp_a > stamp_b) ? stamp_a : stamp_b;
  }

  private void adjustStamp() {
    int max = MINIMUM_USER_STAMP;
    int newStamp = MINIMUM_USER_STAMP;
    for (;;) {
      int min = Integer.MAX_VALUE;
      for (int i = 0; i < stamp.length; i++) {
        int v = stamp[i];
        if (v >= newStamp && min > v) {
          min = v;
        }
        if (max < v) {
          max = v;
        }
      }
      if (max != min && min == Integer.MAX_VALUE) {
        break;
      }
      if (max == min) {
        stamp[0] = 1;
        max = min = 2;
        break;
      }
      for (int i = 0; i < stamp.length; i++) {
        if (stamp[i] == min) {
          stamp[i] = newStamp;
        }
      }
      newStamp++;
      if (min == max) {
        break;
      }
    }
    nextStamp = newStamp;
  }

  private void updateTime() {
    computeTime();
    isTimeSet = true;
  }

  public String getDisplayName(int field, int style, Locale locale) {
    if (!checkDisplayNameParams(field, style, SHORT, NARROW_FORMAT, locale, ERA_MASK | MONTH_MASK | DAY_OF_WEEK_MASK
        | AM_PM_MASK)) {
      return null;
    }
    int fieldValue = get(field);
    String[] names = namesFor(field, style);
    if (names == null || fieldValue < 0 || fieldValue >= names.length) {
      return null;
    }
    return names[fieldValue];
  }

  public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
    if (!checkDisplayNameParams(field, style, ALL_STYLES, NARROW_FORMAT, locale, ERA_MASK | MONTH_MASK
        | DAY_OF_WEEK_MASK | AM_PM_MASK)) {
      return null;
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    int base = style & ~STANDALONE_MASK;
    int[] styles = base == ALL_STYLES ? new int[] {SHORT, LONG} : new int[] {base};
    for (int st : styles) {
      String[] names = namesFor(field, st);
      if (names == null) {
        continue;
      }
      for (int i = 0; i < names.length; i++) {
        if (names[i] != null && !names[i].isEmpty() && !(field == MONTH && i == UNDECIMBER)) {
          result.put(names[i], i);
        }
      }
    }
    return result.isEmpty() ? null : result;
  }

  private String[] namesFor(int field, int style) {
    int base = style & ~STANDALONE_MASK;
    switch (field) {
      case ERA:
        return base == NARROW_FORMAT ? new String[] {"B", "A"} : new String[] {"BC", "AD"};
      case MONTH:
        if (base == LONG) {
          return new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September",
              "October", "November", "December", ""};
        } else if (base == NARROW_FORMAT) {
          return new String[] {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D", ""};
        }
        return new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", ""};
      case DAY_OF_WEEK:
        if (base == LONG) {
          return new String[] {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        } else if (base == NARROW_FORMAT) {
          return new String[] {"", "S", "M", "T", "W", "T", "F", "S"};
        }
        return new String[] {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
      case AM_PM:
        return base == NARROW_FORMAT ? new String[] {"a", "p"} : new String[] {"AM", "PM"};
      default:
        return null;
    }
  }

  private boolean checkDisplayNameParams(int field, int style, int minStyle, int maxStyle, Locale locale,
      int fieldMask) {
    int baseStyle = style & ~STANDALONE_MASK;
    if (field < 0 || field >= fields.length || baseStyle < minStyle || baseStyle > maxStyle || baseStyle == 3) {
      throw new IllegalArgumentException();
    }
    if (locale == null) {
      throw new NullPointerException();
    }
    return isFieldSet(fieldMask, field);
  }

  abstract public int getMinimum(int field);

  public static Set<String> getAvailableCalendarTypes() {
    return Collections.singleton("gregory");
  }

  public String getCalendarType() {
    return getClass().getName();
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    try {
      Calendar that = (Calendar) obj;
      return compareTo(getMillisOf(that)) == 0 && lenient == that.lenient && firstDayOfWeek == that.firstDayOfWeek
          && minimalDaysInFirstWeek == that.minimalDaysInFirstWeek && zone.equals(that.zone);
    } catch (Exception e) {
      return false;
    }
  }

  public int hashCode() {
    int otheritems = (lenient ? 1 : 0) | (firstDayOfWeek << 1) | (minimalDaysInFirstWeek << 4) | (zone.hashCode() << 7);
    long t = getMillisOf(this);
    return (int) t ^ (int) (t >> 32) ^ otheritems;
  }

  public boolean before(Object when) {
    return when instanceof Calendar && compareTo((Calendar) when) < 0;
  }

  public boolean after(Object when) {
    return when instanceof Calendar && compareTo((Calendar) when) > 0;
  }

  @Override
  public int compareTo(Calendar anotherCalendar) {
    return compareTo(getMillisOf(anotherCalendar));
  }

  public abstract void add(int field, int amount);

  public abstract void roll(int field, boolean up);

  public void roll(int field, int amount) {
    while (amount > 0) {
      roll(field, true);
      amount--;
    }
    while (amount < 0) {
      roll(field, false);
      amount++;
    }
  }

  public void setTimeZone(TimeZone value) {
    zone = value;
    sharedZone = false;
    areAllFieldsSet = areFieldsSet = false;
  }

  public TimeZone getTimeZone() {
    if (sharedZone) {
      zone = (TimeZone) zone.clone();
      sharedZone = false;
    }
    return zone;
  }

  TimeZone getZone() {
    return zone;
  }

  void setZoneShared(boolean shared) {
    sharedZone = shared;
  }

  public void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  public boolean isLenient() {
    return lenient;
  }

  public void setFirstDayOfWeek(int value) {
    if (firstDayOfWeek == value) {
      return;
    }
    firstDayOfWeek = value;
    invalidateWeekFields();
  }

  public int getFirstDayOfWeek() {
    return firstDayOfWeek;
  }

  public void setMinimalDaysInFirstWeek(int value) {
    if (minimalDaysInFirstWeek == value) {
      return;
    }
    minimalDaysInFirstWeek = value;
    invalidateWeekFields();
  }

  public int getMinimalDaysInFirstWeek() {
    return minimalDaysInFirstWeek;
  }

  public boolean isWeekDateSupported() {
    return false;
  }

  public int getWeekYear() {
    throw new UnsupportedOperationException();
  }

  public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
    throw new UnsupportedOperationException();
  }

  public int getWeeksInWeekYear() {
    throw new UnsupportedOperationException();
  }

  private void invalidateWeekFields() {
    if (stamp[WEEK_OF_MONTH] != COMPUTED && stamp[WEEK_OF_YEAR] != COMPUTED) {
      return;
    }
    Calendar cal = (Calendar) clone();
    cal.setLenient(true);
    cal.clear(WEEK_OF_MONTH);
    cal.clear(WEEK_OF_YEAR);
    if (stamp[WEEK_OF_MONTH] == COMPUTED) {
      int weekOfMonth = cal.get(WEEK_OF_MONTH);
      if (fields[WEEK_OF_MONTH] != weekOfMonth) {
        fields[WEEK_OF_MONTH] = weekOfMonth;
      }
    }
    if (stamp[WEEK_OF_YEAR] == COMPUTED) {
      int weekOfYear = cal.get(WEEK_OF_YEAR);
      if (fields[WEEK_OF_YEAR] != weekOfYear) {
        fields[WEEK_OF_YEAR] = weekOfYear;
      }
    }
  }

  abstract public int getMaximum(int field);

  abstract public int getGreatestMinimum(int field);

  abstract public int getLeastMaximum(int field);

  public int getActualMinimum(int field) {
    int fieldValue = getGreatestMinimum(field);
    int endValue = getMinimum(field);
    if (fieldValue == endValue) {
      return fieldValue;
    }
    Calendar work = (Calendar) this.clone();
    work.setLenient(true);
    int result = fieldValue;
    do {
      work.set(field, fieldValue);
      if (work.get(field) != fieldValue) {
        break;
      } else {
        result = fieldValue;
        fieldValue--;
      }
    } while (fieldValue >= endValue);
    return result;
  }

  public int getActualMaximum(int field) {
    int fieldValue = getLeastMaximum(field);
    int endValue = getMaximum(field);
    if (fieldValue == endValue) {
      return fieldValue;
    }
    Calendar work = (Calendar) this.clone();
    work.setLenient(true);
    if (field == WEEK_OF_YEAR || field == WEEK_OF_MONTH || field == DAY_OF_MONTH) {
      work.set(DAY_OF_MONTH, 1);
    }
    int result = fieldValue;
    do {
      work.set(field, fieldValue);
      if (work.get(field) != fieldValue) {
        break;
      } else {
        result = fieldValue;
        fieldValue++;
      }
    } while (fieldValue <= endValue);
    return result;
  }

  @Override
  public Object clone() {
    try {
      Calendar other = (Calendar) super.clone();
      other.fields = new int[FIELD_COUNT];
      other.isSet = new boolean[FIELD_COUNT];
      other.stamp = new int[FIELD_COUNT];
      for (int i = 0; i < FIELD_COUNT; i++) {
        other.fields[i] = fields[i];
        other.stamp[i] = stamp[i];
        other.isSet[i] = isSet[i];
      }
      other.zone = (TimeZone) zone.clone();
      return other;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public String toString() {
    StringBuilder buffer = new StringBuilder(800);
    buffer.append(getClass().getName()).append('[');
    appendValue(buffer, "time", isTimeSet, time);
    buffer.append(",areFieldsSet=").append(areFieldsSet);
    buffer.append(",areAllFieldsSet=").append(areAllFieldsSet);
    buffer.append(",lenient=").append(lenient);
    buffer.append(",zone=").append(zone);
    appendValue(buffer, ",firstDayOfWeek", true, (long) firstDayOfWeek);
    appendValue(buffer, ",minimalDaysInFirstWeek", true, (long) minimalDaysInFirstWeek);
    for (int i = 0; i < FIELD_COUNT; ++i) {
      buffer.append(',');
      appendValue(buffer, kFieldNames[i], isSet(i), (long) fields[i]);
    }
    buffer.append(']');
    return buffer.toString();
  }

  private static void appendValue(StringBuilder sb, String item, boolean valid, long value) {
    sb.append(item).append('=');
    if (valid) {
      sb.append(value);
    } else {
      sb.append('?');
    }
  }

  static String getFieldName(int field) {
    return kFieldNames[field];
  }

  public final Instant toInstant() {
    return Instant.ofEpochMilli(getTimeInMillis());
  }

  static long getMillisOf(Calendar calendar) {
    if (calendar.isTimeSet) {
      return calendar.time;
    }
    Calendar cal = (Calendar) calendar.clone();
    cal.setLenient(true);
    return cal.getTimeInMillis();
  }

  private int compareTo(long t) {
    long thisTime = getMillisOf(this);
    return (thisTime > t) ? 1 : (thisTime == t) ? 0 : -1;
  }

  public static class Builder {
    private static final int NFIELDS = FIELD_COUNT + 1;
    private static final int WEEK_YEAR = FIELD_COUNT;

    private long instant;
    private int[] fields;
    private int nextStamp;
    private int maxFieldIndex;
    private String type;
    private TimeZone zone;
    private boolean lenient = true;
    private Locale locale;
    private int firstDayOfWeek;
    private int minimalDaysInFirstWeek;

    public Builder() { }

    public Builder setInstant(long instant) {
      if (fields != null) {
        throw new IllegalStateException();
      }
      this.instant = instant;
      nextStamp = COMPUTED;
      return this;
    }

    public Builder setInstant(Date instant) {
      return setInstant(instant.getTime());
    }

    public Builder set(int field, int value) {
      if (field < 0 || field >= FIELD_COUNT) {
        throw new IllegalArgumentException("field is invalid");
      }
      if (isInstantSet()) {
        throw new IllegalStateException("instant has been set");
      }
      allocateFields();
      internalSet(field, value);
      return this;
    }

    public Builder setFields(int... fieldValuePairs) {
      int len = fieldValuePairs.length;
      if ((len % 2) != 0) {
        throw new IllegalArgumentException();
      }
      if (isInstantSet()) {
        throw new IllegalStateException("instant has been set");
      }
      if ((nextStamp + len / 2) < 0) {
        throw new IllegalStateException("stamp counter overflow");
      }
      allocateFields();
      for (int i = 0; i < len;) {
        int field = fieldValuePairs[i++];
        if (field < 0 || field >= FIELD_COUNT) {
          throw new IllegalArgumentException("field is invalid");
        }
        internalSet(field, fieldValuePairs[i++]);
      }
      return this;
    }

    public Builder setDate(int year, int month, int dayOfMonth) {
      return setFields(YEAR, year, MONTH, month, DAY_OF_MONTH, dayOfMonth);
    }

    public Builder setTimeOfDay(int hourOfDay, int minute, int second) {
      return setTimeOfDay(hourOfDay, minute, second, 0);
    }

    public Builder setTimeOfDay(int hourOfDay, int minute, int second, int millis) {
      return setFields(HOUR_OF_DAY, hourOfDay, MINUTE, minute, SECOND, second, MILLISECOND, millis);
    }

    public Builder setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
      allocateFields();
      internalSet(WEEK_YEAR, weekYear);
      internalSet(WEEK_OF_YEAR, weekOfYear);
      internalSet(DAY_OF_WEEK, dayOfWeek);
      return this;
    }

    public Builder setTimeZone(TimeZone zone) {
      if (zone == null) {
        throw new NullPointerException();
      }
      this.zone = zone;
      return this;
    }

    public Builder setLenient(boolean lenient) {
      this.lenient = lenient;
      return this;
    }

    public Builder setCalendarType(String type) {
      if (type.equals("gregorian")) {
        type = "gregory";
      }
      if (!Calendar.getAvailableCalendarTypes().contains(type) && !type.equals("iso8601")) {
        throw new IllegalArgumentException("unknown calendar type: " + type);
      }
      if (this.type == null) {
        this.type = type;
      } else {
        if (!this.type.equals(type)) {
          throw new IllegalStateException("calendar type override");
        }
      }
      return this;
    }

    public Builder setLocale(Locale locale) {
      if (locale == null) {
        throw new NullPointerException();
      }
      this.locale = locale;
      return this;
    }

    public Builder setWeekDefinition(int firstDayOfWeek, int minimalDaysInFirstWeek) {
      if (!isValidWeekParameter(firstDayOfWeek) || !isValidWeekParameter(minimalDaysInFirstWeek)) {
        throw new IllegalArgumentException();
      }
      this.firstDayOfWeek = firstDayOfWeek;
      this.minimalDaysInFirstWeek = minimalDaysInFirstWeek;
      return this;
    }

    public Calendar build() {
      if (zone == null) {
        zone = TimeZone.getDefault();
      }
      if (locale == null) {
        locale = Locale.getDefault();
      }
      if (type == null) {
        type = "gregory";
      }
      GregorianCalendar cal = new GregorianCalendar(zone, locale);
      cal.clear();
      cal.setLenient(lenient);
      if (type.equals("iso8601")) {
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        setWeekDefinition(MONDAY, 4);
      }
      if (isInstantSet()) {
        cal.setTimeInMillis(instant);
        cal.complete();
        return cal;
      }
      if (fields != null) {
        boolean weekDate = isSet(WEEK_YEAR) && fields[WEEK_YEAR] > fields[YEAR];
        if (weekDate && !cal.isWeekDateSupported()) {
          throw new IllegalArgumentException("week date is unsupported by " + type);
        }
        for (int stamp = MINIMUM_USER_STAMP; stamp < nextStamp; stamp++) {
          for (int index = 0; index <= maxFieldIndex; index++) {
            if (fields[index] == stamp) {
              cal.set(index, fields[NFIELDS + index]);
              break;
            }
          }
        }
        if (weekDate) {
          int weekOfYear = isSet(WEEK_OF_YEAR) ? fields[NFIELDS + WEEK_OF_YEAR] : 1;
          int dayOfWeek = isSet(DAY_OF_WEEK) ? fields[NFIELDS + DAY_OF_WEEK] : cal.getFirstDayOfWeek();
          cal.setWeekDate(fields[NFIELDS + WEEK_YEAR], weekOfYear, dayOfWeek);
        }
        cal.complete();
      }
      return cal;
    }

    private void allocateFields() {
      if (fields == null) {
        fields = new int[NFIELDS * 2];
        nextStamp = MINIMUM_USER_STAMP;
        maxFieldIndex = -1;
      }
    }

    private void internalSet(int field, int value) {
      fields[field] = nextStamp++;
      if (nextStamp < 0) {
        throw new IllegalStateException("stamp counter overflow");
      }
      fields[NFIELDS + field] = value;
      if (field > maxFieldIndex && field < WEEK_YEAR) {
        maxFieldIndex = field;
      }
    }

    private boolean isInstantSet() {
      return nextStamp == COMPUTED;
    }

    private boolean isSet(int index) {
      return fields != null && fields[index] > UNSET;
    }

    private boolean isValidWeekParameter(int value) {
      return value > 0 && value <= 7;
    }
  }
}
