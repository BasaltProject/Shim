/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

public enum TextStyle {
  FULL,
  FULL_STANDALONE,
  SHORT,
  SHORT_STANDALONE,
  NARROW,
  NARROW_STANDALONE;

  public boolean isStandalone() {
    return (ordinal() & 1) == 1;
  }

  public TextStyle asStandalone() {
    return TextStyle.values()[ordinal() | 1];
  }

  public TextStyle asNormal() {
    return TextStyle.values()[ordinal() & ~1];
  }
}
