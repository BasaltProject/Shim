/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class HiddenClassAndClassDataModern {

  public static class DummyHiddenPayload {
    public static String greet(String name) {
      return "hello " + name;
    }
  }

  public static void main(String[] args) throws Throwable {
    testClassOptionEnum();
    testClassIsHiddenOnNormalClasses();
    testClassDataOnNormalClass();
    testPublicLookupPrivilegeRejection();
    testDefineHiddenClassWithClassData();
    testValidationExceptions();
    System.out.println("HiddenClassAndClassDataModern passed successfully");
  }

  private static void testClassOptionEnum() {
    MethodHandles.Lookup.ClassOption nestmate = MethodHandles.Lookup.ClassOption.NESTMATE;
    MethodHandles.Lookup.ClassOption strong = MethodHandles.Lookup.ClassOption.STRONG;
    if (nestmate.ordinal() != 0 || strong.ordinal() != 1) {
      throw new AssertionError("unexpected ClassOption ordinals");
    }
  }

  private static void testClassIsHiddenOnNormalClasses() {
    if (HiddenClassAndClassDataModern.class.isHidden()) {
      throw new AssertionError("normal class must not be hidden");
    }
    if (String.class.isHidden()) {
      throw new AssertionError("String class must not be hidden");
    }
  }

  private static void testClassDataOnNormalClass() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    Object data = MethodHandles.classData(lookup, "_", Object.class);
    if (data != null) {
      throw new AssertionError("expected null classData for normal class, got: " + data);
    }

    Object elem = MethodHandles.classDataAt(lookup, "_", Object.class, 0);
    if (elem != null) {
      throw new AssertionError("expected null classDataAt for normal class, got: " + elem);
    }
  }

  private static void testPublicLookupPrivilegeRejection() {
    MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

    boolean caught = false;
    try {
      MethodHandles.classData(publicLookup, "_", Object.class);
    } catch (IllegalAccessException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected IllegalAccessException for classData with publicLookup");
    }

    caught = false;
    try {
      MethodHandles.classDataAt(publicLookup, "_", Object.class, 0);
    } catch (IllegalAccessException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected IllegalAccessException for classDataAt with publicLookup");
    }
  }

  private static void testDefineHiddenClassWithClassData() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    InputStream in = HiddenClassAndClassDataModern.class.getResourceAsStream(
        "HiddenClassAndClassDataModern$DummyHiddenPayload.class");
    if (in == null) {
      throw new AssertionError("could not load resource for DummyHiddenPayload class");
    }
    byte[] classBytes = in.readAllBytes();

    List<String> payloadList = List.of("alpha", "beta", "gamma");
    MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClassWithClassData(
        classBytes,
        payloadList,
        true,
        MethodHandles.Lookup.ClassOption.NESTMATE);

    Class<?> hiddenClass = hiddenLookup.lookupClass();
    if (!hiddenClass.isHidden()) {
      throw new AssertionError("defined class must report isHidden = true");
    }

    Object classDataObj = MethodHandles.classData(hiddenLookup, "_", Object.class);
    if (!payloadList.equals(classDataObj)) {
      throw new AssertionError("classData mismatch: " + classDataObj);
    }

    String item0 = (String) MethodHandles.classDataAt(hiddenLookup, "_", String.class, 0);
    String item1 = (String) MethodHandles.classDataAt(hiddenLookup, "_", String.class, 1);
    String item2 = (String) MethodHandles.classDataAt(hiddenLookup, "_", String.class, 2);

    if (!"alpha".equals(item0) || !"beta".equals(item1) || !"gamma".equals(item2)) {
      throw new AssertionError("classDataAt elements mismatch: " + item0 + ", " + item1 + ", " + item2);
    }

    MethodHandle greetMh = hiddenLookup.findStatic(
        hiddenClass, "greet", MethodType.methodType(String.class, String.class));
    String greeting = (String) greetMh.invokeWithArguments("world");
    if (!"hello world".equals(greeting)) {
      throw new AssertionError("hidden class method execution failed: " + greeting);
    }
  }

  private static void testValidationExceptions() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    boolean caught = false;
    try {
      MethodHandles.classData(null, "_", Object.class);
    } catch (NullPointerException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected NPE for null lookup in classData");
    }

    caught = false;
    try {
      MethodHandles.classDataAt(null, "_", Object.class, 0);
    } catch (NullPointerException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected NPE for null lookup in classDataAt");
    }

    caught = false;
    try {
      lookup.defineHiddenClass(null, true);
    } catch (NullPointerException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("expected NPE for null bytes in defineHiddenClass");
    }
  }
}
