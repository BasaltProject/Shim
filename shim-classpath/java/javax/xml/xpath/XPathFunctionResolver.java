/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

public interface XPathFunctionResolver {
  XPathFunction resolveFunction(javax.xml.namespace.QName p0, int p1);
}
