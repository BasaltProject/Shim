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

package java.util;

import java.lang.reflect.Array;

import shim.ArraySorts;

public class Arrays {
  private Arrays() { }

  private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    }
    if (fromIndex < 0) {
      throw new ArrayIndexOutOfBoundsException(fromIndex);
    }
    if (toIndex > arrayLength) {
      throw new ArrayIndexOutOfBoundsException(toIndex);
    }
  }

  private static int doubleBitsHash(double value) {
    long bits = Double.doubleToLongBits(value);
    return (int) (bits ^ (bits >>> 32));
  }

  public static String toString(Object[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(String.valueOf(a[i]));
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(boolean[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(byte[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(char[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(short[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(int[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(long[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(float[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static String toString(double[] a) {
    if (a == null) {
      return "null";
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      return "[]";
    }
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(a[i]);
      if (i == iMax) {
        return b.append(']').toString();
      }
      b.append(", ");
    }
  }

  public static int hashCode(Object[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (Object element : a) {
      result = 31 * result + (element == null ? 0 : element.hashCode());
    }
    return result;
  }

  public static int hashCode(boolean[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (boolean element : a) {
      result = 31 * result + (element ? 1231 : 1237);
    }
    return result;
  }

  public static int hashCode(byte[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (byte element : a) {
      result = 31 * result + (int) element;
    }
    return result;
  }

  public static int hashCode(char[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (char element : a) {
      result = 31 * result + (int) element;
    }
    return result;
  }

  public static int hashCode(short[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (short element : a) {
      result = 31 * result + (int) element;
    }
    return result;
  }

  public static int hashCode(int[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (int element : a) {
      result = 31 * result + element;
    }
    return result;
  }

  public static int hashCode(long[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (long element : a) {
      result = 31 * result + (int) (element ^ (element >>> 32));
    }
    return result;
  }

  public static int hashCode(float[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (float element : a) {
      result = 31 * result + Float.floatToIntBits(element);
    }
    return result;
  }

  public static int hashCode(double[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (double element : a) {
      result = 31 * result + doubleBitsHash(element);
    }
    return result;
  }

  public static int deepHashCode(Object[] a) {
    if (a == null) {
      return 0;
    }
    int result = 1;
    for (Object element : a) {
      int elementHash;
      if (element == null) {
        elementHash = 0;
      } else if (element instanceof Object[]) {
        elementHash = deepHashCode((Object[]) element);
      } else if (element instanceof byte[]) {
        elementHash = hashCode((byte[]) element);
      } else if (element instanceof short[]) {
        elementHash = hashCode((short[]) element);
      } else if (element instanceof int[]) {
        elementHash = hashCode((int[]) element);
      } else if (element instanceof long[]) {
        elementHash = hashCode((long[]) element);
      } else if (element instanceof char[]) {
        elementHash = hashCode((char[]) element);
      } else if (element instanceof float[]) {
        elementHash = hashCode((float[]) element);
      } else if (element instanceof double[]) {
        elementHash = hashCode((double[]) element);
      } else if (element instanceof boolean[]) {
        elementHash = hashCode((boolean[]) element);
      } else {
        elementHash = element.hashCode();
      }
      result = 31 * result + elementHash;
    }
    return result;
  }

  public static boolean deepEquals(Object[] a1, Object[] a2) {
    if (a1 == a2) {
      return true;
    }
    if (a1 == null || a2 == null) {
      return false;
    }
    int length = a1.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      Object e1 = a1[i];
      Object e2 = a2[i];
      if (e1 == e2) {
        continue;
      }
      if (e1 == null) {
        return false;
      }
      if (!deepEquals0(e1, e2)) {
        return false;
      }
    }
    return true;
  }

  private static boolean deepEquals0(Object e1, Object e2) {
    if (e1 instanceof Object[] && e2 instanceof Object[]) {
      return deepEquals((Object[]) e1, (Object[]) e2);
    } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
      return equals((byte[]) e1, (byte[]) e2);
    } else if (e1 instanceof short[] && e2 instanceof short[]) {
      return equals((short[]) e1, (short[]) e2);
    } else if (e1 instanceof int[] && e2 instanceof int[]) {
      return equals((int[]) e1, (int[]) e2);
    } else if (e1 instanceof long[] && e2 instanceof long[]) {
      return equals((long[]) e1, (long[]) e2);
    } else if (e1 instanceof char[] && e2 instanceof char[]) {
      return equals((char[]) e1, (char[]) e2);
    } else if (e1 instanceof float[] && e2 instanceof float[]) {
      return equals((float[]) e1, (float[]) e2);
    } else if (e1 instanceof double[] && e2 instanceof double[]) {
      return equals((double[]) e1, (double[]) e2);
    } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
      return equals((boolean[]) e1, (boolean[]) e2);
    }
    return e1.equals(e2);
  }

  public static String deepToString(Object[] a) {
    if (a == null) {
      return "null";
    }
    StringBuilder buf = new StringBuilder();
    deepToString(a, buf, new HashSet<Object[]>());
    return buf.toString();
  }

  private static void deepToString(Object[] a, StringBuilder buf, Set<Object[]> dejaVu) {
    if (a == null) {
      buf.append("null");
      return;
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      buf.append("[]");
      return;
    }
    dejaVu.add(a);
    buf.append('[');
    for (int i = 0; ; i++) {
      Object element = a[i];
      if (element == null) {
        buf.append("null");
      } else if (element instanceof Object[]) {
        if (dejaVu.contains(element)) {
          buf.append("[...]");
        } else {
          deepToString((Object[]) element, buf, dejaVu);
        }
      } else if (element instanceof byte[]) {
        buf.append(toString((byte[]) element));
      } else if (element instanceof short[]) {
        buf.append(toString((short[]) element));
      } else if (element instanceof int[]) {
        buf.append(toString((int[]) element));
      } else if (element instanceof long[]) {
        buf.append(toString((long[]) element));
      } else if (element instanceof char[]) {
        buf.append(toString((char[]) element));
      } else if (element instanceof float[]) {
        buf.append(toString((float[]) element));
      } else if (element instanceof double[]) {
        buf.append(toString((double[]) element));
      } else if (element instanceof boolean[]) {
        buf.append(toString((boolean[]) element));
      } else {
        buf.append(element.toString());
      }
      if (i == iMax) {
        break;
      }
      buf.append(", ");
    }
    buf.append(']');
    dejaVu.remove(a);
  }

  public static boolean equals(boolean[] a, boolean[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(boolean[] a, int aFromIndex, int aToIndex, boolean[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(byte[] a, byte[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(char[] a, char[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(char[] a, int aFromIndex, int aToIndex, char[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(short[] a, short[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(short[] a, int aFromIndex, int aToIndex, short[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(int[] a, int[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(int[] a, int aFromIndex, int aToIndex, int[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(long[] a, long[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(long[] a, int aFromIndex, int aToIndex, long[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(float[] a, float[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (Float.floatToIntBits(a[i]) != Float.floatToIntBits(a2[i])) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(float[] a, int aFromIndex, int aToIndex, float[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (Float.floatToIntBits(a[aFromIndex + i]) != Float.floatToIntBits(b[bFromIndex + i])) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(double[] a, double[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (Double.doubleToLongBits(a[i]) != Double.doubleToLongBits(a2[i])) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(double[] a, int aFromIndex, int aToIndex, double[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (Double.doubleToLongBits(a[aFromIndex + i]) != Double.doubleToLongBits(b[bFromIndex + i])) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(Object[] a, Object[] a2) {
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (!Objects.equals(a[i], a2[i])) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(Object[] a, int aFromIndex, int aToIndex, Object[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (!Objects.equals(a[aFromIndex + i], b[bFromIndex + i])) {
        return false;
      }
    }
    return true;
  }

  public static <T> boolean equals(T[] a, T[] a2, Comparator<? super T> cmp) {
    Objects.requireNonNull(cmp);
    if (a == a2) {
      return true;
    }
    if (a == null || a2 == null) {
      return false;
    }
    int length = a.length;
    if (a2.length != length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (cmp.compare(a[i], a2[i]) != 0) {
        return false;
      }
    }
    return true;
  }

  public static <T> boolean equals(T[] a, int aFromIndex, int aToIndex, T[] b, int bFromIndex, int bToIndex,
                                   Comparator<? super T> cmp) {
    Objects.requireNonNull(cmp);
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    if (aLength != bToIndex - bFromIndex) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (cmp.compare(a[aFromIndex + i], b[bFromIndex + i]) != 0) {
        return false;
      }
    }
    return true;
  }

  public static int mismatch(boolean[] a, boolean[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(boolean[] a, int aFromIndex, int aToIndex, boolean[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(boolean[] a, boolean[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Boolean.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(boolean[] a, int aFromIndex, int aToIndex, boolean[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Boolean.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(byte[] a, byte[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(byte[] a, byte[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Byte.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Byte.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(char[] a, char[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(char[] a, int aFromIndex, int aToIndex, char[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(char[] a, char[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Character.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(char[] a, int aFromIndex, int aToIndex, char[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Character.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(short[] a, short[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(short[] a, int aFromIndex, int aToIndex, short[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(short[] a, short[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Short.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(short[] a, int aFromIndex, int aToIndex, short[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Short.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(int[] a, int[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(int[] a, int aFromIndex, int aToIndex, int[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(int[] a, int[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Integer.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(int[] a, int aFromIndex, int aToIndex, int[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Integer.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(long[] a, long[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(long[] a, int aFromIndex, int aToIndex, long[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(long[] a, long[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Long.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(long[] a, int aFromIndex, int aToIndex, long[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Long.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(float[] a, float[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (Float.floatToIntBits(a[i]) != Float.floatToIntBits(b[i])) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(float[] a, int aFromIndex, int aToIndex, float[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (Float.floatToIntBits(a[aFromIndex + i]) != Float.floatToIntBits(b[bFromIndex + i])) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(float[] a, float[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (Float.floatToIntBits(a[i]) != Float.floatToIntBits(b[i])) {
        return Float.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(float[] a, int aFromIndex, int aToIndex, float[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (Float.floatToIntBits(a[aFromIndex + i]) != Float.floatToIntBits(b[bFromIndex + i])) {
        return Float.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(double[] a, double[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (Double.doubleToLongBits(a[i]) != Double.doubleToLongBits(b[i])) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(double[] a, int aFromIndex, int aToIndex, double[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (Double.doubleToLongBits(a[aFromIndex + i]) != Double.doubleToLongBits(b[bFromIndex + i])) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static int compare(double[] a, double[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (Double.doubleToLongBits(a[i]) != Double.doubleToLongBits(b[i])) {
        return Double.compare(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compare(double[] a, int aFromIndex, int aToIndex, double[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (Double.doubleToLongBits(a[aFromIndex + i]) != Double.doubleToLongBits(b[bFromIndex + i])) {
        return Double.compare(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int compareUnsigned(byte[] a, byte[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Byte.compareUnsigned(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compareUnsigned(byte[] a, int aFromIndex, int aToIndex, byte[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Byte.compareUnsigned(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int compareUnsigned(short[] a, short[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Short.compareUnsigned(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compareUnsigned(short[] a, int aFromIndex, int aToIndex, short[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Short.compareUnsigned(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int compareUnsigned(int[] a, int[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Integer.compareUnsigned(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compareUnsigned(int[] a, int aFromIndex, int aToIndex, int[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Integer.compareUnsigned(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int compareUnsigned(long[] a, long[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] != b[i]) {
        return Long.compareUnsigned(a[i], b[i]);
      }
    }
    return a.length - b.length;
  }

  public static int compareUnsigned(long[] a, int aFromIndex, int aToIndex, long[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (a[aFromIndex + i] != b[bFromIndex + i]) {
        return Long.compareUnsigned(a[aFromIndex + i], b[bFromIndex + i]);
      }
    }
    return aLength - bLength;
  }

  public static int mismatch(Object[] a, Object[] b) {
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      if (!Objects.equals(a[i], b[i])) {
        return i;
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static int mismatch(Object[] a, int aFromIndex, int aToIndex, Object[] b, int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      if (!Objects.equals(a[aFromIndex + i], b[bFromIndex + i])) {
        return i;
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static <T> int mismatch(T[] a, T[] b, Comparator<? super T> cmp) {
    Objects.requireNonNull(cmp);
    int length = Math.min(a.length, b.length);
    if (a == b) {
      return -1;
    }
    for (int i = 0; i < length; i++) {
      T oa = a[i];
      T ob = b[i];
      if (oa != ob) {
        if (cmp.compare(oa, ob) != 0) {
          return i;
        }
      }
    }
    return a.length != b.length ? length : -1;
  }

  public static <T> int mismatch(T[] a, int aFromIndex, int aToIndex, T[] b, int bFromIndex, int bToIndex,
                                 Comparator<? super T> cmp) {
    Objects.requireNonNull(cmp);
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      T oa = a[aFromIndex + i];
      T ob = b[bFromIndex + i];
      if (oa != ob) {
        if (cmp.compare(oa, ob) != 0) {
          return i;
        }
      }
    }
    return aLength != bLength ? length : -1;
  }

  public static <T extends Comparable<? super T>> int compare(T[] a, T[] b) {
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      T oa = a[i];
      T ob = b[i];
      if (oa != ob) {
        if (oa == null || ob == null) {
          return oa == null ? -1 : 1;
        }
        int v = oa.compareTo(ob);
        if (v != 0) {
          return v;
        }
      }
    }
    return a.length - b.length;
  }

  public static <T extends Comparable<? super T>> int compare(T[] a, int aFromIndex, int aToIndex, T[] b,
                                                              int bFromIndex, int bToIndex) {
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      T oa = a[aFromIndex + i];
      T ob = b[bFromIndex + i];
      if (oa != ob) {
        if (oa == null || ob == null) {
          return oa == null ? -1 : 1;
        }
        int v = oa.compareTo(ob);
        if (v != 0) {
          return v;
        }
      }
    }
    return aLength - bLength;
  }

  public static <T> int compare(T[] a, T[] b, Comparator<? super T> cmp) {
    Objects.requireNonNull(cmp);
    if (a == b) {
      return 0;
    }
    if (a == null || b == null) {
      return a == null ? -1 : 1;
    }
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      T oa = a[i];
      T ob = b[i];
      if (oa != ob) {
        int v = cmp.compare(oa, ob);
        if (v != 0) {
          return v;
        }
      }
    }
    return a.length - b.length;
  }

  public static <T> int compare(T[] a, int aFromIndex, int aToIndex, T[] b, int bFromIndex, int bToIndex,
                                Comparator<? super T> cmp) {
    Objects.requireNonNull(cmp);
    rangeCheck(a.length, aFromIndex, aToIndex);
    rangeCheck(b.length, bFromIndex, bToIndex);
    int aLength = aToIndex - aFromIndex;
    int bLength = bToIndex - bFromIndex;
    int length = Math.min(aLength, bLength);
    for (int i = 0; i < length; i++) {
      T oa = a[aFromIndex + i];
      T ob = b[bFromIndex + i];
      if (oa != ob) {
        int v = cmp.compare(oa, ob);
        if (v != 0) {
          return v;
        }
      }
    }
    return aLength - bLength;
  }

  public static int binarySearch(byte[] a, byte key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(byte[] a, int fromIndex, int toIndex, byte key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      byte midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(char[] a, char key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(char[] a, int fromIndex, int toIndex, char key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      char midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(short[] a, short key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(short[] a, int fromIndex, int toIndex, short key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      short midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(int[] a, int key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(int[] a, int fromIndex, int toIndex, int key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      int midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(long[] a, long key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(long[] a, int fromIndex, int toIndex, long key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      long midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(float[] a, float key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(float[] a, int fromIndex, int toIndex, float key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      float midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        int midBits = Float.floatToIntBits(midVal);
        int keyBits = Float.floatToIntBits(key);
        if (midBits == keyBits) {
          return mid;
        } else if (midBits < keyBits) {
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(double[] a, double key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(double[] a, int fromIndex, int toIndex, double key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      double midVal = a[mid];
      if (midVal < key) {
        low = mid + 1;
      } else if (midVal > key) {
        high = mid - 1;
      } else {
        long midBits = Double.doubleToLongBits(midVal);
        long keyBits = Double.doubleToLongBits(key);
        if (midBits == keyBits) {
          return mid;
        } else if (midBits < keyBits) {
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }
    }
    return -(low + 1);
  }

  public static int binarySearch(Object[] a, Object key) {
    return binarySearch(a, 0, a.length, key);
  }

  public static int binarySearch(Object[] a, int fromIndex, int toIndex, Object key) {
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      @SuppressWarnings("rawtypes")
      Comparable midVal = (Comparable) a[mid];
      @SuppressWarnings("unchecked")
      int cmp = midVal.compareTo(key);
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static <T> int binarySearch(T[] a, T key, Comparator<? super T> c) {
    return binarySearch(a, 0, a.length, key, c);
  }

  @SuppressWarnings("unchecked")
  public static <T> int binarySearch(T[] a, int fromIndex, int toIndex, T key, Comparator<? super T> c) {
    if (c == null) {
      return binarySearch((Object[]) a, fromIndex, toIndex, (Object) key);
    }
    rangeCheck(a.length, fromIndex, toIndex);
    int low = fromIndex;
    int high = toIndex - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      T midVal = a[mid];
      int cmp = c.compare(midVal, key);
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid;
      }
    }
    return -(low + 1);
  }

  public static void sort(byte[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(byte[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(byte[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(byte[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(char[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(char[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(char[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(char[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(short[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(short[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(short[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(short[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(int[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(int[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(int[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(int[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(long[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(long[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(long[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(long[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(float[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(float[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(float[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(float[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(double[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void sort(double[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void parallelSort(double[] a) {
    ArraySorts.sort(a, 0, a.length);
  }

  public static void parallelSort(double[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex);
  }

  public static void sort(Object[] a) {
    ArraySorts.sort(a, 0, a.length, null);
  }

  public static void sort(Object[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex, null);
  }

  public static <T> void sort(T[] a, Comparator<? super T> c) {
    ArraySorts.sort(a, 0, a.length, c);
  }

  public static <T> void sort(T[] a, int fromIndex, int toIndex, Comparator<? super T> c) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex, c);
  }

  public static <T extends Comparable<? super T>> void parallelSort(T[] a) {
    ArraySorts.sort(a, 0, a.length, null);
  }

  public static <T extends Comparable<? super T>> void parallelSort(T[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex, null);
  }

  public static <T> void parallelSort(T[] a, Comparator<? super T> cmp) {
    ArraySorts.sort(a, 0, a.length, cmp);
  }

  public static <T> void parallelSort(T[] a, int fromIndex, int toIndex, Comparator<? super T> cmp) {
    rangeCheck(a.length, fromIndex, toIndex);
    ArraySorts.sort(a, fromIndex, toIndex, cmp);
  }

  public static <T> void setAll(T[] array, java.util.function.IntFunction<? extends T> generator) {
    Objects.requireNonNull(generator);
    for (int i = 0; i < array.length; i++) {
      array[i] = generator.apply(i);
    }
  }

  public static void setAll(int[] array, java.util.function.IntUnaryOperator generator) {
    Objects.requireNonNull(generator);
    for (int i = 0; i < array.length; i++) {
      array[i] = generator.applyAsInt(i);
    }
  }

  public static void setAll(long[] array, java.util.function.IntToLongFunction generator) {
    Objects.requireNonNull(generator);
    for (int i = 0; i < array.length; i++) {
      array[i] = generator.applyAsLong(i);
    }
  }

  public static void setAll(double[] array, java.util.function.IntToDoubleFunction generator) {
    Objects.requireNonNull(generator);
    for (int i = 0; i < array.length; i++) {
      array[i] = generator.applyAsDouble(i);
    }
  }

  public static <T> void parallelSetAll(T[] array, java.util.function.IntFunction<? extends T> generator) {
    setAll(array, generator);
  }

  public static void parallelSetAll(int[] array, java.util.function.IntUnaryOperator generator) {
    setAll(array, generator);
  }

  public static void parallelSetAll(long[] array, java.util.function.IntToLongFunction generator) {
    setAll(array, generator);
  }

  public static void parallelSetAll(double[] array, java.util.function.IntToDoubleFunction generator) {
    setAll(array, generator);
  }

  public static <T> void parallelPrefix(T[] array, java.util.function.BinaryOperator<T> op) {
    Objects.requireNonNull(op);
    for (int i = 1; i < array.length; i++) {
      array[i] = op.apply(array[i - 1], array[i]);
    }
  }

  public static <T> void parallelPrefix(T[] array, int fromIndex, int toIndex, java.util.function.BinaryOperator<T> op) {
    Objects.requireNonNull(op);
    rangeCheck(array.length, fromIndex, toIndex);
    for (int i = fromIndex + 1; i < toIndex; i++) {
      array[i] = op.apply(array[i - 1], array[i]);
    }
  }

  public static void parallelPrefix(int[] array, java.util.function.IntBinaryOperator op) {
    Objects.requireNonNull(op);
    for (int i = 1; i < array.length; i++) {
      array[i] = op.applyAsInt(array[i - 1], array[i]);
    }
  }

  public static void parallelPrefix(int[] array, int fromIndex, int toIndex, java.util.function.IntBinaryOperator op) {
    Objects.requireNonNull(op);
    rangeCheck(array.length, fromIndex, toIndex);
    for (int i = fromIndex + 1; i < toIndex; i++) {
      array[i] = op.applyAsInt(array[i - 1], array[i]);
    }
  }

  public static void parallelPrefix(long[] array, java.util.function.LongBinaryOperator op) {
    Objects.requireNonNull(op);
    for (int i = 1; i < array.length; i++) {
      array[i] = op.applyAsLong(array[i - 1], array[i]);
    }
  }

  public static void parallelPrefix(long[] array, int fromIndex, int toIndex, java.util.function.LongBinaryOperator op) {
    Objects.requireNonNull(op);
    rangeCheck(array.length, fromIndex, toIndex);
    for (int i = fromIndex + 1; i < toIndex; i++) {
      array[i] = op.applyAsLong(array[i - 1], array[i]);
    }
  }

  public static void parallelPrefix(double[] array, java.util.function.DoubleBinaryOperator op) {
    Objects.requireNonNull(op);
    for (int i = 1; i < array.length; i++) {
      array[i] = op.applyAsDouble(array[i - 1], array[i]);
    }
  }

  public static void parallelPrefix(double[] array, int fromIndex, int toIndex, java.util.function.DoubleBinaryOperator op) {
    Objects.requireNonNull(op);
    rangeCheck(array.length, fromIndex, toIndex);
    for (int i = fromIndex + 1; i < toIndex; i++) {
      array[i] = op.applyAsDouble(array[i - 1], array[i]);
    }
  }

  private static boolean equal(Object a, Object b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  @SafeVarargs
  public static <T> List<T> asList(T... array) {
    return new ArrayAsList<T>(array);
  }

  private static final class ArrayAsList<E> extends AbstractList<E> implements RandomAccess, java.io.Serializable {
    private final E[] a;

    ArrayAsList(E[] array) {
      a = Objects.requireNonNull(array);
    }

    @Override
    public int size() {
      return a.length;
    }

    @Override
    public Object[] toArray() {
      return Arrays.copyOf(a, a.length, Object[].class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] target) {
      int size = size();
      if (target.length < size) {
        return (T[]) Arrays.copyOf(this.a, size, target.getClass());
      }
      System.arraycopy(this.a, 0, target, 0, size);
      if (target.length > size) {
        target[size] = null;
      }
      return target;
    }

    @Override
    public E get(int index) {
      return a[index];
    }

    @Override
    public E set(int index, E element) {
      E oldValue = a[index];
      a[index] = element;
      return oldValue;
    }

    @Override
    public int indexOf(Object o) {
      for (int i = 0; i < a.length; i++) {
        if (equal(o, a[i])) {
          return i;
        }
      }
      return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
      for (int i = a.length - 1; i >= 0; i--) {
        if (equal(o, a[i])) {
          return i;
        }
      }
      return -1;
    }

    @Override
    public boolean contains(Object o) {
      return indexOf(o) >= 0;
    }

    @Override
    public void forEach(java.util.function.Consumer<? super E> action) {
      Objects.requireNonNull(action);
      for (E e : a) {
        action.accept(e);
      }
    }

    @Override
    public void replaceAll(java.util.function.UnaryOperator<E> operator) {
      Objects.requireNonNull(operator);
      for (int i = 0; i < a.length; i++) {
        a[i] = operator.apply(a[i]);
      }
    }

    @Override
    public void sort(Comparator<? super E> c) {
      Arrays.sort(a, c);
    }

    @Override
    public Spliterator<E> spliterator() {
      return Arrays.spliterator(a);
    }
  }

  private static void checkRange(int len, int start, int stop) {
    rangeCheck(len, start, stop);
  }

  public static void fill(int[] array, int value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(int[] array, int start, int stop, int value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static void fill(char[] array, char value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(char[] array, int start, int stop, char value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static void fill(short[] array, short value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(short[] array, int start, int stop, short value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static void fill(byte[] array, byte value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }
  
  public static void fill(byte[] array, int start, int stop, byte value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static void fill(boolean[] array, boolean value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(boolean[] array, int start, int stop, boolean value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static void fill(long[] array, long value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(long[] array, int start, int stop, long value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

   public static void fill(float[] array, float value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(float[] array, int start, int stop, float value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

 public static void fill(double[] array, double value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(double[] array, int start, int stop, double value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static <T> void fill(T[] array, T value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static <T> void fill(T[] array, int start, int stop, T value) {
    checkRange(array.length, start, stop);
    for (int i=start;i<stop;i++) {
      array[i] = value;
    }
  }

  public static boolean[] copyOf(boolean[] array, int newLength) {
    boolean[] result = new boolean[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static byte[] copyOf(byte[] array, int newLength) {
    byte[] result = new byte[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static char[] copyOf(char[] array, int newLength) {
    char[] result = new char[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static double[] copyOf(double[] array, int newLength) {
    double[] result = new double[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static float[] copyOf(float[] array, int newLength) {
    float[] result = new float[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static int[] copyOf(int[] array, int newLength) {
    int[] result = new int[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static long[] copyOf(long[] array, int newLength) {
    long[] result = new long[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static short[] copyOf(short[] array, int newLength) {
    short[] result = new short[newLength];
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static <T> T[] copyOf(T[] array, int newLength) {
    Class<?> clazz = array.getClass().getComponentType();
    T[] result = (T[])Array.newInstance(clazz, newLength);
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static <T, U> T[] copyOf(U[] array, int newLength,
    Class<? extends T[]> newType)
  {
    T[] result = (T[])Array.newInstance(newType.getComponentType(), newLength);
    int length = array.length > newLength ? newLength : array.length;
    System.arraycopy(array, 0, result, 0, length);
    return result;
  }

  public static boolean[] copyOfRange(boolean[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    boolean[] result = new boolean[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static byte[] copyOfRange(byte[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    byte[] result = new byte[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static char[] copyOfRange(char[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    char[] result = new char[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static short[] copyOfRange(short[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    short[] result = new short[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static int[] copyOfRange(int[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    int[] result = new int[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static long[] copyOfRange(long[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    long[] result = new long[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static float[] copyOfRange(float[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    float[] result = new float[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static double[] copyOfRange(double[] original, int from, int to) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    double[] result = new double[newLength];
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static <T> T[] copyOfRange(T[] original, int from, int to) {
    return copyOfRange(original, from, to, (Class<? extends T[]>) original.getClass());
  }

  public static <T, U> T[] copyOfRange(U[] original, int from, int to, Class<? extends T[]> newType) {
    if (from > to) throw new IllegalArgumentException(from + " > " + to);
    int len = original.length;
    if (from < 0 || from > len) throw new ArrayIndexOutOfBoundsException(from);
    int newLength = to - from;
    T[] result = (T[]) Array.newInstance(newType.getComponentType(), newLength);
    int copyLength = Math.min(newLength, len - from);
    System.arraycopy(original, from, result, 0, copyLength);
    return result;
  }

  public static <T> Spliterator<T> spliterator(T[] array) {
    return Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static <T> Spliterator<T> spliterator(T[] array, int startInclusive, int endExclusive) {
    return Spliterators.spliterator(array, startInclusive, endExclusive, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static Spliterator.OfInt spliterator(int[] array) {
    return Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static Spliterator.OfInt spliterator(int[] array, int startInclusive, int endExclusive) {
    return Spliterators.spliterator(array, startInclusive, endExclusive, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static Spliterator.OfLong spliterator(long[] array) {
    return Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static Spliterator.OfLong spliterator(long[] array, int startInclusive, int endExclusive) {
    return Spliterators.spliterator(array, startInclusive, endExclusive, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static Spliterator.OfDouble spliterator(double[] array) {
    return Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static Spliterator.OfDouble spliterator(double[] array, int startInclusive, int endExclusive) {
    return Spliterators.spliterator(array, startInclusive, endExclusive, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  public static <T> java.util.stream.Stream<T> stream(T[] array) {
    return stream(array, 0, array.length);
  }

  public static <T> java.util.stream.Stream<T> stream(T[] array, int startInclusive, int endExclusive) {
    return java.util.stream.StreamSupport.stream(spliterator(array, startInclusive, endExclusive), false);
  }

  public static java.util.stream.IntStream stream(int[] array) {
    return stream(array, 0, array.length);
  }

  public static java.util.stream.IntStream stream(int[] array, int startInclusive, int endExclusive) {
    return java.util.stream.StreamSupport.intStream(spliterator(array, startInclusive, endExclusive), false);
  }

  public static java.util.stream.LongStream stream(long[] array) {
    return stream(array, 0, array.length);
  }

  public static java.util.stream.LongStream stream(long[] array, int startInclusive, int endExclusive) {
    return java.util.stream.StreamSupport.longStream(spliterator(array, startInclusive, endExclusive), false);
  }

  public static java.util.stream.DoubleStream stream(double[] array) {
    return stream(array, 0, array.length);
  }

  public static java.util.stream.DoubleStream stream(double[] array, int startInclusive, int endExclusive) {
    return java.util.stream.StreamSupport.doubleStream(spliterator(array, startInclusive, endExclusive), false);
  }
}
