/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface DOMImplementationCSS extends org.w3c.dom.DOMImplementation {
  org.w3c.dom.css.CSSStyleSheet createCSSStyleSheet(java.lang.String string, java.lang.String string2) throws org.w3c.dom.DOMException;
}
