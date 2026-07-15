/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class MenuBar extends java.awt.MenuComponent implements java.awt.MenuContainer, javax.accessibility.Accessible {
  public MenuBar() throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public void remove(int p0) { throw new UnsupportedOperationException(); }
  public void remove(java.awt.MenuComponent p0) { throw new UnsupportedOperationException(); }
  public java.awt.Menu add(java.awt.Menu p0) { throw new UnsupportedOperationException(); }
  public int countMenus() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void removeNotify() { throw new UnsupportedOperationException(); }
  public java.awt.MenuItem getShortcutMenuItem(java.awt.MenuShortcut p0) { throw new UnsupportedOperationException(); }
  public java.util.Enumeration<java.awt.MenuShortcut> shortcuts() { throw new UnsupportedOperationException(); }
  public void deleteShortcut(java.awt.MenuShortcut p0) { throw new UnsupportedOperationException(); }
  public java.awt.Menu getMenu(int p0) { throw new UnsupportedOperationException(); }
  public int getMenuCount() { throw new UnsupportedOperationException(); }
  public void setHelpMenu(java.awt.Menu p0) { throw new UnsupportedOperationException(); }
  public java.awt.Menu getHelpMenu() { throw new UnsupportedOperationException(); }
}
