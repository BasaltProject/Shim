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

public class Canvas extends java.awt.Component implements javax.accessibility.Accessible {
  private static int nameCounter;

  String constructComponentName() {
    synchronized (Canvas.class) {
      return "canvas" + nameCounter++;
    }
  }
  public Canvas() {
  }
  public Canvas(java.awt.GraphicsConfiguration p0) {
  }
  public void update(java.awt.Graphics p0) {
    p0.clearRect(0, 0, width, height);
    paint(p0);
  }
  public void addNotify() {
    
  }
  public void paint(java.awt.Graphics p0) {
    p0.clearRect(0, 0, width, height);
  }
  public void createBufferStrategy(int p0) {
    throw new IllegalStateException("Component must have a valid peer");
  }
  public void createBufferStrategy(int p0, java.awt.BufferCapabilities p1) throws java.awt.AWTException {
    throw new IllegalStateException("Component must have a valid peer");
  }
  public java.awt.image.BufferStrategy getBufferStrategy() { return null; }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleAWTCanvas();
    }
    return accessibleContext;
  }
  protected class AccessibleAWTCanvas extends AccessibleAWTComponent {
    protected AccessibleAWTCanvas() { }
    public javax.accessibility.AccessibleRole getAccessibleRole() {
      return javax.accessibility.AccessibleRole.CANVAS;
    }
  }
}
