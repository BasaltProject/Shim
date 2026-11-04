/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.xpath;

public interface XPathExpression {
  java.lang.Object evaluate(org.w3c.dom.Node node, short s, java.lang.Object object) throws org.w3c.dom.xpath.XPathException, org.w3c.dom.DOMException;
}
