/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

public interface PropertyEditor {
  java.lang.Object getValue();
  void setValue(java.lang.Object p0);
  boolean isPaintable();
  void paintValue(java.awt.Graphics p0, java.awt.Rectangle p1);
  java.lang.String getJavaInitializationString();
  java.lang.String getAsText();
  void setAsText(java.lang.String p0) throws java.lang.IllegalArgumentException;
  java.lang.String[] getTags();
  java.awt.Component getCustomEditor();
  boolean supportsCustomEditor();
  void addPropertyChangeListener(java.beans.PropertyChangeListener p0);
  void removePropertyChangeListener(java.beans.PropertyChangeListener p0);
}
