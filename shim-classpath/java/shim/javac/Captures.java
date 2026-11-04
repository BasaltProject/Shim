/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Free variable analysis for local classes, anonymous classes and lambdas. */
final class Captures {
  private final Attr a;
  private final Map<CSym, List<VSym>> classCaptures = new IdentityHashMap<CSym, List<VSym>>();
  private final Set<CSym> inProgress = new HashSet<CSym>();

  Captures(Attr a) {
    this.a = a;
  }

  /** Result of scanning a tree: variables referenced but declared outside, and whether this is needed. */
  static final class Free {
    final Set<VSym> declared = new HashSet<VSym>();
    final Set<VSym> referenced = new LinkedHashSet<VSym>();
    boolean usesThis;
    final Set<CSym> localClasses = new LinkedHashSet<CSym>();
    final Set<CSym> declaredClasses = new HashSet<CSym>();
  }

  Free scan(Tree t) {
    final Free f = new Free();
    new TreeScanner<Void, Void>() {
      public Void visitVariable(VariableTree v, Void p) {
        Object s = a.syms.get(v);
        if (s instanceof VSym) {
          f.declared.add((VSym) s);
        }
        return super.visitVariable(v, p);
      }

      public Void visitIdentifier(IdentifierTree id, Void p) {
        Object s = a.syms.get(id);
        if (s instanceof VSym) {
          f.referenced.add((VSym) s);
        } else if (s instanceof FSym) {
          Object ri = a.info.get(id);
          if (!((FSym) s).isStatic() && ri instanceof Attr.FieldRef && ((Attr.FieldRef) ri).implicitOwner != null) {
            f.usesThis = true;
          }
        } else if (s instanceof CSym) {
          String n = id.getName().toString();
          if (n.equals("this") || n.equals("super")) {
            f.usesThis = true;
          }
        }
        return null;
      }

      public Void visitMemberSelect(MemberSelectTree ms, Void p) {
        Object s = a.syms.get(ms);
        String n = ms.getIdentifier().toString();
        if ((n.equals("this") || n.equals("super")) && s instanceof CSym) {
          f.usesThis = true;
        }
        return super.visitMemberSelect(ms, p);
      }

      public Void visitMethodInvocation(MethodInvocationTree m, Void p) {
        Object ci = a.info.get(m);
        if (ci instanceof AttrCall.CallInfo) {
          AttrCall.CallInfo c = (AttrCall.CallInfo) ci;
          if (c.ctorCall) {
            f.usesThis = true;
          } else if (c.m != null && !c.m.isStatic() && (c.implicitOwner != null || c.superCall)) {
            f.usesThis = true;
          }
        }
        return super.visitMethodInvocation(m, p);
      }

      public Void visitNewClass(NewClassTree nc, Void p) {
        Object ci = a.info.get(nc);
        if (ci instanceof AttrCall.CallInfo) {
          AttrCall.CallInfo c = (AttrCall.CallInfo) ci;
          CSym target = c.anon != null ? c.anon : c.m != null ? c.m.cls() : null;
          if (target != null) {
            if (target.local || target.anon) {
              f.localClasses.add(target);
              if (needsOuterThis(target)) {
                f.usesThis = true;
              }
            } else if (target.isInner() && nc.getEnclosingExpression() == null) {
              f.usesThis = true;
            }
          }
        }
        return super.visitNewClass(nc, p);
      }

      public Void visitMemberReference(MemberReferenceTree mr, Void p) {
        Object ri = a.info.get(mr);
        if (ri instanceof AttrCall.RefInfo) {
          AttrCall.RefInfo r = (AttrCall.RefInfo) ri;
          if (r.kind == AttrCall.REF_SUPER) {
            f.usesThis = true;
          }
          if (r.call != null && r.call.ctorCall && r.call.m != null) {
            CSym target = r.call.m.cls();
            if (target.local) {
              f.localClasses.add(target);
              if (needsOuterThis(target)) {
                f.usesThis = true;
              }
            } else if (target.isInner()) {
              f.usesThis = true;
            }
          }
        }
        return super.visitMemberReference(mr, p);
      }

      public Void visitClass(ClassTree c, Void p) {
        CSym k = a.enter.classOf.get(c);
        if (k != null) {
          f.declaredClasses.add(k);
        }
        return super.visitClass(c, p);
      }
    }.scan(t, null);
    return f;
  }

  static boolean needsOuterThis(CSym c) {
    if (c.anon || c.local) {
      return (c.flags & Sym.STATIC) == 0 && c.outerClass != null;
    }
    return c.isInner();
  }

  /** Local variables a local or anonymous class captures, in a stable order. */
  List<VSym> of(CSym c) {
    List<VSym> r = classCaptures.get(c);
    if (r != null) {
      return r;
    }
    if (!c.local && !c.anon) {
      r = new ArrayList<VSym>();
      classCaptures.put(c, r);
      return r;
    }
    if (!inProgress.add(c)) {
      return new ArrayList<VSym>();
    }
    Free f = scan(c.tree);
    Set<VSym> out = new LinkedHashSet<VSym>();
    for (VSym v : f.referenced) {
      if (!f.declared.contains(v)) {
        out.add(v);
      }
    }
    for (CSym l : f.localClasses) {
      if (l == c || isNested(l, c)) {
        continue;
      }
      for (VSym v : of(l)) {
        if (!f.declared.contains(v)) {
          out.add(v);
        }
      }
    }
    Ty sc = c.superclass();
    if (sc != null && sc.tag == Ty.CLASS && (sc.sym.local || sc.sym.anon)) {
      for (VSym v : of(sc.sym)) {
        if (!f.declared.contains(v)) {
          out.add(v);
        }
      }
    }
    inProgress.remove(c);
    r = new ArrayList<VSym>(out);
    classCaptures.put(c, r);
    return r;
  }

  private boolean isNested(CSym inner, CSym outer) {
    for (CSym c = inner.outerClass; c != null; c = c.outerClass) {
      if (c == outer) {
        return true;
      }
    }
    return false;
  }
}
