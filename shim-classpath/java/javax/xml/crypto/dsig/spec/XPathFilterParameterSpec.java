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

public final class XPathFilterParameterSpec implements TransformParameterSpec {
  private String xPath;
  private Map<String, String> nsMap;

  public XPathFilterParameterSpec(String xPath) {
    if (xPath == null) {
      throw new NullPointerException();
    }
    this.xPath = xPath;
    this.nsMap = Collections.emptyMap();
  }

  @SuppressWarnings("rawtypes")
  public XPathFilterParameterSpec(String xPath, Map namespaceMap) {
    if (xPath == null || namespaceMap == null) {
      throw new NullPointerException();
    }
    this.xPath = xPath;
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

  public String getXPath() {
    return xPath;
  }

  public Map<String, String> getNamespaceMap() {
    return nsMap;
  }
}
