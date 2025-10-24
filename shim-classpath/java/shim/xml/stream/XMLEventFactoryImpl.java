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
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;

public final class XMLEventFactoryImpl extends XMLEventFactory {
  private Location location;

  public void setLocation(Location location) {
    this.location = location;
  }

  private <T extends StaxEvents.Base> T located(T e) {
    if (location != null) {
      e.location = location;
    }
    return e;
  }

  private StaxEvents.Attr attr(QName name, String value) {
    return located(new StaxEvents.Attr(name, value));
  }

  public Attribute createAttribute(String prefix, String namespaceURI, String localName, String value) {
    StaxEvents.Attr a = attr(new QName(namespaceURI, localName, prefix), value);
    a.specified = false;
    return a;
  }

  public Attribute createAttribute(String localName, String value) {
    StaxEvents.Attr a = attr(new QName(localName), value);
    a.specified = false;
    return a;
  }

  public Attribute createAttribute(QName name, String value) {
    StaxEvents.Attr a = attr(new QName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix()), value);
    a.specified = false;
    return a;
  }

  public Namespace createNamespace(String namespaceURI) {
    StaxEvents.Ns n = located(new StaxEvents.Ns("", namespaceURI));
    n.specified = false;
    return n;
  }

  public Namespace createNamespace(String prefix, String namespaceUri) {
    if (prefix == null) {
      throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
    }
    StaxEvents.Ns n = located(new StaxEvents.Ns(prefix, namespaceUri));
    n.specified = false;
    return n;
  }

  public StartElement createStartElement(QName name, Iterator<? extends Attribute> attributes,
      Iterator<? extends Namespace> namespaces) {
    return createStartElement(name, attributes, namespaces, null);
  }

  private StartElement createStartElement(QName name, Iterator<? extends Attribute> attributes,
      Iterator<? extends Namespace> namespaces, NamespaceContext context) {
    StaxEvents.StartElem s = located(new StaxEvents.StartElem(new QName(name.getNamespaceURI(), name.getLocalPart(),
        name.getPrefix())));
    while (attributes != null && attributes.hasNext()) {
      Attribute a = attributes.next();
      s.attributes.put(a.getName(), a);
    }
    while (namespaces != null && namespaces.hasNext()) {
      s.namespaces.add(namespaces.next());
    }
    s.context = context;
    return s;
  }

  public StartElement createStartElement(String prefix, String namespaceUri, String localName) {
    return createStartElement(new QName(namespaceUri, localName, prefix), null, null, null);
  }

  public StartElement createStartElement(String prefix, String namespaceUri, String localName,
      Iterator<? extends Attribute> attributes, Iterator<? extends Namespace> namespaces) {
    return createStartElement(new QName(namespaceUri, localName, prefix), attributes, namespaces, null);
  }

  public StartElement createStartElement(String prefix, String namespaceUri, String localName,
      Iterator<? extends Attribute> attributes, Iterator<? extends Namespace> namespaces,
      NamespaceContext context) {
    return createStartElement(new QName(namespaceUri, localName, prefix), attributes, namespaces, context);
  }

  public EndElement createEndElement(QName name, Iterator<? extends Namespace> namespaces) {
    return located(new StaxEvents.EndElem(new QName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix())));
  }

  public EndElement createEndElement(String prefix, String namespaceUri, String localName) {
    return located(new StaxEvents.EndElem(new QName(namespaceUri, localName, prefix)));
  }

  public EndElement createEndElement(String prefix, String namespaceUri, String localName,
      Iterator<? extends Namespace> namespaces) {
    return createEndElement(prefix, namespaceUri, localName);
  }

  public Characters createCharacters(String content) {
    return located(new StaxEvents.Chars(content));
  }

  public Characters createCData(String content) {
    StaxEvents.Chars c = located(new StaxEvents.Chars(content));
    c.cdata = true;
    return c;
  }

  public Characters createSpace(String content) {
    return located(new StaxEvents.Chars(content));
  }

  public Characters createIgnorableSpace(String content) {
    StaxEvents.Chars c = located(new StaxEvents.Chars(content));
    c.ignorable = true;
    return c;
  }

  public StartDocument createStartDocument() {
    StaxEvents.StartDoc d = new StaxEvents.StartDoc();
    d.encodingSet = true;
    d.location = null;
    return d;
  }

  public StartDocument createStartDocument(String encoding, String version, boolean standalone) {
    StaxEvents.StartDoc d = new StaxEvents.StartDoc();
    d.encoding = encoding;
    d.version = version;
    d.standalone = standalone;
    d.standaloneSet = true;
    d.encodingSet = encoding != null;
    d.location = null;
    return d;
  }

  public StartDocument createStartDocument(String encoding, String version) {
    StaxEvents.StartDoc d = new StaxEvents.StartDoc();
    if (encoding != null) {
      d.encoding = encoding;
      d.encodingSet = true;
    }
    d.version = version;
    d.location = null;
    return d;
  }

  public StartDocument createStartDocument(String encoding) {
    StaxEvents.StartDoc d = new StaxEvents.StartDoc();
    d.encoding = encoding;
    d.encodingSet = encoding != null;
    d.location = null;
    return d;
  }

  public EndDocument createEndDocument() {
    return located(new StaxEvents.EndDoc());
  }

  public EntityReference createEntityReference(String name, EntityDeclaration declaration) {
    return located(new StaxEvents.EntityRef(name, declaration));
  }

  public Comment createComment(String text) {
    return located(new StaxEvents.Comm(text));
  }

  public ProcessingInstruction createProcessingInstruction(String target, String data) {
    return located(new StaxEvents.PI(target, data));
  }

  public DTD createDTD(String dtd) {
    return located(new StaxEvents.Doctype(dtd));
  }
}
