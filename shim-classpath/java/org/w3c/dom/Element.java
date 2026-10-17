/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface Element extends Node {
  String getTagName();

  String getAttribute(String p0);

  void setAttribute(String p0, String p1) throws DOMException;

  void removeAttribute(String p0) throws DOMException;

  Attr getAttributeNode(String p0);

  Attr setAttributeNode(Attr p0) throws DOMException;

  Attr removeAttributeNode(Attr p0) throws DOMException;

  NodeList getElementsByTagName(String p0);

  String getAttributeNS(String p0, String p1) throws DOMException;

  void setAttributeNS(String p0, String p1, String p2) throws DOMException;

  void removeAttributeNS(String p0, String p1) throws DOMException;

  Attr getAttributeNodeNS(String p0, String p1) throws DOMException;

  Attr setAttributeNodeNS(Attr p0) throws DOMException;

  NodeList getElementsByTagNameNS(String p0, String p1) throws DOMException;

  boolean hasAttribute(String p0);

  boolean hasAttributeNS(String p0, String p1) throws DOMException;

  TypeInfo getSchemaTypeInfo();

  void setIdAttribute(String p0, boolean p1) throws DOMException;

  void setIdAttributeNS(String p0, String p1, boolean p2) throws DOMException;

  void setIdAttributeNode(Attr p0, boolean p1) throws DOMException;
}
