/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReflectionLoading {
  public static class Missing { }
  public static class Methods {
    public int field;
    public Missing method(Missing value) { return value; }
  }
  public static class Fields {
    public Missing field;
    public int method() { return 1; }
  }
  public static class Constructors {
    public int field;
    public Constructors(Missing value) { }
    public int method() { return 1; }
  }
  public static class Parent { public Missing inherited(Missing value) { return value; } }
  public static class Child extends Parent { public int local() { return 1; } }

  private static class Loader extends ClassLoader {
    int blocked;
    Loader() { super(ReflectionLoading.class.getClassLoader()); }
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      synchronized (this) {
        if (name.equals(Missing.class.getName())) { ++blocked; throw new ClassNotFoundException(name); }
        Class<?> type = findLoadedClass(name);
        if (type == null && name.startsWith("ReflectionLoading$") && !name.endsWith("Loader")) {
          try {
            InputStream input = ReflectionLoading.class.getResourceAsStream("/" + name.replace('.', '/') + ".class");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            for (int n; (n = input.read(buffer)) != -1;) output.write(buffer, 0, n);
            input.close(); byte[] bytes = output.toByteArray();
            type = defineClass(name, bytes, 0, bytes.length);
          } catch (java.io.IOException error) { throw new ClassNotFoundException(name, error); }
        }
        if (type == null) type = super.loadClass(name, false);
        if (resolve) resolveClass(type);
        return type;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    for (Class<?> source : new Class<?>[] {Methods.class, Fields.class, Constructors.class, Child.class}) {
      for (String operation : new String[] {"methods", "fields", "constructors"}) {
        Loader loader = new Loader();
        String label = source.getSimpleName() + ":" + operation;
        try {
          Class<?> type = Class.forName(source.getName(), false, loader);
          System.out.println(label + ":loaded-without-signatures:" + (loader.blocked == 0));
          if (operation.equals("methods")) System.out.println(label + ":count:" + type.getDeclaredMethods().length);
          else if (operation.equals("fields")) System.out.println(label + ":count:" + type.getDeclaredFields().length);
          else System.out.println(label + ":count:" + type.getDeclaredConstructors().length);
        } catch (Throwable error) { System.out.println(label + ":" + error.getClass().getSimpleName()); }
        System.out.println(label + ":requested-missing:" + (loader.blocked != 0));
      }
    }
  }
}
