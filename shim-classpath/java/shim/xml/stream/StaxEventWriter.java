/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

final class StaxEventWriter implements javax.xml.stream.XMLEventWriter {
  private final XMLStreamWriter writer;

  StaxEventWriter(XMLStreamWriter writer) {
    this.writer = writer;
  }

  public void flush() throws XMLStreamException {
    writer.flush();
  }

  public void close() throws XMLStreamException {
    writer.close();
  }

  public void add(XMLEventReader reader) throws XMLStreamException {
    if (reader == null) {
      throw new XMLStreamException("Event reader shouldn't be null");
    }
    while (reader.hasNext()) {
      add(reader.nextEvent());
    }
  }

  public void add(XMLEvent event) throws XMLStreamException {
    switch (event.getEventType()) {
      case XMLStreamConstants.START_DOCUMENT: {
        StartDocument sd = (StartDocument) event;
        if (writer instanceof StaxWriter) {
          ((StaxWriter) writer).writeDocumentDeclaration(sd.getVersion(), sd.getCharacterEncodingScheme(),
              sd.standaloneSet(), sd.isStandalone());
        } else {
          writer.writeStartDocument(sd.getCharacterEncodingScheme(), sd.getVersion());
        }
        break;
      }
      case XMLStreamConstants.START_ELEMENT: {
        StartElement se = event.asStartElement();
        QName name = se.getName();
        writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
        Iterator<Namespace> ns = se.getNamespaces();
        while (ns.hasNext()) {
          Namespace n = ns.next();
          writer.writeNamespace(n.getPrefix(), n.getNamespaceURI());
        }
        Iterator<Attribute> attrs = se.getAttributes();
        while (attrs.hasNext()) {
          Attribute a = attrs.next();
          QName an = a.getName();
          writer.writeAttribute(an.getPrefix(), an.getNamespaceURI(), an.getLocalPart(), a.getValue());
        }
        break;
      }
      case XMLStreamConstants.NAMESPACE: {
        Namespace n = (Namespace) event;
        writer.writeNamespace(n.getPrefix(), n.getNamespaceURI());
        break;
      }
      case XMLStreamConstants.ATTRIBUTE: {
        Attribute a = (Attribute) event;
        QName an = a.getName();
        writer.writeAttribute(an.getPrefix(), an.getNamespaceURI(), an.getLocalPart(), a.getValue());
        break;
      }
      case XMLStreamConstants.END_ELEMENT:
        writer.writeEndElement();
        break;
      case XMLStreamConstants.CHARACTERS:
      case XMLStreamConstants.CDATA:
      case XMLStreamConstants.SPACE: {
        Characters c = event.asCharacters();
        if (c.isCData()) {
          writer.writeCData(c.getData());
        } else {
          writer.writeCharacters(c.getData());
        }
        break;
      }
      case XMLStreamConstants.COMMENT:
        writer.writeComment(((Comment) event).getText());
        break;
      case XMLStreamConstants.PROCESSING_INSTRUCTION: {
        ProcessingInstruction pi = (ProcessingInstruction) event;
        writer.writeProcessingInstruction(pi.getTarget(), pi.getData());
        break;
      }
      case XMLStreamConstants.DTD:
        writer.writeDTD(((DTD) event).getDocumentTypeDeclaration());
        break;
      case XMLStreamConstants.ENTITY_REFERENCE:
        writer.writeEntityRef(((EntityReference) event).getName());
        break;
      case XMLStreamConstants.END_DOCUMENT:
        writer.writeEndDocument();
        break;
      default:
        break;
    }
  }

  public String getPrefix(String uri) throws XMLStreamException {
    return writer.getPrefix(uri);
  }

  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    writer.setPrefix(prefix, uri);
  }

  public void setDefaultNamespace(String uri) throws XMLStreamException {
    writer.setDefaultNamespace(uri);
  }

  public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    writer.setNamespaceContext(context);
  }

  public NamespaceContext getNamespaceContext() {
    return writer.getNamespaceContext();
  }
}
