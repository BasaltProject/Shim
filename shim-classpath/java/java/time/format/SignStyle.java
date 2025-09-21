/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

public enum SignStyle {
  NORMAL,
  ALWAYS,
  NEVER,
  NOT_NEGATIVE,
  EXCEEDS_PAD;

  boolean parse(boolean positive, boolean strict, boolean fixedWidth) {
    switch (ordinal()) {
      case 0:
        return !positive || !strict;
      case 1:
      case 4:
        return true;
      default:
        return !strict && !fixedWidth;
    }
  }
}
