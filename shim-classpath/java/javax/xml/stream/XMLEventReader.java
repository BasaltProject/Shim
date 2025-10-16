/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

public interface XMLEventReader extends java.util.Iterator<Object> {
  javax.xml.stream.events.XMLEvent nextEvent() throws XMLStreamException;

  boolean hasNext();

  javax.xml.stream.events.XMLEvent peek() throws XMLStreamException;

  String getElementText() throws XMLStreamException;

  javax.xml.stream.events.XMLEvent nextTag() throws XMLStreamException;

  Object getProperty(String p0) throws IllegalArgumentException;

  void close() throws XMLStreamException;
}
