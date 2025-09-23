/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package shim;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotationInvocationHandler implements InvocationHandler {
  private Object[] data;
  private Map<String, Object> members;

  public AnnotationInvocationHandler(Object[] data) {
    this.data = data;
  }

  private Class<?> type() {
    return (Class<?>) data[1];
  }

  public static Object convert(Class<?> returnType, Object raw) {
    if (returnType.isArray() && raw instanceof Object[] && !returnType.isInstance(raw)) {
      Object[] elements = (Object[]) raw;
      Object typed = Array.newInstance(returnType.getComponentType(), elements.length);
      for (int i = 0; i < elements.length; i++) {
        Array.set(typed, i, elements[i]);
      }
      return typed;
    }
    return raw;
  }

  private synchronized Map<String, Object> members() {
    if (members == null) {
      HashMap<String, Object> defaults = new HashMap<String, Object>(0);
      for (Method m : type().getDeclaredMethods()) {
        if (m.getParameterCount() == 0 && !java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
          Object dv = m.getDefaultValue();
          if (dv != null) {
            defaults.put(m.getName(), dv);
          }
        }
      }
      LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(defaults);
      for (int i = 2; i < data.length; i += 2) {
        String name = (String) data[i];
        Object value = data[i + 1];
        try {
          value = convert(type().getDeclaredMethod(name).getReturnType(), value);
        } catch (NoSuchMethodException e) {
          // keep raw value
        }
        map.put(name, value);
      }
      members = map;
    }
    return members;
  }

  private Object memberValue(Method method) {
    Object value = members().get(method.getName());
    if (value == null) {
      throw new java.lang.annotation.IncompleteAnnotationException(
          (Class<? extends Annotation>) type(), method.getName());
    }
    if (value.getClass().isArray() && Array.getLength(value) != 0) {
      return cloneArray(value);
    }
    return value;
  }

  private static Object cloneArray(Object array) {
    int length = Array.getLength(array);
    Object copy = Array.newInstance(array.getClass().getComponentType(), length);
    System.arraycopy(array, 0, copy, 0, length);
    return copy;
  }

  public Object invoke(Object proxy, Method method, Object[] arguments) {
    String name = method.getName();
    int argc = arguments == null ? 0 : arguments.length;
    if ("annotationType".equals(name) && argc == 0) {
      return data[1];
    }
    if ("toString".equals(name) && argc == 0) {
      return toStringImpl();
    }
    if ("hashCode".equals(name) && argc == 0) {
      return Integer.valueOf(hashCodeImpl());
    }
    if ("equals".equals(name) && argc == 1 && method.getParameterTypes()[0] == Object.class) {
      return Boolean.valueOf(equalsImpl(proxy, arguments[0]));
    }
    return memberValue(method);
  }

  private String toStringImpl() {
    StringBuilder result = new StringBuilder(128);
    String canonical = type().getCanonicalName();
    result.append('@').append(canonical == null ? "<no canonical name>" : canonical).append('(');
    boolean first = true;
    boolean lone = members().size() == 1;
    for (Map.Entry<String, Object> e : members().entrySet()) {
      if (first) {
        first = false;
      } else {
        result.append(", ");
      }
      if (!lone || !"value".equals(e.getKey())) {
        result.append(e.getKey()).append('=');
      }
      lone = false;
      result.append(memberValueToString(e.getValue()));
    }
    result.append(')');
    return result.toString();
  }

  private static String quote(char ch) {
    switch (ch) {
      case '\b': return "\\b";
      case '\f': return "\\f";
      case '\n': return "\\n";
      case '\r': return "\\r";
      case '\t': return "\\t";
      case '\'': return "\\'";
      case '\"': return "\\\"";
      case '\\': return "\\\\";
      default:
        return (ch >= ' ' && ch <= '~') ? String.valueOf(ch) : String.format("\\u%04x", (int) ch);
    }
  }

  private static String memberValueToString(Object value) {
    Class<?> type = value.getClass();
    if (!type.isArray()) {
      if (type == Class.class) {
        Class<?> c = (Class<?>) value;
        String canonical = c.getCanonicalName();
        return (canonical == null ? "<no canonical name>" : canonical) + ".class";
      } else if (type == String.class) {
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : ((String) value).toCharArray()) {
          sb.append(ch == '\'' ? "'" : quote(ch));
        }
        return sb.append('"').toString();
      } else if (type == Character.class) {
        char ch = ((Character) value).charValue();
        return "'" + (ch == '"' ? "\"" : quote(ch)) + "'";
      } else if (type == Double.class) {
        double d = ((Double) value).doubleValue();
        if (Double.isNaN(d)) {
          return "0.0/0.0";
        } else if (Double.isInfinite(d)) {
          return d > 0 ? "1.0/0.0" : "-1.0/0.0";
        }
        return value.toString();
      } else if (type == Float.class) {
        float f = ((Float) value).floatValue();
        if (Float.isNaN(f)) {
          return "0.0f/0.0f";
        } else if (Float.isInfinite(f)) {
          return f > 0 ? "1.0f/0.0f" : "-1.0f/0.0f";
        }
        return value + "f";
      } else if (type == Long.class) {
        return value + "L";
      } else if (type == Byte.class) {
        return "(byte)0x" + String.format("%02x", ((Byte) value).byteValue());
      } else if (value instanceof Enum) {
        return ((Enum<?>) value).name();
      }
      return value.toString();
    }
    StringBuilder sb = new StringBuilder("{");
    int length = Array.getLength(value);
    for (int i = 0; i < length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(memberValueToString(Array.get(value, i)));
    }
    return sb.append('}').toString();
  }

  private int hashCodeImpl() {
    int result = 0;
    for (Map.Entry<String, Object> e : members().entrySet()) {
      result += (127 * e.getKey().hashCode()) ^ memberValueHashCode(e.getValue());
    }
    return result;
  }

  private static int memberValueHashCode(Object value) {
    Class<?> type = value.getClass();
    if (!type.isArray()) {
      return value.hashCode();
    }
    if (type == byte[].class) {
      return Arrays.hashCode((byte[]) value);
    } else if (type == char[].class) {
      return Arrays.hashCode((char[]) value);
    } else if (type == double[].class) {
      return Arrays.hashCode((double[]) value);
    } else if (type == float[].class) {
      return Arrays.hashCode((float[]) value);
    } else if (type == int[].class) {
      return Arrays.hashCode((int[]) value);
    } else if (type == long[].class) {
      return Arrays.hashCode((long[]) value);
    } else if (type == short[].class) {
      return Arrays.hashCode((short[]) value);
    } else if (type == boolean[].class) {
      return Arrays.hashCode((boolean[]) value);
    }
    return Arrays.hashCode((Object[]) value);
  }

  private static boolean memberValueEquals(Object a, Object b) {
    Class<?> type = a.getClass();
    if (!type.isArray()) {
      return a.equals(b);
    }
    if (a instanceof Object[] && b instanceof Object[]) {
      return Arrays.equals((Object[]) a, (Object[]) b);
    }
    if (b.getClass() != type) {
      return false;
    }
    if (type == byte[].class) {
      return Arrays.equals((byte[]) a, (byte[]) b);
    } else if (type == char[].class) {
      return Arrays.equals((char[]) a, (char[]) b);
    } else if (type == double[].class) {
      return Arrays.equals((double[]) a, (double[]) b);
    } else if (type == float[].class) {
      return Arrays.equals((float[]) a, (float[]) b);
    } else if (type == int[].class) {
      return Arrays.equals((int[]) a, (int[]) b);
    } else if (type == long[].class) {
      return Arrays.equals((long[]) a, (long[]) b);
    } else if (type == short[].class) {
      return Arrays.equals((short[]) a, (short[]) b);
    }
    return Arrays.equals((boolean[]) a, (boolean[]) b);
  }

  private boolean equalsImpl(Object proxy, Object o) {
    if (o == proxy) {
      return true;
    }
    if (!type().isInstance(o)) {
      return false;
    }
    for (Map.Entry<String, Object> e : members().entrySet()) {
      Object mine = e.getValue();
      Object theirs;
      try {
        Method m = type().getDeclaredMethod(e.getKey());
        m.setAccessible(true);
        theirs = m.invoke(o);
      } catch (ReflectiveOperationException ex) {
        return false;
      }
      if (theirs == null || !memberValueEquals(mine, theirs)) {
        return false;
      }
    }
    return true;
  }
}
