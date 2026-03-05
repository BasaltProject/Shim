/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;

public class RecordEqualsHashModern {
  record All(boolean z, byte b, short s, char c, int i, long l, float f, double d, String str, Object o, int[] arr) { }

  record Empty() { }

  record One(String name) { }

  record Gen<T>(T value, List<T> list) { }

  record Nested(One one, Gen<Integer> gen) { }

  private record Hidden(int x, double y) { }

  static final class Holder {
    private record Inner(String a, long b) { }

    static Object make(String a, long b) {
      return new Inner(a, b);
    }
  }

  static String h(Object o) {
    return Integer.toHexString(o.hashCode());
  }

  public static void main(String[] args) {
    int[] arr = {1, 2};
    All a1 = new All(true, (byte) 1, (short) 2, 'c', 3, 4L, 5.5f, 6.5, "s", "o", arr);
    All a2 = new All(true, (byte) 1, (short) 2, 'c', 3, 4L, 5.5f, 6.5, "s", "o", arr);
    All a3 = new All(true, (byte) 1, (short) 2, 'c', 3, 4L, 5.5f, 6.5, "s", "o", new int[] {1, 2});
    All a4 = new All(false, (byte) 1, (short) 2, 'c', 3, 4L, 5.5f, 6.5, "s", "o", arr);
    System.out.println(a1.equals(a2) + " " + a1.equals(a3) + " " + a1.equals(a4) + " " + a1.equals(null) + " " + a1.equals("x")
        + " " + a1.equals(a1));
    System.out.println((a1.hashCode() == a2.hashCode()) + " " + (a1.hashCode() == a4.hashCode()));
    All n1 = new All(false, (byte) 0, (short) 0, 'a', 0, 0L, Float.NaN, Double.NaN, null, null, null);
    All n2 = new All(false, (byte) 0, (short) 0, 'a', 0, 0L, Float.NaN, Double.NaN, null, null, null);
    All z1 = new All(false, (byte) 0, (short) 0, 'a', 0, 0L, 0.0f, 0.0, null, null, null);
    All z2 = new All(false, (byte) 0, (short) 0, 'a', 0, 0L, -0.0f, -0.0, null, null, null);
    System.out.println(n1.equals(n2) + " " + h(n1) + " " + z1.equals(z2) + " " + h(z1) + " " + h(z2));
    for (int k = 0; k < 8; ++k) {
      All v = new All((k & 1) != 0, (byte) -k, (short) (k * 300), (char) ('a' + k), k * 123456789, k * 98765432123L,
          k * 1.25f, k * 2.5e10, "v" + k, k, null);
      System.out.print(h(v) + " ");
    }
    System.out.println();
    System.out.println(new Empty().equals(new Empty()) + " " + h(new Empty()) + " " + new Empty().equals(null));
    System.out.println(new One("a").equals(new One("a")) + " " + new One("a").equals(new One("b")) + " " + new One(null).equals(new One(null))
        + " " + new One(null).equals(new One("a")) + " " + h(new One("a")) + " " + h(new One(null)));
    Gen<Integer> g1 = new Gen<>(5, List.of(1, 2));
    Gen<Integer> g2 = new Gen<>(5, new ArrayList<>(List.of(1, 2)));
    System.out.println(g1.equals(g2) + " " + h(g1) + " " + h(g2));
    Nested n = new Nested(new One("x"), g1);
    Nested m = new Nested(new One("x"), g2);
    System.out.println(n.equals(m) + " " + h(n) + " " + (n.hashCode() == m.hashCode()));
    System.out.println(new Hidden(1, 2.0).equals(new Hidden(1, 2.0)) + " " + h(new Hidden(1, 2.0)) + " " + new Hidden(1, 2.0).equals(new Hidden(2, 2.0)));
    System.out.println(Holder.make("a", 5).equals(Holder.make("a", 5)) + " " + h(Holder.make("a", 5)) + " " + Holder.make("a", 5).equals(Holder.make("b", 5)));
    Set<One> set = new HashSet<>();
    for (int i = 0; i < 1000; ++i) {
      set.add(new One("k" + (i % 10)));
    }
    System.out.println(set.size());
    System.out.println(a1.str() + " " + new One("q") + " " + new Hidden(3, 4.5));
  }
}
