/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.time.DateTimeException;
import java.time.format.TextStyle;
import java.util.Locale;

public enum ThaiBuddhistEra implements Era {
  BEFORE_BE,
  BE;

  public static ThaiBuddhistEra of(int era) {
    switch (era) {
      case 0:
        return BEFORE_BE;
      case 1:
        return BE;
      default:
        throw new DateTimeException("Invalid era: " + era);
    }
  }

  @Override
  public int getValue() {
    return ordinal();
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    java.util.Objects.requireNonNull(style, "style");
    java.util.Objects.requireNonNull(locale, "locale");
    if (this == BEFORE_BE) {
      return "BC";
    }
    boolean full = style == TextStyle.FULL || style == TextStyle.FULL_STANDALONE;
    return full && locale.getLanguage().equals("de") ? "B.E." : "BE";
  }
}
