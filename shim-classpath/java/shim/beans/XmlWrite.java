/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.beans;

import java.beans.BeanInfo;
import java.beans.ExceptionListener;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class XmlWrite {
  private final StringBuilder out = new StringBuilder();
  private final ExceptionListener listener;
  private final IdentityHashMap<Object, Integer> counts = new IdentityHashMap<Object, Integer>();
  private final IdentityHashMap<Object, String> ids = new IdentityHashMap<Object, String>();
  private final IdentityHashMap<Object, Boolean> written = new IdentityHashMap<Object, Boolean>();
  private final Map<String, Integer> idCounters = new HashMap<String, Integer>();
  private final Map<Class<?>, String[]> ctorProps = new HashMap<Class<?>, String[]>();
  private final Map<Class<?>, String[]> fieldProps = new HashMap<Class<?>, String[]>();

  public XmlWrite(ExceptionListener listener) {
    this.listener = listener;
    fieldProps.put(java.awt.Point.class, new String[] {"x", "y"});
    fieldProps.put(java.awt.Dimension.class, new String[] {"width", "height"});
    fieldProps.put(java.awt.Rectangle.class, new String[] {"x", "y", "width", "height"});
    ctorProps.put(java.awt.Insets.class, new String[] {"top", "left", "bottom", "right"});
  }

  public String render(Object root) {
    counts.clear();
    ids.clear();
    written.clear();
    scan(root, new IdentityHashMap<Object, Boolean>());
    out.setLength(0);
    write(root, 1);
    return out.toString();
  }

  private static boolean isValue(Object o) {
    return o == null || o instanceof String || o instanceof Number || o instanceof Boolean || o instanceof Character
        || o instanceof Class || o instanceof Enum;
  }

  private void scan(Object o, IdentityHashMap<Object, Boolean> path) {
    if (isValue(o)) {
      return;
    }
    Integer c = counts.get(o);
    counts.put(o, c == null ? 1 : c + 1);
    if (c != null) {
      return;
    }
    try {
      if (o.getClass().isArray()) {
        int n = Array.getLength(o);
        for (int i = 0; i < n; i++) {
          scan(Array.get(o, i), path);
        }
      } else if (o instanceof Collection) {
        for (Object e : (Collection<?>) o) {
          scan(e, path);
        }
      } else if (o instanceof Map) {
        for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
          scan(e.getKey(), path);
          scan(e.getValue(), path);
        }
      } else if (fieldProps.containsKey(o.getClass())) {
        return;
      } else {
        for (Prop p : props(o)) {
          scan(p.value, path);
        }
      }
    } catch (Exception e) {
      // reported when written
    }
  }

  private static final class Prop {
    final String name;
    final Object value;
    final boolean readOnly;
    int depth;

    Prop(String name, Object value, boolean readOnly) {
      this.name = name;
      this.value = value;
      this.readOnly = readOnly;
    }
  }

  private List<Prop> props(Object o) throws Exception {
    List<Prop> list = new ArrayList<Prop>();
    Class<?> cls = o.getClass();
    String[] cp = ctorProps.get(cls);
    if (cp != null) {
      for (String n : cp) {
        list.add(new Prop(n, readProperty(o, n), false));
      }
      return list;
    }
    Object def = null;
    java.lang.reflect.Constructor<?> annotated = annotatedConstructor(cls);
    try {
      if (annotated != null) {
        String[] names = annotated.getAnnotation(java.beans.ConstructorProperties.class).value();
        Object[] args = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
          args[i] = readProperty(o, names[i]);
        }
        def = annotated.newInstance(args);
      } else {
        def = cls.getConstructor().newInstance();
      }
    } catch (Exception e) {
      throw new java.lang.InstantiationException(cls.getName());
    }
    BeanInfo info = Introspector.getBeanInfo(cls);
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      Method r = pd.getReadMethod();
      Method w = pd.getWriteMethod();
      if (r == null || pd.getName().equals("class")
          || Boolean.TRUE.equals(pd.getValue("transient")) && r.getDeclaringClass() == cls) {
        continue;
      }
      if (w == null && !(Collection.class.isAssignableFrom(r.getReturnType()) || Map.class.isAssignableFrom(r.getReturnType()))) {
        continue;
      }
      Object v = r.invoke(o);
      Object d = r.invoke(def);
      boolean live = v instanceof Collection || v instanceof Map;
      if (w != null && live && v != null && d != null && v.getClass() == d.getClass()) {
        if (!sameState(v, d, 0)) {
          list.add(new Prop(pd.getName(), v, true));
        }
      } else if (w != null) {
        if (!sameState(v, d, 0)) {
          list.add(new Prop(pd.getName(), v, false));
        }
      } else if (v != null && !sameState(v, d, 0)) {
        list.add(new Prop(pd.getName(), v, true));
      }
    }
    for (Prop p : list) {
      p.depth = declaringDepth(cls, p.name);
    }
    int containerDepth = declaringDepthOf(java.awt.Container.class);
    list.sort((x, y) -> {
      int gx = x.depth > containerDepth ? 1 : 0;
      int gy = y.depth > containerDepth ? 1 : 0;
      return gx != gy ? Integer.compare(gx, gy) : x.name.compareTo(y.name);
    });
    return list;
  }

  private static int declaringDepthOf(Class<?> cls) {
    int depth = 0;
    for (Class<?> c = cls; c.getSuperclass() != null; c = c.getSuperclass()) {
      depth++;
    }
    return depth;
  }

  private static int declaringDepth(Class<?> cls, String name) {
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(cls).getPropertyDescriptors()) {
        if (pd.getName().equals(name)) {
          Method m = pd.getReadMethod() != null ? pd.getReadMethod() : pd.getWriteMethod();
          int depth = 0;
          for (Class<?> c = m == null ? cls : m.getDeclaringClass(); c.getSuperclass() != null; c = c.getSuperclass()) {
            depth++;
          }
          return depth;
        }
      }
    } catch (java.beans.IntrospectionException e) {
      return 0;
    }
    return 0;
  }

  private static java.lang.reflect.Constructor<?> annotatedConstructor(Class<?> cls) {
    for (java.lang.reflect.Constructor<?> k : cls.getConstructors()) {
      if (k.isAnnotationPresent(java.beans.ConstructorProperties.class)) {
        return k;
      }
    }
    return null;
  }

  private static Object readProperty(Object o, String name) throws Exception {
    try {
      return o.getClass().getField(name).get(o);
    } catch (NoSuchFieldException e) {
      // fall back to the accessor
    }
    String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    try {
      return o.getClass().getMethod("get" + cap).invoke(o);
    } catch (NoSuchMethodException e) {
      return o.getClass().getMethod("is" + cap).invoke(o);
    }
  }

  private static boolean sameState(Object a, Object b, int depth) {
    if (deepEquals(a, b)) {
      return true;
    }
    if (a == null || b == null || a.getClass() != b.getClass() || depth > 6) {
      return false;
    }
    Class<?> cls = a.getClass();
    if (cls.isArray()) {
      int n = Array.getLength(a);
      if (n != Array.getLength(b)) {
        return false;
      }
      for (int i = 0; i < n; i++) {
        if (!sameState(Array.get(a, i), Array.get(b, i), depth + 1)) {
          return false;
        }
      }
      return true;
    }
    if (a instanceof Collection || a instanceof Map || isValue(a) || a instanceof java.awt.Component) {
      return false;
    }
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(cls).getPropertyDescriptors()) {
        Method r = pd.getReadMethod();
        if (r == null || pd.getWriteMethod() == null || pd.getName().equals("class")
            || Boolean.TRUE.equals(pd.getValue("transient"))) {
          continue;
        }
        if (!sameState(r.invoke(a), r.invoke(b), depth + 1)) {
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean deepEquals(Object a, Object b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    if (a.getClass().isArray() && b.getClass().isArray()) {
      return java.util.Arrays.deepEquals(new Object[] {a}, new Object[] {b});
    }
    return a.equals(b);
  }

  private void indent(int depth) {
    for (int i = 0; i < depth; i++) {
      out.append(' ');
    }
  }

  private static String esc(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '<': sb.append("&lt;"); break;
        case '>': sb.append("&gt;"); break;
        case '&': sb.append("&amp;"); break;
        case '\'': sb.append("&apos;"); break;
        case '"': sb.append("&quot;"); break;
        case '\r': sb.append("&#13;"); break;
        default:
          if (c < 0x20 && c != '\n' && c != '\t') {
            sb.append("&#x").append(Integer.toHexString(c).toUpperCase()).append(';');
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  private void simple(String tag, String text, int depth) {
    indent(depth);
    out.append('<').append(tag).append('>').append(esc(text)).append("</").append(tag).append(">\n");
  }

  private void write(Object o, int depth) {
    if (o == null) {
      indent(depth);
      out.append("<null/>\n");
    } else if (o instanceof String) {
      simple("string", (String) o, depth);
    } else if (o instanceof Integer) {
      simple("int", o.toString(), depth);
    } else if (o instanceof Long) {
      simple("long", o.toString(), depth);
    } else if (o instanceof Short) {
      simple("short", o.toString(), depth);
    } else if (o instanceof Byte) {
      simple("byte", o.toString(), depth);
    } else if (o instanceof Float) {
      simple("float", o.toString(), depth);
    } else if (o instanceof Double) {
      simple("double", o.toString(), depth);
    } else if (o instanceof Boolean) {
      simple("boolean", o.toString(), depth);
    } else if (o instanceof Character) {
      simple("char", o.toString(), depth);
    } else if (o instanceof Class) {
      simple("class", ((Class<?>) o).getName(), depth);
    } else if (o instanceof Enum) {
      Class<?> ec = ((Enum<?>) o).getDeclaringClass();
      indent(depth);
      out.append("<object class=\"java.lang.Enum\" method=\"valueOf\">\n");
      simple("class", ec.getName(), depth + 1);
      simple("string", ((Enum<?>) o).name(), depth + 1);
      indent(depth);
      out.append("</object>\n");
    } else if (written.containsKey(o)) {
      indent(depth);
      out.append("<object idref=\"").append(ids.get(o)).append("\"/>\n");
    } else {
      written.put(o, Boolean.TRUE);
      idAttr(o);
      writeComposite(o, depth);
    }
  }

  private static String idBase(Class<?> c) {
    if (c.isArray()) {
      return idBase(c.getComponentType()) + "Array";
    }
    String n = c.getName();
    return n.substring(n.lastIndexOf('.') + 1);
  }

  private String idAttrForce(Object o) {
    counts.put(o, 2);
    return idAttr(o);
  }

  private String idAttr(Object o) {
    Integer c = counts.get(o);
    if (c == null || c < 2) {
      return "";
    }
    String id = ids.get(o);
    if (id == null) {
      String name = idBase(o.getClass());
      Integer n = idCounters.get(name);
      n = n == null ? 0 : n;
      idCounters.put(name, n + 1);
      id = name + n;
      ids.put(o, id);
    }
    return " id=\"" + id + "\"";
  }

  private void writeComposite(Object o, int depth) {
    Class<?> cls = o.getClass();
    String before = out.toString();
    try {
      if (cls.isArray()) {
        Class<?> comp = cls.getComponentType();
        int n = Array.getLength(o);
        indent(depth);
        out.append("<array class=\"").append(comp.getName()).append("\" length=\"").append(n).append("\"")
            .append(idAttr(o));
        StringBuilder body = new StringBuilder();
        StringBuilder saved = new StringBuilder(out);
        out.setLength(0);
        boolean any = false;
        for (int i = 0; i < n; i++) {
          Object e = Array.get(o, i);
          if (isDefault(e)) {
            continue;
          }
          any = true;
          indent(depth + 1);
          out.append("<void index=\"").append(i).append("\">\n");
          write(e, depth + 2);
          indent(depth + 1);
          out.append("</void>\n");
        }
        body.append(out);
        out.setLength(0);
        out.append(saved);
        if (any) {
          out.append(">\n").append(body);
          indent(depth);
          out.append("</array>\n");
        } else {
          out.append("/>\n");
        }
        return;
      }
      String[] fp = fieldProps.get(cls);
      if (fp != null) {
        String id = idAttrForce(o);
        indent(depth);
        out.append("<object class=\"").append(cls.getName()).append("\"").append(id).append(">\n");
        for (String n : fp) {
          indent(depth + 1);
          out.append("<void class=\"").append(cls.getName()).append("\" method=\"getField\">\n");
          simple("string", n, depth + 2);
          indent(depth + 2);
          out.append("<void method=\"set\">\n");
          indent(depth + 3);
          out.append("<object idref=\"").append(ids.get(o)).append("\"/>\n");
          write(readProperty(o, n), depth + 3);
          indent(depth + 2);
          out.append("</void>\n");
          indent(depth + 1);
          out.append("</void>\n");
        }
        indent(depth);
        out.append("</object>\n");
        return;
      }
      String[] cp = ctorProps.get(cls);
      if (cp != null) {
        indent(depth);
        out.append("<object class=\"").append(cls.getName()).append("\"").append(idAttr(o)).append(">\n");
        for (String n : cp) {
          write(readProperty(o, n), depth + 1);
        }
        indent(depth);
        out.append("</object>\n");
        return;
      }
      if (o instanceof java.awt.Color) {
        java.awt.Color c = (java.awt.Color) o;
        indent(depth);
        out.append("<object class=\"java.awt.Color\"").append(idAttr(o)).append(">\n");
        write(Integer.valueOf(c.getRed()), depth + 1);
        write(Integer.valueOf(c.getGreen()), depth + 1);
        write(Integer.valueOf(c.getBlue()), depth + 1);
        write(Integer.valueOf(c.getAlpha()), depth + 1);
        indent(depth);
        out.append("</object>\n");
        return;
      }
      if (o instanceof java.awt.Font) {
        java.awt.Font f = (java.awt.Font) o;
        indent(depth);
        out.append("<object class=\"java.awt.Font\"").append(idAttr(o)).append(">\n");
        write(f.getName(), depth + 1);
        write(Integer.valueOf(f.getStyle()), depth + 1);
        write(Integer.valueOf(f.getSize()), depth + 1);
        indent(depth);
        out.append("</object>\n");
        return;
      }
      if (o instanceof java.util.Date && cls == java.util.Date.class) {
        indent(depth);
        out.append("<object class=\"java.util.Date\"").append(idAttr(o)).append(">\n");
        write(Long.valueOf(((java.util.Date) o).getTime()), depth + 1);
        indent(depth);
        out.append("</object>\n");
        return;
      }
      if (!(o instanceof java.net.URI) && annotatedConstructor(cls) == null) {
        try {
          cls.getConstructor();
        } catch (NoSuchMethodException e) {
          throw new java.lang.InstantiationException(cls.getName());
        }
      }
      if (o instanceof java.net.URI) {
        indent(depth);
        out.append("<object class=\"java.net.URI\"").append(idAttr(o)).append(">\n");
        write(o.toString(), depth + 1);
        indent(depth);
        out.append("</object>\n");
        return;
      }
      List<String> body = new ArrayList<String>();
      StringBuilder saved = new StringBuilder(out);
      out.setLength(0);
      boolean any = false;
      boolean childrenDone = !(o instanceof java.awt.Container) || o instanceof javax.swing.JScrollPane
          || o instanceof javax.swing.JTabbedPane || o instanceof javax.swing.JSplitPane;
      if (o instanceof Collection) {
        for (Object e : (Collection<?>) o) {
          any = true;
          indent(depth + 1);
          out.append("<void method=\"add\">\n");
          write(e, depth + 2);
          indent(depth + 1);
          out.append("</void>\n");
        }
      } else if (o instanceof Map) {
        for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
          any = true;
          indent(depth + 1);
          out.append("<void method=\"put\">\n");
          write(e.getKey(), depth + 2);
          write(e.getValue(), depth + 2);
          indent(depth + 1);
          out.append("</void>\n");
        }
      } else {
        java.lang.reflect.Constructor<?> annotated = annotatedConstructor(cls);
        if (annotated != null) {
          for (String n : annotated.getAnnotation(java.beans.ConstructorProperties.class).value()) {
            any = true;
            write(readProperty(o, n), depth + 1);
          }
        }
        int containerDepth = declaringDepthOf(java.awt.Container.class);
        for (Prop p : props(o)) {
          if (!childrenDone && p.depth > containerDepth) {
            childrenDone = true;
            any |= writeChildren((java.awt.Container) o, depth);
          }
          any = true;
          indent(depth + 1);
          out.append("<void property=\"").append(p.name).append("\">\n");
          if (p.readOnly) {
            Object v = p.value;
            if (v instanceof Collection) {
              for (Object e : (Collection<?>) v) {
                indent(depth + 2);
                out.append("<void method=\"add\">\n");
                write(e, depth + 3);
                indent(depth + 2);
                out.append("</void>\n");
              }
            } else if (v instanceof Map) {
              for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                indent(depth + 2);
                out.append("<void method=\"put\">\n");
                write(e.getKey(), depth + 3);
                write(e.getValue(), depth + 3);
                indent(depth + 2);
                out.append("</void>\n");
              }
            }
          } else {
            write(p.value, depth + 2);
          }
          indent(depth + 1);
          out.append("</void>\n");
        }
      }
      if (!childrenDone) {
        any |= writeChildren((java.awt.Container) o, depth);
      }
      String inner = out.toString();
      out.setLength(0);
      out.append(saved);
      indent(depth);
      out.append("<object class=\"").append(cls.getName()).append("\"").append(idAttr(o));
      if (any) {
        out.append(">\n").append(inner);
        indent(depth);
        out.append("</object>\n");
      } else {
        out.append("/>\n");
      }
    } catch (Exception e) {
      out.setLength(0);
      out.append(before);
      listener.exceptionThrown(e);
      listener.exceptionThrown(new Exception("XMLEncoder: discarding statement XMLEncoder.writeObject("
          + cls.getName().substring(cls.getName().lastIndexOf('.') + 1).replace("$", "$") + ");"));
    }
  }

  private boolean writeChildren(java.awt.Container container, int depth) {
    boolean any = false;
    for (java.awt.Component child : container.getComponents()) {
      any = true;
      indent(depth + 1);
      out.append("<void method=\"add\">\n");
      write(child, depth + 2);
      indent(depth + 1);
      out.append("</void>\n");
    }
    return any;
  }

  private static boolean isDefault(Object e) {
    if (e == null) {
      return true;
    }
    if (e instanceof Number) {
      return ((Number) e).doubleValue() == 0.0 && !(e instanceof Double && ((Double) e).isNaN());
    }
    if (e instanceof Boolean) {
      return !((Boolean) e).booleanValue();
    }
    if (e instanceof Character) {
      return ((Character) e).charValue() == '\0';
    }
    return false;
  }
}
