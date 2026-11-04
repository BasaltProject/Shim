/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLAnchorElement extends org.w3c.dom.html.HTMLElement {
  void blur();
  void focus();
  java.lang.String getAccessKey();
  java.lang.String getCharset();
  java.lang.String getCoords();
  java.lang.String getHref();
  java.lang.String getHreflang();
  java.lang.String getName();
  java.lang.String getRel();
  java.lang.String getRev();
  java.lang.String getShape();
  int getTabIndex();
  java.lang.String getTarget();
  java.lang.String getType();
  void setAccessKey(java.lang.String string);
  void setCharset(java.lang.String string);
  void setCoords(java.lang.String string);
  void setHref(java.lang.String string);
  void setHreflang(java.lang.String string);
  void setName(java.lang.String string);
  void setRel(java.lang.String string);
  void setRev(java.lang.String string);
  void setShape(java.lang.String string);
  void setTabIndex(int i);
  void setTarget(java.lang.String string);
  void setType(java.lang.String string);
}
