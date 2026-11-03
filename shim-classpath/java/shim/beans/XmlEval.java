/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.beans;

import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class XmlEval {
  private static final Object kNone = new Object();

  private final Object owner;
  private final ClassLoader loader;
  private final ExceptionListener listener;
  private final Map<String, Object> vars = new HashMap<String, Object>();

  public XmlEval(Object owner, ExceptionListener listener, ClassLoader loader) {
    this.owner = owner;
    this.listener = listener;
    this.loader = loader;
  }

  public List<Object> run(MiniXml.Node root) {
    List<Object> out = new ArrayList<Object>();
    for (MiniXml.Node n : root.children) {
      try {
        Object v = eval(n, null, false);
        if (v != kNone && !n.name.equals("void")) {
          out.add(v);
        }
      } catch (Exception e) {
        listener.exceptionThrown(e);
      }
    }
    return out;
  }

  private Class<?> loadClass(String name) throws ClassNotFoundException {
    switch (name) {
      case "int": return int.class;
      case "long": return long.class;
      case "short": return short.class;
      case "byte": return byte.class;
      case "char": return char.class;
      case "boolean": return boolean.class;
      case "float": return float.class;
      case "double": return double.class;
      default: break;
    }
    ClassLoader cl = loader != null ? loader : Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = ClassLoader.getSystemClassLoader();
    }
    return Class.forName(name, false, cl);
  }

  private Object eval(MiniXml.Node n, Object parent, boolean isVoidContext) throws Exception {
    String tag = n.name;
    switch (tag) {
      case "string": return n.text.toString();
      case "int": return Integer.valueOf(n.text.toString().trim());
      case "long": return Long.valueOf(n.text.toString().trim());
      case "short": return Short.valueOf(n.text.toString().trim());
      case "byte": return Byte.valueOf(n.text.toString().trim());
      case "float": return Float.valueOf(n.text.toString().trim());
      case "double": return Double.valueOf(n.text.toString().trim());
      case "boolean": return Boolean.valueOf(n.text.toString().trim());
      case "char": {
        String code = n.attrs.get("code");
        if (code != null) {
          return Character.valueOf((char) Integer.parseInt(code.substring(code.startsWith("#x") ? 2 : 1), 16));
        }
        String t = n.text.toString();
        return Character.valueOf(t.length() > 0 ? t.charAt(0) : '\0');
      }
      case "null": return null;
      case "class": return loadClass(n.text.toString().trim());
      case "this": return owner;
      case "var": {
        String id = n.attrs.get("idref");
        if (id != null) {
          return vars.get(id);
        }
        Object v = parent;
        String vid = n.attrs.get("id");
        if (vid != null) {
          vars.put(vid, v);
        }
        return v;
      }
      case "array": return evalArray(n);
      case "object":
      case "void":
      case "field":
        return evalObject(n, parent, tag.equals("void"));
      default:
        throw new IllegalStateException("unknown element: " + tag);
    }
  }

  private Object evalArray(MiniXml.Node n) throws Exception {
    String cn = n.attrs.get("class");
    Class<?> component = cn == null ? Object.class : loadClass(cn);
    String len = n.attrs.get("length");
    int count = 0;
    for (MiniXml.Node c : n.children) {
      if (!c.name.equals("void")) {
        count++;
      }
    }
    int length = len != null ? Integer.parseInt(len) : count;
    Object array = Array.newInstance(component, length);
    int next = 0;
    for (MiniXml.Node c : n.children) {
      if (c.name.equals("void")) {
        String idx = c.attrs.get("index");
        int i = idx != null ? Integer.parseInt(idx) : next;
        Object val = c.children.isEmpty() ? null : eval(c.children.get(0), array, false);
        Array.set(array, i, val);
      } else {
        Array.set(array, next++, eval(c, array, false));
      }
    }
    String id = n.attrs.get("id");
    if (id != null) {
      vars.put(id, array);
    }
    return array;
  }

  private Object evalObject(MiniXml.Node n, Object parent, boolean isVoid) throws Exception {
    String idref = n.attrs.get("idref");
    if (idref != null) {
      return vars.get(idref);
    }
    String cn = n.attrs.get("class");
    String method = n.attrs.get("method");
    String property = n.attrs.get("property");
    String field = n.attrs.get("field");
    String index = n.attrs.get("index");
    Class<?> cls = cn == null ? null : loadClass(cn);
    List<Object> args = new ArrayList<Object>();
    List<MiniXml.Node> statements = new ArrayList<MiniXml.Node>();
    for (MiniXml.Node c : n.children) {
      if (c.name.equals("void")) {
        statements.add(c);
      } else {
        args.add(null);
      }
    }
    Object result;
    Object target = cls != null ? cls : parent;
    boolean needArgsLater = true;
    int ai = 0;
    List<Object> evaluated = new ArrayList<Object>();
    for (MiniXml.Node c : n.children) {
      if (!c.name.equals("void")) {
        evaluated.add(eval(c, null, false));
      }
    }
    args = evaluated;
    if (field != null) {
      Class<?> fc = target instanceof Class ? (Class<?>) target : target.getClass();
      Field f = fc.getField(field);
      if (args.isEmpty()) {
        result = f.get(Modifier.isStatic(f.getModifiers()) ? null : target);
      } else {
        f.set(Modifier.isStatic(f.getModifiers()) ? null : target, args.get(0));
        result = null;
      }
    } else if (property != null) {
      result = invokeProperty(target, property, args);
    } else if (index != null) {
      int i = Integer.parseInt(index);
      if (target.getClass().isArray()) {
        if (args.isEmpty()) {
          result = Array.get(target, i);
        } else {
          Array.set(target, i, args.get(0));
          result = null;
        }
      } else {
        java.util.List<Object> l = castList(target);
        if (args.isEmpty()) {
          result = l.get(i);
        } else {
          l.set(i, args.get(0));
          result = null;
        }
      }
    } else if (method != null) {
      result = invokeMethod(target, method, args);
    } else if (cls != null) {
      result = construct(cls, args);
    } else {
      result = parent;
    }
    String id = n.attrs.get("id");
    if (id != null) {
      vars.put(id, result);
    }
    for (MiniXml.Node st : statements) {
      try {
        eval(st, result, true);
      } catch (Exception e) {
        listener.exceptionThrown(e);
      }
    }
    return isVoid ? result : result;
  }

  @SuppressWarnings("unchecked")
  private static java.util.List<Object> castList(Object o) { return (java.util.List<Object>) o; }

  private Object construct(Class<?> cls, List<Object> args) throws Exception {
    if (cls.isPrimitive()) {
      return args.isEmpty() ? null : args.get(0);
    }
    for (Constructor<?> c : cls.getConstructors()) {
      if (compatible(c.getParameterTypes(), args)) {
        return c.newInstance(args.toArray());
      }
    }
    throw new NoSuchMethodException(cls.getName() + ".<init>" + args);
  }

  private Object invokeMethod(Object target, String name, List<Object> args) throws Exception {
    Class<?> type = target instanceof Class ? (Class<?>) target : target.getClass();
    for (Method m : type.getMethods()) {
      if (m.getName().equals(name) && compatible(m.getParameterTypes(), args)) {
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        if (isStatic || !(target instanceof Class)) {
          return m.invoke(isStatic ? null : target, args.toArray());
        }
      }
    }
    for (Method m : type.getMethods()) {
      if (m.getName().equals(name) && compatible(m.getParameterTypes(), args)) {
        return m.invoke(target, args.toArray());
      }
    }
    throw new NoSuchMethodException(type.getName() + "." + name + args);
  }

  private Object invokeProperty(Object target, String name, List<Object> args) throws Exception {
    Class<?> type = target.getClass();
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
        if (pd.getName().equals(name)) {
          if (args.isEmpty() && pd.getReadMethod() != null) {
            return pd.getReadMethod().invoke(target);
          }
          if (!args.isEmpty() && pd.getWriteMethod() != null) {
            pd.getWriteMethod().invoke(target, args.get(0));
            return null;
          }
        }
      }
    } catch (IntrospectionException e) {
      // fall through to reflection
    }
    String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    return invokeMethod(target, args.isEmpty() ? "get" + cap : "set" + cap, args);
  }

  private static boolean compatible(Class<?>[] params, List<Object> args) {
    if (params.length != args.size()) {
      return false;
    }
    for (int i = 0; i < params.length; i++) {
      Object a = args.get(i);
      Class<?> p = params[i];
      if (a == null) {
        if (p.isPrimitive()) {
          return false;
        }
        continue;
      }
      if (p.isPrimitive()) {
        Class<?> ac = a.getClass();
        boolean ok = (p == int.class && ac == Integer.class) || (p == long.class && ac == Long.class)
            || (p == boolean.class && ac == Boolean.class) || (p == double.class && ac == Double.class)
            || (p == float.class && ac == Float.class) || (p == char.class && ac == Character.class)
            || (p == short.class && ac == Short.class) || (p == byte.class && ac == Byte.class);
        if (!ok) {
          return false;
        }
      } else if (!p.isInstance(a)) {
        return false;
      }
    }
    return true;
  }
}
