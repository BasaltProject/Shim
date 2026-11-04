/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class XPathFilter2ParameterSpec implements TransformParameterSpec {
  private final List<XPathType> xPathList;

  @SuppressWarnings("rawtypes")
  public XPathFilter2ParameterSpec(List xPathList) {
    if (xPathList == null) {
      throw new NullPointerException("xPathList cannot be null");
    }
    List<?> copy = new ArrayList<Object>(xPathList);
    if (copy.isEmpty()) {
      throw new IllegalArgumentException("xPathList cannot be empty");
    }
    for (int i = 0, size = copy.size(); i < size; i++) {
      if (!(copy.get(i) instanceof XPathType)) {
        throw new ClassCastException("xPathList[" + i + "] is not a valid type");
      }
    }
    @SuppressWarnings("unchecked")
    List<XPathType> temp = (List<XPathType>) copy;
    this.xPathList = Collections.unmodifiableList(temp);
  }

  public List<XPathType> getXPathList() {
    return xPathList;
  }
}
