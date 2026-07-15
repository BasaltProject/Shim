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

public class ActionEvent extends java.awt.AWTEvent {
  String actionCommand;
  long when;
  int modifiers;
  public static final int SHIFT_MASK = 1;
  public static final int CTRL_MASK = 2;
  public static final int META_MASK = 4;
  public static final int ALT_MASK = 8;
  public static final int ACTION_FIRST = 1001;
  public static final int ACTION_LAST = 1001;
  public static final int ACTION_PERFORMED = 1001;
  public ActionEvent(java.lang.Object p0, int p1, java.lang.String p2, long p3, int p4) {
    super(p0, p1);
    this.actionCommand = p2;
    this.when = p3;
    this.modifiers = p4;
  }
  public ActionEvent(java.lang.Object p0, int p1, java.lang.String p2, int p3) {
    super(p0, p1);
    this.actionCommand = p2;
    this.when = System.currentTimeMillis();
    this.modifiers = p3;
  }
  public ActionEvent(java.lang.Object p0, int p1, java.lang.String p2) {
    super(p0, p1);
    this.actionCommand = p2;
    this.when = System.currentTimeMillis();
  }
  public int getModifiers() {
    return modifiers;
  }
  public java.lang.String paramString() {
    String typeStr;
    switch (id) {
    case ACTION_PERFORMED:
      typeStr = "ACTION_PERFORMED";
      break;
    default:
      typeStr = "unknown type";
    }
    return typeStr + ",cmd=" + actionCommand + ",when=" + when + ",modifiers=" + KeyEvent.getKeyModifiersText(modifiers);
  }
  public long getWhen() {
    return when;
  }
  public java.lang.String getActionCommand() {
    return actionCommand;
  }
}
