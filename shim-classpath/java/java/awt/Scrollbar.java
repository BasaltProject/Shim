/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Scrollbar extends java.awt.Component implements java.awt.Adjustable, javax.accessibility.Accessible {
  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  public Scrollbar(int p0) throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public Scrollbar() throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public Scrollbar(int p0, int p1, int p2, int p3, int p4) throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public int getValue() { throw new UnsupportedOperationException(); }
  public void setValue(int p0) { throw new UnsupportedOperationException(); }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  public int getMaximum() { throw new UnsupportedOperationException(); }
  public int getMinimum() { throw new UnsupportedOperationException(); }
  public void setValueIsAdjusting(boolean p0) { throw new UnsupportedOperationException(); }
  public java.awt.event.AdjustmentListener[] getAdjustmentListeners() { throw new UnsupportedOperationException(); }
  public void setValues(int p0, int p1, int p2, int p3) { throw new UnsupportedOperationException(); }
  public int getVisible() { throw new UnsupportedOperationException(); }
  public void setLineIncrement(int p0) { throw new UnsupportedOperationException(); }
  public int getLineIncrement() { throw new UnsupportedOperationException(); }
  public void setPageIncrement(int p0) { throw new UnsupportedOperationException(); }
  public int getPageIncrement() { throw new UnsupportedOperationException(); }
  protected void processAdjustmentEvent(java.awt.event.AdjustmentEvent p0) { throw new UnsupportedOperationException(); }
  public void setOrientation(int p0) { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
  public void addNotify() { throw new UnsupportedOperationException(); }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) { throw new UnsupportedOperationException(); }
  protected void processEvent(java.awt.AWTEvent p0) { throw new UnsupportedOperationException(); }
  public int getOrientation() { throw new UnsupportedOperationException(); }
  public boolean getValueIsAdjusting() { throw new UnsupportedOperationException(); }
  public void setMinimum(int p0) { throw new UnsupportedOperationException(); }
  public void setMaximum(int p0) { throw new UnsupportedOperationException(); }
  public void setUnitIncrement(int p0) { throw new UnsupportedOperationException(); }
  public int getUnitIncrement() { throw new UnsupportedOperationException(); }
  public void setBlockIncrement(int p0) { throw new UnsupportedOperationException(); }
  public int getBlockIncrement() { throw new UnsupportedOperationException(); }
  public void setVisibleAmount(int p0) { throw new UnsupportedOperationException(); }
  public int getVisibleAmount() { throw new UnsupportedOperationException(); }
  public void addAdjustmentListener(java.awt.event.AdjustmentListener p0) { throw new UnsupportedOperationException(); }
  public void removeAdjustmentListener(java.awt.event.AdjustmentListener p0) { throw new UnsupportedOperationException(); }
}
