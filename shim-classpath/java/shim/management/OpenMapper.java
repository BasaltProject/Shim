/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.management;

import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public final class OpenMapper {
  private static final String[] kMemoryItems = {"committed", "init", "max", "used"};
  private static CompositeType memoryType;

  private OpenMapper() {
  }

  static synchronized CompositeType memoryUsageType() {
    if (memoryType == null) {
      try {
        OpenType<?>[] types = {SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG};
        memoryType = new CompositeType("java.lang.management.MemoryUsage", "java.lang.management.MemoryUsage",
            kMemoryItems, kMemoryItems, types);
      } catch (OpenDataException e) {
        throw new Error(e);
      }
    }
    return memoryType;
  }

  static String openClassName(Class<?> c) {
    if (c == MemoryUsage.class) {
      return CompositeData.class.getName();
    }
    if (List.class.isAssignableFrom(c)) {
      return "[Ljava.lang.String;";
    }
    if (c.isEnum()) {
      return String.class.getName();
    }
    return c.getName();
  }

  static OpenType<?> simpleOf(Class<?> c) {
    if (c == boolean.class || c == Boolean.class) {
      return SimpleType.BOOLEAN;
    }
    if (c == int.class || c == Integer.class) {
      return SimpleType.INTEGER;
    }
    if (c == long.class || c == Long.class) {
      return SimpleType.LONG;
    }
    if (c == double.class || c == Double.class) {
      return SimpleType.DOUBLE;
    }
    if (c == float.class || c == Float.class) {
      return SimpleType.FLOAT;
    }
    if (c == short.class || c == Short.class) {
      return SimpleType.SHORT;
    }
    if (c == byte.class || c == Byte.class) {
      return SimpleType.BYTE;
    }
    if (c == char.class || c == Character.class) {
      return SimpleType.CHARACTER;
    }
    if (c == String.class || c.isEnum()) {
      return SimpleType.STRING;
    }
    if (c == ObjectName.class) {
      return SimpleType.OBJECTNAME;
    }
    if (c == void.class) {
      return SimpleType.VOID;
    }
    if (c == MemoryUsage.class) {
      return memoryUsageType();
    }
    return null;
  }

  static Descriptor attributeDescriptor(Class<?> c) {
    OpenType<?> t = simpleOf(c);
    if (t == null) {
      return null;
    }
    return new ImmutableDescriptor(new String[] {"openType", "originalType"}, new Object[] {t, c.getName()});
  }

  public static Object fromOpen(Object value, Class<?> target) {
    if (value instanceof CompositeData && target == MemoryUsage.class) {
      return MemoryUsage.from((CompositeData) value);
    }
    if (value instanceof String[] && List.class.isAssignableFrom(target)) {
      return java.util.Arrays.asList((String[]) value);
    }
    if (value instanceof CompositeData[] && List.class.isAssignableFrom(target)) {
      List<Object> out = new ArrayList<Object>();
      for (CompositeData d : (CompositeData[]) value) {
        out.add(fromOpen(d, Object.class));
      }
      return out;
    }
    if (value instanceof CompositeData) {
      try {
        Class<?> c = Class.forName(((CompositeData) value).getCompositeType().getTypeName());
        if (isBean(c)) {
          return c.getMethod("from", CompositeData.class).invoke(null, value);
        }
      } catch (ReflectiveOperationException e) {
        return value;
      }
    }
    if (value instanceof String && target.isEnum()) {
      for (Object c : target.getEnumConstants()) {
        if (c.toString().equals(value) || ((Enum<?>) c).name().equals(value)) {
          return c;
        }
      }
    }
    return value;
  }

  static Object toOpen(Object value) {
    if (value instanceof MemoryUsage) {
      MemoryUsage u = (MemoryUsage) value;
      try {
        return new CompositeDataSupport(memoryUsageType(), kMemoryItems,
            new Object[] {u.getCommitted(), u.getInit(), u.getMax(), u.getUsed()});
      } catch (OpenDataException e) {
        throw new Error(e);
      }
    }
    if (value instanceof Enum) {
      return value.toString();
    }
    if (value instanceof List) {
      return listToOpen((List<?>) value);
    }
    if (value instanceof Map) {
      return mapToOpen((Map<?, ?>) value);
    }
    if (value != null && isBean(value.getClass())) {
      return beanToOpen(value);
    }
    return value;
  }

  static boolean isBean(Class<?> c) {
    try {
      Method m = c.getMethod("from", CompositeData.class);
      return Modifier.isStatic(m.getModifiers());
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private static Object listToOpen(List<?> l) {
    boolean allStrings = true;
    for (Object o : l) {
      if (!(o instanceof String)) {
        allStrings = false;
      }
    }
    if (allStrings) {
      return l.toArray(new String[0]);
    }
    Object[] mapped = new Object[l.size()];
    for (int i = 0; i < mapped.length; i++) {
      mapped[i] = toOpen(l.get(i));
    }
    if (mapped.length > 0 && mapped[0] instanceof CompositeData) {
      return Arrays.copyOf(mapped, mapped.length, CompositeData[].class);
    }
    return mapped;
  }

  private static Object mapToOpen(Map<?, ?> m) {
    try {
      String[] names = {"key", "value"};
      OpenType<?>[] types = {SimpleType.STRING, SimpleType.STRING};
      CompositeType row = new CompositeType("java.util.Map", "java.util.Map", names, names, types);
      TabularType tt = new TabularType("java.util.Map", "java.util.Map", row, new String[] {"key"});
      TabularDataSupport t = new TabularDataSupport(tt);
      for (Map.Entry<?, ?> e : m.entrySet()) {
        t.put(new CompositeDataSupport(row, names, new Object[] {String.valueOf(e.getKey()),
            e.getValue() == null ? null : String.valueOf(e.getValue())}));
      }
      return t;
    } catch (OpenDataException e) {
      throw new Error(e);
    }
  }

  private static List<Method> getters(Class<?> c) {
    List<Method> l = new ArrayList<Method>();
    for (Method m : c.getMethods()) {
      String n = m.getName();
      if (m.getParameterTypes().length != 0 || Modifier.isStatic(m.getModifiers()) || m.getReturnType() == void.class
          || n.equals("getClass")) {
        continue;
      }
      if ((n.startsWith("get") && n.length() > 3)
          || (n.startsWith("is") && n.length() > 2 && m.getReturnType() == boolean.class)) {
        l.add(m);
      }
    }
    Collections.sort(l, new Comparator<Method>() {
      public int compare(Method a, Method b) {
        return itemName(a).compareTo(itemName(b));
      }
    });
    return l;
  }

  private static String itemName(Method m) {
    String n = m.getName();
    n = n.startsWith("is") ? n.substring(2) : n.substring(3);
    return Character.toLowerCase(n.charAt(0)) + n.substring(1);
  }

  private static OpenType<?> typeOf(Type t) throws OpenDataException {
    if (t instanceof Class) {
      Class<?> c = (Class<?>) t;
      OpenType<?> s = simpleOf(c);
      if (s != null) {
        return s;
      }
      if (c.isArray()) {
        return ArrayType.getArrayType(typeOf(c.getComponentType()));
      }
      if (Map.class.isAssignableFrom(c)) {
        return mapTypeOf();
      }
      return compositeTypeOf(c);
    }
    if (t instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) t;
      Class<?> raw = (Class<?>) pt.getRawType();
      if (List.class.isAssignableFrom(raw)) {
        return ArrayType.getArrayType(typeOf(pt.getActualTypeArguments()[0]));
      }
      if (Map.class.isAssignableFrom(raw)) {
        return mapTypeOf();
      }
    }
    return SimpleType.STRING;
  }

  private static TabularType mapTypeOf() throws OpenDataException {
    String[] names = {"key", "value"};
    OpenType<?>[] types = {SimpleType.STRING, SimpleType.STRING};
    CompositeType row = new CompositeType("java.util.Map", "java.util.Map", names, names, types);
    return new TabularType("java.util.Map", "java.util.Map", row, new String[] {"key"});
  }

  private static CompositeType compositeTypeOf(Class<?> c) throws OpenDataException {
    List<Method> gs = getters(c);
    String[] names = new String[gs.size()];
    OpenType<?>[] types = new OpenType<?>[gs.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = itemName(gs.get(i));
      types[i] = typeOf(gs.get(i).getGenericReturnType());
    }
    return new CompositeType(c.getName(), c.getName(), names, names, types);
  }

  private static Object beanToOpen(Object bean) {
    try {
      CompositeType ct = compositeTypeOf(bean.getClass());
      List<Method> gs = getters(bean.getClass());
      Object[] vals = new Object[gs.size()];
      for (int i = 0; i < vals.length; i++) {
        vals[i] = toOpen(gs.get(i).invoke(bean));
      }
      return new CompositeDataSupport(ct, ct.keySet().toArray(new String[0]), reorder(ct, gs, vals));
    } catch (ReflectiveOperationException e) {
      throw new Error(e);
    } catch (OpenDataException e) {
      throw new Error(e);
    }
  }

  private static Object[] reorder(CompositeType ct, List<Method> gs, Object[] vals) {
    String[] keys = ct.keySet().toArray(new String[0]);
    Object[] out = new Object[keys.length];
    for (int i = 0; i < keys.length; i++) {
      for (int j = 0; j < gs.size(); j++) {
        if (itemName(gs.get(j)).equals(keys[i])) {
          out[i] = vals[j];
        }
      }
    }
    return out;
  }
}
