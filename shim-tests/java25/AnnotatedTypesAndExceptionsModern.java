// Minimum release: 17
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.MalformedParametersException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.UndeclaredThrowableException;

public class AnnotatedTypesAndExceptionsModern {
  interface SampleInterface {}

  static class SampleBase implements SampleInterface {}

  static class SampleChild extends SampleBase {
    public String sampleField;

    public Integer sampleMethod(String arg1, int arg2) throws IOException {
      return 42;
    }
  }

  public static void main(String[] args) throws Exception {
    testAnnotatedTypesOnClass();
    testAnnotatedTypesOnMember();
    testReflectionExceptions();
    System.out.println("AnnotatedTypesAndExceptionsModern OK");
  }

  private static void testAnnotatedTypesOnClass() {
    AnnotatedType[] ifaces = SampleChild.class.getAnnotatedInterfaces();
    System.out.println("child ifaces length=" + ifaces.length);

    AnnotatedType sup = SampleChild.class.getAnnotatedSuperclass();
    System.out.println("child super exists=" + (sup != null));
    if (sup != null) {
      System.out.println("child super type=" + sup.getType().getTypeName());
    }

    AnnotatedType baseIfaces[] = SampleBase.class.getAnnotatedInterfaces();
    System.out.println("base ifaces length=" + baseIfaces.length);
    if (baseIfaces.length > 0) {
      System.out.println("base iface 0=" + baseIfaces[0].getType().getTypeName());
    }
  }

  private static void testAnnotatedTypesOnMember() throws Exception {
    Field f = SampleChild.class.getField("sampleField");
    AnnotatedType ft = f.getAnnotatedType();
    System.out.println("field annotated type=" + ft.getType().getTypeName());

    Method m = SampleChild.class.getMethod("sampleMethod", String.class, int.class);
    AnnotatedType rt = m.getAnnotatedReturnType();
    System.out.println("method return annotated type=" + rt.getType().getTypeName());

    AnnotatedType[] pts = m.getAnnotatedParameterTypes();
    System.out.println("param annotated count=" + pts.length);
    for (int i = 0; i < pts.length; i++) {
      System.out.println("param " + i + " type=" + pts[i].getType().getTypeName());
    }

    AnnotatedType[] ets = m.getAnnotatedExceptionTypes();
    System.out.println("exception annotated count=" + ets.length);
    if (ets.length > 0) {
      System.out.println("exception 0 type=" + ets[0].getType().getTypeName());
    }

    Parameter[] params = m.getParameters();
    System.out.println("parameter[0] annotated type=" + params[0].getAnnotatedType().getType().getTypeName());
  }

  private static void testReflectionExceptions() {
    IOException cause = new IOException("disk full");
    UndeclaredThrowableException ute = new UndeclaredThrowableException(cause, "wrapper");
    System.out.println("ute message=" + ute.getMessage());
    System.out.println("ute cause=" + ute.getCause().getMessage());
    System.out.println("ute undeclared=" + ute.getUndeclaredThrowable().getMessage());

    MalformedParametersException mpe = new MalformedParametersException("bad param count");
    System.out.println("mpe message=" + mpe.getMessage());

    GenericSignatureFormatError gsfe = new GenericSignatureFormatError("invalid signature");
    System.out.println("gsfe message=" + gsfe.getMessage());

    MalformedParameterizedTypeException mpte = new MalformedParameterizedTypeException("bad type");
    System.out.println("mpte message=" + mpte.getMessage());
  }
}
