/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLOptionElement extends org.w3c.dom.html.HTMLElement {
  boolean getDefaultSelected();
  boolean getDisabled();
  org.w3c.dom.html.HTMLFormElement getForm();
  int getIndex();
  java.lang.String getLabel();
  boolean getSelected();
  java.lang.String getText();
  java.lang.String getValue();
  void setDefaultSelected(boolean flag);
  void setDisabled(boolean flag);
  void setLabel(java.lang.String string);
  void setSelected(boolean flag);
  void setValue(java.lang.String string);
}
