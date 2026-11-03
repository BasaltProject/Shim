/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Menu extends java.awt.MenuItem implements java.awt.MenuContainer, javax.accessibility.Accessible {
  public Menu(java.lang.String p0, boolean p1) throws java.awt.HeadlessException {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Menu(java.lang.String p0) throws java.awt.HeadlessException {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public Menu() throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public void remove(int p0) { throw new UnsupportedOperationException(); }
  public void remove(java.awt.MenuComponent p0) { throw new UnsupportedOperationException(); }
  public void insert(java.awt.MenuItem p0, int p1) { throw new UnsupportedOperationException(); }
  public void insert(java.lang.String p0, int p1) { throw new UnsupportedOperationException(); }
  public void add(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public java.awt.MenuItem add(java.awt.MenuItem p0) { throw new UnsupportedOperationException(); }
  public java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public void removeAll() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public void removeNotify() { throw new UnsupportedOperationException(); }
  public int getItemCount() { throw new UnsupportedOperationException(); }
  public java.awt.MenuItem getItem(int p0) { throw new UnsupportedOperationException(); }
  public int countItems() { throw new UnsupportedOperationException(); }
  public void addSeparator() { throw new UnsupportedOperationException(); }
  public boolean isTearOff() { throw new UnsupportedOperationException(); }
  public void insertSeparator(int p0) { throw new UnsupportedOperationException(); }
}
