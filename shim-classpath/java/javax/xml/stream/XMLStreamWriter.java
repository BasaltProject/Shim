/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

public interface XMLStreamWriter {
  void writeStartElement(String p0) throws XMLStreamException;

  void writeStartElement(String p0, String p1) throws XMLStreamException;

  void writeStartElement(String p0, String p1, String p2) throws XMLStreamException;

  void writeEmptyElement(String p0, String p1) throws XMLStreamException;

  void writeEmptyElement(String p0, String p1, String p2) throws XMLStreamException;

  void writeEmptyElement(String p0) throws XMLStreamException;

  void writeEndElement() throws XMLStreamException;

  void writeEndDocument() throws XMLStreamException;

  void close() throws XMLStreamException;

  void flush() throws XMLStreamException;

  void writeAttribute(String p0, String p1) throws XMLStreamException;

  void writeAttribute(String p0, String p1, String p2, String p3) throws XMLStreamException;

  void writeAttribute(String p0, String p1, String p2) throws XMLStreamException;

  void writeNamespace(String p0, String p1) throws XMLStreamException;

  void writeDefaultNamespace(String p0) throws XMLStreamException;

  void writeComment(String p0) throws XMLStreamException;

  void writeProcessingInstruction(String p0) throws XMLStreamException;

  void writeProcessingInstruction(String p0, String p1) throws XMLStreamException;

  void writeCData(String p0) throws XMLStreamException;

  void writeDTD(String p0) throws XMLStreamException;

  void writeEntityRef(String p0) throws XMLStreamException;

  void writeStartDocument() throws XMLStreamException;

  void writeStartDocument(String p0) throws XMLStreamException;

  void writeStartDocument(String p0, String p1) throws XMLStreamException;

  void writeCharacters(String p0) throws XMLStreamException;

  void writeCharacters(char[] p0, int p1, int p2) throws XMLStreamException;

  String getPrefix(String p0) throws XMLStreamException;

  void setPrefix(String p0, String p1) throws XMLStreamException;

  void setDefaultNamespace(String p0) throws XMLStreamException;

  void setNamespaceContext(javax.xml.namespace.NamespaceContext p0) throws XMLStreamException;

  javax.xml.namespace.NamespaceContext getNamespaceContext();

  Object getProperty(String p0) throws IllegalArgumentException;
}
