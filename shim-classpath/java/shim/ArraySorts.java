/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.util.Comparator;

public final class ArraySorts {
  private static final int kInsertionLimit = 32;

  private ArraySorts() { }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static boolean greater(Object x, Object y, Comparator comparator) {
    return comparator == null ? ((Comparable) x).compareTo(y) > 0 : comparator.compare(x, y) > 0;
  }

  public static void sort(Object[] a, int from, int to, Comparator<?> comparator) {
    int count = to - from;
    if (count < 2) {
      return;
    }
    Object[] aux = new Object[count / 2 + 1];
    mergeSort(a, aux, from, to, comparator);
  }

  private static void insertion(Object[] a, int from, int to, Comparator<?> comparator) {
    for (int i = from + 1; i < to; ++i) {
      Object value = a[i];
      int j = i - 1;
      while (j >= from && greater(a[j], value, comparator)) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(Object[] a, Object[] aux, int lo, int hi, Comparator<?> comparator) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi, comparator);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid, comparator);
    mergeSort(a, aux, mid, hi, comparator);
    if (!greater(a[mid - 1], a[mid], comparator)) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (greater(aux[i], a[j], comparator)) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(byte[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    byte[] aux = new byte[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(byte[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      byte value = a[i];
      int j = i - 1;
      while (j >= from && a[j] > value) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(byte[] a, byte[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(a[mid - 1] > a[mid])) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (aux[i] > a[j]) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(char[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    char[] aux = new char[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(char[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      char value = a[i];
      int j = i - 1;
      while (j >= from && a[j] > value) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(char[] a, char[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(a[mid - 1] > a[mid])) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (aux[i] > a[j]) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(short[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    short[] aux = new short[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(short[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      short value = a[i];
      int j = i - 1;
      while (j >= from && a[j] > value) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(short[] a, short[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(a[mid - 1] > a[mid])) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (aux[i] > a[j]) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(int[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    int[] aux = new int[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(int[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      int value = a[i];
      int j = i - 1;
      while (j >= from && a[j] > value) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(int[] a, int[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(a[mid - 1] > a[mid])) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (aux[i] > a[j]) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(long[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    long[] aux = new long[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(long[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      long value = a[i];
      int j = i - 1;
      while (j >= from && a[j] > value) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(long[] a, long[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(a[mid - 1] > a[mid])) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (aux[i] > a[j]) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(float[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    float[] aux = new float[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(float[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      float value = a[i];
      int j = i - 1;
      while (j >= from && Float.compare(a[j], value) > 0) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(float[] a, float[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(Float.compare(a[mid - 1], a[mid]) > 0)) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (Float.compare(aux[i], a[j]) > 0) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }

  public static void sort(double[] a, int from, int to) {
    if (to - from < 2) {
      return;
    }
    double[] aux = new double[(to - from) / 2 + 1];
    mergeSort(a, aux, from, to);
  }

  private static void insertion(double[] a, int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      double value = a[i];
      int j = i - 1;
      while (j >= from && Double.compare(a[j], value) > 0) {
        a[j + 1] = a[j];
        --j;
      }
      a[j + 1] = value;
    }
  }

  private static void mergeSort(double[] a, double[] aux, int lo, int hi) {
    if (hi - lo <= kInsertionLimit) {
      insertion(a, lo, hi);
      return;
    }
    int mid = (lo + hi) >>> 1;
    mergeSort(a, aux, lo, mid);
    mergeSort(a, aux, mid, hi);
    if (!(Double.compare(a[mid - 1], a[mid]) > 0)) {
      return;
    }
    int leftLength = mid - lo;
    System.arraycopy(a, lo, aux, 0, leftLength);
    int i = 0;
    int j = mid;
    int k = lo;
    while (i < leftLength && j < hi) {
      if (Double.compare(aux[i], a[j]) > 0) {
        a[k++] = a[j++];
      } else {
        a[k++] = aux[i++];
      }
    }
    while (i < leftLength) {
      a[k++] = aux[i++];
    }
  }
}
