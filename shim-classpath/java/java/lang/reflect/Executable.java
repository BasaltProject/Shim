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

public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration {
  Executable() {}

  public abstract Class<?> getDeclaringClass();

  public abstract String getName();

  public abstract int getModifiers();

  public abstract Class<?>[] getParameterTypes();

  public abstract Class<?>[] getExceptionTypes();

  public int getParameterCount() {
    return getParameterTypes().length;
  }

  public Parameter[] getParameters() {
    Class<?>[] types = getParameterTypes();
    Parameter[] params = new Parameter[types.length];
    for (int i = 0; i < types.length; i++) {
      params[i] = new Parameter("arg" + i, 0, this, i);
    }
    return params;
  }

  public abstract Annotation[][] getParameterAnnotations();

  public abstract String toGenericString();

  public boolean isVarArgs() {
    return (getModifiers() & ACC_VARARGS) != 0;
  }

  public boolean isSynthetic() {
    return (getModifiers() & ACC_SYNTHETIC) != 0;
  }

  public TypeVariable<?>[] getTypeParameters() {
    return new TypeVariable<?>[0];
  }

  public Type[] getGenericParameterTypes() {
    return getParameterTypes();
  }

  public Type[] getGenericExceptionTypes() {
    return getExceptionTypes();
  }

  public AnnotatedType getAnnotatedReceiverType() {
    return null;
  }

  public AnnotatedType getAnnotatedReturnType() {
    return new AnnotatedTypeImpl(Void.TYPE);
  }

  public AnnotatedType[] getAnnotatedParameterTypes() {
    Type[] types = getGenericParameterTypes();
    AnnotatedType[] res = new AnnotatedType[types.length];
    for (int i = 0; i < types.length; i++) {
      res[i] = new AnnotatedTypeImpl(types[i]);
    }
    return res;
  }

  public AnnotatedType[] getAnnotatedExceptionTypes() {
    Type[] types = getGenericExceptionTypes();
    AnnotatedType[] res = new AnnotatedType[types.length];
    for (int i = 0; i < types.length; i++) {
      res[i] = new AnnotatedTypeImpl(types[i]);
    }
    return res;
  }
}
