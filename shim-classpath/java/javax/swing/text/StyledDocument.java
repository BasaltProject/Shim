/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface StyledDocument extends javax.swing.text.Document {
  javax.swing.text.Style addStyle(java.lang.String p0, javax.swing.text.Style p1);
  void removeStyle(java.lang.String p0);
  void setLogicalStyle(int p0, javax.swing.text.Style p1);
  javax.swing.text.Style getLogicalStyle(int p0);
  void setParagraphAttributes(int p0, int p1, javax.swing.text.AttributeSet p2, boolean p3);
  javax.swing.text.Element getParagraphElement(int p0);
  void setCharacterAttributes(int p0, int p1, javax.swing.text.AttributeSet p2, boolean p3);
  javax.swing.text.Element getCharacterElement(int p0);
  java.awt.Font getFont(javax.swing.text.AttributeSet p0);
  java.awt.Color getForeground(javax.swing.text.AttributeSet p0);
  java.awt.Color getBackground(javax.swing.text.AttributeSet p0);
  javax.swing.text.Style getStyle(java.lang.String p0);
}
