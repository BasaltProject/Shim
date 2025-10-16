/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

public interface XMLStreamReader extends XMLStreamConstants {
  Object getProperty(String p0) throws IllegalArgumentException;

  int next() throws XMLStreamException;

  void require(int p0, String p1, String p2) throws XMLStreamException;

  String getElementText() throws XMLStreamException;

  int nextTag() throws XMLStreamException;

  boolean hasNext() throws XMLStreamException;

  void close() throws XMLStreamException;

  String getNamespaceURI(String p0);

  boolean isStartElement();

  boolean isEndElement();

  boolean isCharacters();

  boolean isWhiteSpace();

  String getAttributeValue(String p0, String p1);

  int getAttributeCount();

  javax.xml.namespace.QName getAttributeName(int p0);

  String getAttributeNamespace(int p0);

  String getAttributeLocalName(int p0);

  String getAttributePrefix(int p0);

  String getAttributeType(int p0);

  String getAttributeValue(int p0);

  boolean isAttributeSpecified(int p0);

  int getNamespaceCount();

  String getNamespacePrefix(int p0);

  String getNamespaceURI(int p0);

  javax.xml.namespace.NamespaceContext getNamespaceContext();

  int getEventType();

  String getText();

  char[] getTextCharacters();

  int getTextCharacters(int p0, char[] p1, int p2, int p3) throws XMLStreamException;

  int getTextStart();

  int getTextLength();

  String getEncoding();

  boolean hasText();

  Location getLocation();

  javax.xml.namespace.QName getName();

  String getLocalName();

  boolean hasName();

  String getNamespaceURI();

  String getPrefix();

  String getVersion();

  boolean isStandalone();

  boolean standaloneSet();

  String getCharacterEncodingScheme();

  String getPITarget();

  String getPIData();
}
