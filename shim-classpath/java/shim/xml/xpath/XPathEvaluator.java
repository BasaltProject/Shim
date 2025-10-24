/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathNodes;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import shim.xml.xpath.XPathValues.NodeSet;

final class XPathEvaluator {
  private final XPathExpr expr;
  private final XPathVariableResolver variables;
  private final XPathFunctionResolver functions;
  private final NamespaceContext namespaces;

  XPathEvaluator(String text, XPathVariableResolver variables, XPathFunctionResolver functions,
      NamespaceContext namespaces, boolean lenient) throws XPathExpressionException {
    if (text == null) {
      throw new NullPointerException("XPath expression argument can not be null");
    }
    this.variables = variables;
    this.functions = functions;
    this.namespaces = namespaces;
    try {
      this.expr = XPathParser.parse(text, namespaces, lenient);
    } catch (XPathError e) {
      throw new XPathExpressionException(e);
    }
  }

  private Object run(Object item) throws XPathExpressionException {
    XPathContext.Shared shared = new XPathContext.Shared();
    shared.variables = variables;
    shared.functions = functions;
    shared.namespaces = namespaces;
    if (!(item instanceof Node)
        && (expr instanceof XPathExpr.Path || expr instanceof XPathExpr.Filter || expr instanceof XPathExpr.Union)) {
      throw new XPathExpressionException(
          new XPathError("The context can not be null when the operation is context-dependent."));
    }
    XPathContext c = item instanceof Node ? new XPathContext(shared, (Node) item, 1, 1, true)
        : new XPathContext(shared, null, 1, 1, false);
    try {
      Object v = expr.eval(c);
      if (v instanceof NodeSet) {
        XPathValues.sort((NodeSet) v, c);
      }
      return new Object[] {v, c};
    } catch (XPathError e) {
      throw new XPathExpressionException(e);
    } catch (RuntimeException e) {
      throw new XPathExpressionException(e);
    }
  }

  private static Document parse(InputSource source) throws XPathExpressionException {
    if (source == null) {
      throw new NullPointerException("Source argument can not be null");
    }
    try {
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      return f.newDocumentBuilder().parse(source);
    } catch (ParserConfigurationException e) {
      throw new XPathExpressionException(e);
    } catch (SAXException e) {
      throw new XPathExpressionException(e);
    } catch (IOException e) {
      throw new XPathExpressionException(e);
    }
  }

  Object evaluate(Object item, QName returnType) throws XPathExpressionException {
    if (returnType == null) {
      throw new NullPointerException("returnType argument can not be null");
    }
    if (!returnType.equals(XPathConstants.STRING) && !returnType.equals(XPathConstants.NUMBER)
        && !returnType.equals(XPathConstants.BOOLEAN) && !returnType.equals(XPathConstants.NODESET)
        && !returnType.equals(XPathConstants.NODE)) {
      throw new IllegalArgumentException("UnSupported Return Type : " + returnType);
    }
    Object[] r = (Object[]) run(item);
    Object v = r[0];
    XPathContext c = (XPathContext) r[1];
    try {
      if (returnType.equals(XPathConstants.STRING)) {
        return XPathValues.string(v, c);
      }
      if (returnType.equals(XPathConstants.NUMBER)) {
        return Double.valueOf(XPathValues.number(v, c));
      }
      if (returnType.equals(XPathConstants.BOOLEAN)) {
        return Boolean.valueOf(XPathValues.bool(v));
      }
      NodeSet set = XPathExpr.nodes(v);
      if (returnType.equals(XPathConstants.NODESET)) {
        return nodeList(set);
      }
      return set.isEmpty() ? null : set.get(0);
    } catch (XPathError e) {
      throw new XPathExpressionException(e);
    }
  }

  String evaluate(Object item) throws XPathExpressionException {
    return (String) evaluate(item, XPathConstants.STRING);
  }

  Object evaluate(InputSource source, QName returnType) throws XPathExpressionException {
    if (returnType == null) {
      throw new NullPointerException("returnType argument can not be null");
    }
    return evaluate(parse(source), returnType);
  }

  String evaluate(InputSource source) throws XPathExpressionException {
    return evaluate(parse(source));
  }

  XPathEvaluationResult<?> evaluateExpression(Object item) throws XPathExpressionException {
    Object[] r = (Object[]) run(item);
    Object v = r[0];
    final XPathContext c = (XPathContext) r[1];
    if (v instanceof NodeSet) {
      return result(XPathEvaluationResult.XPathResultType.NODESET, nodes((NodeSet) v));
    }
    if (v instanceof Boolean) {
      return result(XPathEvaluationResult.XPathResultType.BOOLEAN, v);
    }
    if (v instanceof Double) {
      return result(XPathEvaluationResult.XPathResultType.NUMBER, v);
    }
    return result(XPathEvaluationResult.XPathResultType.STRING, v);
  }

  @SuppressWarnings("unchecked")
  <T> T evaluateExpression(Object item, Class<T> type) throws XPathExpressionException {
    if (type == null) {
      throw new NullPointerException("returnType argument can not be null");
    }
    if (type != Boolean.class && type != Double.class && type != Integer.class && type != Long.class
        && type != String.class && type != Node.class && type != XPathNodes.class) {
      throw new IllegalArgumentException("UnSupported Return Type : " + type);
    }
    Object[] r = (Object[]) run(item);
    Object v = r[0];
    XPathContext c = (XPathContext) r[1];
    try {
      if (type == Boolean.class) {
        return (T) Boolean.valueOf(XPathValues.bool(v));
      }
      if (type == String.class) {
        return (T) XPathValues.string(v, c);
      }
      if (type == Double.class) {
        return (T) Double.valueOf(XPathValues.number(v, c));
      }
      if (type == Integer.class || type == Long.class) {
        double d = XPathValues.number(v, c);
        return (T) (type == Integer.class ? (Object) Integer.valueOf((int) d) : (Object) Long.valueOf((long) d));
      }
      NodeSet set = XPathExpr.nodes(v);
      if (type == XPathNodes.class) {
        return (T) nodes(set);
      }
      return (T) (set.isEmpty() ? null : set.get(0));
    } catch (XPathError e) {
      throw new XPathExpressionException(e);
    }
  }

  XPathEvaluationResult<?> evaluateExpression(InputSource source) throws XPathExpressionException {
    return evaluateExpression(parse(source));
  }

  <T> T evaluateExpression(InputSource source, Class<T> type) throws XPathExpressionException {
    return evaluateExpression(parse(source), type);
  }

  private static NodeList nodeList(final List<Node> set) {
    return new NodeList() {
      public Node item(int i) {
        return i >= 0 && i < set.size() ? set.get(i) : null;
      }

      public int getLength() {
        return set.size();
      }
    };
  }

  private static XPathNodes nodes(final NodeSet set) {
    return new XPathNodes() {
      public Iterator<Node> iterator() {
        return set.iterator();
      }

      public int size() {
        return set.size();
      }

      public Node get(int index) throws XPathException {
        if (index < 0 || index >= set.size()) {
          throw new IndexOutOfBoundsException("Index " + index + " is out of bounds");
        }
        return set.get(index);
      }
    };
  }

  private static <T> XPathEvaluationResult<T> result(final XPathEvaluationResult.XPathResultType type,
      final T value) {
    return new XPathEvaluationResult<T>() {
      public XPathResultType type() {
        return type;
      }

      public T value() {
        return value;
      }
    };
  }
}
