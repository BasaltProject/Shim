/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.text.DateFormatSymbols;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatConversionException;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;

public final class FormatterDates {
  private FormatterDates() { }

  private static String pad(long value, int digits) {
    boolean neg = value < 0;
    StringBuilder sb = new StringBuilder(Long.toString(neg ? -value : value));
    while (sb.length() < digits) sb.insert(0, '0');
    if (neg) sb.insert(0, '-');
    return sb.toString();
  }

  private interface Fields {
    int hourOfDay();
    int clockHourOfAmPm();
    int minute();
    int second();
    int milli();
    long nano();
    int amPm();
    int offsetSeconds();
    String zoneName(Locale l);
    long epochSeconds();
    long epochMillis();
    int month();
    int dayOfMonth();
    int dayOfWeek();
    int dayOfYear();
    int year();

    Class<?> type();
  }

  private static final class CalendarFields implements Fields {
    private final Calendar c;

    CalendarFields(Calendar c) {
      this.c = c;
    }

    public int hourOfDay() {
      return c.get(Calendar.HOUR_OF_DAY);
    }

    public int clockHourOfAmPm() {
      int h = c.get(Calendar.HOUR);
      return h == 0 ? 12 : h;
    }

    public int minute() {
      return c.get(Calendar.MINUTE);
    }

    public int second() {
      return c.get(Calendar.SECOND);
    }

    public int milli() {
      return c.get(Calendar.MILLISECOND);
    }

    public long nano() {
      return c.get(Calendar.MILLISECOND) * 1000000L;
    }

    public int amPm() {
      return c.get(Calendar.AM_PM);
    }

    public int offsetSeconds() {
      return (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 1000;
    }

    public String zoneName(Locale l) {
      return c.getTimeZone().getDisplayName(c.get(Calendar.DST_OFFSET) != 0, TimeZone.SHORT, l);
    }

    public long epochSeconds() {
      return c.getTimeInMillis() / 1000L;
    }

    public long epochMillis() {
      return c.getTimeInMillis();
    }

    public int month() {
      return c.get(Calendar.MONTH) + 1;
    }

    public int dayOfMonth() {
      return c.get(Calendar.DAY_OF_MONTH);
    }

    public int dayOfWeek() {
      return c.get(Calendar.DAY_OF_WEEK);
    }

    public int dayOfYear() {
      return c.get(Calendar.DAY_OF_YEAR);
    }

    public int year() {
      return c.get(Calendar.YEAR);
    }

    public Class<?> type() {
      return c.getClass();
    }
  }

  private static final class TemporalFields implements Fields {
    private final TemporalAccessor t;

    TemporalFields(TemporalAccessor t) {
      this.t = t;
    }

    public int hourOfDay() {
      return t.get(ChronoField.HOUR_OF_DAY);
    }

    public int clockHourOfAmPm() {
      return t.get(ChronoField.CLOCK_HOUR_OF_AMPM);
    }

    public int minute() {
      return t.get(ChronoField.MINUTE_OF_HOUR);
    }

    public int second() {
      return t.get(ChronoField.SECOND_OF_MINUTE);
    }

    public int milli() {
      return t.get(ChronoField.MILLI_OF_SECOND);
    }

    public long nano() {
      return t.getLong(ChronoField.NANO_OF_SECOND);
    }

    public int amPm() {
      return t.get(ChronoField.AMPM_OF_DAY);
    }

    public int offsetSeconds() {
      return t.get(ChronoField.OFFSET_SECONDS);
    }

    public String zoneName(Locale l) {
      ZoneId zid = t.query(TemporalQueries.zone());
      if (zid == null) {
        throw new DateTimeException("no zone");
      }
      if (!(zid instanceof ZoneOffset) && t.isSupported(ChronoField.INSTANT_SECONDS)) {
        Instant instant = Instant.from(t);
        return TimeZone.getTimeZone(zid.getId()).getDisplayName(zid.getRules().isDaylightSavings(instant),
            TimeZone.SHORT, l);
      }
      return zid.getId();
    }

    public long epochSeconds() {
      return t.getLong(ChronoField.INSTANT_SECONDS);
    }

    public long epochMillis() {
      return t.getLong(ChronoField.INSTANT_SECONDS) * 1000L + t.get(ChronoField.MILLI_OF_SECOND);
    }

    public int month() {
      return t.get(ChronoField.MONTH_OF_YEAR);
    }

    public int dayOfMonth() {
      return t.get(ChronoField.DAY_OF_MONTH);
    }

    public int dayOfWeek() {
      return (t.get(ChronoField.DAY_OF_WEEK) % 7) + 1;
    }

    public int dayOfYear() {
      return t.get(ChronoField.DAY_OF_YEAR);
    }

    public int year() {
      return t.get(ChronoField.YEAR_OF_ERA);
    }

    public Class<?> type() {
      return t.getClass();
    }
  }

  public static String format(char conversion, Object arg, Locale locale) {
    Locale l = locale == null ? Locale.US : locale;
    Fields f;
    if (arg instanceof Long) {
      f = new CalendarFields(calendar(((Long) arg).longValue(), l));
    } else if (arg instanceof Date) {
      f = new CalendarFields(calendar(((Date) arg).getTime(), l));
    } else if (arg instanceof Calendar) {
      f = new CalendarFields((Calendar) arg);
    } else if (arg instanceof TemporalAccessor) {
      f = new TemporalFields((TemporalAccessor) arg);
    } else {
      throw new IllegalArgumentException();
    }
    StringBuilder sb = new StringBuilder();
    print(sb, f, conversion, l);
    return sb.toString();
  }

  private static Calendar calendar(long millis, Locale l) {
    Calendar cal = Calendar.getInstance(l);
    cal.setTimeInMillis(millis);
    return cal;
  }

  private static void print(StringBuilder sb, Fields t, char c, Locale l) {
    try {
      printField(sb, t, c, l);
    } catch (DateTimeException x) {
      throw new IllegalFormatConversionException(c, t.type());
    }
  }

  private static void printField(StringBuilder sb, Fields t, char c, Locale l) {
    switch (c) {
    case 'H':
      sb.append(pad(t.hourOfDay(), 2));
      break;
    case 'I':
      sb.append(pad(t.clockHourOfAmPm(), 2));
      break;
    case 'k':
      sb.append(t.hourOfDay());
      break;
    case 'l':
      sb.append(t.clockHourOfAmPm());
      break;
    case 'M':
      sb.append(pad(t.minute(), 2));
      break;
    case 'S':
      sb.append(pad(t.second(), 2));
      break;
    case 'L':
      sb.append(pad(t.milli(), 3));
      break;
    case 'N':
      sb.append(pad(t.nano(), 9));
      break;
    case 'p': {
      String[] ampm = {"AM", "PM"};
      if (!Locale.US.equals(l)) {
        ampm = DateFormatSymbols.getInstance(l).getAmPmStrings();
      }
      sb.append(ampm[t.amPm()].toLowerCase(Objects.requireNonNullElse(l, Locale.US)));
      break;
    }
    case 'z': {
      int i = t.offsetSeconds();
      boolean neg = i < 0;
      sb.append(neg ? '-' : '+');
      if (neg) {
        i = -i;
      }
      int min = i / 60;
      int offset = (min / 60) * 100 + (min % 60);
      sb.append(pad(offset, 4));
      break;
    }
    case 'Z':
      sb.append(t.zoneName(l));
      break;
    case 's':
      sb.append(t.epochSeconds());
      break;
    case 'Q':
      sb.append(t.epochMillis());
      break;
    case 'B':
      sb.append(DateFormatSymbols.getInstance(l).getMonths()[t.month() - 1]);
      break;
    case 'b':
    case 'h':
      sb.append(DateFormatSymbols.getInstance(l).getShortMonths()[t.month() - 1]);
      break;
    case 'A':
      sb.append(DateFormatSymbols.getInstance(l).getWeekdays()[t.dayOfWeek()]);
      break;
    case 'a':
      sb.append(DateFormatSymbols.getInstance(l).getShortWeekdays()[t.dayOfWeek()]);
      break;
    case 'C':
      sb.append(pad(t.year() / 100, 2));
      break;
    case 'Y':
      sb.append(pad(t.year(), 4));
      break;
    case 'y':
      sb.append(pad(t.year() % 100, 2));
      break;
    case 'j':
      sb.append(pad(t.dayOfYear(), 3));
      break;
    case 'm':
      sb.append(pad(t.month(), 2));
      break;
    case 'd':
      sb.append(pad(t.dayOfMonth(), 2));
      break;
    case 'e':
      sb.append(t.dayOfMonth());
      break;
    case 'R':
      print(sb, t, 'H', l);
      sb.append(':');
      print(sb, t, 'M', l);
      break;
    case 'T':
      print(sb, t, 'H', l);
      sb.append(':');
      print(sb, t, 'M', l);
      sb.append(':');
      print(sb, t, 'S', l);
      break;
    case 'r': {
      print(sb, t, 'I', l);
      sb.append(':');
      print(sb, t, 'M', l);
      sb.append(':');
      print(sb, t, 'S', l);
      sb.append(' ');
      StringBuilder tmp = new StringBuilder();
      print(tmp, t, 'p', l);
      sb.append(tmp.toString().toUpperCase(Objects.requireNonNullElse(l, Locale.US)));
      break;
    }
    case 'D':
      print(sb, t, 'm', l);
      sb.append('/');
      print(sb, t, 'd', l);
      sb.append('/');
      print(sb, t, 'y', l);
      break;
    case 'F':
      try {
        print(sb, t, 'Y', l);
      } catch (IllegalFormatConversionException x) {
        throw new IllegalFormatConversionException('F', t.type());
      }
      sb.append('-');
      print(sb, t, 'm', l);
      sb.append('-');
      print(sb, t, 'd', l);
      break;
    case 'c':
      print(sb, t, 'a', l);
      sb.append(' ');
      print(sb, t, 'b', l);
      sb.append(' ');
      print(sb, t, 'd', l);
      sb.append(' ');
      print(sb, t, 'T', l);
      sb.append(' ');
      print(sb, t, 'Z', l);
      sb.append(' ');
      print(sb, t, 'Y', l);
      break;
    default:
      throw new UnknownFormatConversionException("t" + c);
    }
  }
}
