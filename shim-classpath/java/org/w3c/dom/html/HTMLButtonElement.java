/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLButtonElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAccessKey();
  boolean getDisabled();
  org.w3c.dom.html.HTMLFormElement getForm();
  java.lang.String getName();
  int getTabIndex();
  java.lang.String getType();
  java.lang.String getValue();
  void setAccessKey(java.lang.String string);
  void setDisabled(boolean flag);
  void setName(java.lang.String string);
  void setTabIndex(int i);
  void setValue(java.lang.String string);
}
