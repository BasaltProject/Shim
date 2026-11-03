/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public abstract class AWTEvent extends java.util.EventObject {
  protected int id;
  protected boolean consumed;
  public static final long COMPONENT_EVENT_MASK = 1L;
  public static final long CONTAINER_EVENT_MASK = 2L;
  public static final long FOCUS_EVENT_MASK = 4L;
  public static final long KEY_EVENT_MASK = 8L;
  public static final long MOUSE_EVENT_MASK = 16L;
  public static final long MOUSE_MOTION_EVENT_MASK = 32L;
  public static final long WINDOW_EVENT_MASK = 64L;
  public static final long ACTION_EVENT_MASK = 128L;
  public static final long ADJUSTMENT_EVENT_MASK = 256L;
  public static final long ITEM_EVENT_MASK = 512L;
  public static final long TEXT_EVENT_MASK = 1024L;
  public static final long INPUT_METHOD_EVENT_MASK = 2048L;
  public static final long PAINT_EVENT_MASK = 8192L;
  public static final long INVOCATION_EVENT_MASK = 16384L;
  public static final long HIERARCHY_EVENT_MASK = 32768L;
  public static final long HIERARCHY_BOUNDS_EVENT_MASK = 65536L;
  public static final long MOUSE_WHEEL_EVENT_MASK = 131072L;
  public static final long WINDOW_STATE_EVENT_MASK = 262144L;
  public static final long WINDOW_FOCUS_EVENT_MASK = 524288L;
  public static final int RESERVED_ID_MAX = 1999;
  public AWTEvent(java.lang.Object p0, int p1) {
    super(p0);
    this.id = p1;
  }
  public AWTEvent(java.awt.Event p0) {
    super(p0);
    this.id = p0.id;
  }
  public java.lang.String toString() {
    String srcName = null;
    if (source instanceof Component) {
      srcName = ((Component) source).getName();
    } else if (source instanceof MenuComponent) {
      srcName = ((MenuComponent) source).getName();
    }
    return getClass().getName() + "[" + paramString() + "] on " + (srcName != null ? srcName : source);
  }
  public java.lang.String paramString() {
    return "";
  }
  public int getID() {
    return id;
  }
  protected void consume() {
    consumed = true;
  }
  protected boolean isConsumed() {
    return consumed;
  }
  public void setSource(java.lang.Object p0) {
    if (source == p0) {
      return;
    }
    source = p0;
  }
}
