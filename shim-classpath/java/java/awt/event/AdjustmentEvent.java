/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class AdjustmentEvent extends java.awt.AWTEvent {
  Adjustable adjustable;
  int value;
  int adjustmentType;
  boolean isAdjusting;
  public static final int ADJUSTMENT_FIRST = 601;
  public static final int ADJUSTMENT_LAST = 601;
  public static final int ADJUSTMENT_VALUE_CHANGED = 601;
  public static final int UNIT_INCREMENT = 1;
  public static final int UNIT_DECREMENT = 2;
  public static final int BLOCK_DECREMENT = 3;
  public static final int BLOCK_INCREMENT = 4;
  public static final int TRACK = 5;
  public AdjustmentEvent(java.awt.Adjustable p0, int p1, int p2, int p3) {
    super(p0, p1);
    this.adjustable = p0;
    this.adjustmentType = p2;
    this.value = p3;
  }
  public AdjustmentEvent(java.awt.Adjustable p0, int p1, int p2, int p3, boolean p4) {
    super(p0, p1);
    this.adjustable = p0;
    this.adjustmentType = p2;
    this.value = p3;
    this.isAdjusting = p4;
  }
  public int getValue() {
    return value;
  }
  public java.lang.String paramString() {
    return (id == ADJUSTMENT_VALUE_CHANGED ? "ADJUSTMENT_VALUE_CHANGED" : "unknown type") + ",adjType=" + adjustmentType + ",value=" + value + ",isAdjusting=" + isAdjusting;
  }
  public int getAdjustmentType() {
    return adjustmentType;
  }
  public boolean getValueIsAdjusting() {
    return isAdjusting;
  }
  public java.awt.Adjustable getAdjustable() {
    return adjustable;
  }
}
