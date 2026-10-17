/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.xml.sax.InputSource;

final class XPathImpl implements XPath {
  private final XPathVariableResolver factoryVariables;
  private final XPathFunctionResolver factoryFunctions;
  private XPathVariableResolver variables;
  private XPathFunctionResolver functions;
  private NamespaceContext namespaces;
  private NamespaceContext evaluationNamespaces;

  XPathImpl(XPathVariableResolver variables, XPathFunctionResolver functions) {
    this.factoryVariables = variables;
    this.factoryFunctions = functions;
    this.variables = variables;
    this.functions = functions;
  }

  public void reset() {
    variables = factoryVariables;
    functions = factoryFunctions;
    namespaces = null;
  }

  public void setXPathVariableResolver(XPathVariableResolver resolver) {
    if (resolver == null) {
      throw new NullPointerException("XPathVariableResolver argument can not be null");
    }
    variables = resolver;
  }

  public XPathVariableResolver getXPathVariableResolver() {
    return variables;
  }

  public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
    if (resolver == null) {
      throw new NullPointerException("XPathFunctionResolver argument can not be null");
    }
    functions = resolver;
  }

  public XPathFunctionResolver getXPathFunctionResolver() {
    return functions;
  }

  public void setNamespaceContext(NamespaceContext context) {
    if (context == null) {
      throw new NullPointerException("NamespaceContext argument can not be null");
    }
    namespaces = context;
    evaluationNamespaces = context;
  }

  public NamespaceContext getNamespaceContext() {
    return namespaces;
  }

  private XPathEvaluator compileInternal(String expression) throws XPathExpressionException {
    return new XPathEvaluator(expression, variables, functions, evaluationNamespaces, evaluationNamespaces == null);
  }

  private XPathEvaluator compileFor(String expression, Object item) throws XPathExpressionException {
    return compileInternal(expression);
  }

  public XPathExpression compile(String expression) throws XPathExpressionException {
    return new XPathExpressionImpl(compileInternal(expression));
  }

  public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
    return compileFor(expression, item).evaluate(item, returnType);
  }

  public String evaluate(String expression, Object item) throws XPathExpressionException {
    return compileFor(expression, item).evaluate(item);
  }

  public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
    if (expression == null) {
      throw new NullPointerException("XPath expression argument can not be null");
    }
    return compileInternal(expression).evaluate(source, returnType);
  }

  public String evaluate(String expression, InputSource source) throws XPathExpressionException {
    return compileInternal(expression).evaluate(source);
  }

  public <T> T evaluateExpression(String expression, Object item, Class<T> type) throws XPathExpressionException {
    return compileFor(expression, item).evaluateExpression(item, type);
  }

  public XPathEvaluationResult<?> evaluateExpression(String expression, Object item)
      throws XPathExpressionException {
    return compileFor(expression, item).evaluateExpression(item);
  }

  public <T> T evaluateExpression(String expression, InputSource source, Class<T> type)
      throws XPathExpressionException {
    return compileInternal(expression).evaluateExpression(source, type);
  }

  public XPathEvaluationResult<?> evaluateExpression(String expression, InputSource source)
      throws XPathExpressionException {
    return compileInternal(expression).evaluateExpression(source);
  }
}
