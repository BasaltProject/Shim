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

  protected Calendar calendar;
  protected NumberFormat numberFormat;
  private boolean lenient = true;

  protected DateFormat() {
    this.calendar = Calendar.getInstance();
    this.numberFormat = NumberFormat.getIntegerInstance();
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    if (obj instanceof Date) {
      return format((Date) obj, toAppendTo, fieldPosition);
    } else if (obj instanceof Number) {
      return format(new Date(((Number) obj).longValue()), toAppendTo, fieldPosition);
    } else {
      throw new IllegalArgumentException("Cannot format given Object as a Date");
    }
  }

  public final String format(Date date) {
    return format(date, new StringBuffer(), new FieldPosition(0)).toString();
  }

  public abstract StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition);

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

  public static DateFormat getTimeInstance() {
    return getTimeInstance(DEFAULT, Locale.getDefault());
  }

  public static DateFormat getTimeInstance(int style) {
    return getTimeInstance(style, Locale.getDefault());
  }

  public static DateFormat getTimeInstance(int style, Locale aLocale) {
    String pattern = "HH:mm:ss";
    if (style == SHORT) {
      pattern = "HH:mm";
    }
    return new SimpleDateFormat(pattern, aLocale);
  }

  public static DateFormat getDateInstance() {
    return getDateInstance(DEFAULT, Locale.getDefault());
  }

  public static DateFormat getDateInstance(int style) {
    return getDateInstance(style, Locale.getDefault());
  }

  public static DateFormat getDateInstance(int style, Locale aLocale) {
    String pattern = "yyyy-MM-dd";
    if (style == SHORT) {
      pattern = "yy-MM-dd";
    }
    return new SimpleDateFormat(pattern, aLocale);
  }

  public static DateFormat getDateTimeInstance() {
    return getDateTimeInstance(DEFAULT, DEFAULT, Locale.getDefault());
  }

  public static DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
    return getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault());
  }

  public static DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
    String dPat = (dateStyle == SHORT) ? "yy-MM-dd" : "yyyy-MM-dd";
    String tPat = (timeStyle == SHORT) ? "HH:mm" : "HH:mm:ss";
    return new SimpleDateFormat(dPat + " " + tPat, aLocale);
  }

  public static DateFormat getInstance() {
    return getDateTimeInstance(SHORT, SHORT);
  }

  public static Locale[] getAvailableLocales() {
    return new Locale[] {Locale.getDefault(), Locale.ROOT, Locale.US};
  }

  public void setCalendar(Calendar newCalendar) {
    this.calendar = Objects.requireNonNull(newCalendar, "newCalendar");
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public void setNumberFormat(NumberFormat newNumberFormat) {
    this.numberFormat = Objects.requireNonNull(newNumberFormat, "newNumberFormat");
  }

  public NumberFormat getNumberFormat() {
    return numberFormat;
  }

  public void setTimeZone(TimeZone zone) {
    if (calendar != null) {
      calendar.setTime(new Date());
    }
  }

  public TimeZone getTimeZone() {
    return TimeZone.getDefault();
  }

  public void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  public boolean isLenient() {
    return lenient;
  }

  @Override
  public Object clone() {
    return super.clone();
  }
}
