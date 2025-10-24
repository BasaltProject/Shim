/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

final class StaxEvents {
  private StaxEvents() {
  }

  static final Location kNoLocation = new StaxLocation(-1, -1, -1, null, null);

  abstract static class Base implements XMLEvent {
    final int type;
    Location location = kNoLocation;

    Base(int type) {
      this.type = type;
    }

    public int getEventType() {
      return type;
    }

    public Location getLocation() {
      return location;
    }

    public boolean isStartElement() {
      return type == XMLStreamConstants.START_ELEMENT;
    }

    public boolean isAttribute() {
      return type == XMLStreamConstants.ATTRIBUTE;
    }

    public boolean isNamespace() {
      return type == XMLStreamConstants.NAMESPACE;
    }

    public boolean isEndElement() {
      return type == XMLStreamConstants.END_ELEMENT;
    }

    public boolean isEntityReference() {
      return type == XMLStreamConstants.ENTITY_REFERENCE;
    }

    public boolean isProcessingInstruction() {
      return type == XMLStreamConstants.PROCESSING_INSTRUCTION;
    }

    public boolean isCharacters() {
      return type == XMLStreamConstants.CHARACTERS;
    }

    public boolean isStartDocument() {
      return type == XMLStreamConstants.START_DOCUMENT;
    }

    public boolean isEndDocument() {
      return type == XMLStreamConstants.END_DOCUMENT;
    }

    public StartElement asStartElement() {
      return (StartElement) this;
    }

    public EndElement asEndElement() {
      return (EndElement) this;
    }

    public Characters asCharacters() {
      return (Characters) this;
    }

    public QName getSchemaType() {
      return null;
    }

    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
      try {
        writer.write(toString());
      } catch (IOException e) {
        throw new XMLStreamException(e);
      }
    }
  }

  static String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '&') {
        sb.append("&amp;");
      } else if (c == '<') {
        sb.append("&lt;");
      } else if (c == '>') {
        sb.append("&gt;");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  static String nameAsString(QName name) {
    String ns = name.getNamespaceURI();
    if (ns != null && !ns.isEmpty()) {
      return "['" + ns + "']:" + (name.getPrefix() == null ? "" : name.getPrefix()) + ":" + name.getLocalPart();
    }
    if (name.getPrefix() != null && !name.getPrefix().isEmpty()) {
      return name.getPrefix() + ":" + name.getLocalPart();
    }
    return name.getLocalPart();
  }

  static final class StartDoc extends Base implements StartDocument {
    String encoding = "UTF-8";
    String version = "1.0";
    boolean standalone = true;
    boolean standaloneSet;
    boolean encodingSet;
    String systemId = "";

    StartDoc() {
      super(START_DOCUMENT);
    }

    public String getSystemId() {
      return systemId;
    }

    public String getCharacterEncodingScheme() {
      return encoding;
    }

    public boolean encodingSet() {
      return encodingSet;
    }

    public boolean isStandalone() {
      return standalone;
    }

    public boolean standaloneSet() {
      return standaloneSet;
    }

    public String getVersion() {
      return version;
    }

    @Override
    public String toString() {
      String s = "<?xml version=\"" + version + "\"";
      s += " encoding='" + encoding + "'";
      if (standaloneSet) {
        s += standalone ? " standalone='yes'?>" : " standalone='no'?>";
      } else {
        s += "?>";
      }
      return s;
    }
  }

  static final class EndDoc extends Base implements EndDocument {
    EndDoc() {
      super(END_DOCUMENT);
    }

    @Override
    public String toString() {
      return "ENDDOCUMENT";
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) {
    }
  }

  static class Attr extends Base implements Attribute {
    QName name;
    String value;
    String dtdType = "CDATA";
    boolean specified = true;

    Attr(QName name, String value) {
      super(ATTRIBUTE);
      this.name = name;
      this.value = value;
    }

    Attr(int type, QName name, String value) {
      super(type);
      this.name = name;
      this.value = value;
    }

    public QName getName() {
      return name;
    }

    public String getValue() {
      return value;
    }

    public String getDTDType() {
      return dtdType;
    }

    public boolean isSpecified() {
      return specified;
    }

    @Override
    public String toString() {
      String prefix = name.getPrefix();
      if (prefix != null && !prefix.isEmpty()) {
        return prefix + ":" + name.getLocalPart() + "='" + value + "'";
      }
      return name.getLocalPart() + "='" + value + "'";
    }
  }

  static final class Ns extends Attr implements Namespace {
    String prefix;
    String uri;

    Ns(String prefix, String uri) {
      super(NAMESPACE, nsName(prefix), uri);
      this.prefix = prefix == null ? "" : prefix;
      this.uri = uri;
    }

    private static QName nsName(String prefix) {
      if (prefix == null || prefix.isEmpty()) {
        return new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "", XMLConstants.XMLNS_ATTRIBUTE);
      }
      return new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, XMLConstants.XMLNS_ATTRIBUTE);
    }

    public String getPrefix() {
      return prefix;
    }

    public String getNamespaceURI() {
      return uri;
    }

    public boolean isDefaultNamespaceDeclaration() {
      return prefix.isEmpty();
    }

    @Override
    public boolean isNamespace() {
      return true;
    }

    @Override
    public boolean isAttribute() {
      return false;
    }

    @Override
    public String toString() {
      return "xmlns:" + prefix + "='" + uri + "'";
    }
  }

  static final class StartElem extends Base implements StartElement {
    QName name;
    final Map<QName, Attribute> attributes = new LinkedHashMap<QName, Attribute>();
    final List<Namespace> namespaces = new ArrayList<Namespace>();
    NamespaceContext context;

    StartElem(QName name) {
      super(START_ELEMENT);
      this.name = name;
    }

    public QName getName() {
      return name;
    }

    public Iterator<Attribute> getAttributes() {
      return Collections.unmodifiableCollection(attributes.values()).iterator();
    }

    public Iterator<Namespace> getNamespaces() {
      return Collections.unmodifiableList(namespaces).iterator();
    }

    public Attribute getAttributeByName(QName qname) {
      return attributes.get(qname);
    }

    public NamespaceContext getNamespaceContext() {
      return context;
    }

    public String getNamespaceURI(String prefix) {
      if (context != null) {
        return context.getNamespaceURI(prefix);
      }
      return null;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("<").append(nameAsString(name));
      for (Attribute a : attributes.values()) {
        sb.append(' ').append(a);
      }
      for (Namespace n : namespaces) {
        sb.append(' ').append(n);
      }
      return sb.append('>').toString();
    }
  }

  static final class EndElem extends Base implements EndElement {
    QName name;
    final List<Namespace> namespaces = new ArrayList<Namespace>();

    EndElem(QName name) {
      super(END_ELEMENT);
      this.name = name;
    }

    public QName getName() {
      return name;
    }

    public Iterator<Namespace> getNamespaces() {
      return Collections.unmodifiableList(namespaces).iterator();
    }

    @Override
    public String toString() {
      return "</" + nameAsString(name) + ">";
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
      try {
        writer.write("</");
        String prefix = name.getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
          writer.write(prefix);
          writer.write(':');
        }
        writer.write(name.getLocalPart());
        writer.write('>');
      } catch (IOException e) {
        throw new XMLStreamException(e);
      }
    }
  }

  static final class Chars extends Base implements Characters {
    final String data;
    boolean cdata;
    boolean ignorable;

    Chars(String data) {
      super(CHARACTERS);
      this.data = data;
    }

    Chars(int type, String data) {
      super(type);
      this.data = data;
    }

    public String getData() {
      return data;
    }

    public boolean isWhiteSpace() {
      if (data == null) {
        return false;
      }
      for (int i = 0; i < data.length(); i++) {
        char c = data.charAt(i);
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
          return false;
        }
      }
      return !data.isEmpty();
    }

    public boolean isCData() {
      return cdata;
    }

    public boolean isIgnorableWhiteSpace() {
      return ignorable;
    }

    @Override
    public String toString() {
      return cdata ? "<![CDATA[" + data + "]]>" : data;
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
      try {
        writer.write(cdata ? "<![CDATA[" + data + "]]>" : data == null ? "" : escape(data));
      } catch (IOException e) {
        throw new XMLStreamException(e);
      }
    }
  }

  static final class Comm extends Base implements Comment {
    final String text;

    Comm(String text) {
      super(COMMENT);
      this.text = text;
    }

    public String getText() {
      return text;
    }

    @Override
    public String toString() {
      return "<!--" + text + "-->";
    }
  }

  static final class PI extends Base implements ProcessingInstruction {
    final String target;
    final String data;

    PI(String target, String data) {
      super(PROCESSING_INSTRUCTION);
      this.target = target;
      this.data = data;
    }

    public String getTarget() {
      return target;
    }

    public String getData() {
      return data;
    }

    @Override
    public String toString() {
      if (data != null && target != null) {
        return "<?" + target + " " + data + "?>";
      }
      if (target != null) {
        return "<?" + target + "?>";
      }
      return data != null ? "<?" + data + "?>" : "<??>";
    }
  }

  static final class EntityDecl extends Base implements EntityDeclaration {
    String name;
    String publicId;
    String systemId;
    String notation;
    String replacement;
    String baseUri;

    EntityDecl() {
      super(ENTITY_DECLARATION);
    }

    public String getPublicId() {
      return publicId;
    }

    public String getSystemId() {
      return systemId;
    }

    public String getName() {
      return name;
    }

    public String getNotationName() {
      return notation;
    }

    public String getReplacementText() {
      return replacement;
    }

    public String getBaseURI() {
      return baseUri;
    }

    @Override
    public String toString() {
      return "<!ENTITY " + name + (replacement != null ? " '" + replacement + "'" : "") + ">";
    }
  }

  static final class NotationDecl extends Base implements NotationDeclaration {
    final String name;
    final String publicId;
    final String systemId;

    NotationDecl(String name, String publicId, String systemId) {
      super(NOTATION_DECLARATION);
      this.name = name;
      this.publicId = publicId;
      this.systemId = systemId;
    }

    public String getName() {
      return name;
    }

    public String getPublicId() {
      return publicId;
    }

    public String getSystemId() {
      return systemId;
    }

    @Override
    public String toString() {
      return "<!NOTATION " + name + ">";
    }
  }

  static final class EntityRef extends Base implements EntityReference {
    final String name;
    final EntityDeclaration declaration;

    EntityRef(String name, EntityDeclaration declaration) {
      super(ENTITY_REFERENCE);
      this.name = name;
      this.declaration = declaration;
    }

    public EntityDeclaration getDeclaration() {
      return declaration;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      if (declaration == null) {
        throw new NullPointerException("Cannot invoke \"javax.xml.stream.events.EntityDeclaration.getReplacementText()\" "
            + "because \"this.fEntityDeclaration\" is null");
      }
      String text = declaration.getReplacementText();
      if (text == null) {
        text = "";
      }
      return "&" + name + ";='" + text + "'";
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
      try {
        writer.write("&" + name + ";");
      } catch (IOException e) {
        throw new XMLStreamException(e);
      }
    }
  }

  static final class Doctype extends Base implements DTD {
    final String text;
    List<NotationDeclaration> notations;
    List<EntityDeclaration> entities;

    Doctype(String text) {
      super(DTD);
      this.text = text;
    }

    public String getDocumentTypeDeclaration() {
      return text;
    }

    public Object getProcessedDTD() {
      return null;
    }

    public List<NotationDeclaration> getNotations() {
      return notations;
    }

    public List<EntityDeclaration> getEntities() {
      return entities;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
