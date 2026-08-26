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

public final class ExcC14NParameterSpec implements C14NMethodParameterSpec {
  private List<String> preList;

  public static final String DEFAULT = "#default";

  public ExcC14NParameterSpec() {
    preList = Collections.emptyList();
  }

  @SuppressWarnings("rawtypes")
  public ExcC14NParameterSpec(List prefixList) {
    if (prefixList == null) {
      throw new NullPointerException("prefixList cannot be null");
    }
    List<?> copy = new ArrayList<Object>(prefixList);
    for (int i = 0, size = copy.size(); i < size; i++) {
      if (!(copy.get(i) instanceof String)) {
        throw new ClassCastException("not a String");
      }
    }
    @SuppressWarnings("unchecked")
    List<String> temp = (List<String>) copy;
    preList = Collections.unmodifiableList(temp);
  }

  public List<String> getPrefixList() {
    return preList;
  }
}
