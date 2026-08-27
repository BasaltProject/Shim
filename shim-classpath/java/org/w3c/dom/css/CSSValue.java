/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSValue {
  short CSS_INHERIT = (short) 0;
  short CSS_PRIMITIVE_VALUE = (short) 1;
  short CSS_VALUE_LIST = (short) 2;
  short CSS_CUSTOM = (short) 3;

  java.lang.String getCssText();
  short getCssValueType();
  void setCssText(java.lang.String string) throws org.w3c.dom.DOMException;
}
