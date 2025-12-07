/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocaleDataModern {
  static final String[] TAGS = {"en-US", "en-GB", "es-ES", "es-MX", "de-DE", "de-CH", "fr-FR", "fr-CA", "it-IT",
      "pt-BR", "nl-NL", "ru-RU", "pl-PL", "tr-TR", "ja-JP", "ko-KR", "zh-CN", "zh-TW", "ar-SA", "ar-EG", "hi-IN",
      "sv-SE", "fi-FI", "cs-CZ", "el-GR", "he-IL", "th-TH", "fa-IR", "uk-UA", "af-ZA"};

  static void calendars(Date instant, TimeZone utc) {
    for (String tag : new String[] {"th-TH", "ja-JP-u-ca-japanese", "en-US-u-ca-buddhist", "th-TH-u-ca-gregory",
        "ja-JP"}) {
      Locale locale = Locale.forLanguageTag(tag);
      Calendar calendar = Calendar.getInstance(utc, locale);
      calendar.setTime(instant);
      System.out.println(tag + " calendar " + calendar.getCalendarType() + " " + calendar.get(Calendar.ERA) + " "
          + calendar.get(Calendar.YEAR) + " " + calendar.getFirstDayOfWeek());
      for (int style = DateFormat.FULL; style <= DateFormat.SHORT; style++) {
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(style, locale);
        format.setTimeZone(utc);
        System.out.println(tag + " calendar-date" + style + " " + format.format(instant) + " | " + format.toPattern());
      }
      calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 3);
      System.out.println(tag + " calendar-set " + calendar.get(Calendar.YEAR) + " "
          + calendar.getActualMaximum(Calendar.MONTH));
    }
    Calendar japanese = new Calendar.Builder().setCalendarType("japanese").setTimeZone(utc).build();
    for (long millis : new long[] {-3000000000000L, -1800000000000L, -600000000000L, 1000000000000L, 1600000000000L}) {
      japanese.setTimeInMillis(millis);
      System.out.println("japanese era " + japanese.get(Calendar.ERA) + " " + japanese.get(Calendar.YEAR));
    }
  }

  public static void main(String[] args) {
    calendars(new Date(1792254716201L), TimeZone.getTimeZone("UTC"));
    LocalDate date = LocalDate.of(2026, 10, 17);
    LocalDateTime dateTime = LocalDateTime.of(2026, 10, 17, 15, 4, 5);
    Date instant = new Date(1792254716201L);
    TimeZone utc = TimeZone.getTimeZone("UTC");
    for (String tag : TAGS) {
      Locale locale = Locale.forLanguageTag(tag);
      DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
      System.out.println(tag + " symbols " + symbols.getZeroDigit() + symbols.getGroupingSeparator()
          + symbols.getDecimalSeparator() + symbols.getMinusSign() + symbols.getPercent() + symbols.getInfinity());
      System.out.println(tag + " number " + NumberFormat.getInstance(locale).format(-1234567.891) + " | "
          + NumberFormat.getPercentInstance(locale).format(0.256) + " | "
          + NumberFormat.getIntegerInstance(locale).format(98765.5));
      System.out.println(tag + " format " + String.format(locale, "%d|%,d|%,.2f|%010.3f|%e", -1234567, -1234567,
          -9876543.215, -3.14159, -0.00012345));
      for (int style = DateFormat.FULL; style <= DateFormat.SHORT; style++) {
        SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(style, locale);
        dateFormat.setTimeZone(utc);
        SimpleDateFormat timeFormat = (SimpleDateFormat) DateFormat.getTimeInstance(style, locale);
        timeFormat.setTimeZone(utc);
        String time = style >= DateFormat.MEDIUM ? timeFormat.format(instant) + " | " + timeFormat.toPattern() : "";
        System.out.println(tag + " date" + style + " " + dateFormat.format(instant) + " | " + dateFormat.toPattern()
            + " | " + time);
      }
      DateFormatSymbols names = DateFormatSymbols.getInstance(locale);
      System.out.println(tag + " names " + String.join(",", names.getMonths()) + " | "
          + String.join(",", names.getShortWeekdays()) + " | " + String.join(",", names.getAmPmStrings()));
      System.out.println(tag + " java.time " + DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
          .format(date) + " | " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
          .withLocale(locale).format(dateTime));
      StringBuilder styles = new StringBuilder();
      for (TextStyle textStyle : TextStyle.values()) {
        styles.append(Month.OCTOBER.getDisplayName(textStyle, locale)).append(',')
            .append(DayOfWeek.SATURDAY.getDisplayName(textStyle, locale)).append(';');
      }
      System.out.println(tag + " styles " + styles);
      System.out.println(tag + " quarters " + DateTimeFormatter.ofPattern("G GGGG a QQQ QQQQ qqq", locale)
          .format(dateTime));
      WeekFields week = WeekFields.of(locale);
      System.out.println(tag + " week " + week.getFirstDayOfWeek() + " " + week.getMinimalDaysInFirstWeek());
    }
  }
}
