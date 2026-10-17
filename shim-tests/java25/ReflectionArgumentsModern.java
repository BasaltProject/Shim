/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.reflect.*;
import java.util.*;
public class ReflectionArgumentsModern {
  public static class T {
    public int f; public long lf; public String sf; public static int sx; public final int fin = 3; public Integer boxed;
    public String s(String a) { return "s:" + a; }
    public int i(int a) { return a + 1; }
    public long l(long a) { return a + 1; }
    public double d(double a) { return a + 1; }
    public static String st(String a) { return "st:" + a; }
    public Object o(Object a) { return "o:" + a; }
    public String two(int a, String b) { return a + b; }
    public String vs(String... a) { return String.valueOf(a.length); }
    public void v() {}
    public boolean z(boolean b) { return !b; }
    public char c(char x) { return x; }
    public byte b(byte x) { return x; }
    public short sh(short x) { return x; }
    public float fl(float x) { return x; }
    public Number num(Number n) { return n; }
    public T() {}
    public T(int a) { f = a; }
    public T(String a) { sf = a; }
    public T(int a, String b) { f = a; sf = b; }
  }
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { Object o = c.call(); System.out.println(n + ": " + o); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
  public static void main(String[] a) throws Exception {
    T o = new T();
    Method s = T.class.getMethod("s", String.class), i = T.class.getMethod("i", int.class), l = T.class.getMethod("l", long.class), d = T.class.getMethod("d", double.class), st = T.class.getMethod("st", String.class), oo = T.class.getMethod("o", Object.class), two = T.class.getMethod("two", int.class, String.class), vs = T.class.getMethod("vs", String[].class), v = T.class.getMethod("v"), z = T.class.getMethod("z", boolean.class), c = T.class.getMethod("c", char.class), b = T.class.getMethod("b", byte.class), sh = T.class.getMethod("sh", short.class), fl = T.class.getMethod("fl", float.class), num = T.class.getMethod("num", Number.class);
    t("ok", () -> s.invoke(o, "x"));
    t("wrongtype", () -> s.invoke(o, 5));
    t("wrongcount0", () -> s.invoke(o));
    t("wrongcount2", () -> s.invoke(o, "a", "b"));
    t("nullargs", () -> s.invoke(o, (Object[]) null));
    t("nullargs0", () -> v.invoke(o, (Object[]) null));
    t("nullinst", () -> s.invoke(null, "x"));
    t("wronginst", () -> s.invoke("str", "x"));
    t("staticnull", () -> st.invoke(null, "x"));
    t("staticany", () -> st.invoke("zzz", "x"));
    t("staticwrongtype", () -> st.invoke(null, 7));
    t("nullstring", () -> s.invoke(o, (Object) null));
    t("primint", () -> i.invoke(o, 5));
    t("primintstr", () -> i.invoke(o, "5"));
    t("primintlong", () -> i.invoke(o, 5L));
    t("primintshort", () -> i.invoke(o, (short) 5));
    t("primintbyte", () -> i.invoke(o, (byte) 5));
    t("primintchar", () -> i.invoke(o, 'a'));
    t("primintdouble", () -> i.invoke(o, 5.0));
    t("primlongint", () -> l.invoke(o, 5));
    t("primlongfloat", () -> l.invoke(o, 5f));
    t("primdoubleint", () -> d.invoke(o, 5));
    t("primdoublefloat", () -> d.invoke(o, 5f));
    t("primdoublelong", () -> d.invoke(o, 5L));
    t("primdoublestr", () -> d.invoke(o, "x"));
    t("boolwrong", () -> z.invoke(o, 1));
    t("boolok", () -> z.invoke(o, true));
    t("charok", () -> c.invoke(o, 'q'));
    t("charint", () -> c.invoke(o, 5));
    t("bytes", () -> b.invoke(o, (byte) 1) + " " + sh.invoke(o, (byte) 1));
    t("shortint", () -> sh.invoke(o, 1));
    t("floatint", () -> fl.invoke(o, 1) + " " + fl.invoke(o, 1L) + " " + fl.invoke(o, 1.5));
    t("object", () -> oo.invoke(o, 5));
    t("objectnull", () -> oo.invoke(o, (Object) null));
    t("number", () -> num.invoke(o, 5) + " " + num.invoke(o, 5.5));
    t("numberbad", () -> num.invoke(o, "x"));
    t("two", () -> two.invoke(o, 1, "z"));
    t("twoswap", () -> two.invoke(o, "z", 1));
    t("vsok", () -> vs.invoke(o, (Object) new String[] {"a", "b"}));
    t("vsbad", () -> vs.invoke(o, "a", "b"));
    t("vsbad2", () -> vs.invoke(o, new Object[] {new Object[] {"a"}}));
    t("void", () -> v.invoke(o));
    t("voidwrong", () -> v.invoke(o, 1));
    Constructor<T> c0 = T.class.getConstructor(), c1 = T.class.getConstructor(int.class), c2 = T.class.getConstructor(String.class), c3 = T.class.getConstructor(int.class, String.class);
    t("newok", () -> c1.newInstance(5).f);
    t("newwrongtype", () -> c1.newInstance("x"));
    t("newwrongcount", () -> c1.newInstance());
    t("newwrongcount2", () -> c1.newInstance(1, 2));
    t("newnullargs", () -> c0.newInstance((Object[]) null).f);
    t("newnullargs1", () -> c1.newInstance((Object[]) null));
    t("newstrnull", () -> c2.newInstance((Object) null).sf);
    t("newwiden", () -> c1.newInstance((short) 4).f);
    t("newtwo", () -> c3.newInstance(1, "b").sf);
    t("newtwoswap", () -> c3.newInstance("b", 1).sf);
    Field f = T.class.getField("f"), lf = T.class.getField("lf"), sf = T.class.getField("sf"), sx = T.class.getField("sx"), fin = T.class.getField("fin"), boxed = T.class.getField("boxed");
    t("fget", () -> f.get(o));
    t("fgetnull", () -> f.get(null));
    t("fgetwrong", () -> f.get("zzz"));
    t("fsetstr", () -> { f.set(o, "x"); return "ok"; });
    t("fsetnull", () -> { f.set(o, null); return "ok"; });
    t("fsetshort", () -> { f.set(o, (short) 3); return f.get(o); });
    t("fsetlong", () -> { f.set(o, 3L); return f.get(o); });
    t("lfsetint", () -> { lf.set(o, 3); return lf.get(o); });
    t("sfsetint", () -> { sf.set(o, 3); return "ok"; });
    t("sfsetnull", () -> { sf.set(o, null); return sf.get(o); });
    t("sxget", () -> sx.get(null) + " " + sx.get("any"));
    t("sxset", () -> { sx.set(null, 9); return sx.get(null); });
    t("finset", () -> { fin.set(o, 9); return "ok"; });
    t("boxedset", () -> { boxed.set(o, 5); return boxed.get(o); });
    t("boxedsetlong", () -> { boxed.set(o, 5L); return "ok"; });
    t("getint", () -> f.getInt(o) + " " + f.getLong(o) + " " + f.getDouble(o));
    t("getintbad", () -> sf.getInt(o));
    t("setint", () -> { f.setInt(o, 5); return f.get(o); });
    t("setintbad", () -> { sf.setInt(o, 5); return "ok"; });
    t("setdouble", () -> { f.setDouble(o, 5.5); return "ok"; });
    t("getlongfromint", () -> lf.getInt(o));
  }
}
