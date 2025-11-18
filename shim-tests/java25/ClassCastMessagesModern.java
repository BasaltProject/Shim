/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
public class ClassCastMessagesModern {
  static class A {} static class B extends A {} static class C extends A {}
  static void t(String n, Runnable r) { try { r.run(); System.out.println(n + ": ok"); } catch (Throwable e) { System.out.println(n + ": " + e.getClass().getName() + ": " + e.getMessage()); } }
  public static void main(String[] a) throws Exception {
    Object o1 = "str", o2 = new B(), o3 = new int[0], o4 = new String[0], o5 = new ArrayList<String>(), o6 = new C[0];
    t("string->number", () -> { Number n = (Number) o1; });
    t("b->c", () -> { C c = (C) o2; });
    t("string->b", () -> { B b = (B) o1; });
    t("b->string", () -> { String s = (String) o2; });
    t("int[]->string[]", () -> { String[] s = (String[]) o3; });
    t("string[]->int[]", () -> { int[] s = (int[]) o4; });
    t("c[]->b[]", () -> { B[] s = (B[]) o6; });
    t("list->map", () -> { Map<?, ?> m = (Map<?, ?>) o5; });
    t("string->comparable ok", () -> { Comparable<?> c = (Comparable<?>) o1; });
    t("Class.cast", () -> String.class.cast(o2));
    t("Class.cast2", () -> Number.class.cast(o1));
    t("integer->long", () -> { Object x = Integer.valueOf(1); Long l = (Long) x; });
    t("generic", () -> { List raw = new ArrayList(); raw.add(1); List<String> ls = raw; String s = ls.get(0); });
  }
}
