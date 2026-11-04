/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLOListElement extends org.w3c.dom.html.HTMLElement {
  boolean getCompact();
  int getStart();
  java.lang.String getType();
  void setCompact(boolean flag);
  void setStart(int i);
  void setType(java.lang.String string);
}
