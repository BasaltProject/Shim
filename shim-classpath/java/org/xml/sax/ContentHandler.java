/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface ContentHandler {
  void setDocumentLocator(Locator p0);

  void startDocument() throws SAXException;

  default void declaration(String p0, String p1, String p2) throws SAXException {
  }

  void endDocument() throws SAXException;

  void startPrefixMapping(String p0, String p1) throws SAXException;

  void endPrefixMapping(String p0) throws SAXException;

  void startElement(String p0, String p1, String p2, Attributes p3) throws SAXException;

  void endElement(String p0, String p1, String p2) throws SAXException;

  void characters(char[] p0, int p1, int p2) throws SAXException;

  void ignorableWhitespace(char[] p0, int p1, int p2) throws SAXException;

  void processingInstruction(String p0, String p1) throws SAXException;

  void skippedEntity(String p0) throws SAXException;
}
