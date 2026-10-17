/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

public interface XPathNodes extends Iterable<org.w3c.dom.Node> {
  java.util.Iterator<org.w3c.dom.Node> iterator();

  int size();

  org.w3c.dom.Node get(int p0) throws XPathException;
}
