/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.font;

public interface MultipleMaster {
  int getNumDesignAxes();
  float[] getDesignAxisRanges();
  float[] getDesignAxisDefaults();
  java.lang.String[] getDesignAxisNames();
  java.awt.Font deriveMMFont(float[] p0);
  java.awt.Font deriveMMFont(float[] p0, float p1, float p2, float p3, float p4);
}
