/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.beans;

import java.io.Serializable;
import java.util.Objects;

public final class StaticClass implements Serializable {
  private static final ClassValue<StaticClass> staticClasses = new ClassValue<StaticClass>() {
    @Override
    protected StaticClass computeValue(Class<?> type) {
      return new StaticClass(type);
    }
  };

  private static final long serialVersionUID = 1L;

  private final Class<?> clazz;

  StaticClass(Class<?> clazz) {
    this.clazz = Objects.requireNonNull(clazz);
  }

  public static StaticClass forClass(Class<?> clazz) {
    return staticClasses.get(clazz);
  }

  public Class<?> getRepresentedClass() {
    return clazz;
  }

  @Override
  public String toString() {
    return "StaticClass[" + clazz.getName() + "]";
  }

  private Object readResolve() {
    return forClass(clazz);
  }
}
