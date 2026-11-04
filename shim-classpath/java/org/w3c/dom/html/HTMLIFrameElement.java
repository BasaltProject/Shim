/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLIFrameElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAlign();
  org.w3c.dom.Document getContentDocument();
  java.lang.String getFrameBorder();
  java.lang.String getHeight();
  java.lang.String getLongDesc();
  java.lang.String getMarginHeight();
  java.lang.String getMarginWidth();
  java.lang.String getName();
  java.lang.String getScrolling();
  java.lang.String getSrc();
  java.lang.String getWidth();
  void setAlign(java.lang.String string);
  void setFrameBorder(java.lang.String string);
  void setHeight(java.lang.String string);
  void setLongDesc(java.lang.String string);
  void setMarginHeight(java.lang.String string);
  void setMarginWidth(java.lang.String string);
  void setName(java.lang.String string);
  void setScrolling(java.lang.String string);
  void setSrc(java.lang.String string);
  void setWidth(java.lang.String string);
}
