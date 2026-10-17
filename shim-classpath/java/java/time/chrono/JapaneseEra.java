/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public final class JapaneseEra implements Era, Serializable {
  private static final long serialVersionUID = 1466499369062886794L;

  static final int ERA_OFFSET = 2;

  public static final JapaneseEra MEIJI = new JapaneseEra(-1, LocalDate.of(1868, 1, 1), "Meiji", "M",
      "\u660e\u6cbb", "Meiji");
  public static final JapaneseEra TAISHO = new JapaneseEra(0, LocalDate.of(1912, 7, 30), "Taisho", "T",
      "\u5927\u6b63", "Taish\u014d");
  public static final JapaneseEra SHOWA = new JapaneseEra(1, LocalDate.of(1926, 12, 25), "Showa", "S",
      "\u662d\u548c", "Sh\u014dwa");
  public static final JapaneseEra HEISEI = new JapaneseEra(2, LocalDate.of(1989, 1, 8), "Heisei", "H",
      "\u5e73\u6210", "Heisei");
  public static final JapaneseEra REIWA = new JapaneseEra(3, LocalDate.of(2019, 5, 1), "Reiwa", "R",
      "\u4ee4\u548c", "Reiwa");

  private static final JapaneseEra[] KNOWN = {MEIJI, TAISHO, SHOWA, HEISEI, REIWA};

  private final int eraValue;
  private final transient LocalDate since;
  private final transient String name;
  private final transient String narrow;
  private final transient String japanese;
  private final transient String english;

  private JapaneseEra(int eraValue, LocalDate since, String name, String narrow, String japanese, String english) {
    this.eraValue = eraValue;
    this.since = since;
    this.name = name;
    this.narrow = narrow;
    this.japanese = japanese;
    this.english = english;
  }

  LocalDate startDate() {
    return since;
  }

  LocalDate endDate() {
    int index = eraValue + 1;
    if (index + 1 < KNOWN.length) {
      return KNOWN[index + 1].since.minusDays(1);
    }
    return LocalDate.MAX;
  }

  public static JapaneseEra of(int japaneseEra) {
    int i = japaneseEra + ERA_OFFSET - 1;
    if (i < 0 || i >= KNOWN.length) {
      throw new DateTimeException("Invalid era: " + japaneseEra);
    }
    return KNOWN[i];
  }

  public static JapaneseEra valueOf(String japaneseEra) {
    Objects.requireNonNull(japaneseEra, "japaneseEra");
    for (JapaneseEra era : KNOWN) {
      if (era.name.equals(japaneseEra)) {
        return era;
      }
    }
    throw new IllegalArgumentException("japaneseEra is invalid");
  }

  public static JapaneseEra[] values() {
    return Arrays.copyOf(KNOWN, KNOWN.length);
  }

  static JapaneseEra from(LocalDate date) {
    for (int i = KNOWN.length - 1; i >= 0; i--) {
      if (!date.isBefore(KNOWN[i].since)) {
        return KNOWN[i];
      }
    }
    throw new DateTimeException("JapaneseDate before Meiji 6 is not supported");
  }

  @Override
  public int getValue() {
    return eraValue;
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field == ChronoField.ERA) {
      return JapaneseChronology.INSTANCE.range(ChronoField.ERA);
    }
    return Era.super.range(field);
  }

  @Override
  public long getLong(TemporalField field) {
    if (field == ChronoField.ERA) {
      return getValue();
    }
    if (field instanceof ChronoField) {
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }
    return field.getFrom(this);
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    Objects.requireNonNull(style, "style");
    Objects.requireNonNull(locale, "locale");
    boolean narrowStyle = style == TextStyle.NARROW || style == TextStyle.NARROW_STANDALONE;
    if ("ja".equals(locale.getLanguage())) {
      return narrowStyle ? narrow : japanese;
    }
    return narrowStyle ? narrow : english;
  }

  @Override
  public String toString() {
    return name;
  }

  private Object readResolve() {
    return of(eraValue);
  }
}
