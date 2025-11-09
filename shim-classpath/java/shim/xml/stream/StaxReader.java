/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import shim.xml.XmlDtd;
import shim.xml.XmlException;
import shim.xml.XmlMalformedException;
import shim.xml.XmlPullParser;

public final class StaxReader implements XMLStreamReader {
  static final class Declared {
    String name;
    String value;
    String publicId;
    String systemId;
    String notation;
    boolean notationDecl;
  }

  private static final class ElementRecord {
    String qname;
    String uri;
    String local;
    String prefix;
    String[] decls;
  }

  private static final String kCdataProperty = "http://java.sun.com/xml/stream/properties/report-cdata-event";

  private final XmlPullParser parser;
  private final StaxConfig config;
  private final String encoding;
  private final StaxResolver bridge;
  private final String systemId;
  private final String publicId;
  private XmlPullParser.Ev pending;
  private int type = START_DOCUMENT;
  private boolean finished;
  private boolean mergeNext;
  private boolean inCdata;
  private int[] entityMark;
  private int userEntities;
  private int line = 1;
  private int column = 1;
  private int offset;

  private String qname;
  private String uri;
  private String local;
  private String prefix;
  private List<StaxAttribute> attributes = new ArrayList<StaxAttribute>();
  private String[] decls = new String[0];
  private String text;
  private String piTarget;
  private String piData;
  private String entityName;
  private final ArrayList<ElementRecord> elements = new ArrayList<ElementRecord>();
  private final ArrayList<String[]> scopes = new ArrayList<String[]>();
  private boolean popScope;
  private final List<Declared> declared = new ArrayList<Declared>();

  StaxReader(XmlPullParser parser, StaxConfig config, String encoding, StaxResolver bridge)
      throws XMLStreamException {
    this.parser = parser;
    this.config = config;
    this.encoding = encoding;
    this.bridge = bridge;
    this.systemId = parser.input().systemId;
    this.publicId = parser.input().publicId;
    try {
      parser.beginDocument();
      XmlPullParser.Ev ev = parser.current();
      line = ev.line;
      column = ev.col;
      offset = ev.offset;
      if (!parser.hasDeclaration) {
        line = 1;
        column = 1;
        offset = 0;
      }
    } catch (XmlException e) {
      throw wrap(e);
    } catch (XmlMalformedException e) {
      throw new XMLStreamException(e.getMessage(), location());
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  List<Declared> declarations() {
    return declared;
  }

  private Location location() {
    return new StaxLocation(line, column, offset, systemId, publicId);
  }

  private XMLStreamException wrap(XmlException e) {
    return new XMLStreamException(translate(e.getMessage()), new StaxLocation(e.line, e.column, -1, systemId,
        publicId));
  }

  private String translate(String message) {
    if (!config.namespaceAware) {
      return message;
    }
    java.util.regex.Matcher m = java.util.regex.Pattern.compile(
        "Attribute \"(.*)\" was already specified for element \"(.*)\"\\.").matcher(message);
    if (m.matches()) {
      return "http://www.w3.org/TR/1999/REC-xml-names-19990114#AttributeNotUnique?" + m.group(2) + "&" + m.group(1);
    }
    m = java.util.regex.Pattern.compile("The prefix \"(.*)\" for element \"(.*)\" is not bound\\.").matcher(message);
    if (m.matches()) {
      return "http://www.w3.org/TR/1999/REC-xml-names-19990114#ElementPrefixUnbound?" + m.group(1) + "&"
          + m.group(2);
    }
    m = java.util.regex.Pattern.compile(
        "The prefix \"(.*)\" for attribute \"(.*)\" associated with an element type \"(.*)\" is not bound\\.")
        .matcher(message);
    if (m.matches()) {
      return "http://www.w3.org/TR/1999/REC-xml-names-19990114#AttributePrefixUnbound?" + m.group(3) + "&"
          + m.group(2) + "&" + m.group(1);
    }
    return message;
  }

  private XmlPullParser.Ev raw() throws XMLStreamException {
    if (pending != null) {
      XmlPullParser.Ev e = pending;
      pending = null;
      return e;
    }
    try {
      int kind = parser.next();
      XmlPullParser.Ev current = parser.current();
      if (current == null || current.kind != kind) {
        current = new XmlPullParser.Ev();
        current.kind = kind;
      }
      return current;
    } catch (XmlException e) {
      throw wrap(e);
    } catch (XmlMalformedException e) {
      throw new XMLStreamException(e.getMessage(), new StaxLocation(parser.line(), parser.column(), -1, null, null));
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  private void mark(XmlPullParser.Ev e) {
    line = e.line;
    column = e.col;
    offset = e.offset;
    if (e.kind == XmlPullParser.kCharacters || e.kind == XmlPullParser.kIgnorableWhitespace) {
      column = e.col - e.lookahead + e.staxPad;
      offset = e.offset - e.lookahead + e.staxPad;
    }
  }

  private void markText(XmlPullParser.Ev e, int pad) {
    line = e.line;
    column = e.col - e.lookahead + pad;
    offset = e.offset - e.lookahead + pad;
  }

  private void markPlain(XmlPullParser.Ev e) {
    line = e.line;
    column = e.col;
    offset = e.offset;
  }

  public int next() throws XMLStreamException {
    if (type == END_DOCUMENT) {
      throw new NoSuchElementException("END_DOCUMENT reached: no more elements on the stream.");
    }
    if (popScope) {
      popScope = false;
      scopes.remove(scopes.size() - 1);
    }
    attributes = new ArrayList<StaxAttribute>();
    text = null;
    while (true) {
      XmlPullParser.Ev e = raw();
      switch (e.kind) {
        case XmlPullParser.kDtdStart:
          dtd(e);
          return type;
        case XmlPullParser.kStartElement:
          mergeNext = false;
          startElement(e);
          return type;
        case XmlPullParser.kEndElement:
          mergeNext = false;
          endElement(e);
          return type;
        case XmlPullParser.kComment:
          mergeNext = false;
          type = COMMENT;
          text = e.a;
          mark(e);
          return type;
        case XmlPullParser.kProcessingInstruction:
          mergeNext = false;
          type = PROCESSING_INSTRUCTION;
          piTarget = e.a;
          piData = e.b == null ? "" : e.b;
          mark(e);
          return type;
        case XmlPullParser.kSkippedEntity:
          mergeNext = false;
          type = ENTITY_REFERENCE;
          entityName = e.a;
          text = e.b;
          mark(e);
          return type;
        case XmlPullParser.kEndDocument:
          type = END_DOCUMENT;
          finished = true;
          line = -1;
          column = -1;
          offset = -1;
          return type;
        case XmlPullParser.kEntityStart:
          if (isUserEntity(e.a)) {
            mergeNext = true;
            userEntities++;
            entityMark = new int[] {e.line, e.col, e.offset};
          }
          break;
        case XmlPullParser.kEntityEnd:
          if (isUserEntity(e.a)) {
            userEntities--;
          }
          break;
        case XmlPullParser.kCdataStart:
          inCdata = true;
          break;
        case XmlPullParser.kCdataEnd:
          inCdata = false;
          break;
        case XmlPullParser.kCharacters:
        case XmlPullParser.kIgnorableWhitespace:
          characters(e);
          return type;
        default:
          break;
      }
    }
  }

  private static boolean isUserEntity(String name) {
    return !(name.equals("lt") || name.equals("gt") || name.equals("amp") || name.equals("quot")
        || name.equals("apos") || name.startsWith("[") || name.startsWith("%"));
  }

  private static int padFor(XmlPullParser.Ev pending, boolean coalescing) {
    if (pending == null) {
      return 0;
    }
    switch (pending.kind) {
      case XmlPullParser.kEndElement:
        return 2;
      case XmlPullParser.kStartElement:
        return 1;
      case XmlPullParser.kComment:
        return coalescing ? 4 : 1;
      case XmlPullParser.kProcessingInstruction:
        return coalescing ? 2 : 1;
      default:
        return 0;
    }
  }

  private void setLocation(int[] base, int pad) {
    line = base[0];
    column = base[1] + pad;
    offset = base[2] + pad;
  }

  private static int[] baseOf(XmlPullParser.Ev e) {
    return new int[] {e.line, e.col - e.lookahead, e.offset - e.lookahead};
  }

  private void characters(XmlPullParser.Ev first) throws XMLStreamException {
    StringBuilder sb = new StringBuilder(first.a);
    boolean ignorable = first.kind == XmlPullParser.kIgnorableWhitespace;
    boolean cdata = inCdata;
    boolean merge = config.coalescing || mergeNext;
    boolean lastInEntity = userEntities > 0;
    boolean trailing = userEntities == 0;
    XmlPullParser.Ev last = first;
    while (merge) {
      XmlPullParser.Ev e = raw();
      if (e.kind == XmlPullParser.kCharacters || e.kind == XmlPullParser.kIgnorableWhitespace) {
        sb.append(e.a);
        last = e;
        lastInEntity = userEntities > 0;
        if (!lastInEntity) {
          trailing = true;
        }
      } else if (e.kind == XmlPullParser.kEntityStart) {
        if (isUserEntity(e.a)) {
          userEntities++;
          entityMark = new int[] {e.line, e.col, e.offset};
        }
      } else if (e.kind == XmlPullParser.kEntityEnd) {
        if (isUserEntity(e.a)) {
          userEntities--;
        }
      } else if (e.kind == XmlPullParser.kCdataStart || e.kind == XmlPullParser.kCdataEnd) {
        continue;
      } else {
        pending = e;
        break;
      }
    }
    if (config.coalescing) {
      setLocation(lastInEntity && entityMark != null ? entityMark : baseOf(last), padFor(pending, true));
    } else if (merge) {
      if (trailing && !lastInEntity) {
        setLocation(baseOf(last), 0);
      } else {
        setLocation(entityMark, padFor(pending, false));
      }
    } else {
      mark(first);
    }
    mergeNext = false;
    text = sb.toString();
    if (ignorable) {
      type = SPACE;
    } else if (config.reportCdata && !config.coalescing && first.kind == XmlPullParser.kCharacters && cdata) {
      type = CDATA;
    } else {
      type = CHARACTERS;
    }
  }

  private void dtd(XmlPullParser.Ev start) throws XMLStreamException {
    declared.clear();
    while (true) {
      XmlPullParser.Ev e = raw();
      if (e.kind == XmlPullParser.kInternalEntityDecl || e.kind == XmlPullParser.kExternalEntityDecl
          || e.kind == XmlPullParser.kUnparsedEntityDecl) {
        Declared d = new Declared();
        d.name = e.a;
        if (e.kind == XmlPullParser.kInternalEntityDecl) {
          d.value = e.b;
        } else if (e.kind == XmlPullParser.kExternalEntityDecl) {
          d.publicId = e.b;
          d.systemId = e.c;
        } else {
          d.publicId = e.b;
          d.systemId = e.c;
          d.notation = e.d;
        }
        if (!d.name.startsWith("%")) {
          declared.add(d);
        }
      } else if (e.kind == XmlPullParser.kNotationDecl) {
        Declared d = new Declared();
        d.name = e.a;
        d.publicId = e.b;
        d.systemId = e.c;
        d.notationDecl = true;
        declared.add(d);
      } else if (e.kind == XmlPullParser.kDtdEnd) {
        break;
      }
    }
    XmlDtd dtd = parser.dtd;
    if (!config.supportDtd) {
      dtd.generalEntities.clear();
      dtd.elements.clear();
      dtd.attributes.clear();
      dtd.notations.clear();
    }
    type = DTD;
    text = bridge != null && bridge.externalDtdFailed ? StaxResolver.kPlaceholder : dtd.rawText;
    line = dtd.endLine;
    column = dtd.endCol;
    offset = dtd.endOffset;
    mergeNext = false;
  }

  private static String nullIfEmpty(String s) {
    return s == null || s.isEmpty() ? null : s;
  }

  private void startElement(XmlPullParser.Ev e) {
    type = START_ELEMENT;
    mark(e);
    ElementRecord rec = new ElementRecord();
    rec.qname = e.a;
    if (config.namespaceAware) {
      rec.uri = nullIfEmpty(e.uri);
      rec.local = e.local;
      rec.prefix = e.prefix == null ? "" : e.prefix;
      List<String> d = new ArrayList<String>();
      for (int i = 0; i < e.nsDecls.length; i += 2) {
        d.add(nullIfEmpty(e.nsDecls[i]));
        d.add(nullIfEmpty(e.nsDecls[i + 1]));
      }
      rec.decls = d.toArray(new String[0]);
      for (XmlPullParser.Attr a : e.attrs) {
        if (a.nsDecl) {
          continue;
        }
        StaxAttribute sa = new StaxAttribute();
        sa.uri = nullIfEmpty(a.uri);
        sa.local = a.local;
        sa.prefix = a.prefix == null ? "" : a.prefix;
        sa.value = a.value;
        sa.type = a.type;
        sa.specified = a.specified;
        attributes.add(sa);
      }
    } else {
      rec.uri = null;
      rec.local = e.a;
      rec.prefix = "";
      rec.decls = new String[0];
      for (XmlPullParser.Attr a : e.attrs) {
        StaxAttribute sa = new StaxAttribute();
        int colon = a.name.indexOf(':');
        if (colon > 0) {
          sa.prefix = a.name.substring(0, colon);
          sa.local = a.name.substring(colon + 1);
          sa.uri = sa.prefix.equals("xmlns") ? XMLConstants.XMLNS_ATTRIBUTE_NS_URI
              : sa.prefix.equals("xml") ? XMLConstants.XML_NS_URI : null;
        } else {
          sa.local = a.name;
        }
        sa.value = a.value;
        sa.type = a.type;
        sa.specified = a.specified;
        attributes.add(sa);
      }
    }
    elements.add(rec);
    apply(rec);
    scopes.add(rec.decls);
  }

  private void apply(ElementRecord rec) {
    qname = rec.qname;
    uri = rec.uri;
    local = rec.local;
    prefix = rec.prefix;
    decls = rec.decls;
  }

  private void endElement(XmlPullParser.Ev e) {
    type = END_ELEMENT;
    mark(e);
    ElementRecord rec = elements.remove(elements.size() - 1);
    apply(rec);
    popScope = true;
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    if (name.equals(XMLInputFactory.IS_NAMESPACE_AWARE)) {
      return Boolean.valueOf(config.namespaceAware);
    }
    if (name.equals(XMLInputFactory.IS_COALESCING)) {
      return Boolean.valueOf(config.coalescing);
    }
    if (name.equals(XMLInputFactory.IS_VALIDATING)) {
      return Boolean.valueOf(config.validating);
    }
    if (name.equals(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
      return Boolean.valueOf(config.replacingEntities);
    }
    if (name.equals(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)) {
      return Boolean.valueOf(config.externalEntities);
    }
    if (name.equals(XMLInputFactory.SUPPORT_DTD)) {
      return Boolean.valueOf(config.supportDtd);
    }
    if (name.equals(kCdataProperty)) {
      return Boolean.valueOf(config.reportCdata);
    }
    if (name.equals(XMLInputFactory.REPORTER)) {
      return config.reporter;
    }
    if (name.equals(XMLInputFactory.RESOLVER)) {
      return config.resolver;
    }
    if (name.equals(XMLInputFactory.ALLOCATOR)) {
      return config.allocator;
    }
    return null;
  }

  private void requireState(boolean ok, String message) {
    if (!ok) {
      throw new IllegalStateException(message);
    }
  }

  private boolean isElement() {
    return type == START_ELEMENT || type == END_ELEMENT;
  }

  public void require(int eventType, String namespaceURI, String localName) throws XMLStreamException {
    if (eventType != type) {
      throw new XMLStreamException("Event type " + StaxNames.eventName(eventType)
          + " specified did not match with current parser event " + StaxNames.eventName(type));
    }
    if (namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
      throw new XMLStreamException("Namespace URI " + namespaceURI
          + " specified did not match with current namespace URI");
    }
    if (localName != null && !localName.equals(getLocalName())) {
      throw new XMLStreamException("LocalName " + localName + " specified did not match with current local name");
    }
  }

  public String getElementText() throws XMLStreamException {
    if (getEventType() != START_ELEMENT) {
      throw new XMLStreamException("parser must be on START_ELEMENT to read next text", getLocation());
    }
    int eventType = next();
    StringBuilder content = new StringBuilder();
    while (eventType != END_ELEMENT) {
      if (eventType == CHARACTERS || eventType == CDATA || eventType == SPACE || eventType == ENTITY_REFERENCE) {
        content.append(getText());
      } else if (eventType == PROCESSING_INSTRUCTION || eventType == COMMENT) {
      } else if (eventType == END_DOCUMENT) {
        throw new XMLStreamException("unexpected end of document when reading element text content");
      } else if (eventType == START_ELEMENT) {
        throw new XMLStreamException("elementGetText() function expects text only elment but START_ELEMENT was "
            + "encountered.", getLocation());
      } else {
        throw new XMLStreamException("Unexpected event type " + eventType, getLocation());
      }
      eventType = next();
    }
    return content.toString();
  }

  public int nextTag() throws XMLStreamException {
    int eventType = next();
    while ((eventType == CHARACTERS && isWhiteSpace()) || (eventType == CDATA && isWhiteSpace())
        || eventType == SPACE || eventType == PROCESSING_INSTRUCTION || eventType == COMMENT) {
      eventType = next();
    }
    if (eventType != START_ELEMENT && eventType != END_ELEMENT) {
      throw new XMLStreamException("found: " + StaxNames.eventName(eventType)
          + ", expected START_ELEMENT or END_ELEMENT", getLocation());
    }
    return eventType;
  }

  public boolean hasNext() throws XMLStreamException {
    return type != END_DOCUMENT;
  }

  public void close() throws XMLStreamException {
    parser.closeInput();
  }

  public String getNamespaceURI(String prefixName) {
    if (prefixName == null) {
      throw new IllegalArgumentException("prefix cannot be null.");
    }
    return lookup(prefixName);
  }

  private String lookup(String p) {
    if (p.equals(XMLConstants.XML_NS_PREFIX)) {
      return XMLConstants.XML_NS_URI;
    }
    if (p.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
      return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
    }
    for (int i = scopes.size() - 1; i >= 0; i--) {
      String[] d = scopes.get(i);
      for (int k = 0; k < d.length; k += 2) {
        String dp = d[k] == null ? "" : d[k];
        if (dp.equals(p)) {
          return d[k + 1];
        }
      }
    }
    return null;
  }

  public boolean isStartElement() {
    return type == START_ELEMENT;
  }

  public boolean isEndElement() {
    return type == END_ELEMENT;
  }

  public boolean isCharacters() {
    return type == CHARACTERS;
  }

  public boolean isWhiteSpace() {
    if (type != CHARACTERS && type != CDATA) {
      return false;
    }
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
        return false;
      }
    }
    return true;
  }

  private void attributeState(String function) {
    requireState(type == START_ELEMENT || type == ATTRIBUTE,
        "Current state is not among the states START_ELEMENT , ATTRIBUTEvalid for " + function + "()");
  }

  public String getAttributeValue(String namespaceURI, String localName) {
    attributeState("getAttributeValue");
    for (StaxAttribute a : attributes) {
      if (a.local.equals(localName) && (namespaceURI == null || namespaceURI.equals(a.uri))) {
        return a.value;
      }
    }
    return null;
  }

  public int getAttributeCount() {
    attributeState("getAttributeCount");
    return attributes.size();
  }

  private StaxAttribute attribute(int index) {
    return index >= 0 && index < attributes.size() ? attributes.get(index) : null;
  }

  public QName getAttributeName(int index) {
    attributeState("getAttributeName");
    StaxAttribute a = attribute(index);
    return a == null ? null : new QName(a.uri == null ? "" : a.uri, a.local, a.prefix);
  }

  public String getAttributeNamespace(int index) {
    attributeState("getAttributeNamespace");
    StaxAttribute a = attribute(index);
    return a == null ? null : a.uri;
  }

  public String getAttributeLocalName(int index) {
    if (type != START_ELEMENT && type != ATTRIBUTE) {
      throw new IllegalStateException();
    }
    StaxAttribute a = attribute(index);
    return a == null ? null : a.local;
  }

  public String getAttributePrefix(int index) {
    attributeState("getAttributePrefix");
    StaxAttribute a = attribute(index);
    return a == null ? null : a.prefix;
  }

  public String getAttributeType(int index) {
    attributeState("getAttributeType");
    StaxAttribute a = attribute(index);
    return a == null ? null : a.type;
  }

  public String getAttributeValue(int index) {
    attributeState("getAttributeValue");
    StaxAttribute a = attribute(index);
    return a == null ? null : a.value;
  }

  public boolean isAttributeSpecified(int index) {
    attributeState("isAttributeSpecified");
    StaxAttribute a = attribute(index);
    return a == null || a.specified;
  }

  public int getNamespaceCount() {
    requireState(isElement(), "Current event state is " + StaxNames.eventName(type)
        + " is not among the states START_ELEMENT, END_ELEMENT, UNKNOWN_EVENT_TYPE, 13 valid for "
        + "getNamespaceCount().");
    return decls.length / 2;
  }

  public String getNamespacePrefix(int index) {
    requireState(isElement(), "Current state " + StaxNames.eventName(type)
        + " is not among the states START_ELEMENT, END_ELEMENT, UNKNOWN_EVENT_TYPE, 13 valid for "
        + "getNamespacePrefix().");
    if (index >= 0 && index * 2 < decls.length) {
      return decls[index * 2];
    }
    throw new NullPointerException("Cannot invoke \"String.equals(Object)\" because \"prefix\" is null");
  }

  public String getNamespaceURI(int index) {
    requireState(isElement(), "Current state " + StaxNames.eventName(type)
        + " is not among the states START_ELEMENT, END_ELEMENT, UNKNOWN_EVENT_TYPE, 13 valid for "
        + "getNamespaceURI().");
    return index >= 0 && index * 2 < decls.length ? decls[index * 2 + 1] : null;
  }

  public NamespaceContext getNamespaceContext() {
    return new NamespaceContext() {
      public String getNamespaceURI(String p) {
        if (p == null) {
          throw new IllegalArgumentException("prefix cannot be null.");
        }
        return type == END_DOCUMENT ? null : lookup(p);
      }

      public String getPrefix(String ns) {
        if (ns == null) {
          throw new IllegalArgumentException("uri cannot be null.");
        }
        if (type == END_DOCUMENT) {
          return null;
        }
        if (ns.equals(XMLConstants.XML_NS_URI)) {
          return XMLConstants.XML_NS_PREFIX;
        }
        if (ns.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
          return XMLConstants.XMLNS_ATTRIBUTE;
        }
        for (int i = scopes.size() - 1; i >= 0; i--) {
          String[] d = scopes.get(i);
          for (int k = 0; k < d.length; k += 2) {
            if (ns.equals(d[k + 1])) {
              String p = d[k] == null ? "" : d[k];
              if (ns.equals(lookup(p))) {
                return p;
              }
            }
          }
        }
        return null;
      }

      public Iterator<String> getPrefixes(String ns) {
        List<String> out = new ArrayList<String>();
        String p = getPrefix(ns);
        if (p != null) {
          out.add(p);
        }
        return out.iterator();
      }
    };
  }

  public int getEventType() {
    return type;
  }

  private void textState(String function) {
    requireState(type == CHARACTERS || type == COMMENT || type == CDATA || type == SPACE,
        "Current state = " + StaxNames.eventName(type)
        + " is not among the states CHARACTERS , COMMENT , CDATA , SPACE valid for " + function + "() ");
  }

  public String getText() {
    requireState(type == CHARACTERS || type == COMMENT || type == CDATA || type == SPACE
        || type == ENTITY_REFERENCE || type == DTD, "Current state " + StaxNames.eventName(type)
        + " is not among the statesCHARACTERS, COMMENT, CDATA, SPACE, ENTITY_REFERENCE, DTD valid for "
        + "getText() ");
    return text;
  }

  public char[] getTextCharacters() {
    textState("getTextCharacters");
    return text.toCharArray();
  }

  public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
      throws XMLStreamException {
    textState("getTextLength");
    if (sourceStart < 0 || sourceStart > text.length()) {
      throw new ArrayIndexOutOfBoundsException();
    }
    int n = Math.min(length, text.length() - sourceStart);
    text.getChars(sourceStart, sourceStart + n, target, targetStart);
    return n;
  }

  public int getTextStart() {
    textState("getTextStart");
    return 0;
  }

  public int getTextLength() {
    textState("getTextLength");
    return text.length();
  }

  public String getEncoding() {
    return encoding;
  }

  public boolean hasText() {
    return type == CHARACTERS || type == COMMENT || type == CDATA || type == SPACE || type == ENTITY_REFERENCE
        || type == DTD;
  }

  public Location getLocation() {
    return location();
  }

  public QName getName() {
    requireState(isElement(), "Illegal to call getName() when event type is " + StaxNames.eventName(type)
        + ". Valid states are START_ELEMENT, END_ELEMENT");
    return new QName(uri == null ? "" : uri, local, prefix);
  }

  public String getLocalName() {
    if (isElement()) {
      return local;
    }
    if (type == ENTITY_REFERENCE) {
      return entityName;
    }
    throw new IllegalStateException("Method getLocalName() cannot be called for " + StaxNames.eventName(type)
        + " event.");
  }

  public boolean hasName() {
    return isElement();
  }

  public String getNamespaceURI() {
    return isElement() ? uri : null;
  }

  public String getPrefix() {
    return isElement() ? prefix : null;
  }

  public String getVersion() {
    return parser.hasDeclaration && type != END_DOCUMENT ? parser.version : null;
  }

  public boolean isStandalone() {
    return parser.standalone;
  }

  public boolean standaloneSet() {
    return parser.standaloneDeclared;
  }

  public String getCharacterEncodingScheme() {
    return parser.declaredEncoding;
  }

  public String getPITarget() {
    requireState(type == PROCESSING_INSTRUCTION, "Current state of the parser is " + StaxNames.eventName(type)
        + " But Expected state is " + PROCESSING_INSTRUCTION);
    return piTarget;
  }

  public String getPIData() {
    requireState(type == PROCESSING_INSTRUCTION, "Current state of the parser is " + StaxNames.eventName(type)
        + " But Expected state is " + PROCESSING_INSTRUCTION);
    return piData;
  }

  NamespaceContext snapshotContext() {
    java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
    for (String[] d : scopes) {
      for (int k = 0; k < d.length; k += 2) {
        m.put(d[k] == null ? "" : d[k], d[k + 1]);
      }
    }
    m.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    m.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    return StaxAllocator.copy(m);
  }

  String entityReferenceName() {
    return entityName;
  }

  boolean isFinished() {
    return finished;
  }

  String sourceSystemId() {
    return null;
  }
}
