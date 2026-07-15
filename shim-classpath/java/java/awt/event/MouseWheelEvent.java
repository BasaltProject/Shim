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

public class MouseWheelEvent extends java.awt.event.MouseEvent {
  int scrollType;
  int scrollAmount;
  int wheelRotation;
  double preciseWheelRotation;
  public static final int WHEEL_UNIT_SCROLL = 0;
  public static final int WHEEL_BLOCK_SCROLL = 1;
  public MouseWheelEvent(java.awt.Component source, int id, long when, int modifiers, int x, int y, int xAbs,
      int yAbs, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation,
      double preciseWheelRotation) {
    super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, NOBUTTON);
    this.scrollType = scrollType;
    this.scrollAmount = scrollAmount;
    this.wheelRotation = wheelRotation;
    this.preciseWheelRotation = preciseWheelRotation;
  }
  public MouseWheelEvent(java.awt.Component source, int id, long when, int modifiers, int x, int y, int xAbs,
      int yAbs, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation) {
    this(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, scrollType, scrollAmount,
        wheelRotation, wheelRotation);
  }
  public MouseWheelEvent(java.awt.Component source, int id, long when, int modifiers, int x, int y, int clickCount,
      boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation) {
    super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
    this.scrollType = scrollType;
    this.scrollAmount = scrollAmount;
    this.wheelRotation = wheelRotation;
    this.preciseWheelRotation = wheelRotation;
  }
  public java.lang.String paramString() {
    String scrollTypeStr;
    if (getScrollType() == WHEEL_UNIT_SCROLL) {
      scrollTypeStr = "WHEEL_UNIT_SCROLL";
    } else if (getScrollType() == WHEEL_BLOCK_SCROLL) {
      scrollTypeStr = "WHEEL_BLOCK_SCROLL";
    } else {
      scrollTypeStr = "unknown scroll type";
    }
    return super.paramString() + ",scrollType=" + scrollTypeStr + ",scrollAmount=" + getScrollAmount()
        + ",wheelRotation=" + getWheelRotation() + ",preciseWheelRotation=" + getPreciseWheelRotation();
  }
  public int getScrollType() {
    return scrollType;
  }
  public int getScrollAmount() {
    return scrollAmount;
  }
  public int getWheelRotation() {
    return wheelRotation;
  }
  public double getPreciseWheelRotation() {
    return preciseWheelRotation;
  }
  public int getUnitsToScroll() {
    return scrollAmount * wheelRotation;
  }
}
