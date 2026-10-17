/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

public interface XPath {
  void reset();

  void setXPathVariableResolver(XPathVariableResolver p0);

  XPathVariableResolver getXPathVariableResolver();

  void setXPathFunctionResolver(XPathFunctionResolver p0);

  XPathFunctionResolver getXPathFunctionResolver();

  void setNamespaceContext(javax.xml.namespace.NamespaceContext p0);

  javax.xml.namespace.NamespaceContext getNamespaceContext();

  XPathExpression compile(String p0) throws XPathExpressionException;

  Object evaluate(String p0, Object p1, javax.xml.namespace.QName p2) throws XPathExpressionException;

  String evaluate(String p0, Object p1) throws XPathExpressionException;

  Object evaluate(String p0, org.xml.sax.InputSource p1, javax.xml.namespace.QName p2) throws XPathExpressionException;

  String evaluate(String p0, org.xml.sax.InputSource p1) throws XPathExpressionException;

  default <T> T evaluateExpression(String p0, Object p1, Class<T> p2) throws XPathExpressionException {
    throw new UnsupportedOperationException();
  }

  default XPathEvaluationResult<?> evaluateExpression(String p0, Object p1) throws XPathExpressionException {
    throw new UnsupportedOperationException();
  }

  default <T> T evaluateExpression(String p0, org.xml.sax.InputSource p1, Class<T> p2) throws XPathExpressionException {
    throw new UnsupportedOperationException();
  }

  default XPathEvaluationResult<?> evaluateExpression(String p0, org.xml.sax.InputSource p1) throws XPathExpressionException {
    throw new UnsupportedOperationException();
  }
}
