/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class InputMap implements Serializable {
  private Map<KeyStroke, Object> arrayTable;
  private InputMap parent;

  public InputMap() { }

  public void setParent(InputMap map) { parent = map; }

  public InputMap getParent() { return parent; }

  public void put(KeyStroke keyStroke, Object actionMapKey) {
    if (keyStroke == null) {
      return;
    }
    if (actionMapKey == null) {
      remove(keyStroke);
    } else {
      if (arrayTable == null) {
        arrayTable = new LinkedHashMap<>();
      }
      arrayTable.put(keyStroke, actionMapKey);
    }
  }

  public Object get(KeyStroke keyStroke) {
    Object value = arrayTable == null ? null : arrayTable.get(keyStroke);
    if (value == null) {
      InputMap p = getParent();
      if (p != null) {
        return p.get(keyStroke);
      }
    }
    return value;
  }

  public void remove(KeyStroke key) {
    if (arrayTable != null) {
      arrayTable.remove(key);
    }
  }

  public void clear() {
    if (arrayTable != null) {
      arrayTable.clear();
    }
  }

  public KeyStroke[] keys() {
    if (arrayTable == null) {
      return null;
    }
    return arrayTable.keySet().toArray(new KeyStroke[0]);
  }

  public int size() { return arrayTable == null ? 0 : arrayTable.size(); }

  public KeyStroke[] allKeys() {
    int count = size();
    InputMap p = getParent();
    if (count == 0) {
      return p != null ? p.allKeys() : keys();
    }
    if (p == null) {
      return keys();
    }
    KeyStroke[] keys = keys();
    KeyStroke[] pKeys = p.allKeys();
    if (pKeys == null) {
      return keys;
    }
    if (keys == null) {
      return pKeys;
    }
    Set<KeyStroke> all = new java.util.LinkedHashSet<>();
    for (KeyStroke k : keys) {
      all.add(k);
    }
    for (KeyStroke k : pKeys) {
      all.add(k);
    }
    return all.toArray(new KeyStroke[0]);
  }
}
