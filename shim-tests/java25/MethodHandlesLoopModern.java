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
import java.util.List;

public class MethodHandlesLoopModern {

  public static int parsePositive(String s) {
    if ("err".equals(s)) {
      throw new IllegalArgumentException("invalid input: err");
    }
    return Integer.parseInt(s);
  }

  public static int cleanupDouble(Throwable t, int result, String s) {
    if (t != null) {
      return -1;
    }
    return result * 2;
  }

  public static int cleanupOverrideEx(Throwable t, int result, String s) {
    throw new IllegalStateException("cleanup override exception");
  }

  public static int whileInit(int start, int limit) {
    return start;
  }

  public static boolean whilePred(int i, int start, int limit) {
    return i < limit;
  }

  public static int whileStep(int i, int start, int limit) {
    return i + 1;
  }

  public static int countStep(int acc, int index) {
    return acc + index;
  }

  public static int iterStep(int acc, String elem) {
    return acc + elem.length();
  }

  public static int multiInitI() {
    return 0;
  }

  public static int multiStepI(int i, int sum) {
    return i + 1;
  }

  public static boolean multiPredI(int i, int sum) {
    return i < 5;
  }

  public static int multiFiniI(int i, int sum) {
    return sum;
  }

  public static int multiInitSum() {
    return 0;
  }

  public static int multiStepSum(int sum, int i) {
    return sum + i;
  }

  public static void main(String[] args) throws Throwable {
    testTryFinally();
    testWhileLoop();
    testDoWhileLoop();
    testCountedLoop();
    testIteratedLoop();
    testMultiClauseLoop();
    testValidationErrors();
    System.out.println("MethodHandlesLoopModern passed successfully");
  }

  private static void testTryFinally() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle targetMh = lookup.findStatic(
        MethodHandlesLoopModern.class,
        "parsePositive",
        MethodType.methodType(int.class, String.class));

    MethodHandle cleanupMh = lookup.findStatic(
        MethodHandlesLoopModern.class,
        "cleanupDouble",
        MethodType.methodType(int.class, Throwable.class, int.class, String.class));

    MethodHandle tf = MethodHandles.tryFinally(targetMh, cleanupMh);

    int normal = (int) tf.invokeWithArguments("15");
    if (normal != 30) {
      throw new AssertionError("tryFinally normal result mismatch: " + normal);
    }

    boolean caught = false;
    try {
      int ignored = (int) tf.invokeWithArguments("err");
    } catch (IllegalArgumentException e) {
      caught = true;
      if (!"invalid input: err".equals(e.getMessage())) {
        throw new AssertionError("unexpected exception message: " + e.getMessage());
      }
    }
    if (!caught) {
      throw new AssertionError("expected target exception rethrown from tryFinally");
    }

    MethodHandle cleanupOverride = lookup.findStatic(
        MethodHandlesLoopModern.class,
        "cleanupOverrideEx",
        MethodType.methodType(int.class, Throwable.class, int.class, String.class));
    MethodHandle tfOverride = MethodHandles.tryFinally(targetMh, cleanupOverride);

    caught = false;
    try {
      int ignored = (int) tfOverride.invokeWithArguments("err");
    } catch (IllegalStateException e) {
      caught = true;
      if (!"cleanup override exception".equals(e.getMessage())) {
        throw new AssertionError("unexpected override exception: " + e.getMessage());
      }
    }
    if (!caught) {
      throw new AssertionError("expected cleanup exception to override target exception");
    }
  }

  private static void testWhileLoop() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle init = lookup.findStatic(
        MethodHandlesLoopModern.class, "whileInit", MethodType.methodType(int.class, int.class, int.class));
    MethodHandle pred = lookup.findStatic(
        MethodHandlesLoopModern.class, "whilePred", MethodType.methodType(boolean.class, int.class, int.class, int.class));
    MethodHandle step = lookup.findStatic(
        MethodHandlesLoopModern.class, "whileStep", MethodType.methodType(int.class, int.class, int.class, int.class));

    MethodHandle wl = MethodHandles.whileLoop(init, pred, step);

    int res1 = (int) wl.invokeWithArguments(0, 10);
    if (res1 != 10) {
      throw new AssertionError("whileLoop 0 to 10 mismatch: " + res1);
    }

    int res2 = (int) wl.invokeWithArguments(10, 10);
    if (res2 != 10) {
      throw new AssertionError("whileLoop immediate false mismatch: " + res2);
    }
  }

  private static void testDoWhileLoop() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle init = lookup.findStatic(
        MethodHandlesLoopModern.class, "whileInit", MethodType.methodType(int.class, int.class, int.class));
    MethodHandle pred = lookup.findStatic(
        MethodHandlesLoopModern.class, "whilePred", MethodType.methodType(boolean.class, int.class, int.class, int.class));
    MethodHandle step = lookup.findStatic(
        MethodHandlesLoopModern.class, "whileStep", MethodType.methodType(int.class, int.class, int.class, int.class));

    MethodHandle dwl = MethodHandles.doWhileLoop(init, step, pred);

    int res1 = (int) dwl.invokeWithArguments(0, 5);
    if (res1 != 5) {
      throw new AssertionError("doWhileLoop 0 to 5 mismatch: " + res1);
    }

    int res2 = (int) dwl.invokeWithArguments(10, 5);
    if (res2 != 11) {
      throw new AssertionError("doWhileLoop must execute body once even if pred is false: " + res2);
    }
  }

  private static void testCountedLoop() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle step = lookup.findStatic(
        MethodHandlesLoopModern.class, "countStep", MethodType.methodType(int.class, int.class, int.class));

    MethodHandle iters = MethodHandles.constant(int.class, 5);
    MethodHandle init = MethodHandles.constant(int.class, 0);

    MethodHandle cl1 = MethodHandles.countedLoop(iters, init, step);
    int sum1 = (int) cl1.invokeWithArguments();
    if (sum1 != 10) { // 0 + 1 + 2 + 3 + 4 = 10
      throw new AssertionError("countedLoop 5 mismatch: " + sum1);
    }

    MethodHandle start = MethodHandles.constant(int.class, 2);
    MethodHandle end = MethodHandles.constant(int.class, 5);
    MethodHandle cl2 = MethodHandles.countedLoop(start, end, init, step);
    int sum2 = (int) cl2.invokeWithArguments();
    if (sum2 != 9) { // 2 + 3 + 4 = 9
      throw new AssertionError("countedLoop 2 to 5 mismatch: " + sum2);
    }
  }

  private static void testIteratedLoop() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle step = lookup.findStatic(
        MethodHandlesLoopModern.class, "iterStep", MethodType.methodType(int.class, int.class, String.class));
    MethodHandle init = MethodHandles.constant(int.class, 0);

    MethodHandle il = MethodHandles.iteratedLoop(null, init, step);
    int total = (int) il.invokeWithArguments(new Object[] { List.of("alpha", "beta", "gamma") });
    if (total != 14) { // 5 + 4 + 5 = 14
      throw new AssertionError("iteratedLoop total mismatch: " + total);
    }
  }

  private static void testMultiClauseLoop() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle initI = lookup.findStatic(
        MethodHandlesLoopModern.class, "multiInitI", MethodType.methodType(int.class));
    MethodHandle stepI = lookup.findStatic(
        MethodHandlesLoopModern.class, "multiStepI", MethodType.methodType(int.class, int.class, int.class));
    MethodHandle predI = lookup.findStatic(
        MethodHandlesLoopModern.class, "multiPredI", MethodType.methodType(boolean.class, int.class, int.class));
    MethodHandle finiI = lookup.findStatic(
        MethodHandlesLoopModern.class, "multiFiniI", MethodType.methodType(int.class, int.class, int.class));

    MethodHandle initSum = lookup.findStatic(
        MethodHandlesLoopModern.class, "multiInitSum", MethodType.methodType(int.class));
    MethodHandle stepSum = lookup.findStatic(
        MethodHandlesLoopModern.class, "multiStepSum", MethodType.methodType(int.class, int.class, int.class));

    MethodHandle loopHandle = MethodHandles.loop(
        new MethodHandle[] { initI, stepI, predI, finiI },
        new MethodHandle[] { initSum, stepSum });

    int result = (int) loopHandle.invokeWithArguments();
    if (result != 10) { // 1 + 2 + 3 + 4 = 10
      throw new AssertionError("multiClauseLoop mismatch: " + result);
    }
  }

  private static void testValidationErrors() {
    boolean caught = false;
    try {
      MethodHandles.loop();
    } catch (IllegalArgumentException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("Expected IllegalArgumentException for empty clauses in loop");
    }

    caught = false;
    try {
      MethodHandles.loop(new MethodHandle[][] { null });
    } catch (IllegalArgumentException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("Expected IllegalArgumentException for null clause in loop");
    }

    caught = false;
    try {
      MethodHandles.loop(new MethodHandle[][] { new MethodHandle[5] });
    } catch (IllegalArgumentException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("Expected IllegalArgumentException for clause > 4 in loop");
    }
  }
}
