/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.traversal;

public interface NodeIterator {
  org.w3c.dom.Node getRoot();

  int getWhatToShow();

  NodeFilter getFilter();

  boolean getExpandEntityReferences();

  org.w3c.dom.Node nextNode() throws org.w3c.dom.DOMException;

  org.w3c.dom.Node previousNode() throws org.w3c.dom.DOMException;

  void detach();
}
