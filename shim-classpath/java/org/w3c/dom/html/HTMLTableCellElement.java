/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLTableCellElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAbbr();
  java.lang.String getAlign();
  java.lang.String getAxis();
  java.lang.String getBgColor();
  int getCellIndex();
  java.lang.String getCh();
  java.lang.String getChOff();
  int getColSpan();
  java.lang.String getHeaders();
  java.lang.String getHeight();
  boolean getNoWrap();
  int getRowSpan();
  java.lang.String getScope();
  java.lang.String getVAlign();
  java.lang.String getWidth();
  void setAbbr(java.lang.String string);
  void setAlign(java.lang.String string);
  void setAxis(java.lang.String string);
  void setBgColor(java.lang.String string);
  void setCh(java.lang.String string);
  void setChOff(java.lang.String string);
  void setColSpan(int i);
  void setHeaders(java.lang.String string);
  void setHeight(java.lang.String string);
  void setNoWrap(boolean flag);
  void setRowSpan(int i);
  void setScope(java.lang.String string);
  void setVAlign(java.lang.String string);
  void setWidth(java.lang.String string);
}
