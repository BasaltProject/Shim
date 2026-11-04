/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLParamElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getName();
  java.lang.String getType();
  java.lang.String getValue();
  java.lang.String getValueType();
  void setName(java.lang.String string);
  void setType(java.lang.String string);
  void setValue(java.lang.String string);
  void setValueType(java.lang.String string);
}
