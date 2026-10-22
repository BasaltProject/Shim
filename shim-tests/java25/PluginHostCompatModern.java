/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteOrder;
import java.security.CodeSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class PluginHostCompatModern {
  static abstract class Token<T> {
    final AnnotatedType type = getClass().getAnnotatedSuperclass();
  }

  public class Inner {
  }

  public static class Nested {
  }

  interface Converter {
    String convert(Object value);
  }

  static final VarHandle ELEMENTS = MethodHandles.arrayElementVarHandle(Object[].class);

  static void tokens() {
    Token<Map<String, List<Integer>>> token = new Token<Map<String, List<Integer>>>() { };
    System.out.println(token.type instanceof AnnotatedParameterizedType);
    ParameterizedType parameterized = (ParameterizedType) token.type.getType();
    System.out.println(parameterized.getRawType() == Token.class);
    AnnotatedType[] arguments = ((AnnotatedParameterizedType) token.type).getAnnotatedActualTypeArguments();
    AnnotatedType[] inner = ((AnnotatedParameterizedType) arguments[0]).getAnnotatedActualTypeArguments();
    System.out.println(arguments.length + " " + arguments[0].getType() + " " + inner[1].getType());
    System.out.println(Nested.class.getEnclosingClass() == PluginHostCompatModern.class);
    System.out.println(Inner.class.getEnclosingClass() == PluginHostCompatModern.class);
    System.out.println(PluginHostCompatModern.class.getEnclosingClass());
  }

  static void exchange() throws Exception {
    Object[] cells = new Object[2];
    AtomicInteger wins = new AtomicInteger();
    AtomicInteger bad = new AtomicInteger();
    Thread[] threads = new Thread[4];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(() -> {
        for (int n = 0; n < 20000; n++) {
          Object mine = new Object();
          Object witness = ELEMENTS.compareAndExchange(cells, 1, null, mine);
          if (witness == null) {
            wins.incrementAndGet();
            if (ELEMENTS.compareAndExchange(cells, 1, mine, null) != mine) bad.incrementAndGet();
          } else if (witness == mine) {
            bad.incrementAndGet();
          }
        }
      });
      threads[i].start();
    }
    for (Thread thread : threads) thread.join();
    System.out.println("bad=" + bad + " empty=" + (cells[1] == null) + " wins>0=" + (wins.get() > 0));
    Object marker = new Object();
    Object[] pair = {new String("x")};
    System.out.println(ELEMENTS.compareAndSet(pair, 0, new String("x"), marker));
    Integer[] boxed = {1000};
    VarHandle boxedHandle = MethodHandles.arrayElementVarHandle(Integer[].class);
    System.out.println(boxedHandle.compareAndSet(boxed, 0, Integer.valueOf(1000), Integer.valueOf(1)));
  }

  static void invokerLambda() throws Throwable {
    MethodHandle invoker = MethodHandles.invoker(MethodType.methodType(String.class, Object.class));
    MethodHandle direct = MethodHandles.lookup().findStatic(String.class, "valueOf",
        MethodType.methodType(String.class, Object.class));
    BiFunction<MethodHandle, Object, String> function = (BiFunction<MethodHandle, Object, String>) LambdaMetafactory
        .metafactory(MethodHandles.lookup(), "apply", MethodType.methodType(BiFunction.class),
            MethodType.methodType(Object.class, Object.class, Object.class), invoker,
            MethodType.methodType(String.class, MethodHandle.class, Object.class))
        .getTarget().invoke();
    System.out.println(function.apply(direct, 42));
  }

  static void segments() {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment heap = MemorySegment.ofArray(new byte[16]);
      ValueLayout.OfFloat floats = ValueLayout.JAVA_FLOAT.withByteAlignment(1).withOrder(ByteOrder.BIG_ENDIAN);
      ValueLayout.OfDouble doubles = ValueLayout.JAVA_DOUBLE.withByteAlignment(1).withOrder(ByteOrder.LITTLE_ENDIAN);
      heap.set(floats, 1, 1.5f);
      heap.set(doubles, 5, -2.25);
      System.out.println(heap.get(floats, 1) + " " + heap.get(doubles, 5));
      MemorySegment off = arena.allocate(16);
      off.set(floats, 3, 3.5f);
      System.out.println(off.get(floats, 3));
    }
  }

  static void sources() throws Exception {
    CodeSource source = PluginHostCompatModern.class.getProtectionDomain().getCodeSource();
    System.out.println(source != null);
    System.out.println(java.security.SecureClassLoader.class.getSuperclass() == ClassLoader.class);
    System.out.println(java.net.URLClassLoader.class.getSuperclass() == java.security.SecureClassLoader.class);
    StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    System.out.println(walker.walk(frames -> frames.findFirst().get().getDeclaringClass()) == PluginHostCompatModern.class);
    Class<?> unix = Class.forName("com.sun.security.auth.module.UnixSystem");
    Object system = unix.getConstructor().newInstance();
    long uid = (Long) unix.getMethod("getUid").invoke(system);
    System.out.println(uid >= 0);
  }

  public static void main(String[] args) throws Throwable {
    tokens();
    exchange();
    invokerLambda();
    segments();
    sources();
  }
}
