/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLAreaElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAccessKey();
  java.lang.String getAlt();
  java.lang.String getCoords();
  java.lang.String getHref();
  boolean getNoHref();
  java.lang.String getShape();
  int getTabIndex();
  java.lang.String getTarget();
  void setAccessKey(java.lang.String string);
  void setAlt(java.lang.String string);
  void setCoords(java.lang.String string);
  void setHref(java.lang.String string);
  void setNoHref(boolean flag);
  void setShape(java.lang.String string);
  void setTabIndex(int i);
  void setTarget(java.lang.String string);
}
