/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface Document extends Node {
  DocumentType getDoctype();

  DOMImplementation getImplementation();

  Element getDocumentElement();

  Element createElement(String p0) throws DOMException;

  DocumentFragment createDocumentFragment();

  Text createTextNode(String p0);

  Comment createComment(String p0);

  CDATASection createCDATASection(String p0) throws DOMException;

  ProcessingInstruction createProcessingInstruction(String p0, String p1) throws DOMException;

  Attr createAttribute(String p0) throws DOMException;

  EntityReference createEntityReference(String p0) throws DOMException;

  NodeList getElementsByTagName(String p0);

  Node importNode(Node p0, boolean p1) throws DOMException;

  Element createElementNS(String p0, String p1) throws DOMException;

  Attr createAttributeNS(String p0, String p1) throws DOMException;

  NodeList getElementsByTagNameNS(String p0, String p1);

  Element getElementById(String p0);

  String getInputEncoding();

  String getXmlEncoding();

  boolean getXmlStandalone();

  void setXmlStandalone(boolean p0) throws DOMException;

  String getXmlVersion();

  void setXmlVersion(String p0) throws DOMException;

  boolean getStrictErrorChecking();

  void setStrictErrorChecking(boolean p0);

  String getDocumentURI();

  void setDocumentURI(String p0);

  Node adoptNode(Node p0) throws DOMException;

  DOMConfiguration getDomConfig();

  void normalizeDocument();

  Node renameNode(Node p0, String p1, String p2) throws DOMException;
}
