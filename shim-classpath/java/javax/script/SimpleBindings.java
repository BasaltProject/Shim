/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SimpleBindings implements Bindings {
  private final Map<String, Object> map;

  public SimpleBindings(Map<String, Object> m) {
    if (m == null) {
      throw new NullPointerException();
    }
    this.map = m;
  }

  public SimpleBindings() {
    this(new HashMap<String, Object>());
  }

  @Override
  public Object put(String name, Object value) {
    checkKey(name);
    return map.put(name, value);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> toMerge) {
    if (toMerge == null) {
      throw new NullPointerException("toMerge map is null");
    }
    for (Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
      String key = entry.getKey();
      checkKey(key);
      put(key, entry.getValue());
    }
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    checkKey(key);
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Set<Map.Entry<String, Object>> entrySet() {
    return map.entrySet();
  }

  @Override
  public Object get(Object key) {
    checkKey(key);
    return map.get(key);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public Object remove(Object key) {
    checkKey(key);
    return map.remove(key);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Collection<Object> values() {
    return map.values();
  }

  private void checkKey(Object key) {
    if (key == null) {
      throw new NullPointerException("key can not be null");
    }
    if (!(key instanceof String)) {
      throw new ClassCastException("key should be a String");
    }
    if (key.equals("")) {
      throw new IllegalArgumentException("key can not be empty");
    }
  }
}
