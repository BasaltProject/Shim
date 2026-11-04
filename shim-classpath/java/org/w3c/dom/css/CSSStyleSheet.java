/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSStyleSheet extends org.w3c.dom.stylesheets.StyleSheet {
  void deleteRule(int i) throws org.w3c.dom.DOMException;
  org.w3c.dom.css.CSSRuleList getCssRules();
  org.w3c.dom.css.CSSRule getOwnerRule();
  int insertRule(java.lang.String string, int i) throws org.w3c.dom.DOMException;
}
