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

  public XmlWrite(ExceptionListener listener) {
    this.listener = listener;
    ctorProps.put(java.awt.Point.class, new String[] {"x", "y"});
    ctorProps.put(java.awt.Dimension.class, new String[] {"width", "height"});
    ctorProps.put(java.awt.Rectangle.class, new String[] {"x", "y", "width", "height"});
    ctorProps.put(java.awt.Insets.class, new String[] {"top", "left", "bottom", "right"});
  }

  public String render(Object root) {
    counts.clear();
    ids.clear();
    written.clear();
    scan(root, new IdentityHashMap<Object, Boolean>());
    for (Map.Entry<Object, Integer> e : counts.entrySet()) {
      if (e.getValue() > 1) {
        String name = e.getKey().getClass().getSimpleName();
        if (name.isEmpty()) {
          name = "object";
        }
        Integer n = idCounters.get(name);
        n = n == null ? 0 : n;
        idCounters.put(name, n + 1);
        ids.put(e.getKey(), name + n);
      }
    }
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
    try {
      def = cls.getConstructor().newInstance();
    } catch (Exception e) {
      return list;
    }
    BeanInfo info = Introspector.getBeanInfo(cls);
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      Method r = pd.getReadMethod();
      Method w = pd.getWriteMethod();
      if (r == null || pd.getName().equals("class")) {
        continue;
      }
      if (w == null && !(Collection.class.isAssignableFrom(r.getReturnType()) || Map.class.isAssignableFrom(r.getReturnType()))) {
        continue;
      }
      Object v = r.invoke(o);
      Object d = r.invoke(def);
      if (w != null) {
        if (!deepEquals(v, d)) {
          list.add(new Prop(pd.getName(), v, false));
        }
      } else if (v != null && !deepEquals(v, d)) {
        list.add(new Prop(pd.getName(), v, true));
      }
    }
    return list;
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
      out.append("<object class=\"").append(ec.getName()).append("\" method=\"valueOf\">\n");
      simple("string", ((Enum<?>) o).name(), depth + 1);
      indent(depth);
      out.append("</object>\n");
    } else if (written.containsKey(o)) {
      indent(depth);
      out.append("<object idref=\"").append(ids.get(o)).append("\"/>\n");
    } else {
      written.put(o, Boolean.TRUE);
      writeComposite(o, depth);
    }
  }

  private String idAttr(Object o) {
    String id = ids.get(o);
    return id == null ? "" : " id=\"" + id + "\"";
  }

  private void writeComposite(Object o, int depth) {
    Class<?> cls = o.getClass();
    try {
      if (cls.isArray()) {
        Class<?> comp = cls.getComponentType();
        int n = Array.getLength(o);
        indent(depth);
        out.append("<array").append(idAttr(o)).append(" class=\"").append(comp.getName()).append("\" length=\"")
            .append(n).append("\"");
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
      String[] cp = ctorProps.get(cls);
      if (cp != null) {
        indent(depth);
        out.append("<object").append(idAttr(o)).append(" class=\"").append(cls.getName()).append("\">\n");
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
        out.append("<object").append(idAttr(o)).append(" class=\"java.awt.Color\">\n");
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
        out.append("<object").append(idAttr(o)).append(" class=\"java.awt.Font\">\n");
        write(f.getName(), depth + 1);
        write(Integer.valueOf(f.getStyle()), depth + 1);
        write(Integer.valueOf(f.getSize()), depth + 1);
        indent(depth);
        out.append("</object>\n");
        return;
      }
      if (o instanceof java.util.Date && cls == java.util.Date.class) {
        indent(depth);
        out.append("<object").append(idAttr(o)).append(" class=\"java.util.Date\">\n");
        write(Long.valueOf(((java.util.Date) o).getTime()), depth + 1);
        indent(depth);
        out.append("</object>\n");
        return;
      }
      List<String> body = new ArrayList<String>();
      StringBuilder saved = new StringBuilder(out);
      out.setLength(0);
      boolean any = false;
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
        for (Prop p : props(o)) {
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
      String inner = out.toString();
      out.setLength(0);
      out.append(saved);
      indent(depth);
      out.append("<object").append(idAttr(o)).append(" class=\"").append(cls.getName()).append("\"");
      if (any) {
        out.append(">\n").append(inner);
        indent(depth);
        out.append("</object>\n");
      } else {
        out.append("/>\n");
      }
    } catch (Exception e) {
      listener.exceptionThrown(new Exception("Encoder: cannot persist " + cls.getName(), e));
    }
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
