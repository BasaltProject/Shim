/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

import javax.xml.namespace.QName;
import org.w3c.dom.Node;

public interface XPathEvaluationResult<T> {
  enum XPathResultType {
    ANY(Object.class, null),
    BOOLEAN(Boolean.class, XPathConstants.BOOLEAN),
    NUMBER(Number.class, XPathConstants.NUMBER),
    STRING(String.class, XPathConstants.STRING),
    NODESET(XPathNodes.class, XPathConstants.NODESET),
    NODE(Node.class, XPathConstants.NODE);

    private final Class<?> clazz;
    private final QName qname;

    XPathResultType(Class<?> clazz, QName qname) {
      this.clazz = clazz;
      this.qname = qname;
    }

    public static QName getQNameType(Class<?> type) {
      for (XPathResultType t : values()) {
        if (t != ANY && t.clazz.isAssignableFrom(type)) {
          return t.qname;
        }
      }
      return null;
    }
  }

  XPathResultType type();

  T value();
}
