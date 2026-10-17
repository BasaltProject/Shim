/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.ser;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class XmlSerializer implements ContentHandler, LexicalHandler {
  private static final class Node {
    static final int kElement = 1;
    static final int kText = 2;
    static final int kCdata = 3;
    static final int kComment = 4;
    static final int kPi = 5;
    static final int kRaw = 6;
    int kind;
    String name;
    String key;
    String data;
    List<String[]> attrs;
    List<Node> children = new ArrayList<Node>();
  }

  private final Writer writer;
  private CharsetEncoder encoder;
  private String encoding = "UTF-8";
  private String version = "1.0";
  private String method = "xml";
  private boolean omitDeclaration;
  private String standalone;
  private boolean indent;
  private int indentAmount = 4;
  private String doctypePublic;
  private String doctypeSystem;
  private String mediaType;
  private final Set<String> cdataElements = new HashSet<String>();
  private boolean standaloneNoDeclaration;
  private boolean declarationForDocument = true;

  private String rawDoctype;
  private boolean lsMode;
  private boolean skipBlankText;
  private boolean started;
  private boolean declWritten;
  private boolean docTypeWritten;
  private final List<String[]> pendingNs = new ArrayList<String[]>();
  private final List<Node> stack = new ArrayList<Node>();
  private final List<Node> top = new ArrayList<Node>();
  private boolean inCdata;
  private StringBuilder cdataBuffer;
  private boolean rootStarted;
  private boolean htmlHeadSeen;
  private boolean startTagOpen;
  private String openTag;
  private final List<String> directStack = new ArrayList<String>();
  private final List<String> directKeys = new ArrayList<String>();

  private static String key(String uri, String local, String qName) {
    if (uri == null || uri.isEmpty()) {
      return qName;
    }
    return "{" + uri + "}" + local;
  }

  public XmlSerializer(Writer writer) {
    this.writer = writer;
  }

  public void setEncoding(String enc) {
    try {
      Charset cs = Charset.forName(enc);
      this.encoder = cs.newEncoder();
      this.encoding = enc;
    } catch (RuntimeException e) {
      this.encoder = Charset.forName("UTF-8").newEncoder();
      this.encoding = "UTF-8";
    }
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setOmitDeclaration(boolean omit) {
    this.omitDeclaration = omit;
  }

  public void setStandalone(String standalone) {
    this.standalone = standalone;
  }

  public void setIndent(boolean indent) {
    this.indent = indent;
  }

  public void setIndentAmount(int amount) {
    this.indentAmount = amount;
  }

  public void setDoctype(String publicId, String systemId) {
    this.doctypePublic = publicId;
    this.doctypeSystem = systemId;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public void addCdataElement(String name) {
    cdataElements.add(name);
  }

  private boolean hoistDefault = true;
  private String documentStandalone;
  private boolean documentStandaloneTrue;

  public void setHoistDefaultNamespace(boolean value) {
    this.hoistDefault = value;
  }

  public void setDocumentStandaloneIsDefault(boolean value) {
    this.documentStandaloneTrue = value;
  }

  public void setDocumentStandalone(String value) {
    this.documentStandalone = value;
  }

  public void setDeclarationForDocument(boolean value) {
    this.declarationForDocument = value;
  }

  private boolean wideEncoding() {
    String e = encoding.toUpperCase();
    return e.startsWith("UTF-16") || e.startsWith("UTF-32") || e.startsWith("UTF16") || e.startsWith("UTF32");
  }

  private boolean canEncode(int cp) {
    if (encoder == null) {
      return true;
    }
    if (cp < 0x80) {
      return true;
    }
    return encoder.canEncode(new String(Character.toChars(cp)));
  }

  private void raw(String s) throws SAXException {
    try {
      writer.write(s);
    } catch (IOException e) {
      throw new SAXException(e);
    }
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startDocument() throws SAXException {
    started = true;
  }

  public void endDocument() throws SAXException {
    if (indent) {
      flushTop();
    } else if (isXml()) {
      closeOpenTag();
      writeDeclaration();
    }
    try {
      if (indent && method.equals("xml")) {
        writer.write("\n");
      }
      writer.flush();
    } catch (IOException e) {
      throw new SAXException(e);
    }
  }

  private boolean isXml() {
    return method.equals("xml");
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    pendingNs.add(new String[] {prefix, uri});
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  private void writeDeclaration() throws SAXException {
    if (declWritten) {
      return;
    }
    declWritten = true;
    if (!isXml() || omitDeclaration) {
      return;
    }
    String sa = "";
    String effective = documentStandalone != null ? documentStandalone : standalone;
    if (documentStandalone != null && documentStandaloneTrue) {
      effective = standalone;
    }
    if (effective != null && !effective.isEmpty()) {
      sa = " standalone=\"" + effective + "\"";
    }
    raw("<?xml version=\"" + version + "\" encoding=\"" + encoding + "\"" + sa + "?>");
    if (indent && (effective != null || doctypeSystem != null)) {
      raw("\n");
    }
  }

  private void writeDoctype(String rootName) throws SAXException {
    if (docTypeWritten) {
      return;
    }
    docTypeWritten = true;
    if (rawDoctype != null) {
      raw(rawDoctype);
      return;
    }
    if (doctypeSystem == null) {
      return;
    }
    StringBuilder sb = new StringBuilder("<!DOCTYPE ").append(rootName);
    if (doctypePublic != null) {
      sb.append(" PUBLIC \"").append(doctypePublic).append("\"");
      if (doctypeSystem != null) {
        sb.append(" \"").append(doctypeSystem).append("\"");
      }
    } else {
      sb.append(" SYSTEM \"").append(doctypeSystem).append("\"");
    }
    sb.append(">\n");
    raw(sb.toString());
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    List<String[]> attrs = new ArrayList<String[]>();
    Set<String> inAtts = new HashSet<String>();
    for (int i = 0; i < atts.getLength(); i++) {
      inAtts.add(atts.getQName(i));
    }
    for (String[] ns : pendingNs) {
      String n = ns[0].isEmpty() ? "xmlns" : "xmlns:" + ns[0];
      if (!inAtts.contains(n)) {
        attrs.add(new String[] {n, ns[1]});
      }
    }
    pendingNs.clear();
    for (int i = 0; i < atts.getLength(); i++) {
      attrs.add(new String[] {atts.getQName(i), atts.getValue(i)});
    }
    if (hoistDefault && !rootStarted && stack.isEmpty() && directStack.isEmpty()) {
      for (int i = 1; i < attrs.size(); i++) {
        if (attrs.get(i)[0].equals("xmlns")) {
          attrs.add(0, attrs.remove(i));
          break;
        }
      }
    }
    if (indent) {
      Node n = new Node();
      n.kind = Node.kElement;
      n.name = qName;
      n.key = key(uri, localName, qName);
      n.attrs = attrs;
      if (stack.isEmpty()) {
        top.add(n);
      } else {
        stack.get(stack.size() - 1).children.add(n);
      }
      stack.add(n);
      return;
    }
    closeOpenTag();
    if (method.equals("text")) {
      directStack.add(qName);
      return;
    }
    directKeys.add(key(uri, localName, qName));
    if (!rootStarted) {
      rootStarted = true;
      writeDeclaration();
      writeDoctype(qName);
    }
    writeStartTag(qName, attrs);
    startTagOpen = true;
    directStack.add(qName);
  }

  private void closeOpenTag() throws SAXException {
    if (startTagOpen) {
      raw(">");
      startTagOpen = false;
    }
  }

  private void writeStartTag(String qName, List<String[]> attrs) throws SAXException {
    StringBuilder sb = new StringBuilder("<").append(qName);
    for (String[] a : attrs) {
      sb.append(' ').append(a[0]).append("=\"");
      escape(sb, a[1], true);
      sb.append('"');
    }
    raw(sb.toString());
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (indent) {
      stack.remove(stack.size() - 1);
      return;
    }
    directStack.remove(directStack.size() - 1);
    if (method.equals("text")) {
      return;
    }
    directKeys.remove(directKeys.size() - 1);
    if (startTagOpen) {
      raw("/>");
      startTagOpen = false;
    } else {
      raw("</" + qName + ">");
    }
  }

  private void escape(StringBuilder sb, String s, boolean attribute) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
      case '&':
        sb.append("&amp;");
        continue;
      case '<':
        sb.append("&lt;");
        continue;
      case '>':
        sb.append("&gt;");
        continue;
      case '"':
        if (attribute) {
          sb.append("&quot;");
          continue;
        }
        break;
      case '\n':
        if (attribute) {
          sb.append("&#10;");
          continue;
        }
        break;
      case '\r':
        sb.append("&#13;");
        continue;
      case '\t':
        if (attribute) {
          sb.append("&#9;");
          continue;
        }
        break;
      default:
        break;
      }
      int cp = c;
      if (Character.isHighSurrogate(c) && i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
        cp = Character.toCodePoint(c, s.charAt(i + 1));
        i++;
      }
      if ((cp >= 0x10000 && !wideEncoding()) || (cp >= 0x80 && !canEncode(cp))) {
        sb.append("&#").append(cp).append(';');
      } else {
        sb.appendCodePoint(cp);
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    String s = new String(ch, start, length);
    if (inCdata && !indent && method.equals("text")) {
      raw(s);
      return;
    }
    if (inCdata && !indent) {
      cdataBuffer.append(s);
      return;
    }
    if (inCdata) {
      addLeaf(Node.kCdata, null, s);
      return;
    }
    if (indent) {
      addLeaf(Node.kText, null, s);
      return;
    }
    closeOpenTag();
    if (method.equals("text")) {
      raw(s);
      return;
    }
    writeDeclaration();
    if (!directKeys.isEmpty() && cdataElements.contains(directKeys.get(directKeys.size() - 1))) {
      raw(cdataSection(s));
      return;
    }
    StringBuilder sb = new StringBuilder();
    escape(sb, s, false);
    raw(sb.toString());
  }

  private String cdataSection(String s) {
    return "<![CDATA[" + s.replace("]]>", "]]]]><![CDATA[>") + "]]>";
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    characters(ch, start, length);
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (method.equals("text")) {
      return;
    }
    if (indent) {
      addLeaf(Node.kPi, target, data);
      return;
    }
    closeOpenTag();
    if (!rootStarted && directStack.isEmpty()) {
      writeDeclaration();
    }
    raw("<?" + target + (data == null || data.isEmpty() ? "" : " " + data) + "?>");
  }

  public void skippedEntity(String name) throws SAXException {
    if (indent) {
      addLeaf(Node.kRaw, null, "&" + name + ";");
      return;
    }
    closeOpenTag();
    if (!rootStarted && directStack.isEmpty()) {
      writeDeclaration();
    }
    raw("&" + name + ";");
  }

  public void setRawDoctype(String text) {
    this.rawDoctype = text;
  }

  public void setLsMode(boolean value) {
    this.lsMode = value;
  }

  public void setSkipBlankText(boolean value) {
    this.skipBlankText = value;
  }

  public void startDTD(String name, String publicId, String systemId) throws SAXException {
  }

  public void endDTD() throws SAXException {
  }

  public void startEntity(String name) throws SAXException {
  }

  public void endEntity(String name) throws SAXException {
  }

  public void startCDATA() throws SAXException {
    inCdata = true;
    cdataBuffer = new StringBuilder();
  }

  public void endCDATA() throws SAXException {
    inCdata = false;
    if (!indent && !method.equals("text")) {
      closeOpenTag();
      if (!rootStarted && directStack.isEmpty()) {
        writeDeclaration();
      }
      raw(cdataSection(cdataBuffer.toString()));
    }
  }

  public void comment(char[] ch, int start, int length) throws SAXException {
    if (method.equals("text")) {
      return;
    }
    String s = new String(ch, start, length);
    if (indent) {
      addLeaf(Node.kComment, null, s);
      return;
    }
    closeOpenTag();
    if (!rootStarted && directStack.isEmpty()) {
      writeDeclaration();
    }
    raw("<!--" + s + "-->");
  }

  private void addLeaf(int kind, String name, String data) {
    if (skipBlankText && kind == Node.kText && data.trim().isEmpty() && !stack.isEmpty()) {
      return;
    }
    Node n = new Node();
    n.kind = kind;
    n.name = name;
    n.data = data;
    if (stack.isEmpty()) {
      top.add(n);
    } else {
      List<Node> kids = stack.get(stack.size() - 1).children;
      if (kind == Node.kText && !kids.isEmpty() && kids.get(kids.size() - 1).kind == Node.kText) {
        kids.get(kids.size() - 1).data += data;
      } else {
        kids.add(n);
      }
    }
  }

  private void flushTop() throws SAXException {
    writeDeclaration();
    boolean first = true;
    for (Node n : top) {
      if (n.kind == Node.kElement) {
        writeDoctype(n.name);
      }
      StringBuilder sb = new StringBuilder();
      writeNode(sb, n, 0);
      raw(sb.toString());
    }
  }

  private boolean inlineText(Node e) {
    return e.children.size() == 1 && (e.children.get(0).kind == Node.kText || e.children.get(0).kind == Node.kCdata
        || lsMode && e.children.get(0).kind == Node.kRaw);
  }

  private void pad(StringBuilder sb, int depth) {
    for (int i = 0; i < depth * Math.max(indentAmount, 0); i++) {
      sb.append(' ');
    }
  }

  private void writeNode(StringBuilder sb, Node n, int depth) {
    switch (n.kind) {
    case Node.kElement: {
      sb.append('<').append(n.name);
      for (String[] a : n.attrs) {
        sb.append(' ').append(a[0]).append("=\"");
        escape(sb, a[1], true);
        sb.append('"');
      }
      if (n.children.isEmpty()) {
        sb.append("/>");
      } else if (inlineText(n)) {
        sb.append('>');
        Node c = n.children.get(0);
        if (c.kind == Node.kRaw) {
          sb.append(c.data);
        } else if (c.kind == Node.kText) {
          if (cdataElements.contains(n.key)) {
            sb.append(cdataSection(c.data));
          } else {
            escape(sb, c.data, false);
          }
        } else {
          sb.append(cdataSection(c.data));
        }
        sb.append("</").append(n.name).append('>');
      } else {
        sb.append('>');
        for (Node c : n.children) {
          sb.append('\n');
          pad(sb, depth + 1);
          writeChild(sb, c, depth + 1);
        }
        sb.append('\n');
        pad(sb, depth);
        sb.append("</").append(n.name).append('>');
      }
      return;
    }
    default:
      writeChild(sb, n, depth);
    }
  }

  private void writeChild(StringBuilder sb, Node c, int depth) {
    switch (c.kind) {
    case Node.kElement:
      writeNode(sb, c, depth);
      break;
    case Node.kText: {
      String t = c.data;
      int i = 0;
      while (i < t.length() && t.charAt(i) == '\n') {
        i++;
      }
      escape(sb, t.substring(i), false);
      break;
    }
    case Node.kCdata:
      sb.append(cdataSection(c.data));
      break;
    case Node.kComment:
      sb.append("<!--").append(c.data).append("-->");
      break;
    case Node.kPi:
      sb.append("<?").append(c.name).append(c.data == null || c.data.isEmpty() ? "" : " " + c.data).append("?>");
      break;
    case Node.kRaw:
      sb.append(c.data);
      break;
    default:
      break;
    }
  }
}
