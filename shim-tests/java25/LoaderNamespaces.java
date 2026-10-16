/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class LoaderNamespaces {
  public static int initialized;
  private static class Pool {
    final ArrayList<byte[]> entries = new ArrayList<byte[]>();
    int utf(String value) throws Exception {
      ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(b);
      out.writeByte(1); out.writeUTF(value); entries.add(b.toByteArray()); return entries.size();
    }
    int field(int owner, String name, String descriptor) throws Exception {
      int nameIndex = utf(name), specIndex = utf(descriptor);
      ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(b);
      out.writeByte(12); out.writeShort(nameIndex); out.writeShort(specIndex);
      entries.add(b.toByteArray()); int pair = entries.size();
      b = new ByteArrayOutputStream(); out = new DataOutputStream(b);
      out.writeByte(9); out.writeShort(owner); out.writeShort(pair);
      entries.add(b.toByteArray()); return entries.size();
    }
    int type(String name) throws Exception {
      int index = utf(name); ByteArrayOutputStream b = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(b); out.writeByte(7); out.writeShort(index);
      entries.add(b.toByteArray()); return entries.size();
    }
  }

  private static byte[] data(String name, boolean contract, boolean shadow) throws Exception {
    Pool pool = new Pool();
    int self = pool.type(name.replace('.', '/'));
    int parent = pool.type(name.equals("realm.Child") ? "realm/Parent" : "java/lang/Object");
    int implemented = name.equals("realm.Impl") ? pool.type("realm/Contract") : 0;
    int codeName = pool.utf("Code");
    String[] names;
    String[] specs;
    int[][] bodies;
    if (name.equals("realm.Parent")) {
      names = new String[] {"dependency", "matrix"};
      specs = new String[] {"()Lrealm/ParentOnly;", "([[Lrealm/ParentOnly;)[[Lrealm/ParentOnly;"};
      bodies = new int[][] {{1,176}, {42,176}};
    } else if (contract) {
      names = new String[] {"dependency"}; specs = new String[] {"()Lrealm/ParentOnly;"};
      bodies = new int[][] {null};
    } else if (name.equals("realm.ParentOnly")) {
      int counter = pool.field(pool.type("LoaderNamespaces"), "initialized", "I");
      names = new String[] {"id", "<clinit>"}; specs = new String[] {"()I", "()V"};
      bodies = new int[][] {{shadow ? 6 : 5,172},
          {178,counter >>> 8,counter & 255,4,96,179,counter >>> 8,counter & 255,177}};
    } else {
      names = new String[] {"leaf"}; specs = new String[] {"()I"}; bodies = new int[][] {{4,172}};
    }
    int[] nameIndices = new int[names.length], specIndices = new int[specs.length];
    for (int i = 0; i < names.length; ++i) { nameIndices[i] = pool.utf(names[i]); specIndices[i] = pool.utf(specs[i]); }
    int fieldName = pool.utf("field"), fieldSpec = pool.utf("Lrealm/ParentOnly;");
    ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(b);
    out.writeInt(0xcafebabe); out.writeShort(0); out.writeShort(69); out.writeShort(pool.entries.size() + 1);
    for (byte[] entry : pool.entries) out.write(entry);
    out.writeShort(contract ? 0x601 : 0x21); out.writeShort(self); out.writeShort(parent);
    out.writeShort(implemented == 0 ? 0 : 1); if (implemented != 0) out.writeShort(implemented);
    boolean field = name.equals("realm.Parent");
    out.writeShort(field ? 1 : 0);
    if (field) { out.writeShort(9); out.writeShort(fieldName); out.writeShort(fieldSpec); out.writeShort(0); }
    out.writeShort(names.length);
    for (int i = 0; i < names.length; ++i) {
      out.writeShort(contract ? 0x401 : (names[i].equals("<clinit>") ? 8 : 9)); out.writeShort(nameIndices[i]); out.writeShort(specIndices[i]);
      int[] code = bodies[i]; out.writeShort(code == null ? 0 : 1);
      if (code != null) {
        out.writeShort(codeName); out.writeInt(12 + code.length); out.writeShort(names[i].equals("<clinit>") ? 2 : 1);
        out.writeShort(names[i].equals("matrix") ? 1 : 0); out.writeInt(code.length);
        for (int value : code) out.writeByte(value);
        out.writeShort(0); out.writeShort(0);
      }
    }
    out.writeShort(0); return b.toByteArray();
  }

  private static class ParentLoader extends ClassLoader {
    ParentLoader() { super(LoaderNamespaces.class.getClassLoader()); }
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      if (!name.equals("realm.Parent") && !name.equals("realm.ParentOnly") && !name.equals("realm.Contract")) {
        throw new ClassNotFoundException(name);
      }
      try { byte[] bytes = data(name, name.equals("realm.Contract"), false); return defineClass(name, bytes, 0, bytes.length); }
      catch (Exception e) { throw new ClassNotFoundException(name, e); }
    }
  }

  private static class ChildLoader extends ClassLoader {
    final boolean shadow;
    int dependencyRequests;
    ChildLoader(ParentLoader parent, boolean shadow) { super(parent); this.shadow = shadow; }
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (name.equals("realm.ParentOnly")) {
        ++dependencyRequests;
        if (!shadow) throw new ClassNotFoundException("child cannot see dependency");
        Class<?> type = findLoadedClass(name);
        if (type == null) {
          try { byte[] bytes = data(name, false, true); type = defineClass(name, bytes, 0, bytes.length); }
          catch (Exception e) { throw new ClassNotFoundException(name, e); }
        }
        if (resolve) resolveClass(type);
        return type;
      }
      return super.loadClass(name, resolve);
    }
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      if (!name.equals("realm.Child") && !name.equals("realm.Impl")) throw new ClassNotFoundException(name);
      try { byte[] bytes = data(name, false, false); return defineClass(name, bytes, 0, bytes.length); }
      catch (Exception e) { throw new ClassNotFoundException(name, e); }
    }
    void resolve(Class<?> type) { resolveClass(type); }
  }

  private static void check(String label, int mode, boolean shadow) throws Exception {
    initialized = 0;
    ParentLoader parent = new ParentLoader(); ChildLoader child = new ChildLoader(parent, shadow);
    Class<?> type;
    if (mode == 0) type = child.loadClass("realm.Child");
    else if (mode == 1) type = Class.forName("realm.Parent", false, child);
    else if (mode == 2) { type = parent.loadClass("realm.Parent"); child.resolve(type); }
    else type = child.loadClass("realm.Impl");
    type.getDeclaredMethods();
    if (child.dependencyRequests != 0) throw new AssertionError(label + " used initiating loader");
    Class<?> owner = parent.loadClass(mode == 3 ? "realm.Contract" : "realm.Parent");
    Method dependency = owner.getDeclaredMethod("dependency");
    Class<?> expected = dependency.getReturnType();
    if (expected.getClassLoader() != parent || !expected.getName().equals("realm.ParentOnly")) {
      throw new AssertionError(label + " return namespace");
    }
    if (mode != 3) {
      if (owner.getDeclaredField("field").getType() != expected) throw new AssertionError("field namespace");
      Method matrix = owner.getDeclaredMethod("matrix", java.lang.reflect.Array.newInstance(expected, 0, 0).getClass());
      Class<?> result = matrix.getReturnType();
      if (result.getClassLoader() != parent || result.getComponentType().getComponentType() != expected) {
        throw new AssertionError("array namespace");
      }
    }
    Class<?> array = Class.forName("[[Lrealm.ParentOnly;", true, parent);
    if (array.getComponentType().getComponentType() != expected || initialized != 0) {
      throw new AssertionError("type queries initialized component");
    }
    if (Class.forName("[[I", true, parent).getClassLoader() != null) throw new AssertionError("primitive array loader");
    if (shadow) {
      Class<?> other = child.loadClass("realm.ParentOnly");
      if (other == expected || other.getClassLoader() != child || other.isAssignableFrom(expected)
          || expected.isAssignableFrom(other)) throw new AssertionError("loader identity");
      if (!Integer.valueOf(3).equals(other.getDeclaredMethod("id").invoke(null))
          || !Integer.valueOf(2).equals(expected.getDeclaredMethod("id").invoke(null))) {
        throw new AssertionError("shadow implementation");
      }
      if (initialized != 2 || Class.forName("[Lrealm.ParentOnly;", false, child).getComponentType() != other) {
        throw new AssertionError("shadow array identity");
      }
    } else {
      if (Class.forName("realm.ParentOnly", true, parent) != expected || initialized != 1) {
        throw new AssertionError("explicit initialization");
      }
      Class.forName("realm.ParentOnly", true, parent);
      if (initialized != 1) throw new AssertionError("initialized twice");
    }
    System.out.println(label + " accepted");
  }

  public static void main(String[] args) throws Exception {
    for (String name : new String[] {"[", "[V", "[Q", "[I;", "[L;", "[Ljava.lang.String", "[[Lrealm.ParentOnly;;", "[Ljava/lang/String;", "java/lang/String"}) {
      try { Class.forName(name, false, new ParentLoader()); throw new AssertionError("bad array accepted"); }
      catch (ClassNotFoundException expected) { System.out.println("invalid-array:" + name); }
    }
    try { Class.forName("[Lrealm.Missing;", false, new ParentLoader()); throw new AssertionError("missing array accepted"); }
    catch (ClassNotFoundException expected) { System.out.println("missing-array rejected"); }
    StringBuilder deep = new StringBuilder();
    for (int i = 0; i < 256; ++i) deep.append('[');
    deep.append('I');
    try { Class.forName(deep.toString(), false, new ParentLoader()); throw new AssertionError("256 dimensions accepted"); }
    catch (ClassNotFoundException expected) { System.out.println("too-many-dimensions rejected"); }
    for (boolean shadow : new boolean[] {false, true}) {
      String suffix = shadow ? "-shadow" : "-blocked";
      check("subclass" + suffix, 0, shadow);
      check("delegated-for-name" + suffix, 1, shadow);
      check("delegated-resolve" + suffix, 2, shadow);
      check("inherited-interface" + suffix, 3, shadow);
    }
  }
}
