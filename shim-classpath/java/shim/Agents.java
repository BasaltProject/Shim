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
  private static final class Entry {
    final ClassFileTransformer transformer;
    final boolean retransformable;

    Entry(ClassFileTransformer transformer, boolean retransformable) {
      this.transformer = transformer;
      this.retransformable = retransformable;
    }
  }

  private static final List<Entry> transformers = new ArrayList<Entry>();
  private static final java.util.WeakHashMap<ClassLoader, Map<String, byte[]>> originals =
      new java.util.WeakHashMap<ClassLoader, Map<String, byte[]>>();
  private static final Object bootKey = new Object();
  private static final Map<String, byte[]> bootOriginals = new java.util.HashMap<String, byte[]>();

  private static native String redefine0(Class<?> c, byte[] bytes);

  private static native void setNativePrefixes0(byte[] joined);

  private static volatile boolean canSetPrefix;
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
    canSetPrefix = "true".equalsIgnoreCase(m.getMainAttributes().getValue("Can-Set-Native-Method-Prefix"));
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
    if (redefined == null && bytes != null) {
      remember(loader, name, bytes);
    }
    return runTransformers(loader, name, redefined, pd, bytes, false);
  }

  private static void remember(ClassLoader loader, String name, byte[] bytes) {
    synchronized (originals) {
      Map<String, byte[]> m;
      if (loader == null) {
        m = bootOriginals;
      } else {
        m = originals.get(loader);
        if (m == null) {
          m = new java.util.HashMap<String, byte[]>();
          originals.put(loader, m);
        }
      }
      m.putIfAbsent(name, bytes);
    }
  }

  private static void replaceOriginal(ClassLoader loader, String name, byte[] bytes) {
    synchronized (originals) {
      Map<String, byte[]> m = loader == null ? bootOriginals : originals.get(loader);
      if (m == null) {
        m = new java.util.HashMap<String, byte[]>();
        originals.put(loader, m);
      }
      m.put(name, bytes.clone());
    }
  }

  private static byte[] original(ClassLoader loader, String name) {
    synchronized (originals) {
      Map<String, byte[]> m = loader == null ? bootOriginals : originals.get(loader);
      byte[] b = m == null ? null : m.get(name);
      if (b != null) {
        return b;
      }
    }
    return loader == null ? readClass(ClassLoader.getSystemClassLoader(), name.replace('/', '.'))
        : readClass(loader, name.replace('/', '.'));
  }

  private static byte[] runTransformers(ClassLoader loader, String name, Class<?> redefined, ProtectionDomain pd,
      byte[] bytes, boolean retransformOnly) {
    Entry[] list;
    synchronized (transformers) {
      list = transformers.toArray(new Entry[0]);
    }
    byte[] current = bytes;
    for (Entry e : list) {
      if (retransformOnly && !e.retransformable) {
        continue;
      }
      try {
        byte[] out = e.transformer.transform(loader, name, redefined, pd, current);
        if (out != null) {
          current = out;
        }
      } catch (IllegalClassFormatException | RuntimeException ex) {
        // a failing transformer leaves the bytes untouched, as in the JDK
      }
    }
    return current == bytes ? null : current;
  }

  private static void redefine(Class<?> c, byte[] bytes) throws UnmodifiableClassException {
    String r = redefine0(c, bytes);
    if (r == null) {
      return;
    }
    if (r.startsWith("NCDFE:")) {
      throw new NoClassDefFoundError(r.substring(6));
    }
    if (r.startsWith("UOE:")) {
      throw new UnsupportedOperationException(r.substring(4));
    }
    throw new InternalError(r);
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
        transformers.add(new Entry(transformer, canRetransform));
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
        for (Entry e : transformers) {
          if (e.transformer == transformer) {
            return transformers.remove(e);
          }
        }
        return false;
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
      for (Class<?> c : classes) {
        String internal = c.getName().replace('.', '/');
        byte[] base = original(c.getClassLoader(), internal);
        if (base == null) {
          throw new UnmodifiableClassException(c.getName());
        }
        byte[] changed = runTransformers(c.getClassLoader(), internal, c, c.getProtectionDomain(), base, true);
        redefine(c, changed != null ? changed : base);
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
      java.util.Objects.requireNonNull(definitions);
      if (!canRedefine) {
        throw new UnsupportedOperationException("redefineClasses is not supported in this environment");
      }
      for (ClassDefinition d : definitions) {
        Class<?> c = d.getDefinitionClass();
        if (!isModifiableClass(c)) {
          throw new UnmodifiableClassException(c.getName());
        }
      }
      for (ClassDefinition d : definitions) {
        Class<?> c = d.getDefinitionClass();
        byte[] bytes = d.getDefinitionClassFile();
        replaceOriginal(c.getClassLoader(), c.getName().replace('.', '/'), bytes);
        byte[] changed = runTransformers(c.getClassLoader(), c.getName().replace('.', '/'), c,
            c.getProtectionDomain(), bytes, false);
        redefine(c, changed != null ? changed : bytes);
      }
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
      return !theClass.isArray() && !theClass.isPrimitive() && !theClass.isHidden();
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
      return canSetPrefix;
    }

    private final java.util.Map<ClassFileTransformer, String> prefixes = new java.util.LinkedHashMap<>();

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
      if (!canSetPrefix) {
        throw new UnsupportedOperationException("setNativeMethodPrefix is not supported in this environment");
      }
      java.util.Objects.requireNonNull(transformer, "null passed as 'transformer' in setNativeMethodPrefix");
      java.util.Objects.requireNonNull(prefix, "null passed as 'prefix' in setNativeMethodPrefix");
      synchronized (prefixes) {
        boolean registered = false;
        synchronized (transformers) {
          for (Entry e : transformers) {
            registered |= e.transformer == transformer;
          }
        }
        if (!registered) {
          throw new IllegalArgumentException("transformer not registered in setNativeMethodPrefix");
        }
        prefixes.put(transformer, prefix);
        StringBuilder sb = new StringBuilder();
        for (String p : prefixes.values()) {
          if (!p.isEmpty()) {
            sb.append(p).append('\n');
          }
        }
        setNativePrefixes0(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
      }
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
