/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public abstract class DateFormat extends Format {
  private static final long serialVersionUID = 7218322351708990498L;

  public static final int FULL = 0;
  public static final int LONG = 1;
  public static final int MEDIUM = 2;
  public static final int SHORT = 3;
  public static final int DEFAULT = MEDIUM;

  public static final int ERA_FIELD = 0;
  public static final int YEAR_FIELD = 1;
  public static final int MONTH_FIELD = 2;
  public static final int DATE_FIELD = 3;
  public static final int HOUR_OF_DAY1_FIELD = 4;
  public static final int HOUR_OF_DAY0_FIELD = 5;
  public static final int MINUTE_FIELD = 6;
  public static final int SECOND_FIELD = 7;
  public static final int MILLISECOND_FIELD = 8;
  public static final int DAY_OF_WEEK_FIELD = 9;
  public static final int DAY_OF_YEAR_FIELD = 10;
  public static final int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
  public static final int WEEK_OF_YEAR_FIELD = 12;
  public static final int WEEK_OF_MONTH_FIELD = 13;
  public static final int AM_PM_FIELD = 14;
  public static final int HOUR1_FIELD = 15;
  public static final int HOUR0_FIELD = 16;
  public static final int TIMEZONE_FIELD = 17;

  private static final String kNarrowNoBreak = "\u202f";
  private static final String[] kDatePatterns = {"EEEE, MMMM d, y", "MMMM d, y", "MMM d, y", "M/d/yy"};
  private static final String[] kTimePatterns = {"h:mm:ss" + kNarrowNoBreak + "a zzzz",
      "h:mm:ss" + kNarrowNoBreak + "a z", "h:mm:ss" + kNarrowNoBreak + "a", "h:mm" + kNarrowNoBreak + "a"};

  protected Calendar calendar;
  protected NumberFormat numberFormat;

  protected DateFormat() { }

  @Override
  public final StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    if (obj instanceof Date) {
      return format((Date) obj, toAppendTo, fieldPosition);
    } else if (obj instanceof Number) {
      return format(new Date(((Number) obj).longValue()), toAppendTo, fieldPosition);
    } else {
      throw new IllegalArgumentException("Cannot format given Object as a Date");
    }
  }

  public abstract StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition);

  public final String format(Date date) {
    return format(date, new StringBuffer(), new FieldPosition(0)).toString();
  }

  public Date parse(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Date result = parse(source, pos);
    if (pos.getIndex() == 0) {
      throw new ParseException("Unparseable date: \"" + source + "\"", pos.getErrorIndex());
    }
    return result;
  }

  public abstract Date parse(String source, ParsePosition pos);

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    return parse(source, pos);
  }

  private static void checkStyle(int style) {
    if (style < FULL || style > SHORT) {
      throw new IllegalArgumentException("Illegal date style " + style);
    }
  }

  private static void checkTimeStyle(int style) {
    if (style < FULL || style > SHORT) {
      throw new IllegalArgumentException("Illegal time style " + style);
    }
  }

  public static final DateFormat getTimeInstance() {
    return getTimeInstance(DEFAULT, Locale.getDefault(Locale.Category.FORMAT));
  }

  public static final DateFormat getTimeInstance(int style) {
    return getTimeInstance(style, Locale.getDefault(Locale.Category.FORMAT));
  }

  public static final DateFormat getTimeInstance(int style, Locale aLocale) {
    checkTimeStyle(style);
    return new SimpleDateFormat(kTimePatterns[style], aLocale);
  }

  public static final DateFormat getDateInstance() {
    return getDateInstance(DEFAULT, Locale.getDefault(Locale.Category.FORMAT));
  }

  public static final DateFormat getDateInstance(int style) {
    return getDateInstance(style, Locale.getDefault(Locale.Category.FORMAT));
  }

  public static final DateFormat getDateInstance(int style, Locale aLocale) {
    checkStyle(style);
    return new SimpleDateFormat(kDatePatterns[style], aLocale);
  }

  public static final DateFormat getDateTimeInstance() {
    return getDateTimeInstance(DEFAULT, DEFAULT, Locale.getDefault(Locale.Category.FORMAT));
  }

  public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
    return getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault(Locale.Category.FORMAT));
  }

  public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
    checkStyle(dateStyle);
    checkTimeStyle(timeStyle);
    return new SimpleDateFormat(kDatePatterns[dateStyle] + ", " + kTimePatterns[timeStyle], aLocale);
  }

  public static final DateFormat getInstance() {
    return getDateTimeInstance(SHORT, SHORT);
  }

  public static Locale[] getAvailableLocales() {
    return new Locale[] {Locale.ROOT, Locale.US};
  }

  public void setCalendar(Calendar newCalendar) {
    this.calendar = Objects.requireNonNull(newCalendar, "newCalendar");
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public void setNumberFormat(NumberFormat newNumberFormat) {
    this.numberFormat = newNumberFormat;
  }

  public NumberFormat getNumberFormat() {
    return numberFormat;
  }

  public void setTimeZone(TimeZone zone) {
    calendar.setTimeZone(zone);
  }

  public TimeZone getTimeZone() {
    return calendar.getTimeZone();
  }

  public void setLenient(boolean lenient) {
    calendar.setLenient(lenient);
  }

  public boolean isLenient() {
    return calendar.isLenient();
  }

  @Override
  public int hashCode() {
    return numberFormat == null ? 0 : numberFormat.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    DateFormat other = (DateFormat) obj;
    return calendar.getFirstDayOfWeek() == other.calendar.getFirstDayOfWeek()
        && calendar.getMinimalDaysInFirstWeek() == other.calendar.getMinimalDaysInFirstWeek()
        && calendar.isLenient() == other.calendar.isLenient()
        && calendar.getTimeZone().equals(other.calendar.getTimeZone())
        && Objects.equals(numberFormat, other.numberFormat);
  }

  @Override
  public Object clone() {
    DateFormat other = (DateFormat) super.clone();
    other.calendar = (Calendar) calendar.clone();
    if (numberFormat != null) {
      other.numberFormat = (NumberFormat) numberFormat.clone();
    }
    return other;
  }
}
