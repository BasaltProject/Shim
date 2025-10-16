/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.helpers;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.AttributeList;

@Deprecated
public class AttributeListImpl implements AttributeList {
  private final List<String> names = new ArrayList<String>();
  private final List<String> types = new ArrayList<String>();
  private final List<String> values = new ArrayList<String>();

  public AttributeListImpl() {
  }

  public AttributeListImpl(AttributeList atts) {
    setAttributeList(atts);
  }

  public void setAttributeList(AttributeList atts) {
    int count = atts.getLength();
    clear();
    for (int i = 0; i < count; i++) {
      addAttribute(atts.getName(i), atts.getType(i), atts.getValue(i));
    }
  }

  public void addAttribute(String name, String type, String value) {
    names.add(name);
    types.add(type);
    values.add(value);
  }

  public void removeAttribute(String name) {
    int i = names.indexOf(name);
    if (i >= 0) {
      names.remove(i);
      types.remove(i);
      values.remove(i);
    }
  }

  public void clear() {
    names.clear();
    types.clear();
    values.clear();
  }

  public int getLength() {
    return names.size();
  }

  public String getName(int i) {
    if (i < 0) {
      return null;
    }
    try {
      return names.get(i);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public String getType(int i) {
    if (i < 0) {
      return null;
    }
    try {
      return types.get(i);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public String getValue(int i) {
    if (i < 0) {
      return null;
    }
    try {
      return values.get(i);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public String getType(String name) {
    return getType(names.indexOf(name));
  }

  public String getValue(String name) {
    return getValue(names.indexOf(name));
  }
}
