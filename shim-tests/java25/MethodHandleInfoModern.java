// Minimum release: 21
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class MethodHandleInfoModern {
  public int instanceField = 100;
  public static double staticField = 3.14159;

  public MethodHandleInfoModern() {}

  public MethodHandleInfoModern(String tag) {}

  public static String staticTarget(int count) {
    return "static:" + count;
  }

  public void virtualTarget(long val) {}

  public static void varargsTarget(String prefix, Object... items) {}

  public String bindGreet(String name) {
    return "Hello, " + name;
  }

  interface HelperInterface {
    String interfaceMethod();
  }

  static boolean initTriggered = false;

  static class InitializedTarget {
    static {
      initTriggered = true;
    }
    public static void ping() {}
  }

  public static void main(String[] args) throws Throwable {
    testDirectMethodHandles();
    testFieldMethodHandles();
    testVarArgs();
    testReferenceKindToString();
    testReflectAsValidation();
    testNonDirectRejection();
    testLookupModernMethods();
  }

  private static void testDirectMethodHandles() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    // 1. static method
    MethodHandle smh = lookup.findStatic(MethodHandleInfoModern.class, "staticTarget",
        MethodType.methodType(String.class, int.class));
    MethodHandleInfo sinfo = lookup.revealDirect(smh);
    expectEquals(MethodHandleInfo.REF_invokeStatic, sinfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, sinfo.getDeclaringClass());
    expectEquals("staticTarget", sinfo.getName());
    expectEquals(MethodType.methodType(String.class, int.class), sinfo.getMethodType());
    Method sm = sinfo.reflectAs(Method.class, lookup);
    expectEquals("staticTarget", sm.getName());
    expectEquals("invokeStatic MethodHandleInfoModern.staticTarget:(int)String", sinfo.toString());

    // 2. virtual method
    MethodHandle vmh = lookup.findVirtual(MethodHandleInfoModern.class, "virtualTarget",
        MethodType.methodType(void.class, long.class));
    MethodHandleInfo vinfo = lookup.revealDirect(vmh);
    expectEquals(MethodHandleInfo.REF_invokeVirtual, vinfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, vinfo.getDeclaringClass());
    expectEquals("virtualTarget", vinfo.getName());
    expectEquals(MethodType.methodType(void.class, long.class), vinfo.getMethodType());
    Method vm = vinfo.reflectAs(Method.class, lookup);
    expectEquals("virtualTarget", vm.getName());
    expectEquals("invokeVirtual MethodHandleInfoModern.virtualTarget:(long)void", vinfo.toString());

    // 3. constructor
    MethodHandle cmh = lookup.findConstructor(MethodHandleInfoModern.class,
        MethodType.methodType(void.class, String.class));
    MethodHandleInfo cinfo = lookup.revealDirect(cmh);
    expectEquals(MethodHandleInfo.REF_newInvokeSpecial, cinfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, cinfo.getDeclaringClass());
    expectEquals("<init>", cinfo.getName());
    expectEquals(MethodType.methodType(void.class, String.class), cinfo.getMethodType());
    Constructor<?> cm = cinfo.reflectAs(Constructor.class, lookup);
    expectEquals(MethodHandleInfoModern.class.getName(), cm.getName());
    expectEquals("newInvokeSpecial MethodHandleInfoModern.<init>:(String)void", cinfo.toString());

    // 4. interface method
    MethodHandle imh = lookup.findVirtual(HelperInterface.class, "interfaceMethod",
        MethodType.methodType(String.class));
    MethodHandleInfo iinfo = lookup.revealDirect(imh);
    expectEquals(MethodHandleInfo.REF_invokeInterface, iinfo.getReferenceKind());
    expectEquals(HelperInterface.class, iinfo.getDeclaringClass());
    expectEquals("interfaceMethod", iinfo.getName());
    expectEquals(MethodType.methodType(String.class), iinfo.getMethodType());

    System.out.println("PASS: direct method handles");
  }

  private static void testFieldMethodHandles() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    // getter instance
    MethodHandle fgh = lookup.findGetter(MethodHandleInfoModern.class, "instanceField", int.class);
    MethodHandleInfo fginfo = lookup.revealDirect(fgh);
    expectEquals(MethodHandleInfo.REF_getField, fginfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, fginfo.getDeclaringClass());
    expectEquals("instanceField", fginfo.getName());
    expectEquals(MethodType.methodType(int.class), fginfo.getMethodType());
    Field fgField = fginfo.reflectAs(Field.class, lookup);
    expectEquals("instanceField", fgField.getName());

    // setter instance
    MethodHandle fsh = lookup.findSetter(MethodHandleInfoModern.class, "instanceField", int.class);
    MethodHandleInfo fsinfo = lookup.revealDirect(fsh);
    expectEquals(MethodHandleInfo.REF_putField, fsinfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, fsinfo.getDeclaringClass());
    expectEquals("instanceField", fsinfo.getName());
    expectEquals(MethodType.methodType(void.class, int.class), fsinfo.getMethodType());

    // getter static
    MethodHandle sfgh = lookup.findStaticGetter(MethodHandleInfoModern.class, "staticField", double.class);
    MethodHandleInfo sfginfo = lookup.revealDirect(sfgh);
    expectEquals(MethodHandleInfo.REF_getStatic, sfginfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, sfginfo.getDeclaringClass());
    expectEquals("staticField", sfginfo.getName());
    expectEquals(MethodType.methodType(double.class), sfginfo.getMethodType());

    // setter static
    MethodHandle sfsh = lookup.findStaticSetter(MethodHandleInfoModern.class, "staticField", double.class);
    MethodHandleInfo sfsinfo = lookup.revealDirect(sfsh);
    expectEquals(MethodHandleInfo.REF_putStatic, sfsinfo.getReferenceKind());
    expectEquals(MethodHandleInfoModern.class, sfsinfo.getDeclaringClass());
    expectEquals("staticField", sfsinfo.getName());
    expectEquals(MethodType.methodType(void.class, double.class), sfsinfo.getMethodType());

    System.out.println("PASS: field method handles");
  }

  private static void testVarArgs() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    MethodHandle vmh = lookup.findStatic(MethodHandleInfoModern.class, "varargsTarget",
        MethodType.methodType(void.class, String.class, Object[].class));
    MethodHandleInfo vinfo = lookup.revealDirect(vmh);
    expectTrue(vinfo.isVarArgs());

    MethodHandle smh = lookup.findStatic(MethodHandleInfoModern.class, "staticTarget",
        MethodType.methodType(String.class, int.class));
    MethodHandleInfo sinfo = lookup.revealDirect(smh);
    expectTrue(!sinfo.isVarArgs());

    MethodHandle fgh = lookup.findGetter(MethodHandleInfoModern.class, "instanceField", int.class);
    MethodHandleInfo fginfo = lookup.revealDirect(fgh);
    expectTrue(!fginfo.isVarArgs());

    System.out.println("PASS: varargs");
  }

  private static void testReferenceKindToString() {
    expectEquals("getField", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_getField));
    expectEquals("getStatic", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_getStatic));
    expectEquals("putField", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_putField));
    expectEquals("putStatic", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_putStatic));
    expectEquals("invokeVirtual", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_invokeVirtual));
    expectEquals("invokeStatic", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_invokeStatic));
    expectEquals("invokeSpecial", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_invokeSpecial));
    expectEquals("newInvokeSpecial", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_newInvokeSpecial));
    expectEquals("invokeInterface", MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_invokeInterface));

    try {
      MethodHandleInfo.referenceKindToString(0);
      throw new AssertionError("Expected IllegalArgumentException for kind 0");
    } catch (IllegalArgumentException expected) {
      expectTrue(expected.getMessage().contains("invalid reference kind"));
    }

    try {
      MethodHandleInfo.referenceKindToString(10);
      throw new AssertionError("Expected IllegalArgumentException for kind 10");
    } catch (IllegalArgumentException expected) {
      expectTrue(expected.getMessage().contains("invalid reference kind"));
    }

    System.out.println("PASS: referenceKindToString");
  }

  private static void testReflectAsValidation() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    MethodHandle smh = lookup.findStatic(MethodHandleInfoModern.class, "staticTarget",
        MethodType.methodType(String.class, int.class));
    MethodHandleInfo sinfo = lookup.revealDirect(smh);

    // Reflecting Method as Member succeeds
    Member mem = sinfo.reflectAs(Member.class, lookup);
    expectEquals("staticTarget", mem.getName());

    // Reflecting Method as Field throws ClassCastException
    try {
      sinfo.reflectAs(Field.class, lookup);
      throw new AssertionError("Expected ClassCastException");
    } catch (ClassCastException expected) {
      expectTrue(expected.getMessage().contains("Cannot cast"));
    }

    // Reflecting Field as Method throws ClassCastException
    MethodHandle fgh = lookup.findGetter(MethodHandleInfoModern.class, "instanceField", int.class);
    MethodHandleInfo fginfo = lookup.revealDirect(fgh);
    try {
      fginfo.reflectAs(Method.class, lookup);
      throw new AssertionError("Expected ClassCastException");
    } catch (ClassCastException expected) {
      expectTrue(expected.getMessage().contains("Cannot cast"));
    }

    System.out.println("PASS: reflectAs validation");
  }

  private static void testNonDirectRejection() {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    try {
      lookup.revealDirect(MethodHandles.constant(String.class, "literal"));
      throw new AssertionError("Expected IllegalArgumentException for constant handle");
    } catch (IllegalArgumentException expected) {
      expectTrue(expected.getMessage().contains("not a direct method handle"));
    }

    try {
      lookup.revealDirect(MethodHandles.identity(int.class));
      throw new AssertionError("Expected IllegalArgumentException for identity handle");
    } catch (IllegalArgumentException expected) {
      expectTrue(expected.getMessage().contains("not a direct method handle"));
    }

    System.out.println("PASS: non-direct rejection");
  }

  private static void testLookupModernMethods() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    expectTrue(lookup.hasPrivateAccess());
    expectTrue(lookup.hasFullPrivilegeAccess());

    MethodHandles.Lookup dropped = lookup.dropLookupMode(MethodHandles.Lookup.PRIVATE);
    expectTrue(!dropped.hasPrivateAccess());

    Class<?> accessCls = lookup.accessClass(MethodHandleInfoModern.class);
    expectEquals(MethodHandleInfoModern.class, accessCls);

    Class<?> primCls = lookup.accessClass(int.class);
    expectEquals(int.class, primCls);

    expectTrue(!initTriggered);
    Class<?> initCls = lookup.ensureInitialized(InitializedTarget.class);
    expectEquals(InitializedTarget.class, initCls);
    expectTrue(initTriggered);

    MethodHandle bound = lookup.bind(new MethodHandleInfoModern(), "bindGreet",
        MethodType.methodType(String.class, String.class));
    Object result = bound.invokeWithArguments("Shim");
    expectEquals("Hello, Shim", result);

    System.out.println("PASS: lookup modern methods");
  }

  private static void expectEquals(Object expected, Object actual) {
    if (expected == null && actual == null) return;
    if (expected != null && expected.equals(actual)) return;
    throw new AssertionError("Expected " + expected + " but got " + actual);
  }

  private static void expectTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Condition failed");
    }
  }
}
