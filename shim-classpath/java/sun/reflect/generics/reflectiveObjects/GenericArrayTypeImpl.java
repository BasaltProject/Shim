/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public final class GenericArrayTypeImpl implements GenericArrayType {
  private final Type component;

  public GenericArrayTypeImpl(Type component) {
    this.component = component;
  }

  @Override
  public Type getGenericComponentType() {
    return component;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof GenericArrayType) {
      return Objects.equals(component, ((GenericArrayType) o).getGenericComponentType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(component);
  }

  @Override
  public String toString() {
    return ParameterizedTypeImpl.typeName(component) + "[]";
  }
}
