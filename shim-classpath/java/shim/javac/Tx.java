/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Type relations: subtyping, conversion, least upper bounds and function types. */
final class Tx {
  static final String[] BOXES = {"java/lang/Byte", "java/lang/Short", "java/lang/Character", "java/lang/Integer",
      "java/lang/Long", "java/lang/Float", "java/lang/Double", "java/lang/Boolean", "java/lang/Void"};

  static final class MType {
    List<TVSym> tparams;
    List<Ty> params;
    Ty ret;
    List<Ty> thrown;
    List<TVSym> cfrom;
    List<Ty> cto;
  }

  static final class FnType {
    MSym method;
    List<Ty> params;
    Ty ret;
    List<Ty> thrown;
  }

  final Symtab st;
  private final Map<CSym, MSym> samCache = new HashMap<CSym, MSym>();
  private int depth;
  boolean conflict;

  Tx(Symtab st) {
    this.st = st;
  }

  Ty object() {
    return st.object;
  }

  Ty box(Ty prim) {
    return Ty.cls(st.lookup(BOXES[prim.tag]));
  }

  Ty unboxed(Ty t) {
    switch (t.tag) {
      case Ty.CLASS: {
        String n = t.sym.internal;
        if (!n.startsWith("java/lang/")) {
          return null;
        }
        for (int i = 0; i < 8; i++) {
          if (BOXES[i].equals(n)) {
            return primOf(i);
          }
        }
        return null;
      }
      case Ty.TVAR:
        for (Ty b : t.tv.bounds()) {
          Ty u = unboxed(b);
          if (u != null) {
            return u;
          }
        }
        return null;
      case Ty.INTER:
        for (Ty b : t.args) {
          Ty u = unboxed(b);
          if (u != null) {
            return u;
          }
        }
        return null;
      case Ty.IVAR:
        return t.iv.inst != null ? unboxed(t.iv.inst) : null;
      case Ty.WILD:
        return t.wildKind == 1 ? unboxed(t.bound) : null;
      default:
        return null;
    }
  }

  static Ty primOf(int tag) {
    switch (tag) {
      case Ty.BYTE:
        return Ty.tByte;
      case Ty.SHORT:
        return Ty.tShort;
      case Ty.CHAR:
        return Ty.tChar;
      case Ty.INT:
        return Ty.tInt;
      case Ty.LONG:
        return Ty.tLong;
      case Ty.FLOAT:
        return Ty.tFloat;
      case Ty.DOUBLE:
        return Ty.tDouble;
      default:
        return Ty.tBoolean;
    }
  }

  static boolean primWiden(int s, int t) {
    if (s == t) {
      return true;
    }
    if (s == Ty.BOOLEAN || t == Ty.BOOLEAN || t > Ty.DOUBLE || s > Ty.DOUBLE) {
      return false;
    }
    if (t == Ty.CHAR) {
      return false;
    }
    if (s == Ty.CHAR) {
      return t >= Ty.INT;
    }
    return t > s;
  }

  /* ---------- subtyping with inference bounds ---------- */

  boolean isSubtype(Ty s, Ty t) {
    if (s == t) {
      return true;
    }
    if (depth > 40) {
      return true;
    }
    depth++;
    try {
      return sub(s, t);
    } finally {
      depth--;
    }
  }

  private boolean sub(Ty s, Ty t) {
    if (t.tag == Ty.IVAR) {
      if (t.iv.inst != null) {
        return isSubtype(s, t.iv.inst);
      }
      if (s.tag == Ty.IVAR && s.iv == t.iv) {
        return true;
      }
      if (s.isPrim()) {
        return false;
      }
      addLower(t, s);
      if (s.tag == Ty.IVAR && s.iv.inst == null) {
        addUpper(s, t);
      }
      return true;
    }
    if (s.tag == Ty.IVAR) {
      if (s.iv.inst != null) {
        return isSubtype(s.iv.inst, t);
      }
      if (t.isPrim()) {
        return false;
      }
      addUpper(s, t);
      return true;
    }
    if (s.tag == Ty.ERROR || t.tag == Ty.ERROR) {
      return true;
    }
    if (s.isPrim() || t.isPrim()) {
      return s.isPrim() && t.isPrim() && primWiden(s.tag, t.tag);
    }
    if (s.tag == Ty.VOID || t.tag == Ty.VOID) {
      return s.tag == t.tag;
    }
    if (s.tag == Ty.NULL) {
      return true;
    }
    if (t.tag == Ty.NULL) {
      return false;
    }
    if (t.tag == Ty.INTER) {
      for (Ty b : t.args) {
        if (!isSubtype(s, b)) {
          return false;
        }
      }
      return true;
    }
    if (s.tag == Ty.INTER) {
      for (Ty b : s.args) {
        if (isSubtype(b, t)) {
          return true;
        }
      }
      return false;
    }
    if (t.tag == Ty.WILD) {
      return t.wildKind == 2 ? isSubtype(s, t.bound) : t.wildKind == 0 || false;
    }
    if (s.tag == Ty.WILD) {
      return s.wildKind == 1 ? isSubtype(s.bound, t) : t.isObject();
    }
    if (s.tag == Ty.TVAR) {
      if (t.tag == Ty.TVAR && t.tv == s.tv) {
        return true;
      }
      for (Ty b : s.tv.bounds()) {
        if (isSubtype(b, t)) {
          return true;
        }
      }
      return t.isObject();
    }
    if (t.tag == Ty.TVAR) {
      return false;
    }
    if (s.tag == Ty.ARRAY) {
      if (t.tag == Ty.ARRAY) {
        if (s.elem.isPrim() || t.elem.isPrim()) {
          return s.elem.tag == t.elem.tag;
        }
        return isSubtype(s.elem, t.elem);
      }
      if (t.tag == Ty.CLASS) {
        String n = t.sym.internal;
        return n.equals("java/lang/Object") || n.equals("java/lang/Cloneable") || n.equals("java/io/Serializable");
      }
      return false;
    }
    if (s.tag == Ty.CLASS && t.tag == Ty.CLASS) {
      Ty sup = TypeOps.asSuper(s, t.sym);
      if (sup == null) {
        return false;
      }
      if (t.args.length == 0 || sup.args.length == 0) {
        return true;
      }
      if (sup.args.length != t.args.length) {
        return true;
      }
      for (int i = 0; i < t.args.length; i++) {
        if (!containsType(t.args[i], sup.args[i], declaredBound(t.sym, i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private Ty declaredBound(CSym c, int i) {
    List<TVSym> tps = c.typeParams();
    if (i >= tps.size() || tps.get(i).bounds().isEmpty()) {
      return st.object;
    }
    Ty b = tps.get(i).bounds().get(0);
    return TypeOps.hasTvar(b) ? TypeOps.erasure(b, st.object) : b;
  }

  boolean containsType(Ty t, Ty s) {
    return containsType(t, s, st.object);
  }

  boolean containsType(Ty t, Ty s, Ty declBound) {
    if (t.tag == Ty.WILD) {
      switch (t.wildKind) {
        case 0:
          return true;
        case 1: {
          if (s.tag == Ty.WILD) {
            return s.wildKind == 1 ? isSubtype(s.bound, t.bound) : t.bound.isObject() || isSubtype(declBound, t.bound);
          }
          return isSubtype(s, t.bound);
        }
        default: {
          if (s.tag == Ty.WILD) {
            return s.wildKind == 2 && isSubtype(t.bound, s.bound);
          }
          return isSubtype(t.bound, s);
        }
      }
    }
    if (s.tag == Ty.WILD) {
      if (s.wildKind == 2 && s.bound.isObject() && t.isObject()) {
        return true;
      }
      if (t.tag == Ty.IVAR && t.iv.inst == null) {
        return sameType(t, s.wildKind == 1 ? s.bound : declBound) || true;
      }
      return false;
    }
    return sameType(t, s);
  }

  boolean sameType(Ty a, Ty b) {
    if (a == b) {
      return true;
    }
    if (depth > 40) {
      return true;
    }
    depth++;
    try {
      return same(a, b);
    } finally {
      depth--;
    }
  }

  private boolean same(Ty a, Ty b) {
    if (a.tag == Ty.IVAR && a.iv.inst != null) {
      return sameType(a.iv.inst, b);
    }
    if (b.tag == Ty.IVAR && b.iv.inst != null) {
      return sameType(a, b.iv.inst);
    }
    if (a.tag == Ty.IVAR) {
      if (b.tag == Ty.IVAR && a.iv == b.iv) {
        return true;
      }
      if (b.isPrim() || b.tag == Ty.NULL) {
        return false;
      }
      addEq(a, b);
      if (b.tag == Ty.IVAR) {
        addEq(b, a);
      }
      return true;
    }
    if (b.tag == Ty.IVAR) {
      if (a.isPrim() || a.tag == Ty.NULL) {
        return false;
      }
      addEq(b, a);
      return true;
    }
    if (a.tag == Ty.ERROR || b.tag == Ty.ERROR) {
      return true;
    }
    if (a.tag != b.tag) {
      return false;
    }
    switch (a.tag) {
      case Ty.CLASS: {
        if (a.sym != b.sym) {
          return false;
        }
        if (a.args.length != b.args.length) {
          return a.args.length == 0 || b.args.length == 0;
        }
        for (int i = 0; i < a.args.length; i++) {
          if (!sameType(a.args[i], b.args[i])) {
            return false;
          }
        }
        return true;
      }
      case Ty.ARRAY:
        return sameType(a.elem, b.elem);
      case Ty.TVAR:
        return a.tv == b.tv;
      case Ty.WILD: {
        if (a.wildKind != b.wildKind) {
          return false;
        }
        return a.bound == null || sameType(a.bound, b.bound);
      }
      case Ty.INTER: {
        if (a.args.length != b.args.length) {
          return false;
        }
        for (int i = 0; i < a.args.length; i++) {
          if (!sameType(a.args[i], b.args[i])) {
            return false;
          }
        }
        return true;
      }
      default:
        return true;
    }
  }

  private static boolean has(List<Ty> l, Ty t) {
    for (Ty x : l) {
      if (TypeOps.equal(x, t)) {
        return true;
      }
    }
    return false;
  }

  private void addLower(Ty v, Ty s) {
    if (has(v.iv.lower, s)) {
      return;
    }
    v.iv.lower.add(s);
    for (Ty e : new ArrayList<Ty>(v.iv.eq)) {
      if (!isSubtype(s, e)) {
        conflict = true;
      }
    }
    for (Ty u : new ArrayList<Ty>(v.iv.upper)) {
      if (!isSubtype(s, u)) {
        conflict = true;
      }
    }
  }

  private void addUpper(Ty v, Ty t) {
    if (has(v.iv.upper, t)) {
      return;
    }
    v.iv.upper.add(t);
    for (Ty e : new ArrayList<Ty>(v.iv.eq)) {
      if (!isSubtype(e, t)) {
        conflict = true;
      }
    }
    for (Ty l : new ArrayList<Ty>(v.iv.lower)) {
      if (!isSubtype(l, t)) {
        conflict = true;
      }
    }
  }

  private void addEq(Ty v, Ty e) {
    if (has(v.iv.eq, e)) {
      return;
    }
    v.iv.eq.add(e);
    for (Ty l : new ArrayList<Ty>(v.iv.lower)) {
      if (!isSubtype(l, e)) {
        conflict = true;
      }
    }
    for (Ty u : new ArrayList<Ty>(v.iv.upper)) {
      if (!isSubtype(e, u)) {
        conflict = true;
      }
    }
    for (Ty o : new ArrayList<Ty>(v.iv.eq)) {
      if (o != e && !sameType(o, e)) {
        conflict = true;
      }
    }
  }

  /* ---------- conversions ---------- */

  boolean isStrictConvertible(Ty s, Ty t) {
    if (s.isPrim() != t.isPrim() && s.tag != Ty.ERROR && t.tag != Ty.ERROR) {
      return false;
    }
    return isSubtype(s, t);
  }

  boolean isLooseConvertible(Ty s, Ty t) {
    if (s.tag == Ty.ERROR || t.tag == Ty.ERROR) {
      return true;
    }
    if (s.tag == Ty.VOID || t.tag == Ty.VOID) {
      return false;
    }
    if (s.isPrim() && !t.isPrim()) {
      return isSubtype(box(s), t);
    }
    if (!s.isPrim() && t.isPrim()) {
      if (TypeOps.resolve(s).tag == Ty.IVAR) {
        return sameType(s, box(t));
      }
      Ty u = unboxed(s);
      return u != null && primWiden(u.tag, t.tag);
    }
    return isSubtype(s, t);
  }

  boolean isCastable(Ty s, Ty t) {
    if (s.tag == Ty.ERROR || t.tag == Ty.ERROR) {
      return true;
    }
    if (s.isPrim() && t.isPrim()) {
      return (s.tag == Ty.BOOLEAN) == (t.tag == Ty.BOOLEAN);
    }
    if (s.tag == Ty.VOID || t.tag == Ty.VOID) {
      return false;
    }
    if (s.isPrim()) {
      return isSubtype(box(s), t);
    }
    if (t.isPrim()) {
      Ty u = unboxed(s);
      if (u != null) {
        return primWiden(u.tag, t.tag);
      }
      return isSubtype(box(t), erasure(s));
    }
    if (s.tag == Ty.NULL) {
      return true;
    }
    Ty es = erasure(s);
    Ty et = erasure(t);
    if (isSubtype(es, et) || isSubtype(et, es)) {
      return true;
    }
    if (es.tag == Ty.CLASS && et.tag == Ty.CLASS) {
      boolean si = es.sym.isInterface();
      boolean ti = et.sym.isInterface();
      if (si && ti) {
        return true;
      }
      if (si) {
        return !et.sym.isFinal();
      }
      if (ti) {
        return !es.sym.isFinal();
      }
      return false;
    }
    if (es.tag == Ty.ARRAY && et.tag == Ty.ARRAY) {
      if (es.elem.isPrim() || et.elem.isPrim()) {
        return es.elem.tag == et.elem.tag;
      }
      return isCastable(es.elem, et.elem);
    }
    if (es.tag == Ty.ARRAY || et.tag == Ty.ARRAY) {
      return false;
    }
    return true;
  }

  Ty erasure(Ty t) {
    return TypeOps.erasure(t, st.object);
  }

  /* ---------- member views ---------- */

  /** The class or array types whose members a value of type t exposes. */
  List<Ty> memberSites(Ty t) {
    List<Ty> out = new ArrayList<Ty>();
    collectSites(t, out, 0);
    if (out.isEmpty()) {
      out.add(st.object);
    }
    return out;
  }

  private void collectSites(Ty t, List<Ty> out, int d) {
    if (d > 20) {
      return;
    }
    switch (t.tag) {
      case Ty.CLASS:
      case Ty.ARRAY:
        out.add(t);
        break;
      case Ty.TVAR:
        for (Ty b : t.tv.bounds()) {
          collectSites(b, out, d + 1);
        }
        break;
      case Ty.INTER:
        for (Ty b : t.args) {
          collectSites(b, out, d + 1);
        }
        break;
      case Ty.WILD:
        if (t.wildKind == 1) {
          collectSites(t.bound, out, d + 1);
        }
        break;
      case Ty.IVAR:
        if (t.iv.inst != null) {
          collectSites(t.iv.inst, out, d + 1);
        } else {
          for (Ty b : t.iv.upper) {
            collectSites(b, out, d + 1);
          }
        }
        break;
      default:
        break;
    }
  }

  Ty upper(Ty t) {
    if (t.tag == Ty.WILD) {
      return t.wildKind == 1 ? t.bound : st.object;
    }
    return t;
  }

  Ty lowerParam(Ty t) {
    if (t.tag == Ty.WILD) {
      return t.wildKind == 0 ? st.object : t.bound;
    }
    return t;
  }

  MType memberType(MSym m, Ty site) {
    MType r = new MType();
    r.tparams = m.tparams;
    r.params = m.params;
    r.ret = m.ret;
    r.thrown = m.thrown;
    CSym owner = m.cls();
    if (m.isStatic() || site == null) {
      return r;
    }
    Ty sup = TypeOps.asSuper(site, owner);
    if (sup == null) {
      return r;
    }
    if (sup.args.length == 0 && !owner.typeParams().isEmpty()) {
      r.tparams = new ArrayList<TVSym>();
      r.params = new ArrayList<Ty>();
      for (Ty p : m.params) {
        r.params.add(erasure(p));
      }
      r.ret = m.isCtor() ? m.ret : erasure(m.ret);
      r.thrown = new ArrayList<Ty>();
      for (Ty p : m.thrown) {
        r.thrown.add(erasure(p));
      }
      return r;
    }
    if (sup.args.length == 0) {
      return r;
    }
    List<Ty> args = java.util.Arrays.asList(sup.args);
    r.cfrom = owner.tparams;
    r.cto = args;
    r.params = new ArrayList<Ty>();
    for (Ty p : m.params) {
      r.params.add(lowerParam(TypeOps.subst(p, owner.tparams, args)));
    }
    Ty ret = TypeOps.subst(m.ret, owner.tparams, args);
    r.ret = ret.tag == Ty.WILD ? projectWild(ret, m.ret, owner) : ret;
    r.thrown = TypeOps.subst(m.thrown, owner.tparams, args);
    return r;
  }

  /** Upper bound of a wildcard that replaced the class type parameter named by the declared type. */
  Ty projectWild(Ty w, Ty declared, CSym owner) {
    Ty db = null;
    if (declared.tag == Ty.TVAR) {
      for (TVSym tp : owner.tparams) {
        if (tp == declared.tv && !tp.bounds().isEmpty()) {
          Ty b0 = tp.bounds().get(0);
          db = TypeOps.hasTvar(b0) ? TypeOps.erasure(b0, st.object) : b0;
        }
      }
    }
    if (w.wildKind == 1 && !w.bound.isObject()) {
      if (db != null && !db.isObject() && isSubtype(db, w.bound)) {
        return db;
      }
      return w.bound;
    }
    return db != null ? db : st.object;
  }

  Ty fieldType(FSym f, Ty site) {
    if (f.isStatic() || site == null) {
      return f.type;
    }
    Ty sup = TypeOps.asSuper(site, f.cls());
    if (sup == null) {
      return f.type;
    }
    if (sup.args.length == 0) {
      return f.cls().typeParams().isEmpty() ? f.type : erasure(f.type);
    }
    Ty r = TypeOps.subst(f.type, f.cls().tparams, java.util.Arrays.asList(sup.args));
    return r.tag == Ty.WILD ? projectWild(r, f.type, f.cls()) : r;
  }

  /* ---------- lub / glb ---------- */

  private void closure(Ty t, Set<CSym> out, int d) {
    if (d > 30) {
      return;
    }
    switch (t.tag) {
      case Ty.CLASS: {
        if (!out.add(t.sym)) {
          return;
        }
        Ty sc = t.sym.superclass();
        if (sc != null) {
          closure(sc, out, d + 1);
        }
        for (Ty i : t.sym.interfaces()) {
          closure(i, out, d + 1);
        }
        out.add(st.objectSym);
        break;
      }
      case Ty.TVAR:
        for (Ty b : t.tv.bounds()) {
          closure(b, out, d + 1);
        }
        break;
      case Ty.INTER:
        for (Ty b : t.args) {
          closure(b, out, d + 1);
        }
        break;
      case Ty.WILD:
        if (t.wildKind == 1) {
          closure(t.bound, out, d + 1);
        } else {
          out.add(st.objectSym);
        }
        break;
      case Ty.IVAR:
        if (t.iv.inst != null) {
          closure(t.iv.inst, out, d + 1);
        }
        break;
      default:
        out.add(st.objectSym);
        break;
    }
  }

  Ty lub(List<Ty> in) {
    return lub(in, 0);
  }

  private Ty lub(List<Ty> in, int d) {
    List<Ty> ts = new ArrayList<Ty>();
    for (Ty t : in) {
      if (t.tag == Ty.NULL) {
        continue;
      }
      boolean dup = false;
      for (Ty o : ts) {
        if (TypeOps.equal(o, t)) {
          dup = true;
          break;
        }
      }
      if (!dup) {
        ts.add(t);
      }
    }
    if (ts.isEmpty()) {
      return Ty.tNull;
    }
    if (ts.size() == 1) {
      return ts.get(0);
    }
    for (Ty t : ts) {
      boolean top = true;
      for (Ty o : ts) {
        if (!isSubtype(o, t)) {
          top = false;
          break;
        }
      }
      if (top && !TypeOps.hasIvar(t)) {
        return t;
      }
    }
    boolean allArrays = true;
    for (Ty t : ts) {
      if (t.tag != Ty.ARRAY) {
        allArrays = false;
      }
    }
    if (allArrays) {
      List<Ty> es = new ArrayList<Ty>();
      boolean prim = false;
      for (Ty t : ts) {
        es.add(t.elem);
        prim |= t.elem.isPrim();
      }
      return prim ? st.object : Ty.array(lub(es, d + 1));
    }
    Set<CSym> common = null;
    for (Ty t : ts) {
      Set<CSym> c = new LinkedHashSet<CSym>();
      closure(t, c, 0);
      if (common == null) {
        common = c;
      } else {
        common.retainAll(c);
      }
    }
    List<CSym> cands = new ArrayList<CSym>(common);
    List<Ty> result = new ArrayList<Ty>();
    for (CSym c : cands) {
      boolean minimal = true;
      for (CSym o : cands) {
        if (o != c && isSubClass(o, c)) {
          minimal = false;
          break;
        }
      }
      if (!minimal) {
        continue;
      }
      if (c == st.objectSym && !cands.isEmpty() && cands.size() > 1) {
        continue;
      }
      result.add(parameterizedLub(c, ts, d));
    }
    if (result.isEmpty()) {
      return st.object;
    }
    if (result.size() > 1) {
      List<Ty> ordered = new ArrayList<Ty>();
      for (Ty r : result) {
        if (!r.sym.isInterface()) {
          ordered.add(r);
        }
      }
      for (Ty r : result) {
        if (r.sym.isInterface()) {
          ordered.add(r);
        }
      }
      return Ty.inter(ordered);
    }
    return result.get(0);
  }

  private boolean isSubClass(CSym sub, CSym sup) {
    Set<CSym> c = new LinkedHashSet<CSym>();
    closure(Ty.cls(sub), c, 0);
    return c.contains(sup);
  }

  private Ty parameterizedLub(CSym c, List<Ty> ts, int d) {
    if (c.typeParams().isEmpty()) {
      return Ty.cls(c);
    }
    List<Ty> sups = new ArrayList<Ty>();
    for (Ty t : ts) {
      Ty s = TypeOps.asSuper(t, c);
      if (s == null || s.args.length == 0) {
        return Ty.cls(c);
      }
      sups.add(s);
    }
    Ty[] args = new Ty[c.tparams.size()];
    for (int i = 0; i < args.length; i++) {
      Ty first = sups.get(0).args[i];
      boolean allSame = true;
      for (Ty s : sups) {
        if (!TypeOps.equal(s.args[i], first)) {
          allSame = false;
        }
      }
      if (allSame) {
        args[i] = first;
      } else if (d < 2) {
        List<Ty> as = new ArrayList<Ty>();
        for (Ty s : sups) {
          as.add(upper(s.args[i]));
        }
        Ty l = lub(as, d + 1);
        args[i] = l.isObject() ? Ty.wild(0, null) : Ty.wild(1, l);
      } else {
        args[i] = Ty.wild(0, null);
      }
    }
    return Ty.cls(c, args);
  }

  Ty glb(Ty a, Ty b) {
    if (isSubtype(a, b)) {
      return a;
    }
    if (isSubtype(b, a)) {
      return b;
    }
    List<Ty> l = new ArrayList<Ty>();
    if (a.tag == Ty.CLASS && !a.sym.isInterface()) {
      l.add(a);
      l.add(b);
    } else {
      l.add(b.tag == Ty.CLASS && !b.sym.isInterface() ? b : a);
      l.add(b.tag == Ty.CLASS && !b.sym.isInterface() ? a : b);
    }
    return Ty.inter(l);
  }

  /* ---------- functional interfaces ---------- */

  MSym sam(CSym c) {
    if (samCache.containsKey(c)) {
      return samCache.get(c);
    }
    MSym r = null;
    if (c.isInterface()) {
      Map<String, MSym> abstracts = new LinkedHashMap<String, MSym>();
      Set<String> defaults = new LinkedHashSet<String>();
      Set<CSym> seen = new LinkedHashSet<CSym>();
      Ty site = c.type();
      collectSam(c, abstracts, defaults, seen, site);
      List<MSym> left = new ArrayList<MSym>();
      for (Map.Entry<String, MSym> e : abstracts.entrySet()) {
        String k = e.getKey();
        if (defaults.contains(k)) {
          continue;
        }
        if (k.equals("equals(Ljava/lang/Object;)") || k.equals("hashCode()") || k.equals("toString()")) {
          continue;
        }
        left.add(e.getValue());
      }
      if (left.size() == 1) {
        r = left.get(0);
      } else if (left.size() > 1) {
        boolean sameName = true;
        for (MSym m : left) {
          if (!m.name.equals(left.get(0).name) || m.params.size() != left.get(0).params.size()) {
            sameName = false;
          }
        }
        if (sameName) {
          r = left.get(0);
        }
      }
    }
    samCache.put(c, r);
    return r;
  }

  private String key(MSym m, Ty site) {
    StringBuilder sb = new StringBuilder(m.name).append('(');
    List<Ty> params = m.params;
    if (site != null && !m.cls().typeParams().isEmpty()) {
      Ty sup = TypeOps.asSuper(site, m.cls());
      if (sup != null && sup.args.length > 0) {
        params = memberType(m, sup).params;
      }
    }
    for (Ty p : params) {
      sb.append(TypeOps.erasure(p, Ty.tError).desc());
    }
    return sb.append(')').toString();
  }

  private void collectSam(CSym c, Map<String, MSym> abstracts, Set<String> defaults, Set<CSym> seen, Ty site) {
    if (!seen.add(c)) {
      return;
    }
    c.members();
    for (MSym m : c.allMethods) {
      if (m.isStatic() || m.isPrivate()) {
        continue;
      }
      String k = key(m, site);
      if ((m.flags & Sym.ABSTRACT) != 0 && (m.flags & Sym.DEFAULT) == 0) {
        if (!abstracts.containsKey(k)) {
          abstracts.put(k, m);
        }
      } else {
        defaults.add(k);
      }
    }
    for (Ty i : c.interfaces()) {
      collectSam(i.sym, abstracts, defaults, seen, site);
    }
  }

  Ty nonWildcard(Ty fi) {
    boolean any = false;
    for (Ty a : fi.args) {
      if (a.tag == Ty.WILD) {
        any = true;
      }
    }
    if (!any) {
      return fi;
    }
    Ty[] args = new Ty[fi.args.length];
    for (int i = 0; i < args.length; i++) {
      Ty a = fi.args[i];
      if (a.tag == Ty.WILD) {
        if (a.wildKind == 0) {
          TVSym tp = fi.sym.tparams.get(i);
          Ty b = tp.bounds().isEmpty() ? st.object : tp.bounds().get(0);
          args[i] = TypeOps.hasTvar(b) ? st.object : b;
        } else {
          args[i] = a.bound;
        }
      } else {
        args[i] = a;
      }
    }
    return Ty.cls(fi.sym, args);
  }

  /** The function type of a functional interface type, or null. */
  FnType fnType(Ty fi) {
    if (fi.tag == Ty.INTER) {
      for (Ty b : fi.args) {
        FnType f = fnType(b);
        if (f != null) {
          return f;
        }
      }
      return null;
    }
    if (fi.tag == Ty.IVAR && fi.iv.inst != null) {
      return fnType(fi.iv.inst);
    }
    if (fi.tag != Ty.CLASS || !fi.sym.isInterface()) {
      return null;
    }
    MSym m = sam(fi.sym);
    if (m == null) {
      return null;
    }
    Ty p = nonWildcard(fi);
    MType mt = memberType(m, p);
    FnType r = new FnType();
    r.method = m;
    r.params = mt.params;
    r.ret = mt.ret;
    r.thrown = mt.thrown;
    return r;
  }
}
