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

public class FocusEvent extends java.awt.event.ComponentEvent {
  boolean temporary;
  Component opposite;
  Cause cause = Cause.UNKNOWN;
  public static final int FOCUS_FIRST = 1004;
  public static final int FOCUS_LAST = 1005;
  public static final int FOCUS_GAINED = 1004;
  public static final int FOCUS_LOST = 1005;
  public FocusEvent(java.awt.Component p0, int p1) {
    super(p0, p1);
  }
  public FocusEvent(java.awt.Component p0, int p1, boolean p2) {
    super(p0, p1);
    this.temporary = p2;
  }
  public FocusEvent(java.awt.Component p0, int p1, boolean p2, java.awt.Component p3, java.awt.event.FocusEvent.Cause p4) {
    super(p0, p1);
    this.temporary = p2;
    this.opposite = p3;
    this.cause = p4;
  }
  public FocusEvent(java.awt.Component p0, int p1, boolean p2, java.awt.Component p3) {
    super(p0, p1);
    this.temporary = p2;
    this.opposite = p3;
  }
  public final java.awt.event.FocusEvent.Cause getCause() {
    return cause;
  }
  public java.lang.String paramString() {
    String typeStr;
    switch (id) {
    case FOCUS_GAINED:
      typeStr = "FOCUS_GAINED";
      break;
    case FOCUS_LOST:
      typeStr = "FOCUS_LOST";
      break;
    default:
      typeStr = "unknown type";
    }
    return typeStr + (temporary ? ",temporary" : ",permanent") + ",opposite=" + opposite + ",cause=" + cause;
  }
  public java.awt.Component getOppositeComponent() {
    return opposite;
  }
  public boolean isTemporary() {
    return temporary;
  }

  public enum Cause {
    UNKNOWN, MOUSE_EVENT, TRAVERSAL, TRAVERSAL_UP, TRAVERSAL_DOWN, TRAVERSAL_FORWARD, TRAVERSAL_BACKWARD, ROLLBACK, UNEXPECTED, ACTIVATION, CLEAR_GLOBAL_FOCUS_OWNER;
  }
}
