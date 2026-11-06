/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.font.LineMetrics;

public final class SimpleLineMetrics extends LineMetrics {
  private final float ascent;
  private final float descent;
  private final float leading;
  private final float size;
  private final int numChars;
  private final float[] style;

  public SimpleLineMetrics(FontFace face, float size, int numChars) {
    this(face, size, numChars, 1f, 0f);
  }

  public SimpleLineMetrics(FontFace face, float size, int numChars, float scale, float shiftUp) {
    float asc = face.pixels(face.ascent(), size);
    float desc = face.pixels(face.descent(), size);
    float lead = face.leadingFromHeight() ? face.pixels(face.lineHeight(), size) - asc - desc
        : face.pixels(face.leading(), size);
    this.ascent = asc * scale + shiftUp;
    this.descent = desc * scale - shiftUp;
    this.leading = lead * scale;
    this.size = size;
    this.numChars = numChars;
    float[] st = face.styleMetrics(size, asc);
    this.style = new float[] {st[0] * scale - shiftUp, st[1] * scale, st[2] * scale - shiftUp, st[3] * scale};
  }

  public int getNumChars() { return numChars; }

  public int getBaselineIndex() { return 0; }

  public float[] getBaselineOffsets() { return new float[] {0f, (descent / 2f - ascent) / 2f, -ascent}; }

  public float getStrikethroughOffset() { return style[0]; }

  public float getStrikethroughThickness() { return style[1]; }

  public float getUnderlineOffset() { return style[2]; }

  public float getUnderlineThickness() { return style[3]; }

  public float getHeight() { return ascent + descent + leading; }

  public float getAscent() { return ascent; }

  public float getDescent() { return descent; }

  public float getLeading() { return leading; }
}
