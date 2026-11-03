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

public class ComponentEvent extends java.awt.AWTEvent {
  public static final int COMPONENT_FIRST = 100;
  public static final int COMPONENT_LAST = 103;
  public static final int COMPONENT_MOVED = 100;
  public static final int COMPONENT_RESIZED = 101;
  public static final int COMPONENT_SHOWN = 102;
  public static final int COMPONENT_HIDDEN = 103;
  public ComponentEvent(java.awt.Component p0, int p1) {
    super(p0, p1);
  }
  public java.lang.String paramString() {
    String typeStr;
    switch (id) {
    case COMPONENT_SHOWN:
      typeStr = "COMPONENT_SHOWN";
      break;
    case COMPONENT_HIDDEN:
      typeStr = "COMPONENT_HIDDEN";
      break;
    case COMPONENT_MOVED:
      typeStr = "COMPONENT_MOVED (" + boundsText();
      break;
    case COMPONENT_RESIZED:
      typeStr = "COMPONENT_RESIZED (" + boundsText();
      break;
    default:
      typeStr = "unknown type";
    }
    return typeStr;
  }
  private String boundsText() {
    Rectangle r = ((Component) source).getBounds();
    return r.x + "," + r.y + " " + r.width + "x" + r.height + ")";
  }
  public java.awt.Component getComponent() {
    return (source instanceof Component) ? (Component) source : null;
  }
}
