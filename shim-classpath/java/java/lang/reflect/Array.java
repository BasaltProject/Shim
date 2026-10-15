/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

public final class Array {
  private Array() { }

  public static Object get(Object array, int index) {
    String className = array.getClass().getName();
    if (! className.startsWith("[")) {
      throw new IllegalArgumentException();
    }

    switch (className.charAt(1)) {
    case 'B':
      return Byte.valueOf(((byte[]) array)[index]);
    case 'C':
      return Character.valueOf(((char[]) array)[index]);
    case 'D':
      return Double.valueOf(((double[]) array)[index]);
    case 'F':
      return Float.valueOf(((float[]) array)[index]);
    case 'I':
      return Integer.valueOf(((int[]) array)[index]);
    case 'J':
      return Long.valueOf(((long[]) array)[index]);
    case 'S':
      return Short.valueOf(((short[]) array)[index]);
    case 'Z':
      return Boolean.valueOf(((boolean[]) array)[index]);
    case 'L':
    case '[':
      return ((Object[]) array)[index];

    default:
      throw new Error();
    }
  }

  public static void set(Object array, int index, Object value) {
    String className = array.getClass().getName();
    if (! className.startsWith("[")) {
      throw new IllegalArgumentException();
    }

    switch (className.charAt(1)) {
    case 'B':
      ((byte[]) array)[index] = (Byte) value;
      break;
    case 'C':
      ((char[]) array)[index] = (Character) value;
      break;
    case 'D':
      ((double[]) array)[index] = (Double) value;
      break;
    case 'F':
      ((float[]) array)[index] = (Float) value;
      break;
    case 'I':
      ((int[]) array)[index] = (Integer) value;
      break;
    case 'J':
      ((long[]) array)[index] = (Long) value;
      break;
    case 'S':
      ((short[]) array)[index] = (Short) value;
      break;
    case 'Z':
      ((boolean[]) array)[index] = (Boolean) value;
      break;
    case 'L':
    case '[':
      if (value == null
          || array.getClass().getComponentType().isInstance(value))
      {
        ((Object[]) array)[index] = value;
      } else {
        throw new IllegalArgumentException
          ("need " + array.getClass().getComponentType() +
           ", got " + value.getClass().getName());
      }
      break;

    default:
      throw new Error();
    }    
  }

  public static native int getLength(Object array);

  private static native Object makeObjectArray(Class elementType, int length);

  public static Object newInstance(Class elementType, int length) {
    if (length < 0) {
      throw new NegativeArraySizeException();
    }

    if (elementType.isPrimitive()) {
      if (elementType.equals(boolean.class)) {
        return new boolean[length];
      } else if (elementType.equals(byte.class)) {
        return new byte[length];
      } else if (elementType.equals(char.class)) {
        return new char[length];
      } else if (elementType.equals(short.class)) {
        return new short[length];
      } else if (elementType.equals(int.class)) {
        return new int[length];
      } else if (elementType.equals(long.class)) {
        return new long[length];
      } else if (elementType.equals(float.class)) {
        return new float[length];
      } else if (elementType.equals(double.class)) {
        return new double[length];
      } else {
        throw new IllegalArgumentException();
      }
    } else {
      return makeObjectArray(elementType, length);
    }
  }

  public static boolean getBoolean(Object array, int index) {
    if (array instanceof boolean[]) {
      return ((boolean[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of type boolean");
  }

  public static byte getByte(Object array, int index) {
    if (array instanceof byte[]) {
      return ((byte[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of type byte");
  }

  public static char getChar(Object array, int index) {
    if (array instanceof char[]) {
      return ((char[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of type char");
  }

  public static short getShort(Object array, int index) {
    if (array instanceof short[]) {
      return ((short[]) array)[index];
    }
    if (array instanceof byte[]) {
      return ((byte[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of type short or byte");
  }

  public static int getInt(Object array, int index) {
    if (array instanceof int[]) {
      return ((int[]) array)[index];
    }
    if (array instanceof byte[]) {
      return ((byte[]) array)[index];
    }
    if (array instanceof short[]) {
      return ((short[]) array)[index];
    }
    if (array instanceof char[]) {
      return ((char[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of compatible integral type");
  }

  public static long getLong(Object array, int index) {
    if (array instanceof long[]) {
      return ((long[]) array)[index];
    }
    if (array instanceof int[]) {
      return ((int[]) array)[index];
    }
    if (array instanceof byte[]) {
      return ((byte[]) array)[index];
    }
    if (array instanceof short[]) {
      return ((short[]) array)[index];
    }
    if (array instanceof char[]) {
      return ((char[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of compatible integral type");
  }

  public static float getFloat(Object array, int index) {
    if (array instanceof float[]) {
      return ((float[]) array)[index];
    }
    if (array instanceof int[]) {
      return ((int[]) array)[index];
    }
    if (array instanceof long[]) {
      return ((long[]) array)[index];
    }
    if (array instanceof byte[]) {
      return ((byte[]) array)[index];
    }
    if (array instanceof short[]) {
      return ((short[]) array)[index];
    }
    if (array instanceof char[]) {
      return ((char[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of compatible type");
  }

  public static double getDouble(Object array, int index) {
    if (array instanceof double[]) {
      return ((double[]) array)[index];
    }
    if (array instanceof float[]) {
      return ((float[]) array)[index];
    }
    if (array instanceof long[]) {
      return ((long[]) array)[index];
    }
    if (array instanceof int[]) {
      return ((int[]) array)[index];
    }
    if (array instanceof byte[]) {
      return ((byte[]) array)[index];
    }
    if (array instanceof short[]) {
      return ((short[]) array)[index];
    }
    if (array instanceof char[]) {
      return ((char[]) array)[index];
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of compatible type");
  }

  public static void setBoolean(Object array, int index, boolean z) {
    if (array instanceof boolean[]) {
      ((boolean[]) array)[index] = z;
      return;
    }
    if (array == null) throw new NullPointerException();
    throw new IllegalArgumentException("argument is not an array of type boolean");
  }

  public static void setByte(Object array, int index, byte b) {
    if (array instanceof byte[]) {
      ((byte[]) array)[index] = b;
    } else if (array instanceof short[]) {
      ((short[]) array)[index] = b;
    } else if (array instanceof int[]) {
      ((int[]) array)[index] = b;
    } else if (array instanceof long[]) {
      ((long[]) array)[index] = b;
    } else if (array instanceof float[]) {
      ((float[]) array)[index] = b;
    } else if (array instanceof double[]) {
      ((double[]) array)[index] = b;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of compatible type");
    }
  }

  public static void setChar(Object array, int index, char c) {
    if (array instanceof char[]) {
      ((char[]) array)[index] = c;
    } else if (array instanceof int[]) {
      ((int[]) array)[index] = c;
    } else if (array instanceof long[]) {
      ((long[]) array)[index] = c;
    } else if (array instanceof float[]) {
      ((float[]) array)[index] = c;
    } else if (array instanceof double[]) {
      ((double[]) array)[index] = c;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of compatible type");
    }
  }

  public static void setShort(Object array, int index, short s) {
    if (array instanceof short[]) {
      ((short[]) array)[index] = s;
    } else if (array instanceof int[]) {
      ((int[]) array)[index] = s;
    } else if (array instanceof long[]) {
      ((long[]) array)[index] = s;
    } else if (array instanceof float[]) {
      ((float[]) array)[index] = s;
    } else if (array instanceof double[]) {
      ((double[]) array)[index] = s;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of compatible type");
    }
  }

  public static void setInt(Object array, int index, int i) {
    if (array instanceof int[]) {
      ((int[]) array)[index] = i;
    } else if (array instanceof long[]) {
      ((long[]) array)[index] = i;
    } else if (array instanceof float[]) {
      ((float[]) array)[index] = i;
    } else if (array instanceof double[]) {
      ((double[]) array)[index] = i;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of compatible type");
    }
  }

  public static void setLong(Object array, int index, long l) {
    if (array instanceof long[]) {
      ((long[]) array)[index] = l;
    } else if (array instanceof float[]) {
      ((float[]) array)[index] = l;
    } else if (array instanceof double[]) {
      ((double[]) array)[index] = l;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of compatible type");
    }
  }

  public static void setFloat(Object array, int index, float f) {
    if (array instanceof float[]) {
      ((float[]) array)[index] = f;
    } else if (array instanceof double[]) {
      ((double[]) array)[index] = f;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of compatible type");
    }
  }

  public static void setDouble(Object array, int index, double d) {
    if (array instanceof double[]) {
      ((double[]) array)[index] = d;
    } else if (array == null) {
      throw new NullPointerException();
    } else {
      throw new IllegalArgumentException("argument is not an array of type double");
    }
  }

  public static Object newInstance(Class componentType, int... dimensions)
      throws IllegalArgumentException, NegativeArraySizeException {
    if (componentType == null) {
      throw new NullPointerException();
    }
    if (dimensions == null || dimensions.length == 0 || dimensions.length > 255) {
      throw new IllegalArgumentException();
    }
    if (componentType == void.class) {
      throw new IllegalArgumentException("Cannot create void array");
    }
    for (int d : dimensions) {
      if (d < 0) {
        throw new NegativeArraySizeException();
      }
    }
    if (dimensions.length == 1) {
      return newInstance(componentType, dimensions[0]);
    }
    return multiNewInstance(componentType, dimensions, 0);
  }

  private static Object multiNewInstance(Class componentType, int[] dimensions, int index) {
    int length = dimensions[index];
    if (index == dimensions.length - 1) {
      return newInstance(componentType, length);
    }
    Class subArrayType = componentType;
    for (int i = 0; i < dimensions.length - 1 - index; i++) {
      subArrayType = subArrayType.arrayType();
    }
    Object[] result = (Object[]) makeObjectArray(subArrayType, length);
    for (int i = 0; i < length; i++) {
      result[i] = multiNewInstance(componentType, dimensions, index + 1);
    }
    return result;
  }
}
