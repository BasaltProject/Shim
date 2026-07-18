/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import sun.misc.Unsafe;

final class FastVar {
  static final Unsafe U = Unsafe.getUnsafe();

  private FastVar() { }

  static char kind(Class<?> type) {
    if (type == int.class) return 'I';
    if (type == long.class) return 'J';
    return type.isPrimitive() ? 0 : 'L';
  }

  private static int toInt(Object value) {
    return value instanceof Character ? ((Character) value).charValue() : ((Number) value).intValue();
  }

  private static long toLong(Object value) {
    return value instanceof Character ? ((Character) value).charValue() : ((Number) value).longValue();
  }

  static Object get(char kind, Object base, long offset) {
    switch (kind) {
    case 'I': return Integer.valueOf(U.getIntVolatile(base, offset));
    case 'J': return Long.valueOf(U.getLongVolatile(base, offset));
    default: return U.getObjectVolatile(base, offset);
    }
  }

  static void set(char kind, Object base, long offset, Object value) {
    switch (kind) {
    case 'I': U.putIntVolatile(base, offset, toInt(value)); break;
    case 'J': U.putLongVolatile(base, offset, toLong(value)); break;
    default: U.putObjectVolatile(base, offset, value);
    }
  }

  static boolean compareAndSet(char kind, Object base, long offset, Object expected, Object update) {
    switch (kind) {
    case 'I': return U.compareAndSwapInt(base, offset, toInt(expected), toInt(update));
    case 'J': return U.compareAndSwapLong(base, offset, toLong(expected), toLong(update));
    default: return U.compareAndSwapObject(base, offset, expected, update);
    }
  }

  static Object compareAndExchange(char kind, Object base, long offset, Object expected, Object update) {
    for (;;) {
      Object current = get(kind, base, offset);
      boolean same = kind == 'L' ? current == expected : current.equals(kind == 'I'
          ? (Object) Integer.valueOf(toInt(expected)) : (Object) Long.valueOf(toLong(expected)));
      if (!same) return current;
      if (compareAndSet(kind, base, offset, expected, update)) return current;
    }
  }

  static Object getAndSet(char kind, Object base, long offset, Object value) {
    for (;;) {
      Object current = get(kind, base, offset);
      if (compareAndSet(kind, base, offset, current, value)) return current;
    }
  }

  static Object getAndAdd(char kind, Object base, long offset, Object delta) {
    for (;;) {
      Object current = get(kind, base, offset);
      Object next;
      if (kind == 'I') {
        next = Integer.valueOf(((Integer) current).intValue() + toInt(delta));
      } else if (kind == 'J') {
        next = Long.valueOf(((Long) current).longValue() + toLong(delta));
      } else {
        throw new UnsupportedOperationException("getAndAdd not supported on reference types");
      }
      if (compareAndSet(kind, base, offset, current, next)) return current;
    }
  }
}
