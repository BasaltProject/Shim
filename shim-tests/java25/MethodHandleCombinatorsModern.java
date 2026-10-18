/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
public class MethodHandleCombinatorsModern {
  interface Th { Object call() throws Throwable; }
  static void t(String n, Th c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static int f1(int sel, String s) { return s.length() + 100; }
  public static int f2(int sel, String s) { return s.length() + 200; }
  public static int fb(int sel, String s) { return -sel; }
  public static String join3(String a, String b, String c) { return a + "|" + b + "|" + c; }
  public static String join2(String a, String b) { return a + "|" + b; }
  public static String ints(int a, long b, short c, byte d, char e, float f, double g, boolean h) { return a + "," + b + "," + c + "," + d + "," + (int) e + "," + f + "," + g + "," + h; }
  public static long retLong(int x) { return x * 3L; }
  public static void sideEffect(String s) { }
  public static String vsum(String... parts) { return String.join("+", parts); }
  int field;
  public static void main(String[] a) throws Throwable {
    MethodHandles.Lookup l = MethodHandles.lookup();
    MethodHandle h1 = l.findStatic(MethodHandleCombinatorsModern.class, "f1", MethodType.methodType(int.class, int.class, String.class));
    MethodHandle h2 = l.findStatic(MethodHandleCombinatorsModern.class, "f2", MethodType.methodType(int.class, int.class, String.class));
    MethodHandle fb = l.findStatic(MethodHandleCombinatorsModern.class, "fb", MethodType.methodType(int.class, int.class, String.class));
    MethodHandle ts = MethodHandles.tableSwitch(fb, h1, h2);
    t("tableSwitch", () -> (int) ts.invokeExact(0, "abc") + " " + (int) ts.invokeExact(1, "abcd") + " " + (int) ts.invokeExact(2, "x") + " " + (int) ts.invokeExact(-1, "x") + " " + ts.type());
    t("tableSwitchEmpty", () -> MethodHandles.tableSwitch(fb));
    t("tableSwitchBadType", () -> MethodHandles.tableSwitch(fb, l.findStatic(MethodHandleCombinatorsModern.class, "join2", MethodType.methodType(String.class, String.class, String.class))));
    t("tableSwitchNoInt", () -> MethodHandles.tableSwitch(l.findStatic(MethodHandleCombinatorsModern.class, "join2", MethodType.methodType(String.class, String.class, String.class)), h1));
    t("tableSwitchNull", () -> MethodHandles.tableSwitch(null, h1));
    MethodHandle j3 = l.findStatic(MethodHandleCombinatorsModern.class, "join3", MethodType.methodType(String.class, String.class, String.class, String.class));
    MethodHandle d1 = MethodHandles.dropArgumentsToMatch(j3, 1, List.of(String.class, int.class, String.class, String.class, double.class), 2);
    t("dropToMatch", () -> d1.type() + " " + d1.invoke("A", "B", 5, "C", "D", 2.5));
    t("dropToMatch0", () -> { MethodHandle d = MethodHandles.dropArgumentsToMatch(j3, 0, List.of(String.class, String.class, String.class, long.class), 0); return d.type() + " " + d.invoke("a", "b", "c", 9L); });
    t("dropToMatchBad", () -> MethodHandles.dropArgumentsToMatch(j3, 1, List.of(String.class, int.class), 0));
    t("dropToMatchBadType", () -> MethodHandles.dropArgumentsToMatch(j3, 0, List.of(int.class, String.class, String.class), 0));
    t("dropToMatchBadPos", () -> MethodHandles.dropArgumentsToMatch(j3, 0, List.of(String.class, String.class, String.class), 5));
    t("dropToMatchBadSkip", () -> MethodHandles.dropArgumentsToMatch(j3, 4, List.of(String.class), 0));
    MethodHandle im = l.findStatic(MethodHandleCombinatorsModern.class, "ints", MethodType.methodType(String.class, int.class, long.class, short.class, byte.class, char.class, float.class, double.class, boolean.class));
    MethodHandle ec = MethodHandles.explicitCastArguments(im, MethodType.methodType(String.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, int.class));
    t("explicitDoubles", () -> ec.invoke(300.7, 1e19, -70000.5, 200.0, 65.9, 3.99, 2.5, 3));
    t("explicitNaN", () -> ec.invoke(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e10, -1.0, 1.5, 2.5, 2));
    MethodHandle ec2 = MethodHandles.explicitCastArguments(im, MethodType.methodType(Object.class, Integer.class, Long.class, Short.class, Byte.class, Character.class, Float.class, Double.class, Boolean.class));
    t("explicitBoxed", () -> ec2.invoke(null, null, null, null, null, null, null, null));
    t("explicitBoxed2", () -> ec2.invoke(7, 8L, (short) 9, (byte) 10, 'c', 1.5f, 2.5, true));
    MethodHandle ec3 = MethodHandles.explicitCastArguments(im, MethodType.methodType(String.class, long.class, int.class, int.class, int.class, int.class, int.class, int.class, long.class));
    t("explicitLongs", () -> ec3.invoke(5000000000L, 70000, 70000, 300, 70000, 16777217, 5, 3L));
    MethodHandle rl = l.findStatic(MethodHandleCombinatorsModern.class, "retLong", MethodType.methodType(long.class, int.class));
    MethodHandle rec = MethodHandles.explicitCastArguments(rl, MethodType.methodType(byte.class, long.class));
    t("explicitRet", () -> (byte) rec.invokeExact(100L));
    MethodHandle rec2 = MethodHandles.explicitCastArguments(rl, MethodType.methodType(void.class, int.class));
    t("explicitVoid", () -> rec2.type() + " " + rec2.invoke(1));
    MethodHandle rec3 = MethodHandles.explicitCastArguments(rl, MethodType.methodType(Object.class, Object.class));
    t("explicitObj", () -> rec3.invoke(7));
    t("explicitObjBad", () -> rec3.invoke("str"));
    t("explicitArity", () -> MethodHandles.explicitCastArguments(rl, MethodType.methodType(long.class, int.class, int.class)));
    t("explicitSame", () -> MethodHandles.explicitCastArguments(rl, rl.type()) == rl);
    VarHandle vh = l.findVarHandle(MethodHandleCombinatorsModern.class, "field", int.class);
    MethodHandle vi = MethodHandles.varHandleInvoker(VarHandle.AccessMode.GET_AND_ADD, MethodType.methodType(int.class, MethodHandleCombinatorsModern.class, int.class));
    MethodHandleCombinatorsModern o = new MethodHandleCombinatorsModern(); o.field = 5;
    t("varHandleInvoker", () -> vi.type() + " " + (int) vi.invoke(vh, o, 3) + " " + o.field);
    MethodHandle ve = MethodHandles.varHandleExactInvoker(VarHandle.AccessMode.GET, MethodType.methodType(int.class, MethodHandleCombinatorsModern.class));
    t("varHandleExact", () -> (int) ve.invoke(vh, o));
    MethodHandle veBad = MethodHandles.varHandleExactInvoker(VarHandle.AccessMode.GET, MethodType.methodType(long.class, MethodHandleCombinatorsModern.class));
    t("varHandleExactBad", () -> veBad.invoke(vh, o));
    t("reflectAs", () -> MethodHandles.reflectAs(Method.class, h1) + " " + MethodHandles.reflectAs(Field.class, l.findGetter(MethodHandleCombinatorsModern.class, "field", int.class)) + " " + MethodHandles.reflectAs(Constructor.class, l.findConstructor(MethodHandleCombinatorsModern.class, MethodType.methodType(void.class))));
    t("reflectAsBad", () -> MethodHandles.reflectAs(Field.class, h1));
    MethodHandle vs = l.findStatic(MethodHandleCombinatorsModern.class, "vsum", MethodType.methodType(String.class, String[].class));
    t("withVarargs", () -> vs.isVarargsCollector() + " " + vs.withVarargs(true).isVarargsCollector() + " " + vs.withVarargs(true).invoke("a", "b", "c") + " " + vs.withVarargs(true).withVarargs(false).isVarargsCollector());
    t("withVarargsBad", () -> j3.withVarargs(true));
    t("withVarargsNoop", () -> j3.withVarargs(false) == j3);
    MethodHandle sp = j3.asSpreader(1, Object[].class, 2);
    t("asSpreaderPos", () -> sp.type() + " " + sp.invoke("a", new Object[] {"b", "c"}));
    t("asSpreaderPos0", () -> { MethodHandle s0 = j3.asSpreader(0, String[].class, 3); return s0.type() + " " + s0.invoke((Object) new String[] {"x", "y", "z"}); });
    t("asSpreaderBadPos", () -> j3.asSpreader(4, Object[].class, 0));
    t("asSpreaderBadLen", () -> j3.asSpreader(2, Object[].class, 2));
    t("asSpreaderBadType", () -> j3.asSpreader(0, String.class, 1));
        t("asCollectorBadParam", () -> j3.asCollector(1, String[].class, 0));
    t("asCollectorPos", () -> { MethodHandle c1 = l.findStatic(MethodHandleCombinatorsModern.class, "vsum", MethodType.methodType(String.class, String[].class)); MethodHandle t3 = MethodHandles.insertArguments(MethodHandles.dropArguments(c1, 1, String.class), 1, "I"); return t3.type(); });
    MethodHandle mid = l.findStatic(Arrays.class, "asList", MethodType.methodType(List.class, Object[].class));
    t("asCollectorPos2", () -> { MethodHandle t4 = l.findStatic(MethodHandleCombinatorsModern.class, "mix", MethodType.methodType(String.class, String.class, String[].class, String.class)); MethodHandle c = t4.asCollector(1, String[].class, 2); return c.type() + " " + c.invoke("A", "x", "y", "Z"); });
    t("asCollectorBadPos", () -> j3.asCollector(5, String[].class, 1));
    t("asCollectorNotArray", () -> j3.asCollector(0, String.class, 1));
    t("invokeReflect", () -> MethodHandle.class.getMethod("invokeExact", Object[].class).getReturnType().getSimpleName());
  }
  public static String mix(String a, String[] mid, String z) { return a + Arrays.toString(mid) + z; }
}
