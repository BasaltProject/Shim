/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

public interface XPathExpression {
  Object evaluate(Object p0, javax.xml.namespace.QName p1) throws XPathExpressionException;

  String evaluate(Object p0) throws XPathExpressionException;

  Object evaluate(org.xml.sax.InputSource p0, javax.xml.namespace.QName p1) throws XPathExpressionException;

  String evaluate(org.xml.sax.InputSource p0) throws XPathExpressionException;

  default <T> T evaluateExpression(Object p0, Class<T> p1) throws XPathExpressionException {
    throw new UnsupportedOperationException();

  }

  default XPathEvaluationResult<?> evaluateExpression(Object p0) throws XPathExpressionException {
    throw new UnsupportedOperationException();

  }

  default <T> T evaluateExpression(org.xml.sax.InputSource p0, Class<T> p1) throws XPathExpressionException {
    throw new UnsupportedOperationException();

  }

  default XPathEvaluationResult<?> evaluateExpression(org.xml.sax.InputSource p0) throws XPathExpressionException {
    throw new UnsupportedOperationException();

  }
}
