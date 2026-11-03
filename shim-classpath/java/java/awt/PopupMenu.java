/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class PopupMenu extends java.awt.Menu {
  public PopupMenu() throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public PopupMenu(java.lang.String p0) throws java.awt.HeadlessException {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public java.awt.MenuContainer getParent() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void show(java.awt.Component p0, int p1, int p2) { throw new UnsupportedOperationException(); }
}
