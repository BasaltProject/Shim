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

package java.text;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class SimpleDateFormat extends DateFormat {
  private static final long serialVersionUID = 4774881970558875024L;

  private static final String kPatternChars = "GyMdkHmsSEDFwWahKzZYuXL";
  private static final int[] kCalendarFields = {Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.DATE,
      Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND,
      Calendar.DAY_OF_WEEK, Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK_IN_MONTH, Calendar.WEEK_OF_YEAR,
      Calendar.WEEK_OF_MONTH, Calendar.AM_PM, Calendar.HOUR, Calendar.HOUR, Calendar.ZONE_OFFSET,
      Calendar.ZONE_OFFSET, Calendar.YEAR, Calendar.DAY_OF_WEEK, Calendar.ZONE_OFFSET, Calendar.MONTH};
  private static final int[] kFieldIds = {DateFormat.ERA_FIELD, DateFormat.YEAR_FIELD, DateFormat.MONTH_FIELD,
      DateFormat.DATE_FIELD, DateFormat.HOUR_OF_DAY1_FIELD, DateFormat.HOUR_OF_DAY0_FIELD,
      DateFormat.MINUTE_FIELD, DateFormat.SECOND_FIELD, DateFormat.MILLISECOND_FIELD,
      DateFormat.DAY_OF_WEEK_FIELD, DateFormat.DAY_OF_YEAR_FIELD, DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD,
      DateFormat.WEEK_OF_YEAR_FIELD, DateFormat.WEEK_OF_MONTH_FIELD, DateFormat.AM_PM_FIELD,
      DateFormat.HOUR1_FIELD, DateFormat.HOUR0_FIELD, DateFormat.TIMEZONE_FIELD, DateFormat.TIMEZONE_FIELD,
      DateFormat.YEAR_FIELD, DateFormat.DAY_OF_WEEK_FIELD, DateFormat.TIMEZONE_FIELD, DateFormat.MONTH_FIELD};
  private static final String[] kKnownZoneNames = {"PST", "PDT", "MST", "MDT", "CST", "CDT", "EST", "EDT", "CET",
      "CEST", "EET", "EEST", "WET", "WEST", "HST", "IST", "JST", "BST"};

  private String pattern;
  private DateFormatSymbols formatSymbols;
  private transient char[] compiled;
  private Date defaultCenturyStart;
  private transient int defaultCenturyStartYear;
  private final Locale locale;

  public SimpleDateFormat() {
    this("M/d/yy, h:mm a", Locale.getDefault(Locale.Category.FORMAT));
  }

  public SimpleDateFormat(String pattern) {
    this(pattern, Locale.getDefault(Locale.Category.FORMAT));
  }

  public SimpleDateFormat(String pattern, Locale locale) {
    this.locale = Objects.requireNonNull(locale, "locale");
    this.pattern = Objects.requireNonNull(pattern, "pattern");
    this.formatSymbols = DateFormatSymbols.getInstance(locale);
    initialize();
    this.compiled = compile(pattern);
  }

  public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
    this.locale = Locale.getDefault(Locale.Category.FORMAT);
    this.pattern = Objects.requireNonNull(pattern, "pattern");
    this.formatSymbols = (DateFormatSymbols) Objects.requireNonNull(formatSymbols, "formatSymbols").clone();
    initialize();
    this.compiled = compile(pattern);
  }

  private void initialize() {
    calendar = Calendar.getInstance(TimeZone.getDefault(), locale);
    numberFormat = NumberFormat.getIntegerInstance(locale);
    numberFormat.setGroupingUsed(false);
    Calendar c = (Calendar) calendar.clone();
    c.setTimeInMillis(System.currentTimeMillis());
    c.add(Calendar.YEAR, -80);
    defaultCenturyStart = c.getTime();
    defaultCenturyStartYear = c.get(Calendar.YEAR);
  }

  private static char[] compile(String pattern) {
    int length = pattern.length();
    StringBuilder out = new StringBuilder();
    boolean inQuote = false;
    for (int i = 0; i < length; i++) {
      char c = pattern.charAt(i);
      if (c == '\'') {
        if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
          i++;
          out.append((char) 0).append('\'');
          continue;
        }
        inQuote = !inQuote;
        continue;
      }
      if (!inQuote && ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
        if (kPatternChars.indexOf(c) < 0) {
          throw new IllegalArgumentException("Illegal pattern character '" + c + "'");
        }
        int count = 1;
        while (i + 1 < length && pattern.charAt(i + 1) == c) {
          i++;
          count++;
        }
        out.append((char) 1).append(c).append((char) count);
      } else {
        out.append((char) 0).append(c);
      }
    }
    if (inQuote) {
      throw new IllegalArgumentException("Unterminated quote");
    }
    return out.toString().toCharArray();
  }

  public void set2DigitYearStart(Date startDate) {
    defaultCenturyStart = new Date(startDate.getTime());
    Calendar c = (Calendar) calendar.clone();
    c.setTime(startDate);
    defaultCenturyStartYear = c.get(Calendar.YEAR);
  }

  public Date get2DigitYearStart() {
    return (Date) defaultCenturyStart.clone();
  }

  public void applyPattern(String pattern) {
    this.compiled = compile(Objects.requireNonNull(pattern, "pattern"));
    this.pattern = pattern;
  }

  public void applyLocalizedPattern(String pattern) {
    applyPattern(pattern);
  }

  public String toPattern() {
    return pattern;
  }

  public String toLocalizedPattern() {
    return pattern;
  }

  public DateFormatSymbols getDateFormatSymbols() {
    return (DateFormatSymbols) formatSymbols.clone();
  }

  public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
    this.formatSymbols = (DateFormatSymbols) Objects.requireNonNull(newFormatSymbols, "newFormatSymbols").clone();
  }

  private static final DateFormat.Field[] kFieldAttributes = {DateFormat.Field.ERA, DateFormat.Field.YEAR,
      DateFormat.Field.MONTH, DateFormat.Field.DAY_OF_MONTH, DateFormat.Field.HOUR_OF_DAY1,
      DateFormat.Field.HOUR_OF_DAY0, DateFormat.Field.MINUTE, DateFormat.Field.SECOND,
      DateFormat.Field.MILLISECOND, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.DAY_OF_YEAR,
      DateFormat.Field.DAY_OF_WEEK_IN_MONTH, DateFormat.Field.WEEK_OF_YEAR, DateFormat.Field.WEEK_OF_MONTH,
      DateFormat.Field.AM_PM, DateFormat.Field.HOUR1, DateFormat.Field.HOUR0, DateFormat.Field.TIME_ZONE,
      DateFormat.Field.TIME_ZONE, DateFormat.Field.YEAR, DateFormat.Field.DAY_OF_WEEK,
      DateFormat.Field.TIME_ZONE, DateFormat.Field.MONTH};

  private transient java.util.List<Object[]> fieldSegments;

  @Override
  public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
    Date date;
    if (obj instanceof Date) {
      date = (Date) obj;
    } else if (obj instanceof Number) {
      date = new Date(((Number) obj).longValue());
    } else if (obj == null) {
      throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
    } else {
      throw new IllegalArgumentException("Cannot format given Object as a Date");
    }
    fieldSegments = new java.util.ArrayList<Object[]>();
    try {
      String text = format(date, new StringBuffer(), new FieldPosition(0)).toString();
      AttributedString result = new AttributedString(text);
      for (Object[] seg : fieldSegments) {
        if ((Integer) seg[2] > (Integer) seg[1]) {
          result.addAttribute((AttributedCharacterIterator.Attribute) seg[0],
              (AttributedCharacterIterator.Attribute) seg[0], (Integer) seg[1], (Integer) seg[2]);
        }
      }
      return result.getIterator();
    } finally {
      fieldSegments = null;
    }
  }

  @Override
  public StringBuffer format(Date date, StringBuffer buffer, FieldPosition position) {
    position.setBeginIndex(0);
    position.setEndIndex(0);
    calendar.setTime(date);
    boolean positioned = false;
    for (int i = 0; i < compiled.length;) {
      if (compiled[i] == 0) {
        buffer.append(compiled[i + 1]);
        i += 2;
        continue;
      }
      char c = compiled[i + 1];
      int count = compiled[i + 2];
      i += 3;
      int start = buffer.length();
      formatField(c, count, buffer);
      if (fieldSegments != null) {
        fieldSegments.add(new Object[] {kFieldAttributes[kPatternChars.indexOf(c)], start, buffer.length()});
      }
      if (!positioned && position.getField() == kFieldIds[kPatternChars.indexOf(c)]) {
        positioned = true;
        position.setBeginIndex(start);
        position.setEndIndex(buffer.length());
      }
    }
    return buffer;
  }

  private void zeroPad(StringBuffer buffer, long value, int minDigits) {
    char zero = zeroDigit();
    if (zero == '0') {
      zeroPadAscii(buffer, value, minDigits);
      return;
    }
    int start = buffer.length();
    zeroPadAscii(buffer, value, minDigits);
    for (int k = start; k < buffer.length(); k++) {
      char c = buffer.charAt(k);
      if (c >= '0' && c <= '9') {
        buffer.setCharAt(k, (char) (zero + (c - '0')));
      }
    }
  }

  private char zeroDigit() {
    if (numberFormat instanceof DecimalFormat) {
      return ((DecimalFormat) numberFormat).getDecimalFormatSymbols().getZeroDigit();
    }
    return '0';
  }

  private static void zeroPadAscii(StringBuffer buffer, long value, int minDigits) {
    if (value < 0) {
      buffer.append('-');
      value = -value;
    }
    String digits = Long.toString(value);
    for (int k = digits.length(); k < minDigits; k++) {
      buffer.append('0');
    }
    buffer.append(digits);
  }

  private void appendOffset(StringBuffer buffer, boolean iso, int count) {
    int offset = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 60000;
    if (iso && offset == 0) {
      buffer.append('Z');
      return;
    }
    buffer.append(offset < 0 ? '-' : '+');
    offset = Math.abs(offset);
    zeroPadAscii(buffer, offset / 60, 2);
    if (iso && count == 1) {
      return;
    }
    if (iso && count == 3) {
      buffer.append(':');
    }
    zeroPadAscii(buffer, offset % 60, 2);
  }

  private String eraName(int count, int value) {
    String type = calendar.getCalendarType();
    int style = count >= 5 ? 2 : count == 4 ? 1 : 0;
    if (!type.equals("gregory")) {
      String[] names = shim.LocaleDb.eraNames(locale, type);
      if (names != null) {
        int samples = names.length / 3;
        int era = samples == 1 ? 0 : Math.min(value, samples - 1);
        return names[style * samples + era];
      }
    }
    return formatSymbols.getEras()[value];
  }

  private void formatField(char c, int count, StringBuffer buffer) {
    int field = kCalendarFields[kPatternChars.indexOf(c)];
    int value = c == 'Y' ? (calendar.isWeekDateSupported() ? calendar.getWeekYear() : calendar.get(Calendar.YEAR))
        : (field == Calendar.ZONE_OFFSET ? 0 : calendar.get(field));
    switch (c) {
      case 'G':
        buffer.append(eraName(count, value));
        break;
      case 'y':
      case 'Y':
        if (!calendar.getCalendarType().equals("gregory")) {
          if (value == 1 && calendar.getCalendarType().equals("japanese") && locale.getLanguage().equals("ja")) {
            buffer.append('\u5143');
          } else {
            zeroPad(buffer, count == 2 ? value % 100 : value, count >= 4 ? 1 : count);
          }
          break;
        }
        zeroPad(buffer, count == 2 ? value % 100 : value, count);
        break;
      case 'M':
      case 'L':
        if (count >= 4) {
          buffer.append(formatSymbols.getMonths()[value]);
        } else if (count == 3) {
          buffer.append(formatSymbols.getShortMonths()[value]);
        } else {
          zeroPad(buffer, value + 1, count);
        }
        break;
      case 'k':
        zeroPad(buffer, value == 0 ? 24 : value, count);
        break;
      case 'h':
        zeroPad(buffer, value == 0 ? 12 : value, count);
        break;
      case 'E':
        if (count >= 4) {
          buffer.append(formatSymbols.getWeekdays()[value]);
        } else {
          buffer.append(formatSymbols.getShortWeekdays()[value]);
        }
        break;
      case 'u':
        zeroPad(buffer, value == Calendar.SUNDAY ? 7 : value - 1, count);
        break;
      case 'a':
        buffer.append(formatSymbols.getAmPmStrings()[value]);
        break;
      case 'z': {
        boolean daylight = calendar.get(Calendar.DST_OFFSET) != 0;
        buffer.append(calendar.getTimeZone().getDisplayName(daylight, count >= 4 ? TimeZone.LONG : TimeZone.SHORT,
            locale));
        break;
      }
      case 'Z':
        appendOffset(buffer, false, count);
        break;
      case 'X':
        appendOffset(buffer, true, count);
        break;
      default:
        zeroPad(buffer, value, count);
        break;
    }
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isNumericField(char c, int count) {
    switch (c) {
      case 'y': case 'Y': case 'd': case 'k': case 'H': case 'm': case 's': case 'S': case 'D': case 'F':
      case 'w': case 'W': case 'h': case 'K': case 'u':
        return true;
      case 'M': case 'L':
        return count <= 2;
      default:
        return false;
    }
  }

  private boolean abutsNumeric(int next) {
    return next < compiled.length && compiled[next] == 1 && isNumericField(compiled[next + 1], compiled[next + 2]);
  }

  private static boolean literalMatches(char expected, char actual, boolean lenient) {
    if (expected == actual) {
      return true;
    }
    return lenient && (expected == ' ' || expected == ' ') && (actual == ' ' || actual == ' ');
  }

  private static int matchName(String text, int start, String[] names, int first, int[] index) {
    int bestLength = 0;
    index[0] = -1;
    for (int i = first; i < names.length; i++) {
      String name = names[i];
      int length = name.length();
      if (length > bestLength && start + length <= text.length() && text.regionMatches(true, start, name, 0, length)) {
        index[0] = i;
        bestLength = length;
      }
    }
    return bestLength;
  }

  private static Date fail(ParsePosition pos, int start, int errorAt) {
    pos.setErrorIndex(errorAt);
    pos.setIndex(start);
    return null;
  }

  @Override
  public Date parse(String text, ParsePosition pos) {
    int start = pos.getIndex();
    int at = start;
    int textLength = text.length();
    Calendar cal = (Calendar) calendar.clone();
    cal.clear();
    boolean ampmSet = false;
    int ampm = 0;
    int hour12 = -1;
    boolean hasHour24 = false;
    boolean yearSet = false;
    int yearValue = 0;
    boolean twoDigitYear = false;
    int offsetMillis = Integer.MIN_VALUE;
    TimeZone parsedZone = null;
    int[] index = new int[1];
    for (int i = 0; i < compiled.length;) {
      if (compiled[i] == 0) {
        char expected = compiled[i + 1];
        i += 2;
        if (at >= textLength || !literalMatches(expected, text.charAt(at), cal.isLenient())) {
          return fail(pos, start, at);
        }
        at++;
        continue;
      }
      char c = compiled[i + 1];
      int count = compiled[i + 2];
      i += 3;
      if (isNumericField(c, count)) {
        int p = at;
        int sign = 1;
        if ((c == 'y' || c == 'Y') && p < textLength && text.charAt(p) == '-') {
          p++;
          sign = -1;
        }
        int digitsStart = p;
        int limit = abutsNumeric(i) ? count : 18;
        long number = 0;
        while (p < textLength && isDigit(text.charAt(p)) && p - digitsStart < limit) {
          number = number * 10 + (text.charAt(p) - '0');
          p++;
        }
        if (p == digitsStart) {
          return fail(pos, start, at);
        }
        int digits = p - digitsStart;
        at = p;
        int value = (int) (number * sign);
        switch (c) {
          case 'y':
          case 'Y':
            yearValue = value;
            twoDigitYear = count <= 2 && digits == 2 && sign == 1;
            yearSet = true;
            break;
          case 'M':
          case 'L':
            cal.set(Calendar.MONTH, value - 1);
            break;
          case 'k':
            hasHour24 = true;
            cal.set(Calendar.HOUR_OF_DAY, value == 24 ? 0 : value);
            break;
          case 'H':
            hasHour24 = true;
            cal.set(Calendar.HOUR_OF_DAY, value);
            break;
          case 'h':
            hour12 = value == 12 ? 0 : value;
            break;
          case 'K':
            hour12 = value;
            break;
          case 'u':
            cal.set(Calendar.DAY_OF_WEEK, value == 7 ? Calendar.SUNDAY : value + 1);
            break;
          default:
            cal.set(kCalendarFields[kPatternChars.indexOf(c)], value);
            break;
        }
        continue;
      }
      switch (c) {
        case 'M':
        case 'L': {
          int longLength = matchName(text, at, formatSymbols.getMonths(), 0, index);
          int longIndex = index[0] < 12 ? index[0] : -1;
          int shortLength = matchName(text, at, formatSymbols.getShortMonths(), 0, index);
          int shortIndex = index[0] < 12 ? index[0] : -1;
          int month = longIndex;
          int length = longLength;
          if (shortIndex >= 0 && (longIndex < 0 || shortLength > longLength)) {
            month = shortIndex;
            length = shortLength;
          }
          if (month < 0) {
            return fail(pos, start, at);
          }
          cal.set(Calendar.MONTH, month);
          at += length;
          break;
        }
        case 'E': {
          int longLength = matchName(text, at, formatSymbols.getWeekdays(), 1, index);
          int day = index[0];
          int length = longLength;
          int shortLength = matchName(text, at, formatSymbols.getShortWeekdays(), 1, index);
          if (index[0] > 0 && (day < 0 || shortLength > longLength)) {
            day = index[0];
            length = shortLength;
          }
          if (day < 0) {
            return fail(pos, start, at);
          }
          cal.set(Calendar.DAY_OF_WEEK, day);
          at += length;
          break;
        }
        case 'a': {
          int length = matchName(text, at, formatSymbols.getAmPmStrings(), 0, index);
          if (index[0] < 0) {
            return fail(pos, start, at);
          }
          ampm = index[0];
          ampmSet = true;
          at += length;
          break;
        }
        case 'G': {
          int length = matchName(text, at, formatSymbols.getEras(), 0, index);
          int era = index[0];
          if (era < 0) {
            return fail(pos, start, at);
          }
          cal.set(Calendar.ERA, era);
          at += length;
          break;
        }
        case 'z':
        case 'Z':
        case 'X': {
          int[] consumed = new int[1];
          Object zone = parseZone(text, at, consumed);
          if (zone == null) {
            return fail(pos, start, at);
          }
          if (zone instanceof TimeZone) {
            parsedZone = (TimeZone) zone;
          } else {
            offsetMillis = (Integer) zone;
          }
          at += consumed[0];
          break;
        }
        default:
          return fail(pos, start, at);
      }
    }
    if (yearSet) {
      int year = yearValue;
      if (twoDigitYear) {
        year += defaultCenturyStartYear / 100 * 100;
        if (year < defaultCenturyStartYear) {
          year += 100;
        }
      }
      cal.set(Calendar.YEAR, year);
    }
    if (hour12 >= 0 && !hasHour24) {
      cal.set(Calendar.HOUR, hour12);
    }
    if (ampmSet && !hasHour24) {
      cal.set(Calendar.AM_PM, ampm);
    }
    if (parsedZone != null) {
      cal.setTimeZone(parsedZone);
    } else if (offsetMillis != Integer.MIN_VALUE) {
      cal.set(Calendar.ZONE_OFFSET, offsetMillis);
      cal.set(Calendar.DST_OFFSET, 0);
    }
    Date result;
    try {
      result = cal.getTime();
    } catch (IllegalArgumentException ex) {
      return fail(pos, start, at);
    }
    pos.setIndex(at);
    return result;
  }

  private static Object parseZone(String text, int at, int[] consumed) {
    int length = text.length();
    if (at >= length) {
      return null;
    }
    if (text.charAt(at) == 'Z' && (at + 1 >= length || !Character.isLetter(text.charAt(at + 1)))) {
      consumed[0] = 1;
      return Integer.valueOf(0);
    }
    int p = at;
    boolean gmt = text.startsWith("GMT", p) || text.startsWith("UTC", p);
    if (gmt) {
      p += 3;
    }
    if (p < length && (text.charAt(p) == '+' || text.charAt(p) == '-')) {
      int sign = text.charAt(p) == '-' ? -1 : 1;
      int q = p + 1;
      int hours = 0;
      int digits = 0;
      while (q < length && isDigit(text.charAt(q)) && digits < 2) {
        hours = hours * 10 + text.charAt(q) - '0';
        q++;
        digits++;
      }
      if (digits > 0) {
        int minutes = 0;
        if (q + 2 < length && text.charAt(q) == ':' && isDigit(text.charAt(q + 1)) && isDigit(text.charAt(q + 2))) {
          minutes = (text.charAt(q + 1) - '0') * 10 + text.charAt(q + 2) - '0';
          q += 3;
        } else if (digits == 2 && q + 1 < length && isDigit(text.charAt(q)) && isDigit(text.charAt(q + 1))) {
          minutes = (text.charAt(q) - '0') * 10 + text.charAt(q + 1) - '0';
          q += 2;
        }
        consumed[0] = q - at;
        return Integer.valueOf(sign * (hours * 60 + minutes) * 60000);
      }
    }
    if (gmt) {
      consumed[0] = 3;
      return Integer.valueOf(0);
    }
    String best = null;
    for (String id : TimeZone.getAvailableIDs()) {
      if ((best == null || id.length() > best.length()) && text.startsWith(id, at)) {
        best = id;
      }
    }
    for (String name : kKnownZoneNames) {
      if ((best == null || name.length() > best.length()) && text.startsWith(name, at)) {
        best = name;
      }
    }
    if (best == null) {
      return null;
    }
    consumed[0] = best.length();
    if (best.length() == 3 && best.charAt(1) == 'D' && best.charAt(2) == 'T') {
      return TimeZone.getTimeZone(best.charAt(0) + "ST");
    }
    return TimeZone.getTimeZone(best);
  }

  @Override
  public int hashCode() {
    return pattern.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    SimpleDateFormat that = (SimpleDateFormat) obj;
    return pattern.equals(that.pattern) && formatSymbols.equals(that.formatSymbols);
  }

  @Override
  public Object clone() {
    SimpleDateFormat other = (SimpleDateFormat) super.clone();
    other.formatSymbols = (DateFormatSymbols) formatSymbols.clone();
    other.defaultCenturyStart = new Date(defaultCenturyStart.getTime());
    return other;
  }
}
