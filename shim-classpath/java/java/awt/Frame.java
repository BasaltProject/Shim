/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Frame extends java.awt.Window implements java.awt.MenuContainer {
  public static final int DEFAULT_CURSOR = 0;
  public static final int CROSSHAIR_CURSOR = 1;
  public static final int TEXT_CURSOR = 2;
  public static final int WAIT_CURSOR = 3;
  public static final int SW_RESIZE_CURSOR = 4;
  public static final int SE_RESIZE_CURSOR = 5;
  public static final int NW_RESIZE_CURSOR = 6;
  public static final int NE_RESIZE_CURSOR = 7;
  public static final int N_RESIZE_CURSOR = 8;
  public static final int S_RESIZE_CURSOR = 9;
  public static final int W_RESIZE_CURSOR = 10;
  public static final int E_RESIZE_CURSOR = 11;
  public static final int HAND_CURSOR = 12;
  public static final int MOVE_CURSOR = 13;
  public static final int NORMAL = 0;
  public static final int ICONIFIED = 1;
  public static final int MAXIMIZED_HORIZ = 2;
  public static final int MAXIMIZED_VERT = 4;
  public static final int MAXIMIZED_BOTH = 6;
  public Frame(java.lang.String p0, java.awt.GraphicsConfiguration p1) {
    super((java.awt.Frame) null);
    throw new java.awt.HeadlessException();
  }
  public Frame(java.lang.String p0) throws java.awt.HeadlessException {
    super((java.awt.Frame) null);
    throw new java.awt.HeadlessException();
  }
  public Frame(java.awt.GraphicsConfiguration p0) {
    super((java.awt.Frame) null);
    throw new java.awt.HeadlessException();
  }
  public Frame() throws java.awt.HeadlessException {
    super((java.awt.Frame) null);
    throw new java.awt.HeadlessException();
  }
  public void remove(java.awt.MenuComponent p0) { throw new UnsupportedOperationException(); }
  public int getState() { throw new UnsupportedOperationException(); }
  public void setState(int p0) { throw new UnsupportedOperationException(); }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public void setMenuBar(java.awt.MenuBar p0) { throw new UnsupportedOperationException(); }
  public void setMaximizedBounds(java.awt.Rectangle p0) { throw new UnsupportedOperationException(); }
  public int getExtendedState() { throw new UnsupportedOperationException(); }
  public void setExtendedState(int p0) { throw new UnsupportedOperationException(); }
  public java.awt.Image getIconImage() { throw new UnsupportedOperationException(); }
  public java.awt.MenuBar getMenuBar() { throw new UnsupportedOperationException(); }
  public java.awt.Rectangle getMaximizedBounds() { throw new UnsupportedOperationException(); }
  public int getCursorType() { throw new UnsupportedOperationException(); }
  public static java.awt.Frame[] getFrames() { return new java.awt.Frame[0]; }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void removeNotify() { throw new UnsupportedOperationException(); }
  public void setBackground(java.awt.Color p0) { throw new UnsupportedOperationException(); }
  public void setCursor(int p0) { throw new UnsupportedOperationException(); }
  public void setOpacity(float p0) { throw new UnsupportedOperationException(); }
  public void setIconImage(java.awt.Image p0) { throw new UnsupportedOperationException(); }
  public void setShape(java.awt.Shape p0) { throw new UnsupportedOperationException(); }
  public void setTitle(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void setResizable(boolean p0) { throw new UnsupportedOperationException(); }
  public boolean isUndecorated() { throw new UnsupportedOperationException(); }
  public java.lang.String getTitle() { throw new UnsupportedOperationException(); }
  public boolean isResizable() { throw new UnsupportedOperationException(); }
  public void setUndecorated(boolean p0) { throw new UnsupportedOperationException(); }
}
