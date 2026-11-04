/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.xpath;

public interface XPathResult {
  short ANY_TYPE = (short) 0;
  short NUMBER_TYPE = (short) 1;
  short STRING_TYPE = (short) 2;
  short BOOLEAN_TYPE = (short) 3;
  short UNORDERED_NODE_ITERATOR_TYPE = (short) 4;
  short ORDERED_NODE_ITERATOR_TYPE = (short) 5;
  short UNORDERED_NODE_SNAPSHOT_TYPE = (short) 6;
  short ORDERED_NODE_SNAPSHOT_TYPE = (short) 7;
  short ANY_UNORDERED_NODE_TYPE = (short) 8;
  short FIRST_ORDERED_NODE_TYPE = (short) 9;

  boolean getBooleanValue() throws org.w3c.dom.xpath.XPathException;
  boolean getInvalidIteratorState();
  double getNumberValue() throws org.w3c.dom.xpath.XPathException;
  short getResultType();
  org.w3c.dom.Node getSingleNodeValue() throws org.w3c.dom.xpath.XPathException;
  int getSnapshotLength() throws org.w3c.dom.xpath.XPathException;
  java.lang.String getStringValue() throws org.w3c.dom.xpath.XPathException;
  org.w3c.dom.Node iterateNext() throws org.w3c.dom.xpath.XPathException, org.w3c.dom.DOMException;
  org.w3c.dom.Node snapshotItem(int i) throws org.w3c.dom.xpath.XPathException;
}
