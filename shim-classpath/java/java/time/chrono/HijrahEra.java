/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.DateTimeException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Objects;

public enum HijrahEra implements Era {
  AH;

  public static HijrahEra of(int hijrahEra) {
    if (hijrahEra == 1) {
      return AH;
    }
    throw new DateTimeException("Invalid era: " + hijrahEra);
  }

  @Override
  public int getValue() {
    return 1;
  }

  @Override
  public ValueRange range(TemporalField field) {
    if (field == ChronoField.ERA) {
      return ValueRange.of(1, 1);
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
    return "ar".equals(locale.getLanguage()) ? "\u0647\u0640" : "AH";
  }
}
