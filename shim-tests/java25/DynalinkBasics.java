/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import jdk.dynalink.*; import jdk.dynalink.beans.*; import jdk.dynalink.linker.*; import jdk.dynalink.linker.support.*; import jdk.dynalink.support.*;
import java.lang.invoke.*; import java.util.*;
public class DynalinkBasics {
  interface C { Object call() throws Throwable; }
  public static class Bean { private String name = "n"; public int count = 5; public static int SC = 7; public String getName() { return name; } public void setName(String n) { name = n; } public boolean isOk() { return true; }
    public String hello(String who) { return "hello " + who; } public String hello(int i) { return "int " + i; } public String hello(Object o) { return "obj " + o; } public static String sm(String a, int b) { return a + b; } public String va(String... xs) { return String.join("|", xs); }
    public Bean() {} public Bean(String n) { name = n; } public Bean(int c) { count = c; } }
  static void t(String l, C c) { try { Object v = c.call(); System.out.println(l + ": " + v); } catch (Throwable e) { System.out.println(l + ": !" + e.getClass().getName() + ": " + e.getMessage()); } }
  static DynamicLinker linker = new DynamicLinkerFactory().createLinker();
  static MethodHandles.Lookup L = MethodHandles.lookup();
  static CallSite site(Operation op, MethodType t) { return linker.link(new ChainedCallSite(new CallSiteDescriptor(L, op, t))); }
  static Object call(Operation op, MethodType t, Object... args) throws Throwable { return site(op, t).dynamicInvoker().invokeWithArguments(args); }
  static final MethodType OO = MethodType.methodType(Object.class, Object.class), OOO = MethodType.methodType(Object.class, Object.class, Object.class), OOOO = MethodType.methodType(Object.class, Object.class, Object.class, Object.class);
  public static void main(String[] a) throws Throwable {
    Operation get = StandardOperation.GET, set = StandardOperation.SET;
    Operation p = get.withNamespace(StandardNamespace.PROPERTY), pm = get.withNamespaces(StandardNamespace.PROPERTY, StandardNamespace.METHOD, StandardNamespace.ELEMENT);
    System.out.println(get + " " + p + " " + p.named("foo") + " " + pm + " " + pm.named("x").toString() + " " + set.withNamespace(StandardNamespace.ELEMENT).named(3));
    System.out.println(p.equals(get.withNamespace(StandardNamespace.PROPERTY)) + " " + (p.hashCode() == get.withNamespace(StandardNamespace.PROPERTY).hashCode()) + " " + p.named("a").equals(p.named("a")) + " " + p.named("a").equals(p.named("b")));
    System.out.println(NamedOperation.getName(p.named("n")) + " " + NamedOperation.getName(p) + " " + NamedOperation.getBaseOperation(p.named("n")) + " " + NamespaceOperation.getBaseOperation(p) + " " + Arrays.toString(NamespaceOperation.getNamespaces(pm)) + " " + NamespaceOperation.contains(p, get, StandardNamespace.PROPERTY) + NamespaceOperation.contains(p, get, StandardNamespace.METHOD) + " " + StandardNamespace.findFirst(pm.named("x")) + " " + StandardNamespace.findFirst(get));
    t("noNs", () -> new NamespaceOperation(get)); t("nullNs", () -> new NamespaceOperation(get, (Namespace) null)); t("nsIdx", () -> pm.withNamespace(StandardNamespace.METHOD).getNamespace(9)); t("changeName", () -> ((NamedOperation) p.named("a")).changeName("b"));
    CallSiteDescriptor d = new CallSiteDescriptor(L, p.named("foo"), OO);
    System.out.println(d.toString().replaceAll("@.*", "@L") + " " + d.equals(new CallSiteDescriptor(L, p.named("foo"), OO)) + " " + (d.hashCode() == new CallSiteDescriptor(L, p.named("foo"), OO).hashCode()) + " " + (d.changeMethodType(OO) == d) + " " + d.changeMethodType(OOO).getMethodType() + " " + d.changeOperation(set).getOperation());
    t("lookup", () -> d.getLookup().lookupClass().getName()); t("desc null", () -> new CallSiteDescriptor(null, p, OO));
    // TypeUtilities
    Class<?>[] cs = {int.class, long.class, short.class, byte.class, char.class, boolean.class, float.class, double.class, void.class, Integer.class, Long.class, Object.class, String.class, Number.class, Character.class, Boolean.class, Comparable.class, int[].class, Object[].class, String[].class};
    StringBuilder sb = new StringBuilder();
    for (Class<?> x : cs) for (Class<?> y : cs) { sb.append(TypeUtilities.isSubtype(x, y) ? 'S' : '-').append(TypeUtilities.isMethodInvocationConvertible(x, y) ? 'M' : '-').append(TypeUtilities.isConvertibleWithoutLoss(x, y) ? 'L' : '-').append(' '); sb.append(x == cs[cs.length - 1] ? "\n" : ""); }
    System.out.println(sb.toString().hashCode());
    for (Class<?> x : cs) System.out.println(x.getSimpleName() + " " + TypeUtilities.getPrimitiveType(x) + " " + TypeUtilities.getWrapperType(x) + " " + TypeUtilities.isWrapperType(x));
    for (String n : new String[]{"int", "void", "boolean", "java.lang.Integer", "String", "double", "char"}) System.out.print(TypeUtilities.getPrimitiveTypeByName(n) + " "); System.out.println();
    // Guards
    MethodType so = MethodType.methodType(boolean.class, Object.class), ss = MethodType.methodType(boolean.class, String.class), oi = MethodType.methodType(boolean.class, Object.class, int.class);
    System.out.println((boolean) Guards.isOfClass(String.class, so).invoke("s") + " " + (boolean) Guards.isOfClass(String.class, so).invoke((Object) 1) + " " + " " + (boolean) Guards.isInstance(Number.class, so).invoke((Object) 1) + " " + (boolean) Guards.isInstance(Number.class, oi).invoke((Object) 1, 5) + " " + (boolean) Guards.isArray(0, so).invoke((Object) new int[0]) + " " + (boolean) Guards.isNull().invoke((Object) null) + " " + (boolean) Guards.isNotNull().invoke((Object) null) + " " + (boolean) Guards.getIdentityGuard("x").invoke((Object) "x") + (boolean) Guards.getClassGuard(String.class).invoke((Object) "q") + (boolean) Guards.getInstanceOfGuard(CharSequence.class).invoke((Object) "q"));
    // Lookup
    t("lk static", () -> Lookup.PUBLIC.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class)).invoke("12"));
    t("lk nostatic", () -> Lookup.PUBLIC.findStatic(Integer.class, "nope", MethodType.methodType(int.class, String.class)));
    t("lk virtual", () -> Lookup.PUBLIC.findVirtual(String.class, "length", MethodType.methodType(int.class)).invoke("abc"));
    t("lk novirtual", () -> Lookup.PUBLIC.findVirtual(String.class, "zzz", MethodType.methodType(int.class)));
    t("lk getter", () -> Lookup.PUBLIC.findGetter(Bean.class, "count", int.class).invoke(new Bean()));
    t("lk nogetter", () -> Lookup.PUBLIC.findGetter(Bean.class, "zz", int.class));
    t("lk own", () -> new Lookup(L).findOwnStatic("t", void.class, String.class, C.class).type());
    // GuardedInvocation
    MethodHandle inv = L.findStatic(DynalinkBasics.class, "fallbackX", MethodType.methodType(String.class, Object.class)), fb = L.findStatic(DynalinkBasics.class, "fbY", MethodType.methodType(String.class, Object.class));
    GuardedInvocation gi = new GuardedInvocation(inv, Guards.isOfClass(String.class, so));
    MethodHandle c = gi.compose(fb); System.out.println((String) c.invoke((Object) "str") + " " + (String) c.invoke((Object) 3) + " " + gi.asType(MethodType.methodType(Object.class, String.class)).getInvocation().type() + " " + gi.getGuard().type() + " " + gi.hasBeenInvalidated() + " " + gi.getException());
    SwitchPoint sp = new SwitchPoint(); GuardedInvocation gs = new GuardedInvocation(inv, null, sp); MethodHandle cs2 = gs.compose(fb); System.out.print((String) cs2.invoke((Object) "a") + " "); SwitchPoint.invalidateAll(new SwitchPoint[]{sp}); System.out.println((String) cs2.invoke((Object) "a") + " " + gs.hasBeenInvalidated() + " " + gs.addSwitchPoint(new SwitchPoint()).getSwitchPoints().length + " " + gs.addSwitchPoint(null).getSwitchPoints().length);
    t("gi null", () -> new GuardedInvocation(null));
    t("gi drop", () -> new GuardedInvocation(inv, Guards.isOfClass(String.class, so)).dropArguments(1, int.class).getInvocation().type());
    t("gi filter", () -> new GuardedInvocation(inv).filterArguments(0, L.findStatic(DynalinkBasics.class, "filt", MethodType.methodType(Object.class, Object.class))).getInvocation().invoke((Object) "q"));
    // Beans
    Bean b = new Bean();
    t("get name", () -> call(p.named("name"), OO, b)); t("get count", () -> call(p.named("count"), OO, b)); t("get ok", () -> call(p.named("ok"), OO, b)); t("get class", () -> call(p.named("class"), OO, b));
    t("set name", () -> { call(set.withNamespace(StandardNamespace.PROPERTY).named("name"), MethodType.methodType(void.class, Object.class, Object.class), b, "zed"); return b.getName(); });
    t("set count", () -> { call(set.withNamespace(StandardNamespace.PROPERTY).named("count"), MethodType.methodType(void.class, Object.class, int.class), b, 9); return b.count; });
    t("get dyn", () -> call(p, OOO, b, "name"));
    t("get missing", () -> call(p.named("zzz"), OO, b));
    t("get method", () -> BeansLinker.isDynamicMethod(call(get.withNamespace(StandardNamespace.METHOD).named("hello"), OO, b)));
    t("call hello", () -> { Object m = call(get.withNamespace(StandardNamespace.METHOD).named("hello"), OO, b); return call(StandardOperation.CALL, MethodType.methodType(Object.class, Object.class, Object.class, Object.class), m, b, "bob"); });
    t("call hello int", () -> { Object m = call(get.withNamespace(StandardNamespace.METHOD).named("hello"), OO, b); return call(StandardOperation.CALL, MethodType.methodType(Object.class, Object.class, Object.class, int.class), m, b, 4); });
    t("call hello dbl", () -> { Object m = call(get.withNamespace(StandardNamespace.METHOD).named("hello"), OO, b); return call(StandardOperation.CALL, MethodType.methodType(Object.class, Object.class, Object.class, Object.class), m, b, 4.5); });
    t("call va", () -> { Object m = call(get.withNamespace(StandardNamespace.METHOD).named("va"), OO, b); return call(StandardOperation.CALL, MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class), m, b, "x", "y"); });
    t("call nomethod", () -> call(get.withNamespace(StandardNamespace.METHOD).named("nope"), OO, b));
    t("static", () -> { StaticClass sc = StaticClass.forClass(Bean.class); Object m = call(get.withNamespace(StandardNamespace.METHOD).named("sm"), OO, sc); return call(StandardOperation.CALL, MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class), m, sc, "k", 3) + " " + call(p.named("SC"), OO, sc) + " " + sc + " " + (sc == StaticClass.forClass(Bean.class)) + " " + sc.getRepresentedClass().getSimpleName(); });
    t("new", () -> { Object o = call(StandardOperation.NEW, MethodType.methodType(Object.class, Object.class, Object.class), StaticClass.forClass(Bean.class), "made"); return ((Bean) o).getName(); });
    t("new int", () -> { Object o = call(StandardOperation.NEW, MethodType.methodType(Object.class, Object.class, int.class), StaticClass.forClass(Bean.class), 42); return ((Bean) o).count; });
    t("array get", () -> call(get.withNamespace(StandardNamespace.ELEMENT), OOO, new int[]{4, 5, 6}, 1));
    t("array set", () -> { int[] arr = new int[3]; call(set.withNamespace(StandardNamespace.ELEMENT), MethodType.methodType(void.class, Object.class, Object.class, Object.class), arr, 2, 8); return arr[2]; });
    t("list get", () -> call(get.withNamespace(StandardNamespace.ELEMENT), OOO, List.of("a", "b"), 1));
    t("list get named", () -> call(get.withNamespace(StandardNamespace.ELEMENT).named(0), OO, List.of("a", "b")));
    t("map get", () -> call(get.withNamespace(StandardNamespace.ELEMENT), OOO, Map.of("k", "v"), "k"));
    t("len arr", () -> call(p.named("length"), OO, new int[5])); t("len list", () -> call(p.named("length"), OO, new ArrayList<>(List.of(1, 2))));
    t("multi ns", () -> call(pm.named("name"), OO, b)); t("multi ns method", () -> BeansLinker.isDynamicMethod(call(pm.named("hello"), OO, b)));
    t("null recv", () -> call(p.named("x"), OO, new Object[]{null}));
    t("string len", () -> { Object m = call(get.withNamespace(StandardNamespace.METHOD).named("length"), OO, "abcd"); return call(StandardOperation.CALL, OOO, m, "abcd"); });
    t("ctor", () -> BeansLinker.isDynamicConstructor(BeansLinker.getConstructorMethod(Bean.class, "java.lang.String")) + " " + BeansLinker.getConstructorMethod(Bean.class, "double"));
    // custom linker, relinking
    GuardingDynamicLinker strLinker = (req, svc) -> { Object r = req.getReceiver(); if (r instanceof String) return new GuardedInvocation(MethodHandles.dropArguments(MethodHandles.constant(Object.class, "STR"), 0, Object.class), Guards.isOfClass(String.class, req.getCallSiteDescriptor().getMethodType())); return null; };
    DynamicLinkerFactory f = new DynamicLinkerFactory(); f.setPrioritizedLinker(strLinker); DynamicLinker l2 = f.createLinker();
    CallSite cs3 = l2.link(new ChainedCallSite(new CallSiteDescriptor(L, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("length"), OO)));
    System.out.println(cs3.dynamicInvoker().invoke((Object) "abc") + " " + cs3.dynamicInvoker().invoke((Object) new int[3]) + " " + cs3.dynamicInvoker().invoke((Object) "z") + " " + (l2.getLinkerServices().canConvert(int.class, long.class)) + (l2.getLinkerServices().canConvert(String.class, Integer.class)));
    t("nolinker", () -> new DynamicLinkerFactory().createLinker().link(new SimpleRelinkableCallSite(new CallSiteDescriptor(L, StandardOperation.CALL, OO))).dynamicInvoker().invoke((Object) "x"));
    t("prefallback", () -> { DynamicLinkerFactory ff = new DynamicLinkerFactory(); ff.setFallbackLinkers(new GuardingDynamicLinker[0]); return ff.createLinker().link(new SimpleRelinkableCallSite(new CallSiteDescriptor(L, p.named("a"), OO))).dynamicInvoker().invoke((Object) "x"); });
    t("services conv", () -> { LinkerServices ls = linker.getLinkerServices(); return ls.asType(L.findStatic(Integer.class, "toString", MethodType.methodType(String.class, int.class)), MethodType.methodType(Object.class, Integer.class)).invoke((Integer) 5) + " " + ls.compareConversion(int.class, long.class, double.class) + " " + ls.compareConversion(Integer.class, Object.class, Number.class); });
  }
  static String fallbackX(Object o) { return "X:" + o; } static String fbY(Object o) { return "Y:" + o; } static Object filt(Object o) { return "filtered " + o; }
}
