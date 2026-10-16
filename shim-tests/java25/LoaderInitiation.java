/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class LoaderInitiation {
  static int initialized;
  public static class Target { static { ++initialized; } }
  public static class User { public static Class<?> target() { return Target.class; } }
  public static class Child extends Target { }
  public static class Δ { }
  public static class 𐐀 { }
  public static class UnicodeUser {
    public static Class<?> bmp() { return Δ.class; }
    public static Class<?> supplementary() { return 𐐀.class; }
  }
  private static class Missing extends ClassNotFoundException { }
  private static class Delegating extends ClassLoader {
    Delegating(ClassLoader parent) { super(parent); }
    Class<?> seen(String name) { return findLoadedClass(name); }
  }

  private static byte[] bytes(Class<?> type) throws Exception {
    InputStream input = LoaderInitiation.class.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[256];
    for (int n; (n = input.read(buffer)) != -1;) output.write(buffer, 0, n);
    input.close();
    return output.toByteArray();
  }

  private static class Loader extends ClassLoader {
    final String requested;
    Class<?> answer;
    int calls;
    Throwable failure;
    Loader(String requested, Class<?> answer) {
      super(LoaderInitiation.class.getClassLoader());
      this.requested = requested; this.answer = answer;
    }
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      if (name.equals(requested)) {
        ++calls;
        if (failure instanceof RuntimeException) throw (RuntimeException) failure;
        if (failure instanceof Error) throw (Error) failure;
        if (failure instanceof ClassNotFoundException) throw (ClassNotFoundException) failure;
        return answer;
      }
      return super.loadClass(name);
    }
    Class<?> seen(String name) { return findLoadedClass(name); }
    Class<?> define(Class<?> source) throws Exception {
      byte[] data = bytes(source);
      return defineClass(source.getName(), data, 0, data.length);
    }
  }

  private static void lookup(String label, String name, Class<?> answer) {
    Loader loader = new Loader(name, answer);
    try {
      Class<?> type = Class.forName(name, false, loader);
      System.out.println(label + ":returned:" + type.getName());
    } catch (Throwable error) { System.out.println(label + ":" + error.getClass().getSimpleName()); }
    System.out.println(label + ":requested-recorded:" + (loader.seen(name) != null));
    if (answer != null) System.out.println(label + ":answer-recorded:" + (loader.seen(answer.getName()) != null));
  }

  public static void main(String[] args) throws Exception {
    lookup("wrong", "realm.Requested", String.class);
    lookup("null", "realm.Requested", null);
    lookup("embedded-nul", "java.lang.String\0Other", String.class);
    lookup("primitive", "int", int.class);
    lookup("primitive-answer", "realm.Requested", int.class);
    lookup("array-answer", "realm.Requested", String[].class);
    lookup("matching", Target.class.getName(), Target.class);
    Loader cache = new Loader(Target.class.getName(), Target.class);
    Class<?> first = Class.forName(Target.class.getName(), false, cache);
    cache.answer = String.class;
    Class<?> second = Class.forName(Target.class.getName(), false, cache);
    System.out.println("cached-identity:" + (first == second));
    System.out.println("cached-calls:" + cache.calls);
    System.out.println("not-initialized:" + (initialized == 0));
    Class.forName(Target.class.getName(), true, cache);
    System.out.println("initialized-once:" + (initialized == 1));
    try { cache.define(Target.class); System.out.println("redefine-initiated:accepted"); }
    catch (Throwable error) { System.out.println("redefine-initiated:" + error.getClass().getSimpleName()); }
    System.out.println("initiated-preserved:" + (cache.seen(Target.class.getName()) == first));
    Loader direct = new Loader("realm.Requested", String.class);
    System.out.println("direct-result:" + direct.loadClass("realm.Requested").getName());
    System.out.println("direct-unrecorded:" + (direct.seen("java.lang.String") == null));
    Loader valid = new Loader(Target.class.getName(), Target.class);
    Class<?> user = valid.define(User.class);
    System.out.println("symbolic-identity:" + (user.getMethod("target").invoke(null) == Target.class));
    System.out.println("symbolic-recorded:" + (valid.seen(Target.class.getName()) == Target.class));
    Loader invalid = new Loader(Target.class.getName(), String.class);
    Class<?> bad = invalid.define(User.class);
    try {
      System.out.println("symbolic-wrong:" + ((Class<?>) bad.getMethod("target").invoke(null)).getName());
    } catch (InvocationTargetException error) {
      System.out.println("symbolic-wrong:" + error.getCause().getClass().getSimpleName());
    }
    System.out.println("symbolic-wrong-unpublished:" + (invalid.seen(Target.class.getName()) == null));
    System.out.println("symbolic-unrelated-unpublished:" + (invalid.seen("java.lang.String") == null));
    for (Throwable failure : new Throwable[] {new Missing(), new IllegalStateException(), new LinkageError()}) {
      Loader failing = new Loader(Target.class.getName(), null);
      failing.failure = failure;
      try { Class.forName(Target.class.getName(), false, failing); System.out.println("failure:accepted"); }
      catch (Throwable error) { System.out.println("failure:" + error.getClass().getSimpleName()); }
      Class<?> symbolic = failing.define(User.class);
      try { symbolic.getMethod("target").invoke(null); System.out.println("symbolic-failure:accepted"); }
      catch (InvocationTargetException error) {
        System.out.println("symbolic-failure:" + error.getCause().getClass().getSimpleName());
      }
    }
    for (Class<?> target : new Class<?>[] {Δ.class, 𐐀.class}) {
      Loader unicode = new Loader(target.getName(), target);
      System.out.println("unicode-for-name:" + (Class.forName(target.getName(), false, unicode) == target));
      System.out.println("unicode-recorded:" + (unicode.seen(target.getName()) == target));
      String arrayName = "[[L" + target.getName() + ";";
      Class<?> array = Class.forName(arrayName, false, unicode);
      System.out.println("unicode-array:" + (array.getComponentType().getComponentType() == target));
      Loader symbolicLoader = new Loader(target.getName(), target);
      Class<?> symbolic = symbolicLoader.define(UnicodeUser.class);
      String method = target == Δ.class ? "bmp" : "supplementary";
      System.out.println("unicode-symbolic:" + (symbolic.getMethod(method).invoke(null) == target));
    }
    Loader origin = new Loader(Target.class.getName(), Target.class);
    Delegating middle = new Delegating(origin), outer = new Delegating(middle);
    System.out.println("delegated-identity:" + (Class.forName(Target.class.getName(), false, outer) == Target.class));
    System.out.println("outer-initiated:" + (outer.seen(Target.class.getName()) == Target.class));
    System.out.println("middle-unrecorded:" + (middle.seen(Target.class.getName()) == null));
    System.out.println("origin-unrecorded:" + (origin.seen(Target.class.getName()) == null));
    Loader parent = new Loader(Target.class.getName(), String.class);
    try { parent.define(Child.class); System.out.println("wrong-super:accepted"); }
    catch (Throwable error) { System.out.println("wrong-super:" + error.getClass().getSimpleName()); }
  }
}
