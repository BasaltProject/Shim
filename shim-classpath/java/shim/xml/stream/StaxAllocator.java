/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.stream.util.XMLEventConsumer;

public final class StaxAllocator implements XMLEventAllocator {
  public XMLEventAllocator newInstance() {
    return new StaxAllocator();
  }

  public XMLEvent allocate(XMLStreamReader r) throws XMLStreamException {
    if (r == null) {
      throw new XMLStreamException("Reader cannot be null");
    }
    XMLEvent e = build(r);
    ((StaxEvents.Base) e).location = r.getLocation();
    return e;
  }

  public void allocate(XMLStreamReader r, XMLEventConsumer consumer) throws XMLStreamException {
    consumer.add(allocate(r));
  }

  private static QName name(XMLStreamReader r) {
    QName q = r.getName();
    return new QName(q.getNamespaceURI(), q.getLocalPart(), q.getPrefix());
  }

  private XMLEvent build(XMLStreamReader r) throws XMLStreamException {
    switch (r.getEventType()) {
      case XMLStreamConstants.START_DOCUMENT: {
        StaxEvents.StartDoc d = new StaxEvents.StartDoc();
        d.version = r.getVersion();
        d.encoding = r.getEncoding();
        d.standalone = r.isStandalone();
        d.standaloneSet = r.standaloneSet();
        d.encodingSet = r.getCharacterEncodingScheme() != null;
        d.systemId = r.getLocation().getSystemId();
        return d;
      }
      case XMLStreamConstants.START_ELEMENT: {
        StaxEvents.StartElem s = new StaxEvents.StartElem(name(r));
        for (int i = 0; i < r.getAttributeCount(); i++) {
          QName an = r.getAttributeName(i);
          StaxEvents.Attr a = new StaxEvents.Attr(new QName(an.getNamespaceURI(), an.getLocalPart(), an.getPrefix()),
              r.getAttributeValue(i));
          a.dtdType = r.getAttributeType(i);
          a.specified = r.isAttributeSpecified(i);
          s.attributes.put(a.name, a);
        }
        for (int i = 0; i < r.getNamespaceCount(); i++) {
          s.namespaces.add(new StaxEvents.Ns(r.getNamespacePrefix(i), r.getNamespaceURI(i)));
        }
        s.context = r instanceof StaxReader ? ((StaxReader) r).snapshotContext() : r.getNamespaceContext();
        return s;
      }
      case XMLStreamConstants.END_ELEMENT:
        return new StaxEvents.EndElem(name(r));
      case XMLStreamConstants.CHARACTERS:
        return new StaxEvents.Chars(r.getText());
      case XMLStreamConstants.CDATA: {
        StaxEvents.Chars c = new StaxEvents.Chars(XMLStreamConstants.CDATA, r.getText());
        c.cdata = true;
        return c;
      }
      case XMLStreamConstants.SPACE: {
        StaxEvents.Chars c = new StaxEvents.Chars(r.getText());
        c.ignorable = true;
        return c;
      }
      case XMLStreamConstants.COMMENT:
        return new StaxEvents.Comm(r.getText());
      case XMLStreamConstants.PROCESSING_INSTRUCTION:
        return new StaxEvents.PI(r.getPITarget(), r.getPIData());
      case XMLStreamConstants.ENTITY_REFERENCE: {
        StaxEvents.EntityDecl d = new StaxEvents.EntityDecl();
        d.name = r.getLocalName();
        d.replacement = r.getText();
        return new StaxEvents.EntityRef(r.getLocalName(), d);
      }
      case XMLStreamConstants.DTD: {
        StaxEvents.Doctype d = new StaxEvents.Doctype(r.getText());
        if (r instanceof StaxReader) {
          List<EntityDeclaration> ents = new ArrayList<EntityDeclaration>();
          List<NotationDeclaration> nots = new ArrayList<NotationDeclaration>();
          for (StaxReader.Declared x : ((StaxReader) r).declarations()) {
            if (x.notationDecl) {
              nots.add(new StaxEvents.NotationDecl(x.name, x.publicId, x.systemId));
            } else {
              StaxEvents.EntityDecl ed = new StaxEvents.EntityDecl();
              ed.name = x.name;
              ed.replacement = x.value;
              ed.publicId = x.publicId;
              ed.systemId = x.systemId;
              ed.notation = x.notation;
              ents.add(ed);
            }
          }
          d.entities = ents.isEmpty() ? null : ents;
          d.notations = nots.isEmpty() ? null : nots;
        }
        return d;
      }
      case XMLStreamConstants.END_DOCUMENT:
        return new StaxEvents.EndDoc();
      default:
        throw new XMLStreamException("Unsupported event type " + r.getEventType());
    }
  }

  static NamespaceContext copy(final java.util.Map<String, String> bindings) {
    return new NamespaceContext() {
      public String getNamespaceURI(String prefix) {
        if (prefix == null) {
          throw new IllegalArgumentException("prefix cannot be null.");
        }
        return bindings.get(prefix);
      }

      public String getPrefix(String uri) {
        for (java.util.Map.Entry<String, String> e : bindings.entrySet()) {
          if (e.getValue() != null && e.getValue().equals(uri)) {
            return e.getKey();
          }
        }
        return null;
      }

      public Iterator<String> getPrefixes(String uri) {
        List<String> out = new ArrayList<String>();
        for (java.util.Map.Entry<String, String> e : bindings.entrySet()) {
          if (e.getValue() != null && e.getValue().equals(uri)) {
            out.add(e.getKey());
          }
        }
        return out.iterator();
      }
    };
  }
}
