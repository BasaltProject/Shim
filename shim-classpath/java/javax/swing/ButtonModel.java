/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface ButtonModel extends java.awt.ItemSelectable {
  boolean isEnabled();
  int getMnemonic();
  void setArmed(boolean p0);
  void setPressed(boolean p0);
  boolean isPressed();
  void setRollover(boolean p0);
  void setMnemonic(int p0);
  default javax.swing.ButtonGroup getGroup() { return null; }
  void removeChangeListener(javax.swing.event.ChangeListener p0);
  void addChangeListener(javax.swing.event.ChangeListener p0);
  boolean isRollover();
  boolean isArmed();
  void setGroup(javax.swing.ButtonGroup p0);
  void setEnabled(boolean p0);
  java.lang.String getActionCommand();
  void addActionListener(java.awt.event.ActionListener p0);
  void setActionCommand(java.lang.String p0);
  void removeActionListener(java.awt.event.ActionListener p0);
  void addItemListener(java.awt.event.ItemListener p0);
  void removeItemListener(java.awt.event.ItemListener p0);
  boolean isSelected();
  void setSelected(boolean p0);
}
