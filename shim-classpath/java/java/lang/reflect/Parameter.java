/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Objects;

public final class Parameter implements AnnotatedElement {
  private final String name;
  private final int modifiers;
  private final Executable executable;
  private final int index;

  public Parameter(String name, int modifiers, Executable executable, int index) {
    this.name = Objects.requireNonNull(name);
    this.modifiers = modifiers;
    this.executable = Objects.requireNonNull(executable);
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public int getModifiers() {
    return modifiers;
  }

  public Executable getDeclaringExecutable() {
    return executable;
  }

  public Class<?> getType() {
    return executable.getParameterTypes()[index];
  }

  public Type getParameterizedType() {
    return getType();
  }

  public int getIndex() {
    return index;
  }

  public boolean isVarArgs() {
    return executable.isVarArgs() && index == executable.getParameterCount() - 1;
  }

  public boolean isImplicit() {
    return (modifiers & 0x1000) != 0;
  }

  public boolean isSynthetic() {
    return (modifiers & 0x1000) != 0;
  }

  public boolean isNamePresent() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Parameter) {
      Parameter other = (Parameter) obj;
      return other.executable.equals(executable) && other.index == index;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return executable.hashCode() ^ index;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String typeName = getType().getTypeName();
    sb.append(typeName);
    if (isVarArgs()) {
      if (sb.toString().endsWith("[]")) {
        sb.setLength(sb.length() - 2);
      }
      sb.append("...");
    }
    sb.append(" ");
    sb.append(getName());
    return sb.toString();
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return getAnnotation(annotationClass) != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    for (Annotation a : getAnnotations()) {
      if (annotationClass.isInstance(a)) {
        return (T) a;
      }
    }
    return null;
  }

  @Override
  public Annotation[] getAnnotations() {
    Annotation[][] all = executable.getParameterAnnotations();
    if (index < all.length && all[index] != null) {
      return all[index];
    }
    return new Annotation[0];
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }
}
