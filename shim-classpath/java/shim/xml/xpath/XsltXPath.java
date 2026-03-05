/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Node;
import shim.xml.xpath.XPathValues.NodeSet;

/** Evaluates XPath expressions with an explicit context position and size, as the XSLT processor needs. */
public final class XsltXPath {
  private final XPathExpr expr;
  private final XPathContext.Shared shared = new XPathContext.Shared();

  public XsltXPath(String text, NamespaceContext namespaces, XPathVariableResolver variables,
      XPathFunctionResolver functions) {
    shared.variables = variables;
    shared.functions = functions;
    shared.namespaces = namespaces;
    this.expr = XPathParser.parseXslt(text, namespaces);
  }

  /** Returns a String, Double, Boolean or a List of nodes in document order. */
  public Object evaluate(Node node, int position, int size) {
    XPathContext c = new XPathContext(shared, node, position, size, true);
    Object v = expr.eval(c);
    if (v instanceof NodeSet) {
      NodeSet set = (NodeSet) v;
      XPathValues.sort(set, c);
      return new ArrayList<Node>(set);
    }
    return v;
  }

  private XPathContext plain() {
    return new XPathContext(shared, null, 1, 1, false);
  }

  @SuppressWarnings("unchecked")
  private static Object normalize(Object v) {
    if (v instanceof List) {
      NodeSet set = new NodeSet();
      set.addAll((List<Node>) v);
      return set;
    }
    return v;
  }

  public static String string(Object v) {
    XPathContext c = new XPathContext(new XPathContext.Shared(), null, 1, 1, false);
    return XPathValues.string(normalize(v), c);
  }

  public static double number(Object v) {
    XPathContext c = new XPathContext(new XPathContext.Shared(), null, 1, 1, false);
    return XPathValues.number(normalize(v), c);
  }

  public static boolean bool(Object v) {
    return XPathValues.bool(normalize(v));
  }

  public static String numberToString(double d) {
    return XPathValues.numberToString(d);
  }

  public static double parseNumber(String s) {
    return XPathValues.parseNumber(s);
  }

  public static String stringValue(Node n) {
    return XPathValues.stringValue(n, new XPathContext(new XPathContext.Shared(), null, 1, 1, false));
  }
}
