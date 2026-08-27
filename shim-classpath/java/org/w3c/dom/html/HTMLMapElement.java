/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLMapElement extends org.w3c.dom.html.HTMLElement {
  org.w3c.dom.html.HTMLCollection getAreas();
  java.lang.String getName();
  void setName(java.lang.String string);
}
