/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLOptGroupElement extends org.w3c.dom.html.HTMLElement {
  boolean getDisabled();
  java.lang.String getLabel();
  void setDisabled(boolean flag);
  void setLabel(java.lang.String string);
}
