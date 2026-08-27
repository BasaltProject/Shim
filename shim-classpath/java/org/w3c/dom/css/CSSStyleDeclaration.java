/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSStyleDeclaration {
  java.lang.String getCssText();
  int getLength();
  org.w3c.dom.css.CSSRule getParentRule();
  org.w3c.dom.css.CSSValue getPropertyCSSValue(java.lang.String string);
  java.lang.String getPropertyPriority(java.lang.String string);
  java.lang.String getPropertyValue(java.lang.String string);
  java.lang.String item(int i);
  java.lang.String removeProperty(java.lang.String string) throws org.w3c.dom.DOMException;
  void setCssText(java.lang.String string) throws org.w3c.dom.DOMException;
  void setProperty(java.lang.String string, java.lang.String string2, java.lang.String string3) throws org.w3c.dom.DOMException;
}
