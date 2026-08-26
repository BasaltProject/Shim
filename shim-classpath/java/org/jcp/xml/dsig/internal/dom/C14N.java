/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public final class C14N {
  static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";
  static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

  private final boolean exclusive;
  private boolean c14n11;
  private final boolean comments;
  private final Set<String> inclusivePrefixes;
  private final Set<Node> subset;
  private final OutputStream out;
  private final List<Map<String, String>> rendered = new ArrayList<Map<String, String>>();

  private C14N(boolean exclusive, boolean comments, Set<String> inclusivePrefixes, Set<Node> subset,
      OutputStream out) {
    this.exclusive = exclusive;
    this.comments = comments;
    this.inclusivePrefixes = inclusivePrefixes;
    this.subset = subset;
    this.out = out;
  }

  public static void canonicalize(Node root, Set<Node> subset, boolean exclusive, boolean comments,
      Set<String> inclusivePrefixes, OutputStream out) throws IOException {
    canonicalize(root, subset, exclusive, false, comments, inclusivePrefixes, out);
  }

  public static void canonicalize(Node root, Set<Node> subset, boolean exclusive, boolean c14n11, boolean comments,
      Set<String> inclusivePrefixes, OutputStream out) throws IOException {
    C14N c = new C14N(exclusive, comments, inclusivePrefixes == null ? Collections.<String>emptySet()
        : inclusivePrefixes, subset, out);
    c.c14n11 = c14n11;
    c.rendered.add(new HashMap<String, String>());
    c.process(root, root.getNodeType() == Node.DOCUMENT_NODE);
  }

  public static byte[] canonicalize(Node root, Set<Node> subset, boolean exclusive, boolean comments,
      Set<String> inclusivePrefixes) throws IOException {
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    canonicalize(root, subset, exclusive, comments, inclusivePrefixes, bos);
    return bos.toByteArray();
  }

  private boolean included(Node n) {
    return subset == null || subset.contains(n);
  }

  private void write(String s) throws IOException {
    out.write(s.getBytes(StandardCharsets.UTF_8));
  }

  private void process(Node n, boolean docLevel) throws IOException {
    switch (n.getNodeType()) {
      case Node.DOCUMENT_NODE: {
        boolean seenRoot = false;
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
          if (c.getNodeType() == Node.ELEMENT_NODE) {
            process(c, false);
            seenRoot = true;
          } else if (c.getNodeType() == Node.COMMENT_NODE || c.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            if (!included(c) || (c.getNodeType() == Node.COMMENT_NODE && !comments)) {
              continue;
            }
            if (seenRoot) {
              write("\n");
            }
            leaf(c);
            if (!seenRoot) {
              write("\n");
            }
          }
        }
        break;
      }
      case Node.DOCUMENT_FRAGMENT_NODE:
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
          process(c, false);
        }
        break;
      case Node.ELEMENT_NODE:
        element((Element) n);
        break;
      case Node.TEXT_NODE:
      case Node.CDATA_SECTION_NODE:
        if (included(n)) {
          text(n.getNodeValue());
        }
        break;
      case Node.COMMENT_NODE:
        if (included(n) && comments) {
          leaf(n);
        }
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        if (included(n)) {
          leaf(n);
        }
        break;
      case Node.ATTRIBUTE_NODE: {
        Attr a = (Attr) n;
        if (included(n)) {
          write(" " + a.getName() + "=\"");
          attrValue(a.getValue());
          write("\"");
        }
        break;
      }
      default:
        break;
    }
  }

  private void leaf(Node c) throws IOException {
    if (c.getNodeType() == Node.COMMENT_NODE) {
      write("<!--" + c.getNodeValue() + "-->");
    } else {
      ProcessingInstruction pi = (ProcessingInstruction) c;
      String data = pi.getData();
      write("<?" + pi.getTarget() + (data == null || data.isEmpty() ? "" : " " + data) + "?>");
    }
  }

  private void text(String s) throws IOException {
    StringBuilder b = new StringBuilder(s.length() + 8);
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      switch (ch) {
        case '&':
          b.append("&amp;");
          break;
        case '<':
          b.append("&lt;");
          break;
        case '>':
          b.append("&gt;");
          break;
        case '\r':
          b.append("&#xD;");
          break;
        default:
          b.append(ch);
      }
    }
    write(b.toString());
  }

  private void attrValue(String s) throws IOException {
    StringBuilder b = new StringBuilder(s.length() + 8);
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      switch (ch) {
        case '&':
          b.append("&amp;");
          break;
        case '<':
          b.append("&lt;");
          break;
        case '"':
          b.append("&quot;");
          break;
        case '\t':
          b.append("&#x9;");
          break;
        case '\n':
          b.append("&#xA;");
          break;
        case '\r':
          b.append("&#xD;");
          break;
        default:
          b.append(ch);
      }
    }
    write(b.toString());
  }

  private static String nsPrefix(Attr a) {
    String name = a.getName();
    if (name.equals("xmlns")) {
      return "";
    }
    return name.substring(6);
  }

  private static boolean isNsDecl(Attr a) {
    String name = a.getName();
    return name.equals("xmlns") || name.startsWith("xmlns:");
  }

  private Map<String, String> inScope(Element e) {
    Map<String, String> ns = new TreeMap<String, String>();
    List<Element> chain = new ArrayList<Element>();
    for (Node n = e; n != null && n.getNodeType() == Node.ELEMENT_NODE; n = n.getParentNode()) {
      chain.add((Element) n);
    }
    for (int i = chain.size() - 1; i >= 0; i--) {
      NamedNodeMap attrs = chain.get(i).getAttributes();
      for (int j = 0; j < attrs.getLength(); j++) {
        Attr a = (Attr) attrs.item(j);
        if (isNsDecl(a)) {
          String p = nsPrefix(a);
          String v = a.getValue();
          if (p.isEmpty() && v.isEmpty()) {
            ns.remove("");
          } else if (!v.isEmpty() || p.isEmpty()) {
            ns.put(p, v);
          }
        }
      }
    }
    return ns;
  }

  private void element(Element e) throws IOException {
    boolean in = included(e);
    Map<String, String> parentRendered = rendered.get(rendered.size() - 1);
    if (!in) {
      rendered.add(parentRendered);
      for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
        process(c, false);
      }
      rendered.remove(rendered.size() - 1);
      return;
    }
    Map<String, String> inScope = inScope(e);
    List<Attr> attrs = new ArrayList<Attr>();
    NamedNodeMap nm = e.getAttributes();
    for (int i = 0; i < nm.getLength(); i++) {
      Attr a = (Attr) nm.item(i);
      if (!isNsDecl(a) && included(a)) {
        attrs.add(a);
      }
    }
    if (!exclusive) {
      Node p = e.getParentNode();
      boolean ownXml = false;
      if (!c14n11) {
        for (Attr a : attrs) {
          if (XML_NS.equals(a.getNamespaceURI())) {
            ownXml = true;
          }
        }
      }
      if (p != null && p.getNodeType() == Node.ELEMENT_NODE && (!included(p) || ownXml)) {
        Map<String, Attr> inherited = new TreeMap<String, Attr>();
        for (Node anc = p; anc != null && anc.getNodeType() == Node.ELEMENT_NODE; anc = anc.getParentNode()) {
          NamedNodeMap am = anc.getAttributes();
          for (int i = 0; i < am.getLength(); i++) {
            Attr a = (Attr) am.item(i);
            if (XML_NS.equals(a.getNamespaceURI()) && !inherited.containsKey(a.getLocalName())
                && e.getAttributeNodeNS(XML_NS, a.getLocalName()) == null) {
              inherited.put(a.getLocalName(), a);
            }
          }
        }
        attrs.addAll(inherited.values());
      }
    }
    Collections.sort(attrs, new Comparator<Attr>() {
      public int compare(Attr a, Attr b) {
        String ua = a.getNamespaceURI() == null ? "" : a.getNamespaceURI();
        String ub = b.getNamespaceURI() == null ? "" : b.getNamespaceURI();
        int c = ua.compareTo(ub);
        if (c != 0) {
          return c;
        }
        String la = a.getLocalName() == null ? a.getName() : a.getLocalName();
        String lb = b.getLocalName() == null ? b.getName() : b.getLocalName();
        return la.compareTo(lb);
      }
    });
    Map<String, String> toRender = new TreeMap<String, String>();
    Map<String, String> newRendered = new HashMap<String, String>(parentRendered);
    if (!exclusive) {
      for (Map.Entry<String, String> en : inScope.entrySet()) {
        String old = parentRendered.get(en.getKey());
        if (en.getKey().isEmpty() && en.getValue().isEmpty()) {
          continue;
        }
        if (old == null || !old.equals(en.getValue())) {
          if (old == null && en.getKey().isEmpty() && en.getValue().isEmpty()) {
            continue;
          }
          toRender.put(en.getKey(), en.getValue());
        }
      }
      if (!inScope.containsKey("") && parentRendered.containsKey("") && !parentRendered.get("").isEmpty()) {
        toRender.put("", "");
      }
    } else {
      Set<String> utilized = new java.util.TreeSet<String>();
      String ep = e.getPrefix();
      utilized.add(ep == null ? "" : ep);
      for (Attr a : attrs) {
        String ap = a.getPrefix();
        if (ap != null && !ap.isEmpty() && !ap.equals("xml")) {
          utilized.add(ap);
        }
      }
      for (String ip : inclusivePrefixes) {
        utilized.add(ip.equals("#default") ? "" : ip);
      }
      for (String p : utilized) {
        String uri = inScope.get(p);
        String old = parentRendered.get(p);
        if (uri == null) {
          if (p.isEmpty() && old != null && !old.isEmpty()) {
            toRender.put("", "");
          }
          continue;
        }
        if (old == null || !old.equals(uri)) {
          toRender.put(p, uri);
        }
      }
    }
    for (Map.Entry<String, String> en : toRender.entrySet()) {
      newRendered.put(en.getKey(), en.getValue());
      if (!exclusive && !c14n11 && !en.getValue().isEmpty() && !en.getValue().contains(":")) {
        throw new IOException("Element " + e.getLocalName() + " has a relative namespace: xmlns"
            + (en.getKey().isEmpty() ? "" : ":" + en.getKey()) + "=\"" + en.getValue() + "\"");
      }
    }
    StringBuilder tag = new StringBuilder();
    tag.append('<').append(e.getTagName());
    write(tag.toString());
    for (Map.Entry<String, String> en : toRender.entrySet()) {
      String k = en.getKey();
      write(" " + (k.isEmpty() ? "xmlns" : "xmlns:" + k) + "=\"");
      attrValue(en.getValue());
      write("\"");
    }
    for (Attr a : attrs) {
      write(" " + a.getName() + "=\"");
      attrValue(a.getValue());
      write("\"");
    }
    write(">");
    rendered.add(newRendered);
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      process(c, false);
    }
    rendered.remove(rendered.size() - 1);
    write("</" + e.getTagName() + ">");
  }
}
