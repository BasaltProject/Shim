/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLIsIndexElement extends org.w3c.dom.html.HTMLElement {
  org.w3c.dom.html.HTMLFormElement getForm();
  java.lang.String getPrompt();
  void setPrompt(java.lang.String string);
}
