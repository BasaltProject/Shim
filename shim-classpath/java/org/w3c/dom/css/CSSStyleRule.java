/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSStyleRule extends org.w3c.dom.css.CSSRule {
  java.lang.String getSelectorText();
  org.w3c.dom.css.CSSStyleDeclaration getStyle();
  void setSelectorText(java.lang.String string) throws org.w3c.dom.DOMException;
}
