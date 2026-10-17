/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ImmutableDescriptor implements Descriptor {
  private static final long serialVersionUID = 8853308591080540165L;

  public static final ImmutableDescriptor EMPTY_DESCRIPTOR = new ImmutableDescriptor();

  private final String[] names;
  private final Object[] values;
  private transient int hashCode = -1;

  private ImmutableDescriptor() {
    this.names = new String[0];
    this.values = new Object[0];
  }

  public ImmutableDescriptor(String[] fieldNames, Object[] fieldValues) {
    this(makeMap(fieldNames, fieldValues));
  }

  public ImmutableDescriptor(String... fields) {
    this(makeMap(fields));
  }

  public ImmutableDescriptor(Map<String, ?> fields) {
    if (fields == null) {
      throw new IllegalArgumentException("Null Map");
    }
    TreeMap<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, ?> entry : fields.entrySet()) {
      String name = entry.getKey();
      if (name == null || name.equals("")) {
        throw new IllegalArgumentException("Empty or null field name");
      }
      if (map.containsKey(name)) {
        throw new IllegalArgumentException("Duplicate name: " + name);
      }
      map.put(name, entry.getValue());
    }
    int size = map.size();
    this.names = map.keySet().toArray(new String[size]);
    this.values = map.values().toArray(new Object[size]);
  }

  private static Map<String, Object> makeMap(String[] fieldNames, Object[] fieldValues) {
    if (fieldNames == null || fieldValues == null) {
      throw new IllegalArgumentException("Null array parameter");
    }
    if (fieldNames.length != fieldValues.length) {
      throw new IllegalArgumentException("Different size arrays");
    }
    Map<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    for (int i = 0; i < fieldNames.length; i++) {
      String name = fieldNames[i];
      if (name == null || name.equals("")) {
        throw new IllegalArgumentException("Empty or null field name");
      }
      if (map.containsKey(name)) {
        throw new IllegalArgumentException("Duplicate field name: " + name);
      }
      map.put(name, fieldValues[i]);
    }
    return map;
  }

  private static Map<String, Object> makeMap(String[] fields) {
    if (fields == null) {
      throw new IllegalArgumentException("Null fields parameter");
    }
    String[] fieldNames = new String[fields.length];
    String[] fieldValues = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      String field = fields[i];
      int eq = field.indexOf('=');
      if (eq < 0) {
        throw new IllegalArgumentException("Missing = character: " + field);
      }
      fieldNames[i] = field.substring(0, eq);
      fieldValues[i] = field.substring(eq + 1);
    }
    return makeMap(fieldNames, fieldValues);
  }

  public static ImmutableDescriptor union(Descriptor... descriptors) {
    int index = findNonEmpty(descriptors, 0);
    if (index < 0) {
      return EMPTY_DESCRIPTOR;
    }
    if (descriptors[index] instanceof ImmutableDescriptor && findNonEmpty(descriptors, index + 1) < 0) {
      return (ImmutableDescriptor) descriptors[index];
    }
    Map<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    ImmutableDescriptor biggest = EMPTY_DESCRIPTOR;
    for (Descriptor d : descriptors) {
      if (d == null) {
        continue;
      }
      String[] names;
      if (d instanceof ImmutableDescriptor) {
        ImmutableDescriptor id = (ImmutableDescriptor) d;
        names = id.names;
        if (id.getClass() == ImmutableDescriptor.class && names.length > biggest.names.length) {
          biggest = id;
        }
      } else {
        names = d.getFieldNames();
      }
      for (String n : names) {
        Object v = d.getFieldValue(n);
        Object old = map.put(n, v);
        if (old != null) {
          boolean equal;
          if (old.getClass().isArray()) {
            equal = Arrays.deepEquals(new Object[] {old}, new Object[] {v});
          } else {
            equal = old.equals(v);
          }
          if (!equal) {
            throw new IllegalArgumentException("Inconsistent values for descriptor field " + n + ": " + old
                + " :: " + v);
          }
        }
      }
    }
    if (biggest.names.length == map.size()) {
      return biggest;
    }
    return new ImmutableDescriptor(map);
  }

  private static int findNonEmpty(Descriptor[] ds, int start) {
    for (int i = start; i < ds.length; i++) {
      if (ds[i] != null && ds[i].getFieldNames().length > 0) {
        return i;
      }
    }
    return -1;
  }

  private static boolean isEmpty(Descriptor d) {
    return d == null || d.getFieldNames().length == 0;
  }

  static Descriptor nonNullDescriptor(Descriptor d) {
    return d == null ? EMPTY_DESCRIPTOR : d;
  }

  private int fieldIndex(String name) {
    return Arrays.binarySearch(names, name, String.CASE_INSENSITIVE_ORDER);
  }

  @Override
  public final Object getFieldValue(String fieldName) {
    checkIllegalFieldName(fieldName);
    int i = fieldIndex(fieldName);
    if (i < 0) {
      return null;
    }
    Object v = values[i];
    if (v == null || !v.getClass().isArray()) {
      return v;
    }
    if (v instanceof Object[]) {
      return ((Object[]) v).clone();
    }
    int len = Array.getLength(v);
    Object a = Array.newInstance(v.getClass().getComponentType(), len);
    System.arraycopy(v, 0, a, 0, len);
    return a;
  }

  @Override
  public final String[] getFields() {
    String[] result = new String[names.length];
    for (int i = 0; i < result.length; i++) {
      Object value = values[i];
      if (value == null) {
        value = "";
      } else if (!(value instanceof String)) {
        value = "(" + value + ")";
      }
      result[i] = names[i] + "=" + value;
    }
    return result;
  }

  @Override
  public final Object[] getFieldValues(String... fieldNames) {
    if (fieldNames == null) {
      return values.clone();
    }
    Object[] result = new Object[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      String name = fieldNames[i];
      if (name != null && !name.equals("")) {
        result[i] = getFieldValue(name);
      }
    }
    return result;
  }

  @Override
  public final String[] getFieldNames() {
    return names.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Descriptor)) {
      return false;
    }
    String[] onames;
    if (o instanceof ImmutableDescriptor) {
      onames = ((ImmutableDescriptor) o).names;
    } else {
      onames = ((Descriptor) o).getFieldNames();
      Arrays.sort(onames, String.CASE_INSENSITIVE_ORDER);
    }
    if (names.length != onames.length) {
      return false;
    }
    for (int i = 0; i < names.length; i++) {
      if (!names[i].equalsIgnoreCase(onames[i])) {
        return false;
      }
    }
    Object[] ovalues;
    if (o instanceof ImmutableDescriptor) {
      ovalues = ((ImmutableDescriptor) o).values;
    } else {
      ovalues = ((Descriptor) o).getFieldValues(onames);
    }
    return Arrays.deepEquals(values, ovalues);
  }

  @Override
  public int hashCode() {
    if (hashCode == -1) {
      int hash = 0;
      for (int i = 0; i < names.length; i++) {
        Object v = values[i];
        int h;
        if (v == null) {
          h = 0;
        } else if (v.getClass().isArray()) {
          h = Arrays.deepHashCode(new Object[] {v}) - 31;
        } else {
          h = v.hashCode();
        }
        hash += names[i].toLowerCase().hashCode() ^ h;
      }
      hashCode = hash;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < names.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(names[i]).append("=");
      Object v = values[i];
      if (v != null && v.getClass().isArray()) {
        String s = Arrays.deepToString(new Object[] {v});
        v = s.substring(1, s.length() - 1);
      }
      sb.append(String.valueOf(v));
    }
    return sb.append("}").toString();
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public Descriptor clone() {
    return this;
  }

  @Override
  public final void setFields(String[] fieldNames, Object[] fieldValues) throws RuntimeOperationsException {
    if (fieldNames == null || fieldValues == null) {
      illegal("Null argument");
    }
    if (fieldNames.length != fieldValues.length) {
      illegal("Different array sizes");
    }
    for (int i = 0; i < fieldNames.length; i++) {
      checkIllegalFieldName(fieldNames[i]);
    }
    for (int i = 0; i < fieldNames.length; i++) {
      setField(fieldNames[i], fieldValues[i]);
    }
  }

  @Override
  public final void setField(String fieldName, Object fieldValue) throws RuntimeOperationsException {
    checkIllegalFieldName(fieldName);
    int i = fieldIndex(fieldName);
    if (i >= 0) {
      Object v = values[i];
      if (v == null ? fieldValue == null : v.equals(fieldValue)) {
        return;
      }
    }
    throw new RuntimeOperationsException(new UnsupportedOperationException("Descriptor is read-only"));
  }

  @Override
  public final void removeField(String fieldName) {
    if (fieldName != null && fieldIndex(fieldName) >= 0) {
      throw new RuntimeOperationsException(new UnsupportedOperationException("Descriptor is read-only"));
    }
  }

  private static void illegal(String message) {
    throw new RuntimeOperationsException(new IllegalArgumentException(message));
  }

  private static void checkIllegalFieldName(String name) {
    if (name == null || name.equals("")) {
      illegal("Null or empty field name");
    }
  }
}
