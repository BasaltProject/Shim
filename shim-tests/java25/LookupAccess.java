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
import p.Subject;

public class LookupAccess {
  interface Action { Object run() throws Throwable; }

  private int own() { return 21; }
  private static int ownStatic() { return 22; }

  static void probe(String label, Action action) {
    try {
      System.out.println(label + ":" + action.run());
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName());
    }
  }

  static final MethodType INT = MethodType.methodType(int.class);

  public static void main(String[] args) {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    probe("public-method", () -> lookup.findVirtual(Subject.class, "publicMethod", INT).invokeWithArguments(new Subject()));
    probe("private-method", () -> lookup.findVirtual(Subject.class, "privateMethod", INT));
    probe("package-method", () -> lookup.findVirtual(Subject.class, "packageMethod", INT));
    probe("protected-method", () -> lookup.findVirtual(Subject.class, "protectedMethod", INT));
    probe("public-static", () -> lookup.findStatic(Subject.class, "publicStatic", INT).invokeWithArguments());
    probe("private-static", () -> lookup.findStatic(Subject.class, "privateStatic", INT));
    probe("missing-method", () -> lookup.findVirtual(Subject.class, "absent", INT));
    probe("public-getter", () -> lookup.findGetter(Subject.class, "publicField", int.class).invokeWithArguments(new Subject()));
    probe("private-getter", () -> lookup.findGetter(Subject.class, "privateField", int.class));
    probe("package-getter", () -> lookup.findGetter(Subject.class, "packageField", int.class));
    probe("protected-setter", () -> lookup.findSetter(Subject.class, "protectedField", int.class));
    probe("private-varhandle", () -> lookup.findVarHandle(Subject.class, "privateField", int.class));
    probe("public-constructor", () -> lookup.findConstructor(Subject.class, MethodType.methodType(void.class)).invokeWithArguments() != null);
    probe("package-constructor", () -> lookup.findConstructor(Subject.class, MethodType.methodType(void.class, int.class)));
    probe("own-private", () -> lookup.findVirtual(LookupAccess.class, "own", INT).invokeWithArguments(new LookupAccess()));
    probe("own-private-static", () -> lookup.findStatic(LookupAccess.class, "ownStatic", INT).invokeWithArguments());
    probe("public-lookup-own", () -> MethodHandles.publicLookup().findStatic(LookupAccess.class, "ownStatic", INT));
    probe("public-lookup-public", () -> MethodHandles.publicLookup().findStatic(Subject.class, "publicStatic", INT).invokeWithArguments());
    probe("lookup-in-package", () -> lookup.in(Subject.class).findVirtual(Subject.class, "packageMethod", INT));
    probe("lookup-in-public", () -> lookup.in(Subject.class).findVirtual(Subject.class, "publicMethod", INT).invokeWithArguments(new Subject()));
  }
}
