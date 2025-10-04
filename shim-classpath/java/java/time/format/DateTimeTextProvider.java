/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class DateTimeTextProvider {
  private static final String[] kMonthsFull = {"January", "February", "March", "April", "May", "June", "July",
      "August", "September", "October", "November", "December"};
  private static final String[] kMonthsShort = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
      "Nov", "Dec"};
  private static final String[] kMonthsNarrow = {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};
  private static final String[] kDaysFull = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
      "Sunday"};
  private static final String[] kDaysShort = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
  private static final String[] kDaysNarrow = {"M", "T", "W", "T", "F", "S", "S"};

  private static final DateTimeTextProvider INSTANCE = new DateTimeTextProvider();

  static DateTimeTextProvider getInstance() {
    return INSTANCE;
  }

  private static String[] table(TemporalField field, TextStyle style) {
    TextStyle normal = style.asNormal();
    if (field == ChronoField.MONTH_OF_YEAR) {
      return normal == TextStyle.FULL ? kMonthsFull : normal == TextStyle.SHORT ? kMonthsShort : kMonthsNarrow;
    }
    if (field == ChronoField.DAY_OF_WEEK) {
      return normal == TextStyle.FULL ? kDaysFull : normal == TextStyle.SHORT ? kDaysShort : kDaysNarrow;
    }
    if (field == ChronoField.AMPM_OF_DAY) {
      return normal == TextStyle.NARROW ? new String[] {"a", "p"} : new String[] {"AM", "PM"};
    }
    if (field == ChronoField.ERA) {
      return normal == TextStyle.FULL ? new String[] {"Before Christ", "Anno Domini"}
          : normal == TextStyle.SHORT ? new String[] {"BC", "AD"} : new String[] {"B", "A"};
    }
    if (field == IsoFields.QUARTER_OF_YEAR) {
      return normal == TextStyle.FULL
          ? new String[] {"1st quarter", "2nd quarter", "3rd quarter", "4th quarter"}
          : normal == TextStyle.SHORT ? new String[] {"Q1", "Q2", "Q3", "Q4"} : new String[] {"1", "2", "3", "4"};
    }
    return null;
  }

  private static long base(TemporalField field) {
    return (field == ChronoField.AMPM_OF_DAY || field == ChronoField.ERA) ? 0 : 1;
  }

  String getText(TemporalField field, long value, TextStyle style, Locale locale) {
    String[] names = table(field, style);
    if (names == null) {
      return null;
    }
    long index = value - base(field);
    if (index < 0 || index >= names.length) {
      return null;
    }
    return names[(int) index];
  }

  String getText(java.time.chrono.Chronology chrono, TemporalField field, long value, TextStyle style,
      Locale locale) {
    if (field == ChronoField.ERA && chrono != null && chrono != java.time.chrono.IsoChronology.INSTANCE) {
      try {
        return chrono.eraOf((int) value).getDisplayName(style, locale);
      } catch (java.time.DateTimeException e) {
        return null;
      }
    }
    return getText(field, value, style, locale);
  }

  Iterator<Map.Entry<String, Long>> getTextIterator(java.time.chrono.Chronology chrono, TemporalField field,
      TextStyle style, Locale locale) {
    Map<String, Long> unique = new java.util.LinkedHashMap<String, Long>();
    TextStyle[] styles = style == null ? new TextStyle[] {TextStyle.FULL, TextStyle.SHORT, TextStyle.NARROW}
        : new TextStyle[] {style};
    boolean any = false;
    for (TextStyle s : styles) {
      String[] names = table(field, s);
      if (field == ChronoField.ERA && chrono != null && chrono != java.time.chrono.IsoChronology.INSTANCE) {
        List<java.time.chrono.Era> eras = chrono.eras();
        names = new String[eras.size()];
        for (int i = 0; i < names.length; i++) {
          names[i] = eras.get(i).getDisplayName(s, locale);
        }
      }
      if (names == null) {
        continue;
      }
      any = true;
      for (int i = 0; i < names.length; i++) {
        unique.put(names[i], Long.valueOf(i + base(field)));
      }
    }
    if (!any) {
      return null;
    }
    List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>();
    for (Map.Entry<String, Long> e : unique.entrySet()) {
      list.add(new AbstractMap.SimpleImmutableEntry<String, Long>(e.getKey(), e.getValue()));
    }
    sortByLength(list);
    return list.iterator();
  }

  Iterator<Map.Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
    return getTextIterator(null, field, style, locale);
  }

  static void sortByLength(List<Map.Entry<String, Long>> list) {
    Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
      @Override
      public int compare(Map.Entry<String, Long> a, Map.Entry<String, Long> b) {
        return b.getKey().length() - a.getKey().length();
      }
    });
  }
}
