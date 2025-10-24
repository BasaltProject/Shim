/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream.util;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class EventReaderDelegate implements XMLEventReader {
  private XMLEventReader reader;

  public EventReaderDelegate() {
  }

  public EventReaderDelegate(XMLEventReader reader) {
    this.reader = reader;
  }

  public void setParent(XMLEventReader reader) {
    this.reader = reader;
  }

  public XMLEventReader getParent() {
    return reader;
  }

  public XMLEvent nextEvent() throws XMLStreamException {
    return reader.nextEvent();
  }

  public Object next() {
    return reader.next();
  }

  public boolean hasNext() {
    return reader.hasNext();
  }

  public XMLEvent peek() throws XMLStreamException {
    return reader.peek();
  }

  public void close() throws XMLStreamException {
    reader.close();
  }

  public String getElementText() throws XMLStreamException {
    return reader.getElementText();
  }

  public XMLEvent nextTag() throws XMLStreamException {
    return reader.nextTag();
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    return reader.getProperty(name);
  }

  public void remove() {
    reader.remove();
  }
}
