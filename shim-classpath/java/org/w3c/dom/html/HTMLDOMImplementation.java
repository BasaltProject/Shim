/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLDOMImplementation extends org.w3c.dom.DOMImplementation {
  org.w3c.dom.html.HTMLDocument createHTMLDocument(java.lang.String string);
}
