/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;

public class TabStop implements Serializable {
  public static final int ALIGN_LEFT = 0;
  public static final int ALIGN_RIGHT = 1;
  public static final int ALIGN_CENTER = 2;
  public static final int ALIGN_DECIMAL = 4;
  public static final int ALIGN_BAR = 5;
  public static final int LEAD_NONE = 0;
  public static final int LEAD_DOTS = 1;
  public static final int LEAD_HYPHENS = 2;
  public static final int LEAD_UNDERLINE = 3;
  public static final int LEAD_THICKLINE = 4;
  public static final int LEAD_EQUALS = 5;

  private final int alignment;
  private final float position;
  private final int leader;

  public TabStop(float pos) { this(pos, ALIGN_LEFT, LEAD_NONE); }

  public TabStop(float pos, int align, int leader) {
    alignment = align;
    this.leader = leader;
    position = pos;
  }

  public float getPosition() { return position; }

  public int getAlignment() { return alignment; }

  public int getLeader() { return leader; }

  public boolean equals(Object other) {
    if (other instanceof TabStop) {
      TabStop o = (TabStop) other;
      return alignment == o.alignment && leader == o.leader && position == o.position;
    }
    return false;
  }

  public int hashCode() { return alignment ^ leader ^ Math.round(position); }

  public String toString() {
    String buf;
    switch (alignment) {
      case ALIGN_RIGHT: buf = "right "; break;
      case ALIGN_CENTER: buf = "center "; break;
      case ALIGN_DECIMAL: buf = "decimal "; break;
      case ALIGN_BAR: buf = "bar "; break;
      default: buf = ""; break;
    }
    buf = buf + "tab @" + position;
    if (leader != LEAD_NONE) {
      buf = buf + " (w/leaders)";
    }
    return buf;
  }
}
