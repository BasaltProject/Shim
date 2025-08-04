/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.lang.reflect.Method;

public abstract class Enum<E extends Enum<E>> implements Comparable<E>, java.lang.constant.Constable {
  private final String name;
  protected final int ordinal;

  public Enum(String name, int ordinal) {
    this.name = name;
    this.ordinal = ordinal;
  }

  public int compareTo(E other) {
    if (getDeclaringClass() != other.getDeclaringClass()) {
      throw new ClassCastException();
    }

    return ordinal - other.ordinal;
  }

  public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
    if (name == null) throw new NullPointerException("name");
    if (!enumType.isEnum())
      throw new IllegalArgumentException(enumType.getCanonicalName() + " is not an enum.");

    try {
      Method method = enumType.getMethod("values");
      Enum values[] = (Enum[]) method.invoke(null);
      for (Enum value: values) {
        if (name.equals(value.name)) {
          return (T) value;
        }
      }
    } catch (Exception ex) {
      // Cannot happen
      throw new Error(ex);
    }

    throw new IllegalArgumentException(enumType.getCanonicalName() + "." + name + " is not an enum constant.");
  }

  public int ordinal() {
    return ordinal;
  }

  public final String name() {
    return name;
  }

  public String toString() {
    return name;
  }

  public Class<E> getDeclaringClass() {
    Class c = getClass();
    while (c.getSuperclass() != Enum.class) {
      c = c.getSuperclass();
    }
    return c;
  }

  @Override
  public final java.util.Optional<EnumDesc<E>> describeConstable() {
    return getDeclaringClass().describeConstable()
        .map(c -> EnumDesc.of(c, name));
  }

  public static final class EnumDesc<E extends Enum<E>> extends java.lang.constant.DynamicConstantDesc<E> {
    private final java.lang.constant.ClassDesc enumDesc;
    private final String constantName;

    private EnumDesc(java.lang.constant.ClassDesc enumDesc, String constantName) {
      super(java.lang.constant.ConstantDescs.BSM_ENUM_CONSTANT,
            constantName,
            enumDesc,
            new java.lang.constant.ConstantDesc[0]);
      this.enumDesc = enumDesc;
      this.constantName = constantName;
    }

    public static <E extends Enum<E>> EnumDesc<E> of(java.lang.constant.ClassDesc enumClass, String constantName) {
      java.util.Objects.requireNonNull(enumClass, "enumClass");
      java.util.Objects.requireNonNull(constantName, "constantName");
      return new EnumDesc<E>(enumClass, constantName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E resolveConstantDesc(java.lang.invoke.MethodHandles.Lookup lookup) throws ReflectiveOperationException {
      return (E) Enum.valueOf((Class) enumDesc.resolveConstantDesc(lookup), constantName);
    }

    @Override
    public String toString() {
      return "EnumDesc[" + enumDesc.displayName() + "." + constantName + "]";
    }
  }
}
