/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.xpath;

public interface XPathNamespace extends org.w3c.dom.Node {
  short XPATH_NAMESPACE_NODE = (short) 13;

  org.w3c.dom.Element getOwnerElement();
}
