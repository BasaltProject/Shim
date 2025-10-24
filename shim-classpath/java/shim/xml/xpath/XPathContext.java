/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

final class XPathContext {
  static final String kXmlnsNs = "http://www.w3.org/2000/xmlns/";
  static final String kXmlNs = "http://www.w3.org/XML/1998/namespace";

  static final class Shared {
    final Map<Node, Element> nsOwner = new IdentityHashMap<Node, Element>();
    final Map<Node, Integer> nsIndex = new IdentityHashMap<Node, Integer>();
    final Map<String, Node> nsCache = new java.util.HashMap<String, Node>();
    final Map<Node, Integer> childIndex = new IdentityHashMap<Node, Integer>();
    XPathVariableResolver variables;
    XPathFunctionResolver functions;
    NamespaceContext namespaces;
  }

  final Shared shared;
  final Node node;
  final int position;
  final int size;
  final boolean hasContext;

  XPathContext(Shared shared, Node node, int position, int size, boolean hasContext) {
    this.shared = shared;
    this.node = node;
    this.position = position;
    this.size = size;
    this.hasContext = hasContext;
  }

  XPathContext at(Node n, int position, int size) {
    return new XPathContext(shared, n, position, size, true);
  }

  Node parent(Node n) {
    Element owner = shared.nsOwner.get(n);
    if (owner != null) {
      return owner;
    }
    if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
      return ((Attr) n).getOwnerElement();
    }
    Node p = n.getParentNode();
    while (p != null && p.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
      p = p.getParentNode();
    }
    return p;
  }

  boolean isNamespaceNode(Node n) {
    return shared.nsOwner.containsKey(n);
  }

  Node namespaceNode(Element owner, String prefix, String uri, int index) {
    String key = System.identityHashCode(owner) + "|" + prefix + "|" + uri;
    Node n = shared.nsCache.get(key);
    if (n == null) {
      Document d = owner.getOwnerDocument();
      Attr a = d.createAttributeNS(kXmlnsNs, prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix);
      a.setValue(uri);
      n = a;
      shared.nsCache.put(key, n);
      shared.nsOwner.put(n, owner);
      shared.nsIndex.put(n, Integer.valueOf(index));
    }
    return n;
  }

  String namespaceNodePrefix(Node n) {
    String name = n.getNodeName();
    return name.equals("xmlns") ? "" : name.substring(6);
  }

  private List<Node> chain(Node n) {
    ArrayList<Node> out = new ArrayList<Node>();
    for (Node x = n; x != null; x = parent(x)) {
      out.add(0, x);
    }
    return out;
  }

  private int rank(Node n) {
    if (shared.nsOwner.containsKey(n)) {
      return 0;
    }
    return n.getNodeType() == Node.ATTRIBUTE_NODE ? 1 : 2;
  }

  private int indexOf(Node n) {
    if (shared.nsOwner.containsKey(n)) {
      return shared.nsIndex.get(n).intValue();
    }
    Node p = parent(n);
    if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
      NamedNodeMap m = p.getAttributes();
      for (int i = 0; i < m.getLength(); i++) {
        if (m.item(i) == n) {
          return i;
        }
      }
      return 0;
    }
    Integer cached = shared.childIndex.get(n);
    if (cached == null) {
      Node actual = n.getParentNode();
      int i = 0;
      for (Node ch = actual == null ? null : actual.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
        shared.childIndex.put(ch, Integer.valueOf(i++));
      }
      cached = shared.childIndex.get(n);
    }
    return cached == null ? 0 : cached.intValue();
  }

  int compare(Node a, Node b) {
    if (a == b) {
      return 0;
    }
    List<Node> ca = chain(a);
    List<Node> cb = chain(b);
    int i = 0;
    while (i < ca.size() && i < cb.size() && ca.get(i) == cb.get(i)) {
      i++;
    }
    if (i == ca.size()) {
      return -1;
    }
    if (i == cb.size()) {
      return 1;
    }
    Node x = ca.get(i);
    Node y = cb.get(i);
    if (i == 0) {
      return System.identityHashCode(x) < System.identityHashCode(y) ? -1 : 1;
    }
    int rx = rank(x);
    int ry = rank(y);
    if (rx != ry) {
      return rx < ry ? -1 : 1;
    }
    return Integer.compare(indexOf(x), indexOf(y));
  }
}
