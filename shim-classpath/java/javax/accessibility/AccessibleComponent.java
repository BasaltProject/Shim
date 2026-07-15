/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleComponent {
  boolean contains(java.awt.Point p0);
  java.awt.Rectangle getBounds();
  java.awt.Point getLocation();
  boolean isEnabled();
  java.awt.Dimension getSize();
  void setSize(java.awt.Dimension p0);
  boolean isVisible();
  java.awt.Font getFont();
  void setFont(java.awt.Font p0);
  boolean isShowing();
  void requestFocus();
  void setVisible(boolean p0);
  void setBounds(java.awt.Rectangle p0);
  javax.accessibility.Accessible getAccessibleAt(java.awt.Point p0);
  void setLocation(java.awt.Point p0);
  java.awt.Point getLocationOnScreen();
  void setEnabled(boolean p0);
  java.awt.Color getForeground();
  void setForeground(java.awt.Color p0);
  java.awt.Color getBackground();
  void setBackground(java.awt.Color p0);
  boolean isFocusTraversable();
  void addFocusListener(java.awt.event.FocusListener p0);
  java.awt.FontMetrics getFontMetrics(java.awt.Font p0);
  void setCursor(java.awt.Cursor p0);
  java.awt.Cursor getCursor();
  void removeFocusListener(java.awt.event.FocusListener p0);
}
