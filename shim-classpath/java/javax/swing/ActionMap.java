/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ActionMap implements Serializable {
  private Map<Object, Action> arrayTable;
  private ActionMap parent;

  public ActionMap() { }

  public void setParent(ActionMap map) { parent = map; }

  public ActionMap getParent() { return parent; }

  public void put(Object key, Action action) {
    if (key == null) {
      return;
    }
    if (action == null) {
      remove(key);
    } else {
      if (arrayTable == null) {
        arrayTable = new LinkedHashMap<>();
      }
      arrayTable.put(key, action);
    }
  }

  public Action get(Object key) {
    Action value = arrayTable == null ? null : arrayTable.get(key);
    if (value == null) {
      ActionMap p = getParent();
      if (p != null) {
        return p.get(key);
      }
    }
    return value;
  }

  public void remove(Object key) {
    if (arrayTable != null) {
      arrayTable.remove(key);
    }
  }

  public void clear() {
    if (arrayTable != null) {
      arrayTable.clear();
    }
  }

  public Object[] keys() {
    if (arrayTable == null) {
      return null;
    }
    return arrayTable.keySet().toArray();
  }

  public int size() { return arrayTable == null ? 0 : arrayTable.size(); }

  public Object[] allKeys() {
    int count = size();
    ActionMap p = getParent();
    if (count == 0) {
      return p != null ? p.allKeys() : keys();
    }
    if (p == null) {
      return keys();
    }
    Object[] keys = keys();
    Object[] pKeys = p.allKeys();
    if (pKeys == null) {
      return keys;
    }
    if (keys == null) {
      return pKeys;
    }
    Set<Object> all = new LinkedHashSet<>();
    for (Object k : keys) {
      all.add(k);
    }
    for (Object k : pKeys) {
      all.add(k);
    }
    return all.toArray();
  }
}
