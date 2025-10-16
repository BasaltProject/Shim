/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

public class DomImplementationSource implements DOMImplementationSource {
  public DOMImplementation getDOMImplementation(String features) {
    DomImplementation impl = DomImplementation.getInstance();
    if (matches(impl, features)) {
      return impl;
    }
    return null;
  }

  public DOMImplementationList getDOMImplementationList(String features) {
    final List<DOMImplementation> list = new ArrayList<DOMImplementation>();
    DomImplementation impl = DomImplementation.getInstance();
    if (matches(impl, features)) {
      list.add(impl);
    }
    return new DOMImplementationList() {
      public DOMImplementation item(int index) {
        return index >= 0 && index < list.size() ? list.get(index) : null;
      }

      public int getLength() {
        return list.size();
      }
    };
  }

  private static boolean matches(DomImplementation impl, String features) {
    StringTokenizer st = new StringTokenizer(features);
    String feature = null;
    String version = null;
    if (st.hasMoreTokens()) {
      feature = st.nextToken();
    }
    while (feature != null) {
      boolean isVersion = false;
      if (st.hasMoreTokens()) {
        String token = st.nextToken();
        char first = token.charAt(0);
        if (Character.isDigit(first)) {
          version = token;
          isVersion = true;
        } else {
          version = null;
          if (!impl.hasFeature(feature, null)) {
            return false;
          }
          feature = token;
          continue;
        }
      }
      if (!impl.hasFeature(feature, version)) {
        return false;
      }
      if (isVersion) {
        feature = st.hasMoreTokens() ? st.nextToken() : null;
      } else {
        feature = null;
      }
    }
    return true;
  }
}
