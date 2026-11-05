/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Shape;

public abstract class FontFace {
  public abstract String family();

  public abstract boolean canDisplay(int cp);

  public abstract float advance(int cp);

  public abstract Shape outline(int cp, boolean bold, boolean italic);

  public abstract float ascent();

  public abstract float descent();

  public abstract float leading();

  public String fullName() {
    return null;
  }

  public String postScriptName() {
    return null;
  }

  public int numGlyphs() {
    return 95;
  }

  public int missingGlyph() {
    return 0;
  }

  public int hintedAdvance(int cp, float size) {
    return -1;
  }

  public int maxAdvancePixels(float size) {
    return Math.round(size);
  }

  private static boolean zeroWidth(int cp) {
    return cp == 0 || (cp >= 9 && cp <= 13) || cp == 0x85 || cp == 0xAD || (cp >= 0x200B && cp <= 0x200F)
        || (cp >= 0x2028 && cp <= 0x202E) || (cp >= 0x2060 && cp <= 0x206F) || cp == 0xFEFF;
  }

  private static boolean wide(int cp) {
    return (cp >= 0x3000 && cp <= 0x30FF) || (cp >= 0x3400 && cp <= 0x4DBF) || (cp >= 0x4E00 && cp <= 0x9FFF)
        || (cp >= 0xAC00 && cp <= 0xD7AF) || (cp >= 0xF900 && cp <= 0xFAFF) || (cp >= 0xFF01 && cp <= 0xFF60)
        || (cp >= 0xFFE0 && cp <= 0xFFE6);
  }

  public float pixelAdvance(int cp, float size, boolean fractional, boolean hinted) {
    if (zeroWidth(cp)) {
      return 0f;
    }
    if (wide(cp) && !canDisplay(cp)) {
      return fractional ? size : Math.round(size);
    }
    if (fractional) {
      return advance(cp) * size;
    }
    if (hinted) {
      int h = hintedAdvance(cp, size);
      if (h >= 0) {
        return h;
      }
    }
    return Math.round(advance(cp) * size);
  }

  public boolean styled() {
    return false;
  }

  public float kerning(int a, int b) {
    return 0f;
  }
}
