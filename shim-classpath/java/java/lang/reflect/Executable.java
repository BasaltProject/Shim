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

  String sharedToGenericString(int modifierMask, boolean isDefault) {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & modifierMask;
    if (mod != 0 && !isDefault) {
      sb.append(Modifier.toString(mod)).append(' ');
    } else {
      int access = mod & Modifier.PUBLIC | mod & Modifier.PROTECTED | mod & Modifier.PRIVATE;
      if (access != 0) {
        sb.append(Modifier.toString(access)).append(' ');
      }
      if (isDefault) {
        sb.append("default ");
      }
      mod &= ~access;
      if (mod != 0) {
        sb.append(Modifier.toString(mod)).append(' ');
      }
    }
    TypeVariable<?>[] typeParams = getTypeParameters();
    if (typeParams.length > 0) {
      sb.append('<');
      for (int i = 0; i < typeParams.length; i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(typeParams[i].getName());
        Type[] bounds = typeParams[i].getBounds();
        if (!(bounds.length == 1 && bounds[0] == Object.class)) {
          sb.append(" extends ");
          for (int j = 0; j < bounds.length; j++) {
            if (j > 0) {
              sb.append(" & ");
            }
            sb.append(bounds[j].getTypeName());
          }
        }
      }
      sb.append("> ");
    }
    if (this instanceof Method) {
      sb.append(((Method) this).getGenericReturnType().getTypeName()).append(' ');
      sb.append(getDeclaringClass().getTypeName()).append('.');
    } else {
      sb.append(getDeclaringClass().getTypeName());
    }
    if (this instanceof Method) {
      sb.append(getName());
    }
    sb.append('(');
    Type[] params = getGenericParameterTypes();
    for (int i = 0; i < params.length; i++) {
      String name = params[i].getTypeName();
      if (i == params.length - 1 && isVarArgs() && name.endsWith("[]")) {
        name = name.substring(0, name.length() - 2) + "...";
      }
      if (i > 0) {
        sb.append(',');
      }
      sb.append(name);
    }
    sb.append(')');
    Type[] exceptions = getGenericExceptionTypes();
    if (exceptions.length > 0) {
      sb.append(" throws ");
      for (int i = 0; i < exceptions.length; i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(exceptions[i].getTypeName());
      }
    }
    return sb.toString();
  }

  public boolean isVarArgs() {
    return (getModifiers() & ACC_VARARGS) != 0;
  }

  public boolean isSynthetic() {
    return (getModifiers() & ACC_SYNTHETIC) != 0;
  }

  @Override
  public java.util.Set<AccessFlag> accessFlags() {
    return AccessFlag.maskToAccessFlags(getModifiers(), AccessFlag.Location.METHOD);
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
    return AnnotatedTypeImpl.create(Void.TYPE);
  }

  public AnnotatedType[] getAnnotatedParameterTypes() {
    Type[] types = getGenericParameterTypes();
    AnnotatedType[] res = new AnnotatedType[types.length];
    for (int i = 0; i < types.length; i++) {
      res[i] = AnnotatedTypeImpl.create(types[i]);
    }
    return res;
  }

  public AnnotatedType[] getAnnotatedExceptionTypes() {
    Type[] types = getGenericExceptionTypes();
    AnnotatedType[] res = new AnnotatedType[types.length];
    for (int i = 0; i < types.length; i++) {
      res[i] = AnnotatedTypeImpl.create(types[i]);
    }
    return res;
  }
}
