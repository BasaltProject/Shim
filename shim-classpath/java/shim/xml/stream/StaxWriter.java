/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class StaxWriter implements XMLStreamWriter {
  private static final class Element {
    final String qname;
    final Map<String, String> declared = new LinkedHashMap<String, String>();

    Element(String qname) {
      this.qname = qname;
    }
  }

  private Writer out;
  private final CharsetEncoder encoder;
  private final String streamEncoding;
  private final boolean repairing;
  private final boolean escape;
  private int open;
  private boolean openEmpty;
  private final ArrayList<Element> elements = new ArrayList<Element>();
  private final ArrayList<Map<String, String>> scopes = new ArrayList<Map<String, String>>();
  private NamespaceContext external;
  private int generated;
  private boolean closed;

  StaxWriter(Writer out, CharsetEncoder encoder, String streamEncoding, boolean repairing, boolean escape) {
    this.out = out;
    this.encoder = encoder;
    this.streamEncoding = streamEncoding;
    this.repairing = repairing;
    this.escape = escape;
    scopes.add(new LinkedHashMap<String, String>());
  }

  static StaxWriter forStream(OutputStream stream, String encoding, boolean repairing, boolean escape)
      throws XMLStreamException {
    Charset cs;
    try {
      cs = Charset.forName(encoding);
    } catch (RuntimeException e) {
      throw new XMLStreamException(new java.io.UnsupportedEncodingException(encoding));
    }
    return new StaxWriter(stream == null ? null : new OutputStreamWriter(stream, cs), cs.newEncoder(), encoding,
        repairing, escape);
  }

  private void raw(String s) throws XMLStreamException {
    if (out == null) {
      throw new NullPointerException("Cannot invoke \"java.io.Writer.write(int)\" because \"this.fWriter\" is null");
    }
    try {
      out.write(s);
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  private void text(String s, boolean attribute) throws XMLStreamException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); ) {
      int cp = s.codePointAt(i);
      i += Character.charCount(cp);
      if (escape && cp == '&') {
        sb.append("&amp;");
      } else if (escape && cp == '<') {
        sb.append("&lt;");
      } else if (escape && cp == '>') {
        sb.append("&gt;");
      } else if (escape && attribute && cp == '"') {
        sb.append("&quot;");
      } else if (encoder != null && cp >= 0x80 && !encoder.canEncode(new String(Character.toChars(cp)))) {
        sb.append("&#x").append(Integer.toHexString(cp)).append(';');
      } else {
        sb.appendCodePoint(cp);
      }
    }
    raw(sb.toString());
  }

  private void closePending() throws XMLStreamException {
    if (open == 0) {
      return;
    }
    if (open == 1) {
      raw(">");
    } else {
      raw("/>");
    }
    open = 0;
    if (openEmpty) {
      openEmpty = false;
      popElement();
    }
  }

  private void popElement() {
    elements.remove(elements.size() - 1);
    scopes.remove(scopes.size() - 1);
  }

  private Map<String, String> innermost() {
    return scopes.get(scopes.size() - 1);
  }

  private String declaredUri(String prefix) {
    for (int i = elements.size() - 1; i >= 0; i--) {
      String u = elements.get(i).declared.get(prefix);
      if (u != null) {
        return u;
      }
    }
    return null;
  }

  private String declaredPrefix(String uri) {
    for (int i = elements.size() - 1; i >= 0; i--) {
      for (Map.Entry<String, String> e : elements.get(i).declared.entrySet()) {
        if (e.getValue().equals(uri) && uri.equals(declaredUri(e.getKey()))) {
          return e.getKey();
        }
      }
    }
    return null;
  }

  private String lookupUri(String prefix) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      String u = scopes.get(i).get(prefix);
      if (u != null) {
        return u;
      }
    }
    if (external != null) {
      String u = external.getNamespaceURI(prefix);
      if (u != null && !u.isEmpty()) {
        return u;
      }
    }
    return null;
  }

  private String lookupPrefix(String uri) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      for (Map.Entry<String, String> e : scopes.get(i).entrySet()) {
        if (e.getValue().equals(uri) && uri.equals(lookupUri(e.getKey()))) {
          return e.getKey();
        }
      }
    }
    if (external != null) {
      return external.getPrefix(uri);
    }
    return null;
  }

  private void startTag(String prefix, String local, String uri, boolean empty) throws XMLStreamException {
    closePending();
    String qname = prefix == null || prefix.isEmpty() ? local : prefix + ":" + local;
    raw("<" + qname);
    Element el = new Element(qname);
    elements.add(el);
    scopes.add(new LinkedHashMap<String, String>());
    open = empty ? 2 : 1;
    openEmpty = empty;
    if (repairing && uri != null) {
      String p = prefix == null ? "" : prefix;
      if (!uri.equals(declaredUri(p))) {
        declare(p, uri);
      }
    }
  }

  private void declare(String prefix, String uri) throws XMLStreamException {
    if (prefix.isEmpty()) {
      raw(" xmlns=\"");
    } else {
      raw(" xmlns:" + prefix + "=\"");
    }
    text(uri, true);
    raw("\"");
    innermost().put(prefix, uri);
    elements.get(elements.size() - 1).declared.put(prefix, uri);
  }

  public void writeStartElement(String localName) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }
    startTag(null, localName, null, false);
  }

  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }
    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null");
    }
    String prefix = lookupPrefix(namespaceURI);
    if (prefix == null) {
      if (!repairing) {
        throw new XMLStreamException("Prefix cannot be null");
      }
      prefix = generatePrefix();
    }
    startTag(prefix, localName, namespaceURI, false);
  }

  public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }
    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null");
    }
    if (!repairing && prefix == null) {
      throw new XMLStreamException("Prefix cannot be null");
    }
    startTag(prefix, localName, namespaceURI, false);
  }

  public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }
    String prefix = lookupPrefix(namespaceURI);
    if (prefix == null) {
      if (!repairing) {
        throw new XMLStreamException("Prefix cannot be null");
      }
      prefix = generatePrefix();
    }
    startTag(prefix, localName, namespaceURI, true);
  }

  public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }
    if (!repairing && prefix == null) {
      throw new XMLStreamException("Prefix cannot be null");
    }
    startTag(prefix, localName, namespaceURI, true);
  }

  public void writeEmptyElement(String localName) throws XMLStreamException {
    if (localName == null) {
      throw new XMLStreamException("Local Name cannot be null");
    }
    startTag(null, localName, null, true);
  }

  private String generatePrefix() {
    return "zdef" + (generated++);
  }

  public void writeEndElement() throws XMLStreamException {
    closePending();
    if (elements.isEmpty()) {
      throw new XMLStreamException("No element was found to write: java.lang.ArrayIndexOutOfBoundsException: "
          + "Index -1 out of bounds for length 10");
    }
    raw("</" + elements.get(elements.size() - 1).qname + ">");
    popElement();
  }

  public void writeEndDocument() throws XMLStreamException {
    closePending();
    while (!elements.isEmpty()) {
      writeEndElement();
    }
  }

  public void close() throws XMLStreamException {
    if (out != null) {
      try {
        out.flush();
      } catch (IOException e) {
        throw new XMLStreamException(e);
      }
      out = null;
    }
    closed = true;
  }

  public void flush() throws XMLStreamException {
    if (out == null) {
      throw new NullPointerException("Cannot invoke \"java.io.Writer.flush()\" because \"this.fWriter\" is null");
    }
    try {
      out.flush();
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
  }

  private void attribute(String qname, String value) throws XMLStreamException {
    if (open == 0) {
      throw new XMLStreamException("Attribute not associated with any element");
    }
    raw(" " + qname + "=\"");
    if (value == null) {
      throw new NullPointerException("Cannot invoke \"String.length()\" because \"content\" is null");
    }
    text(value, true);
    raw("\"");
  }

  public void writeAttribute(String localName, String value) throws XMLStreamException {
    attribute(localName, value);
  }

  public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
      throws XMLStreamException {
    if (open == 0) {
      throw new XMLStreamException("Attribute not associated with any element");
    }
    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null");
    }
    if (localName == null) {
      throw new XMLStreamException("Local name cannot be null");
    }
    String p = prefix;
    if (repairing && !namespaceURI.isEmpty()) {
      if (p == null || p.isEmpty()) {
        p = declaredPrefix(namespaceURI);
        if (p == null || p.isEmpty()) {
          p = lookupPrefix(namespaceURI);
          if (p == null || p.isEmpty() || p.equals(elements.get(elements.size() - 1).declared.get(p))) {
            p = generatePrefix();
          }
          declare(p, namespaceURI);
        }
      } else if (!namespaceURI.equals(declaredUri(p))) {
        String here = elements.get(elements.size() - 1).declared.get(p);
        if (here != null) {
          p = generatePrefix();
        }
        declare(p, namespaceURI);
      }
    }
    attribute(p == null || p.isEmpty() ? localName : p + ":" + localName, value);
  }

  public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
    if (open == 0) {
      throw new XMLStreamException("Attribute not associated with any element");
    }
    if (namespaceURI == null) {
      throw new XMLStreamException("NamespaceURI cannot be null");
    }
    String prefix = lookupPrefix(namespaceURI);
    if (prefix == null && !repairing) {
      throw new XMLStreamException("Prefix cannot be null");
    }
    writeAttribute(prefix, namespaceURI, localName, value);
  }

  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    if (prefix == null || prefix.isEmpty() || prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
      writeDefaultNamespace(namespaceURI);
      return;
    }
    if (open == 0) {
      throw new XMLStreamException("Invalid state: start tag is not opened at writeNamespace(" + prefix + ", "
          + namespaceURI + ")");
    }
    Element el = elements.get(elements.size() - 1);
    String old = el.declared.get(prefix);
    if (old != null && !old.equals(namespaceURI)) {
      throw new XMLStreamException(prefix + " has been already bound to " + old + ". Rebinding it to "
          + namespaceURI + " is an error");
    }
    if (repairing && old != null) {
      return;
    }
    declare(prefix, namespaceURI);
  }

  public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    if (open == 0) {
      throw new XMLStreamException("Invalid state: start tag is not opened at writeDefaultNamespace("
          + namespaceURI + ")");
    }
    Element el = elements.get(elements.size() - 1);
    String old = el.declared.get("");
    if (old != null && !old.equals(namespaceURI)) {
      throw new XMLStreamException("xmlns has been already bound to " + old + ". Rebinding it to " + namespaceURI
          + " is an error");
    }
    if (repairing && old != null) {
      return;
    }
    declare("", namespaceURI);
  }

  public void writeComment(String data) throws XMLStreamException {
    closePending();
    raw("<!--" + (data == null ? "" : data) + "-->");
  }

  public void writeProcessingInstruction(String target) throws XMLStreamException {
    writeProcessingInstruction(target, null);
  }

  public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
    if (target == null) {
      throw new XMLStreamException("PI target cannot be null");
    }
    closePending();
    raw("<?" + target + (data != null ? " " + data : "") + "?>");
  }

  public void writeCData(String data) throws XMLStreamException {
    if (data == null) {
      throw new XMLStreamException("cdata cannot be null");
    }
    closePending();
    raw("<![CDATA[" + data + "]]>");
  }

  public void writeDTD(String dtd) throws XMLStreamException {
    closePending();
    raw(dtd);
  }

  public void writeEntityRef(String name) throws XMLStreamException {
    closePending();
    raw("&" + name + ";");
  }

  void writeDocumentDeclaration(String version, String encoding, boolean standaloneSet, boolean standalone)
      throws XMLStreamException {
    raw("<?xml version=\"" + version + "\"" + (encoding != null ? " encoding=\"" + encoding + "\"" : "")
        + (standaloneSet ? " standalone=\"" + (standalone ? "yes" : "no") + "\"" : "") + "?>");
  }

  public void writeStartDocument() throws XMLStreamException {
    raw("<?xml version=\"1.0\" ?>");
  }

  public void writeStartDocument(String version) throws XMLStreamException {
    if (version == null) {
      writeStartDocument();
      return;
    }
    raw("<?xml version=\"" + version + "\"?>");
  }

  public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    if (encoding == null && version == null) {
      writeStartDocument();
      return;
    }
    if (encoding == null) {
      writeStartDocument(version);
      return;
    }
    if (streamEncoding != null && !streamEncoding.equalsIgnoreCase(encoding)) {
      throw new XMLStreamException("Underlying stream encoding '" + streamEncoding
          + "' and input paramter for writeStartDocument() method '" + encoding + "' do not match.");
    }
    raw("<?xml version=\"" + (version == null ? "1.0" : version) + "\" encoding=\"" + encoding + "\"?>");
  }

  public void writeCharacters(String text) throws XMLStreamException {
    closePending();
    if (text != null && !text.isEmpty()) {
      text(text, false);
    }
  }

  public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
    closePending();
    if (len > 0) {
      text(new String(text, start, len), false);
    }
  }

  public String getPrefix(String uri) throws XMLStreamException {
    return lookupPrefix(uri);
  }

  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    if (prefix == null) {
      throw new XMLStreamException("Prefix cannot be null");
    }
    if (uri == null) {
      throw new XMLStreamException("URI cannot be null");
    }
    innermost().put(prefix, uri);
  }

  public void setDefaultNamespace(String uri) throws XMLStreamException {
    if (uri != null) {
      innermost().put("", uri);
    }
  }

  public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    external = context;
  }

  public NamespaceContext getNamespaceContext() {
    return new NamespaceContext() {
      public String getNamespaceURI(String prefix) {
        if (prefix == null) {
          throw new IllegalArgumentException("prefix cannot be null.");
        }
        return lookupUri(prefix);
      }

      public String getPrefix(String uri) {
        if (uri == null) {
          throw new IllegalArgumentException("uri cannot be null.");
        }
        return lookupPrefix(uri);
      }

      public Iterator<String> getPrefixes(String uri) {
        List<String> out = new ArrayList<String>();
        String p = lookupPrefix(uri);
        if (p != null) {
          out.add(p);
        }
        return out.iterator();
      }
    };
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals(XMLOutputFactory.IS_REPAIRING_NAMESPACES)) {
      return Boolean.valueOf(repairing);
    }
    throw new IllegalArgumentException("Property '" + name + "' is not supported");
  }
}
