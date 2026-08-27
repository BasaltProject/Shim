/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLSelectElement extends org.w3c.dom.html.HTMLElement {
  void add(org.w3c.dom.html.HTMLElement hTMLElement, org.w3c.dom.html.HTMLElement hTMLElement2) throws org.w3c.dom.DOMException;
  void blur();
  void focus();
  boolean getDisabled();
  org.w3c.dom.html.HTMLFormElement getForm();
  int getLength();
  boolean getMultiple();
  java.lang.String getName();
  org.w3c.dom.html.HTMLCollection getOptions();
  int getSelectedIndex();
  int getSize();
  int getTabIndex();
  java.lang.String getType();
  java.lang.String getValue();
  void remove(int i);
  void setDisabled(boolean flag);
  void setMultiple(boolean flag);
  void setName(java.lang.String string);
  void setSelectedIndex(int i);
  void setSize(int i);
  void setTabIndex(int i);
  void setValue(java.lang.String string);
}
