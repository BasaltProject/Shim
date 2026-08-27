/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLTableRowElement extends org.w3c.dom.html.HTMLElement {
  void deleteCell(int i) throws org.w3c.dom.DOMException;
  java.lang.String getAlign();
  java.lang.String getBgColor();
  org.w3c.dom.html.HTMLCollection getCells();
  java.lang.String getCh();
  java.lang.String getChOff();
  int getRowIndex();
  int getSectionRowIndex();
  java.lang.String getVAlign();
  org.w3c.dom.html.HTMLElement insertCell(int i) throws org.w3c.dom.DOMException;
  void setAlign(java.lang.String string);
  void setBgColor(java.lang.String string);
  void setCh(java.lang.String string);
  void setChOff(java.lang.String string);
  void setVAlign(java.lang.String string);
}
