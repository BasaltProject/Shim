/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class TypeUtilities {
  private static final Map<Class<?>, Class<?>> WRAPPER_TYPES = createWrapperTypes();
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPES = invertMap(WRAPPER_TYPES);
  private static final Map<String, Class<?>> PRIMITIVE_TYPES_BY_NAME = createClassNameMapping(WRAPPER_TYPES.keySet());

  private TypeUtilities() {
  }

  public static boolean isSubtype(Class<?> subType, Class<?> superType) {
    if (superType.isAssignableFrom(subType)) {
      return true;
    }
    if (superType.isPrimitive() && subType.isPrimitive()) {
      return isProperPrimitiveSubtype(subType, superType);
    }
    return false;
  }

  public static boolean isMethodInvocationConvertible(Class<?> sourceType, Class<?> targetType) {
    if (targetType.isAssignableFrom(sourceType)) {
      return true;
    }
    if (sourceType.isPrimitive()) {
      if (targetType.isPrimitive()) {
        return isProperPrimitiveSubtype(sourceType, targetType);
      }
      return targetType.isAssignableFrom(getWrapperType(sourceType));
    }
    if (targetType.isPrimitive()) {
      final Class<?> unboxedCallSiteType = getPrimitiveType(sourceType);
      return unboxedCallSiteType != null
          && (unboxedCallSiteType == targetType || isProperPrimitiveSubtype(unboxedCallSiteType, targetType));
    }
    return false;
  }

  public static boolean isConvertibleWithoutLoss(Class<?> sourceType, Class<?> targetType) {
    if (targetType.isAssignableFrom(sourceType) || targetType == void.class) {
      return true;
    }
    if (sourceType.isPrimitive()) {
      if (targetType.isPrimitive()) {
        return isLosslessPrimitive(sourceType, targetType);
      }
      if (sourceType == void.class) {
        return targetType == Object.class;
      }
      return targetType.isAssignableFrom(getWrapperType(sourceType));
    }
    return false;
  }

  private static boolean isLosslessPrimitive(Class<?> subType, Class<?> superType) {
    if (subType == byte.class) {
      return superType == short.class || superType == int.class || superType == long.class
          || superType == float.class || superType == double.class;
    }
    if (subType == short.class) {
      return superType == int.class || superType == long.class || superType == float.class
          || superType == double.class;
    }
    if (subType == int.class) {
      return superType == long.class || superType == double.class;
    }
    if (subType == float.class) {
      return superType == double.class;
    }
    return false;
  }

  private static boolean isProperPrimitiveSubtype(Class<?> subType, Class<?> superType) {
    if (superType == void.class) {
      return subType == byte.class || subType == short.class || subType == char.class;
    }
    if (superType == boolean.class || subType == boolean.class) {
      return false;
    }
    if (subType == byte.class) {
      return superType == short.class || superType == int.class || superType == long.class
          || superType == float.class || superType == double.class;
    }
    if (subType == char.class || subType == short.class) {
      return superType == int.class || superType == long.class || superType == float.class
          || superType == double.class;
    }
    if (subType == int.class) {
      return superType == long.class || superType == float.class || superType == double.class;
    }
    if (subType == long.class) {
      return superType == float.class || superType == double.class;
    }
    if (subType == float.class) {
      return superType == double.class;
    }
    return false;
  }

  private static Map<Class<?>, Class<?>> createWrapperTypes() {
    final Map<Class<?>, Class<?>> wrapperTypes = new IdentityHashMap<>(8);
    wrapperTypes.put(boolean.class, Boolean.class);
    wrapperTypes.put(byte.class, Byte.class);
    wrapperTypes.put(char.class, Character.class);
    wrapperTypes.put(short.class, Short.class);
    wrapperTypes.put(int.class, Integer.class);
    wrapperTypes.put(long.class, Long.class);
    wrapperTypes.put(float.class, Float.class);
    wrapperTypes.put(double.class, Double.class);
    wrapperTypes.put(void.class, Void.class);
    return Collections.unmodifiableMap(wrapperTypes);
  }

  private static Map<Class<?>, Class<?>> invertMap(Map<Class<?>, Class<?>> map) {
    final Map<Class<?>, Class<?>> inverted = new IdentityHashMap<>(map.size());
    for (Map.Entry<Class<?>, Class<?>> entry : map.entrySet()) {
      inverted.put(entry.getValue(), entry.getKey());
    }
    return Collections.unmodifiableMap(inverted);
  }

  private static Map<String, Class<?>> createClassNameMapping(Iterable<Class<?>> classes) {
    final Map<String, Class<?>> map = new HashMap<>();
    for (Class<?> clazz : classes) {
      map.put(clazz.getName(), clazz);
    }
    return map;
  }

  public static Class<?> getPrimitiveTypeByName(String name) {
    return PRIMITIVE_TYPES_BY_NAME.get(name);
  }

  public static Class<?> getPrimitiveType(Class<?> wrapperType) {
    return PRIMITIVE_TYPES.get(wrapperType);
  }

  public static Class<?> getWrapperType(Class<?> primitiveType) {
    return WRAPPER_TYPES.get(primitiveType);
  }

  public static boolean isWrapperType(Class<?> type) {
    return PRIMITIVE_TYPES.containsKey(type);
  }
}
