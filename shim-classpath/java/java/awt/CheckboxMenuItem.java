/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class CheckboxMenuItem extends java.awt.MenuItem implements java.awt.ItemSelectable, javax.accessibility.Accessible {
  public CheckboxMenuItem(java.lang.String p0, boolean p1) throws java.awt.HeadlessException {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public CheckboxMenuItem(java.lang.String p0) throws java.awt.HeadlessException {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public CheckboxMenuItem() throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public boolean getState() { throw new UnsupportedOperationException(); }
  public void setState(boolean p0) { throw new UnsupportedOperationException(); }
  public java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) { throw new UnsupportedOperationException(); }
  protected void processEvent(java.awt.AWTEvent p0) { throw new UnsupportedOperationException(); }
  protected void processItemEvent(java.awt.event.ItemEvent p0) { throw new UnsupportedOperationException(); }
  public void addItemListener(java.awt.event.ItemListener p0) { throw new UnsupportedOperationException(); }
  public java.lang.Object[] getSelectedObjects() { throw new UnsupportedOperationException(); }
  public void removeItemListener(java.awt.event.ItemListener p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.ItemListener[] getItemListeners() { throw new UnsupportedOperationException(); }
}
