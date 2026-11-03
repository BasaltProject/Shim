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

public class WindowEvent extends java.awt.event.ComponentEvent {
  Window opposite;
  int oldState;
  int newState;
  public static final int WINDOW_FIRST = 200;
  public static final int WINDOW_OPENED = 200;
  public static final int WINDOW_CLOSING = 201;
  public static final int WINDOW_CLOSED = 202;
  public static final int WINDOW_ICONIFIED = 203;
  public static final int WINDOW_DEICONIFIED = 204;
  public static final int WINDOW_ACTIVATED = 205;
  public static final int WINDOW_DEACTIVATED = 206;
  public static final int WINDOW_GAINED_FOCUS = 207;
  public static final int WINDOW_LOST_FOCUS = 208;
  public static final int WINDOW_STATE_CHANGED = 209;
  public static final int WINDOW_LAST = 209;
  public WindowEvent(java.awt.Window p0, int p1) {
    super(p0, p1);
  }
  public WindowEvent(java.awt.Window p0, int p1, int p2, int p3) {
    super(p0, p1);
    this.oldState = p2;
    this.newState = p3;
  }
  public WindowEvent(java.awt.Window p0, int p1, java.awt.Window p2) {
    super(p0, p1);
    this.opposite = p2;
  }
  public WindowEvent(java.awt.Window p0, int p1, java.awt.Window p2, int p3, int p4) {
    super(p0, p1);
    this.opposite = p2;
    this.oldState = p3;
    this.newState = p4;
  }
  public java.lang.String paramString() {
    return "WINDOW_" + id + ",opposite=" + opposite + ",oldState=" + oldState + ",newState=" + newState;
  }
  public java.awt.Window getOppositeWindow() {
    return opposite;
  }
  public java.awt.Window getWindow() {
    return (source instanceof Window) ? (Window) source : null;
  }
  public int getOldState() {
    return oldState;
  }
  public int getNewState() {
    return newState;
  }
}
