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

public class PaintEvent extends java.awt.event.ComponentEvent {
  Rectangle updateRect;
  public static final int PAINT_FIRST = 800;
  public static final int PAINT_LAST = 801;
  public static final int PAINT = 800;
  public static final int UPDATE = 801;
  public PaintEvent(java.awt.Component p0, int p1, java.awt.Rectangle p2) {
    super(p0, p1);
    this.updateRect = p2;
  }
  public java.lang.String paramString() {
    return (id == PAINT ? "PAINT" : id == UPDATE ? "UPDATE" : "unknown type") + ",updateRect=" + (updateRect != null ? updateRect.toString() : "null");
  }
  public java.awt.Rectangle getUpdateRect() {
    return updateRect;
  }
  public void setUpdateRect(java.awt.Rectangle p0) {
    this.updateRect = p0;
  }
}
