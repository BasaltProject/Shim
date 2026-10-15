/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DateTimeFormatter {
  private enum FormatKind {
    ISO_LOCAL_DATE,
    ISO_LOCAL_TIME,
    ISO_LOCAL_DATE_TIME,
    ISO_INSTANT,
    BASIC_ISO_DATE,
    PATTERN
  }

  public static final DateTimeFormatter ISO_LOCAL_DATE = new DateTimeFormatter(FormatKind.ISO_LOCAL_DATE, null, Locale.getDefault(), null);
  public static final DateTimeFormatter ISO_LOCAL_TIME = new DateTimeFormatter(FormatKind.ISO_LOCAL_TIME, null, Locale.getDefault(), null);
  public static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatter(FormatKind.ISO_LOCAL_DATE_TIME, null, Locale.getDefault(), null);
  public static final DateTimeFormatter ISO_INSTANT = new DateTimeFormatter(FormatKind.ISO_INSTANT, null, Locale.getDefault(), ZoneOffset.UTC);
  public static final DateTimeFormatter ISO_DATE = ISO_LOCAL_DATE;
  public static final DateTimeFormatter ISO_TIME = ISO_LOCAL_TIME;
  public static final DateTimeFormatter ISO_DATE_TIME = ISO_LOCAL_DATE_TIME;
  public static final DateTimeFormatter BASIC_ISO_DATE = new DateTimeFormatter(FormatKind.BASIC_ISO_DATE, null, Locale.getDefault(), null);

  private final FormatKind kind;
  private final String pattern;
  private final Locale locale;
  private final ZoneId zone;

  private DateTimeFormatter(FormatKind kind, String pattern, Locale locale, ZoneId zone) {
    this.kind = kind;
    this.pattern = pattern;
    this.locale = locale;
    this.zone = zone;
  }

  public static DateTimeFormatter ofPattern(String pattern) {
    Objects.requireNonNull(pattern, "pattern");
    return new DateTimeFormatter(FormatKind.PATTERN, pattern, Locale.getDefault(), null);
  }

  public static DateTimeFormatter ofPattern(String pattern, Locale locale) {
    Objects.requireNonNull(pattern, "pattern");
    Objects.requireNonNull(locale, "locale");
    return new DateTimeFormatter(FormatKind.PATTERN, pattern, locale, null);
  }

  public Locale getLocale() {
    return locale;
  }

  public DateTimeFormatter withLocale(Locale locale) {
    if (this.locale.equals(locale)) {
      return this;
    }
    return new DateTimeFormatter(kind, pattern, locale, zone);
  }

  public ZoneId getZone() {
    return zone;
  }

  public DateTimeFormatter withZone(ZoneId zone) {
    if (Objects.equals(this.zone, zone)) {
      return this;
    }
    return new DateTimeFormatter(kind, pattern, locale, zone);
  }

  public String format(TemporalAccessor temporal) {
    Objects.requireNonNull(temporal, "temporal");
    StringBuilder sb = new StringBuilder(32);
    formatTo(temporal, sb);
    return sb.toString();
  }

  public void formatTo(TemporalAccessor temporal, Appendable appendable) {
    Objects.requireNonNull(temporal, "temporal");
    Objects.requireNonNull(appendable, "appendable");
    try {
      switch (kind) {
        case ISO_LOCAL_DATE:
          appendable.append(LocalDate.from(temporal).toString());
          break;
        case ISO_LOCAL_TIME: {
          LocalTime lt = LocalTime.from(temporal);
          StringBuilder tsb = new StringBuilder(18);
          appendZeroPadded(tsb, lt.getHour(), 2);
          tsb.append(':');
          appendZeroPadded(tsb, lt.getMinute(), 2);
          tsb.append(':');
          appendZeroPadded(tsb, lt.getSecond(), 2);
          if (lt.getNano() > 0) {
            tsb.append('.');
            appendNanos(tsb, lt.getNano());
          }
          appendable.append(tsb.toString());
          break;
        }
        case ISO_LOCAL_DATE_TIME: {
          LocalDate ld = LocalDate.from(temporal);
          LocalTime lt = LocalTime.from(temporal);
          appendable.append(ld.toString());
          appendable.append('T');
          StringBuilder tsb = new StringBuilder(18);
          appendZeroPadded(tsb, lt.getHour(), 2);
          tsb.append(':');
          appendZeroPadded(tsb, lt.getMinute(), 2);
          tsb.append(':');
          appendZeroPadded(tsb, lt.getSecond(), 2);
          if (lt.getNano() > 0) {
            tsb.append('.');
            appendNanos(tsb, lt.getNano());
          }
          appendable.append(tsb.toString());
          break;
        }
        case ISO_INSTANT:
          appendable.append(Instant.from(temporal).toString());
          break;
        case BASIC_ISO_DATE: {
          LocalDate ld = LocalDate.from(temporal);
          int y = ld.getYear();
          int m = ld.getMonthValue();
          int d = ld.getDayOfMonth();
          StringBuilder sb = new StringBuilder(8);
          appendZeroPadded(sb, y, 4);
          appendZeroPadded(sb, m, 2);
          appendZeroPadded(sb, d, 2);
          appendable.append(sb.toString());
          break;
        }
        case PATTERN:
          formatPattern(temporal, pattern, appendable);
          break;
      }
    } catch (java.io.IOException ex) {
      throw new DateTimeException(ex.getMessage(), ex);
    }
  }

  private void formatPattern(TemporalAccessor temporal, String pat, Appendable appendable) throws java.io.IOException {
    int len = pat.length();
    int i = 0;
    while (i < len) {
      char c = pat.charAt(i);
      if (c == '\'') {
        int end = pat.indexOf('\'', i + 1);
        if (end < 0) {
          throw new IllegalArgumentException("Unclosed quote in pattern: " + pat);
        }
        appendable.append(pat.substring(i + 1, end));
        i = end + 1;
      } else if (isPatternLetter(c)) {
        int start = i;
        while (i < len && pat.charAt(i) == c) {
          i++;
        }
        int count = i - start;
        formatField(temporal, c, count, appendable);
      } else {
        appendable.append(c);
        i++;
      }
    }
  }

  private static boolean isPatternLetter(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  private void formatField(TemporalAccessor temporal, char c, int count, Appendable appendable) throws java.io.IOException {
    StringBuilder sb = new StringBuilder();
    switch (c) {
      case 'y':
      case 'u': {
        int year = temporal.get(ChronoField.YEAR);
        if (count == 2) {
          int y2 = Math.abs(year) % 100;
          appendZeroPadded(sb, y2, 2);
        } else {
          appendZeroPadded(sb, year, count);
        }
        break;
      }
      case 'M':
      case 'L': {
        int month = temporal.get(ChronoField.MONTH_OF_YEAR);
        appendZeroPadded(sb, month, count);
        break;
      }
      case 'd': {
        int day = temporal.get(ChronoField.DAY_OF_MONTH);
        appendZeroPadded(sb, day, count);
        break;
      }
      case 'H': {
        int hour = temporal.get(ChronoField.HOUR_OF_DAY);
        appendZeroPadded(sb, hour, count);
        break;
      }
      case 'm': {
        int minute = temporal.get(ChronoField.MINUTE_OF_HOUR);
        appendZeroPadded(sb, minute, count);
        break;
      }
      case 's': {
        int second = temporal.get(ChronoField.SECOND_OF_MINUTE);
        appendZeroPadded(sb, second, count);
        break;
      }
      case 'S': {
        int nano = temporal.get(ChronoField.NANO_OF_SECOND);
        long fraction = (long) (nano / Math.pow(10, 9 - count));
        appendZeroPadded(sb, (int) fraction, count);
        break;
      }
      default:
        throw new UnsupportedTemporalTypeException("Unsupported pattern letter: " + c);
    }
    appendable.append(sb.toString());
  }

  public <T> T parse(CharSequence text, TemporalQuery<T> query) {
    Objects.requireNonNull(text, "text");
    Objects.requireNonNull(query, "query");
    TemporalAccessor parsed = parse(text);
    return query.queryFrom(parsed);
  }

  public TemporalAccessor parse(CharSequence text) {
    Objects.requireNonNull(text, "text");
    String str = text.toString().trim();
    Parsed parsed = new Parsed();
    if (kind == FormatKind.ISO_LOCAL_DATE || kind == FormatKind.BASIC_ISO_DATE) {
      parseIsoDate(str, parsed);
    } else if (kind == FormatKind.ISO_LOCAL_TIME) {
      parseIsoTime(str, parsed);
    } else if (kind == FormatKind.ISO_LOCAL_DATE_TIME) {
      int t = str.indexOf('T');
      if (t < 0) t = str.indexOf('t');
      if (t < 0) throw new DateTimeParseException("Expected 'T' separator", str, 0);
      parseIsoDate(str.substring(0, t), parsed);
      parseIsoTime(str.substring(t + 1), parsed);
    } else if (kind == FormatKind.ISO_INSTANT) {
      if (!str.endsWith("Z") && !str.endsWith("z")) {
        throw new DateTimeParseException("Expected 'Z' at end of Instant", str, str.length() - 1);
      }
      String body = str.substring(0, str.length() - 1);
      int t = body.indexOf('T');
      if (t < 0) t = body.indexOf('t');
      if (t < 0) throw new DateTimeParseException("Expected 'T' separator", str, 0);
      parseIsoDate(body.substring(0, t), parsed);
      parseIsoTime(body.substring(t + 1), parsed);
      parsed.put(ChronoField.OFFSET_SECONDS, 0L);
    } else if (kind == FormatKind.PATTERN) {
      parsePattern(str, pattern, parsed);
    }
    return parsed;
  }

  private void parseIsoDate(String str, Parsed parsed) {
    if (kind == FormatKind.BASIC_ISO_DATE) {
      if (str.length() != 8) {
        throw new DateTimeParseException("Expected 8 characters for BASIC_ISO_DATE", str, 0);
      }
      parsed.put(ChronoField.YEAR, Long.parseLong(str.substring(0, 4)));
      parsed.put(ChronoField.MONTH_OF_YEAR, Long.parseLong(str.substring(4, 6)));
      parsed.put(ChronoField.DAY_OF_MONTH, Long.parseLong(str.substring(6, 8)));
      return;
    }
    String[] parts = str.split("-");
    if (parts.length != 3) {
      throw new DateTimeParseException("Invalid ISO date: " + str, str, 0);
    }
    parsed.put(ChronoField.YEAR, Long.parseLong(parts[0]));
    parsed.put(ChronoField.MONTH_OF_YEAR, Long.parseLong(parts[1]));
    parsed.put(ChronoField.DAY_OF_MONTH, Long.parseLong(parts[2]));
  }

  private void parseIsoTime(String str, Parsed parsed) {
    String[] parts = str.split(":");
    if (parts.length < 2 || parts.length > 3) {
      throw new DateTimeParseException("Invalid ISO time: " + str, str, 0);
    }
    parsed.put(ChronoField.HOUR_OF_DAY, Long.parseLong(parts[0]));
    parsed.put(ChronoField.MINUTE_OF_HOUR, Long.parseLong(parts[1]));
    if (parts.length == 3) {
      String secStr = parts[2];
      int dot = secStr.indexOf('.');
      if (dot >= 0) {
        parsed.put(ChronoField.SECOND_OF_MINUTE, Long.parseLong(secStr.substring(0, dot)));
        String nanoStr = secStr.substring(dot + 1);
        long nano = Long.parseLong(nanoStr);
        for (int i = nanoStr.length(); i < 9; ++i) {
          nano *= 10;
        }
        parsed.put(ChronoField.NANO_OF_SECOND, nano);
      } else {
        parsed.put(ChronoField.SECOND_OF_MINUTE, Long.parseLong(secStr));
      }
    } else {
      parsed.put(ChronoField.SECOND_OF_MINUTE, 0L);
    }
  }

  private void parsePattern(String text, String pat, Parsed parsed) {
    // Basic pattern parser for standard numeric elements
    int pIdx = 0;
    int tIdx = 0;
    int pLen = pat.length();
    int tLen = text.length();

    while (pIdx < pLen && tIdx < tLen) {
      char c = pat.charAt(pIdx);
      if (isPatternLetter(c)) {
        int pStart = pIdx;
        while (pIdx < pLen && pat.charAt(pIdx) == c) {
          pIdx++;
        }
        int count = pIdx - pStart;
        int numDigits = count;
        if (tIdx + numDigits > tLen) {
          numDigits = tLen - tIdx;
        }
        long val = Long.parseLong(text.substring(tIdx, tIdx + numDigits));
        tIdx += numDigits;
        switch (c) {
          case 'y':
          case 'u':
            parsed.put(ChronoField.YEAR, val);
            break;
          case 'M':
          case 'L':
            parsed.put(ChronoField.MONTH_OF_YEAR, val);
            break;
          case 'd':
            parsed.put(ChronoField.DAY_OF_MONTH, val);
            break;
          case 'H':
            parsed.put(ChronoField.HOUR_OF_DAY, val);
            break;
          case 'm':
            parsed.put(ChronoField.MINUTE_OF_HOUR, val);
            break;
          case 's':
            parsed.put(ChronoField.SECOND_OF_MINUTE, val);
            break;
          case 'S':
            for (int k = count; k < 9; ++k) val *= 10;
            parsed.put(ChronoField.NANO_OF_SECOND, val);
            break;
        }
      } else {
        if (text.charAt(tIdx) != c) {
          throw new DateTimeParseException("Mismatch in literal character '" + c + "'", text, tIdx);
        }
        pIdx++;
        tIdx++;
      }
    }
  }

  private static void appendZeroPadded(StringBuilder sb, int value, int digits) {
    String s = Integer.toString(value);
    for (int i = s.length(); i < digits; ++i) {
      sb.append('0');
    }
    sb.append(s);
  }

  private static void appendNanos(StringBuilder sb, int nano) {
    if (nano % 1000000 == 0) {
      appendZeroPadded(sb, nano / 1000000, 3);
    } else if (nano % 1000 == 0) {
      appendZeroPadded(sb, nano / 1000, 6);
    } else {
      appendZeroPadded(sb, nano, 9);
    }
  }

  private static final class Parsed implements TemporalAccessor {
    private final Map<TemporalField, Long> fields = new HashMap<TemporalField, Long>();

    void put(TemporalField field, long value) {
      fields.put(field, value);
    }

    @Override
    public boolean isSupported(TemporalField field) {
      return fields.containsKey(field);
    }

    @Override
    public long getLong(TemporalField field) {
      Long val = fields.get(field);
      if (val != null) {
        return val.longValue();
      }
      if (field == ChronoField.EPOCH_DAY && isSupported(ChronoField.YEAR) && isSupported(ChronoField.MONTH_OF_YEAR) && isSupported(ChronoField.DAY_OF_MONTH)) {
        LocalDate ld = LocalDate.of((int) getLong(ChronoField.YEAR), (int) getLong(ChronoField.MONTH_OF_YEAR), (int) getLong(ChronoField.DAY_OF_MONTH));
        return ld.toEpochDay();
      }
      if (field == ChronoField.NANO_OF_DAY && isSupported(ChronoField.HOUR_OF_DAY) && isSupported(ChronoField.MINUTE_OF_HOUR)) {
        int h = (int) getLong(ChronoField.HOUR_OF_DAY);
        int m = (int) getLong(ChronoField.MINUTE_OF_HOUR);
        int s = isSupported(ChronoField.SECOND_OF_MINUTE) ? (int) getLong(ChronoField.SECOND_OF_MINUTE) : 0;
        int n = isSupported(ChronoField.NANO_OF_SECOND) ? (int) getLong(ChronoField.NANO_OF_SECOND) : 0;
        LocalTime lt = LocalTime.of(h, m, s, n);
        return lt.toNanoOfDay();
      }
      if (field == ChronoField.INSTANT_SECONDS && isSupported(ChronoField.EPOCH_DAY) && isSupported(ChronoField.NANO_OF_DAY)) {
        long epochDay = getLong(ChronoField.EPOCH_DAY);
        long nanoOfDay = getLong(ChronoField.NANO_OF_DAY);
        return epochDay * 86400L + (nanoOfDay / 1000000000L);
      }
      throw new UnsupportedTemporalTypeException("Field not found: " + field);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
      if (query == TemporalQueries.localDate()) {
        if (isSupported(ChronoField.YEAR) && isSupported(ChronoField.MONTH_OF_YEAR) && isSupported(ChronoField.DAY_OF_MONTH)) {
          return (R) LocalDate.of((int) getLong(ChronoField.YEAR), (int) getLong(ChronoField.MONTH_OF_YEAR), (int) getLong(ChronoField.DAY_OF_MONTH));
        }
      } else if (query == TemporalQueries.localTime()) {
        if (isSupported(ChronoField.HOUR_OF_DAY) && isSupported(ChronoField.MINUTE_OF_HOUR)) {
          int h = (int) getLong(ChronoField.HOUR_OF_DAY);
          int m = (int) getLong(ChronoField.MINUTE_OF_HOUR);
          int s = isSupported(ChronoField.SECOND_OF_MINUTE) ? (int) getLong(ChronoField.SECOND_OF_MINUTE) : 0;
          int n = isSupported(ChronoField.NANO_OF_SECOND) ? (int) getLong(ChronoField.NANO_OF_SECOND) : 0;
          return (R) LocalTime.of(h, m, s, n);
        }
      } else if (query == TemporalQueries.offset()) {
        if (isSupported(ChronoField.OFFSET_SECONDS)) {
          return (R) ZoneOffset.ofTotalSeconds((int) getLong(ChronoField.OFFSET_SECONDS));
        }
      }
      return TemporalAccessor.super.query(query);
    }
  }
}
