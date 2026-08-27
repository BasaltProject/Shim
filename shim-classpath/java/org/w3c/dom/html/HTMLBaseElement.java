/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLBaseElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getHref();
  java.lang.String getTarget();
  void setHref(java.lang.String string);
  void setTarget(java.lang.String string);
}
