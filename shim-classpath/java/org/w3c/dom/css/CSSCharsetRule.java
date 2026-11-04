/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSCharsetRule extends org.w3c.dom.css.CSSRule {
  java.lang.String getEncoding();
  void setEncoding(java.lang.String string) throws org.w3c.dom.DOMException;
}
