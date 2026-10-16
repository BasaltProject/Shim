/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class LoadingConstraints {
  public static class Type { }
  public static class Other { }
  public interface Contract { Type method(Type value); }
  public static class Implementation implements Contract {
    public Type method(Type value) { return value; }
  }
  public static class Library {
    public static Type field;
    public static char wide = '\uffff';
    public static Contract contract = new Implementation();
    public static Type[][] matrix;
    public Library() { }
    public Library(Type value) { }
    public Type instance(Type value) { return value; }
    public static Type multi(Type value, Other other) { return value; }
    public static Type method(Type value) { return value; }
    public static Type[][] array(Type[][] value) { return value; }
    public static int primitive(int value) { return value + 1; }
  }
  public static class Library2 {
    public static Type method(Type value) { return value; }
  }
  public static class Sub extends Library { }
  public static class Caller {
    public static Object method() { return Library.method(null); }
    public static Object skip() { return choose(false); }
    public static Object choose(boolean call) { return call ? Library.method(null) : null; }
    public static Object cast() { return castValue(null); }
    public static Object test() { return testValue(null); }
    public static Object castValue(Object value) { return (Type) value; }
    public static boolean testValue(Object value) { return value instanceof Type; }
    public static Object second() { return Library2.method(null); }
    public static Object contract() { return Library.contract.method(null); }
    public static Object inherited() { return Sub.method(null); }
    public static Object instance() { return new Library().instance(null); }
    public static Object constructor() { return new Library((Type) null); }
    public static Object multi() { return Library.multi(null, null); }
    public static Object matrix() { return Library.matrix; }
    public static Object array() { return Library.array(null); }
    public static Object field() { return Library.field; }
    public static int charField() { return Library.wide; }
    public static int primitive() { return Library.primitive(3); }
  }

  private static class Loader extends ClassLoader {
    final Class<?> provided;
    Loader secondLibrary;
    boolean delegateType;
    Loader(ClassLoader parent, boolean library) { this(parent, library ? Library.class : Caller.class); }
    Loader(ClassLoader parent, Class<?> provided) { super(parent); this.provided = provided; }
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      synchronized (this) {
        Class<?> type = findLoadedClass(name);
        boolean local = (!delegateType && name.equals(Type.class.getName()))
            || name.equals(Other.class.getName()) || name.equals(provided.getName())
            || (provided == Library.class && (name.equals(Contract.class.getName())
                || name.equals(Implementation.class.getName())))
            || (provided == Caller.class && name.equals(Sub.class.getName()));
        if (type == null && secondLibrary != null && name.equals(Library2.class.getName())) {
          type = secondLibrary.loadClass(name);
        }
        if (type == null && local) {
          try {
            InputStream input = LoadingConstraints.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            for (int n; (n = input.read(buffer)) != -1;) output.write(buffer, 0, n);
            input.close();
            byte[] bytes = output.toByteArray();
            type = defineClass(name, bytes, 0, bytes.length);
          } catch (java.io.IOException error) { throw new ClassNotFoundException(name, error); }
        }
        if (type == null) type = super.loadClass(name, false);
        if (resolve) resolveClass(type);
        return type;
      }
    }
    Class<?> seen() { return findLoadedClass(Type.class.getName()); }
  }

  private static void invoke(String label, Class<?> caller, String method) throws Exception {
    try {
      Object result = caller.getMethod(method).invoke(null);
      System.out.println(label + ":" + (result == null || result instanceof Number || result instanceof Boolean ? result : result.getClass().getName()));
    }
    catch (InvocationTargetException error) { System.out.println(label + ":" + error.getCause().getClass().getSimpleName()); }
  }

  private static void load(String label, Loader loader) {
    try { Class.forName(Type.class.getName(), false, loader); System.out.println(label + ":loaded"); }
    catch (Throwable error) { System.out.println(label + ":" + error.getClass().getSimpleName()); }
  }

  public static void main(String[] args) throws Exception {
    for (String operation : new String[] {"method", "array", "field", "matrix", "inherited", "instance", "constructor", "contract", "cast", "test", "skip", "charField", "primitive"}) {
      Loader parent = new Loader(LoadingConstraints.class.getClassLoader(), true);
      Loader child = new Loader(parent, false);
      Class<?> caller = Class.forName(Caller.class.getName(), false, child);
      Class<?> parentType = Class.forName(Type.class.getName(), false, parent);
      Class<?> childType = Class.forName(Type.class.getName(), false, child);
      System.out.println(operation + ":distinct:" + (parentType != childType));
      invoke(operation + ":preloaded", caller, operation);
    }
    for (String operation : new String[] {"method", "array", "field", "matrix", "inherited", "instance", "constructor", "contract"}) {
      for (boolean parentFirst : new boolean[] {true, false}) {
        Loader parent = new Loader(LoadingConstraints.class.getClassLoader(), true);
        Loader child = new Loader(parent, false);
        Class<?> caller = Class.forName(Caller.class.getName(), false, child);
        invoke(operation + ":deferred", caller, operation);
        System.out.println(operation + ":unloaded:" + (parent.seen() == null && child.seen() == null));
        Loader first = parentFirst ? parent : child, second = parentFirst ? child : parent;
        load(operation + ":first", first);
        load(operation + ":second", second);
        System.out.println(operation + ":unpublished:" + (second.seen() == null));
        System.out.println(operation + ":first-retained:" + (first.seen() != null));
      }
    }
    for (String operation : new String[] {"cast", "test", "skip"}) {
      Loader p = new Loader(LoadingConstraints.class.getClassLoader(), true);
      Loader c = new Loader(p, false);
      Class<?> type = Class.forName(Caller.class.getName(), false, c);
      invoke(operation + ":null", type, operation);
      System.out.println(operation + ":unloaded:" + (p.seen() == null && c.seen() == null));
      load(operation + ":parent", p);
      load(operation + ":child", c);
    }
    Loader parent = new Loader(LoadingConstraints.class.getClassLoader(), true);
    Loader child = new Loader(parent, false);
    Class<?> caller = Class.forName(Caller.class.getName(), false, child);
    Class.forName(Other.class.getName(), false, parent);
    Class.forName(Other.class.getName(), false, child);
    invoke("signature-conflict", caller, "multi");
    load("signature-parent", parent);
    load("signature-child", child);
    Loader first = new Loader(LoadingConstraints.class.getClassLoader(), Library.class);
    Loader second = new Loader(LoadingConstraints.class.getClassLoader(), Library2.class);
    Loader third = new Loader(first, false);
    third.secondLibrary = second;
    Class<?> bridge = Class.forName(Caller.class.getName(), false, third);
    invoke("transitive-first", bridge, "method");
    invoke("transitive-second", bridge, "second");
    load("transitive-bind", first);
    load("transitive-conflict", second);
    load("transitive-caller-conflict", third);
    Loader matching = new Loader(first, false);
    matching.delegateType = true;
    Class<?> matchingCaller = Class.forName(Caller.class.getName(), false, matching);
    Class<?> matchingType = Class.forName(Type.class.getName(), false, matching);
    System.out.println("matching-identity:" + (matchingType == first.seen()));
    invoke("matching-call", matchingCaller, "method");
  }
}
