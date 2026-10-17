/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.management;

import java.lang.management.MemoryUsage;
import java.util.List;
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
      return ((List<?>) value).toArray(new String[0]);
    }
    return value;
  }
}
