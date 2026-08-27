/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLScriptElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getCharset();
  boolean getDefer();
  java.lang.String getEvent();
  java.lang.String getHtmlFor();
  java.lang.String getSrc();
  java.lang.String getText();
  java.lang.String getType();
  void setCharset(java.lang.String string);
  void setDefer(boolean flag);
  void setEvent(java.lang.String string);
  void setHtmlFor(java.lang.String string);
  void setSrc(java.lang.String string);
  void setText(java.lang.String string);
  void setType(java.lang.String string);
}
