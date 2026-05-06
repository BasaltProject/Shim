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

class AnnotatedTypeImpl implements AnnotatedType {
  private final Type type;

  AnnotatedTypeImpl(Type type) {
    this.type = Objects.requireNonNull(type);
  }

  static AnnotatedType create(Type type) {
    if (type instanceof ParameterizedType) {
      return new Parameterized((ParameterizedType) type);
    } else if (type instanceof GenericArrayType) {
      return new Array((GenericArrayType) type);
    } else if (type instanceof TypeVariable) {
      return new Variable((TypeVariable<?>) type);
    } else if (type instanceof WildcardType) {
      return new Wildcard((WildcardType) type);
    }
    return new AnnotatedTypeImpl(type);
  }

  static AnnotatedType[] createAll(Type[] types) {
    AnnotatedType[] result = new AnnotatedType[types.length];
    for (int i = 0; i < types.length; i++) {
      result[i] = create(types[i]);
    }
    return result;
  }

  private static final class Parameterized extends AnnotatedTypeImpl implements AnnotatedParameterizedType {
    Parameterized(ParameterizedType type) {
      super(type);
    }

    @Override
    public AnnotatedType[] getAnnotatedActualTypeArguments() {
      return createAll(((ParameterizedType) getType()).getActualTypeArguments());
    }
  }

  private static final class Array extends AnnotatedTypeImpl implements AnnotatedArrayType {
    Array(GenericArrayType type) {
      super(type);
    }

    @Override
    public AnnotatedType getAnnotatedGenericComponentType() {
      return create(((GenericArrayType) getType()).getGenericComponentType());
    }
  }

  private static final class Variable extends AnnotatedTypeImpl implements AnnotatedTypeVariable {
    Variable(TypeVariable<?> type) {
      super(type);
    }

    @Override
    public AnnotatedType[] getAnnotatedBounds() {
      return createAll(((TypeVariable<?>) getType()).getBounds());
    }
  }

  private static final class Wildcard extends AnnotatedTypeImpl implements AnnotatedWildcardType {
    Wildcard(WildcardType type) {
      super(type);
    }

    @Override
    public AnnotatedType[] getAnnotatedLowerBounds() {
      return createAll(((WildcardType) getType()).getLowerBounds());
    }

    @Override
    public AnnotatedType[] getAnnotatedUpperBounds() {
      return createAll(((WildcardType) getType()).getUpperBounds());
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return false;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return null;
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return new Annotation[0];
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
