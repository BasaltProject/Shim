/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XPathType {
  private final String expression;
  private final Filter filter;
  private Map<String, String> nsMap;

  public XPathType(String expression, Filter filter) {
    if (expression == null) {
      throw new NullPointerException("expression cannot be null");
    }
    if (filter == null) {
      throw new NullPointerException("filter cannot be null");
    }
    this.expression = expression;
    this.filter = filter;
    this.nsMap = Collections.emptyMap();
  }

  @SuppressWarnings("rawtypes")
  public XPathType(String expression, Filter filter, Map namespaceMap) {
    this(expression, filter);
    Map<?, ?> copy = new HashMap<Object, Object>(namespaceMap);
    for (Map.Entry<?, ?> e : copy.entrySet()) {
      if (!(e.getKey() instanceof String) || !(e.getValue() instanceof String)) {
        throw new ClassCastException("not a String");
      }
    }
    @SuppressWarnings("unchecked")
    Map<String, String> temp = (Map<String, String>) copy;
    nsMap = Collections.unmodifiableMap(temp);
  }

  public String getExpression() {
    return expression;
  }

  public Filter getFilter() {
    return filter;
  }

  public Map<String, String> getNamespaceMap() {
    return nsMap;
  }

  public static class Filter {
    private final String operation;

    private Filter(String operation) {
      this.operation = operation;
    }

    public String toString() {
      return operation;
    }

    public static final Filter INTERSECT = new Filter("intersect");
    public static final Filter SUBTRACT = new Filter("subtract");
    public static final Filter UNION = new Filter("union");
  }
}
