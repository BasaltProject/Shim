/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLLabelElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getAccessKey();
  org.w3c.dom.html.HTMLFormElement getForm();
  java.lang.String getHtmlFor();
  void setAccessKey(java.lang.String string);
  void setHtmlFor(java.lang.String string);
}
