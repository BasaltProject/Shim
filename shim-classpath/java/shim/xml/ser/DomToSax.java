/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.ser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public final class DomToSax {
  private final ContentHandler handler;
  private final LexicalHandler lexical;
  private final boolean fixup;
  private final List<Map<String, String>> scopes = new ArrayList<Map<String, String>>();
  private int nextPrefix = 1;

  public DomToSax(ContentHandler handler, LexicalHandler lexical, boolean fixup) {
    this.handler = handler;
    this.lexical = lexical;
    this.fixup = fixup;
  }

  public void parse(Node node) throws SAXException {
    scopes.add(new HashMap<String, String>());
    visit(node);
  }

  private String lookup(String prefix) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      String uri = scopes.get(i).get(prefix);
      if (uri != null) {
        return uri;
      }
    }
    if (prefix.equals("xml")) {
      return "http://www.w3.org/XML/1998/namespace";
    }
    return null;
  }

  private String prefixFor(String uri) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      for (Map.Entry<String, String> e : scopes.get(i).entrySet()) {
        if (e.getValue().equals(uri) && uri.equals(lookup(e.getKey()))) {
          return e.getKey();
        }
      }
    }
    return null;
  }

  private void visit(Node node) throws SAXException {
    switch (node.getNodeType()) {
    case Node.DOCUMENT_NODE:
      handler.startDocument();
      for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
        visit(c);
      }
      handler.endDocument();
      break;
    case Node.DOCUMENT_FRAGMENT_NODE:
      for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
        visit(c);
      }
      break;
    case Node.ELEMENT_NODE:
      element(node);
      break;
    case Node.TEXT_NODE: {
      char[] c = node.getNodeValue().toCharArray();
      handler.characters(c, 0, c.length);
      break;
    }
    case Node.CDATA_SECTION_NODE: {
      char[] c = node.getNodeValue().toCharArray();
      if (lexical != null) {
        lexical.startCDATA();
      }
      handler.characters(c, 0, c.length);
      if (lexical != null) {
        lexical.endCDATA();
      }
      break;
    }
    case Node.COMMENT_NODE: {
      char[] c = node.getNodeValue().toCharArray();
      if (lexical != null) {
        lexical.comment(c, 0, c.length);
      }
      break;
    }
    case Node.PROCESSING_INSTRUCTION_NODE:
      handler.processingInstruction(node.getNodeName(), node.getNodeValue());
      break;
    case Node.ENTITY_REFERENCE_NODE:
      if (lexical != null) {
        lexical.startEntity(node.getNodeName());
      }
      for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
        visit(c);
      }
      if (lexical != null) {
        lexical.endEntity(node.getNodeName());
      }
      break;
    default:
      break;
    }
  }

  private boolean declare(Map<String, String> scope, List<String> declared, String prefix, String uri)
      throws SAXException {
    if (uri.equals(lookup(prefix)) || (prefix.isEmpty() && uri.isEmpty() && lookup("") == null)) {
      return false;
    }
    scope.put(prefix, uri);
    handler.startPrefixMapping(prefix, uri);
    declared.add(prefix);
    return true;
  }

  private static void addDecl(AttributesImpl atts, String prefix, String uri) {
    if (atts.getIndex(prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix) >= 0) {
      return;
    }
    atts.addAttribute("", prefix, prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix, "CDATA", uri);
  }

  private void element(Node node) throws SAXException {
    Map<String, String> scope = new HashMap<String, String>();
    scopes.add(scope);
    List<String> declared = new ArrayList<String>();
    NamedNodeMap map = node.getAttributes();
    List<Attr> regular = new ArrayList<Attr>();
    List<Attr> declarations = new ArrayList<Attr>();
    for (int i = 0; i < map.getLength(); i++) {
      Attr a = (Attr) map.item(i);
      String name = a.getNodeName();
      if (name.equals("xmlns") || name.startsWith("xmlns:")) {
        declarations.add(a);
      } else {
        regular.add(a);
      }
    }
    String ns = node.getNamespaceURI();
    String prefix = node.getPrefix();
    String p = prefix == null ? "" : prefix;
    String ownUri = null;
    boolean ownDecl = false;
    if (node.getLocalName() != null) {
      if (ns != null) {
        ownUri = ns;
        ownDecl = true;
      } else {
        ownUri = "";
        ownDecl = true;
      }
    }
    boolean ownFirst = !declarations.isEmpty();
    AttributesImpl atts = new AttributesImpl();
    if (ownDecl && ownFirst) {
      if (declare(scope, declared, p, ownUri)) {
        addDecl(atts, p, ownUri);
      }
    }
    for (Attr a : declarations) {
      String name = a.getNodeName();
      String ap = name.equals("xmlns") ? "" : name.substring(6);
      if (declare(scope, declared, ap, a.getValue())) {
        addDecl(atts, ap, a.getValue());
      }
    }
    for (Attr a : regular) {
      String ans = a.getNamespaceURI();
      String attrName = a.getName();
      if (a.getLocalName() == null) {
        int colon = attrName.indexOf(':');
        if (colon > 0) {
          String pre = attrName.substring(0, colon);
          String uri = lookup(pre);
          if (uri == null) {
            throw new RuntimeException("Namespace for prefix '" + pre + "' has not been declared.");
          }
          atts.addAttribute(uri, attrName.substring(colon + 1), attrName, "CDATA", a.getValue());
        } else {
          atts.addAttribute("", attrName, attrName, "CDATA", a.getValue());
        }
        continue;
      }
      if (ans != null && ans.length() > 0) {
        String ap = a.getPrefix();
        String use = ap;
        if (ap == null) {
          use = prefixFor(ans);
          if (use == null || use.isEmpty()) {
            use = "ns" + (nextPrefix++);
          }
        }
        if (declare(scope, declared, use, ans)) {
          addDecl(atts, use, ans);
        }
        if (ap == null) {
          attrName = use + ":" + a.getName();
        }
      }
      atts.addAttribute(ans == null ? "" : ans, a.getLocalName(), attrName, "CDATA", a.getValue());
    }
    if (ownDecl && !ownFirst) {
      if (declare(scope, declared, p, ownUri)) {
        addDecl(atts, p, ownUri);
      }
    }
    String local = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
    String elementUri = ns == null ? "" : ns;
    if (node.getLocalName() == null) {
      String qn = node.getNodeName();
      int colon = qn.indexOf(':');
      if (colon > 0) {
        String found = lookup(qn.substring(0, colon));
        elementUri = found == null ? "" : found;
        local = qn.substring(colon + 1);
      } else {
        String dflt = lookup("");
        elementUri = dflt == null ? "" : dflt;
        local = qn;
      }
    }
    handler.startElement(elementUri, local, node.getNodeName(), atts);
    for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
      visit(c);
    }
    handler.endElement(elementUri, local, node.getNodeName());
    for (int i = declared.size() - 1; i >= 0; i--) {
      handler.endPrefixMapping(declared.get(i));
    }
    scopes.remove(scopes.size() - 1);
  }
}
