/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class AnnotationSupport {
  private AnnotationSupport() { }

  private static int indexOf(Annotation[] declared, Class<?> type) {
    for (int i = 0; i < declared.length; i++) {
      if (declared[i].annotationType() == type) {
        return i;
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> A[] directlyAndIndirectlyPresent(Annotation[] declared, Class<A> type) {
    List<A> result = new ArrayList<A>();
    int directIndex = indexOf(declared, type);
    Repeatable repeatable = type.getDeclaredAnnotation(Repeatable.class);
    int containerIndex = repeatable == null ? -1 : indexOf(declared, repeatable.value());
    A[] indirect = null;
    if (containerIndex >= 0) {
      try {
        Method value = repeatable.value().getDeclaredMethod("value");
        value.setAccessible(true);
        indirect = (A[]) value.invoke(declared[containerIndex]);
      } catch (ReflectiveOperationException e) {
        throw new java.lang.annotation.AnnotationFormatError("Malformed repeatable container", e);
      }
    }
    if (directIndex >= 0 && indirect != null) {
      if (directIndex < containerIndex) {
        result.add((A) declared[directIndex]);
        for (A a : indirect) {
          result.add(a);
        }
      } else {
        for (A a : indirect) {
          result.add(a);
        }
        result.add((A) declared[directIndex]);
      }
    } else if (directIndex >= 0) {
      result.add((A) declared[directIndex]);
    } else if (indirect != null) {
      for (A a : indirect) {
        result.add(a);
      }
    }
    return result.toArray((A[]) Array.newInstance(type, result.size()));
  }
}
