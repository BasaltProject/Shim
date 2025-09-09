/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.lang.reflect.Member;
import java.util.Objects;

public interface MethodHandleInfo {
  public static final int REF_getField = 1;
  public static final int REF_getStatic = 2;
  public static final int REF_putField = 3;
  public static final int REF_putStatic = 4;
  public static final int REF_invokeVirtual = 5;
  public static final int REF_invokeStatic = 6;
  public static final int REF_invokeSpecial = 7;
  public static final int REF_newInvokeSpecial = 8;
  public static final int REF_invokeInterface = 9;

  public int getReferenceKind();

  public Class<?> getDeclaringClass();

  public String getName();

  public MethodType getMethodType();

  public <T extends Member> T reflectAs(Class<T> expected, MethodHandles.Lookup lookup);

  public int getModifiers();

  public default boolean isVarArgs() {
    int kind = getReferenceKind();
    switch (kind) {
    case REF_getField:
    case REF_getStatic:
    case REF_putField:
    case REF_putStatic:
      return false;
    default:
      return (getModifiers() & 0x0080) != 0;
    }
  }

  public static String referenceKindToString(int referenceKind) {
    switch (referenceKind) {
    case REF_getField:
      return "getField";
    case REF_getStatic:
      return "getStatic";
    case REF_putField:
      return "putField";
    case REF_putStatic:
      return "putStatic";
    case REF_invokeVirtual:
      return "invokeVirtual";
    case REF_invokeStatic:
      return "invokeStatic";
    case REF_invokeSpecial:
      return "invokeSpecial";
    case REF_newInvokeSpecial:
      return "newInvokeSpecial";
    case REF_invokeInterface:
      return "invokeInterface";
    default:
      throw new IllegalArgumentException("invalid reference kind: " + referenceKind);
    }
  }

  public static String toString(int kind, Class<?> defc, String name, MethodType type) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    return referenceKindToString(kind) + " " + defc.getName() + "." + name + ":" + type;
  }
}
