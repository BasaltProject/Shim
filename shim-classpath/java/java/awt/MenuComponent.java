/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class MenuComponent implements java.io.Serializable {
  public MenuComponent() throws java.awt.HeadlessException {
  }
  public java.lang.String getName() { throw new UnsupportedOperationException(); }
  public java.lang.String toString() { throw new UnsupportedOperationException(); }
  public java.awt.MenuContainer getParent() { throw new UnsupportedOperationException(); }
  public void setName(java.lang.String p0) { throw new UnsupportedOperationException(); }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  protected final java.lang.Object getTreeLock() { throw new UnsupportedOperationException(); }
  public void removeNotify() { throw new UnsupportedOperationException(); }
  public final void dispatchEvent(java.awt.AWTEvent p0) { throw new UnsupportedOperationException(); }
  public java.awt.Font getFont() { throw new UnsupportedOperationException(); }
  public void setFont(java.awt.Font p0) { throw new UnsupportedOperationException(); }
  protected void processEvent(java.awt.AWTEvent p0) { throw new UnsupportedOperationException(); }
  public boolean postEvent(java.awt.Event p0) { throw new UnsupportedOperationException(); }
}
