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

public enum MinguoEra implements Era {
  BEFORE_ROC,
  ROC;

  public static MinguoEra of(int era) {
    switch (era) {
      case 0:
        return BEFORE_ROC;
      case 1:
        return ROC;
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
    boolean full = style == TextStyle.FULL || style == TextStyle.FULL_STANDALONE;
    boolean narrow = style == TextStyle.NARROW || style == TextStyle.NARROW_STANDALONE;
    String lang = locale.getLanguage();
    if (this == ROC) {
      return lang.isEmpty() ? "R.O.C." : "Minguo";
    }
    if (lang.isEmpty()) {
      return "Before R.O.C.";
    }
    if (lang.equals("de")) {
      return full ? "vor Volksrepublik China" : narrow ? "v. VR China" : "Before R.O.C.";
    }
    return "B.R.O.C.";
  }
}
