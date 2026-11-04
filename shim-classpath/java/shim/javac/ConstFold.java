/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.Tree;

/** Compile-time constant evaluation. */
final class ConstFold {
  private ConstFold() {
  }

  static Object coerce(Object v, Ty t) {
    if (v == null) {
      return null;
    }
    if (t.tag == Ty.CLASS) {
      return t.isString() && v instanceof String ? v : null;
    }
    if (t.tag == Ty.BOOLEAN) {
      return v instanceof Boolean ? v : null;
    }
    if (!t.isNumeric()) {
      return null;
    }
    if (v instanceof Boolean || v instanceof String) {
      return null;
    }
    if (v instanceof Character) {
      long c = ((Character) v).charValue();
      return fromLong(c, (double) c, t, false);
    }
    Number n = (Number) v;
    boolean fp = v instanceof Float || v instanceof Double;
    return fromLong(fp ? 0 : n.longValue(), n.doubleValue(), t, fp, n);
  }

  private static Object fromLong(long l, double d, Ty t, boolean fp) {
    return fromLong(l, d, t, fp, Long.valueOf(l));
  }

  private static Object fromLong(long l, double d, Ty t, boolean fp, Number src) {
    switch (t.tag) {
      case Ty.BYTE:
        return Byte.valueOf(fp ? (byte) (int) d : (byte) l);
      case Ty.SHORT:
        return Short.valueOf(fp ? (short) (int) d : (short) l);
      case Ty.CHAR:
        return Character.valueOf(fp ? (char) (int) d : (char) l);
      case Ty.INT:
        return Integer.valueOf(fp ? (int) d : (int) l);
      case Ty.LONG:
        return Long.valueOf(fp ? (long) d : l);
      case Ty.FLOAT:
        return Float.valueOf(fp ? src.floatValue() : (float) l);
      case Ty.DOUBLE:
        return Double.valueOf(fp ? d : (double) l);
      default:
        return null;
    }
  }

  static int intOf(Object v) {
    return v instanceof Character ? ((Character) v).charValue() : ((Number) v).intValue();
  }

  static long longOf(Object v) {
    return v instanceof Character ? ((Character) v).charValue() : ((Number) v).longValue();
  }

  static double doubleOf(Object v) {
    return v instanceof Character ? ((Character) v).charValue() : ((Number) v).doubleValue();
  }

  static float floatOf(Object v) {
    return v instanceof Character ? ((Character) v).charValue() : ((Number) v).floatValue();
  }

  static Object unary(Tree.Kind k, Object v, Ty rt) {
    if (v == null) {
      return null;
    }
    switch (k) {
      case LOGICAL_COMPLEMENT:
        return v instanceof Boolean ? Boolean.valueOf(!((Boolean) v).booleanValue()) : null;
      case UNARY_PLUS:
        return coerce(v, rt);
      case UNARY_MINUS: {
        if (v instanceof Boolean || v instanceof String) {
          return null;
        }
        switch (rt.tag) {
          case Ty.INT:
            return Integer.valueOf(-intOf(v));
          case Ty.LONG:
            return Long.valueOf(-longOf(v));
          case Ty.FLOAT:
            return Float.valueOf(-floatOf(v));
          case Ty.DOUBLE:
            return Double.valueOf(-doubleOf(v));
          default:
            return null;
        }
      }
      case BITWISE_COMPLEMENT:
        if (rt.tag == Ty.INT) {
          return Integer.valueOf(~intOf(v));
        }
        if (rt.tag == Ty.LONG) {
          return Long.valueOf(~longOf(v));
        }
        return null;
      default:
        return null;
    }
  }

  /** Folds a binary operation whose operands were already promoted to type ot. */
  static Object binary(Tree.Kind k, Object a, Object b, Ty ot) {
    if (a == null || b == null) {
      return null;
    }
    if (a instanceof String || b instanceof String) {
      if (k == Tree.Kind.PLUS) {
        return str(a) + str(b);
      }
      if (a instanceof String && b instanceof String) {
        if (k == Tree.Kind.EQUAL_TO) {
          return Boolean.valueOf(a.equals(b));
        }
        if (k == Tree.Kind.NOT_EQUAL_TO) {
          return Boolean.valueOf(!a.equals(b));
        }
      }
      return null;
    }
    if (a instanceof Boolean && b instanceof Boolean) {
      boolean x = ((Boolean) a).booleanValue();
      boolean y = ((Boolean) b).booleanValue();
      switch (k) {
        case CONDITIONAL_AND:
        case AND:
          return Boolean.valueOf(x & y);
        case CONDITIONAL_OR:
        case OR:
          return Boolean.valueOf(x | y);
        case XOR:
        case NOT_EQUAL_TO:
          return Boolean.valueOf(x ^ y);
        case EQUAL_TO:
          return Boolean.valueOf(x == y);
        default:
          return null;
      }
    }
    if (a instanceof Boolean || b instanceof Boolean) {
      return null;
    }
    if (k == Tree.Kind.LEFT_SHIFT || k == Tree.Kind.RIGHT_SHIFT || k == Tree.Kind.UNSIGNED_RIGHT_SHIFT) {
      long sh = longOf(b);
      if (ot.tag == Ty.INT) {
        int x = intOf(a);
        int s = (int) sh;
        return Integer.valueOf(k == Tree.Kind.LEFT_SHIFT ? x << s : k == Tree.Kind.RIGHT_SHIFT ? x >> s : x >>> s);
      }
      long x = longOf(a);
      int s = (int) sh;
      return Long.valueOf(k == Tree.Kind.LEFT_SHIFT ? x << s : k == Tree.Kind.RIGHT_SHIFT ? x >> s : x >>> s);
    }
    switch (ot.tag) {
      case Ty.INT: {
        int x = intOf(a);
        int y = intOf(b);
        return foldInt(k, x, y);
      }
      case Ty.LONG: {
        long x = longOf(a);
        long y = longOf(b);
        return foldLong(k, x, y);
      }
      case Ty.FLOAT: {
        float x = floatOf(a);
        float y = floatOf(b);
        switch (k) {
          case PLUS:
            return Float.valueOf(x + y);
          case MINUS:
            return Float.valueOf(x - y);
          case MULTIPLY:
            return Float.valueOf(x * y);
          case DIVIDE:
            return Float.valueOf(x / y);
          case REMAINDER:
            return Float.valueOf(x % y);
          case LESS_THAN:
            return Boolean.valueOf(x < y);
          case GREATER_THAN:
            return Boolean.valueOf(x > y);
          case LESS_THAN_EQUAL:
            return Boolean.valueOf(x <= y);
          case GREATER_THAN_EQUAL:
            return Boolean.valueOf(x >= y);
          case EQUAL_TO:
            return Boolean.valueOf(x == y);
          case NOT_EQUAL_TO:
            return Boolean.valueOf(x != y);
          default:
            return null;
        }
      }
      case Ty.DOUBLE: {
        double x = doubleOf(a);
        double y = doubleOf(b);
        switch (k) {
          case PLUS:
            return Double.valueOf(x + y);
          case MINUS:
            return Double.valueOf(x - y);
          case MULTIPLY:
            return Double.valueOf(x * y);
          case DIVIDE:
            return Double.valueOf(x / y);
          case REMAINDER:
            return Double.valueOf(x % y);
          case LESS_THAN:
            return Boolean.valueOf(x < y);
          case GREATER_THAN:
            return Boolean.valueOf(x > y);
          case LESS_THAN_EQUAL:
            return Boolean.valueOf(x <= y);
          case GREATER_THAN_EQUAL:
            return Boolean.valueOf(x >= y);
          case EQUAL_TO:
            return Boolean.valueOf(x == y);
          case NOT_EQUAL_TO:
            return Boolean.valueOf(x != y);
          default:
            return null;
        }
      }
      default:
        return null;
    }
  }

  private static Object foldInt(Tree.Kind k, int x, int y) {
    switch (k) {
      case PLUS:
        return Integer.valueOf(x + y);
      case MINUS:
        return Integer.valueOf(x - y);
      case MULTIPLY:
        return Integer.valueOf(x * y);
      case DIVIDE:
        return y == 0 ? null : Integer.valueOf(x / y);
      case REMAINDER:
        return y == 0 ? null : Integer.valueOf(x % y);
      case AND:
        return Integer.valueOf(x & y);
      case OR:
        return Integer.valueOf(x | y);
      case XOR:
        return Integer.valueOf(x ^ y);
      case LESS_THAN:
        return Boolean.valueOf(x < y);
      case GREATER_THAN:
        return Boolean.valueOf(x > y);
      case LESS_THAN_EQUAL:
        return Boolean.valueOf(x <= y);
      case GREATER_THAN_EQUAL:
        return Boolean.valueOf(x >= y);
      case EQUAL_TO:
        return Boolean.valueOf(x == y);
      case NOT_EQUAL_TO:
        return Boolean.valueOf(x != y);
      default:
        return null;
    }
  }

  private static Object foldLong(Tree.Kind k, long x, long y) {
    switch (k) {
      case PLUS:
        return Long.valueOf(x + y);
      case MINUS:
        return Long.valueOf(x - y);
      case MULTIPLY:
        return Long.valueOf(x * y);
      case DIVIDE:
        return y == 0 ? null : Long.valueOf(x / y);
      case REMAINDER:
        return y == 0 ? null : Long.valueOf(x % y);
      case AND:
        return Long.valueOf(x & y);
      case OR:
        return Long.valueOf(x | y);
      case XOR:
        return Long.valueOf(x ^ y);
      case LESS_THAN:
        return Boolean.valueOf(x < y);
      case GREATER_THAN:
        return Boolean.valueOf(x > y);
      case LESS_THAN_EQUAL:
        return Boolean.valueOf(x <= y);
      case GREATER_THAN_EQUAL:
        return Boolean.valueOf(x >= y);
      case EQUAL_TO:
        return Boolean.valueOf(x == y);
      case NOT_EQUAL_TO:
        return Boolean.valueOf(x != y);
      default:
        return null;
    }
  }

  static String str(Object v) {
    return String.valueOf(v);
  }
}
