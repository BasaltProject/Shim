/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import shim.xml.xpath.XsltXPath;

/** Sorting, numbering and the XSLT-specific XPath functions. */
final class XsltSupport {
  private XsltSupport() {
  }

  static void collectAll(Node n, List<Node> out) {
    out.add(n);
    if (n.getNodeType() == Node.ELEMENT_NODE) {
      NamedNodeMap attrs = n.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node a = attrs.item(i);
        String name = a.getNodeName();
        if (!name.equals("xmlns") && !name.startsWith("xmlns:")) {
          out.add(a);
        }
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
      collectAll(c, out);
    }
  }

  static List<Node> sort(final XsltTransformer t, List<Node> nodes, List<Element> sorts) {
    final int n = nodes.size();
    final Object[][] keys = new Object[sorts.size()][n];
    final boolean[] numeric = new boolean[sorts.size()];
    final boolean[] descending = new boolean[sorts.size()];
    final boolean[] upperFirst = new boolean[sorts.size()];
    Node saved = t.currentNode();
    try {
      for (int s = 0; s < sorts.size(); s++) {
        Element sort = sorts.get(s);
        numeric[s] = "number".equals(t.avt(sort, "data-type"));
        descending[s] = "descending".equals(t.avt(sort, "order"));
        upperFirst[s] = "upper-first".equals(t.avt(sort, "case-order"));
        String select = sort.hasAttribute("select") ? sort.getAttribute("select") : ".";
        for (int i = 0; i < n; i++) {
          t.setContext(nodes.get(i), i + 1, n);
          Object v = t.evalText(sort, "sort:" + select, select);
          keys[s][i] = numeric[s] ? (Object) Double.valueOf(XsltXPath.number(v)) : XsltXPath.string(v);
        }
      }
    } finally {
      t.setContext(saved, t.contextPosition(), t.contextSize());
    }
    List<Integer> order = new ArrayList<Integer>();
    for (int i = 0; i < n; i++) {
      order.add(Integer.valueOf(i));
    }
    final java.text.Collator collator = java.text.Collator.getInstance();
    Collections.sort(order, new Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
        for (int s = 0; s < keys.length; s++) {
          int c;
          if (numeric[s]) {
            double x = ((Double) keys[s][a]).doubleValue();
            double y = ((Double) keys[s][b]).doubleValue();
            c = x != x ? (y != y ? 0 : -1) : (y != y ? 1 : Double.compare(x, y));
          } else {
            String x = (String) keys[s][a];
            String y = (String) keys[s][b];
            c = collator.compare(x, y);
            if (c == 0 && !x.equals(y)) {
              c = upperFirst[s] ? -x.compareTo(y) : x.compareTo(y);
            }
          }
          if (descending[s]) {
            c = -c;
          }
          if (c != 0) {
            return c;
          }
        }
        return 0;
      }
    });
    List<Node> result = new ArrayList<Node>(n);
    for (Integer i : order) {
      result.add(nodes.get(i.intValue()));
    }
    return result;
  }

  static String number(XsltTransformer t, Element e) {
    List<Integer> values = new ArrayList<Integer>();
    if (e.hasAttribute("value")) {
      double d = XsltXPath.number(t.eval(e, "value"));
      values.add(Integer.valueOf((int) Math.round(d)));
    } else {
      Node node = t.currentNode();
      String level = e.hasAttribute("level") ? e.getAttribute("level") : "single";
      String count = e.getAttribute("count");
      List<Node> chain = new ArrayList<Node>();
      if (level.equals("any")) {
        List<Node> all = new ArrayList<Node>();
        collectAll(t.sourceDocument(), all);
        int num = 0;
        for (Node n : all) {
          if (n == node) {
            if (counts(t, e, count, n, true)) {
              num++;
            }
            break;
          }
          if (counts(t, e, count, n, true)) {
            num++;
          }
        }
        values.add(Integer.valueOf(num));
      } else {
        for (Node a = node; a != null && a.getNodeType() != Node.DOCUMENT_NODE;
            a = a.getNodeType() == Node.ATTRIBUTE_NODE ? ((Attr) a).getOwnerElement() : a.getParentNode()) {
          if (counts(t, e, count, a)) {
            chain.add(a);
            if (level.equals("single")) {
              break;
            }
          }
        }
        Collections.reverse(chain);
        for (Node a : chain) {
          int pos = 1;
          for (Node s = a.getPreviousSibling(); s != null; s = s.getPreviousSibling()) {
            if (counts(t, e, count, s) && sameKind(a, s, count)) {
              pos++;
            }
          }
          values.add(Integer.valueOf(pos));
        }
      }
    }
    String format = e.hasAttribute("format") ? t.avt(e, "format") : "1";
    return format(values, format);
  }

  private static boolean sameKind(Node a, Node b, String count) {
    return a.getNodeType() == b.getNodeType() || !count.isEmpty();
  }

  private static boolean eq(String a, String b) {
    return (a == null ? "" : a).equals(b == null ? "" : b);
  }

  private static boolean counts(XsltTransformer t, Element e, String count, Node n) {
    return counts(t, e, count, n, false);
  }

  private static boolean counts(XsltTransformer t, Element e, String count, Node n, boolean byName) {
    if (count.isEmpty()) {
      Node cur = t.currentNode();
      if (byName) {
        return n.getNodeType() == cur.getNodeType() && XsltStylesheet.localName(n).equals(XsltStylesheet.localName(cur))
            && eq(n.getNamespaceURI(), cur.getNamespaceURI());
      }
      return n.getNodeType() == cur.getNodeType();
    }
    for (String alt : XsltStylesheet.splitUnion(count)) {
      XsltXPath p = t.compile(e, "count:" + alt, alt.trim());
      for (Node a = n; a != null; a = a.getNodeType() == Node.ATTRIBUTE_NODE ? ((Attr) a).getOwnerElement() : a.getParentNode()) {
        Object v = p.evaluate(a, 1, 1);
        if (v instanceof List && ((List<?>) v).contains(n)) {
          return true;
        }
      }
    }
    return false;
  }

  static String format(List<Integer> values, String format) {
    List<String> tokens = new ArrayList<String>();
    List<String> separators = new ArrayList<String>();
    int i = 0;
    String lead = "";
    while (i < format.length() && !Character.isLetterOrDigit(format.charAt(i))) {
      i++;
    }
    lead = format.substring(0, i);
    String trail = "";
    while (i < format.length()) {
      int s = i;
      while (i < format.length() && Character.isLetterOrDigit(format.charAt(i))) {
        i++;
      }
      tokens.add(format.substring(s, i));
      int sep = i;
      while (i < format.length() && !Character.isLetterOrDigit(format.charAt(i))) {
        i++;
      }
      if (i >= format.length()) {
        trail = format.substring(sep);
      } else {
        separators.add(format.substring(sep, i));
      }
    }
    if (tokens.isEmpty()) {
      tokens.add("1");
    }
    StringBuilder sb = new StringBuilder(lead);
    for (int k = 0; k < values.size(); k++) {
      if (k > 0) {
        sb.append(k - 1 < separators.size() ? separators.get(k - 1) : separators.isEmpty() ? "." : separators.get(separators.size() - 1));
      }
      String token = tokens.get(Math.min(k, tokens.size() - 1));
      sb.append(formatOne(values.get(k).intValue(), token));
    }
    return sb.append(trail).toString();
  }

  private static String formatOne(int v, String token) {
    char c = token.charAt(token.length() - 1);
    if (c == 'a' || c == 'A') {
      StringBuilder sb = new StringBuilder();
      int n = v;
      while (n > 0) {
        n--;
        sb.insert(0, (char) (c + n % 26));
        n /= 26;
      }
      return sb.toString();
    }
    if (c == 'i' || c == 'I') {
      String r = roman(v);
      return c == 'i' ? r.toLowerCase() : r;
    }
    String s = Integer.toString(v);
    while (s.length() < token.length()) {
      s = "0" + s;
    }
    return s;
  }

  private static String roman(int n) {
    int[] vals = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    String[] syms = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < vals.length; i++) {
      while (n >= vals[i]) {
        sb.append(syms[i]);
        n -= vals[i];
      }
    }
    return sb.toString();
  }

  static Object function(XsltTransformer t, String name, List args) {
    switch (name) {
    case "current":
      return t.currentNode();
    case "generate-id": {
      Node n = args.isEmpty() ? t.currentNode() : first(args.get(0));
      return n == null ? "" : t.generateId(n);
    }
    case "key":
      return nodeList(t.keyLookup(XsltXPath.string(toValue(args.get(0))), toValue(args.get(1))));
    case "document": {
      Object a = toValue(args.get(0));
      List<Node> out = new ArrayList<Node>();
      if (a instanceof List) {
        for (Object o : (List<?>) a) {
          addDocument(t, out, XsltXPath.stringValue((Node) o));
        }
      } else {
        addDocument(t, out, XsltXPath.string(a));
      }
      return nodeList(out);
    }
    case "format-number": {
      double d = XsltXPath.number(toValue(args.get(0)));
      String pattern = XsltXPath.string(toValue(args.get(1)));
      String fmt = args.size() > 2 ? XsltXPath.string(toValue(args.get(2))) : "";
      return formatNumber(t, d, pattern, fmt);
    }
    case "system-property": {
      String p = XsltXPath.string(toValue(args.get(0)));
      if (p.endsWith("version")) {
        return Double.valueOf(1.0);
      }
      if (p.endsWith("vendor")) {
        return "Basalt Shim";
      }
      if (p.endsWith("vendor-url")) {
        return "https://basaltmc.org";
      }
      return "";
    }
    case "element-available": {
      String n = XsltXPath.string(toValue(args.get(0)));
      String local = n.substring(n.indexOf(':') + 1);
      return Boolean.valueOf(java.util.Arrays.asList("apply-templates", "call-template", "for-each", "value-of",
          "copy-of", "copy", "if", "choose", "variable", "text", "element", "attribute", "comment",
          "processing-instruction", "number", "message", "apply-imports", "sort", "param").contains(local));
    }
    case "function-available": {
      String n = XsltXPath.string(toValue(args.get(0)));
      return Boolean.valueOf(java.util.Arrays.asList("current", "generate-id", "key", "document", "format-number",
          "system-property", "element-available", "function-available", "unparsed-entity-uri", "string", "number",
          "boolean", "count", "sum", "concat", "contains", "substring", "position", "last", "name", "local-name",
          "namespace-uri", "normalize-space", "translate", "starts-with", "floor", "ceiling", "round", "id", "lang",
          "not", "true", "false", "string-length", "substring-before", "substring-after").contains(n));
    }
    case "unparsed-entity-uri":
      return "";
    default:
      throw new IllegalStateException("Unknown function: " + name);
    }
  }

  private static void addDocument(XsltTransformer t, List<Node> out, String href) {
    if (href.isEmpty()) {
      out.add(t.sheetDocument());
      return;
    }
    Document d = t.document(href);
    if (d != null) {
      out.add(d);
    }
  }

  private static Object toValue(Object o) {
    if (o instanceof NodeList) {
      NodeList l = (NodeList) o;
      List<Node> out = new ArrayList<Node>();
      for (int i = 0; i < l.getLength(); i++) {
        out.add(l.item(i));
      }
      return out;
    }
    return o;
  }

  private static Node first(Object o) {
    Object v = toValue(o);
    if (v instanceof List) {
      List<?> l = (List<?>) v;
      return l.isEmpty() ? null : (Node) l.get(0);
    }
    return null;
  }

  private static NodeList nodeList(final List<Node> nodes) {
    return new NodeList() {
      public Node item(int i) {
        return i >= 0 && i < nodes.size() ? nodes.get(i) : null;
      }

      public int getLength() {
        return nodes.size();
      }
    };
  }

  static String formatNumber(XsltTransformer t, double d, String pattern, String formatName) {
    DecimalFormatSymbols sym = new DecimalFormatSymbols(java.util.Locale.ROOT);
    Element df = t.decimalFormats().get(formatName);
    if (df != null) {
      if (df.hasAttribute("decimal-separator")) {
        sym.setDecimalSeparator(df.getAttribute("decimal-separator").charAt(0));
      }
      if (df.hasAttribute("grouping-separator")) {
        sym.setGroupingSeparator(df.getAttribute("grouping-separator").charAt(0));
      }
      if (df.hasAttribute("infinity")) {
        sym.setInfinity(df.getAttribute("infinity"));
      }
      if (df.hasAttribute("NaN")) {
        sym.setNaN(df.getAttribute("NaN"));
      }
      if (df.hasAttribute("minus-sign")) {
        sym.setMinusSign(df.getAttribute("minus-sign").charAt(0));
      }
      if (df.hasAttribute("percent")) {
        sym.setPercent(df.getAttribute("percent").charAt(0));
      }
      if (df.hasAttribute("zero-digit")) {
        sym.setZeroDigit(df.getAttribute("zero-digit").charAt(0));
      }
    }
    if (d != d) {
      return sym.getNaN();
    }
    DecimalFormat f = new DecimalFormat(pattern, sym);
    f.setRoundingMode(java.math.RoundingMode.HALF_EVEN);
    if (Double.isInfinite(d)) {
      return (d < 0 ? String.valueOf(sym.getMinusSign()) : "") + sym.getInfinity();
    }
    return f.format(d);
  }
}
