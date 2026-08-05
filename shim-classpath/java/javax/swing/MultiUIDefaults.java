/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class MultiUIDefaults extends UIDefaults {
  private UIDefaults[] tables;

  public MultiUIDefaults(UIDefaults[] defaults) {
    super();
    tables = defaults;
  }

  public MultiUIDefaults() {
    super();
    tables = new UIDefaults[0];
  }

  public Object get(Object key) {
    Object value = super.get(key);
    if (value != null) {
      return value;
    }
    for (UIDefaults table : tables) {
      value = (table != null) ? table.get(key) : null;
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  public Object get(Object key, Locale l) {
    Object value = super.get(key, l);
    if (value != null) {
      return value;
    }
    for (UIDefaults table : tables) {
      value = (table != null) ? table.get(key, l) : null;
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  public int size() { return entrySet().size(); }

  public boolean isEmpty() { return size() == 0; }

  public Enumeration<Object> keys() { return new MultiUIDefaultsEnumerator(MultiUIDefaultsEnumerator.Type.KEYS, entrySet()); }

  public Enumeration<Object> elements() {
    return new MultiUIDefaultsEnumerator(MultiUIDefaultsEnumerator.Type.ELEMENTS, entrySet());
  }

  public Set<Map.Entry<Object, Object>> entrySet() {
    Set<Map.Entry<Object, Object>> set = new HashSet<Map.Entry<Object, Object>>();
    for (int i = tables.length - 1; i >= 0; i--) {
      if (tables[i] != null) {
        set.addAll(tables[i].entrySet());
      }
    }
    set.addAll(super.entrySet());
    return set;
  }

  protected void getUIError(String msg) {
    if (tables.length > 0) {
      tables[0].getUIError(msg);
    } else {
      super.getUIError(msg);
    }
  }

  public Object remove(Object key) {
    Object value = null;
    for (int i = tables.length - 1; i >= 0; i--) {
      if (tables[i] != null) {
        Object v = tables[i].remove(key);
        if (v != null) {
          value = v;
        }
      }
    }
    Object v = super.remove(key);
    if (v != null) {
      value = v;
    }
    return value;
  }

  public void clear() {
    super.clear();
    for (UIDefaults table : tables) {
      if (table != null) {
        table.clear();
      }
    }
  }

  public synchronized String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("{");
    Enumeration<?> keys = keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      buf.append(key).append("=").append(get(key)).append(", ");
    }
    int length = buf.length();
    if (length > 1) {
      buf.delete(length - 2, length);
    }
    buf.append("}");
    return buf.toString();
  }

  static class MultiUIDefaultsEnumerator implements Enumeration<Object> {
    public enum Type {
      KEYS, ELEMENTS;
    }

    private final List<Object> items = new ArrayList<Object>();
    private int index;

    MultiUIDefaultsEnumerator(Type type, Set<Map.Entry<Object, Object>> entries) {
      for (Map.Entry<Object, Object> e : entries) {
        items.add(type == Type.KEYS ? e.getKey() : e.getValue());
      }
    }

    public boolean hasMoreElements() { return index < items.size(); }

    public Object nextElement() { return items.get(index++); }
  }
}
