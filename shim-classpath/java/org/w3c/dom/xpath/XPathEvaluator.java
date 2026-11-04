/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.xpath;

public interface XPathEvaluator {
  org.w3c.dom.xpath.XPathExpression createExpression(java.lang.String string, org.w3c.dom.xpath.XPathNSResolver xPathNSResolver) throws org.w3c.dom.xpath.XPathException, org.w3c.dom.DOMException;
  org.w3c.dom.xpath.XPathNSResolver createNSResolver(org.w3c.dom.Node node);
  java.lang.Object evaluate(java.lang.String string, org.w3c.dom.Node node, org.w3c.dom.xpath.XPathNSResolver xPathNSResolver, short s, java.lang.Object object) throws org.w3c.dom.xpath.XPathException, org.w3c.dom.DOMException;
}
