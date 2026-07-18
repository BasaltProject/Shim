/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class TextUI extends javax.swing.plaf.ComponentUI {
  protected TextUI() {
  }
  public abstract javax.swing.text.EditorKit getEditorKit(javax.swing.text.JTextComponent p0);
  public abstract int viewToModel(javax.swing.text.JTextComponent p0, java.awt.Point p1);
  public abstract int viewToModel(javax.swing.text.JTextComponent p0, java.awt.Point p1, javax.swing.text.Position.Bias[] p2);
  public abstract java.awt.Rectangle modelToView(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2) throws javax.swing.text.BadLocationException;
  public abstract java.awt.Rectangle modelToView(javax.swing.text.JTextComponent p0, int p1) throws javax.swing.text.BadLocationException;
  public java.awt.geom.Rectangle2D modelToView2D(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2) throws javax.swing.text.BadLocationException { throw new UnsupportedOperationException(); }
  public int viewToModel2D(javax.swing.text.JTextComponent p0, java.awt.geom.Point2D p1, javax.swing.text.Position.Bias[] p2) { throw new UnsupportedOperationException(); }
  public abstract int getNextVisualPositionFrom(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2, int p3, javax.swing.text.Position.Bias[] p4) throws javax.swing.text.BadLocationException;
  public abstract void damageRange(javax.swing.text.JTextComponent p0, int p1, int p2);
  public abstract void damageRange(javax.swing.text.JTextComponent p0, int p1, int p2, javax.swing.text.Position.Bias p3, javax.swing.text.Position.Bias p4);
  public abstract javax.swing.text.View getRootView(javax.swing.text.JTextComponent p0);
  public java.lang.String getToolTipText2D(javax.swing.text.JTextComponent p0, java.awt.geom.Point2D p1) { throw new UnsupportedOperationException(); }
  public java.lang.String getToolTipText(javax.swing.text.JTextComponent p0, java.awt.Point p1) { throw new UnsupportedOperationException(); }
}
