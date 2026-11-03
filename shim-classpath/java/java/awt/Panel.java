/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

public class Panel extends java.awt.Container implements javax.accessibility.Accessible {
  private static int nameCounter;

  String constructComponentName() {
    synchronized (Panel.class) {
      return "panel" + nameCounter++;
    }
  }
  public Panel() {
    setLayout(new FlowLayout());
  }
  public Panel(java.awt.LayoutManager p0) {
    setLayout(p0);
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleAWTPanel();
    }
    return accessibleContext;
  }
  protected class AccessibleAWTPanel extends AccessibleAWTContainer {
    protected AccessibleAWTPanel() { }
    public javax.accessibility.AccessibleRole getAccessibleRole() {
      return javax.accessibility.AccessibleRole.PANEL;
    }
  }
  public void addNotify() { }
}
