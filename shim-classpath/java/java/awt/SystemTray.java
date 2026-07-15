/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class SystemTray {
  SystemTray() {
  }
  public void remove(java.awt.TrayIcon p0) { throw new UnsupportedOperationException(); }
  public void add(java.awt.TrayIcon p0) throws java.awt.AWTException { throw new UnsupportedOperationException(); }
  public static boolean isSupported() { return false; }
  public java.awt.TrayIcon[] getTrayIcons() { throw new UnsupportedOperationException(); }
  public java.awt.Dimension getTrayIconSize() { throw new UnsupportedOperationException(); }
  public static java.awt.SystemTray getSystemTray() { throw new java.awt.HeadlessException(); }
  public void addPropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1) { throw new UnsupportedOperationException(); }
  public java.beans.PropertyChangeListener[] getPropertyChangeListeners(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void removePropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1) { throw new UnsupportedOperationException(); }
}
