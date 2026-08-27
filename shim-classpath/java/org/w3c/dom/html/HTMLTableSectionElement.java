/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLTableSectionElement extends org.w3c.dom.html.HTMLElement {
  void deleteRow(int i) throws org.w3c.dom.DOMException;
  java.lang.String getAlign();
  java.lang.String getCh();
  java.lang.String getChOff();
  org.w3c.dom.html.HTMLCollection getRows();
  java.lang.String getVAlign();
  org.w3c.dom.html.HTMLElement insertRow(int i) throws org.w3c.dom.DOMException;
  void setAlign(java.lang.String string);
  void setCh(java.lang.String string);
  void setChOff(java.lang.String string);
  void setVAlign(java.lang.String string);
}
