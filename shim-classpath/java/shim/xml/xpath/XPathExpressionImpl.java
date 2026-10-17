/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.InputSource;

final class XPathExpressionImpl implements XPathExpression {
  private final XPathEvaluator evaluator;

  XPathExpressionImpl(XPathEvaluator evaluator) {
    this.evaluator = evaluator;
  }

  public Object evaluate(Object item, QName returnType) throws XPathExpressionException {
    return evaluator.evaluate(item, returnType);
  }

  public String evaluate(Object item) throws XPathExpressionException {
    return evaluator.evaluate(item);
  }

  public Object evaluate(InputSource source, QName returnType) throws XPathExpressionException {
    return evaluator.evaluate(source, returnType);
  }

  public String evaluate(InputSource source) throws XPathExpressionException {
    return evaluator.evaluate(source);
  }

  public <T> T evaluateExpression(Object item, Class<T> type) throws XPathExpressionException {
    return evaluator.evaluateExpression(item, type);
  }

  public XPathEvaluationResult<?> evaluateExpression(Object item) throws XPathExpressionException {
    return evaluator.evaluateExpression(item);
  }

  public <T> T evaluateExpression(InputSource source, Class<T> type) throws XPathExpressionException {
    return evaluator.evaluateExpression(source, type);
  }

  public XPathEvaluationResult<?> evaluateExpression(InputSource source) throws XPathExpressionException {
    return evaluator.evaluateExpression(source);
  }
}
