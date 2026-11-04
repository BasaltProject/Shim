/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Member lookup: fields, methods and access control. */
final class Resolve {
  final Symtab st;
  final Tx tx;
  final Enter enter;

  Resolve(Symtab st, Tx tx, Enter enter) {
    this.st = st;
    this.tx = tx;
    this.enter = enter;
  }

  boolean isSubClass(CSym sub, CSym sup) {
    if (sub == sup) {
      return true;
    }
    return TypeOps.asSuper(Ty.cls(sub), sup) != null;
  }

  boolean accessibleClass(CSym c, Env env) {
    if ((c.flags & Sym.PUBLIC) != 0 && (c.outerClass == null || accessibleClass(c.outerClass, env))) {
      return true;
    }
    if (env.cls == null) {
      return true;
    }
    if ((c.flags & Sym.PRIVATE) != 0) {
      return c.outermost() == env.cls.outermost();
    }
    return c.pkg.equals(env.cls.pkg) || (c.flags & Sym.PROTECTED) != 0;
  }

  boolean accessible(Sym member, CSym owner, Env env) {
    int f = member.flags;
    if ((f & Sym.PUBLIC) != 0) {
      return true;
    }
    CSym from = env.cls;
    if (from == null) {
      return true;
    }
    if ((f & Sym.PRIVATE) != 0) {
      return owner.outermost() == from.outermost();
    }
    if (owner.pkg.equals(from.pkg)) {
      return true;
    }
    if ((f & Sym.PROTECTED) != 0) {
      for (CSym c = from; c != null; c = c.outerClass) {
        if (isSubClass(c, owner)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Finds a field by name in class c or its supertypes. */
  FSym findField(CSym c, String name) {
    return findField(c, name, new LinkedHashSet<CSym>(), null);
  }

  /** Like findField but skips private fields that are not accessible from env. */
  FSym findField(CSym c, String name, Env env) {
    return findField(c, name, new LinkedHashSet<CSym>(), env);
  }

  private FSym findField(CSym c, String name, Set<CSym> seen, Env env) {
    if (!seen.add(c)) {
      return null;
    }
    FSym f = c.field(name);
    if (f != null && (env == null || accessible(f, c, env))) {
      return f;
    }
    Ty sc = c.superclass();
    if (sc != null && sc.tag == Ty.CLASS) {
      f = findField(sc.sym, name, seen, env);
      if (f != null) {
        return f;
      }
    }
    for (Ty i : c.interfaces()) {
      if (i.tag == Ty.CLASS) {
        f = findField(i.sym, name, seen, env);
        if (f != null) {
          return f;
        }
      }
    }
    return null;
  }

  FSym findFieldIn(Ty site, String name, Env env) {
    for (Ty s : tx.memberSites(site)) {
      if (s.tag == Ty.CLASS) {
        FSym f = findField(s.sym, name);
        if (f != null) {
          return f;
        }
      }
    }
    return null;
  }

  void collectMethods(CSym c, String name, List<MSym> out, Set<CSym> seen) {
    boolean top = seen.isEmpty();
    if (!seen.add(c)) {
      return;
    }
    for (MSym m : c.methodsNamed(name)) {
      if (!(c.isInterface() && m.isStatic() && !top)) {
        out.add(m);
      }
    }
    Ty sc = c.superclass();
    if (sc != null && sc.tag == Ty.CLASS) {
      collectMethods(sc.sym, name, out, seen);
    }
    for (Ty i : c.interfaces()) {
      if (i.tag == Ty.CLASS) {
        collectMethods(i.sym, name, out, seen);
      }
    }
    if (c.isInterface()) {
      collectMethods(st.objectSym, name, out, seen);
    }
  }

  /** Candidate methods named name visible on a value of the given type. */
  List<MSym> methodsOf(Ty site, String name) {
    List<MSym> out = new ArrayList<MSym>();
    Set<CSym> seen = new LinkedHashSet<CSym>();
    for (Ty s : tx.memberSites(site)) {
      if (s.tag == Ty.CLASS) {
        collectMethods(s.sym, name, out, seen);
      } else {
        collectMethods(st.objectSym, name, out, seen);
      }
    }
    return out;
  }
}
