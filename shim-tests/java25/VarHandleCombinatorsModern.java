/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 25

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.List;

public class VarHandleCombinatorsModern {

  public static int offsetByTen(int i) {
    return i + 10;
  }

  public static int sum(int a, int b) {
    return a + b;
  }

  public static void main(String[] args) throws Throwable {
    testFilterValue();
    testInsertCoordinates();
    testDropCoordinates();
    testFilterCoordinates();
    testPermuteCoordinates();
    testCollectCoordinates();
    testValidationErrors();
    System.out.println("VarHandleCombinatorsModern passed successfully");
  }

  private static void testFilterValue() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    VarHandle intArrVh = MethodHandles.arrayElementVarHandle(int[].class);

    MethodHandle toTarget = lookup.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
    MethodHandle fromTarget = lookup.findStatic(Integer.class, "toString", MethodType.methodType(String.class, int.class));

    VarHandle strViewVh = MethodHandles.filterValue(intArrVh, toTarget, fromTarget);
    if (strViewVh.varType() != String.class) {
      throw new AssertionError("filterValue varType mismatch: " + strViewVh.varType());
    }

    MethodHandle setMh = strViewVh.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle getMh = strViewVh.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle casMh = strViewVh.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
    MethodHandle gasMh = strViewVh.toMethodHandle(VarHandle.AccessMode.GET_AND_SET);

    int[] arr = new int[4];
    setMh.invokeWithArguments(arr, 0, "100");
    if (arr[0] != 100) {
      throw new AssertionError("raw array value mismatch: " + arr[0]);
    }
    if (!"100".equals(getMh.invokeWithArguments(arr, 0))) {
      throw new AssertionError("filtered get mismatch");
    }

    boolean casOk = (boolean) casMh.invokeWithArguments(arr, 0, "100", "200");
    if (!casOk || arr[0] != 200 || !"200".equals(getMh.invokeWithArguments(arr, 0))) {
      throw new AssertionError("filtered compareAndSet mismatch");
    }

    String oldVal = (String) gasMh.invokeWithArguments(arr, 0, "300");
    if (!"200".equals(oldVal) || arr[0] != 300) {
      throw new AssertionError("filtered getAndSet mismatch");
    }
  }

  private static void testInsertCoordinates() throws Throwable {
    VarHandle strArrVh = MethodHandles.arrayElementVarHandle(String[].class);
    String[] arr = new String[] { "zero", "one", "two" };

    VarHandle bound = MethodHandles.insertCoordinates(strArrVh, 0, (Object) arr);
    if (bound.coordinateTypes().size() != 1 || bound.coordinateTypes().get(0) != int.class) {
      throw new AssertionError("bound coords mismatch: " + bound.coordinateTypes());
    }

    MethodHandle getMh = bound.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setMh = bound.toMethodHandle(VarHandle.AccessMode.SET);

    if (!"one".equals(getMh.invokeWithArguments(1))) {
      throw new AssertionError("bound get mismatch");
    }
    setMh.invokeWithArguments(1, "updated_one");
    if (!"updated_one".equals(arr[1])) {
      throw new AssertionError("bound set mismatch: " + arr[1]);
    }
  }

  private static void testDropCoordinates() throws Throwable {
    VarHandle strArrVh = MethodHandles.arrayElementVarHandle(String[].class);
    String[] arr = new String[] { "alpha", "beta", "gamma" };
    VarHandle bound = MethodHandles.insertCoordinates(strArrVh, 0, (Object) arr);

    VarHandle dropped = MethodHandles.dropCoordinates(bound, 0, String.class);
    if (dropped.coordinateTypes().size() != 2
        || dropped.coordinateTypes().get(0) != String.class
        || dropped.coordinateTypes().get(1) != int.class) {
      throw new AssertionError("dropped coords mismatch: " + dropped.coordinateTypes());
    }

    MethodHandle getMh = dropped.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setMh = dropped.toMethodHandle(VarHandle.AccessMode.SET);

    if (!"beta".equals(getMh.invokeWithArguments("unusedPrefix", 1))) {
      throw new AssertionError("dropped get mismatch");
    }
    setMh.invokeWithArguments("unusedPrefix", 1, "new_beta");
    if (!"new_beta".equals(arr[1])) {
      throw new AssertionError("dropped set mismatch");
    }
  }

  private static void testFilterCoordinates() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    VarHandle strArrVh = MethodHandles.arrayElementVarHandle(String[].class);

    MethodHandle offsetMh = lookup.findStatic(
        VarHandleCombinatorsModern.class,
        "offsetByTen",
        MethodType.methodType(int.class, int.class));

    VarHandle filtered = MethodHandles.filterCoordinates(strArrVh, 1, offsetMh);
    String[] bigArr = new String[20];

    MethodHandle setMh = filtered.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle getMh = filtered.toMethodHandle(VarHandle.AccessMode.GET);

    setMh.invokeWithArguments(bigArr, 2, "offsetTwelve");
    if (!"offsetTwelve".equals(bigArr[12])) {
      throw new AssertionError("filterCoordinates set target mismatch: " + bigArr[12]);
    }
    if (!"offsetTwelve".equals(getMh.invokeWithArguments(bigArr, 2))) {
      throw new AssertionError("filterCoordinates get mismatch");
    }
  }

  private static void testPermuteCoordinates() throws Throwable {
    VarHandle strArrVh = MethodHandles.arrayElementVarHandle(String[].class);
    VarHandle perm = MethodHandles.permuteCoordinates(
        strArrVh,
        List.of(int.class, String[].class),
        1, 0);

    if (perm.coordinateTypes().get(0) != int.class || perm.coordinateTypes().get(1) != String[].class) {
      throw new AssertionError("permute coords mismatch: " + perm.coordinateTypes());
    }

    String[] arr = new String[5];
    MethodHandle setMh = perm.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle getMh = perm.toMethodHandle(VarHandle.AccessMode.GET);

    setMh.invokeWithArguments(3, arr, "permutedThree");
    if (!"permutedThree".equals(arr[3])) {
      throw new AssertionError("permute set mismatch");
    }
    if (!"permutedThree".equals(getMh.invokeWithArguments(3, arr))) {
      throw new AssertionError("permute get mismatch");
    }
  }

  private static void testCollectCoordinates() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    VarHandle strArrVh = MethodHandles.arrayElementVarHandle(String[].class);

    MethodHandle sumMh = lookup.findStatic(
        VarHandleCombinatorsModern.class,
        "sum",
        MethodType.methodType(int.class, int.class, int.class));

    VarHandle coll = MethodHandles.collectCoordinates(strArrVh, 1, sumMh);
    if (coll.coordinateTypes().size() != 3
        || coll.coordinateTypes().get(0) != String[].class
        || coll.coordinateTypes().get(1) != int.class
        || coll.coordinateTypes().get(2) != int.class) {
      throw new AssertionError("collect coords mismatch: " + coll.coordinateTypes());
    }

    String[] arr = new String[10];
    MethodHandle setMh = coll.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle getMh = coll.toMethodHandle(VarHandle.AccessMode.GET);

    setMh.invokeWithArguments(arr, 3, 4, "sumSeven");
    if (!"sumSeven".equals(arr[7])) {
      throw new AssertionError("collect set mismatch: " + arr[7]);
    }
    if (!"sumSeven".equals(getMh.invokeWithArguments(arr, 3, 4))) {
      throw new AssertionError("collect get mismatch");
    }
  }

  private static void testValidationErrors() {
    VarHandle strArrVh = MethodHandles.arrayElementVarHandle(String[].class);

    boolean caught = false;
    try {
      MethodHandles.filterCoordinates(strArrVh, 2);
    } catch (IllegalArgumentException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected exception for invalid pos in filterCoordinates");
    }

    caught = false;
    try {
      MethodHandles.insertCoordinates(strArrVh, 3, "val");
    } catch (IllegalArgumentException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected exception for invalid pos in insertCoordinates");
    }

    caught = false;
    try {
      MethodHandles.permuteCoordinates(strArrVh, List.of(int.class), 0);
    } catch (IllegalArgumentException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected exception for mismatched reorder count in permuteCoordinates");
    }
  }
}
