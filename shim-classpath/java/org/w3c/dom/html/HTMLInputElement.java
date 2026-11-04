/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLInputElement extends org.w3c.dom.html.HTMLElement {
  void blur();
  void click();
  void focus();
  java.lang.String getAccept();
  java.lang.String getAccessKey();
  java.lang.String getAlign();
  java.lang.String getAlt();
  boolean getChecked();
  boolean getDefaultChecked();
  java.lang.String getDefaultValue();
  boolean getDisabled();
  org.w3c.dom.html.HTMLFormElement getForm();
  int getMaxLength();
  java.lang.String getName();
  boolean getReadOnly();
  java.lang.String getSize();
  java.lang.String getSrc();
  int getTabIndex();
  java.lang.String getType();
  java.lang.String getUseMap();
  java.lang.String getValue();
  void select();
  void setAccept(java.lang.String string);
  void setAccessKey(java.lang.String string);
  void setAlign(java.lang.String string);
  void setAlt(java.lang.String string);
  void setChecked(boolean flag);
  void setDefaultChecked(boolean flag);
  void setDefaultValue(java.lang.String string);
  void setDisabled(boolean flag);
  void setMaxLength(int i);
  void setName(java.lang.String string);
  void setReadOnly(boolean flag);
  void setSize(java.lang.String string);
  void setSrc(java.lang.String string);
  void setTabIndex(int i);
  void setUseMap(java.lang.String string);
  void setValue(java.lang.String string);
}
