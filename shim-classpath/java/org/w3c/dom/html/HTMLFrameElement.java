/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLFrameElement extends org.w3c.dom.html.HTMLElement {
  org.w3c.dom.Document getContentDocument();
  java.lang.String getFrameBorder();
  java.lang.String getLongDesc();
  java.lang.String getMarginHeight();
  java.lang.String getMarginWidth();
  java.lang.String getName();
  boolean getNoResize();
  java.lang.String getScrolling();
  java.lang.String getSrc();
  void setFrameBorder(java.lang.String string);
  void setLongDesc(java.lang.String string);
  void setMarginHeight(java.lang.String string);
  void setMarginWidth(java.lang.String string);
  void setName(java.lang.String string);
  void setNoResize(boolean flag);
  void setScrolling(java.lang.String string);
  void setSrc(java.lang.String string);
}
