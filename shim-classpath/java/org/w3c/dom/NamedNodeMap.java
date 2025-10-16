/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface NamedNodeMap {
  Node getNamedItem(String p0);

  Node setNamedItem(Node p0) throws DOMException;

  Node removeNamedItem(String p0) throws DOMException;

  Node item(int p0);

  int getLength();

  Node getNamedItemNS(String p0, String p1) throws DOMException;

  Node setNamedItemNS(Node p0) throws DOMException;

  Node removeNamedItemNS(String p0, String p1) throws DOMException;
}
