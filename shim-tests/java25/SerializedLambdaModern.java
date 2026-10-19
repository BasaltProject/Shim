/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */
import java.io.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.*;

public class SerializedLambdaModern {
  interface Marker { }

  interface SerFn<A, B> extends Function<A, B>, Serializable { }

  interface Wide {
    Object apply(Integer i, String s, String t);
  }

  static String join(Number n, Object... rest) {
    return n + ":" + rest.length;
  }

  static String twice(String s) {
    return s + s;
  }

  String prefix = "p-";

  String withPrefix(String s) {
    return prefix + s;
  }

  static void t(Callable<Object> c) {
    try {
      System.out.println(c.call());
    } catch (Throwable e) {
      System.out.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  static String norm(String s) {
    return s.replaceAll("lambda\\$([a-zA-Z]+)\\$[0-9a-f]+\\$?[0-9]*", "lambda\\$$1\\$H").replaceAll("\\$\\$Lambda.*", "\\$\\$Lambda");
  }

  static SerializedLambda info(Object o) throws Exception {
    Method m = o.getClass().getDeclaredMethod("writeReplace");
    m.setAccessible(true);
    return (SerializedLambda) m.invoke(o);
  }

  @SuppressWarnings("unchecked")
  static <T> T roundTrip(T o) throws Exception {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(b)) {
      out.writeObject(o);
    }
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b.toByteArray()))) {
      return (T) in.readObject();
    }
  }

  public static void main(String[] args) throws Exception {
    int k = 5;
    long l = 1L << 40;
    double d = 2.5;
    String s = "x";
    Runnable marked = (Runnable & Marker) () -> { };
    t(() -> marked instanceof Marker);
    t(() -> marked.getClass().getInterfaces().length);
    Supplier<String> sup = (Supplier<String> & Serializable) () -> s + k + l + d;
    t(() -> sup.get());
    t(() -> sup instanceof Serializable);
    t(() -> sup.getClass().isSynthetic() + " " + java.lang.reflect.Modifier.toString(sup.getClass().getModifiers()));
    t(() -> {
      SerializedLambda sl = info(sup);
      return norm(sl.getImplMethodName()) + " " + sl.getCapturedArgCount() + " " + sl.getCapturedArg(0) + " "
          + sl.getCapturedArg(1) + " " + sl.getCapturedArg(2) + " " + sl.getCapturedArg(3) + " "
          + sl.getFunctionalInterfaceClass() + " " + sl.getFunctionalInterfaceMethodName() + " "
          + sl.getFunctionalInterfaceMethodSignature() + " " + sl.getImplClass() + " " + sl.getImplMethodKind() + " "
          + sl.getImplMethodSignature() + " " + sl.getInstantiatedMethodType() + " " + sl.getCapturingClass();
    });
    t(() -> norm(info(sup).toString()));
    t(() -> roundTrip(sup).get());
    SerFn<String, String> ref = SerializedLambdaModern::twice;
    t(() -> roundTrip(ref).apply("ab"));
    t(() -> {
      SerializedLambda sl = info(ref);
      return sl.getImplMethodName() + " " + sl.getImplMethodKind() + " " + sl.getCapturedArgCount() + " "
          + sl.getImplMethodSignature();
    });
    SerializedLambdaModern self = new SerializedLambdaModern();
    SerFn<String, String> bound = self::withPrefix;
    t(() -> roundTrip(bound).apply("q"));
    t(() -> info(bound).getImplMethodKind() + " " + info(bound).getCapturedArgCount());
    SerFn<String, Integer> unbound = String::length;
    t(() -> roundTrip(unbound).apply("four"));
    t(() -> info(unbound).getImplMethodKind() + " " + info(unbound).getImplClass());
    SerFn<String, StringBuilder> ctor = StringBuilder::new;
    t(() -> roundTrip(ctor).apply("c").toString());
    t(() -> info(ctor).getImplMethodKind());
    BiFunction<Integer, Integer, Integer> plain = (a, b) -> a + b;
    t(() -> {
      try {
        plain.getClass().getDeclaredMethod("writeReplace");
        return "has";
      } catch (NoSuchMethodException e) {
        return "none";
      }
    });
    Wide wide = SerializedLambdaModern::join;
    t(() -> wide.apply(1, "a", "b"));
  }
}
