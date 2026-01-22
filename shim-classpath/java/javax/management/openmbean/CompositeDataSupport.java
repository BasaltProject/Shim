/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CompositeDataSupport implements CompositeData, Serializable {
  private static final long serialVersionUID = 8003518976613702244L;

  private final SortedMap<String, Object> contents;
  private final CompositeType compositeType;

  public CompositeDataSupport(CompositeType compositeType, String[] itemNames, Object[] itemValues)
      throws OpenDataException {
    this(makeMap(itemNames, itemValues), compositeType);
  }

  public CompositeDataSupport(CompositeType compositeType, Map<String, ?> items) throws OpenDataException {
    this(makeMap(items), compositeType);
  }

  private CompositeDataSupport(SortedMap<String, Object> items, CompositeType compositeType)
      throws OpenDataException {
    if (compositeType == null) {
      throw new IllegalArgumentException("Argument compositeType cannot be null.");
    }
    if (items.containsKey("")) {
      throw new IllegalArgumentException("Argument itemNames[] cannot contain null or empty string");
    }
    if (compositeType.keySet().size() != items.size()) {
      throw new InvalidOpenTypeException("Number of items does not match the composite type");
    }
    for (Map.Entry<String, Object> e : items.entrySet()) {
      String name = e.getKey();
      Object value = e.getValue();
      OpenType<?> itemType = compositeType.getType(name);
      if (itemType == null) {
        throw new OpenDataException("Argument itemNames[] contains an element \"" + name
            + "\" that is not an item name of compositeType");
      }
      if (value != null && !itemType.isValue(value)) {
        throw new OpenDataException("Argument value of item \"" + name + "\" is not a valid value for the item's "
            + "open type: " + itemType);
      }
    }
    this.compositeType = compositeType;
    this.contents = items;
  }

  private static SortedMap<String, Object> makeMap(String[] itemNames, Object[] itemValues)
      throws OpenDataException {
    if (itemNames == null || itemValues == null) {
      throw new IllegalArgumentException("Argument itemNames[] or itemValues[] cannot be null.");
    }
    if (itemNames.length != itemValues.length) {
      throw new IllegalArgumentException("Array arguments itemNames[] and itemValues[] should be of same length "
          + "(got " + itemNames.length + " and " + itemValues.length + ").");
    }
    SortedMap<String, Object> map = new TreeMap<String, Object>();
    for (int i = 0; i < itemNames.length; i++) {
      String name = itemNames[i];
      if (name == null || name.trim().equals("")) {
        throw new IllegalArgumentException("Argument's element itemNames[" + i + "] cannot be null or empty");
      }
      if (map.containsKey(name.trim())) {
        throw new OpenDataException("Argument itemNames[" + i + "] duplicates a previous item name: " + name);
      }
      map.put(name.trim(), itemValues[i]);
    }
    return map;
  }

  private static SortedMap<String, Object> makeMap(Map<String, ?> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Argument items cannot be null or empty");
    }
    SortedMap<String, Object> map = new TreeMap<String, Object>();
    for (Map.Entry<String, ?> e : items.entrySet()) {
      map.put(e.getKey(), e.getValue());
    }
    return map;
  }

  @Override
  public CompositeType getCompositeType() {
    return compositeType;
  }

  @Override
  public Object get(String key) {
    if (key == null || key.trim().equals("")) {
      throw new IllegalArgumentException("Argument key cannot be a null or empty String.");
    }
    if (!contents.containsKey(key.trim())) {
      throw new InvalidKeyException("Argument key=\"" + key.trim() + "\" is not an existing item name for this "
          + "CompositeData instance.");
    }
    return contents.get(key.trim());
  }

  @Override
  public Object[] getAll(String[] keys) {
    if (keys == null || keys.length == 0) {
      return new Object[0];
    }
    Object[] results = new Object[keys.length];
    for (int i = 0; i < keys.length; i++) {
      results[i] = this.get(keys[i]);
    }
    return results;
  }

  @Override
  public boolean containsKey(String key) {
    if (key == null || key.trim().equals("")) {
      return false;
    }
    return contents.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return contents.containsValue(value);
  }

  @Override
  public Collection<?> values() {
    return Collections.unmodifiableCollection(contents.values());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CompositeData)) {
      return false;
    }
    CompositeData other = (CompositeData) obj;
    if (!this.getCompositeType().equals(other.getCompositeType())) {
      return false;
    }
    if (contents.size() != other.values().size()) {
      return false;
    }
    for (Map.Entry<String, Object> entry : contents.entrySet()) {
      Object e1 = entry.getValue();
      Object e2 = other.get(entry.getKey());
      if (e1 == e2) {
        continue;
      }
      if (e1 == null) {
        return false;
      }
      boolean eq = e1.getClass().isArray() ? Arrays.deepEquals(new Object[] {e1}, new Object[] {e2})
          : e1.equals(e2);
      if (!eq) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += compositeType.hashCode();
    for (Object o : contents.values()) {
      if (o instanceof Object[]) {
        hash += Arrays.deepHashCode((Object[]) o);
      } else if (o != null) {
        hash += o.hashCode();
      }
    }
    return hash;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(this.getClass().getName()).append("(compositeType=")
        .append(compositeType.toString()).append(",contents=").append(contentsString()).append(")").toString();
  }

  private String contentsString() {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (java.util.Map.Entry<String, Object> e : contents.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      Object v = e.getValue();
      String text = v != null && v.getClass().isArray() ? java.util.Arrays.deepToString(new Object[] {v}) : String.valueOf(v);
      if (v != null && v.getClass().isArray()) {
        text = text.substring(1, text.length() - 1);
      }
      sb.append(e.getKey()).append('=').append(text);
    }
    return sb.append('}').toString();
  }
}
