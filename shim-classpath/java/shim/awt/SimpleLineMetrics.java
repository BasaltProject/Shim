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

  public SimpleLineMetrics(FontFace face, float size, int numChars) {
    this.ascent = face.ascent() * size;
    this.descent = face.descent() * size;
    this.leading = face.leading() * size;
    this.size = size;
    this.numChars = numChars;
  }

  public int getNumChars() { return numChars; }

  public int getBaselineIndex() { return 0; }

  public float[] getBaselineOffsets() { return new float[] {0f, (descent - ascent) / 2f, -ascent}; }

  public float getStrikethroughOffset() { return -ascent / 3f; }

  public float getStrikethroughThickness() { return Math.max(1f, size / 12f); }

  public float getUnderlineOffset() { return descent / 3f; }

  public float getUnderlineThickness() { return Math.max(1f, size / 12f); }

  public float getHeight() { return ascent + descent + leading; }

  public float getAscent() { return ascent; }

  public float getDescent() { return descent; }

  public float getLeading() { return leading; }
}
