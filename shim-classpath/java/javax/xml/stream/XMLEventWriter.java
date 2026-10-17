/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

public interface XMLEventWriter extends javax.xml.stream.util.XMLEventConsumer {
  void flush() throws XMLStreamException;

  void close() throws XMLStreamException;

  void add(javax.xml.stream.events.XMLEvent p0) throws XMLStreamException;

  void add(XMLEventReader p0) throws XMLStreamException;

  String getPrefix(String p0) throws XMLStreamException;

  void setPrefix(String p0, String p1) throws XMLStreamException;

  void setDefaultNamespace(String p0) throws XMLStreamException;

  void setNamespaceContext(javax.xml.namespace.NamespaceContext p0) throws XMLStreamException;

  javax.xml.namespace.NamespaceContext getNamespaceContext();
}
