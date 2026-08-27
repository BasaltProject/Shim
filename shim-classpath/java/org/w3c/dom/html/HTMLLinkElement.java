/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLLinkElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getCharset();
  boolean getDisabled();
  java.lang.String getHref();
  java.lang.String getHreflang();
  java.lang.String getMedia();
  java.lang.String getRel();
  java.lang.String getRev();
  java.lang.String getTarget();
  java.lang.String getType();
  void setCharset(java.lang.String string);
  void setDisabled(boolean flag);
  void setHref(java.lang.String string);
  void setHreflang(java.lang.String string);
  void setMedia(java.lang.String string);
  void setRel(java.lang.String string);
  void setRev(java.lang.String string);
  void setTarget(java.lang.String string);
  void setType(java.lang.String string);
}
