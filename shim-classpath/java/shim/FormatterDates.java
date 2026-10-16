/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class FormatterDates {
  private static final String[] kMonths = {"January", "February", "March", "April", "May", "June", "July",
      "August", "September", "October", "November", "December"};
  private static final String[] kDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
      "Saturday"};

  private FormatterDates() { }

  private static String two(long value) {
    return value < 10 ? "0" + value : Long.toString(value);
  }

  private static String pad(long value, int digits) {
    StringBuilder sb = new StringBuilder(Long.toString(value));
    while (sb.length() < digits) sb.insert(0, '0');
    return sb.toString();
  }

  public static String format(char conversion, long millis) {
    long days = Math.floorDiv(millis, 86400000L);
    long ms = Math.floorMod(millis, 86400000L);
    long z = days + 719468;
    long era = Math.floorDiv(z, 146097L);
    long doe = z - era * 146097;
    long yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
    long year = yoe + era * 400;
    long doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    long mp = (5 * doy + 2) / 153;
    long day = doy - (153 * mp + 2) / 5 + 1;
    long month = mp < 10 ? mp + 3 : mp - 9;
    if (month <= 2) ++year;
    int weekday = (int) Math.floorMod(days + 4, 7L);
    boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    int[] cumulative = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    long dayOfYear = cumulative[(int) month - 1] + day + (leap && month > 2 ? 1 : 0);
    long hour = ms / 3600000L;
    long minute = ms / 60000L % 60;
    long second = ms / 1000L % 60;
    long milli = ms % 1000;
    int hour12 = (int) (hour % 12 == 0 ? 12 : hour % 12);
    switch (conversion) {
    case 'H': return two(hour);
    case 'I': return two(hour12);
    case 'k': return Long.toString(hour);
    case 'l': return Integer.toString(hour12);
    case 'M': return two(minute);
    case 'S': return two(second);
    case 'L': return pad(milli, 3);
    case 'N': return pad(milli * 1000000L, 9);
    case 'p': return hour < 12 ? "am" : "pm";
    case 'z': return "+0000";
    case 'Z': return "UTC";
    case 's': return Long.toString(Math.floorDiv(millis, 1000L));
    case 'Q': return Long.toString(millis);
    case 'B': return kMonths[(int) month - 1];
    case 'b':
    case 'h': return kMonths[(int) month - 1].substring(0, 3);
    case 'A': return kDays[weekday];
    case 'a': return kDays[weekday].substring(0, 3);
    case 'C': return two(Math.floorDiv(year, 100L));
    case 'Y': return pad(year, 4);
    case 'y': return two(Math.floorMod(year, 100L));
    case 'j': return pad(dayOfYear, 3);
    case 'm': return two(month);
    case 'd': return two(day);
    case 'e': return Long.toString(day);
    case 'R': return two(hour) + ":" + two(minute);
    case 'T': return two(hour) + ":" + two(minute) + ":" + two(second);
    case 'r': return two(hour12) + ":" + two(minute) + ":" + two(second) + (hour < 12 ? " AM" : " PM");
    case 'D': return two(month) + "/" + two(day) + "/" + two(Math.floorMod(year, 100L));
    case 'F': return pad(year, 4) + "-" + two(month) + "-" + two(day);
    case 'c':
      return kDays[weekday].substring(0, 3) + " " + kMonths[(int) month - 1].substring(0, 3) + " " + two(day) + " "
          + two(hour) + ":" + two(minute) + ":" + two(second) + " UTC " + pad(year, 4);
    default: throw new java.util.UnknownFormatConversionException("t" + conversion);
    }
  }
}
