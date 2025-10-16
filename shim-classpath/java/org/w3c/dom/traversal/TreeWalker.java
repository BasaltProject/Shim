/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.traversal;

public interface TreeWalker {
  org.w3c.dom.Node getRoot();

  int getWhatToShow();

  NodeFilter getFilter();

  boolean getExpandEntityReferences();

  org.w3c.dom.Node getCurrentNode();

  void setCurrentNode(org.w3c.dom.Node p0) throws org.w3c.dom.DOMException;

  org.w3c.dom.Node parentNode();

  org.w3c.dom.Node firstChild();

  org.w3c.dom.Node lastChild();

  org.w3c.dom.Node previousSibling();

  org.w3c.dom.Node nextSibling();

  org.w3c.dom.Node previousNode();

  org.w3c.dom.Node nextNode();
}
