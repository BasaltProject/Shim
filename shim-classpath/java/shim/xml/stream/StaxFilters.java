/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import javax.xml.stream.util.StreamReaderDelegate;

final class StaxFilters {
  private StaxFilters() {
  }

  static final class StreamFilterReader extends StreamReaderDelegate {
    private final StreamFilter filter;

    StreamFilterReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {
      super(reader);
      this.filter = filter;
      while (!filter.accept(reader) && reader.hasNext()) {
        reader.next();
      }
    }

    @Override
    public int next() throws XMLStreamException {
      XMLStreamReader r = getParent();
      while (r.hasNext()) {
        r.next();
        if (filter.accept(r)) {
          break;
        }
      }
      return r.getEventType();
    }
  }

  static final class EventFilterReader extends EventReaderDelegate {
    private final EventFilter filter;

    EventFilterReader(XMLEventReader reader, EventFilter filter) {
      super(reader);
      this.filter = filter;
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
      XMLEventReader r = getParent();
      XMLEvent e = r.peek();
      while (e != null && !filter.accept(e)) {
        r.nextEvent();
        e = r.peek();
      }
      return e;
    }

    @Override
    public boolean hasNext() {
      try {
        return peek() != null;
      } catch (XMLStreamException ex) {
        return false;
      }
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
      if (peek() == null) {
        throw new java.util.NoSuchElementException();
      }
      return getParent().nextEvent();
    }

    @Override
    public Object next() {
      try {
        return nextEvent();
      } catch (XMLStreamException ex) {
        throw new java.util.NoSuchElementException(ex.getMessage());
      }
    }
  }
}
