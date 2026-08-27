/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLObjectElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAlign();
  java.lang.String getArchive();
  java.lang.String getBorder();
  java.lang.String getCode();
  java.lang.String getCodeBase();
  java.lang.String getCodeType();
  org.w3c.dom.Document getContentDocument();
  java.lang.String getData();
  boolean getDeclare();
  org.w3c.dom.html.HTMLFormElement getForm();
  java.lang.String getHeight();
  java.lang.String getHspace();
  java.lang.String getName();
  java.lang.String getStandby();
  int getTabIndex();
  java.lang.String getType();
  java.lang.String getUseMap();
  java.lang.String getVspace();
  java.lang.String getWidth();
  void setAlign(java.lang.String string);
  void setArchive(java.lang.String string);
  void setBorder(java.lang.String string);
  void setCode(java.lang.String string);
  void setCodeBase(java.lang.String string);
  void setCodeType(java.lang.String string);
  void setData(java.lang.String string);
  void setDeclare(boolean flag);
  void setHeight(java.lang.String string);
  void setHspace(java.lang.String string);
  void setName(java.lang.String string);
  void setStandby(java.lang.String string);
  void setTabIndex(int i);
  void setType(java.lang.String string);
  void setUseMap(java.lang.String string);
  void setVspace(java.lang.String string);
  void setWidth(java.lang.String string);
}
