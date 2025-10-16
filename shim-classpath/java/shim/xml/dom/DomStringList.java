/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.List;
import org.w3c.dom.DOMStringList;

final class DomStringList implements DOMStringList {
  private final List<String> list;

  DomStringList(List<String> list) {
    this.list = list;
  }

  public String item(int index) {
    return index >= 0 && index < list.size() ? list.get(index) : null;
  }

  public int getLength() {
    return list.size();
  }

  public boolean contains(String str) {
    return list.contains(str);
  }
}
