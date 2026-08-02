/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface Caret {
  void removeChangeListener(javax.swing.event.ChangeListener p0);
  void addChangeListener(javax.swing.event.ChangeListener p0);
  void deinstall(javax.swing.text.JTextComponent p0);
  void install(javax.swing.text.JTextComponent p0);
  void setDot(int p0);
  void moveDot(int p0);
  int getMark();
  int getDot();
  int getBlinkRate();
  void setBlinkRate(int p0);
  boolean isSelectionVisible();
  void setSelectionVisible(boolean p0);
  void setMagicCaretPosition(java.awt.Point p0);
  java.awt.Point getMagicCaretPosition();
  boolean isVisible();
  void paint(java.awt.Graphics p0);
  void setVisible(boolean p0);
}
