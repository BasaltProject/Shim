/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TabularDataSupport implements TabularData, Map<Object, Object>, Cloneable, Serializable {
  static final long serialVersionUID = 5720150593236309827L;

  private Map<Object, CompositeData> dataMap;
  private final TabularType tabularType;
  private transient String[] indexNamesArray;

  public TabularDataSupport(TabularType tabularType) {
    this(tabularType, 16, 0.75f);
  }

  public TabularDataSupport(TabularType tabularType, int initialCapacity, float loadFactor) {
    if (tabularType == null) {
      throw new IllegalArgumentException("Argument tabularType cannot be null.");
    }
    this.tabularType = tabularType;
    List<String> tmpNames = tabularType.getIndexNames();
    this.indexNamesArray = tmpNames.toArray(new String[tmpNames.size()]);
    this.dataMap = new LinkedHashMap<Object, CompositeData>(initialCapacity, loadFactor);
  }

  @Override
  public TabularType getTabularType() {
    return tabularType;
  }

  @Override
  public Object[] calculateIndex(CompositeData value) {
    checkValueType(value);
    return internalCalculateIndex(value).toArray();
  }

  @Override
  public boolean containsKey(Object key) {
    Object[] k;
    try {
      k = (Object[]) key;
    } catch (ClassCastException e) {
      return false;
    }
    return containsKey(k);
  }

  @Override
  public boolean containsKey(Object[] key) {
    return key == null ? false : dataMap.containsKey(Arrays.asList(key));
  }

  @Override
  public boolean containsValue(CompositeData value) {
    return dataMap.containsValue(value);
  }

  @Override
  public boolean containsValue(Object value) {
    return dataMap.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return get((Object[]) key);
  }

  @Override
  public CompositeData get(Object[] key) {
    checkKeyType(key);
    return dataMap.get(Arrays.asList(key));
  }

  @Override
  public Object put(Object key, Object value) {
    internalPut((CompositeData) value);
    return value;
  }

  @Override
  public void put(CompositeData value) {
    internalPut(value);
  }

  private CompositeData internalPut(CompositeData value) throws InvalidOpenTypeException, KeyAlreadyExistsException {
    List<Object> index = checkValueAndIndex(value);
    dataMap.put(index, value);
    return value;
  }

  @Override
  public Object remove(Object key) {
    return remove((Object[]) key);
  }

  @Override
  public CompositeData remove(Object[] key) {
    checkKeyType(key);
    return dataMap.remove(Arrays.asList(key));
  }

  @Override
  public void putAll(Map<?, ?> t) {
    if (t == null || t.size() == 0) {
      return;
    }
    CompositeData[] values;
    try {
      values = t.values().toArray(new CompositeData[t.size()]);
    } catch (java.lang.ArrayStoreException e) {
      throw new ClassCastException("Map argument t contains values which are not instances of {@code CompositeData}");
    }
    putAll(values);
  }

  @Override
  public void putAll(CompositeData[] values) {
    if (values == null || values.length == 0) {
      return;
    }
    List<List<Object>> indexes = new ArrayList<List<Object>>(values.length);
    for (int i = 0; i < values.length; i++) {
      List<Object> index = checkValueAndIndex(values[i]);
      if (indexes.contains(index)) {
        throw new KeyAlreadyExistsException("Argument elements values[" + i + "] and values["
            + indexes.indexOf(index) + "] have the same indexes, calculated according to this TabularData "
            + "instance's tabularType.");
      }
      indexes.add(index);
    }
    for (int i = 0; i < values.length; i++) {
      dataMap.put(indexes.get(i), values[i]);
    }
  }

  @Override
  public void clear() {
    dataMap.clear();
  }

  @Override
  public int size() {
    return dataMap.size();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Set<Object> keySet() {
    return (Set) dataMap.keySet();
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Collection<Object> values() {
    return (Collection) dataMap.values();
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Set<Map.Entry<Object, Object>> entrySet() {
    return (Set) dataMap.entrySet();
  }

  @Override
  public Object clone() {
    try {
      TabularDataSupport c = (TabularDataSupport) super.clone();
      c.dataMap = new LinkedHashMap<Object, CompositeData>(c.dataMap);
      return c;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString(), e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    TabularData other;
    try {
      other = (TabularData) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (!this.getTabularType().equals(other.getTabularType())) {
      return false;
    }
    if (this.size() != other.size()) {
      return false;
    }
    for (CompositeData value : dataMap.values()) {
      if (!other.containsValue(value)) {
        return false;
      }
      if (!other.containsKey(internalCalculateIndex(value).toArray())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 0;
    result += this.tabularType.hashCode();
    for (Object value : values()) {
      result += value.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return getClass().getName() + "(tabularType=" + tabularType.toString() + ",contents=" + dataMap.toString() + ")";
  }

  private List<Object> internalCalculateIndex(CompositeData value) {
    return Collections.unmodifiableList(Arrays.asList(value.getAll(this.indexNamesArray)));
  }

  private void checkKeyType(Object[] key) {
    if (key.length != this.indexNamesArray.length) {
      throw new InvalidKeyException("Argument key's length=" + key.length
          + " is different from the number of item values, which is " + indexNamesArray.length
          + ", specified for the indexing rows in this TabularData instance.");
    }
    OpenType<?> keyElementType;
    for (int i = 0; i < key.length; i++) {
      keyElementType = tabularType.getRowType().getType(this.indexNamesArray[i]);
      if ((key[i] != null) && (!keyElementType.isValue(key[i]))) {
        throw new InvalidKeyException("Argument element key[" + i
            + "] is not a value for the open type expected for this element of the index, whose name is \""
            + indexNamesArray[i] + "\" and whose open type is " + keyElementType);
      }
    }
  }

  private void checkValueType(CompositeData value) {
    if (value == null) {
      throw new NullPointerException("Argument value cannot be null.");
    }
    if (!tabularType.getRowType().isValue(value)) {
      throw new InvalidOpenTypeException("Argument value's composite type [" + value.getCompositeType()
          + "] is not assignable to this TabularData instance's row type [" + tabularType.getRowType() + "].");
    }
  }

  private List<Object> checkValueAndIndex(CompositeData value) {
    checkValueType(value);
    List<Object> index = internalCalculateIndex(value);
    if (dataMap.containsKey(index)) {
      throw new KeyAlreadyExistsException("Argument value's index, calculated according to this TabularData "
          + "instance's tabularType, already refers to a value in this table.");
    }
    return index;
  }
}
