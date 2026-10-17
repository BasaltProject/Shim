/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.util.NoSuchElementException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;

final class StaxEventReader implements XMLEventReader {
  private final XMLStreamReader reader;
  private final XMLEventAllocator allocator;
  private XMLEvent peeked;
  private boolean first = true;
  private boolean closed;
  private XMLEvent lastEvent;

  StaxEventReader(XMLStreamReader reader, XMLEventAllocator allocator) {
    this.reader = reader;
    this.allocator = allocator;
  }

  public XMLEvent nextEvent() throws XMLStreamException {
    if (closed) {
      throw new NoSuchElementException();
    }
    XMLEvent e;
    if (peeked != null) {
      e = peeked;
      peeked = null;
    } else if (first) {
      first = false;
      e = allocator.allocate(reader);
    } else if (reader.hasNext()) {
      reader.next();
      e = allocator.allocate(reader);
    } else {
      throw new NoSuchElementException();
    }
    lastEvent = e;
    return e;
  }

  public boolean hasNext() {
    if (closed) {
      return false;
    }
    if (peeked != null || first) {
      return true;
    }
    try {
      return reader.hasNext();
    } catch (XMLStreamException e) {
      return false;
    }
  }

  public XMLEvent peek() throws XMLStreamException {
    if (peeked != null) {
      return peeked;
    }
    if (closed) {
      return null;
    }
    if (first) {
      peeked = allocator.allocate(reader);
      first = false;
      return peeked;
    }
    if (reader.hasNext()) {
      reader.next();
      peeked = allocator.allocate(reader);
      return peeked;
    }
    return null;
  }

  public String getElementText() throws XMLStreamException {
    if (lastEvent == null) {
      throw new NullPointerException(
          "Cannot invoke \"javax.xml.stream.events.XMLEvent.getEventType()\" because \"this.fLastEvent\" is null");
    }
    if (lastEvent.getEventType() != XMLStreamConstants.START_ELEMENT) {
      throw new XMLStreamException("parser must be on START_ELEMENT to read next text", lastEvent.getLocation());
    }
    StringBuilder content = new StringBuilder();
    if (peeked == null) {
      String data = reader.getElementText();
      lastEvent = allocator.allocate(reader);
      return data;
    }
    while (true) {
      XMLEvent e = nextEvent();
      if (e.isEndElement()) {
        return content.toString();
      }
      if (e.isCharacters()) {
        content.append(((Characters) e).getData());
      } else if (e.isStartElement()) {
        throw new XMLStreamException("elementGetText() function expects text only elment but START_ELEMENT was "
            + "encountered.", e.getLocation());
      } else if (e.isEndDocument()) {
        throw new XMLStreamException("unexpected end of document when reading element text content");
      } else if (e.isEntityReference()) {
        String text = ((javax.xml.stream.events.EntityReference) e).getDeclaration().getReplacementText();
        content.append(text == null ? "" : text);
      }
    }
  }

  public XMLEvent nextTag() throws XMLStreamException {
    while (true) {
      XMLEvent e = nextEvent();
      if (e.isStartElement() || e.isEndElement()) {
        return e;
      }
      if (e.isCharacters() && ((Characters) e).isWhiteSpace()) {
        continue;
      }
      if (e.getEventType() == XMLStreamConstants.COMMENT || e.isProcessingInstruction()
          || e.isStartDocument()) {
        continue;
      }
      throw new XMLStreamException("found: " + StaxNames.eventName(e.getEventType())
          + ", expected START_ELEMENT or END_ELEMENT", e.getLocation());
    }
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    return reader.getProperty(name);
  }

  public void close() throws XMLStreamException {
    closed = true;
    reader.close();
  }

  public Object next() {
    try {
      return nextEvent();
    } catch (XMLStreamException e) {
      throw new NoSuchElementException(e.getMessage());
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
