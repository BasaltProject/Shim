/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.SwitchPoint;
import java.lang.invoke.VolatileCallSite;
import java.lang.invoke.WrongMethodTypeException;

public class CallSitesAndInvokersModern {

  public static void main(String[] args) throws Throwable {
    testConstantCallSite();
    testMutableCallSite();
    testVolatileCallSite();
    testSwitchPoint();
    testSpreaderAndCollector();
    testVarargsCollector();
    testCollectArguments();
    testInvokers();
    testWrongMethodTypeException();
    System.out.println("CallSitesAndInvokersModern passed successfully");
  }

  public static int add(int a, int b) {
    return a + b;
  }

  public static int multiply(int a, int b) {
    return a * b;
  }

  public static int sumThree(int a, int b, int c) {
    return a + b + c;
  }

  public static String joinStrings(String prefix, String[] items) {
    return prefix + ":" + String.join("-", items);
  }

  public static int square(int x) {
    return x * x;
  }

  private static void testConstantCallSite() throws Throwable {
    MethodHandle addMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "add", MethodType.methodType(int.class, int.class, int.class));

    ConstantCallSite ccs = new ConstantCallSite(addMH);
    System.out.println("ccs type: " + ccs.type());
    System.out.println("ccs getTarget eq: " + (ccs.getTarget() == addMH));
    System.out.println("ccs invoker invoke: " + ccs.dynamicInvoker().invokeWithArguments(10, 20));

    boolean caughtUoe = false;
    try {
      ccs.setTarget(addMH);
    } catch (UnsupportedOperationException e) {
      caughtUoe = true;
    }
    System.out.println("ccs setTarget threw UOE: " + caughtUoe);
  }

  private static void testMutableCallSite() throws Throwable {
    MethodType mt = MethodType.methodType(int.class, int.class, int.class);
    MethodHandle addMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "add", mt);
    MethodHandle mulMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "multiply", mt);

    MutableCallSite mcs = new MutableCallSite(addMH);
    System.out.println("mcs type: " + mcs.type());

    MethodHandle invoker = mcs.dynamicInvoker();
    System.out.println("mcs initial invoker: " + invoker.invokeWithArguments(3, 4));

    mcs.setTarget(mulMH);
    System.out.println("mcs after setTarget: " + invoker.invokeWithArguments(3, 4));

    boolean caughtWmte = false;
    try {
      MethodHandle wrongTypeMH = MethodHandles.lookup().findStatic(
          CallSitesAndInvokersModern.class, "square", MethodType.methodType(int.class, int.class));
      mcs.setTarget(wrongTypeMH);
    } catch (WrongMethodTypeException e) {
      caughtWmte = true;
    }
    System.out.println("mcs setTarget wrong type threw WMTE: " + caughtWmte);

    MutableCallSite.syncAll(new MutableCallSite[] { mcs });
    System.out.println("mcs syncAll passed: true");
  }

  private static void testVolatileCallSite() throws Throwable {
    MethodType mt = MethodType.methodType(int.class, int.class, int.class);
    MethodHandle addMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "add", mt);
    MethodHandle mulMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "multiply", mt);

    VolatileCallSite vcs = new VolatileCallSite(addMH);
    System.out.println("vcs type: " + vcs.type());

    MethodHandle invoker = vcs.dynamicInvoker();
    System.out.println("vcs initial invoker: " + invoker.invokeWithArguments(5, 6));

    vcs.setTarget(mulMH);
    System.out.println("vcs after setTarget: " + invoker.invokeWithArguments(5, 6));
  }

  private static void testSwitchPoint() throws Throwable {
    MethodType mt = MethodType.methodType(int.class, int.class, int.class);
    MethodHandle addMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "add", mt);
    MethodHandle mulMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "multiply", mt);

    SwitchPoint sp = new SwitchPoint();
    System.out.println("sp initially invalidated: " + sp.hasBeenInvalidated());

    MethodHandle guarded = sp.guardWithTest(addMH, mulMH);
    System.out.println("guarded before invalidation: " + guarded.invokeWithArguments(3, 5));

    SwitchPoint.invalidateAll(new SwitchPoint[] { sp });
    System.out.println("sp after invalidation: " + sp.hasBeenInvalidated());
    System.out.println("guarded after invalidation: " + guarded.invokeWithArguments(3, 5));

    SwitchPoint sp2 = new SwitchPoint();
    MethodHandle guarded2 = sp2.guardWithTest(addMH, mulMH);
    System.out.println("second switchPoint guardWithTest: " + guarded2.invokeWithArguments(10, 2));
  }

  private static void testSpreaderAndCollector() throws Throwable {
    MethodHandle sumMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "sumThree",
        MethodType.methodType(int.class, int.class, int.class, int.class));

    MethodHandle spreader = sumMH.asSpreader(int[].class, 3);
    System.out.println("spreader type: " + spreader.type());
    System.out.println("spreader result: " + spreader.invokeWithArguments(new int[] { 1, 2, 3 }));

    MethodHandle joinMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "joinStrings",
        MethodType.methodType(String.class, String.class, String[].class));

    MethodHandle collector = joinMH.asCollector(String[].class, 2);
    System.out.println("collector type: " + collector.type());
    System.out.println("collector result: " + collector.invokeWithArguments("items", "foo", "bar"));
  }

  private static void testVarargsCollector() throws Throwable {
    MethodHandle joinMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "joinStrings",
        MethodType.methodType(String.class, String.class, String[].class));

    MethodHandle varargsMH = joinMH.asVarargsCollector(String[].class);
    System.out.println("isVarargsCollector: " + varargsMH.isVarargsCollector());
    System.out.println("asFixedArity eq: " + (varargsMH.asFixedArity() == joinMH));

    String fixedArray = (String) varargsMH.asFixedArity().invokeWithArguments("data", new String[] { "one", "two" });
    System.out.println("fixed arity direct array: " + fixedArray);

    String collected = (String) varargsMH.invokeWithArguments("data", "alpha", "beta", "gamma");
    System.out.println("varargs collected: " + collected);
  }

  private static void testCollectArguments() throws Throwable {
    MethodHandle addMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "add",
        MethodType.methodType(int.class, int.class, int.class));
    MethodHandle sqMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "square",
        MethodType.methodType(int.class, int.class));

    MethodHandle collected = MethodHandles.collectArguments(addMH, 1, sqMH);
    System.out.println("collectArguments type: " + collected.type());
    System.out.println("collectArguments result: " + collected.invokeWithArguments(10, 5));
  }

  private static void testInvokers() throws Throwable {
    MethodType mt = MethodType.methodType(int.class, int.class, int.class);
    MethodHandle addMH = MethodHandles.lookup().findStatic(
        CallSitesAndInvokersModern.class, "add", mt);

    MethodHandle exactInvoker = MethodHandles.exactInvoker(mt);
    System.out.println("exactInvoker type: " + exactInvoker.type());
    System.out.println("exactInvoker result: " + exactInvoker.invokeWithArguments(addMH, 7, 8));

    MethodHandle genericInvoker = MethodHandles.invoker(mt);
    System.out.println("genericInvoker result: " + genericInvoker.invokeWithArguments(addMH, 15, 25));

    MethodHandle spreadInvoker = MethodHandles.spreadInvoker(mt, 0);
    System.out.println("spreadInvoker result: " + spreadInvoker.invokeWithArguments(addMH, new Object[] { 100, 200 }));
  }

  private static void testWrongMethodTypeException() {
    WrongMethodTypeException wmte = new WrongMethodTypeException("bad type");
    System.out.println("wmte msg: " + wmte.getMessage());
    System.out.println("wmte is RuntimeException: " + (wmte instanceof RuntimeException));
  }
}
