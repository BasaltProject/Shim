/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 9
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInvoke {
  interface Action { Object run() throws Throwable; }

  public static int add(int a, int b) { return a + b; }
  public static long widen(long a, double b) { return a + (long) b; }
  public static String join(String a, Object b, char c, boolean d) { return a + b + c + d; }
  public static void nothing() { System.out.println("nothing"); }
  public static Integer boxed(Integer value) { return value + 1; }
  public int instance(int value) { return value * 3; }
  public static int[] array(int[] values) { return values; }

  static void probe(String label, Action action) {
    try {
      System.out.println(label + ":" + action.run());
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName());
    }
  }

  static MethodHandle find(String name, MethodType type) throws Throwable {
    return MethodHandles.lookup().findStatic(MethodHandleInvoke.class, name, type);
  }

  public static void main(String[] args) throws Throwable {
    MethodHandle add = find("add", MethodType.methodType(int.class, int.class, int.class));
    MethodHandle widen = find("widen", MethodType.methodType(long.class, long.class, double.class));
    MethodHandle join = find("join", MethodType.methodType(String.class, String.class, Object.class, char.class,
                                                          boolean.class));
    MethodHandle nothing = find("nothing", MethodType.methodType(void.class));
    MethodHandle boxed = find("boxed", MethodType.methodType(Integer.class, Integer.class));
    MethodHandle array = find("array", MethodType.methodType(int[].class, int[].class));
    MethodHandle instance = MethodHandles.lookup().findVirtual(MethodHandleInvoke.class, "instance",
        MethodType.methodType(int.class, int.class));

    probe("exact-int", () -> (int) add.invokeExact(2, 3));
    probe("exact-long", () -> (long) widen.invokeExact(4L, 5.0));
    probe("exact-mixed", () -> (String) join.invokeExact("a", (Object) "b", 'c', true));
    probe("exact-void", () -> { nothing.invokeExact(); return "done"; });
    probe("exact-instance", () -> (int) instance.invokeExact(new MethodHandleInvoke(), 7));
    probe("exact-array", () -> ((int[]) array.invokeExact(new int[] {1, 2, 3})).length);
    probe("exact-wrong-return", () -> (long) add.invokeExact(2, 3));
    probe("exact-wrong-argument", () -> (int) add.invokeExact(2L, 3));
    probe("invoke-same", () -> (int) add.invoke(2, 3));
    probe("invoke-widen-argument", () -> (int) add.invoke((short) 2, (byte) 3));
    probe("invoke-box-argument", () -> (int) boxed.invoke(41));
    probe("invoke-unbox-return", () -> (long) add.invoke(1, 2));
    probe("invoke-object-return", () -> (Object) add.invoke(1, 2));
    probe("invoke-drop-return", () -> { add.invoke(1, 2); return "dropped"; });
    probe("invoke-null", () -> ((MethodHandle) null).invoke());
    probe("invoke-incompatible", () -> (String) add.invoke("x", 2));
    probe("invoke-arity", () -> (int) add.invoke(1));
  }
}
