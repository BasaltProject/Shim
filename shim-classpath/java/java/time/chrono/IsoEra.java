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

public enum IsoEra implements Era {
  BCE,
  CE;

  public static IsoEra of(int isoEra) {
    switch (isoEra) {
      case 0:
        return BCE;
      case 1:
        return CE;
      default:
        throw new DateTimeException("Invalid era: " + isoEra);
    }
  }

  @Override
  public int getValue() {
    return ordinal();
  }

  @Override
  public String getDisplayName(TextStyle style, Locale locale) {
    switch (style) {
      case FULL:
      case FULL_STANDALONE:
        return this == BCE ? "Before Christ" : "Anno Domini";
      case NARROW:
      case NARROW_STANDALONE:
        return this == BCE ? "B" : "A";
      default:
        return this == BCE ? "BC" : "AD";
    }
  }
}
