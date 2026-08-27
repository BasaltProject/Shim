/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLTableElement extends org.w3c.dom.html.HTMLElement {
  org.w3c.dom.html.HTMLElement createCaption();
  org.w3c.dom.html.HTMLElement createTFoot();
  org.w3c.dom.html.HTMLElement createTHead();
  void deleteCaption();
  void deleteRow(int i) throws org.w3c.dom.DOMException;
  void deleteTFoot();
  void deleteTHead();
  java.lang.String getAlign();
  java.lang.String getBgColor();
  java.lang.String getBorder();
  org.w3c.dom.html.HTMLTableCaptionElement getCaption();
  java.lang.String getCellPadding();
  java.lang.String getCellSpacing();
  java.lang.String getFrame();
  org.w3c.dom.html.HTMLCollection getRows();
  java.lang.String getRules();
  java.lang.String getSummary();
  org.w3c.dom.html.HTMLCollection getTBodies();
  org.w3c.dom.html.HTMLTableSectionElement getTFoot();
  org.w3c.dom.html.HTMLTableSectionElement getTHead();
  java.lang.String getWidth();
  org.w3c.dom.html.HTMLElement insertRow(int i) throws org.w3c.dom.DOMException;
  void setAlign(java.lang.String string);
  void setBgColor(java.lang.String string);
  void setBorder(java.lang.String string);
  void setCaption(org.w3c.dom.html.HTMLTableCaptionElement hTMLTableCaptionElement);
  void setCellPadding(java.lang.String string);
  void setCellSpacing(java.lang.String string);
  void setFrame(java.lang.String string);
  void setRules(java.lang.String string);
  void setSummary(java.lang.String string);
  void setTFoot(org.w3c.dom.html.HTMLTableSectionElement hTMLTableSectionElement);
  void setTHead(org.w3c.dom.html.HTMLTableSectionElement hTMLTableSectionElement);
  void setWidth(java.lang.String string);
}
