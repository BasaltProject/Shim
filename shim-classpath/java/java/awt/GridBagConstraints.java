/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class GridBagConstraints implements java.lang.Cloneable, java.io.Serializable {
  public static final int RELATIVE = -1;
  public static final int REMAINDER = 0;
  public static final int NONE = 0;
  public static final int BOTH = 1;
  public static final int HORIZONTAL = 2;
  public static final int VERTICAL = 3;
  public static final int CENTER = 10;
  public static final int NORTH = 11;
  public static final int NORTHEAST = 12;
  public static final int EAST = 13;
  public static final int SOUTHEAST = 14;
  public static final int SOUTH = 15;
  public static final int SOUTHWEST = 16;
  public static final int WEST = 17;
  public static final int NORTHWEST = 18;
  public static final int PAGE_START = 19;
  public static final int PAGE_END = 20;
  public static final int LINE_START = 21;
  public static final int LINE_END = 22;
  public static final int FIRST_LINE_START = 23;
  public static final int FIRST_LINE_END = 24;
  public static final int LAST_LINE_START = 25;
  public static final int LAST_LINE_END = 26;
  public static final int BASELINE = 256;
  public static final int BASELINE_LEADING = 512;
  public static final int BASELINE_TRAILING = 768;
  public static final int ABOVE_BASELINE = 1024;
  public static final int ABOVE_BASELINE_LEADING = 1280;
  public static final int ABOVE_BASELINE_TRAILING = 1536;
  public static final int BELOW_BASELINE = 1792;
  public static final int BELOW_BASELINE_LEADING = 2048;
  public static final int BELOW_BASELINE_TRAILING = 2304;
  public int gridx = RELATIVE;
  public int gridy = RELATIVE;
  public int gridwidth = 1;
  public int gridheight = 1;
  public double weightx = 0;
  public double weighty = 0;
  public int anchor = CENTER;
  public int fill = NONE;
  public java.awt.Insets insets = new java.awt.Insets(0, 0, 0, 0);
  public int ipadx = 0;
  public int ipady = 0;
  int tempX;
  int tempY;
  int tempWidth;
  int tempHeight;
  int minWidth;
  int minHeight;
  public GridBagConstraints() {
  }
  public GridBagConstraints(int p0, int p1, int p2, int p3, double p4, double p5, int p6, int p7, java.awt.Insets p8, int p9, int p10) {
    this.gridx = p0;
    this.gridy = p1;
    this.gridwidth = p2;
    this.gridheight = p3;
    this.weightx = p4;
    this.weighty = p5;
    this.anchor = p6;
    this.fill = p7;
    this.insets = p8;
    this.ipadx = p9;
    this.ipady = p10;
  }
  public java.lang.Object clone() {
    try {
      GridBagConstraints c = (GridBagConstraints) super.clone();
      c.insets = (java.awt.Insets) insets.clone();
      return c;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
