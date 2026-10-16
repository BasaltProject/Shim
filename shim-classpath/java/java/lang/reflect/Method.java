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

package java.lang.reflect;

import shim.VMMethod;
import shim.AnnotationInvocationHandler;
import shim.SystemClassLoader;
import shim.Classes;

import java.lang.annotation.Annotation;

public class Method extends Executable {
  public final VMMethod vmMethod;
  private boolean accessible;

  public Method(VMMethod vmMethod) {
    this.vmMethod = vmMethod;
  }

  public boolean equals(Object o) {
    return o instanceof Method && ((Method) o).vmMethod == vmMethod;
  }

  public boolean isAccessible() {
    return accessible;
  }

  public void setAccessible(boolean v) {
    accessible = v;
  }

  public static native VMMethod getCaller();

  public Class<?> getDeclaringClass() {
    return SystemClassLoader.getClass(vmMethod.class_);
  }

  public int getModifiers() {
    return vmMethod.flags;
  }

  public String getName() {
    return getName(vmMethod);
  }

  public static String getName(VMMethod vmMethod) {
    return Classes.makeString(vmMethod.name, 0, vmMethod.name.length - 1);
  }

  private String getSpec() {
    return getSpec(vmMethod);
  }
  
  public static String getSpec(VMMethod vmMethod) {
    return Classes.makeString(vmMethod.spec, 0, vmMethod.spec.length - 1);
  }

  public Class[] getParameterTypes() {
    return Classes.getParameterTypes(vmMethod);
  }

  public Object invoke(Object instance, Object ... arguments)
    throws InvocationTargetException, IllegalAccessException
  {
    if ((vmMethod.flags & Modifier.STATIC) != 0
        || Class.isInstance(vmMethod.class_, instance))
    {
      if ((vmMethod.flags & Modifier.STATIC) != 0) {
        instance = null;
      }

      if (arguments == null) {
        if (vmMethod.parameterCount > 0) {
          throw new NullPointerException();
        }
        arguments = new Object[0];
      }

      if (arguments.length == vmMethod.parameterCount) {
        Classes.initialize(vmMethod.class_);

        return invoke(vmMethod, instance, arguments);        
      } else {
        throw new ArrayIndexOutOfBoundsException();
      }
    } else {
//       System.out.println
//         (getDeclaringClass() + "." + getName() + " flags: " + vmMethod.flags + " vm flags: " + vmMethod.vmFlags + " return code: " + vmMethod.returnCode);
      throw new IllegalArgumentException();
    }
  }

  private static native Object invoke(VMMethod method, Object instance,
                                      Object ... arguments)
    throws InvocationTargetException, IllegalAccessException;

  public Object invokeNonVirtual(Object instance, Object ... arguments)
    throws InvocationTargetException, IllegalAccessException
  {
    if (instance == null) {
      throw new NullPointerException();
    }
    if (!Class.isInstance(vmMethod.class_, instance)) {
      throw new IllegalArgumentException();
    }
    if (arguments == null) {
      if (vmMethod.parameterCount > 0) {
        throw new NullPointerException();
      }
      arguments = new Object[0];
    }
    if (arguments.length == vmMethod.parameterCount) {
      Classes.initialize(vmMethod.class_);
      return invokeNonVirtual(vmMethod, instance, arguments);
    } else {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  private static native Object invokeNonVirtual(VMMethod method, Object instance,
                                                Object ... arguments)
    throws InvocationTargetException, IllegalAccessException;

  public Class getReturnType() {
    for (int i = 0; i < vmMethod.spec.length - 1; ++i) {
      if (vmMethod.spec[i] == ')') {
        return Classes.forCanonicalName
          (vmMethod.class_.loader,
           Classes.makeString
           (vmMethod.spec, i + 1, vmMethod.spec.length - i - 2));
      }
    }
    throw new RuntimeException();
  }

  public <T extends Annotation> T getAnnotation(Class<T> class_) {
    if (vmMethod.hasAnnotations()) {
      Object[] table = (Object[]) vmMethod.addendum.annotationTable;
      for (int i = 0; i < table.length; ++i) {
        Object[] a = (Object[]) table[i];
        if (a[1] == class_) {
          return (T) Classes.getAnnotation(vmMethod.class_.loader, a);
        }
      }
    }
    return null;
  }

  public Annotation[] getAnnotations() {
    if (vmMethod.hasAnnotations()) {
      Object[] table = (Object[]) vmMethod.addendum.annotationTable;
      Annotation[] array = new Annotation[table.length];
      for (int i = 0; i < table.length; ++i) {
        array[i] = Classes.getAnnotation
          (vmMethod.class_.loader, (Object[]) table[i]);
      }
      return array;
    } else {
      return new Annotation[0];
    }
  }

  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }

  public boolean isVarArgs() {
    return (getModifiers() & ACC_VARARGS) != 0;
  }

  public boolean isSynthetic() {
    return (getModifiers() & ACC_SYNTHETIC) != 0;
  }

  public boolean isDefault() {
    return ((getModifiers() & (Modifier.ABSTRACT | Modifier.STATIC | Modifier.PUBLIC)) == Modifier.PUBLIC)
        && getDeclaringClass().isInterface();
  }

  public Object getDefaultValue() {
    ClassLoader loader = getDeclaringClass().getClassLoader();
    return shim.AnnotationInvocationHandler.convert(getReturnType(),
        Classes.getAnnotationDefaultValue(loader, vmMethod.addendum));
  }

  public Annotation[][] getParameterAnnotations() {
    int count = getParameterCount();
    Classes.parseParameterAnnotations(vmMethod.class_.loader, vmMethod.addendum);
    if (vmMethod.addendum != null && vmMethod.addendum.parameterAnnotationTable != null) {
      Object[] table = (Object[]) vmMethod.addendum.parameterAnnotationTable;
      Annotation[][] result = new Annotation[count][];
      for (int i = 0; i < count; i++) {
        if (i < table.length && table[i] != null) {
          Object[] paramTable = (Object[]) table[i];
          result[i] = new Annotation[paramTable.length];
          for (int j = 0; j < paramTable.length; j++) {
            result[i][j] = Classes.getAnnotation(vmMethod.class_.loader, (Object[]) paramTable[j]);
          }
        } else {
          result[i] = new Annotation[0];
        }
      }
      return result;
    }
    return new Annotation[count][0];
  }

  public Class<?>[] getExceptionTypes() {
    if (vmMethod != null) {
      Class<?>[] types = Classes.getExceptionTypes(vmMethod);
      if (types != null) {
        return types;
      }
    }
    return new Class<?>[0];
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & Modifier.methodModifiers();
    if (mod != 0) {
      sb.append(Modifier.toString(mod)).append(' ');
    }
    sb.append(getReturnType().getName()).append(' ');
    sb.append(getDeclaringClass().getName()).append('.');
    sb.append(getName()).append('(');
    Class<?>[] params = getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(params[i].getName());
    }
    sb.append(')');
    Class<?>[] exceptions = getExceptionTypes();
    if (exceptions.length > 0) {
      sb.append(" throws ");
      for (int i = 0; i < exceptions.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(exceptions[i].getName());
      }
    }
    return sb.toString();
  }

  public String toGenericString() {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & Modifier.methodModifiers();
    if (mod != 0) {
      sb.append(Modifier.toString(mod)).append(' ');
    }
    sb.append(getReturnType().getTypeName()).append(' ');
    sb.append(getDeclaringClass().getTypeName()).append('.');
    sb.append(getName()).append('(');
    Class<?>[] params = getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(params[i].getTypeName());
    }
    sb.append(')');
    Class<?>[] exceptions = getExceptionTypes();
    if (exceptions.length > 0) {
      sb.append(" throws ");
      for (int i = 0; i < exceptions.length; i++) {
        if (i > 0) sb.append(',');
        sb.append(exceptions[i].getTypeName());
      }
    }
    return sb.toString();
  }

  public String getGenericSignature() {
    if (vmMethod.addendum != null && vmMethod.addendum.signature != null) {
      return Classes.toString((byte[]) vmMethod.addendum.signature);
    }
    return null;
  }

  public Type getGenericReturnType() {
    if (vmMethod.addendum != null && vmMethod.addendum.signature != null) {
      String sig = Classes.toString((byte[]) vmMethod.addendum.signature);
      try {
        SignatureParser.MethodSignature ms =
            SignatureParser.parseMethod(vmMethod.class_.loader, sig, getDeclaringClass());
        return ms.returnType;
      } catch (Exception ignored) {
      }
    }
    return getReturnType();
  }

  @Override
  public Type[] getGenericParameterTypes() {
    if (vmMethod.addendum != null && vmMethod.addendum.signature != null) {
      String sig = Classes.toString((byte[]) vmMethod.addendum.signature);
      try {
        SignatureParser.MethodSignature ms =
            SignatureParser.parseMethod(vmMethod.class_.loader, sig, getDeclaringClass());
        return ms.parameterTypes;
      } catch (Exception ignored) {
      }
    }
    return getParameterTypes();
  }

  @Override
  public Type[] getGenericExceptionTypes() {
    if (vmMethod.addendum != null && vmMethod.addendum.signature != null) {
      String sig = Classes.toString((byte[]) vmMethod.addendum.signature);
      try {
        SignatureParser.MethodSignature ms =
            SignatureParser.parseMethod(vmMethod.class_.loader, sig, getDeclaringClass());
        return ms.exceptionTypes;
      } catch (Exception ignored) {
      }
    }
    return getExceptionTypes();
  }

  @Override
  public TypeVariable<?>[] getTypeParameters() {
    if (vmMethod.addendum != null && vmMethod.addendum.signature != null) {
      String sig = Classes.toString((byte[]) vmMethod.addendum.signature);
      try {
        SignatureParser.MethodSignature ms =
            SignatureParser.parseMethod(vmMethod.class_.loader, sig, getDeclaringClass());
        return ms.typeParameters;
      } catch (Exception ignored) {
      }
    }
    return new TypeVariable<?>[0];
  }

  @Override
  public AnnotatedType getAnnotatedReturnType() {
    return new AnnotatedTypeImpl(getGenericReturnType());
  }
}
