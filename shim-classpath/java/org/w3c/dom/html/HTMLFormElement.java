/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLFormElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAcceptCharset();
  java.lang.String getAction();
  org.w3c.dom.html.HTMLCollection getElements();
  java.lang.String getEnctype();
  int getLength();
  java.lang.String getMethod();
  java.lang.String getName();
  java.lang.String getTarget();
  void reset();
  void setAcceptCharset(java.lang.String string);
  void setAction(java.lang.String string);
  void setEnctype(java.lang.String string);
  void setMethod(java.lang.String string);
  void setName(java.lang.String string);
  void setTarget(java.lang.String string);
  void submit();
}
