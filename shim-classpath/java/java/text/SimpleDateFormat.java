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
  private static final long serialVersionUID = 4774881970558870743L;

  private String pattern;
  private DateFormatSymbols formatSymbols;

  public SimpleDateFormat() {
    this("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  }

  public SimpleDateFormat(String pattern) {
    this(pattern, Locale.getDefault());
  }

  public SimpleDateFormat(String pattern, Locale locale) {
    this(pattern, DateFormatSymbols.getInstance(locale));
  }

  public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
    this.pattern = Objects.requireNonNull(pattern, "pattern");
    this.formatSymbols = (DateFormatSymbols) Objects.requireNonNull(formatSymbols, "formatSymbols").clone();
  }

  public void applyPattern(String pattern) {
    this.pattern = Objects.requireNonNull(pattern, "pattern");
  }

  public String toPattern() {
    return pattern;
  }

  public DateFormatSymbols getDateFormatSymbols() {
    return (DateFormatSymbols) formatSymbols.clone();
  }

  public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
    this.formatSymbols = (DateFormatSymbols) Objects.requireNonNull(newFormatSymbols, "newFormatSymbols").clone();
  }

  @Override
  public void setTimeZone(TimeZone tz) {
    super.setTimeZone(tz);
  }

  @Override
  public StringBuffer format(Date date, StringBuffer buffer, FieldPosition position) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    int i = 0;
    int len = pattern.length();
    while (i < len) {
      char c = pattern.charAt(i);
      if (c == '\'') {
        i++;
        if (i < len && pattern.charAt(i) == '\'') {
          buffer.append('\'');
          i++;
          continue;
        }
        while (i < len) {
          char ch = pattern.charAt(i++);
          if (ch == '\'') {
            if (i < len && pattern.charAt(i) == '\'') {
              buffer.append('\'');
              i++;
            } else {
              break;
            }
          } else {
            buffer.append(ch);
          }
        }
      } else if (isPatternLetter(c)) {
        int count = 1;
        while (i + 1 < len && pattern.charAt(i + 1) == c) {
          count++;
          i++;
        }
        formatField(c, count, cal, buffer);
        i++;
      } else {
        buffer.append(c);
        i++;
      }
    }
    return buffer;
  }

  private static boolean isPatternLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private void formatField(char ch, int count, Calendar cal, StringBuffer buffer) {
    switch (ch) {
      case 'y': {
        int year = cal.get(Calendar.YEAR);
        if (count == 2) {
          pad(buffer, year % 100, 2);
        } else {
          pad(buffer, year, count);
        }
        break;
      }
      case 'M': {
        int month = cal.get(Calendar.MONTH);
        if (count >= 4) {
          String[] months = formatSymbols.getMonths();
          buffer.append(month < months.length ? months[month] : "");
        } else if (count == 3) {
          String[] shortMonths = formatSymbols.getShortMonths();
          buffer.append(month < shortMonths.length ? shortMonths[month] : "");
        } else {
          pad(buffer, month + 1, count);
        }
        break;
      }
      case 'd':
        pad(buffer, cal.get(Calendar.DAY_OF_MONTH), count);
        break;
      case 'H':
        pad(buffer, cal.get(Calendar.HOUR_OF_DAY), count);
        break;
      case 'h': {
        int h = cal.get(Calendar.HOUR);
        if (h == 0) h = 12;
        pad(buffer, h, count);
        break;
      }
      case 'm':
        pad(buffer, cal.get(Calendar.MINUTE), count);
        break;
      case 's':
        pad(buffer, cal.get(Calendar.SECOND), count);
        break;
      case 'S':
        pad(buffer, 0, count);
        break;
      case 'a': {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String[] ampm = formatSymbols.getAmPmStrings();
        buffer.append(hour < 12 ? ampm[0] : ampm[1]);
        break;
      }
      case 'E': {
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        if (count >= 4) {
          String[] weekdays = formatSymbols.getWeekdays();
          buffer.append(dow < weekdays.length ? weekdays[dow] : "");
        } else {
          String[] shortWeekdays = formatSymbols.getShortWeekdays();
          buffer.append(dow < shortWeekdays.length ? shortWeekdays[dow] : "");
        }
        break;
      }
      case 'z':
      case 'Z':
      case 'X':
        buffer.append("GMT");
        break;
      default:
        for (int k = 0; k < count; k++) {
          buffer.append(ch);
        }
        break;
    }
  }

  @Override
  public Date parse(String text, ParsePosition position) {
    int textIndex = position.getIndex();
    int patIndex = 0;
    int patLen = pattern.length();
    int textLen = text.length();

    Calendar cal = Calendar.getInstance();

    while (patIndex < patLen) {
      char c = pattern.charAt(patIndex);
      if (c == '\'') {
        patIndex++;
        while (patIndex < patLen) {
          char ch = pattern.charAt(patIndex++);
          if (ch == '\'') {
            break;
          }
          if (textIndex >= textLen || text.charAt(textIndex++) != ch) {
            position.setErrorIndex(textIndex);
            return null;
          }
        }
      } else if (isPatternLetter(c)) {
        int count = 1;
        while (patIndex + 1 < patLen && pattern.charAt(patIndex + 1) == c) {
          count++;
          patIndex++;
        }
        patIndex++;

        switch (c) {
          case 'y': {
            int val = parseDigits(text, textIndex, count > 2 ? 4 : 2);
            if (val < 0) {
              position.setErrorIndex(textIndex);
              return null;
            }
            int consumed = (count > 2 && textIndex + 4 <= textLen) ? 4 : 2;
            textIndex += consumed;
            if (consumed == 2 && val < 100) {
              val += 2000;
            }
            cal.set(Calendar.YEAR, val);
            break;
          }
          case 'M': {
            if (count >= 3) {
              String[] names = (count >= 4) ? formatSymbols.getMonths() : formatSymbols.getShortMonths();
              int matched = -1;
              for (int m = 0; m < names.length; m++) {
                String name = names[m];
                if (name.length() > 0 && text.startsWith(name, textIndex)) {
                  matched = m;
                  textIndex += name.length();
                  break;
                }
              }
              if (matched < 0) {
                position.setErrorIndex(textIndex);
                return null;
              }
              cal.set(Calendar.MONTH, matched);
            } else {
              int val = parseDigits(text, textIndex, count);
              if (val < 0) {
                position.setErrorIndex(textIndex);
                return null;
              }
              textIndex += count;
              cal.set(Calendar.MONTH, val - 1);
            }
            break;
          }
          case 'd': {
            int val = parseDigits(text, textIndex, count);
            if (val < 0) {
              position.setErrorIndex(textIndex);
              return null;
            }
            textIndex += count;
            cal.set(Calendar.DAY_OF_MONTH, val);
            break;
          }
          case 'H': {
            int val = parseDigits(text, textIndex, count);
            if (val < 0) {
              position.setErrorIndex(textIndex);
              return null;
            }
            textIndex += count;
            cal.set(Calendar.HOUR_OF_DAY, val);
            break;
          }
          case 'm': {
            int val = parseDigits(text, textIndex, count);
            if (val < 0) {
              position.setErrorIndex(textIndex);
              return null;
            }
            textIndex += count;
            cal.set(Calendar.MINUTE, val);
            break;
          }
          case 's': {
            int val = parseDigits(text, textIndex, count);
            if (val < 0) {
              position.setErrorIndex(textIndex);
              return null;
            }
            textIndex += count;
            cal.set(Calendar.SECOND, val);
            break;
          }
          default:
            break;
        }
      } else {
        if (textIndex >= textLen || text.charAt(textIndex++) != c) {
          position.setErrorIndex(textIndex);
          return null;
        }
        patIndex++;
      }
    }

    position.setIndex(textIndex);
    return cal.getTime();
  }

  private static int parseDigits(String text, int offset, int maxDigits) {
    int end = Math.min(text.length(), offset + maxDigits);
    if (offset >= end) return -1;
    int val = 0;
    for (int i = offset; i < end; i++) {
      char ch = text.charAt(i);
      if (ch < '0' || ch > '9') {
        if (i == offset) return -1;
        break;
      }
      val = val * 10 + (ch - '0');
    }
    return val;
  }

  private static void pad(StringBuffer buffer, int value, int digits) {
    int v = Math.abs(value);
    int d = 1;
    int temp = v;
    while (temp >= 10) {
      temp /= 10;
      d++;
    }
    while (d < digits) {
      buffer.append('0');
      digits--;
    }
    buffer.append(v);
  }
}
