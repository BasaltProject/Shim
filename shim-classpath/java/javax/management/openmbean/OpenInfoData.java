/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;

final class OpenInfoData implements Serializable {
  private static final long serialVersionUID = 1L;

  final OpenType<?> openType;
  final Object defaultValue;
  final Set<?> legalValues;
  final Comparable<?> minValue;
  final Comparable<?> maxValue;

  OpenInfoData(OpenType<?> openType, Object defaultValue, Set<?> legalValues, Comparable<?> minValue,
      Comparable<?> maxValue) {
    this.openType = openType;
    this.defaultValue = defaultValue;
    this.legalValues = legalValues;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  static void checkNameAndDescription(String name, String description) {
    if (name == null || name.trim().equals("")) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
    if (description == null || description.trim().equals("")) {
      throw new IllegalArgumentException("Description cannot be null or empty");
    }
  }

  static OpenInfoData simple(OpenType<?> openType) {
    if (openType == null) {
      throw new IllegalArgumentException("Argument openType cannot be null");
    }
    return new OpenInfoData(openType, null, null, null, null);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T> OpenInfoData create(OpenType<T> openType, T defaultValue, T[] legalValues, Comparable<T> minValue,
      Comparable<T> maxValue) throws OpenDataException {
    if (openType == null) {
      throw new IllegalArgumentException("Argument openType cannot be null");
    }
    if (defaultValue != null && (openType instanceof ArrayType || openType instanceof TabularType)) {
      throw new OpenDataException("Default value not supported for ArrayType and TabularType");
    }
    if (defaultValue != null && !openType.isValue(defaultValue)) {
      throw new OpenDataException("Argument defaultValue's class [\"" + defaultValue.getClass().getName()
          + "\"] does not match the one defined in openType[\"" + openType.getClassName() + "\"]");
    }
    Set<T> legal = null;
    if (legalValues != null && legalValues.length > 0) {
      if (openType instanceof ArrayType || openType instanceof TabularType) {
        throw new OpenDataException("Legal values not supported for ArrayType and TabularType");
      }
      Set<T> set = new HashSet<T>(legalValues.length * 2);
      for (T v : legalValues) {
        if (v != null && !openType.isValue(v)) {
          throw new OpenDataException("Element legalValues[" + set.size() + "]=" + v
              + " is not a valid value for the specified openType [" + openType.toString() + "]");
        }
        set.add(v);
      }
      legal = Collections.unmodifiableSet(set);
    }
    if (minValue != null || maxValue != null) {
      if (openType instanceof ArrayType || openType instanceof TabularType) {
        throw new OpenDataException("Min/Max values not supported for ArrayType and TabularType");
      }
    }
    if (minValue != null && !openType.isValue(minValue)) {
      throw new OpenDataException("Type of minValue [" + minValue.getClass().getName()
          + "] does not match OpenType [" + openType.getClassName() + "]");
    }
    if (maxValue != null && !openType.isValue(maxValue)) {
      throw new OpenDataException("Type of maxValue [" + maxValue.getClass().getName()
          + "] does not match OpenType [" + openType.getClassName() + "]");
    }
    if (minValue != null && maxValue != null && minValue.compareTo((T) maxValue) > 0) {
      throw new OpenDataException("minValue cannot be greater than maxValue");
    }
    if (defaultValue != null) {
      if (legal != null && !legal.contains(defaultValue)) {
        throw new OpenDataException("defaultValue is not contained in legalValues");
      }
      if (minValue != null && minValue.compareTo(defaultValue) > 0) {
        throw new OpenDataException("minValue cannot be greater than defaultValue");
      }
      if (maxValue != null && ((Comparable) maxValue).compareTo(defaultValue) < 0) {
        throw new OpenDataException("maxValue cannot be less than defaultValue");
      }
    }
    return new OpenInfoData(openType, defaultValue, legal, minValue, maxValue);
  }

  Descriptor descriptor(Descriptor base) {
    Map<String, Object> fields = new HashMap<String, Object>();
    fields.put("openType", openType);
    if (defaultValue != null) {
      fields.put("defaultValue", defaultValue);
    }
    if (legalValues != null) {
      fields.put("legalValues", legalValues);
    }
    if (minValue != null) {
      fields.put("minValue", minValue);
    }
    if (maxValue != null) {
      fields.put("maxValue", maxValue);
    }
    if (base == null) {
      return new ImmutableDescriptor(fields);
    }
    return ImmutableDescriptor.union(base, new ImmutableDescriptor(fields));
  }

  boolean isValue(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!openType.isValue(obj)) {
      return false;
    }
    if (legalValues != null && !legalValues.contains(obj)) {
      return false;
    }
    return true;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  boolean isValueChecked(Object obj) {
    if (obj == null || !openType.isValue(obj)) {
      return false;
    }
    if (legalValues != null && !legalValues.contains(obj)) {
      return false;
    }
    if (minValue != null && ((Comparable) minValue).compareTo(obj) > 0) {
      return false;
    }
    if (maxValue != null && ((Comparable) maxValue).compareTo(obj) < 0) {
      return false;
    }
    return true;
  }

  boolean sameAs(OpenInfoData o) {
    return eq(openType, o.openType) && eq(defaultValue, o.defaultValue) && eq(legalValues, o.legalValues)
        && eq(minValue, o.minValue) && eq(maxValue, o.maxValue);
  }

  int hash() {
    int result = 0;
    result += openType.hashCode();
    if (defaultValue != null) {
      result += defaultValue.hashCode();
    }
    if (legalValues != null) {
      result += legalValues.hashCode();
    }
    if (minValue != null) {
      result += minValue.hashCode();
    }
    if (maxValue != null) {
      result += maxValue.hashCode();
    }
    return result;
  }

  String describe() {
    return ",openType=" + openType + ",default=" + defaultValue + ",minValue=" + minValue + ",maxValue=" + maxValue
        + ",legalValues=" + legalValues;
  }

  private static boolean eq(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }
}
