/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import shim.xml.xpath.XPathValues.NodeSet;

final class XPathSteps {
  static final int kChild = 0;
  static final int kDescendant = 1;
  static final int kParent = 2;
  static final int kAncestor = 3;
  static final int kFollowingSibling = 4;
  static final int kPrecedingSibling = 5;
  static final int kFollowing = 6;
  static final int kPreceding = 7;
  static final int kAttribute = 8;
  static final int kNamespace = 9;
  static final int kSelf = 10;
  static final int kDescendantOrSelf = 11;
  static final int kAncestorOrSelf = 12;

  static final int kTestName = 0;
  static final int kTestNode = 1;
  static final int kTestText = 2;
  static final int kTestComment = 3;
  static final int kTestPi = 4;

  private XPathSteps() {
  }

  static final class Step {
    final int axis;
    final int test;
    final boolean anyName;
    final String uri;
    final String local;
    final boolean anyLocal;
    final List<XPathExpr> predicates;

    Step(int axis, int test, boolean anyName, String uri, String local, boolean anyLocal,
        List<XPathExpr> predicates) {
      this.axis = axis;
      this.test = test;
      this.anyName = anyName;
      this.uri = uri;
      this.local = local;
      this.anyLocal = anyLocal;
      this.predicates = predicates;
    }
  }

  static int axisOf(String name) {
    switch (name) {
      case "child":
        return kChild;
      case "descendant":
        return kDescendant;
      case "parent":
        return kParent;
      case "ancestor":
        return kAncestor;
      case "following-sibling":
        return kFollowingSibling;
      case "preceding-sibling":
        return kPrecedingSibling;
      case "following":
        return kFollowing;
      case "preceding":
        return kPreceding;
      case "attribute":
        return kAttribute;
      case "namespace":
        return kNamespace;
      case "self":
        return kSelf;
      case "descendant-or-self":
        return kDescendantOrSelf;
      case "ancestor-or-self":
        return kAncestorOrSelf;
      default:
        return -1;
    }
  }

  static boolean reverse(int axis) {
    return axis == kAncestor || axis == kAncestorOrSelf || axis == kPreceding || axis == kPrecedingSibling;
  }

  static List<Node> apply(Step s, List<Node> input, XPathContext c) {
    List<Node> all = new ArrayList<Node>();
    for (Node n : input) {
      List<Node> cand = new ArrayList<Node>();
      axis(s.axis, n, c, cand);
      List<Node> matched = new ArrayList<Node>();
      for (Node m : cand) {
        if (matches(s, m, c)) {
          matched.add(m);
        }
      }
      for (XPathExpr p : s.predicates) {
        matched = applyPredicate(matched, p, c);
      }
      if (reverse(s.axis)) {
        Collections.reverse(matched);
      }
      all.addAll(matched);
    }
    if (input.size() <= 1) {
      return all;
    }
    NodeSet set = XPathValues.unique(all);
    XPathValues.sort(set, c);
    return set;
  }

  static List<Node> applyPredicate(List<Node> nodes, XPathExpr p, XPathContext c) {
    List<Node> out = new ArrayList<Node>();
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      Object v = p.eval(c.at(nodes.get(i), i + 1, size));
      boolean keep;
      if (v instanceof Double) {
        double d = ((Double) v).doubleValue();
        keep = (int) d == i + 1 && d == d;
      } else {
        keep = XPathValues.bool(v);
      }
      if (keep) {
        out.add(nodes.get(i));
      }
    }
    return out;
  }

  private static boolean matches(Step s, Node n, XPathContext c) {
    short t = n.getNodeType();
    switch (s.test) {
      case kTestNode:
        return true;
      case kTestText:
        return t == Node.TEXT_NODE || t == Node.CDATA_SECTION_NODE;
      case kTestComment:
        return t == Node.COMMENT_NODE;
      case kTestPi:
        return t == Node.PROCESSING_INSTRUCTION_NODE && (s.local == null || s.local.equals(n.getNodeName()));
      default:
        break;
    }
    boolean principal;
    if (s.axis == kAttribute) {
      principal = t == Node.ATTRIBUTE_NODE;
    } else if (s.axis == kNamespace) {
      principal = c.isNamespaceNode(n);
    } else {
      principal = t == Node.ELEMENT_NODE;
    }
    if (!principal) {
      return false;
    }
    if (s.anyName) {
      return true;
    }
    String uri;
    String local;
    if (s.axis == kNamespace) {
      uri = "";
      local = c.namespaceNodePrefix(n);
    } else {
      uri = n.getNamespaceURI();
      local = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }
    String want = s.uri == null ? "" : s.uri;
    if (!want.equals(uri == null ? "" : uri)) {
      return false;
    }
    return s.anyLocal || s.local.equals(local);
  }

  static void children(Node n, List<Node> out) {
    if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
      return;
    }
    List<Node> raw = new ArrayList<Node>();
    rawChildren(n, raw);
    boolean lastText = false;
    for (Node ch : raw) {
      short ct = ch.getNodeType();
      boolean text = ct == Node.TEXT_NODE || ct == Node.CDATA_SECTION_NODE;
      if (!(text && lastText)) {
        out.add(ch);
      }
      lastText = text;
    }
  }

  private static void rawChildren(Node n, List<Node> out) {
    for (Node ch = n.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
      short ct = ch.getNodeType();
      if (ct == Node.DOCUMENT_TYPE_NODE) {
        continue;
      }
      if (ct == Node.ENTITY_REFERENCE_NODE) {
        rawChildren(ch, out);
      } else {
        out.add(ch);
      }
    }
  }

  private static void descendants(Node n, List<Node> out) {
    List<Node> kids = new ArrayList<Node>();
    children(n, kids);
    for (Node k : kids) {
      out.add(k);
      descendants(k, out);
    }
  }

  private static void reverseSubtree(Node n, List<Node> out) {
    List<Node> kids = new ArrayList<Node>();
    children(n, kids);
    for (int i = kids.size() - 1; i >= 0; i--) {
      reverseSubtree(kids.get(i), out);
    }
    out.add(n);
  }

  private static boolean isDeclaration(Attr a) {
    String name = a.getName();
    return name.equals("xmlns") || name.startsWith("xmlns:");
  }

  private static void axis(int axis, Node n, XPathContext c, List<Node> out) {
    switch (axis) {
      case kChild:
        children(n, out);
        break;
      case kDescendant:
        descendants(n, out);
        break;
      case kDescendantOrSelf:
        out.add(n);
        descendants(n, out);
        break;
      case kSelf:
        out.add(n);
        break;
      case kParent: {
        Node p = c.parent(n);
        if (p != null) {
          out.add(p);
        }
        break;
      }
      case kAncestor:
      case kAncestorOrSelf:
        if (axis == kAncestorOrSelf) {
          out.add(n);
        }
        for (Node p = c.parent(n); p != null; p = c.parent(p)) {
          out.add(p);
        }
        break;
      case kFollowingSibling:
      case kPrecedingSibling: {
        Node p = c.parent(n);
        if (p == null || n.getNodeType() == Node.ATTRIBUTE_NODE) {
          break;
        }
        List<Node> sibs = new ArrayList<Node>();
        children(p, sibs);
        int idx = sibs.indexOf(n);
        if (axis == kFollowingSibling) {
          for (int i = idx + 1; i < sibs.size(); i++) {
            out.add(sibs.get(i));
          }
        } else {
          for (int i = idx - 1; i >= 0; i--) {
            out.add(sibs.get(i));
          }
        }
        break;
      }
      case kFollowing: {
        Node cur = n;
        if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
          cur = c.parent(n);
          if (cur == null) {
            break;
          }
          descendants(cur, out);
        }
        while (cur != null) {
          Node p = c.parent(cur);
          if (p != null) {
            List<Node> sibs = new ArrayList<Node>();
            children(p, sibs);
            for (int i = sibs.indexOf(cur) + 1; i < sibs.size(); i++) {
              out.add(sibs.get(i));
              descendants(sibs.get(i), out);
            }
          }
          cur = p;
        }
        break;
      }
      case kPreceding: {
        Node cur = n.getNodeType() == Node.ATTRIBUTE_NODE ? c.parent(n) : n;
        while (cur != null) {
          Node p = c.parent(cur);
          if (p != null) {
            List<Node> sibs = new ArrayList<Node>();
            children(p, sibs);
            for (int i = sibs.indexOf(cur) - 1; i >= 0; i--) {
              reverseSubtree(sibs.get(i), out);
            }
          }
          cur = p;
        }
        break;
      }
      case kAttribute: {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
          break;
        }
        NamedNodeMap m = n.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
          Attr a = (Attr) m.item(i);
          if (!isDeclaration(a)) {
            out.add(a);
          }
        }
        break;
      }
      default:
        namespaces(n, c, out);
        break;
    }
  }

  private static void namespaces(Node n, XPathContext c, List<Node> out) {
    if (n.getNodeType() != Node.ELEMENT_NODE) {
      return;
    }
    List<String> seen = new ArrayList<String>();
    Element top = (Element) n;
    for (Node x = c.parent(n); x != null && x.getNodeType() == Node.ELEMENT_NODE; x = c.parent(x)) {
      top = (Element) x;
    }
    int index = 0;
    for (Node x = n; x != null && x.getNodeType() == Node.ELEMENT_NODE; x = c.parent(x)) {
      NamedNodeMap m = x.getAttributes();
      for (int i = 0; i < m.getLength(); i++) {
        Attr a = (Attr) m.item(i);
        if (!isDeclaration(a)) {
          continue;
        }
        String prefix = a.getName().equals("xmlns") ? "" : a.getName().substring(6);
        if (seen.contains(prefix)) {
          continue;
        }
        seen.add(prefix);
        if (!a.getValue().isEmpty()) {
          out.add(c.namespaceNode((Element) x, prefix, a.getValue(), index++));
        }
      }
    }
    if (!seen.contains("xml")) {
      out.add(c.namespaceNode(top, "xml", XPathContext.kXmlNs, index));
    }
  }
}
