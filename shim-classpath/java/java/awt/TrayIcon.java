/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class TrayIcon {
  public TrayIcon(java.awt.Image p0, java.lang.String p1, java.awt.PopupMenu p2) {
    throw new java.awt.HeadlessException();
  }
  public TrayIcon(java.awt.Image p0, java.lang.String p1) {
    throw new java.awt.HeadlessException();
  }
  public TrayIcon(java.awt.Image p0) {
    throw new java.awt.HeadlessException();
  }
  public static boolean isSupported() { return false; }
  public java.awt.Dimension getSize() { throw new UnsupportedOperationException(); }
  public void setImage(java.awt.Image p0) { throw new UnsupportedOperationException(); }
  public void setToolTip(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void setPopupMenu(java.awt.PopupMenu p0) { throw new UnsupportedOperationException(); }
  public void displayMessage(java.lang.String p0, java.lang.String p1, java.awt.TrayIcon.MessageType p2) { throw new UnsupportedOperationException(); }
  public java.awt.PopupMenu getPopupMenu() { throw new UnsupportedOperationException(); }
  public java.lang.String getToolTip() { throw new UnsupportedOperationException(); }
  public void setImageAutoSize(boolean p0) { throw new UnsupportedOperationException(); }
  public boolean isImageAutoSize() { throw new UnsupportedOperationException(); }
  public java.awt.Image getImage() { throw new UnsupportedOperationException(); }
  public void addMouseListener(java.awt.event.MouseListener p0) { throw new UnsupportedOperationException(); }
  public void addMouseMotionListener(java.awt.event.MouseMotionListener p0) { throw new UnsupportedOperationException(); }
  public void removeMouseListener(java.awt.event.MouseListener p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.MouseListener[] getMouseListeners() { throw new UnsupportedOperationException(); }
  public void removeMouseMotionListener(java.awt.event.MouseMotionListener p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.MouseMotionListener[] getMouseMotionListeners() { throw new UnsupportedOperationException(); }
  public java.lang.String getActionCommand() { throw new UnsupportedOperationException(); }
  public void addActionListener(java.awt.event.ActionListener p0) { throw new UnsupportedOperationException(); }
  public void setActionCommand(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void removeActionListener(java.awt.event.ActionListener p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.ActionListener[] getActionListeners() { throw new UnsupportedOperationException(); }

  public enum MessageType {
    ERROR, WARNING, INFO, NONE;
  }
}
