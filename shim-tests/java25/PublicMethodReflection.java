/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class PublicMethodReflection {
  public interface Root {
    Object value();
    default String greeting() { return "hello"; }
    static void rootStatic() { }
  }
  public interface Left extends Root { }
  public interface Right extends Root { }
  public interface Diamond extends Left, Right { }
  public interface Narrow extends Root { String value(); }
  public interface Separate { Object value(); }
  public interface Repeated extends Root, Separate { }
  public abstract static class Abstract implements Diamond { }
  public static class Parent {
    public Object value() { return "parent"; }
    public static void shared() { }
    public void inherited(int x) { }
    private void secret() { }
    protected void protectedMethod() { }
    void packageMethod() { }
  }
  public static class Child extends Parent implements Narrow, Diamond {
    public String value() { return "child"; }
    public static void shared() { }
    public void inherited(String x) { }
    public void array(String[] x) { }
    public void array(String[][] x) { }
    public void array(Object x) { }
  }
  public static class ClassWins extends Parent implements Root { }
  public interface Empty { }
  public static class MissingOptionalType { }
  public static class OptionalApi {
    private MissingOptionalType implementation() { return null; }
    public static String available() { return "available"; }
  }

  private static void optionalPrivateSignature() throws Exception {
    String apiName = OptionalApi.class.getName();
    String missingName = MissingOptionalType.class.getName();
    byte[] bytes;
    try (java.io.InputStream in = PublicMethodReflection.class.getResourceAsStream(
        "/" + apiName.replace('.', '/') + ".class")) {
      bytes = in.readAllBytes();
    }
    ClassLoader loader = new ClassLoader(PublicMethodReflection.class.getClassLoader()) {
      protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals(missingName)) throw new ClassNotFoundException(name);
        if (!name.equals(apiName)) return super.loadClass(name, resolve);
        Class<?> type = findLoadedClass(name);
        if (type == null) type = defineClass(name, bytes, 0, bytes.length);
        if (resolve) resolveClass(type);
        return type;
      }
    };
    Class<?> api = Class.forName(apiName, false, loader);
    System.out.println("optional public method: " + api.getMethod("available").invoke(null));
    for (Method method : api.getMethods()) {
      if (!Modifier.isPublic(method.getModifiers())) throw new AssertionError(method);
    }
    try {
      api.getDeclaredMethods();
      throw new AssertionError("Declared reflection must still resolve private signatures");
    } catch (NoClassDefFoundError expected) {
      System.out.println("optional private signature remains unresolved");
    }
  }

  private static String identity(Method method) {
    return method.getDeclaringClass().getName() + ":" + method.getName()
      + Arrays.toString(method.getParameterTypes()) + ":" + method.getReturnType().getName()
      + ":" + (method.getModifiers() & ~Modifier.NATIVE);
  }

  private static void dump(Class<?> type, boolean declared) {
    Method[] methods = declared ? type.getDeclaredMethods() : type.getMethods();
    String[] names = new String[methods.length];
    for (int i = 0; i < methods.length; ++i) names[i] = identity(methods[i]);
    Arrays.sort(names);
    System.out.println(type.getName() + ":" + declared + ":" + Arrays.toString(names));
  }

  private static void lookup(Class<?> type, String name, boolean declared, Class<?>... parameters) {
    try {
      Method method = declared ? type.getDeclaredMethod(name, parameters) : type.getMethod(name, parameters);
      String result = identity(method);
      if (type == Repeated.class && name.equals("value") && !declared) {
        Class<?> owner = method.getDeclaringClass();
        if ((owner != Root.class && owner != Separate.class) || method.getReturnType() != Object.class) {
          throw new AssertionError(result);
        }
        result = "unrelated public interface method";
      }
      System.out.println(type.getName() + ":" + name + ":" + declared + ":" + result);
    } catch (NoSuchMethodException e) {
      System.out.println(type.getName() + ":" + name + ":" + declared + ":missing");
    }
  }

  public static void main(String[] args) throws Exception {
    optionalPrivateSignature();
    Class<?>[] types = {Root.class, Left.class, Diamond.class, Narrow.class, Repeated.class,
      Abstract.class, Parent.class, Child.class, ClassWins.class, Empty.class,
      int.class, void.class, String[].class, int[][].class};
    for (Class<?> type : types) {
      dump(type, false);
      dump(type, true);
      lookup(type, "value", false);
      lookup(type, "value", true);
      lookup(type, "rootStatic", false);
      lookup(type, "greeting", true);
    }
    for (String name : new String[] {"secret", "protectedMethod", "packageMethod", "shared", "greeting",
        "toString", "clone", "<init>", "<clinit>"}) {
      lookup(Child.class, name, false);
      lookup(Parent.class, name, true);
    }
    lookup(Child.class, "inherited", false, int.class);
    lookup(Child.class, "inherited", false, String.class);
    lookup(Child.class, "inherited", true, int.class);
    lookup(Child.class, "value", false, (Class<?>[]) null);
    lookup(Child.class, "value", false, new Class<?>[] {null});
    for (boolean declared : new boolean[] {false, true}) {
      for (Class<?> parameter : new Class<?>[] {null, Object.class, String.class, String[].class,
          String[][].class, Object[].class, int[].class}) {
        lookup(Child.class, "array", declared, new Class<?>[] {parameter});
        lookup(Child.class, "inherited", declared, new Class<?>[] {parameter});
      }
    }
    System.out.println(Child.class.getMethod("value").invoke(new Child()));
    System.out.println(Child.class.getMethod("greeting").invoke(new Child()));
    for (boolean declared : new boolean[] {false, true}) {
      try {
        if (declared) Child.class.getDeclaredMethod(null); else Child.class.getMethod(null);
        throw new AssertionError();
      } catch (NullPointerException expected) { System.out.println("null name rejected"); }
    }
  }
}
