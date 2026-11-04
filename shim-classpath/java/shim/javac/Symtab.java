/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Class table: source classes entered by the compiler plus library classes loaded through reflection. */
final class Symtab {
  final ClassLoader loader;
  final Map<String, CSym> classes = new HashMap<String, CSym>();
  private final Set<String> missing = new HashSet<String>();
  private final Map<String, Map<String, TVSym>> tvars = new HashMap<String, Map<String, TVSym>>();
  private final Map<String, Map<String, Object>> constantCache = new HashMap<String, Map<String, Object>>();

  CSym objectSym;
  CSym stringSym;
  Ty object;
  Ty string;

  Symtab(ClassLoader loader) {
    this.loader = loader;
    objectSym = lookup("java/lang/Object");
    stringSym = lookup("java/lang/String");
    object = Ty.cls(objectSym);
    string = Ty.cls(stringSym);
  }

  Ty classType(String internal) {
    CSym c = lookup(internal);
    return c == null ? Ty.tError : Ty.cls(c);
  }

  /** Declares a source class. */
  void enter(CSym c) {
    classes.put(c.internal, c);
    missing.remove(c.internal);
  }

  CSym lookup(String internal) {
    CSym c = classes.get(internal);
    if (c != null) {
      return c;
    }
    if (missing.contains(internal)) {
      return null;
    }
    Class<?> k;
    try {
      k = Class.forName(internal.replace('/', '.'), false, loader);
    } catch (Throwable t) {
      missing.add(internal);
      return null;
    }
    return reflected(k);
  }

  boolean packageHasClass(String dotted, String name) {
    return lookup(dotted.replace('.', '/') + "/" + name) != null;
  }

  CSym reflected(Class<?> k) {
    String internal = k.getName().replace('.', '/');
    CSym c = classes.get(internal);
    if (c != null) {
      return c;
    }
    c = new CSym(internal);
    c.reflect = k;
    int dollar = internal.lastIndexOf('$');
    String sn = k.getDeclaringClass() == null && !k.isArray() ? internal.substring(internal.lastIndexOf('/') + 1)
        : k.getSimpleName();
    c.name = sn;
    classes.put(internal, c);
    final Symtab self = this;
    CSym.Completer comp = new CSym.Completer() {
      public void completeHeader(CSym s) {
        self.completeHeader(s);
      }

      public void completeMembers(CSym s) {
        self.completeMembers(s);
      }
    };
    c.completer = comp;
    int mods = k.getModifiers();
    int f = mods & (Sym.PUBLIC | Sym.PRIVATE | Sym.PROTECTED | Sym.STATIC | Sym.FINAL | Sym.ABSTRACT);
    if (k.isInterface()) {
      f |= Sym.INTERFACE | Sym.ABSTRACT;
    }
    if (k.isAnnotation()) {
      f |= Sym.ANNOTATION;
    }
    if (k.isEnum()) {
      f |= Sym.ENUM;
    }
    if (k.isRecord()) {
      f |= Sym.RECORD;
    }
    c.flags = f;
    Class<?> d = k.getDeclaringClass();
    if (d != null) {
      c.outerClass = lookup(d.getName().replace('.', '/'));
      if (c.outerClass != null && c.outerClass.isInterface()) {
        c.flags |= Sym.STATIC;
      }
    }
    return c;
  }

  private TVSym tvsym(final TypeVariable<?> tv) {
    Object decl = tv.getGenericDeclaration();
    String key = declKey(decl);
    Map<String, TVSym> m = tvars.get(key);
    if (m == null) {
      m = new HashMap<String, TVSym>();
      tvars.put(key, m);
    }
    TVSym s = m.get(tv.getName());
    if (s == null) {
      s = new TVSym(tv.getName(), decl);
      final TVSym fs = s;
      s.lazyBounds = new Runnable() {
        public void run() {
          for (java.lang.reflect.Type b : tv.getBounds()) {
            fs.bounds.add(convert(b));
          }
          if (fs.bounds.isEmpty()) {
            fs.bounds.add(object);
          }
        }
      };
      m.put(tv.getName(), s);
    }
    return s;
  }

  private static String declKey(Object decl) {
    if (decl instanceof Class) {
      return "C:" + ((Class<?>) decl).getName();
    }
    if (decl instanceof java.lang.reflect.Executable) {
      java.lang.reflect.Executable e = (java.lang.reflect.Executable) decl;
      StringBuilder sb = new StringBuilder("M:").append(e.getDeclaringClass().getName()).append('.')
          .append(e.getName()).append('(');
      for (Class<?> p : e.getParameterTypes()) {
        sb.append(p.getName()).append(',');
      }
      return sb.append(')').toString();
    }
    return String.valueOf(decl);
  }

  Ty convert(java.lang.reflect.Type t) {
    if (t instanceof Class) {
      Class<?> k = (Class<?>) t;
      if (k.isPrimitive()) {
        return Ty.primByName(k.getName());
      }
      if (k.isArray()) {
        return Ty.array(convert(k.getComponentType()));
      }
      CSym c = reflected(k);
      return Ty.cls(c);
    }
    if (t instanceof ParameterizedType) {
      ParameterizedType p = (ParameterizedType) t;
      Class<?> raw = (Class<?>) p.getRawType();
      CSym c = reflected(raw);
      java.lang.reflect.Type[] as = p.getActualTypeArguments();
      Ty[] args = new Ty[as.length];
      for (int i = 0; i < as.length; i++) {
        args[i] = convert(as[i]);
      }
      return Ty.cls(c, args);
    }
    if (t instanceof TypeVariable) {
      return tvsym((TypeVariable<?>) t).type;
    }
    if (t instanceof GenericArrayType) {
      return Ty.array(convert(((GenericArrayType) t).getGenericComponentType()));
    }
    if (t instanceof WildcardType) {
      WildcardType w = (WildcardType) t;
      java.lang.reflect.Type[] lo = w.getLowerBounds();
      if (lo.length > 0) {
        return Ty.wild(2, convert(lo[0]));
      }
      java.lang.reflect.Type[] up = w.getUpperBounds();
      if (up.length == 0 || up[0] == Object.class) {
        return Ty.wild(0, null);
      }
      return Ty.wild(1, convert(up[0]));
    }
    return Ty.tError;
  }

  private void completeHeader(CSym c) {
    Class<?> k = c.reflect;
    try {
      for (TypeVariable<?> tv : k.getTypeParameters()) {
        c.tparams.add(tvsym(tv));
      }
      if (!k.isInterface()) {
        java.lang.reflect.Type sc = k.getGenericSuperclass();
        if (sc != null) {
          c.superclass = convert(sc);
        }
      }
      for (java.lang.reflect.Type i : k.getGenericInterfaces()) {
        c.interfaces.add(convert(i));
      }
    } catch (Throwable t) {
      if (c.superclass == null && !k.isInterface() && k != Object.class) {
        Class<?> sk = k.getSuperclass();
        c.superclass = sk == null ? null : convert(sk);
      }
    }
  }

  private void completeMembers(CSym c) {
    Class<?> k = c.reflect;
    try {
      for (Field f : k.getDeclaredFields()) {
        if (f.isSynthetic()) {
          continue;
        }
        try {
          addField(c, f);
        } catch (Throwable t) {
          // skip members whose types cannot be loaded
        }
      }
    } catch (Throwable t) {
      // partially available members
    }
    try {
      for (Method m : k.getDeclaredMethods()) {
        if (m.isSynthetic() || m.isBridge()) {
          continue;
        }
        try {
          addMethod(c, m);
        } catch (Throwable t) {
          // skip members whose types cannot be loaded
        }
      }
    } catch (Throwable t) {
      // partially available members
    }
    if (!k.isEnum() && !k.isInterface()) {
      try {
        boolean inner = c.isInner();
        for (Constructor<?> m : k.getDeclaredConstructors()) {
          if (m.isSynthetic()) {
            continue;
          }
          try {
            addCtor(c, m, inner);
          } catch (Throwable t) {
            // skip members whose types cannot be loaded
          }
        }
      } catch (Throwable t) {
        // partially available members
      }
    }
  }

  private void addField(CSym c, Field f) {
    {
      {
        FSym fs = new FSym();
        fs.name = f.getName();
        fs.flags = f.getModifiers() & 0xffff;
        if (f.isEnumConstant()) {
          fs.flags |= Sym.ENUM;
        }
        fs.reflect = f;
        try {
          fs.type = convert(f.getGenericType());
        } catch (Throwable t) {
          fs.type = convert(f.getType());
        }
        c.addField(fs);
      }
    }
  }

  private void addMethod(CSym c, Method m) {
    MSym ms = new MSym();
    ms.name = m.getName();
    ms.flags = m.getModifiers() & 0xffff;
    if (m.isVarArgs()) {
      ms.flags |= Sym.VARARGS;
    } else {
      ms.flags &= ~Sym.VARARGS;
    }
    if (m.isDefault()) {
      ms.flags |= Sym.DEFAULT;
    }
    ms.reflect = m;
    fillExecutable(ms, m, false);
    try {
      ms.ret = convert(m.getGenericReturnType());
    } catch (Throwable t) {
      ms.ret = convert(m.getReturnType());
    }
    c.addMethod(ms);
  }

  private void addCtor(CSym c, Constructor<?> m, boolean inner) {
    MSym ms = new MSym();
    ms.name = "<init>";
    ms.flags = m.getModifiers() & 0xffff;
    if (m.isVarArgs()) {
      ms.flags |= Sym.VARARGS;
    } else {
      ms.flags &= ~Sym.VARARGS;
    }
    ms.reflect = m;
    fillExecutable(ms, m, inner);
    c.addMethod(ms);
  }

  private void fillExecutable(MSym ms, java.lang.reflect.Executable m, boolean dropFirst) {
    for (TypeVariable<?> tv : m.getTypeParameters()) {
      ms.tparams.add(tvsym(tv));
    }
    java.lang.reflect.Type[] gp;
    Class<?>[] pt = m.getParameterTypes();
    try {
      gp = m.getGenericParameterTypes();
    } catch (Throwable t) {
      gp = pt;
    }
    int skip = dropFirst ? 1 : 0;
    if (gp.length == pt.length) {
      for (int i = skip; i < pt.length; i++) {
        ms.params.add(convert(gp[i]));
      }
    } else if (gp.length + skip == pt.length) {
      for (int i = 0; i < gp.length; i++) {
        ms.params.add(convert(gp[i]));
      }
    } else {
      for (int i = skip; i < pt.length; i++) {
        ms.params.add(convert(pt[i]));
      }
    }
    java.lang.reflect.Type[] ge;
    try {
      ge = m.getGenericExceptionTypes();
    } catch (Throwable t) {
      ge = m.getExceptionTypes();
    }
    for (java.lang.reflect.Type e : ge) {
      ms.thrown.add(convert(e));
    }
  }

  /** The value of a library constant field, read from the class file without initializing the class. */
  Object constantOf(FSym f) {
    if (f.constKnown) {
      return f.constant;
    }
    f.constKnown = true;
    if (f.reflect == null || !Modifier.isStatic(f.flags) || !Modifier.isFinal(f.flags)) {
      return null;
    }
    CSym c = (CSym) f.owner;
    Map<String, Object> m = constantCache.get(c.internal);
    if (m == null) {
      m = new HashMap<String, Object>();
      constantCache.put(c.internal, m);
      java.io.InputStream in = null;
      try {
        in = loader.getResourceAsStream(c.internal + ".class");
        if (in != null) {
          byte[] b = ClassScan.readAll(in);
          ClassScan.constants(b, m);
        }
      } catch (Throwable t) {
        // no constants
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (java.io.IOException e) {
          // ignored
        }
      }
    }
    Object v = m.get(f.name);
    if (v != null) {
      Ty t = f.type;
      if (t.tag == Ty.BOOLEAN && v instanceof Integer) {
        v = Boolean.valueOf(((Integer) v).intValue() != 0);
      } else if (t.tag == Ty.CHAR && v instanceof Integer) {
        v = Character.valueOf((char) ((Integer) v).intValue());
      } else if (t.tag == Ty.BYTE && v instanceof Integer) {
        v = Byte.valueOf((byte) ((Integer) v).intValue());
      } else if (t.tag == Ty.SHORT && v instanceof Integer) {
        v = Short.valueOf((short) ((Integer) v).intValue());
      }
    }
    f.constant = v;
    return v;
  }

  static List<Ty> list(Ty... t) {
    List<Ty> l = new ArrayList<Ty>();
    for (Ty x : t) {
      l.add(x);
    }
    return l;
  }
}
