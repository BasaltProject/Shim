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

public class Date implements Serializable, Cloneable, Comparable<Date> {
  private static final long serialVersionUID = 7523967970034938905L;

  private static final String[] kDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final String[] kMonths = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
      "Nov", "Dec"};

  private transient long fastTime;

  public Date() {
    this(System.currentTimeMillis());
  }

  public Date(long date) {
    fastTime = date;
  }

  @Deprecated
  public Date(int year, int month, int date) {
    this(year, month, date, 0, 0, 0);
  }

  @Deprecated
  public Date(int year, int month, int date, int hrs, int min) {
    this(year, month, date, hrs, min, 0);
  }

  @Deprecated
  public Date(int year, int month, int date, int hrs, int min, int sec) {
    int y = year + 1900;
    if (month >= 12) {
      y += month / 12;
      month %= 12;
    } else if (month < 0) {
      y += Math.floorDiv(month, 12);
      month = Math.floorMod(month, 12);
    }
    GregorianCalendar cal = new GregorianCalendar(y, month, date, hrs, min, sec);
    fastTime = cal.getTimeInMillis();
  }

  @Deprecated
  public Date(String s) {
    this(parse(s));
  }

  @Override
  public Object clone() {
    Date d = null;
    try {
      d = (Date) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
    return d;
  }

  @Deprecated
  public static long UTC(int year, int month, int date, int hrs, int min, int sec) {
    int y = year + 1900;
    if (month >= 12) {
      y += month / 12;
      month %= 12;
    } else if (month < 0) {
      y += Math.floorDiv(month, 12);
      month = Math.floorMod(month, 12);
    }
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    cal.clear();
    cal.set(y, month, date, hrs, min, sec);
    return cal.getTimeInMillis();
  }

  @Deprecated
  public static long parse(String s) {
    throw new IllegalArgumentException();
  }

  private GregorianCalendar calendar() {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeInMillis(fastTime);
    return cal;
  }

  @Deprecated
  public int getYear() {
    return calendar().get(Calendar.YEAR) - 1900;
  }

  @Deprecated
  public void setYear(int year) {
    GregorianCalendar cal = calendar();
    cal.set(Calendar.YEAR, year + 1900);
    fastTime = cal.getTimeInMillis();
  }

  @Deprecated
  public int getMonth() {
    return calendar().get(Calendar.MONTH);
  }

  @Deprecated
  public void setMonth(int month) {
    GregorianCalendar cal = calendar();
    int y = cal.get(Calendar.YEAR);
    if (month >= 12) {
      y += month / 12;
      month %= 12;
    } else if (month < 0) {
      y += Math.floorDiv(month, 12);
      month = Math.floorMod(month, 12);
    }
    cal.set(Calendar.YEAR, y);
    cal.set(Calendar.MONTH, month);
    fastTime = cal.getTimeInMillis();
  }

  @Deprecated
  public int getDate() {
    return calendar().get(Calendar.DAY_OF_MONTH);
  }

  @Deprecated
  public void setDate(int date) {
    GregorianCalendar cal = calendar();
    cal.set(Calendar.DAY_OF_MONTH, date);
    fastTime = cal.getTimeInMillis();
  }

  @Deprecated
  public int getDay() {
    return calendar().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
  }

  @Deprecated
  public int getHours() {
    return calendar().get(Calendar.HOUR_OF_DAY);
  }

  @Deprecated
  public void setHours(int hours) {
    GregorianCalendar cal = calendar();
    cal.set(Calendar.HOUR_OF_DAY, hours);
    fastTime = cal.getTimeInMillis();
  }

  @Deprecated
  public int getMinutes() {
    return calendar().get(Calendar.MINUTE);
  }

  @Deprecated
  public void setMinutes(int minutes) {
    GregorianCalendar cal = calendar();
    cal.set(Calendar.MINUTE, minutes);
    fastTime = cal.getTimeInMillis();
  }

  @Deprecated
  public int getSeconds() {
    return calendar().get(Calendar.SECOND);
  }

  @Deprecated
  public void setSeconds(int seconds) {
    GregorianCalendar cal = calendar();
    cal.set(Calendar.SECOND, seconds);
    fastTime = cal.getTimeInMillis();
  }

  public long getTime() {
    return fastTime;
  }

  public void setTime(long time) {
    fastTime = time;
  }

  public boolean before(Date when) {
    return getTime() < when.getTime();
  }

  public boolean after(Date when) {
    return getTime() > when.getTime();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Date && getTime() == ((Date) obj).getTime();
  }

  @Override
  public int compareTo(Date anotherDate) {
    return Long.compare(getTime(), anotherDate.getTime());
  }

  @Override
  public int hashCode() {
    long ht = getTime();
    return (int) ht ^ (int) (ht >> 32);
  }

  @Override
  public String toString() {
    GregorianCalendar cal = calendar();
    TimeZone zone = cal.getTimeZone();
    boolean daylight = zone.inDaylightTime(this);
    StringBuilder sb = new StringBuilder(28);
    sb.append(kDays[cal.get(Calendar.DAY_OF_WEEK) - 1]).append(' ');
    sb.append(kMonths[cal.get(Calendar.MONTH)]).append(' ');
    int d = cal.get(Calendar.DAY_OF_MONTH);
    sb.append(d < 10 ? "0" : "").append(d).append(' ');
    int h = cal.get(Calendar.HOUR_OF_DAY);
    int m = cal.get(Calendar.MINUTE);
    int s = cal.get(Calendar.SECOND);
    sb.append(h < 10 ? "0" : "").append(h).append(m < 10 ? ":0" : ":").append(m).append(s < 10 ? ":0" : ":")
        .append(s).append(' ');
    sb.append(zone.getDisplayName(daylight, TimeZone.SHORT, Locale.US)).append(' ');
    sb.append(cal.get(Calendar.YEAR));
    return sb.toString();
  }

  @Deprecated
  public String toLocaleString() {
    GregorianCalendar cal = calendar();
    int h = cal.get(Calendar.HOUR);
    if (h == 0) {
      h = 12;
    }
    int m = cal.get(Calendar.MINUTE);
    int s = cal.get(Calendar.SECOND);
    return kMonths[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR)
        + ", " + h + (m < 10 ? ":0" : ":") + m + (s < 10 ? ":0" : ":") + s + "\u202f"
        + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
  }

  @Deprecated
  public String toGMTString() {
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    cal.setTimeInMillis(fastTime);
    int h = cal.get(Calendar.HOUR_OF_DAY);
    int m = cal.get(Calendar.MINUTE);
    int s = cal.get(Calendar.SECOND);
    return cal.get(Calendar.DAY_OF_MONTH) + " " + kMonths[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR)
        + " " + (h < 10 ? "0" : "") + h + (m < 10 ? ":0" : ":") + m + (s < 10 ? ":0" : ":") + s + " GMT";
  }

  @Deprecated
  public int getTimezoneOffset() {
    GregorianCalendar cal = calendar();
    return -(cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
  }

  public static Date from(Instant instant) {
    try {
      return new Date(instant.toEpochMilli());
    } catch (ArithmeticException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  public Instant toInstant() {
    return Instant.ofEpochMilli(getTime());
  }
}
