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
  private static final DateTimeTextProvider INSTANCE = new DateTimeTextProvider();

  static DateTimeTextProvider getInstance() {
    return INSTANCE;
  }

  private static int styleIndex(TextStyle style) {
    switch (style) {
      case FULL: return 0;
      case SHORT: return 1;
      case NARROW: return 2;
      case FULL_STANDALONE: return 3;
      case SHORT_STANDALONE: return 4;
      default: return 5;
    }
  }

  private static String[] slice(String[] all, int from, int count) {
    if (all == null || all.length < from + count) {
      return null;
    }
    String[] part = new String[count];
    System.arraycopy(all, from, part, 0, count);
    return part;
  }

  private static String[] table(TemporalField field, TextStyle style, Locale locale) {
    int index = styleIndex(style);
    if (field == ChronoField.MONTH_OF_YEAR) {
      return shim.LocaleDb.list(locale, "M" + index);
    }
    if (field == ChronoField.DAY_OF_WEEK) {
      return shim.LocaleDb.list(locale, "D" + index);
    }
    if (field == ChronoField.AMPM_OF_DAY) {
      return slice(shim.LocaleDb.list(locale, "AMP"), styleIndex(style.asNormal()) * 2, 2);
    }
    if (field == ChronoField.ERA) {
      int normal = styleIndex(style.asNormal());
      return slice(shim.LocaleDb.list(locale, "E"), (normal == 0 ? 1 : normal == 1 ? 0 : 2) * 2, 2);
    }
    if (field == IsoFields.QUARTER_OF_YEAR) {
      int[] order = {1, 0, 2, 4, 3, 5};
      return slice(shim.LocaleDb.list(locale, "Q"), order[index] * 4, 4);
    }
    return null;
  }

  private static long base(TemporalField field) {
    return (field == ChronoField.AMPM_OF_DAY || field == ChronoField.ERA) ? 0 : 1;
  }

  String getText(TemporalField field, long value, TextStyle style, Locale locale) {
    String[] names = table(field, style, locale);
    if (names == null) {
      return null;
    }
    long index = value - base(field);
    if (index < 0 || index >= names.length) {
      return null;
    }
    return names[(int) index];
  }

  private static final String[] kHijrahFull = {"Muharram", "Safar", "Rabi\u02bb I", "Rabi\u02bb II", "Jumada I",
      "Jumada II", "Rajab", "Sha\u02bbban", "Ramadan", "Shawwal", "Dhu\u02bbl-Qi\u02bbdah", "Dhu\u02bbl-Hijjah"};
  private static final String[] kHijrahShort = {"Muh.", "Saf.", "Rab. I", "Rab. II", "Jum. I", "Jum. II", "Raj.",
      "Sha.", "Ram.", "Shaw.", "Dhu\u02bbl-Q.", "Dhu\u02bbl-H."};
  private static final String[] kHijrahNarrow = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

  private static boolean isHijrahMonth(java.time.chrono.Chronology chrono, TemporalField field) {
    return field == ChronoField.MONTH_OF_YEAR && chrono instanceof java.time.chrono.HijrahChronology;
  }

  private static String[] hijrahMonths(TextStyle style) {
    TextStyle normal = style.asNormal();
    return normal == TextStyle.FULL ? kHijrahFull : normal == TextStyle.SHORT ? kHijrahShort : kHijrahNarrow;
  }

  String getText(java.time.chrono.Chronology chrono, TemporalField field, long value, TextStyle style,
      Locale locale) {
    if (isHijrahMonth(chrono, field)) {
      return value >= 1 && value <= 12 ? hijrahMonths(style)[(int) value - 1] : null;
    }
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
      String[] names = isHijrahMonth(chrono, field) ? hijrahMonths(s) : table(field, s, locale);
      if (field == ChronoField.ERA && chrono != null && chrono != java.time.chrono.IsoChronology.INSTANCE) {
        List<java.time.chrono.Era> eras = chrono.eras();
        names = new String[eras.size()];
        for (int i = 0; i < names.length; i++) {
          names[i] = eras.get(i).getDisplayName(s, locale);
          unique.put(names[i], Long.valueOf(eras.get(i).getValue()));
        }
        any = true;
        continue;
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
