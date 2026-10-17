/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import shim.xml.xpath.XPathValues.NodeSet;

final class XPathFunctions {
  private static final String kPackage = "com.sun.org.apache.xpath.internal.functions.";
  private static final Map<String, Object[]> kArity = new HashMap<String, Object[]>();

  static {
    arity("last", 0, 0, "FuncLast");
    arity("position", 0, 0, "FuncPosition");
    arity("count", 1, 1, "FuncCount");
    arity("id", 1, 1, "FuncId");
    arity("local-name", 0, 1, "FuncLocalPart");
    arity("namespace-uri", 0, 1, "FuncNamespace");
    arity("name", 0, 1, "FuncQname");
    arity("string", 0, 1, "FuncString");
    arity("concat", 2, Integer.MAX_VALUE, "FuncConcat");
    arity("starts-with", 2, 2, "FuncStartsWith");
    arity("contains", 2, 2, "FuncContains");
    arity("substring-before", 2, 2, "FuncSubstringBefore");
    arity("substring-after", 2, 2, "FuncSubstringAfter");
    arity("substring", 2, 3, "FuncSubstring");
    arity("string-length", 0, 1, "FuncStringLength");
    arity("normalize-space", 0, 1, "FuncNormalizeSpace");
    arity("translate", 3, 3, "FuncTranslate");
    arity("boolean", 1, 1, "FuncBoolean");
    arity("not", 1, 1, "FuncNot");
    arity("true", 0, 0, "FuncTrue");
    arity("false", 0, 0, "FuncFalse");
    arity("lang", 1, 1, "FuncLang");
    arity("number", 0, 1, "FuncNumber");
    arity("sum", 1, 1, "FuncSum");
    arity("floor", 1, 1, "FuncFloor");
    arity("ceiling", 1, 1, "FuncCeiling");
    arity("round", 1, 1, "FuncRound");
  }

  private XPathFunctions() {
  }

  private static void arity(String name, int min, int max, String cls) {
    kArity.put(name, new Object[] {Integer.valueOf(min), Integer.valueOf(max), cls});
  }

  static void check(String name, int count) {
    Object[] a = kArity.get(name);
    if (a == null) {
      throw new XPathError("Could not find function: " + name);
    }
    int min = ((Integer) a[0]).intValue();
    int max = ((Integer) a[1]).intValue();
    if (count >= min && count <= max) {
      return;
    }
    String allowed;
    if (max == Integer.MAX_VALUE) {
      allowed = ">" + (min - 1);
    } else if (min == max) {
      allowed = String.valueOf(min);
    } else {
      allowed = min + " or " + max;
    }
    throw new XPathError(kPackage + a[2] + " only allows " + allowed + " arguments");
  }

  private static NodeSet contextSet(XPathContext c) {
    if (!c.hasContext || c.node == null) {
      throw new XPathError("The context can not be null when the operation is context-dependent.");
    }
    NodeSet s = new NodeSet();
    s.add(c.node);
    return s;
  }

  private static Object arg(List<XPathExpr> args, int i, XPathContext c) {
    return args.get(i).eval(c);
  }

  private static String str(List<XPathExpr> args, int i, XPathContext c) {
    return XPathValues.string(arg(args, i, c), c);
  }

  private static double num(List<XPathExpr> args, int i, XPathContext c) {
    return XPathValues.number(arg(args, i, c), c);
  }

  private static Boolean b(boolean v) {
    return Boolean.valueOf(v);
  }

  private static Double d(double v) {
    return Double.valueOf(v);
  }

  private static Node firstNode(List<XPathExpr> args, XPathContext c) {
    NodeSet s = args.isEmpty() ? contextSet(c) : XPathExpr.nodes(arg(args, 0, c));
    return s.isEmpty() ? null : XPathValues.first(s, c);
  }

  static double round(double x) {
    if (x != x || Double.isInfinite(x)) {
      return x;
    }
    if (x < 0 && x >= -0.5) {
      return -0.0;
    }
    return Math.floor(x + 0.5);
  }

  static Object call(String name, List<XPathExpr> args, XPathContext c) {
    switch (name) {
      case "last":
        return d(c.size);
      case "position":
        return d(c.position);
      case "count":
        return d(XPathExpr.nodes(arg(args, 0, c)).size());
      case "id":
        return id(arg(args, 0, c), c);
      case "local-name":
      case "name":
      case "namespace-uri": {
        Node n = firstNode(args, c);
        return n == null ? "" : nameOf(name, n, c);
      }
      case "string":
        return args.isEmpty() ? XPathValues.string(contextSet(c), c) : str(args, 0, c);
      case "concat": {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
          sb.append(str(args, i, c));
        }
        return sb.toString();
      }
      case "starts-with":
        return b(str(args, 0, c).startsWith(str(args, 1, c)));
      case "contains":
        return b(str(args, 0, c).contains(str(args, 1, c)));
      case "substring-before": {
        String s = str(args, 0, c);
        int i = s.indexOf(str(args, 1, c));
        return i < 0 ? "" : s.substring(0, i);
      }
      case "substring-after": {
        String s = str(args, 0, c);
        String t = str(args, 1, c);
        int i = s.indexOf(t);
        return i < 0 ? "" : s.substring(i + t.length());
      }
      case "substring":
        return substring(str(args, 0, c), num(args, 1, c), args.size() > 2 ? num(args, 2, c) : Double.NaN,
            args.size() > 2);
      case "string-length":
        return d((args.isEmpty() ? XPathValues.string(contextSet(c), c) : str(args, 0, c)).length());
      case "normalize-space":
        return normalize(args.isEmpty() ? XPathValues.string(contextSet(c), c) : str(args, 0, c));
      case "translate":
        return translate(str(args, 0, c), str(args, 1, c), str(args, 2, c));
      case "boolean":
        return b(XPathValues.bool(arg(args, 0, c)));
      case "not":
        return b(!XPathValues.bool(arg(args, 0, c)));
      case "true":
        return Boolean.TRUE;
      case "false":
        return Boolean.FALSE;
      case "lang":
        return b(lang(str(args, 0, c), c));
      case "number":
        return d(args.isEmpty() ? XPathValues.number(contextSet(c), c) : num(args, 0, c));
      case "sum": {
        double sum = 0;
        for (Node n : XPathExpr.nodes(arg(args, 0, c))) {
          sum += XPathValues.parseNumber(XPathValues.stringValue(n, c));
        }
        return d(sum);
      }
      case "floor":
        return d(Math.floor(num(args, 0, c)));
      case "ceiling":
        return d(Math.ceil(num(args, 0, c)));
      default:
        return d(round(num(args, 0, c)));
    }
  }

  private static String nameOf(String fn, Node n, XPathContext c) {
    if (c.isNamespaceNode(n)) {
      return fn.equals("namespace-uri") ? "" : c.namespaceNodePrefix(n);
    }
    short t = n.getNodeType();
    if (fn.equals("namespace-uri")) {
      String u = n.getNamespaceURI();
      return u == null ? "" : u;
    }
    if (t != Node.ELEMENT_NODE && t != Node.ATTRIBUTE_NODE && (t != Node.PROCESSING_INSTRUCTION_NODE || fn.equals("name"))) {
      return "";
    }
    if (fn.equals("name")) {
      return n.getNodeName();
    }
    if (n.getLocalName() != null) {
      return n.getLocalName();
    }
    String q = n.getNodeName();
    return q.substring(q.indexOf(':') + 1);
  }

  private static Object substring(String s, double start, double len, boolean hasLen) {
    if (start != start && !hasLen) {
      return s;
    }
    double from = round(start);
    double to = hasLen ? from + round(len) : Double.POSITIVE_INFINITY;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      double p = i + 1;
      if (p >= from && p < to) {
        sb.append(s.charAt(i));
      }
    }
    return sb.toString();
  }

  private static String normalize(String s) {
    StringBuilder sb = new StringBuilder();
    boolean pending = false;
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (XPathValues.isSpace(ch)) {
        pending = sb.length() > 0;
      } else {
        if (pending) {
          sb.append(' ');
          pending = false;
        }
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  private static String translate(String s, String from, String to) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      int k = from.indexOf(ch);
      if (k < 0) {
        sb.append(ch);
      } else if (k < to.length()) {
        sb.append(to.charAt(k));
      }
    }
    return sb.toString();
  }

  private static boolean lang(String want, XPathContext c) {
    for (Node n = c.hasContext ? c.node : null; n != null; n = c.parent(n)) {
      if (n.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element e = (Element) n;
      String v = null;
      if (e.hasAttribute("xml:lang")) {
        v = e.getAttribute("xml:lang");
      } else if (e.hasAttributeNS(XPathContext.kXmlNs, "lang")) {
        v = e.getAttributeNS(XPathContext.kXmlNs, "lang");
      }
      if (v != null) {
        String a = v.toLowerCase();
        String w = want.toLowerCase();
        return a.equals(w) || a.startsWith(w + "-");
      }
    }
    return false;
  }

  private static Object id(Object v, XPathContext c) {
    List<String> ids = new ArrayList<String>();
    if (v instanceof NodeSet) {
      for (Node n : (NodeSet) v) {
        split(XPathValues.stringValue(n, c), ids);
      }
    } else {
      split(XPathValues.string(v, c), ids);
    }
    NodeSet out = new NodeSet();
    Node n = c.hasContext ? c.node : null;
    Document doc = n == null ? null : n.getNodeType() == Node.DOCUMENT_NODE ? (Document) n : n.getOwnerDocument();
    if (doc != null) {
      for (String id : ids) {
        Element e = doc.getElementById(id);
        if (e != null && !out.contains(e)) {
          out.add(e);
        }
      }
      XPathValues.sort(out, c);
    }
    return out;
  }

  private static void split(String s, List<String> out) {
    int i = 0;
    while (i < s.length()) {
      while (i < s.length() && XPathValues.isSpace(s.charAt(i))) {
        i++;
      }
      int st = i;
      while (i < s.length() && !XPathValues.isSpace(s.charAt(i))) {
        i++;
      }
      if (i > st) {
        out.add(s.substring(st, i));
      }
    }
  }

  static Object extension(XPathExpr.Call call, XPathContext c) {
    if (c.shared.functions == null) {
      StringBuilder sb = new StringBuilder("[");
      for (int i = 0; i < call.args.size(); i++) {
        sb.append(i > 0 ? ", " : "").append(XPathValues.string(call.args.get(i).eval(c), c));
      }
      throw new XPathError("Attempting to call an extension function " + sb
          + "], but an extension provider is not set.");
    }
    String uri = "";
    if (c.shared.namespaces != null) {
      String u = c.shared.namespaces.getNamespaceURI(call.prefix);
      uri = u == null ? "" : u;
    }
    XPathFunction f = c.shared.functions.resolveFunction(new QName(uri, call.local), call.args.size());
    if (f == null) {
      throw new XPathError("java.lang.NullPointerException: Cannot invoke "
          + "\"javax.xml.xpath.XPathFunction.evaluate(java.util.List)\" because \"xpathFunction\" is null");
    }
    List<Object> values = new ArrayList<Object>();
    for (XPathExpr a : call.args) {
      Object v = a.eval(c);
      if (v instanceof NodeSet) {
        final NodeSet set = (NodeSet) v;
        values.add(new NodeList() {
          public Node item(int i) {
            return i >= 0 && i < set.size() ? set.get(i) : null;
          }

          public int getLength() {
            return set.size();
          }
        });
      } else {
        values.add(v);
      }
    }
    try {
      return fromResult(f.evaluate(values), c);
    } catch (XPathFunctionException e) {
      throw new XPathError(e.getMessage());
    }
  }

  private static Object fromResult(Object v, XPathContext c) {
    return XPathExpr.fromJava(v, c);
  }
}
