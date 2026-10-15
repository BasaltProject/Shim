// Minimum release: 21
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleCombinatorsModern {

  public static int add3(int a, int b, int c) {
    return a + b + c;
  }

  public static String concat(String a, int b) {
    return a + ":" + b;
  }

  public static String filterStr(String s) {
    return "[" + s + "]";
  }

  public static int filterInt(int x) {
    return x * 10;
  }

  public static String combine(int a, int b) {
    return "{" + a + "," + b + "}";
  }

  public static String targetForFold(String comb, int a, int b) {
    return comb + "->" + (a + b);
  }

  private static int sideEffectCount = 0;

  public static void sideEffect(int a) {
    sideEffectCount += a;
  }

  public static String targetForVoidFold(int x, int a) {
    return x + ":" + a;
  }

  public static boolean isEven(int n) {
    return (n % 2) == 0;
  }

  public static String evenBranch(int n) {
    return "even:" + n;
  }

  public static String oddBranch(int n) {
    return "odd:" + n;
  }

  public static String fallible(String input) throws Exception {
    if ("fail".equals(input)) {
      throw new IllegalArgumentException("planned-failure");
    }
    return "success:" + input;
  }

  public static String rescue(IllegalArgumentException ex, String input) {
    return "rescued:" + ex.getMessage() + ":" + input;
  }

  public static void main(String[] args) throws Throwable {
    testInsertArguments();
    testFilterArguments();
    testPermuteArguments();
    testFoldArguments();
    testFoldArgumentsVoid();
    testGuardWithTest();
    testCatchException();
    testZeroAndEmpty();
    testArrayCombinators();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testInsertArguments() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle mh = lookup.findStatic(MethodHandleCombinatorsModern.class, "add3",
        MethodType.methodType(int.class, int.class, int.class, int.class));

    MethodHandle bound = MethodHandles.insertArguments(mh, 1, 20);
    if (!bound.type().equals(MethodType.methodType(int.class, int.class, int.class))) {
      throw new AssertionError("insertArguments type mismatch: " + bound.type());
    }
    int res = (Integer) bound.invokeWithArguments(5, 3);
    if (res != 28) { // 5 + 20 + 3
      throw new AssertionError("insertArguments result mismatch: " + res);
    }
  }

  private static void testFilterArguments() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle target = lookup.findStatic(MethodHandleCombinatorsModern.class, "concat",
        MethodType.methodType(String.class, String.class, int.class));
    MethodHandle fStr = lookup.findStatic(MethodHandleCombinatorsModern.class, "filterStr",
        MethodType.methodType(String.class, String.class));
    MethodHandle fInt = lookup.findStatic(MethodHandleCombinatorsModern.class, "filterInt",
        MethodType.methodType(int.class, int.class));

    MethodHandle filtered = MethodHandles.filterArguments(target, 0, fStr, fInt);
    String res = (String) filtered.invokeWithArguments("apple", 7);
    if (!"[apple]:70".equals(res)) {
      throw new AssertionError("filterArguments result mismatch: " + res);
    }

    MethodHandle partialFilter = MethodHandles.filterArguments(target, 1, fInt);
    String res2 = (String) partialFilter.invokeWithArguments("orange", 3);
    if (!"orange:30".equals(res2)) {
      throw new AssertionError("partial filterArguments result mismatch: " + res2);
    }
  }

  private static void testPermuteArguments() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle target = lookup.findStatic(MethodHandleCombinatorsModern.class, "concat",
        MethodType.methodType(String.class, String.class, int.class));

    // Incoming type: (int, String)String -> reordered to (1, 0)
    MethodType newType = MethodType.methodType(String.class, int.class, String.class);
    MethodHandle permuted = MethodHandles.permuteArguments(target, newType, 1, 0);
    String res = (String) permuted.invokeWithArguments(42, "num");
    if (!"num:42".equals(res)) {
      throw new AssertionError("permuteArguments result mismatch: " + res);
    }
  }

  private static void testFoldArguments() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle target = lookup.findStatic(MethodHandleCombinatorsModern.class, "targetForFold",
        MethodType.methodType(String.class, String.class, int.class, int.class));
    MethodHandle comb = lookup.findStatic(MethodHandleCombinatorsModern.class, "combine",
        MethodType.methodType(String.class, int.class, int.class));

    MethodHandle folded = MethodHandles.foldArguments(target, comb);
    if (!folded.type().equals(MethodType.methodType(String.class, int.class, int.class))) {
      throw new AssertionError("folded type mismatch: " + folded.type());
    }
    String res = (String) folded.invokeWithArguments(3, 4);
    if (!"{3,4}->7".equals(res)) {
      throw new AssertionError("folded result mismatch: " + res);
    }
  }

  private static void testFoldArgumentsVoid() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle target = lookup.findStatic(MethodHandleCombinatorsModern.class, "targetForVoidFold",
        MethodType.methodType(String.class, int.class, int.class));
    MethodHandle comb = lookup.findStatic(MethodHandleCombinatorsModern.class, "sideEffect",
        MethodType.methodType(void.class, int.class));

    sideEffectCount = 0;
    MethodHandle folded = MethodHandles.foldArguments(target, 1, comb);
    String res = (String) folded.invokeWithArguments(100, 50);
    if (!"100:50".equals(res) || sideEffectCount != 50) {
      throw new AssertionError("void fold mismatch: res=" + res + ", sideEffectCount=" + sideEffectCount);
    }
  }

  private static void testGuardWithTest() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle test = lookup.findStatic(MethodHandleCombinatorsModern.class, "isEven",
        MethodType.methodType(boolean.class, int.class));
    MethodHandle tBranch = lookup.findStatic(MethodHandleCombinatorsModern.class, "evenBranch",
        MethodType.methodType(String.class, int.class));
    MethodHandle fBranch = lookup.findStatic(MethodHandleCombinatorsModern.class, "oddBranch",
        MethodType.methodType(String.class, int.class));

    MethodHandle gwt = MethodHandles.guardWithTest(test, tBranch, fBranch);
    String even = (String) gwt.invokeWithArguments(10);
    String odd = (String) gwt.invokeWithArguments(7);
    if (!"even:10".equals(even) || !"odd:7".equals(odd)) {
      throw new AssertionError("guardWithTest mismatch: even=" + even + ", odd=" + odd);
    }
  }

  private static void testCatchException() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle target = lookup.findStatic(MethodHandleCombinatorsModern.class, "fallible",
        MethodType.methodType(String.class, String.class));
    MethodHandle handler = lookup.findStatic(MethodHandleCombinatorsModern.class, "rescue",
        MethodType.methodType(String.class, IllegalArgumentException.class, String.class));

    MethodHandle wrapped = MethodHandles.catchException(target, IllegalArgumentException.class, handler);
    String ok = (String) wrapped.invokeWithArguments("test");
    if (!"success:test".equals(ok)) {
      throw new AssertionError("catchException success mismatch: " + ok);
    }
    String rescued = (String) wrapped.invokeWithArguments("fail");
    if (!"rescued:planned-failure:fail".equals(rescued)) {
      throw new AssertionError("catchException handler mismatch: " + rescued);
    }
  }

  private static void testZeroAndEmpty() throws Throwable {
    MethodHandle zInt = MethodHandles.zero(int.class);
    if ((Integer) zInt.invokeWithArguments() != 0) {
      throw new AssertionError("zero int mismatch");
    }
    MethodHandle zLong = MethodHandles.zero(long.class);
    if ((Long) zLong.invokeWithArguments() != 0L) {
      throw new AssertionError("zero long mismatch");
    }
    MethodHandle zStr = MethodHandles.zero(String.class);
    if (zStr.invokeWithArguments() != null) {
      throw new AssertionError("zero String mismatch");
    }

    MethodHandle empty = MethodHandles.empty(MethodType.methodType(String.class, int.class, double.class));
    if (empty.invokeWithArguments(12, 3.4) != null) {
      throw new AssertionError("empty mismatch");
    }
  }

  private static void testArrayCombinators() throws Throwable {
    MethodHandle ctor = MethodHandles.arrayConstructor(String[].class);
    MethodHandle len = MethodHandles.arrayLength(String[].class);
    MethodHandle getter = MethodHandles.arrayElementGetter(String[].class);
    MethodHandle setter = MethodHandles.arrayElementSetter(String[].class);

    String[] arr = (String[]) ctor.invokeWithArguments(3);
    if (arr.length != 3) {
      throw new AssertionError("arrayConstructor length mismatch");
    }
    setter.invokeWithArguments(arr, 1, "shim-val");
    if ((Integer) len.invokeWithArguments((Object) arr) != 3) {
      throw new AssertionError("arrayLength mismatch");
    }
    if (!"shim-val".equals(getter.invokeWithArguments(arr, 1))) {
      throw new AssertionError("arrayElementGetter mismatch");
    }
  }
}
