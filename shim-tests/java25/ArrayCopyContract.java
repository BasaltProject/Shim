/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Array;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class ArrayCopyContract {
  public static class Token { public Token() { } }
  private static class Loader extends ClassLoader {
    Loader() { super(ArrayCopyContract.class.getClassLoader()); }
    Class<?> token() throws Exception {
      InputStream input = ArrayCopyContract.class.getResourceAsStream("/ArrayCopyContract$Token.class");
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      for (int n; (n = input.read(buffer)) != -1;) output.write(buffer, 0, n);
      input.close();
      byte[] bytes = output.toByteArray();
      return defineClass(null, bytes, 0, bytes.length);
    }
  }
  private static Object primitive(int type) {
    switch (type) {
    case 0: return new boolean[] {true, false, true, false};
    case 1: return new byte[] {-128, -1, 0, 127};
    case 2: return new char[] {0, 127, 32768, 65535};
    case 3: return new short[] {-32768, -1, 0, 32767};
    case 4: return new int[] {Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE};
    case 5: return new long[] {Long.MIN_VALUE, -1, 0, Long.MAX_VALUE};
    case 6: return new float[] {-0f, Float.intBitsToFloat(0x7fc12345), 0.5f, Float.POSITIVE_INFINITY};
    default: return new double[] {-0d, Double.longBitsToDouble(0x7ff8123456789abcL), 0.5d, Double.POSITIVE_INFINITY};
    }
  }

  private static String describe(Object value) {
    if (value == null) return "null";
    Class<?> type = value.getClass();
    if (type.isArray()) {
      StringBuilder result = new StringBuilder(type.getName()).append('[');
      for (int i = 0; i < Array.getLength(value); ++i) {
        if (i != 0) result.append(',');
        result.append(describe(Array.get(value, i)));
      }
      return result.append(']').toString();
    }
    if (value instanceof Character) return "char:" + (int) ((Character) value).charValue();
    if (value instanceof Float) return "float:" + Float.floatToRawIntBits((Float) value);
    if (value instanceof Double) return "double:" + Double.doubleToRawLongBits((Double) value);
    if (value instanceof Number || value instanceof Boolean || value instanceof CharSequence) return value.toString();
    return type.getName();
  }

  private static void copy(String name, Object source, int start, Object destination, int target, int length) {
    String result = "ok";
    try { System.arraycopy(source, start, destination, target, length); }
    catch (RuntimeException e) { result = e.getClass().getSimpleName(); }
    System.out.println(name + ":" + result + ":" + describe(destination));
  }

  public static void main(String[] args) throws Exception {
    for (int source = 0; source < 8; ++source) {
      for (int target = 0; target < 8; ++target) {
        copy("primitive-empty:" + source + ":" + target, primitive(source), 0, primitive(target), 0, 0);
        copy("primitive-typed:" + source + ":" + target, primitive(source), 0, primitive(target), 0, 1);
      }
      copy("primitive:" + source, primitive(source), 1, primitive(source), 0, 3);
      copy("primitive-reference:" + source, primitive(source), 0, new Object[4], 0, 0);
      copy("reference-primitive:" + source, new Object[4], 0, primitive(source), 0, 0);
    }
    Object[] choices = {null, new Object(), new int[2], new String[2]};
    for (int source = 0; source < choices.length; ++source) {
      for (int destination = 0; destination < choices.length; ++destination) {
        for (int length : new int[] {-1, 0, 1}) {
          copy("priority:" + source + ":" + destination + ":" + length,
            choices[source], -1, choices[destination], -1, length);
        }
      }
    }
    for (int position : new int[] {Integer.MIN_VALUE, -1, 0, 3, 4, 5, Integer.MAX_VALUE}) {
      copy("empty-source:" + position, new int[4], position, new int[4], 0, 0);
      copy("empty-destination:" + position, new int[4], 0, new int[4], position, 0);
      copy("empty-reference:" + position, new Object[4], position, new String[4], 0, 0);
    }
    for (boolean reverse : new boolean[] {false, true}) {
      int[] integers = {1, 2, 3, 4, 5};
      String[] strings = {"a", "b", "c", "d", "e"};
      copy("overlap-primitive:" + reverse, integers, reverse ? 1 : 0, integers, reverse ? 0 : 1, 4);
      copy("overlap-reference:" + reverse, strings, reverse ? 1 : 0, strings, reverse ? 0 : 1, 4);
    }
    copy("covariant", new String[] {"a", "b", "c"}, 0, new Object[3], 0, 3);
    copy("checked", new Object[] {"a", null, "c"}, 0, new String[] {"x", "y", "z"}, 0, 3);
    for (int bad = 0; bad < 4; ++bad) {
      Object[] source = {"a", "b", "c", "d"};
      source[bad] = Integer.valueOf(9);
      copy("partial:" + bad, source, 0, new String[] {"w", "x", "y", "z"}, 0, 4);
      copy("partial-offset:" + bad, source, 1, new String[] {"w", "x", "y", "z"}, 1, 3);
    }
    copy("interface-partial", new Object[] {"a", new StringBuilder("b"), Integer.valueOf(3)},
      0, new CharSequence[3], 0, 3);
    copy("cloneable-partial", new Object[] {new int[1], "bad"}, 0, new Cloneable[2], 0, 2);
    copy("primitive-matrix-partial", new Object[] {new int[1], new long[1]}, 0, new int[2][], 0, 2);
    copy("nested-reference", new Object[] {new String[] {"a"}, null}, 0, new String[2][], 0, 2);
    copy("reference-bounds-before-element", new Object[] {Integer.valueOf(1)}, 0, new String[1], 0, 2);
    copy("reference-empty-bad-elements", new Object[] {Integer.valueOf(1)}, 0, new String[1], 0, 0);
    int[][] ranges = {{Integer.MAX_VALUE, 0, 2}, {0, Integer.MAX_VALUE, 2},
      {0, 0, Integer.MAX_VALUE}, {Integer.MIN_VALUE, 0, 1}, {0, Integer.MIN_VALUE, 1},
      {0, 0, Integer.MIN_VALUE}, {3, 0, 2}, {0, 3, 2}, {4, 4, 0}, {0, 0, -1}};
    for (int i = 0; i < ranges.length; ++i) {
      int[] range = ranges[i];
      copy("extreme-primitive:" + i, primitive(7), range[0], primitive(7), range[1], range[2]);
      copy("extreme-reference:" + i, new Object[] {"a", "b", "c", "d"}, range[0],
        new String[] {"w", "x", "y", "z"}, range[1], range[2]);
    }
    Class<?> first = new Loader().token(), second = new Loader().token();
    Object firstValue = first.getDeclaredConstructor().newInstance();
    Object secondValue = second.getDeclaredConstructor().newInstance();
    Object[] source = (Object[]) Array.newInstance(first, 1);
    Object[] destination = (Object[]) Array.newInstance(second, 2);
    source[0] = firstValue;
    destination[0] = secondValue;
    destination[1] = secondValue;
    System.out.println("loader-identity:" + (first == second));
    copy("loader-reject", source, 0, destination, 0, 1);
    copy("loader-partial", new Object[] {null, firstValue}, 0, destination, 0, 2);
    System.out.println("loader-partial-state:" + (destination[0] == null && destination[1] == secondValue));
    copy("loader-broad", source, 0, new Object[1], 0, 1);
    source[0] = null;
    copy("loader-null", source, 0, destination, 0, 1);
  }
}
