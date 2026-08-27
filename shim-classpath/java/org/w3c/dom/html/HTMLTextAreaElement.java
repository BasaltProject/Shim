/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLTextAreaElement extends org.w3c.dom.html.HTMLElement {
  void blur();
  void focus();
  java.lang.String getAccessKey();
  int getCols();
  java.lang.String getDefaultValue();
  boolean getDisabled();
  org.w3c.dom.html.HTMLFormElement getForm();
  java.lang.String getName();
  boolean getReadOnly();
  int getRows();
  int getTabIndex();
  java.lang.String getType();
  java.lang.String getValue();
  void select();
  void setAccessKey(java.lang.String string);
  void setCols(int i);
  void setDefaultValue(java.lang.String string);
  void setDisabled(boolean flag);
  void setName(java.lang.String string);
  void setReadOnly(boolean flag);
  void setRows(int i);
  void setTabIndex(int i);
  void setValue(java.lang.String string);
}
