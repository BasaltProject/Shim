/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class Agents {
  private static final List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();
  private static volatile boolean active;
  private static volatile boolean canRetransform;
  private static volatile boolean canRedefine;

  private Agents() { }

  public static void start(String spec) {
    int eq = spec.indexOf('=');
    String path = eq < 0 ? spec : spec.substring(0, eq);
    String options = eq < 0 ? null : spec.substring(eq + 1);
    try (JarFile jar = new JarFile(path)) {
      Manifest m = jar.getManifest();
      String main = m == null ? null : m.getMainAttributes().getValue("Premain-Class");
      if (main == null) {
        System.err.println("Error: Premain-Class not specified in " + path);
        System.exit(1);
      }
      flags(m);
      Class<?> c = Class.forName(main.trim(), true, ClassLoader.getSystemClassLoader());
      Instrumentation inst = new Impl();
      Method two = find(c, "premain", String.class, Instrumentation.class);
      if (two != null) {
        two.setAccessible(true);
        two.invoke(null, options, inst);
      } else {
        Method one = find(c, "premain", String.class);
        if (one == null) {
          throw new NoSuchMethodException(main + ".premain");
        }
        one.setAccessible(true);
        one.invoke(null, options);
      }
    } catch (Throwable t) {
      System.err.println("Exception in premain of " + path + ": " + t);
      System.exit(1);
    }
  }

  private static void flags(Manifest m) {
    canRetransform = "true".equalsIgnoreCase(m.getMainAttributes().getValue("Can-Retransform-Classes"));
    canRedefine = "true".equalsIgnoreCase(m.getMainAttributes().getValue("Can-Redefine-Classes"));
  }

  private static Method find(Class<?> c, String name, Class<?>... types) {
    try {
      return c.getDeclaredMethod(name, types);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  public static void launcher(String path) {
    try (JarFile jar = new JarFile(path)) {
      Manifest m = jar.getManifest();
      String main = m == null ? null : m.getMainAttributes().getValue("Launcher-Agent-Class");
      if (main == null) {
        return;
      }
      flags(m);
      Class<?> c = Class.forName(main.trim(), true, ClassLoader.getSystemClassLoader());
      Method two = find(c, "agentmain", String.class, Instrumentation.class);
      if (two != null) {
        two.setAccessible(true);
        two.invoke(null, "", new Impl());
      } else {
        Method one = find(c, "agentmain", String.class);
        if (one == null) {
          throw new NoSuchMethodException(main + ".agentmain");
        }
        one.setAccessible(true);
        one.invoke(null, "");
      }
    } catch (Throwable t) {
      System.err.println("Exception in agentmain of " + path + ": " + t);
      System.exit(1);
    }
  }

  private static final List<JarFile> appended = new ArrayList<JarFile>();

  public static byte[] readAppended(String name) {
    String entry = name.replace('.', '/') + ".class";
    synchronized (appended) {
      for (JarFile jar : appended) {
        java.util.jar.JarEntry e = jar.getJarEntry(entry);
        if (e != null) {
          try (InputStream in = jar.getInputStream(e)) {
            return in.readAllBytes();
          } catch (java.io.IOException ignored) {
          }
        }
      }
    }
    return null;
  }

  public static boolean isActive() {
    return active;
  }

  public static byte[] transform(ClassLoader loader, String name, Class<?> redefined, ProtectionDomain pd,
      byte[] bytes) {
    ClassFileTransformer[] list;
    synchronized (transformers) {
      list = transformers.toArray(new ClassFileTransformer[0]);
    }
    byte[] current = bytes;
    for (ClassFileTransformer t : list) {
      try {
        byte[] out = t.transform(loader, name, redefined, pd, current);
        if (out != null) {
          current = out;
        }
      } catch (IllegalClassFormatException | RuntimeException e) {
        // a failing transformer leaves the bytes untouched, as in the JDK
      }
    }
    return current == bytes ? null : current;
  }

  public static byte[] readClass(ClassLoader loader, String name) {
    try (InputStream in = loader.getResourceAsStream(name.replace('.', '/') + ".class")) {
      if (in == null) {
        return null;
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      for (int n; (n = in.read(buf)) > 0;) {
        out.write(buf, 0, n);
      }
      return out.toByteArray();
    } catch (java.io.IOException e) {
      return null;
    }
  }

  private static final class Impl implements Instrumentation {
    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
      java.util.Objects.requireNonNull(transformer);
      synchronized (transformers) {
        transformers.add(transformer);
      }
      active = true;
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
      addTransformer(transformer, false);
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
      synchronized (transformers) {
        return transformers.remove(transformer);
      }
    }

    @Override
    public boolean isRetransformClassesSupported() {
      return canRetransform;
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
      if (!canRetransform) {
        throw new UnsupportedOperationException("retransformClasses is not supported in this environment");
      }
      for (Class<?> c : classes) {
        if (!isModifiableClass(c)) {
          throw new UnmodifiableClassException(c.getName());
        }
      }
    }

    @Override
    public boolean isRedefineClassesSupported() {
      return canRedefine;
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException,
        UnmodifiableClassException {
      if (!canRedefine) {
        throw new UnsupportedOperationException("redefineClasses is not supported in this environment");
      }
      throw new UnsupportedOperationException("redefineClasses");
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
      return !theClass.isArray() && !theClass.isPrimitive();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getAllLoadedClasses() {
      List<Class> all = new ArrayList<Class>();
      for (Object o : shim.jdwp.Agent.allClasses(null)) {
        all.add((Class) o);
      }
      for (Object o : shim.jdwp.Agent.allClasses(ClassLoader.getSystemClassLoader())) {
        if (!all.contains(o)) {
          all.add((Class) o);
        }
      }
      return all.toArray(new Class[0]);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getInitiatedClasses(ClassLoader loader) {
      List<Class> all = new ArrayList<Class>();
      for (Object o : shim.jdwp.Agent.allClasses(loader)) {
        all.add((Class) o);
      }
      return all.toArray(new Class[0]);
    }

    @Override
    public long getObjectSize(Object o) {
      Class<?> c = o.getClass();
      if (c.isArray()) {
        Class<?> e = c.getComponentType();
        int len = java.lang.reflect.Array.getLength(o);
        long size = e == boolean.class || e == byte.class ? 1 : e == char.class || e == short.class ? 2
            : e == long.class || e == double.class ? 8 : 4;
        return (16 + size * len + 7) & ~7L;
      }
      long size = 12;
      for (Class<?> k = c; k != null; k = k.getSuperclass()) {
        for (Field f : k.getDeclaredFields()) {
          if (!Modifier.isStatic(f.getModifiers())) {
            Class<?> t = f.getType();
            size += t == boolean.class || t == byte.class ? 1 : t == char.class || t == short.class ? 2
                : t == long.class || t == double.class ? 8 : 4;
          }
        }
      }
      return (size + 7) & ~7L;
    }

    @Override
    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
      synchronized (appended) {
        appended.add(java.util.Objects.requireNonNull(jarfile));
      }
    }

    @Override
    public void appendToSystemClassLoaderSearch(JarFile jarfile) {
      appendToBootstrapClassLoaderSearch(jarfile);
    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
      return false;
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void redefineModule(Module module, Set<Module> extraReads, Map<String, Set<Module>> extraExports,
        Map<String, Set<Module>> extraOpens, Set<Class<?>> extraUses,
        Map<Class<?>, List<Class<?>>> extraProvides) {
    }

    @Override
    public boolean isModifiableModule(Module module) {
      return true;
    }
  }
}
