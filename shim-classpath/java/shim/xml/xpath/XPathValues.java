/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

final class XPathValues {
  private XPathValues() {
  }

  static final class NodeSet extends ArrayList<Node> {
    private static final long serialVersionUID = 1L;
  }

  static String string(Object v, XPathContext c) {
    if (v instanceof String) {
      return (String) v;
    }
    if (v instanceof Double) {
      return numberToString(((Double) v).doubleValue());
    }
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue() ? "true" : "false";
    }
    NodeSet set = (NodeSet) v;
    return set.isEmpty() ? "" : stringValue(first(set, c), c);
  }

  static double number(Object v, XPathContext c) {
    if (v instanceof Double) {
      return ((Double) v).doubleValue();
    }
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue() ? 1 : 0;
    }
    return parseNumber(string(v, c));
  }

  static boolean bool(Object v) {
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue();
    }
    if (v instanceof Double) {
      double d = ((Double) v).doubleValue();
      return d != 0 && d == d;
    }
    if (v instanceof String) {
      return !((String) v).isEmpty();
    }
    return !((NodeSet) v).isEmpty();
  }

  static Node first(NodeSet set, XPathContext c) {
    if (set.size() == 1) {
      return set.get(0);
    }
    Node best = set.get(0);
    for (int i = 1; i < set.size(); i++) {
      if (c.compare(set.get(i), best) < 0) {
        best = set.get(i);
      }
    }
    return best;
  }

  static String numberToString(double d) {
    if (d != d) {
      return "NaN";
    }
    if (Double.isInfinite(d)) {
      return d > 0 ? "Infinity" : "-Infinity";
    }
    if (d == 0) {
      return "0";
    }
    if (d == Math.rint(d) && Math.abs(d) < 1e15) {
      return Long.toString((long) d);
    }
    return new BigDecimal(Double.toString(d)).stripTrailingZeros().toPlainString();
  }

  static double parseNumber(String s) {
    int a = 0;
    int b = s.length();
    while (a < b && isSpace(s.charAt(a))) {
      a++;
    }
    while (b > a && isSpace(s.charAt(b - 1))) {
      b--;
    }
    if (a == b) {
      return Double.NaN;
    }
    int i = a;
    if (s.charAt(i) == '-') {
      i++;
    }
    int digits = 0;
    int dots = 0;
    for (int j = i; j < b; j++) {
      char ch = s.charAt(j);
      if (ch >= '0' && ch <= '9') {
        digits++;
      } else if (ch == '.' && ++dots == 1) {
        continue;
      } else {
        return Double.NaN;
      }
    }
    if (digits == 0) {
      return Double.NaN;
    }
    return Double.parseDouble(s.substring(a, b));
  }

  static boolean isSpace(char ch) {
    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
  }

  static String stringValue(Node n, XPathContext c) {
    switch (n.getNodeType()) {
      case Node.DOCUMENT_NODE:
      case Node.ELEMENT_NODE:
      case Node.DOCUMENT_FRAGMENT_NODE:
      case Node.ENTITY_REFERENCE_NODE:
        StringBuilder sb = new StringBuilder();
        collect(n, sb);
        return sb.toString();
      case Node.ATTRIBUTE_NODE:
        return ((Attr) n).getValue();
      case Node.PROCESSING_INSTRUCTION_NODE:
        return ((org.w3c.dom.ProcessingInstruction) n).getData();
      case Node.TEXT_NODE:
      case Node.CDATA_SECTION_NODE:
        StringBuilder run = new StringBuilder();
        for (Node x = n; x != null && (x.getNodeType() == Node.TEXT_NODE || x.getNodeType() == Node.CDATA_SECTION_NODE);
            x = x.getNextSibling()) {
          run.append(x.getNodeValue());
        }
        return run.toString();
      default:
        String v = n.getNodeValue();
        return v == null ? "" : v;
    }
  }

  private static void collect(Node n, StringBuilder sb) {
    for (Node ch = n.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
      short t = ch.getNodeType();
      if (t == Node.TEXT_NODE || t == Node.CDATA_SECTION_NODE) {
        sb.append(ch.getNodeValue());
      } else if (t == Node.ELEMENT_NODE || t == Node.ENTITY_REFERENCE_NODE) {
        collect(ch, sb);
      }
    }
  }

  static boolean equalsOp(Object a, Object b, XPathContext c, boolean negate) {
    boolean r = compareEq(a, b, c, negate);
    return r;
  }

  private static boolean compareEq(Object a, Object b, XPathContext c, boolean ne) {
    boolean an = a instanceof NodeSet;
    boolean bn = b instanceof NodeSet;
    if (an && bn) {
      NodeSet x = (NodeSet) a;
      NodeSet y = (NodeSet) b;
      for (Node p : x) {
        String sp = stringValue(p, c);
        for (Node q : y) {
          if (sp.equals(stringValue(q, c)) != ne) {
            return true;
          }
        }
      }
      return false;
    }
    if (an || bn) {
      NodeSet set = (NodeSet) (an ? a : b);
      Object other = an ? b : a;
      if (other instanceof Boolean) {
        return (bool(set) == ((Boolean) other).booleanValue()) != ne;
      }
      for (Node p : set) {
        String sp = stringValue(p, c);
        boolean eq = other instanceof Double ? parseNumber(sp) == ((Double) other).doubleValue() : sp.equals(other);
        if (eq != ne) {
          return true;
        }
      }
      return false;
    }
    boolean eq;
    if (a instanceof Boolean || b instanceof Boolean) {
      eq = bool(a) == bool(b);
    } else if (a instanceof Double || b instanceof Double) {
      eq = number(a, c) == number(b, c);
    } else {
      eq = ((String) a).equals(b);
    }
    return eq != ne;
  }

  static boolean relational(String op, Object a, Object b, XPathContext c) {
    boolean an = a instanceof NodeSet;
    boolean bn = b instanceof NodeSet;
    if (an && bn) {
      for (Node p : (NodeSet) a) {
        double x = parseNumber(stringValue(p, c));
        for (Node q : (NodeSet) b) {
          if (cmp(op, x, parseNumber(stringValue(q, c)))) {
            return true;
          }
        }
      }
      return false;
    }
    if (an || bn) {
      NodeSet set = (NodeSet) (an ? a : b);
      Object other = an ? b : a;
      if (other instanceof Boolean) {
        double x = an ? (bool(set) ? 1 : 0) : (((Boolean) other).booleanValue() ? 1 : 0);
        double y = an ? (((Boolean) other).booleanValue() ? 1 : 0) : (bool(set) ? 1 : 0);
        return cmp(op, x, y);
      }
      double o = number(other, c);
      for (Node p : set) {
        double v = parseNumber(stringValue(p, c));
        if (an ? cmp(op, v, o) : cmp(op, o, v)) {
          return true;
        }
      }
      return false;
    }
    return cmp(op, number(a, c), number(b, c));
  }

  private static boolean cmp(String op, double x, double y) {
    switch (op) {
      case "<":
        return x < y;
      case "<=":
        return x <= y;
      case ">":
        return x > y;
      default:
        return x >= y;
    }
  }

  static NodeSet unique(List<Node> in) {
    NodeSet out = new NodeSet();
    Map<Node, Boolean> seen = new IdentityHashMap<Node, Boolean>();
    for (Node n : in) {
      if (seen.put(n, Boolean.TRUE) == null) {
        out.add(n);
      }
    }
    return out;
  }

  static void sort(NodeSet set, final XPathContext c) {
    Collections.sort(set, new Comparator<Node>() {
      public int compare(Node a, Node b) {
        return c.compare(a, b);
      }
    });
  }
}
