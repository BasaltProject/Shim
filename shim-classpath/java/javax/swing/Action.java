/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface Action extends java.awt.event.ActionListener {
  java.lang.String DEFAULT = "Default";
  java.lang.String NAME = "Name";
  java.lang.String SHORT_DESCRIPTION = "ShortDescription";
  java.lang.String LONG_DESCRIPTION = "LongDescription";
  java.lang.String SMALL_ICON = "SmallIcon";
  java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey";
  java.lang.String ACCELERATOR_KEY = "AcceleratorKey";
  java.lang.String MNEMONIC_KEY = "MnemonicKey";
  java.lang.String SELECTED_KEY = "SwingSelectedKey";
  java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey";
  java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey";
  java.lang.Object getValue(java.lang.String p0);
  boolean isEnabled();
  default boolean accept(java.lang.Object sender) { return isEnabled(); }
  void putValue(java.lang.String p0, java.lang.Object p1);
  void addPropertyChangeListener(java.beans.PropertyChangeListener p0);
  void setEnabled(boolean p0);
  void removePropertyChangeListener(java.beans.PropertyChangeListener p0);
}
