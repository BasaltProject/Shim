/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.util.ArrayList;
import java.util.List;

/** Pure operations over types that need no symbol table. */
final class TypeOps {
  private TypeOps() {
  }

  static Ty erasure(Ty t) {
    switch (t.tag) {
      case Ty.CLASS:
        return t.args.length == 0 ? t : Ty.cls(t.sym);
      case Ty.ARRAY: {
        Ty e = erasure(t.elem);
        return e == t.elem ? t : Ty.array(e);
      }
      case Ty.TVAR:
        return t.tv.bounds().isEmpty() ? null : erasure(t.tv.bounds().get(0));
      case Ty.INTER:
        return erasure(t.args[0]);
      case Ty.WILD:
        return t.wildKind == 1 ? erasure(t.bound) : null;
      case Ty.IVAR:
        return t.iv.inst != null ? erasure(t.iv.inst) : null;
      default:
        return t;
    }
  }

  static Ty erasure(Ty t, Ty object) {
    Ty e = erasure(t);
    return e == null ? object : e;
  }

  static boolean hasIvar(Ty t) {
    switch (t.tag) {
      case Ty.IVAR:
        return t.iv.inst == null || hasIvar(t.iv.inst);
      case Ty.CLASS:
      case Ty.INTER:
        for (Ty a : t.args) {
          if (hasIvar(a)) {
            return true;
          }
        }
        return false;
      case Ty.ARRAY:
        return hasIvar(t.elem);
      case Ty.WILD:
        return t.bound != null && hasIvar(t.bound);
      default:
        return false;
    }
  }

  /** Replaces instantiated inference variables by their instantiation. */
  static Ty resolve(Ty t) {
    switch (t.tag) {
      case Ty.IVAR:
        return t.iv.inst != null ? resolve(t.iv.inst) : t;
      case Ty.CLASS: {
        if (t.args.length == 0) {
          return t;
        }
        Ty[] a = null;
        for (int i = 0; i < t.args.length; i++) {
          Ty r = resolve(t.args[i]);
          if (r != t.args[i]) {
            if (a == null) {
              a = t.args.clone();
            }
            a[i] = r;
          }
        }
        return a == null ? t : Ty.cls(t.sym, a);
      }
      case Ty.INTER: {
        Ty[] a = null;
        for (int i = 0; i < t.args.length; i++) {
          Ty r = resolve(t.args[i]);
          if (r != t.args[i]) {
            if (a == null) {
              a = t.args.clone();
            }
            a[i] = r;
          }
        }
        if (a == null) {
          return t;
        }
        Ty n = new Ty(Ty.INTER);
        n.args = a;
        return n;
      }
      case Ty.ARRAY: {
        Ty r = resolve(t.elem);
        return r == t.elem ? t : Ty.array(r);
      }
      case Ty.WILD: {
        if (t.bound == null) {
          return t;
        }
        Ty r = resolve(t.bound);
        return r == t.bound ? t : Ty.wild(t.wildKind, r);
      }
      default:
        return t;
    }
  }

  static Ty subst(Ty t, List<TVSym> from, List<Ty> to) {
    if (from.isEmpty()) {
      return t;
    }
    switch (t.tag) {
      case Ty.TVAR: {
        for (int i = 0; i < from.size(); i++) {
          if (from.get(i) == t.tv) {
            return i < to.size() ? boundedWildcard(t.tv, to.get(i)) : t;
          }
        }
        return t;
      }
      case Ty.CLASS: {
        if (t.args.length == 0) {
          return t;
        }
        Ty[] a = null;
        for (int i = 0; i < t.args.length; i++) {
          Ty r = subst(t.args[i], from, to);
          if (r != t.args[i]) {
            if (a == null) {
              a = t.args.clone();
            }
            a[i] = r;
          }
        }
        return a == null ? t : Ty.cls(t.sym, a);
      }
      case Ty.INTER: {
        Ty[] a = t.args.clone();
        boolean ch = false;
        for (int i = 0; i < a.length; i++) {
          Ty r = subst(a[i], from, to);
          if (r != a[i]) {
            a[i] = r;
            ch = true;
          }
        }
        if (!ch) {
          return t;
        }
        Ty n = new Ty(Ty.INTER);
        n.args = a;
        return n;
      }
      case Ty.ARRAY: {
        Ty r = subst(t.elem, from, to);
        return r == t.elem ? t : Ty.array(r);
      }
      case Ty.WILD: {
        if (t.bound == null) {
          return t;
        }
        Ty r = subst(t.bound, from, to);
        if (r == t.bound) {
          return t;
        }
        if (r.tag == Ty.WILD) {
          if (t.wildKind == 1 && r.wildKind == 1 || t.wildKind == 2 && r.wildKind == 2) {
            return r;
          }
          if (t.wildKind == 2 && r.wildKind == 1) {
            return Ty.wild(2, r.bound);
          }
          return Ty.wild(0, null);
        }
        return Ty.wild(t.wildKind, r);
      }
      default:
        return t;
    }
  }

  private static Ty boundedWildcard(TVSym tv, Ty arg) {
    if (arg.tag == Ty.WILD && arg.wildKind == 0) {
      List<Ty> bounds = tv.bounds();
      if (!bounds.isEmpty() && !bounds.get(0).isObject() && !hasTvar(bounds.get(0))) {
        return Ty.wild(1, bounds.get(0));
      }
    }
    return arg;
  }

  static List<Ty> subst(List<Ty> l, List<TVSym> from, List<Ty> to) {
    List<Ty> r = new ArrayList<Ty>(l.size());
    for (Ty t : l) {
      r.add(subst(t, from, to));
    }
    return r;
  }

  static List<Ty> tvTypes(List<TVSym> l) {
    List<Ty> r = new ArrayList<Ty>(l.size());
    for (TVSym s : l) {
      r.add(s.type);
    }
    return r;
  }

  /** Finds the supertype of t whose class is target, with the arguments of t substituted. */
  static Ty asSuper(Ty t, CSym target) {
    return asSuper(t, target, 0);
  }

  private static Ty asSuper(Ty t, CSym target, int depth) {
    if (depth > 64) {
      return null;
    }
    switch (t.tag) {
      case Ty.CLASS: {
        if (t.sym == target) {
          return t;
        }
        CSym c = t.sym;
        boolean raw = t.args.length == 0 && !c.typeParams().isEmpty();
        Ty sc = c.superclass();
        if (sc != null) {
          Ty s = raw ? erasure(sc) : subst(sc, c.tparams, java.util.Arrays.asList(t.args));
          Ty r = asSuper(s, target, depth + 1);
          if (r != null) {
            return r;
          }
        }
        for (Ty i : c.interfaces()) {
          Ty s = raw ? erasure(i) : subst(i, c.tparams, java.util.Arrays.asList(t.args));
          Ty r = asSuper(s, target, depth + 1);
          if (r != null) {
            return r;
          }
        }
        if (target.internal.equals("java/lang/Object")) {
          return Ty.cls(target);
        }
        return null;
      }
      case Ty.TVAR: {
        for (Ty b : t.tv.bounds()) {
          Ty r = asSuper(b, target, depth + 1);
          if (r != null) {
            return r;
          }
        }
        return target.internal.equals("java/lang/Object") ? Ty.cls(target) : null;
      }
      case Ty.INTER: {
        for (Ty b : t.args) {
          Ty r = asSuper(b, target, depth + 1);
          if (r != null) {
            return r;
          }
        }
        return null;
      }
      case Ty.ARRAY: {
        String n = target.internal;
        if (n.equals("java/lang/Object") || n.equals("java/lang/Cloneable") || n.equals("java/io/Serializable")) {
          return Ty.cls(target);
        }
        return null;
      }
      case Ty.IVAR:
        if (t.iv.inst != null) {
          return asSuper(t.iv.inst, target, depth + 1);
        }
        for (Ty b : t.iv.upper) {
          Ty r = asSuper(b, target, depth + 1);
          if (r != null) {
            return r;
          }
        }
        return null;
      case Ty.WILD:
        return t.wildKind == 1 ? asSuper(t.bound, target, depth + 1)
            : target.internal.equals("java/lang/Object") ? Ty.cls(target) : null;
      default:
        return null;
    }
  }

  /** Structural equality without inference side effects. */
  static boolean equal(Ty a, Ty b) {
    if (a == b) {
      return true;
    }
    a = resolve(a);
    b = resolve(b);
    if (a == b) {
      return true;
    }
    if (a.tag != b.tag) {
      return false;
    }
    switch (a.tag) {
      case Ty.CLASS: {
        if (a.sym != b.sym || a.args.length != b.args.length) {
          return false;
        }
        for (int i = 0; i < a.args.length; i++) {
          if (!equal(a.args[i], b.args[i])) {
            return false;
          }
        }
        return true;
      }
      case Ty.ARRAY:
        return equal(a.elem, b.elem);
      case Ty.TVAR:
        return a.tv == b.tv;
      case Ty.WILD:
        return a.wildKind == b.wildKind && (a.bound == null ? b.bound == null : b.bound != null && equal(a.bound, b.bound));
      case Ty.INTER: {
        if (a.args.length != b.args.length) {
          return false;
        }
        for (int i = 0; i < a.args.length; i++) {
          if (!equal(a.args[i], b.args[i])) {
            return false;
          }
        }
        return true;
      }
      case Ty.IVAR:
        return a.iv == b.iv;
      default:
        return true;
    }
  }

  static boolean hasTvar(Ty t) {
    switch (t.tag) {
      case Ty.TVAR:
        return true;
      case Ty.CLASS:
      case Ty.INTER:
        for (Ty a : t.args) {
          if (hasTvar(a)) {
            return true;
          }
        }
        return false;
      case Ty.ARRAY:
        return hasTvar(t.elem);
      case Ty.WILD:
        return t.bound != null && hasTvar(t.bound);
      default:
        return false;
    }
  }
}
