/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLLIElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getType();
  int getValue();
  void setType(java.lang.String string);
  void setValue(int i);
}
