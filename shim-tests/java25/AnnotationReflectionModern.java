/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

public class AnnotationReflectionModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  enum Color { RED, GREEN }

  @Retention(RetentionPolicy.RUNTIME) @interface Inner { String v() default "in"; }

  @Retention(RetentionPolicy.RUNTIME) @interface Everything {
    byte b() default 1; short s() default 2; int i() default 3; long l() default 4L; float f() default 5.5f; double d() default 6.5;
    char c() default 'c'; boolean z() default true; String str() default "str\"q'\n"; Color color() default Color.GREEN;
    Class<?> cls() default String.class; Class<?>[] clss() default {int.class, int[].class, Map.Entry.class};
    int[] ints() default {1, 2}; String[] strs() default {}; Color[] colors() default {Color.RED};
    Inner inner() default @Inner; Inner[] inners() default {@Inner(v = "a"), @Inner};
    long[] longs() default {7L}; double[] doubles() default {1.5}; char[] chars() default {'x'}; boolean[] bools() default {false, true}; byte[] bytes() default {9}; float[] floats() default {2.5f}; short[] shorts() default {3};
  }

  @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR}) @interface Plain { int value(); }
  @Retention(RetentionPolicy.RUNTIME) @interface Two { int a(); String b() default "bb"; }
  @Retention(RetentionPolicy.RUNTIME) @interface Marker { }
  @Retention(RetentionPolicy.CLASS) @interface ClassOnly { }
  @Retention(RetentionPolicy.SOURCE) @interface SourceOnly { }
  @Retention(RetentionPolicy.RUNTIME) @Inherited @interface Inh { String value() default "i"; }
  @Retention(RetentionPolicy.RUNTIME) @Repeatable(Reps.class) @interface Rep { String value(); }
  @Retention(RetentionPolicy.RUNTIME) @Inherited @interface Reps { Rep[] value(); }
  @Retention(RetentionPolicy.RUNTIME) @Repeatable(NonInhReps.class) @interface Nr { int value(); }
  @Retention(RetentionPolicy.RUNTIME) @interface NonInhReps { Nr[] value(); }

  @Everything @Plain(1) @Two(a = 5) @Marker @ClassOnly @SourceOnly @Inh("base") @Rep("r1") @Rep("r2") @Nr(1) @Nr(2)
  static class Base {
    @Plain(2) public int field;
    @Plain(3) @Marker public void method(@Plain(4) int p, String q, @Marker @Plain(5) long r) { }
    @Plain(6) public Base() { }
    @Plain(7) public Base(int x) { }
    @Rep("m") public void single() { }
    @Rep("m1") @Rep("m2") public void multi() { }
  }

  static class Derived extends Base { }
  @Inh("derived") static class Derived2 extends Base { }
  @Rep("own") static class Derived3 extends Base { }
  static class Derived4 extends Derived { }
  interface Iface { }
  @Inh("iface") interface Ifaced { }
  static class Impl implements Ifaced { }

  static String names(Annotation[] a) { List<String> l = new ArrayList<>(); for (Annotation x : a) l.add(x.annotationType().getSimpleName()); return l.toString(); }

  public static void main(String[] args) throws Exception {
    probe("base-annotations", () -> names(Base.class.getAnnotations()));
    probe("base-declared", () -> names(Base.class.getDeclaredAnnotations()));
    probe("derived", () -> names(Derived.class.getAnnotations()) + " " + names(Derived.class.getDeclaredAnnotations()));
    probe("derived2", () -> names(Derived2.class.getAnnotations()) + " " + Derived2.class.getAnnotation(Inh.class).value() + " " + names(Derived2.class.getDeclaredAnnotations()));
    probe("derived3", () -> names(Derived3.class.getAnnotations()) + " " + Derived3.class.getAnnotationsByType(Rep.class).length + " " + Derived3.class.getAnnotation(Reps.class));
    probe("derived4", () -> names(Derived4.class.getAnnotations()) + " " + Derived4.class.isAnnotationPresent(Inh.class) + " " + Derived4.class.isAnnotationPresent(Plain.class) + " " + Derived4.class.getDeclaredAnnotation(Inh.class));
    probe("interface-not-inherited", () -> names(Impl.class.getAnnotations()) + " " + Impl.class.isAnnotationPresent(Inh.class));
    probe("by-type-rep", () -> { Rep[] a = Base.class.getAnnotationsByType(Rep.class); return a.length + " " + a[0].value() + " " + a[1].value() + " " + Base.class.getDeclaredAnnotationsByType(Rep.class).length + " " + Derived.class.getAnnotationsByType(Rep.class).length + " " + Derived.class.getDeclaredAnnotationsByType(Rep.class).length; });
    probe("by-type-noninherited", () -> Base.class.getAnnotationsByType(Nr.class).length + " " + Derived.class.getAnnotationsByType(Nr.class).length + " " + Base.class.getAnnotation(Nr.class) + " " + names(new Annotation[] {Base.class.getAnnotation(NonInhReps.class)}));
    probe("by-type-single", () -> Base.class.getAnnotationsByType(Plain.class).length + " " + Base.class.getAnnotationsByType(Marker.class).length + " " + Base.class.getAnnotationsByType(ClassOnly.class).length + " " + Base.class.getAnnotationsByType(Inherited.class).length);
    probe("by-type-array-class", () -> Base.class.getAnnotationsByType(Rep.class).getClass().getSimpleName());
    probe("runtime-only", () -> Base.class.isAnnotationPresent(ClassOnly.class) + " " + Base.class.isAnnotationPresent(SourceOnly.class) + " " + Base.class.isAnnotationPresent(Marker.class));
    probe("method-annotations", () -> { try { Method m = Base.class.getMethod("method", int.class, String.class, long.class); return names(m.getAnnotations()) + " " + names(m.getDeclaredAnnotations()) + " " + m.getAnnotation(Plain.class).value() + " " + m.isAnnotationPresent(Marker.class); } catch (NoSuchMethodException e) { return e; } });
    probe("method-params", () -> { try { Method m = Base.class.getMethod("method", int.class, String.class, long.class); Annotation[][] pa = m.getParameterAnnotations(); StringBuilder sb = new StringBuilder(); for (Annotation[] a : pa) sb.append(names(a)); Parameter[] ps = m.getParameters(); return sb + " " + names(ps[0].getAnnotations()) + " " + names(ps[1].getAnnotations()) + " " + ps[2].getAnnotation(Plain.class).value() + " " + ps[0].getAnnotationsByType(Plain.class).length; } catch (NoSuchMethodException e) { return e; } });
    probe("field-annotations", () -> { try { Field f = Base.class.getField("field"); return names(f.getAnnotations()) + " " + f.getAnnotation(Plain.class).value() + " " + f.getDeclaredAnnotation(Plain.class).value() + " " + f.getAnnotationsByType(Plain.class).length; } catch (NoSuchFieldException e) { return e; } });
    probe("ctor-annotations", () -> { try { return names(Base.class.getConstructor().getAnnotations()) + " " + Base.class.getConstructor(int.class).getAnnotation(Plain.class).value(); } catch (NoSuchMethodException e) { return e; } });
    probe("method-repeatable", () -> { try { Method s = Base.class.getMethod("single"); Method m = Base.class.getMethod("multi"); return names(s.getAnnotations()) + " " + s.getAnnotationsByType(Rep.class).length + " " + names(m.getAnnotations()) + " " + m.getAnnotationsByType(Rep.class).length + " " + m.getAnnotationsByType(Rep.class)[1].value() + " " + m.getAnnotation(Rep.class) + " " + m.getDeclaredAnnotationsByType(Rep.class).length; } catch (NoSuchMethodException e) { return e; } });
    probe("to-string", () -> Base.class.getAnnotation(Plain.class) + " | " + Base.class.getAnnotation(Two.class) + " | " + Base.class.getAnnotation(Marker.class) + " | " + Base.class.getAnnotation(Inh.class));
    probe("everything-to-string", () -> Base.class.getAnnotation(Everything.class).toString());
    probe("everything-values", () -> { Everything e = Base.class.getAnnotation(Everything.class); return e.b() + " " + e.s() + " " + e.i() + " " + e.l() + " " + e.f() + " " + e.d() + " " + e.c() + " " + e.z() + " " + e.color() + " " + e.cls().getName() + " " + Arrays.toString(e.clss()) + " " + Arrays.toString(e.ints()) + " " + e.strs().length + " " + Arrays.toString(e.colors()) + " " + e.inner().v() + " " + e.inners().length + e.inners()[0].v() + " " + Arrays.toString(e.longs()) + Arrays.toString(e.doubles()) + Arrays.toString(e.chars()) + Arrays.toString(e.bools()) + Arrays.toString(e.bytes()) + Arrays.toString(e.floats()) + Arrays.toString(e.shorts()); });
    probe("array-types", () -> { Everything e = Base.class.getAnnotation(Everything.class); return e.ints().getClass().getSimpleName() + " " + e.strs().getClass().getSimpleName() + " " + e.colors().getClass().getSimpleName() + " " + e.inners().getClass().getSimpleName() + " " + e.clss().getClass().getSimpleName() + " " + e.bools().getClass().getSimpleName(); });
    probe("array-clone", () -> { Everything e = Base.class.getAnnotation(Everything.class); int[] a = e.ints(); a[0] = 99; return e.ints()[0] + " " + (e.ints() != e.ints()); });
    probe("equals-hash", () -> { Plain a = Base.class.getAnnotation(Plain.class); Plain b = Base.class.getDeclaredAnnotation(Plain.class); Two t = Base.class.getAnnotation(Two.class); return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(t) + " " + a.equals(null) + " " + a.equals(a) + " " + (t.hashCode() == ((127 * "a".hashCode()) ^ 5) + ((127 * "b".hashCode()) ^ "bb".hashCode())) + " " + Base.class.getAnnotation(Marker.class).hashCode(); });
    probe("equals-different-values", () -> { try { Plain one = Base.class.getMethod("method", int.class, String.class, long.class).getAnnotation(Plain.class); Plain two = Base.class.getAnnotation(Plain.class); return one.equals(two) + " " + (one.hashCode() == two.hashCode()); } catch (NoSuchMethodException e) { return e; } });
    probe("everything-equals", () -> { Everything a = Base.class.getAnnotation(Everything.class); Everything b = Base.class.getDeclaredAnnotation(Everything.class); return a.equals(b) + " " + (a.hashCode() == b.hashCode()); });
    probe("annotation-type", () -> Base.class.getAnnotation(Plain.class).annotationType().getSimpleName() + " " + Plain.class.isAnnotation() + " " + Plain.class.isInterface() + " " + Arrays.toString(Plain.class.getInterfaces()) + " " + Annotation.class.isAssignableFrom(Plain.class) + " " + (Base.class.getAnnotation(Plain.class) instanceof Annotation));
    probe("default-values", () -> { try { return Two.class.getMethod("a").getDefaultValue() + " " + Two.class.getMethod("b").getDefaultValue() + " " + Arrays.toString((int[]) Everything.class.getMethod("ints").getDefaultValue()) + " " + Everything.class.getMethod("inner").getDefaultValue() + " " + Everything.class.getMethod("color").getDefaultValue(); } catch (Exception e) { return e; } });
    probe("meta", () -> names(Plain.class.getAnnotations()) + " " + Plain.class.getAnnotation(Retention.class).value() + " " + Arrays.toString(Plain.class.getAnnotation(Target.class).value()) + " " + names(Inh.class.getAnnotations()) + " " + Rep.class.getAnnotation(Repeatable.class).value().getSimpleName() + " " + names(Reps.class.getAnnotations()));
    probe("target-values", () -> Arrays.toString(Plain.class.getAnnotation(Target.class).value()));
    probe("null-arg", () -> Base.class.getAnnotation(null));
    probe("null-arg2", () -> Base.class.getAnnotationsByType(null));
    probe("null-arg3", () -> Base.class.getDeclaredAnnotation(null));
    probe("no-annotations", () -> names(Iface.class.getAnnotations()) + " " + names(Object.class.getAnnotations()) + " " + Object.class.getAnnotation(Plain.class) + " " + names(int.class.getAnnotations()) + " " + names(int[].class.getAnnotations()));
    probe("proxy-object-methods", () -> { Runnable r = (Runnable) Proxy.newProxyInstance(Runnable.class.getClassLoader(), new Class<?>[] {Runnable.class}, (p, m, a) -> { switch (m.getName()) { case "toString": return "PX"; case "hashCode": return 42; case "equals": return a[0] == p; default: return null; } }); return r + " " + r.hashCode() + " " + r.equals(r) + " " + r.equals("x"); });
    probe("proxy-object-methods-seen", () -> { List<String> seen = new ArrayList<>(); Runnable r = (Runnable) Proxy.newProxyInstance(Runnable.class.getClassLoader(), new Class<?>[] {Runnable.class}, (p, m, a) -> { seen.add(m.getDeclaringClass().getSimpleName() + "." + m.getName()); return m.getName().equals("hashCode") ? Integer.valueOf(1) : m.getName().equals("equals") ? Boolean.FALSE : m.getName().equals("toString") ? "s" : null; }); r.run(); r.hashCode(); r.equals(r); r.toString(); return seen; });
    probe("proxy-two-interfaces", () -> { Object p = Proxy.newProxyInstance(Runnable.class.getClassLoader(), new Class<?>[] {Runnable.class, Comparable.class, java.io.Serializable.class}, (px, m, a) -> m.getName().equals("compareTo") ? 7 : m.getName().equals("hashCode") ? 3 : m.getName().equals("toString") ? "multi" : null); return ((Comparable<Object>) p).compareTo("x") + " " + p + " " + p.hashCode(); });
  }
}
