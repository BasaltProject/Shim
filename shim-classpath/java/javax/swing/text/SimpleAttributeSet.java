/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleAttributeSet implements MutableAttributeSet, Serializable, Cloneable {
  public static final AttributeSet EMPTY = new EmptyAttributeSet();

  private Map<Object, Object> table = new LinkedHashMap<>(3);

  public SimpleAttributeSet() { }

  public SimpleAttributeSet(AttributeSet source) { addAttributes(source); }

  public boolean isEmpty() { return table.isEmpty(); }

  public int getAttributeCount() { return table.size(); }

  public boolean isDefined(Object attrName) { return table.containsKey(attrName); }

  public boolean isEqual(AttributeSet attr) {
    return getAttributeCount() == attr.getAttributeCount() && containsAttributes(attr);
  }

  public AttributeSet copyAttributes() { return (AttributeSet) clone(); }

  public Enumeration<?> getAttributeNames() { return java.util.Collections.enumeration(table.keySet()); }

  public Object getAttribute(Object name) {
    Object value = table.get(name);
    if (value == null) {
      AttributeSet parent = getResolveParent();
      if (parent != null) {
        value = parent.getAttribute(name);
      }
    }
    return value;
  }

  public boolean containsAttribute(Object name, Object value) {
    return value != null && value.equals(getAttribute(name));
  }

  public boolean containsAttributes(AttributeSet attributes) {
    Enumeration<?> names = attributes.getAttributeNames();
    while (names.hasMoreElements()) {
      Object name = names.nextElement();
      Object v = attributes.getAttribute(name);
      if (v == null || !v.equals(getAttribute(name))) {
        return false;
      }
    }
    return true;
  }

  public void addAttribute(Object name, Object value) { table.put(name, value); }

  public void addAttributes(AttributeSet attributes) {
    Enumeration<?> names = attributes.getAttributeNames();
    while (names.hasMoreElements()) {
      Object name = names.nextElement();
      addAttribute(name, attributes.getAttribute(name));
    }
  }

  public void removeAttribute(Object name) { table.remove(name); }

  public void removeAttributes(Enumeration<?> names) {
    while (names.hasMoreElements()) {
      removeAttribute(names.nextElement());
    }
  }

  public void removeAttributes(AttributeSet attributes) {
    if (attributes == this) {
      table.clear();
      return;
    }
    Enumeration<?> names = attributes.getAttributeNames();
    while (names.hasMoreElements()) {
      Object name = names.nextElement();
      Object value = attributes.getAttribute(name);
      if (value != null && value.equals(table.get(name))) {
        removeAttribute(name);
      }
    }
  }

  public AttributeSet getResolveParent() { return (AttributeSet) table.get(StyleConstants.ResolveAttribute); }

  public void setResolveParent(AttributeSet parent) { addAttribute(StyleConstants.ResolveAttribute, parent); }

  public Object clone() {
    SimpleAttributeSet attr;
    try {
      attr = (SimpleAttributeSet) super.clone();
      attr.table = new LinkedHashMap<>(table);
    } catch (CloneNotSupportedException cnse) {
      attr = null;
    }
    return attr;
  }

  public int hashCode() { return table.hashCode(); }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof AttributeSet && isEqual((AttributeSet) obj);
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    for (Map.Entry<Object, Object> e : table.entrySet()) {
      Object v = e.getValue();
      s.append(e.getKey()).append('=').append(v instanceof AttributeSet ? "AttributeSet" : String.valueOf(v)).append(' ');
    }
    return s.toString();
  }

  static class EmptyAttributeSet implements AttributeSet, Serializable {
    public int getAttributeCount() { return 0; }

    public boolean isDefined(Object attrName) { return false; }

    public boolean isEqual(AttributeSet attr) { return attr.getAttributeCount() == 0; }

    public AttributeSet copyAttributes() { return this; }

    public Object getAttribute(Object key) { return null; }

    public Enumeration<?> getAttributeNames() { return java.util.Collections.emptyEnumeration(); }

    public boolean containsAttribute(Object name, Object value) { return false; }

    public boolean containsAttributes(AttributeSet attributes) { return attributes.getAttributeCount() == 0; }

    public AttributeSet getResolveParent() { return null; }

    public boolean equals(Object obj) {
      return this == obj || (obj instanceof AttributeSet && ((AttributeSet) obj).getAttributeCount() == 0);
    }

    public int hashCode() { return 0; }
  }
}
