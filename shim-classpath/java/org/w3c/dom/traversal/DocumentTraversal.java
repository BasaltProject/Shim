/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.traversal;

public interface DocumentTraversal {
  NodeIterator createNodeIterator(org.w3c.dom.Node p0, int p1, NodeFilter p2, boolean p3)
      throws org.w3c.dom.DOMException;

  TreeWalker createTreeWalker(org.w3c.dom.Node p0, int p1, NodeFilter p2, boolean p3) throws org.w3c.dom.DOMException;
}
