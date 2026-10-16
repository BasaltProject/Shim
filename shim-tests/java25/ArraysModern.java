/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 16
 */

import java.util.*;

public class ArraysModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      Object value = action.run();
      System.out.println(label + ":" + value);
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName() + (error instanceof ClassCastException ? "" : ":" + error.getMessage()));
    }
  }

  record Item(String name, int rank) {
    public String toString() { return name + rank; }
  }

  public static void main(String[] args) {
    int[] ints = {5, 3, 9, 1, 7, -4, 0, 2, 8, 6, 3, 3};
    probe("sort-int", () -> { int[] a = ints.clone(); Arrays.sort(a); return Arrays.toString(a); });
    probe("sort-int-range", () -> { int[] a = ints.clone(); Arrays.sort(a, 2, 8); return Arrays.toString(a); });
    probe("sort-int-bad", () -> { Arrays.sort(ints.clone(), 5, 2); return 0; });
    probe("sort-int-oob", () -> { Arrays.sort(ints.clone(), -1, 2); return 0; });
    probe("sort-int-oob-high", () -> { Arrays.sort(ints.clone(), 0, 99); return 0; });
    probe("sort-long", () -> { long[] a = {5L, -3L, Long.MAX_VALUE, Long.MIN_VALUE, 0L}; Arrays.sort(a); return Arrays.toString(a); });
    probe("sort-short", () -> { short[] a = {5, -3, 9, 1}; Arrays.sort(a); return Arrays.toString(a); });
    probe("sort-byte", () -> { byte[] a = {5, -3, 9, 1, -128, 127}; Arrays.sort(a); return Arrays.toString(a); });
    probe("sort-char", () -> { char[] a = "hello world".toCharArray(); Arrays.sort(a); return Arrays.toString(a); });
    probe("sort-double", () -> {
      double[] a = {3.5, Double.NaN, -0.0, 0.0, Double.NEGATIVE_INFINITY, 1e-300, Double.POSITIVE_INFINITY, -2.5};
      Arrays.sort(a);
      return Arrays.toString(a);
    });
    probe("sort-float", () -> {
      float[] a = {3.5f, Float.NaN, -0.0f, 0.0f, Float.NEGATIVE_INFINITY, 1e-30f, -2.5f};
      Arrays.sort(a);
      return Arrays.toString(a);
    });
    probe("sort-large", () -> {
      Random random = new Random(12345);
      int[] a = new int[5000];
      for (int i = 0; i < a.length; ++i) a[i] = random.nextInt(1000) - 500;
      int[] b = a.clone();
      Arrays.sort(a);
      boolean sorted = true;
      for (int i = 1; i < a.length; ++i) if (a[i - 1] > a[i]) sorted = false;
      long sumA = 0, sumB = 0;
      for (int i = 0; i < a.length; ++i) { sumA += a[i]; sumB += b[i]; }
      return sorted + "," + (sumA == sumB) + "," + a[0] + "," + a[a.length - 1] + "," + Arrays.hashCode(a);
    });
    probe("sort-objects-stable", () -> {
      Item[] items = new Item[100];
      for (int i = 0; i < items.length; ++i) items[i] = new Item("n" + i, i % 5);
      Arrays.sort(items, Comparator.comparingInt(Item::rank));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 12; ++i) sb.append(items[i]).append(' ');
      return sb;
    });
    probe("sort-objects-natural", () -> { String[] a = {"pear", "apple", "fig", "banana"}; Arrays.sort(a); return Arrays.toString(a); });
    probe("sort-objects-range", () -> { String[] a = {"d", "c", "b", "a", "z"}; Arrays.sort(a, 1, 4); return Arrays.toString(a); });
    probe("sort-objects-range-cmp", () -> {
      String[] a = {"d", "c", "b", "a", "z"};
      Arrays.sort(a, 1, 4, Comparator.reverseOrder());
      return Arrays.toString(a);
    });
    probe("sort-objects-null-cmp", () -> { String[] a = {"b", "a"}; Arrays.sort(a, null); return Arrays.toString(a); });
    probe("sort-objects-not-comparable", () -> { Object[] a = {new Object(), new Object()}; Arrays.sort(a); return 0; });
    probe("parallel-sort", () -> { int[] a = ints.clone(); Arrays.parallelSort(a); return Arrays.toString(a); });
    probe("parallel-sort-objects", () -> { String[] a = {"b", "c", "a"}; Arrays.parallelSort(a); return Arrays.toString(a); });

    probe("to-string-null", () -> Arrays.toString((int[]) null) + "," + Arrays.toString((Object[]) null) + "," + Arrays.toString(new int[0]));
    probe("to-string-char", () -> Arrays.toString(new char[] {'a', 'b'}) + Arrays.toString(new boolean[] {true, false}));
    probe("to-string-floats", () -> Arrays.toString(new float[] {1.5f, Float.NaN}) + Arrays.toString(new double[] {-0.0, 1e10}));
    probe("to-string-objects", () -> Arrays.toString(new Object[] {1, null, "x", new int[0].length}));
    probe("deep-to-string", () -> Arrays.deepToString(new Object[] {1, new int[] {1, 2}, new Object[] {"a", new String[] {"b"}}, null}));
    probe("deep-to-string-cycle", () -> {
      Object[] a = new Object[2];
      a[0] = "x";
      a[1] = a;
      return Arrays.deepToString(a);
    });
    probe("deep-to-string-null", () -> Arrays.deepToString(null));
    probe("hash-code", () -> Arrays.hashCode(new int[] {1, 2, 3}) + "," + Arrays.hashCode(new long[] {1L << 40, -1L}) + ","
        + Arrays.hashCode(new double[] {1.5, -0.0}) + "," + Arrays.hashCode(new float[] {1.5f}) + ","
        + Arrays.hashCode(new boolean[] {true, false}) + "," + Arrays.hashCode(new byte[] {-1, 2}) + ","
        + Arrays.hashCode(new char[] {'a'}) + "," + Arrays.hashCode(new short[] {7}));
    probe("hash-code-null", () -> Arrays.hashCode((int[]) null) + "," + Arrays.hashCode((Object[]) null) + "," + Arrays.hashCode(new Object[0]));
    probe("hash-code-objects", () -> Arrays.hashCode(new Object[] {1, "a", null}));
    probe("deep-hash-code", () -> Arrays.deepHashCode(new Object[] {1, new int[] {1, 2}, new Object[] {"a"}, null}));
    probe("equals-basic", () -> Arrays.equals(new int[] {1, 2}, new int[] {1, 2}) + "," + Arrays.equals(new int[] {1}, new int[] {2})
        + "," + Arrays.equals((int[]) null, (int[]) null) + "," + Arrays.equals(new int[0], null));
    probe("equals-float", () -> Arrays.equals(new float[] {Float.NaN}, new float[] {Float.NaN}) + "," + Arrays.equals(new double[] {0.0}, new double[] {-0.0}));
    probe("equals-boolean", () -> Arrays.equals(new boolean[] {true}, new boolean[] {true}));
    probe("equals-range", () -> Arrays.equals(new int[] {1, 2, 3, 4}, 1, 3, new int[] {9, 2, 3}, 1, 3) + "," + Arrays.equals(new int[] {1, 2}, 0, 1, new int[] {1, 2}, 0, 2));
    probe("equals-range-bad", () -> Arrays.equals(new int[] {1, 2}, 0, 5, new int[] {1, 2}, 0, 2));
    probe("equals-objects-cmp", () -> Arrays.equals(new String[] {"a", "B"}, new String[] {"A", "b"}, String.CASE_INSENSITIVE_ORDER));
    probe("equals-objects-range-cmp", () -> Arrays.equals(new String[] {"x", "a"}, 1, 2, new String[] {"A"}, 0, 1, String.CASE_INSENSITIVE_ORDER));
    probe("deep-equals", () -> Arrays.deepEquals(new Object[] {new int[] {1}, "a"}, new Object[] {new int[] {1}, "a"}) + ","
        + Arrays.deepEquals(new Object[] {new int[] {1}}, new Object[] {new int[] {2}}));
    probe("compare-int", () -> Arrays.compare(new int[] {1, 2}, new int[] {1, 3}) + "," + Arrays.compare(new int[] {1}, new int[] {1, 2})
        + "," + Arrays.compare(new int[] {1, 2}, new int[] {1, 2}) + "," + Arrays.compare((int[]) null, new int[0]));
    probe("compare-byte", () -> Arrays.compare(new byte[] {1, 2}, new byte[] {1, 9}) + "," + Arrays.compareUnsigned(new byte[] {-1}, new byte[] {1}));
    probe("compare-double", () -> Arrays.compare(new double[] {0.0}, new double[] {-0.0}) + "," + Arrays.compare(new double[] {Double.NaN}, new double[] {1.0}));
    probe("compare-boolean", () -> Arrays.compare(new boolean[] {true}, new boolean[] {false}));
    probe("compare-range", () -> Arrays.compare(new int[] {9, 1, 2}, 1, 3, new int[] {1, 3}, 0, 2));
    probe("compare-objects", () -> Arrays.compare(new String[] {"a", "b"}, new String[] {"a", "c"}) + "," + Arrays.compare(new String[] {"a", null}, new String[] {"a", "b"}));
    probe("compare-objects-cmp", () -> Arrays.compare(new String[] {"a", "B"}, new String[] {"a", "b"}, String.CASE_INSENSITIVE_ORDER));
    probe("compare-unsigned", () -> Arrays.compareUnsigned(new int[] {-1}, new int[] {1}) + "," + Arrays.compareUnsigned(new long[] {-1L}, new long[] {1L})
        + "," + Arrays.compareUnsigned(new short[] {-1}, new short[] {1}));
    probe("mismatch", () -> Arrays.mismatch(new int[] {1, 2, 3}, new int[] {1, 2, 4}) + "," + Arrays.mismatch(new int[] {1, 2}, new int[] {1, 2, 3})
        + "," + Arrays.mismatch(new int[] {1}, new int[] {1}));
    probe("mismatch-range", () -> Arrays.mismatch(new int[] {0, 1, 2}, 1, 3, new int[] {1, 5}, 0, 2));
    probe("mismatch-objects", () -> Arrays.mismatch(new String[] {"a", "b"}, new String[] {"a", "c"}) + ","
        + Arrays.mismatch(new String[] {"a", "B"}, new String[] {"A", "b"}, String.CASE_INSENSITIVE_ORDER));
    probe("mismatch-float", () -> Arrays.mismatch(new float[] {Float.NaN, 1f}, new float[] {Float.NaN, 2f}));

    probe("search-int", () -> { int[] a = {1, 3, 5, 7, 9}; return Arrays.binarySearch(a, 9) + "," + Arrays.binarySearch(a, 1) + "," + Arrays.binarySearch(a, 4)
        + "," + Arrays.binarySearch(a, 100) + "," + Arrays.binarySearch(a, -5) + "," + Arrays.binarySearch(a, 1, 3, 7); });
    probe("search-long", () -> Arrays.binarySearch(new long[] {1L, 5L, 9L}, 9L));
    probe("search-short-byte-char", () -> Arrays.binarySearch(new short[] {1, 5, 9}, (short) 5) + "," + Arrays.binarySearch(new byte[] {1, 5, 9}, (byte) 9)
        + "," + Arrays.binarySearch(new char[] {'a', 'c', 'e'}, 'd'));
    probe("search-double", () -> Arrays.binarySearch(new double[] {-0.0, 0.0, 1.5, Double.NaN}, 0.0) + "," + Arrays.binarySearch(new double[] {1.0, 2.0}, 1.5)
        + "," + Arrays.binarySearch(new double[] {-0.0, 0.0}, -0.0));
    probe("search-float", () -> Arrays.binarySearch(new float[] {1f, 2f, 3f}, 3f) + "," + Arrays.binarySearch(new float[] {1f, 2f, 3f}, 1, 3, 1f));
    probe("search-objects", () -> Arrays.binarySearch(new String[] {"a", "c", "e"}, "c") + "," + Arrays.binarySearch(new String[] {"a", "c", "e"}, "d"));
    probe("search-cmp", () -> Arrays.binarySearch(new String[] {"e", "c", "a"}, "c", Comparator.reverseOrder()) + ","
        + Arrays.binarySearch(new String[] {"a", "c"}, "b", null));
    probe("search-bad-range", () -> Arrays.binarySearch(new int[] {1, 2}, 3, 1, 1));

    probe("prefix-int", () -> { int[] a = {1, 2, 3, 4}; Arrays.parallelPrefix(a, Integer::sum); return Arrays.toString(a); });
    probe("prefix-range", () -> { long[] a = {1, 2, 3, 4}; Arrays.parallelPrefix(a, 1, 4, (x, y) -> x * y); return Arrays.toString(a); });
    probe("prefix-double", () -> { double[] a = {1, 2, 3}; Arrays.parallelPrefix(a, Math::max); return Arrays.toString(a); });
    probe("prefix-objects", () -> { String[] a = {"a", "b", "c"}; Arrays.parallelPrefix(a, String::concat); return Arrays.toString(a); });
    probe("set-all", () -> { int[] a = new int[5]; Arrays.setAll(a, i -> i * i); return Arrays.toString(a); });
    probe("set-all-objects", () -> { String[] a = new String[3]; Arrays.setAll(a, i -> "s" + i); return Arrays.toString(a); });
    probe("set-all-long-double", () -> { long[] a = new long[3]; Arrays.setAll(a, i -> i * 10L); double[] d = new double[2]; Arrays.parallelSetAll(d, i -> i + 0.5);
        return Arrays.toString(a) + Arrays.toString(d); });
    probe("fill-range-bad", () -> { Arrays.fill(new int[3], 2, 1, 0); return 0; });
    probe("fill-range-oob", () -> { Arrays.fill(new int[3], -1, 1, 0); return 0; });
    probe("copy-range", () -> Arrays.toString(Arrays.copyOfRange(new int[] {1, 2, 3}, 1, 5)));
    probe("copy-range-bad", () -> Arrays.copyOfRange(new int[] {1, 2, 3}, 2, 1));
    probe("as-list", () -> { List<String> list = Arrays.asList("c", "a", "b"); Collections.sort(list); return list; });
    probe("stream", () -> Arrays.stream(new int[] {1, 2, 3, 4}, 1, 3).sum());
  }
}
