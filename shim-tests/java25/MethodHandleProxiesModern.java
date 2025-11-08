/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.invoke.*;
import java.util.*;
import java.util.function.*;
public class MethodHandleProxiesModern {
  public interface Two { void a(); void b(); }
  interface Hidden { void a(); }
  public interface Def { int f(int x); default int g() { return 7; } String toString(); }
  public interface Ex { void run() throws java.io.IOException; }
  public interface Gen<T> { T get(T x); }
  public interface Cmp { boolean equals(Object o); int cmp(String a, String b); }
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void main(String[] a) throws Throwable {
    MethodHandles.Lookup l = MethodHandles.lookup();
    MethodHandle len = l.findVirtual(String.class, "length", MethodType.methodType(int.class));
    t("two", () -> MethodHandleProxies.asInterfaceInstance(Two.class, len));
    t("hidden", () -> MethodHandleProxies.asInterfaceInstance(Hidden.class, len));
    t("class", () -> MethodHandleProxies.asInterfaceInstance(String.class, len));
    t("nulls", () -> MethodHandleProxies.asInterfaceInstance(null, len));
    t("nulls2", () -> MethodHandleProxies.asInterfaceInstance(Runnable.class, null));
    MethodHandle sq = l.findStatic(Math.class, "abs", MethodType.methodType(int.class, int.class));
    Def d = MethodHandleProxies.asInterfaceInstance(Def.class, sq);
    t("def", () -> d.f(-5) + " " + d.g() + " " + d.equals(d) + " " + (d.hashCode() == d.hashCode()));
    Function<String,Integer> fn = MethodHandleProxies.asInterfaceInstance(Function.class, l.findVirtual(String.class, "length", MethodType.methodType(int.class)));
    t("fn", () -> fn.apply("hello") + " " + fn.andThen(x -> x * 2).apply("ab"));
    t("wrapper", () -> MethodHandleProxies.isWrapperInstance(fn) + " " + MethodHandleProxies.isWrapperInstance("s") + " " + MethodHandleProxies.wrapperInstanceTarget(fn).type() + " " + MethodHandleProxies.wrapperInstanceType(fn).getName());
    t("wrapperBad", () -> MethodHandleProxies.wrapperInstanceTarget("x"));
    t("wrapperBad2", () -> MethodHandleProxies.wrapperInstanceType("x"));
    t("wrapperNull", () -> MethodHandleProxies.isWrapperInstance(null));
    Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, l.findStatic(MethodHandleProxiesModern.class, "boom", MethodType.methodType(void.class)));
    t("boom", () -> { r.run(); return "ok"; });
    Ex ex = MethodHandleProxies.asInterfaceInstance(Ex.class, l.findStatic(MethodHandleProxiesModern.class, "boom", MethodType.methodType(void.class)));
    t("exDeclared", () -> { ex.run(); return "ok"; });
    Runnable r2 = MethodHandleProxies.asInterfaceInstance(Runnable.class, l.findStatic(MethodHandleProxiesModern.class, "checked", MethodType.methodType(void.class)));
    t("undeclared", () -> { r2.run(); return "ok"; });
    t("badType", () -> { Runnable r3 = MethodHandleProxies.asInterfaceInstance(Runnable.class, len); r3.run(); return "ok"; });
    Comparator<String> cmp = MethodHandleProxies.asInterfaceInstance(Comparator.class, l.findVirtual(String.class, "compareTo", MethodType.methodType(int.class, String.class)));
    t("cmp", () -> cmp.compare("a", "b") + " " + cmp.reversed().compare("a", "b"));
    t("cmpEq", () -> cmp.equals(cmp) + " " + cmp.equals("x"));
    t("ret", () -> { Supplier<Object> s = MethodHandleProxies.asInterfaceInstance(Supplier.class, MethodHandles.constant(String.class, "k")); return s.get(); });
    t("retVoid", () -> { Runnable rr = MethodHandleProxies.asInterfaceInstance(Runnable.class, MethodHandles.constant(String.class, "k")); rr.run(); return "ok"; });
  }
  static void boom() { throw new IllegalStateException("boom"); }
  static void checked() throws Exception { throw new java.io.IOException("io"); }
}
