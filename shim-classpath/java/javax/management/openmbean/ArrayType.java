/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.lang.reflect.Array;

public class ArrayType<T> extends OpenType<T> {
  static final long serialVersionUID = 720504429830309770L;

  private final int dimension;
  private final OpenType<?> elementType;
  private final boolean primitiveArray;
  private transient Integer myHashCode;
  private transient String myToString;

  private static final String[][] kPrimitives = {
    {"java.lang.Boolean", "Z", "boolean"}, {"java.lang.Character", "C", "char"}, {"java.lang.Byte", "B", "byte"},
    {"java.lang.Short", "S", "short"}, {"java.lang.Integer", "I", "int"}, {"java.lang.Long", "J", "long"},
    {"java.lang.Float", "F", "float"}, {"java.lang.Double", "D", "double"},
  };

  static boolean isPrimitiveContentType(String primitiveKey) {
    for (String[] p : kPrimitives) {
      if (p[1].equals(primitiveKey)) {
        return true;
      }
    }
    return false;
  }

  static String getPrimitiveTypeKey(String elementClassName) {
    for (String[] p : kPrimitives) {
      if (p[0].equals(elementClassName)) {
        return p[1];
      }
    }
    return null;
  }

  static String getPrimitiveTypeName(String primitiveKey) {
    for (String[] p : kPrimitives) {
      if (p[1].equals(primitiveKey)) {
        return p[2];
      }
    }
    return null;
  }

  static SimpleType<?> getPrimitiveOpenType(String primitiveKey) {
    switch (primitiveKey) {
    case "Z": return SimpleType.BOOLEAN;
    case "C": return SimpleType.CHARACTER;
    case "B": return SimpleType.BYTE;
    case "S": return SimpleType.SHORT;
    case "I": return SimpleType.INTEGER;
    case "J": return SimpleType.LONG;
    case "F": return SimpleType.FLOAT;
    case "D": return SimpleType.DOUBLE;
    default: return null;
    }
  }

  public ArrayType(int dimension, OpenType<?> elementType) throws OpenDataException {
    super(buildClassName(dimension, elementType), buildClassName(dimension, elementType),
        buildDescription(dimension, elementType));
    if (elementType.isArray()) {
      ArrayType<?> inner = (ArrayType<?>) elementType;
      this.dimension = inner.getDimension() + dimension;
      this.elementType = inner.getElementOpenType();
      this.primitiveArray = inner.isPrimitiveArray();
    } else {
      this.dimension = dimension;
      this.elementType = elementType;
      this.primitiveArray = false;
    }
  }

  public ArrayType(SimpleType<?> elementType, boolean primitiveArray) throws OpenDataException {
    super(buildClassName(elementType, primitiveArray), buildClassName(elementType, primitiveArray),
        buildDescription(elementType, primitiveArray));
    this.dimension = 1;
    this.elementType = elementType;
    this.primitiveArray = primitiveArray;
  }

  ArrayType(String className, String typeName, String description, int dimension, OpenType<?> elementType,
      boolean primitiveArray) throws OpenDataException {
    super(className, typeName, description);
    this.dimension = dimension;
    this.elementType = elementType;
    this.primitiveArray = primitiveArray;
  }

  private static String repeat(char c, int n) {
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      sb.append(c);
    }
    return sb.toString();
  }

  private static String buildClassName(int dimension, OpenType<?> elementType) {
    if (dimension < 1) {
      throw new IllegalArgumentException("Value of argument dimension must be greater than 0");
    }
    String name = elementType.getClassName();
    if (elementType.isArray()) {
      return repeat('[', dimension) + name;
    }
    return repeat('[', dimension) + "L" + name + ";";
  }

  private static String buildDescription(int dimension, OpenType<?> elementType) {
    if (dimension < 1) {
      throw new IllegalArgumentException("Value of argument dimension must be greater than 0");
    }
    if (elementType.isArray()) {
      ArrayType<?> inner = (ArrayType<?>) elementType;
      int total = inner.getDimension() + dimension;
      String base = inner.isPrimitiveArray() ? getPrimitiveTypeName(getPrimitiveTypeKey(
          inner.getElementOpenType().getClassName())) : inner.getElementOpenType().getClassName();
      return total + "-dimension array of " + base;
    }
    return dimension + "-dimension array of " + elementType.getClassName();
  }

  private static String buildClassName(SimpleType<?> elementType, boolean primitiveArray) throws OpenDataException {
    if (primitiveArray) {
      String key = getPrimitiveTypeKey(elementType.getClassName());
      if (key == null) {
        throw new OpenDataException("Element type is not primitive: " + elementType.getClassName());
      }
      return "[" + key;
    }
    return "[L" + elementType.getClassName() + ";";
  }

  private static String buildDescription(SimpleType<?> elementType, boolean primitiveArray) {
    String name = elementType.getClassName();
    if (primitiveArray) {
      String key = getPrimitiveTypeKey(name);
      if (key != null) {
        name = getPrimitiveTypeName(key);
      }
    }
    return "1-dimension array of " + name;
  }

  public int getDimension() {
    return dimension;
  }

  public OpenType<?> getElementOpenType() {
    return elementType;
  }

  public boolean isPrimitiveArray() {
    return primitiveArray;
  }

  @Override
  public boolean isValue(Object obj) {
    if (obj == null) {
      return false;
    }
    Class<?> c = obj.getClass();
    if (!c.isArray()) {
      return false;
    }
    if (getClassName().equals(c.getName())) {
      return true;
    }
    if (primitiveArray) {
      return false;
    }
    int dims = 0;
    Class<?> leaf = c;
    while (leaf.isArray()) {
      dims++;
      leaf = leaf.getComponentType();
    }
    if (dims != dimension) {
      return false;
    }
    if (elementType instanceof CompositeType) {
      return CompositeData.class.isAssignableFrom(leaf) && leavesAreValues(obj, dims);
    }
    if (elementType instanceof TabularType) {
      return TabularData.class.isAssignableFrom(leaf) && leavesAreValues(obj, dims);
    }
    return false;
  }

  private boolean leavesAreValues(Object array, int dims) {
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      Object element = Array.get(array, i);
      if (dims > 1) {
        if (element != null && !leavesAreValues(element, dims - 1)) {
          return false;
        }
      } else if (element != null && !elementType.isValue(element)) {
        return false;
      }
    }
    return true;
  }

  boolean isAssignableFrom(OpenType<?> ot) {
    if (!(ot instanceof ArrayType)) {
      return false;
    }
    ArrayType<?> at = (ArrayType<?>) ot;
    return at.getDimension() == dimension && at.isPrimitiveArray() == primitiveArray
        && elementType.equals(at.getElementOpenType());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ArrayType)) {
      return false;
    }
    ArrayType<?> other = (ArrayType<?>) obj;
    return dimension == other.dimension && primitiveArray == other.primitiveArray
        && elementType.equals(other.elementType);
  }

  @Override
  public int hashCode() {
    if (myHashCode == null) {
      int value = 0;
      value += dimension;
      value += elementType.hashCode();
      value += Boolean.valueOf(primitiveArray).hashCode();
      myHashCode = Integer.valueOf(value);
    }
    return myHashCode.intValue();
  }

  @Override
  public String toString() {
    if (myToString == null) {
      myToString = getClass().getName() + "(name=" + getTypeName() + ",dimension=" + dimension + ",elementType="
          + elementType + ",primitiveArray=" + primitiveArray + ")";
    }
    return myToString;
  }

  @SuppressWarnings("unchecked")
  public static <E> ArrayType<E[]> getArrayType(OpenType<E> elementType) throws OpenDataException {
    return new ArrayType<E[]>(1, elementType);
  }

  @SuppressWarnings("unchecked")
  public static <T> ArrayType<T> getPrimitiveArrayType(Class<T> arrayClass) {
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("arrayClass must be an array");
    }
    int dims = 0;
    Class<?> leaf = arrayClass;
    while (leaf.isArray()) {
      dims++;
      leaf = leaf.getComponentType();
    }
    if (!leaf.isPrimitive()) {
      throw new IllegalArgumentException("component type of the array must be a primitive type");
    }
    String key = arrayClass.getName().substring(dims);
    try {
      SimpleType<?> element = getPrimitiveOpenType(key);
      String className = repeat('[', dims) + key;
      return new ArrayType<T>(className, className, dims + "-dimension array of " + getPrimitiveTypeName(key), dims,
          element, true);
    } catch (OpenDataException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
}
