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

public class ContainerEvent extends java.awt.event.ComponentEvent {
  Component child;
  public static final int CONTAINER_FIRST = 300;
  public static final int CONTAINER_LAST = 301;
  public static final int COMPONENT_ADDED = 300;
  public static final int COMPONENT_REMOVED = 301;
  public ContainerEvent(java.awt.Component p0, int p1, java.awt.Component p2) {
    super(p0, p1);
    this.child = p2;
  }
  public java.lang.String paramString() {
    return (id == COMPONENT_ADDED ? "COMPONENT_ADDED" : id == COMPONENT_REMOVED ? "COMPONENT_REMOVED" : "unknown type") + ",child=" + child.getName();
  }
  public java.awt.Component getChild() {
    return child;
  }
  public java.awt.Container getContainer() {
    return (source instanceof Container) ? (Container) source : null;
  }
}
