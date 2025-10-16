/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ranges;

public interface Range {
  short START_TO_START = 0;
  short START_TO_END = 1;
  short END_TO_END = 2;
  short END_TO_START = 3;

  org.w3c.dom.Node getStartContainer() throws org.w3c.dom.DOMException;

  int getStartOffset() throws org.w3c.dom.DOMException;

  org.w3c.dom.Node getEndContainer() throws org.w3c.dom.DOMException;

  int getEndOffset() throws org.w3c.dom.DOMException;

  boolean getCollapsed() throws org.w3c.dom.DOMException;

  org.w3c.dom.Node getCommonAncestorContainer() throws org.w3c.dom.DOMException;

  void setStart(org.w3c.dom.Node p0, int p1) throws RangeException, org.w3c.dom.DOMException;

  void setEnd(org.w3c.dom.Node p0, int p1) throws RangeException, org.w3c.dom.DOMException;

  void setStartBefore(org.w3c.dom.Node p0) throws RangeException, org.w3c.dom.DOMException;

  void setStartAfter(org.w3c.dom.Node p0) throws RangeException, org.w3c.dom.DOMException;

  void setEndBefore(org.w3c.dom.Node p0) throws RangeException, org.w3c.dom.DOMException;

  void setEndAfter(org.w3c.dom.Node p0) throws RangeException, org.w3c.dom.DOMException;

  void collapse(boolean p0) throws org.w3c.dom.DOMException;

  void selectNode(org.w3c.dom.Node p0) throws RangeException, org.w3c.dom.DOMException;

  void selectNodeContents(org.w3c.dom.Node p0) throws RangeException, org.w3c.dom.DOMException;

  short compareBoundaryPoints(short p0, Range p1) throws org.w3c.dom.DOMException;

  void deleteContents() throws org.w3c.dom.DOMException;

  org.w3c.dom.DocumentFragment extractContents() throws org.w3c.dom.DOMException;

  org.w3c.dom.DocumentFragment cloneContents() throws org.w3c.dom.DOMException;

  void insertNode(org.w3c.dom.Node p0) throws org.w3c.dom.DOMException, RangeException;

  void surroundContents(org.w3c.dom.Node p0) throws org.w3c.dom.DOMException, RangeException;

  Range cloneRange() throws org.w3c.dom.DOMException;

  String toString() throws org.w3c.dom.DOMException;

  void detach() throws org.w3c.dom.DOMException;
}
