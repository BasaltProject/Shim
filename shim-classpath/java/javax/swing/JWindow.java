/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public class JWindow extends java.awt.Window implements javax.accessibility.Accessible, javax.swing.RootPaneContainer, javax.swing.TransferHandler.HasGetTransferHandler {
  private java.awt.Container bp_ContentPane;
  private javax.swing.JLayeredPane bp_LayeredPane;
  private java.awt.Component bp_GlassPane;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  private java.awt.Graphics bp_Graphics;
  private javax.swing.JRootPane bp_RootPane;
  private javax.swing.TransferHandler bp_TransferHandler;
  protected javax.swing.JRootPane rootPane;
  protected boolean rootPaneCheckingEnabled;
  protected javax.accessibility.AccessibleContext accessibleContext;
  public JWindow(java.awt.Window p0, java.awt.GraphicsConfiguration p1) {
    super(p0, p1);
    throw new java.awt.HeadlessException();
  }
  public JWindow(java.awt.Window p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public JWindow(java.awt.Frame p0) {
    super(p0);
    throw new java.awt.HeadlessException();
  }
  public JWindow(java.awt.GraphicsConfiguration p0) {
    super((java.awt.Frame) null);
    throw new java.awt.HeadlessException();
  }
  public JWindow() {
    super((java.awt.Frame) null);
    throw new java.awt.HeadlessException();
  }
  public void remove(java.awt.Component p0) { throw new UnsupportedOperationException(); }
  public void update(java.awt.Graphics p0) { throw new UnsupportedOperationException(); }
  protected java.lang.String paramString() { throw new UnsupportedOperationException(); }
  protected void windowInit() { throw new UnsupportedOperationException(); }
  protected javax.swing.JRootPane createRootPane() { throw new UnsupportedOperationException(); }
  protected void setRootPane(javax.swing.JRootPane p0) { throw new UnsupportedOperationException(); }
  protected void setRootPaneCheckingEnabled(boolean p0) { throw new UnsupportedOperationException(); }
  protected boolean isRootPaneCheckingEnabled() { throw new UnsupportedOperationException(); }
  public void setContentPane(java.awt.Container p0) { bp_ContentPane = p0; }
  public void setLayeredPane(javax.swing.JLayeredPane p0) { bp_LayeredPane = p0; }
  public java.awt.Component getGlassPane() { return bp_GlassPane; }
  public void setGlassPane(java.awt.Component p0) { bp_GlassPane = p0; }
  public javax.accessibility.AccessibleContext getAccessibleContext() { return bp_AccessibleContext; }
  public void setLayout(java.awt.LayoutManager p0) { throw new UnsupportedOperationException(); }
  protected void addImpl(java.awt.Component p0, java.lang.Object p1, int p2) { throw new UnsupportedOperationException(); }
  public void repaint(long p0, int p1, int p2, int p3, int p4) { throw new UnsupportedOperationException(); }
  public java.awt.Graphics getGraphics() { return bp_Graphics; }
  public javax.swing.JRootPane getRootPane() { return bp_RootPane; }
  public javax.swing.JLayeredPane getLayeredPane() { return bp_LayeredPane; }
  public java.awt.Container getContentPane() { return bp_ContentPane; }
  public void setTransferHandler(javax.swing.TransferHandler p0) { bp_TransferHandler = p0; }
  public javax.swing.TransferHandler getTransferHandler() { return bp_TransferHandler; }
}
