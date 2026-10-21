/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.math.BigDecimal;
import java.util.regex.Pattern;
import shim.xml.validation.XsdSimple.Lexical;

/** Lexical spaces, value equality and ordering for the built-in simple types. */
final class XsdLexical {
  private XsdLexical() {
  }

  private static final Pattern kDecimal = Pattern.compile("[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)");
  private static final Pattern kInteger = Pattern.compile("[+-]?[0-9]+");
  private static final Pattern kFloat = Pattern.compile("[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?");
  private static final String kTz = "(Z|[+-](0[0-9]|1[0-3]):[0-5][0-9]|[+-]14:00)?";
  private static final Pattern kDate = Pattern.compile("-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-([0-2][0-9]|3[01])" + kTz);
  private static final Pattern kTime = Pattern.compile("([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?" + kTz + "|24:00:00(\\.0+)?" + kTz);
  private static final Pattern kDateTime = Pattern.compile("-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-([0-2][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?|24:00:00(\\.0+)?)" + kTz);
  private static final Pattern kDuration = Pattern.compile("-?P(?=[0-9T])([0-9]+Y)?([0-9]+M)?([0-9]+D)?(T(?=[0-9])([0-9]+H)?([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)?");
  private static final Pattern kGYear = Pattern.compile("-?([1-9][0-9]{3,}|0[0-9]{3})" + kTz);
  private static final Pattern kGYearMonth = Pattern.compile("-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])" + kTz);
  private static final Pattern kGMonth = Pattern.compile("--(0[1-9]|1[0-2])" + kTz);
  private static final Pattern kGMonthDay = Pattern.compile("--(0[1-9]|1[0-2])-([0-2][0-9]|3[01])" + kTz);
  private static final Pattern kGDay = Pattern.compile("---([0-2][0-9]|3[01])" + kTz);
  private static final Pattern kHex = Pattern.compile("([0-9a-fA-F]{2})*");
  private static final Pattern kBase64 = Pattern.compile("[A-Za-z0-9+/=\\s]*");

  static boolean valid(Lexical lexical, String text, XsdSimple.Ctx ctx) {
    switch (lexical) {
    case STRING:
    case ANYSIMPLE:
    case ANYURI:
      return true;
    case BOOLEAN:
      return text.equals("true") || text.equals("false") || text.equals("1") || text.equals("0");
    case DECIMAL:
      return kDecimal.matcher(text).matches();
    case INTEGER:
      return kInteger.matcher(text).matches();
    case FLOAT:
    case DOUBLE:
      return text.equals("INF") || text.equals("-INF") || text.equals("NaN") || kFloat.matcher(text).matches();
    case DATE:
      return kDate.matcher(text).matches() && validDay(text);
    case DATETIME:
      return kDateTime.matcher(text).matches() && validDay(text);
    case TIME:
      return kTime.matcher(text).matches();
    case DURATION:
      return kDuration.matcher(text).matches();
    case GYEAR:
      return kGYear.matcher(text).matches();
    case GYEARMONTH:
      return kGYearMonth.matcher(text).matches();
    case GMONTH:
      return kGMonth.matcher(text).matches();
    case GMONTHDAY:
      return kGMonthDay.matcher(text).matches() && validMonthDay(text);
    case GDAY:
      return kGDay.matcher(text).matches() && !text.startsWith("---00");
    case QNAME: {
      int colon = text.indexOf(':');
      if (colon < 0) {
        return isNcName(text);
      }
      String prefix = text.substring(0, colon);
      return isNcName(prefix) && isNcName(text.substring(colon + 1)) && (ctx == null || ctx.resolvePrefix(prefix) != null);
    }
    case NCNAME:
      return isNcName(text);
    case NAME:
      return isName(text);
    case NMTOKEN:
      return isNmtoken(text);
    case HEX:
      return kHex.matcher(text).matches();
    case BASE64:
      return kBase64.matcher(text).matches();
    case NOTATION:
      return false;
    default:
      return true;
    }
  }

  private static boolean validDay(String text) {
    int start = text.startsWith("-") ? 1 : 0;
    int dash = text.indexOf('-', start);
    int dash2 = text.indexOf('-', dash + 1);
    int year;
    int month;
    int day;
    try {
      year = Integer.parseInt(text.substring(start, dash));
      month = Integer.parseInt(text.substring(dash + 1, dash2));
      day = Integer.parseInt(text.substring(dash2 + 1, dash2 + 3));
    } catch (NumberFormatException e) {
      return false;
    }
    return day >= 1 && day <= daysIn(year, month);
  }

  private static boolean validMonthDay(String text) {
    int month = Integer.parseInt(text.substring(2, 4));
    int day = Integer.parseInt(text.substring(5, 7));
    return day >= 1 && day <= daysIn(2000, month);
  }

  private static int daysIn(int year, int month) {
    switch (month) {
    case 2:
      return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
    case 4:
    case 6:
    case 9:
    case 11:
      return 30;
    default:
      return 31;
    }
  }

  static boolean isNcName(String s) {
    return isName(s) && s.indexOf(':') < 0;
  }

  static boolean isName(String s) {
    if (s.isEmpty() || !nameStart(s.codePointAt(0))) {
      return false;
    }
    for (int i = 0; i < s.length();) {
      int cp = s.codePointAt(i);
      if (!nameChar(cp)) {
        return false;
      }
      i += Character.charCount(cp);
    }
    return true;
  }

  static boolean isNmtoken(String s) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length();) {
      int cp = s.codePointAt(i);
      if (!nameChar(cp)) {
        return false;
      }
      i += Character.charCount(cp);
    }
    return true;
  }

  private static boolean nameStart(int c) {
    return c == ':' || c == '_' || Character.isLetter(c) || (c >= 0xC0 && c <= 0x2FF && c != 0xD7 && c != 0xF7);
  }

  private static boolean nameChar(int c) {
    return nameStart(c) || c == '-' || c == '.' || Character.isDigit(c) || c == 0xB7 || (c >= 0x300 && c <= 0x36F);
  }

  static boolean equalValues(Lexical lexical, String a, String b) {
    switch (lexical) {
    case DECIMAL:
    case INTEGER:
      return compare(lexical, a, b) == 0;
    case FLOAT:
    case DOUBLE:
      return compare(lexical, a, b) == 0;
    case BOOLEAN:
      return canonicalBoolean(a).equals(canonicalBoolean(b));
    default:
      return a.equals(b);
    }
  }

  private static String canonicalBoolean(String s) {
    return s.equals("1") || s.equals("true") ? "true" : "false";
  }

  /** Orders two literals of the same type; non-ordered types compare lexically. */
  static int compare(Lexical lexical, String a, String b) {
    switch (lexical) {
    case DECIMAL:
    case INTEGER:
      return decimal(a).compareTo(decimal(b));
    case FLOAT:
    case DOUBLE:
      return Double.compare(toDouble(a), toDouble(b));
    default:
      return a.compareTo(b);
    }
  }

  private static BigDecimal decimal(String s) {
    return new BigDecimal(s.startsWith("+") ? s.substring(1) : s);
  }

  private static double toDouble(String s) {
    switch (s) {
    case "INF":
      return Double.POSITIVE_INFINITY;
    case "-INF":
      return Double.NEGATIVE_INFINITY;
    case "NaN":
      return Double.NaN;
    default:
      return Double.parseDouble(s);
    }
  }
}
