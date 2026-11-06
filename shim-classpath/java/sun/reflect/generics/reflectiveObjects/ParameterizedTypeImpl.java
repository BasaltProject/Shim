/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public final class ParameterizedTypeImpl implements ParameterizedType {
  private final Type[] args;
  private final Type owner;
  private final Type raw;

  public ParameterizedTypeImpl(Type[] args, Type owner, Type raw) {
    this.args = args;
    this.owner = owner;
    this.raw = raw;
  }

  public static String typeName(Type type) {
    if (type == null) {
      return "null";
    }
    if (type instanceof Class) {
      return ((Class<?>) type).getTypeName();
    }
    return type.toString();
  }

  @Override
  public Type getRawType() {
    return raw;
  }

  @Override
  public Type getOwnerType() {
    return owner;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return args;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ParameterizedType) {
      ParameterizedType that = (ParameterizedType) o;
      if (this == that) {
        return true;
      }
      return Objects.equals(owner, that.getOwnerType()) && Objects.equals(raw, that.getRawType())
          && Arrays.equals(args, that.getActualTypeArguments());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(args) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(typeName(raw));
    builder.append('<');
    String sep = "";
    for (Type t : args) {
      builder.append(sep).append(typeName(t));
      sep = ", ";
    }
    builder.append('>');
    return builder.toString();
  }
}
