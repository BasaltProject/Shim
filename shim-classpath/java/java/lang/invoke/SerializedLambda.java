/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2016, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2016, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.invoke;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class SerializedLambda implements java.io.Serializable {
  private static final long serialVersionUID = 8025925345765570181L;

  private final Class<?> capturingClass;
  private final String functionalInterfaceClass;
  private final String functionalInterfaceMethodName;
  private final String functionalInterfaceMethodSignature;
  private final String implClass;
  private final String implMethodName;
  private final String implMethodSignature;
  private final int implMethodKind;
  private final String instantiatedMethodType;
  private final Object[] capturedArgs;

  public SerializedLambda(Class<?> capturingClass,
                          String functionalInterfaceClass,
                          String functionalInterfaceMethodName,
                          String functionalInterfaceMethodSignature,
                          int implMethodKind,
                          String implClass,
                          String implMethodName,
                          String implMethodSignature,
                          String instantiatedMethodType,
                          Object[] capturedArgs) {
    this.capturingClass = capturingClass;
    this.functionalInterfaceClass = functionalInterfaceClass;
    this.functionalInterfaceMethodName = functionalInterfaceMethodName;
    this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
    this.implMethodKind = implMethodKind;
    this.implClass = implClass;
    this.implMethodName = implMethodName;
    this.implMethodSignature = implMethodSignature;
    this.instantiatedMethodType = instantiatedMethodType;
    this.capturedArgs = Objects.requireNonNull(capturedArgs).clone();
  }

  public String getCapturingClass() {
    return capturingClass.getName().replace('.', '/');
  }

  public String getFunctionalInterfaceClass() {
    return functionalInterfaceClass;
  }

  public String getFunctionalInterfaceMethodName() {
    return functionalInterfaceMethodName;
  }

  public String getFunctionalInterfaceMethodSignature() {
    return functionalInterfaceMethodSignature;
  }

  public String getImplClass() {
    return implClass;
  }

  public String getImplMethodName() {
    return implMethodName;
  }

  public String getImplMethodSignature() {
    return implMethodSignature;
  }

  public int getImplMethodKind() {
    return implMethodKind;
  }

  public final String getInstantiatedMethodType() {
    return instantiatedMethodType;
  }

  public int getCapturedArgCount() {
    return capturedArgs.length;
  }

  public Object getCapturedArg(int i) {
    return capturedArgs[i];
  }

  private Object readResolve() throws ObjectStreamException {
    try {
      Method deserialize = capturingClass.getDeclaredMethod("$deserializeLambda$", SerializedLambda.class);
      deserialize.setAccessible(true);
      return deserialize.invoke(null, this);
    } catch (ReflectiveOperationException e) {
      InvalidObjectException ex = new InvalidObjectException("ReflectiveOperationException during deserialization");
      ex.initCause(e);
      throw ex;
    }
  }

  @Override
  public String toString() {
    String implKind = MethodHandleInfo.referenceKindToString(implMethodKind);
    return String.format("SerializedLambda[%s=%s, %s=%s.%s:%s, %s=%s %s.%s:%s, %s=%s, %s=%d]",
        "capturingClass", capturingClass, "functionalInterfaceMethod", functionalInterfaceClass,
        functionalInterfaceMethodName, functionalInterfaceMethodSignature, "implementation", implKind, implClass,
        implMethodName, implMethodSignature, "instantiatedMethodType", instantiatedMethodType, "numCaptured",
        capturedArgs.length);
  }
}
