/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Window extends java.awt.Container implements javax.accessibility.Accessible {
  public Window(java.awt.Frame p0) {
    throw new java.awt.HeadlessException();
  }
  public Window(java.awt.Window p0) {
    throw new java.awt.HeadlessException();
  }
  public Window(java.awt.Window p0, java.awt.GraphicsConfiguration p1) {
    throw new java.awt.HeadlessException();
  }
  public java.awt.Window.Type getType() { throw new UnsupportedOperationException(); }
  public boolean isOpaque() { throw new UnsupportedOperationException(); }
  public boolean isActive() { throw new UnsupportedOperationException(); }
  public java.awt.Window getOwner() { throw new UnsupportedOperationException(); }
  public java.util.Locale getLocale() { throw new UnsupportedOperationException(); }
  public void setSize(int p0, int p1) { throw new UnsupportedOperationException(); }
  public void setSize(java.awt.Dimension p0) { throw new UnsupportedOperationException(); }
  public void dispose() { throw new UnsupportedOperationException(); }
  public java.awt.Shape getShape() { throw new UnsupportedOperationException(); }
  public boolean isValidateRoot() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void removeNotify() { throw new UnsupportedOperationException(); }
  public final boolean isFocusCycleRoot() { throw new UnsupportedOperationException(); }
  public java.awt.Component getFocusOwner() { throw new UnsupportedOperationException(); }
  public boolean isShowing() { throw new UnsupportedOperationException(); }
  public void paint(java.awt.Graphics p0) { throw new UnsupportedOperationException(); }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) { throw new UnsupportedOperationException(); }
  protected void processEvent(java.awt.AWTEvent p0) { throw new UnsupportedOperationException(); }
  public boolean postEvent(java.awt.Event p0) { throw new UnsupportedOperationException(); }
  public java.awt.Component getMostRecentFocusOwner() { throw new UnsupportedOperationException(); }
  public final java.awt.Container getFocusCycleRootAncestor() { throw new UnsupportedOperationException(); }
  public void addPropertyChangeListener(java.beans.PropertyChangeListener p0) { throw new UnsupportedOperationException(); }
  public void addPropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1) { throw new UnsupportedOperationException(); }
  public void setVisible(boolean p0) { throw new UnsupportedOperationException(); }
  public void setBounds(int p0, int p1, int p2, int p3) { throw new UnsupportedOperationException(); }
  public void setBounds(java.awt.Rectangle p0) { throw new UnsupportedOperationException(); }
  public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int p0) { throw new UnsupportedOperationException(); }
  public final void setFocusCycleRoot(boolean p0) { throw new UnsupportedOperationException(); }
  public java.awt.Toolkit getToolkit() { throw new UnsupportedOperationException(); }
  public void setLocation(int p0, int p1) { throw new UnsupportedOperationException(); }
  public void setLocation(java.awt.Point p0) { throw new UnsupportedOperationException(); }
  public java.awt.im.InputContext getInputContext() { throw new UnsupportedOperationException(); }
  public void show() { throw new UnsupportedOperationException(); }
  public void hide() { throw new UnsupportedOperationException(); }
  public java.awt.Color getBackground() { throw new UnsupportedOperationException(); }
  public void setBackground(java.awt.Color p0) { throw new UnsupportedOperationException(); }
  public void reshape(int p0, int p1, int p2, int p3) { throw new UnsupportedOperationException(); }
  public void createBufferStrategy(int p0, java.awt.BufferCapabilities p1) throws java.awt.AWTException { throw new UnsupportedOperationException(); }
  public void createBufferStrategy(int p0) { throw new UnsupportedOperationException(); }
  public final boolean isFocusableWindow() { throw new UnsupportedOperationException(); }
  public void setMinimumSize(java.awt.Dimension p0) { throw new UnsupportedOperationException(); }
  public void setCursor(java.awt.Cursor p0) { throw new UnsupportedOperationException(); }
  public java.awt.image.BufferStrategy getBufferStrategy() { throw new UnsupportedOperationException(); }
  public boolean isAlwaysOnTopSupported() { throw new UnsupportedOperationException(); }
  public void setLocationByPlatform(boolean p0) { throw new UnsupportedOperationException(); }
  public final boolean isAlwaysOnTop() { throw new UnsupportedOperationException(); }
  public final void setAlwaysOnTop(boolean p0) { throw new UnsupportedOperationException(); }
  public void setIconImages(java.util.List<? extends java.awt.Image> p0) { throw new UnsupportedOperationException(); }
  public void toFront() { throw new UnsupportedOperationException(); }
  public void toBack() { throw new UnsupportedOperationException(); }
  public static java.awt.Window[] getWindows() { return new java.awt.Window[0]; }
  public java.awt.Window[] getOwnedWindows() { throw new UnsupportedOperationException(); }
  protected void processWindowEvent(java.awt.event.WindowEvent p0) { throw new UnsupportedOperationException(); }
  protected void processWindowFocusEvent(java.awt.event.WindowEvent p0) { throw new UnsupportedOperationException(); }
  protected void processWindowStateEvent(java.awt.event.WindowEvent p0) { throw new UnsupportedOperationException(); }
  public boolean isFocused() { throw new UnsupportedOperationException(); }
  public boolean getFocusableWindowState() { throw new UnsupportedOperationException(); }
  public void applyResourceBundle(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void applyResourceBundle(java.util.ResourceBundle p0) { throw new UnsupportedOperationException(); }
  public void addWindowListener(java.awt.event.WindowListener p0) { throw new UnsupportedOperationException(); }
  public void addWindowFocusListener(java.awt.event.WindowFocusListener p0) { throw new UnsupportedOperationException(); }
  public void addWindowStateListener(java.awt.event.WindowStateListener p0) { throw new UnsupportedOperationException(); }
  public void setModalExclusionType(java.awt.Dialog.ModalExclusionType p0) { throw new UnsupportedOperationException(); }
  public void setOpacity(float p0) { throw new UnsupportedOperationException(); }
  public java.util.List<java.awt.Image> getIconImages() { throw new UnsupportedOperationException(); }
  public void setIconImage(java.awt.Image p0) { throw new UnsupportedOperationException(); }
  public void pack() { throw new UnsupportedOperationException(); }
  public final java.lang.String getWarningString() { throw new UnsupportedOperationException(); }
  public static java.awt.Window[] getOwnerlessWindows() { return new java.awt.Window[0]; }
  public java.awt.Dialog.ModalExclusionType getModalExclusionType() { throw new UnsupportedOperationException(); }
  public void removeWindowListener(java.awt.event.WindowListener p0) { throw new UnsupportedOperationException(); }
  public void removeWindowStateListener(java.awt.event.WindowStateListener p0) { throw new UnsupportedOperationException(); }
  public void removeWindowFocusListener(java.awt.event.WindowFocusListener p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.WindowListener[] getWindowListeners() { throw new UnsupportedOperationException(); }
  public java.awt.event.WindowFocusListener[] getWindowFocusListeners() { throw new UnsupportedOperationException(); }
  public java.awt.event.WindowStateListener[] getWindowStateListeners() { throw new UnsupportedOperationException(); }
  public void setFocusableWindowState(boolean p0) { throw new UnsupportedOperationException(); }
  public void setAutoRequestFocus(boolean p0) { throw new UnsupportedOperationException(); }
  public boolean isAutoRequestFocus() { throw new UnsupportedOperationException(); }
  public void setType(java.awt.Window.Type p0) { throw new UnsupportedOperationException(); }
  public void setLocationRelativeTo(java.awt.Component p0) { throw new UnsupportedOperationException(); }
  public boolean isLocationByPlatform() { throw new UnsupportedOperationException(); }
  public float getOpacity() { throw new UnsupportedOperationException(); }
  public void setShape(java.awt.Shape p0) { throw new UnsupportedOperationException(); }

  public enum Type {
    NORMAL, UTILITY, POPUP;
  }
}
