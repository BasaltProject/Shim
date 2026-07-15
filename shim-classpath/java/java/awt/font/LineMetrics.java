/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

public abstract class LineMetrics {
  protected LineMetrics() {
  }
  public abstract int getBaselineIndex();
  public abstract float[] getBaselineOffsets();
  public abstract int getNumChars();
  public abstract float getStrikethroughOffset();
  public abstract float getStrikethroughThickness();
  public abstract float getUnderlineOffset();
  public abstract float getUnderlineThickness();
  public abstract float getHeight();
  public abstract float getAscent();
  public abstract float getDescent();
  public abstract float getLeading();
}
