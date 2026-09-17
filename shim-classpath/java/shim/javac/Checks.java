/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Definite assignment (JLS 16) and checked exception analysis (JLS 11.2) over attributed method bodies. */
final class Checks {
  private final Attr a;
  private static final Set<VSym> ALL = new HashSet<VSym>();
  private final Set<VSym> tracked = new HashSet<VSym>();

  Checks(Attr a) {
    this.a = a;
  }

  /* ---------- entry points ---------- */

  void method(MethodTree mt, MSym ms) {
    if (mt.getBody() == null) {
      return;
    }
    Set<VSym> in = new HashSet<VSym>();
    stmt(mt.getBody(), in, new ArrayList<Jump>());
    List<Ty> allowed = new ArrayList<Ty>(ms.thrown);
    excScan(mt.getBody(), allowed);
  }

  /* ---------- definite assignment ---------- */

  private static final class Jump {
    final Tree target;
    final String label;
    final boolean loop;
    Set<VSym> breaks = ALL;
    Set<VSym> conts = ALL;

    Jump(Tree t, String label, boolean loop) {
      target = t;
      this.label = label;
      this.loop = loop;
    }
  }

  private static Set<VSym> meet(Set<VSym> x, Set<VSym> y) {
    if (x == ALL) {
      return y;
    }
    if (y == ALL) {
      return x;
    }
    Set<VSym> r = new HashSet<VSym>(x);
    r.retainAll(y);
    return r;
  }

  private static Set<VSym> copy(Set<VSym> s) {
    return s == ALL ? ALL : new HashSet<VSym>(s);
  }

  private static Set<VSym> add(Set<VSym> s, VSym v) {
    if (s == ALL) {
      return s;
    }
    s.add(v);
    return s;
  }

  private void use(VSym v, Set<VSym> in, Tree at) {
    if (tracked.contains(v) && in != ALL && !in.contains(v)) {
      a.err(at, "compiler.err.var.might.not.have.been.initialized", "variable " + v.name
          + " might not have been initialized");
      in.add(v);
    }
  }

  private Set<VSym> stmts(List<? extends StatementTree> l, Set<VSym> in, List<Jump> js) {
    Set<VSym> cur = in;
    for (StatementTree s : l) {
      cur = stmt(s, cur, js);
    }
    return cur;
  }

  private Set<VSym> stmt(StatementTree s, Set<VSym> in, List<Jump> js) {
    if (s == null) {
      return in;
    }
    switch (s.getKind()) {
      case BLOCK:
        return stmts(((BlockTree) s).getStatements(), in, js);
      case VARIABLE: {
        VariableTree v = (VariableTree) s;
        Object sym = a.syms.get(v);
        if (v.getInitializer() != null) {
          Set<VSym> o = expr(v.getInitializer(), in, js);
          return o;
        }
        if (sym instanceof VSym) {
          tracked.add((VSym) sym);
          if (in != ALL) {
            in.remove(sym);
          }
        }
        return in;
      }
      case EXPRESSION_STATEMENT:
        return expr(((ExpressionStatementTree) s).getExpression(), in, js);
      case IF: {
        IfTree i = (IfTree) s;
        Set<VSym>[] c = cond(i.getCondition(), in, js);
        Set<VSym> t = stmt(i.getThenStatement(), c[0], js);
        Set<VSym> f = i.getElseStatement() == null ? c[1] : stmt(i.getElseStatement(), c[1], js);
        return meet(t, f);
      }
      case WHILE_LOOP: {
        WhileLoopTree w = (WhileLoopTree) s;
        Jump j = new Jump(s, null, true);
        js.add(j);
        Set<VSym>[] c = cond(w.getCondition(), in, js);
        stmt(w.getStatement(), c[0], js);
        js.remove(js.size() - 1);
        return meet(c[1], j.breaks);
      }
      case DO_WHILE_LOOP: {
        DoWhileLoopTree d = (DoWhileLoopTree) s;
        Jump j = new Jump(s, null, true);
        js.add(j);
        Set<VSym> o = stmt(d.getStatement(), in, js);
        js.remove(js.size() - 1);
        Set<VSym>[] c = cond(d.getCondition(), meet(o, j.conts), js);
        return meet(c[1], j.breaks);
      }
      case FOR_LOOP: {
        ForLoopTree f = (ForLoopTree) s;
        Set<VSym> cur = in;
        for (StatementTree i : f.getInitializer()) {
          cur = stmt(i, cur, js);
        }
        Jump j = new Jump(s, null, true);
        js.add(j);
        Set<VSym>[] c = f.getCondition() == null ? new Set[] {cur, ALL} : cond(f.getCondition(), cur, js);
        Set<VSym> b = stmt(f.getStatement(), c[0], js);
        js.remove(js.size() - 1);
        Set<VSym> u = meet(b, j.conts);
        for (ExpressionStatementTree e : f.getUpdate()) {
          u = stmt(e, u, js);
        }
        return meet(c[1], j.breaks);
      }
      case ENHANCED_FOR_LOOP: {
        EnhancedForLoopTree f = (EnhancedForLoopTree) s;
        Set<VSym> o = expr(f.getExpression(), in, js);
        Jump j = new Jump(s, null, true);
        js.add(j);
        stmt(f.getStatement(), copy(o), js);
        js.remove(js.size() - 1);
        return meet(o, j.breaks);
      }
      case LABELED_STATEMENT: {
        LabeledStatementTree l = (LabeledStatementTree) s;
        Jump j = new Jump(s, l.getLabel().toString(), false);
        js.add(j);
        Set<VSym> o = stmt(l.getStatement(), in, js);
        js.remove(js.size() - 1);
        return meet(o, j.breaks);
      }
      case RETURN: {
        ExpressionTree e = ((ReturnTree) s).getExpression();
        if (e != null) {
          expr(e, in, js);
        }
        return ALL;
      }
      case THROW:
        expr(((ThrowTree) s).getExpression(), in, js);
        return ALL;
      case YIELD:
        expr(((YieldTree) s).getValue(), in, js);
        return ALL;
      case BREAK: {
        BreakTree b = (BreakTree) s;
        for (int i = js.size() - 1; i >= 0; i--) {
          Jump j = js.get(i);
          if (b.getLabel() == null ? (j.loop || j.target instanceof SwitchTree) : b.getLabel().toString()
              .equals(j.label)) {
            j.breaks = meet(j.breaks, in);
            break;
          }
        }
        return ALL;
      }
      case CONTINUE: {
        ContinueTree c = (ContinueTree) s;
        for (int i = js.size() - 1; i >= 0; i--) {
          Jump j = js.get(i);
          if (c.getLabel() == null ? j.loop : j.target instanceof LabeledStatementTree
              && c.getLabel().toString().equals(j.label)) {
            j.conts = meet(j.conts, in);
            break;
          }
        }
        return ALL;
      }
      case SWITCH:
        return switchStmt((SwitchTree) s, in, js);
      case TRY: {
        TryTree t = (TryTree) s;
        Set<VSym> cur = copy(in);
        for (Tree r : t.getResources()) {
          if (r instanceof VariableTree) {
            cur = stmt((VariableTree) r, cur, js);
          } else {
            cur = expr((ExpressionTree) r, cur, js);
          }
        }
        Set<VSym> out = stmt(t.getBlock(), cur, js);
        for (CatchTree c : t.getCatches()) {
          out = meet(out, stmt(c.getBlock(), copy(in), js));
        }
        if (t.getFinallyBlock() != null) {
          Set<VSym> f = stmt(t.getFinallyBlock(), copy(in), js);
          if (f == ALL) {
            return ALL;
          }
          if (out != ALL) {
            Set<VSym> r = new HashSet<VSym>(out);
            r.addAll(f);
            return r;
          }
          return ALL;
        }
        return out;
      }
      case SYNCHRONIZED: {
        SynchronizedTree y = (SynchronizedTree) s;
        Set<VSym> o = expr(y.getExpression(), in, js);
        return stmt(y.getBlock(), o, js);
      }
      case ASSERT: {
        AssertTree as = (AssertTree) s;
        Set<VSym>[] c = cond(as.getCondition(), copy(in), js);
        if (as.getDetail() != null) {
          expr(as.getDetail(), c[1], js);
        }
        return in;
      }
      default:
        return in;
    }
  }

  private Set<VSym> switchStmt(SwitchTree sw, Set<VSym> in, List<Jump> js) {
    Set<VSym> sel = expr(sw.getExpression(), in, js);
    Jump j = new Jump(sw, null, false);
    js.add(j);
    boolean hasDefault = false;
    Set<VSym> prev = ALL;
    boolean arrow = !sw.getCases().isEmpty() && sw.getCases().get(0).getCaseKind() == CaseTree.CaseKind.RULE;
    Set<VSym> exit = ALL;
    for (CaseTree c : sw.getCases()) {
      for (CaseLabelTree l : c.getLabels()) {
        if (l instanceof DefaultCaseLabelTree) {
          hasDefault = true;
        }
      }
      Set<VSym> ci = copy(sel);
      if (c.getGuard() != null) {
        ci = cond(c.getGuard(), ci, js)[0];
      }
      if (arrow) {
        Tree body = c.getBody();
        Set<VSym> o = body instanceof ExpressionTree ? expr((ExpressionTree) body, ci, js)
            : stmt((StatementTree) body, ci, js);
        exit = meet(exit, o);
      } else {
        if (prev != ALL) {
          ci = meet(ci, prev);
        }
        prev = c.getStatements() == null ? ci : stmts(c.getStatements(), ci, js);
      }
    }
    js.remove(js.size() - 1);
    Object sd = a.syms.get(sw);
    boolean exhaustive = hasDefault || sd instanceof AttrStat.SwitchData && ((AttrStat.SwitchData) sd).exhaustive;
    Set<VSym> r = meet(arrow ? exit : prev, j.breaks);
    if (!exhaustive) {
      r = meet(r, sel);
    }
    return r;
  }

  @SuppressWarnings("unchecked")
  private Set<VSym>[] cond(ExpressionTree e, Set<VSym> in, List<Jump> js) {
    e = Attr.unparen(e);
    Object c = a.consts.get(e);
    if (c instanceof Boolean) {
      Set<VSym> o = in;
      boolean v = ((Boolean) c).booleanValue();
      return new Set[] {v ? o : ALL, v ? ALL : copy(o)};
    }
    switch (e.getKind()) {
      case LOGICAL_COMPLEMENT: {
        Set<VSym>[] r = cond(((UnaryTree) e).getExpression(), in, js);
        return new Set[] {r[1], r[0]};
      }
      case CONDITIONAL_AND: {
        BinaryTree b = (BinaryTree) e;
        Set<VSym>[] l = cond(b.getLeftOperand(), in, js);
        Set<VSym>[] r = cond(b.getRightOperand(), l[0], js);
        return new Set[] {r[0], meet(l[1], r[1])};
      }
      case CONDITIONAL_OR: {
        BinaryTree b = (BinaryTree) e;
        Set<VSym>[] l = cond(b.getLeftOperand(), in, js);
        Set<VSym>[] r = cond(b.getRightOperand(), l[1], js);
        return new Set[] {meet(l[0], r[0]), r[1]};
      }
      default: {
        Set<VSym> o = expr(e, in, js);
        return new Set[] {o, copy(o)};
      }
    }
  }

  private Set<VSym> expr(ExpressionTree e, Set<VSym> in, List<Jump> js) {
    if (e == null) {
      return in;
    }
    switch (e.getKind()) {
      case IDENTIFIER: {
        Object s = a.syms.get(e);
        if (s instanceof VSym) {
          use((VSym) s, in, e);
        }
        return in;
      }
      case ASSIGNMENT: {
        AssignmentTree as = (AssignmentTree) e;
        ExpressionTree v = Attr.unparen(as.getVariable());
        Set<VSym> o = in;
        if (v.getKind() == Tree.Kind.IDENTIFIER) {
          o = expr(as.getExpression(), in, js);
          Object s = a.syms.get(v);
          if (s instanceof VSym) {
            add(o, (VSym) s);
          }
        } else {
          o = expr(v, in, js);
          o = expr(as.getExpression(), o, js);
        }
        return o;
      }
      case LOGICAL_COMPLEMENT:
      case CONDITIONAL_AND:
      case CONDITIONAL_OR: {
        Set<VSym>[] c = cond(e, in, js);
        return meet(c[0], c[1]);
      }
      case CONDITIONAL_EXPRESSION: {
        ConditionalExpressionTree c = (ConditionalExpressionTree) e;
        Set<VSym>[] k = cond(c.getCondition(), in, js);
        Set<VSym> t = expr(c.getTrueExpression(), k[0], js);
        Set<VSym> f = expr(c.getFalseExpression(), k[1], js);
        return meet(t, f);
      }
      case LAMBDA_EXPRESSION: {
        LambdaExpressionTree l = (LambdaExpressionTree) e;
        Set<VSym> body = copy(in);
        if (l.getBody() instanceof ExpressionTree) {
          expr((ExpressionTree) l.getBody(), body, new ArrayList<Jump>());
        } else {
          stmt((StatementTree) l.getBody(), body, new ArrayList<Jump>());
        }
        return in;
      }
      case SWITCH_EXPRESSION: {
        SwitchExpressionTree sw = (SwitchExpressionTree) e;
        Set<VSym> sel = expr(sw.getExpression(), in, js);
        Set<VSym> out = ALL;
        for (CaseTree c : sw.getCases()) {
          Set<VSym> ci = copy(sel);
          if (c.getGuard() != null) {
            ci = cond(c.getGuard(), ci, js)[0];
          }
          Tree body = c.getBody();
          if (body instanceof ExpressionTree) {
            out = meet(out, expr((ExpressionTree) body, ci, js));
          } else if (body instanceof StatementTree) {
            stmt((StatementTree) body, ci, js);
            out = meet(out, sel);
          } else if (c.getStatements() != null) {
            stmts(c.getStatements(), ci, js);
            out = meet(out, sel);
          }
        }
        return out == ALL ? sel : out;
      }
      case NEW_CLASS: {
        NewClassTree nc = (NewClassTree) e;
        Set<VSym> o = nc.getEnclosingExpression() == null ? in : expr(nc.getEnclosingExpression(), in, js);
        for (ExpressionTree arg : nc.getArguments()) {
          o = expr(arg, o, js);
        }
        if (nc.getClassBody() != null) {
          new com.sun.source.util.TreeScanner<Void, Set<VSym>>() {
            public Void visitIdentifier(IdentifierTree id, Set<VSym> s) {
              Object sym = a.syms.get(id);
              if (sym instanceof VSym) {
                use((VSym) sym, s, id);
              }
              return null;
            }
          }.scan(nc.getClassBody(), o);
        }
        return o;
      }
      default:
        break;
    }
    Set<VSym> cur = in;
    for (Tree child : children(e)) {
      cur = expr((ExpressionTree) child, cur, js);
    }
    return cur;
  }

  private List<Tree> children(ExpressionTree e) {
    List<Tree> out = new ArrayList<Tree>();
    switch (e.getKind()) {
      case PARENTHESIZED:
        out.add(((ParenthesizedTree) e).getExpression());
        break;
      case METHOD_INVOCATION: {
        MethodInvocationTree m = (MethodInvocationTree) e;
        if (m.getMethodSelect() instanceof MemberSelectTree) {
          out.add(((MemberSelectTree) m.getMethodSelect()).getExpression());
        }
        out.addAll(m.getArguments());
        break;
      }
      case MEMBER_SELECT:
        out.add(((MemberSelectTree) e).getExpression());
        break;
      case ARRAY_ACCESS:
        out.add(((ArrayAccessTree) e).getExpression());
        out.add(((ArrayAccessTree) e).getIndex());
        break;
      case NEW_ARRAY: {
        NewArrayTree na = (NewArrayTree) e;
        out.addAll(na.getDimensions());
        if (na.getInitializers() != null) {
          out.addAll(na.getInitializers());
        }
        break;
      }
      case TYPE_CAST:
        out.add(((TypeCastTree) e).getExpression());
        break;
      case INSTANCE_OF:
        out.add(((InstanceOfTree) e).getExpression());
        break;
      case MEMBER_REFERENCE:
        out.add(((MemberReferenceTree) e).getQualifierExpression());
        break;
      default:
        if (e instanceof BinaryTree) {
          out.add(((BinaryTree) e).getLeftOperand());
          out.add(((BinaryTree) e).getRightOperand());
        } else if (e instanceof UnaryTree) {
          out.add(((UnaryTree) e).getExpression());
        } else if (e instanceof CompoundAssignmentTree) {
          out.add(((CompoundAssignmentTree) e).getVariable());
          out.add(((CompoundAssignmentTree) e).getExpression());
        }
        break;
    }
    List<Tree> r = new ArrayList<Tree>();
    for (Tree t : out) {
      if (t instanceof ExpressionTree && !isTypeTree(t)) {
        r.add(t);
      }
    }
    return r;
  }

  private boolean isTypeTree(Tree t) {
    Tree.Kind k = t.getKind();
    return k == Tree.Kind.PRIMITIVE_TYPE || k == Tree.Kind.ARRAY_TYPE || k == Tree.Kind.PARAMETERIZED_TYPE
        || a.syms.get(t) instanceof CSym && !(t instanceof IdentifierTree
        && ((IdentifierTree) t).getName().contentEquals("this"));
  }

  /* ---------- checked exceptions ---------- */

  private static final class Frame {
    final List<Ty> caught;
    final boolean[] used;
    final Tree at;

    Frame(List<Ty> caught, Tree at) {
      this.caught = caught;
      this.used = new boolean[caught.size()];
      this.at = at;
    }
  }

  private boolean isChecked(Ty t) {
    t = TypeOps.resolve(t);
    if (t.tag != Ty.CLASS) {
      return false;
    }
    CSym rt = a.st.lookup("java/lang/RuntimeException");
    CSym er = a.st.lookup("java/lang/Error");
    CSym th = a.st.lookup("java/lang/Throwable");
    if (TypeOps.asSuper(t, rt) != null || TypeOps.asSuper(t, er) != null) {
      return false;
    }
    return TypeOps.asSuper(t, th) != null;
  }

  private void excScan(Tree root, final List<Ty> allowed) {
    final List<Frame> frames = new ArrayList<Frame>();
    final Set<Tree> reported = new HashSet<Tree>();
    new com.sun.source.util.TreeScanner<Void, Void>() {
      private void thrown(Ty t, Tree at) {
        t = TypeOps.resolve(t);
        if (!isChecked(t)) {
          return;
        }
        for (int i = frames.size() - 1; i >= 0; i--) {
          Frame f = frames.get(i);
          boolean covered = false;
          for (int k = 0; k < f.caught.size(); k++) {
            if (a.tx.isSubtype(t, f.caught.get(k))) {
              f.used[k] = true;
              covered = true;
            } else if (a.tx.isSubtype(f.caught.get(k), t)) {
              f.used[k] = true;
            }
          }
          if (covered) {
            return;
          }
        }
        for (Ty al : allowed) {
          if (a.tx.isSubtype(t, TypeOps.resolve(al))) {
            return;
          }
        }
        if (reported.add(at)) {
          a.err(at, "compiler.err.unreported.exception.need.to.catch.or.throw", "unreported exception " + t
              + "; must be caught or declared to be thrown");
        }
      }

      public Void visitClass(ClassTree t, Void p) {
        return null;
      }

      public Void visitNewClass(NewClassTree nc, Void p) {
        scan(nc.getEnclosingExpression(), p);
        scan(nc.getArguments(), p);
        Object ci = a.info.get(nc);
        if (ci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) ci).m != null) {
          AttrCall.CallInfo c = (AttrCall.CallInfo) ci;
          for (Ty t : a.tx.memberType(c.m, c.classType == null ? c.site : c.classType).thrown) {
            thrown(t, nc);
          }
        }
        return null;
      }

      public Void visitMethodInvocation(MethodInvocationTree m, Void p) {
        super.visitMethodInvocation(m, p);
        Object ci = a.info.get(m);
        if (ci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) ci).m != null) {
          AttrCall.CallInfo c = (AttrCall.CallInfo) ci;
          if (!c.polySig || true) {
            List<Ty> th = a.tx.memberType(c.m, c.site).thrown;
            Tree at = m;
            if (m.getMethodSelect() instanceof N.Base) {
              N.Base sel = (N.Base) m.getMethodSelect();
              N.EmptyStatement mk = new N.EmptyStatement();
              mk.start = sel.end;
              mk.pos = sel.end;
              mk.end = sel.end + 1;
              at = mk;
            }
            for (Ty t : th) {
              thrown(t, at);
            }
          }
        }
        return null;
      }

      public Void visitThrow(ThrowTree t, Void p) {
        scan(t.getExpression(), p);
        ExpressionTree e = Attr.unparen(t.getExpression());
        Object s = a.syms.get(e);
        if (s instanceof VSym && ((VSym) s).tree != null && catchParams.contains(((VSym) s).tree)) {
          return null;
        }
        Ty ty = a.typeOf(t.getExpression());
        if (ty != null) {
          thrown(ty, t);
        }
        return null;
      }

      final Set<Tree> catchParams = new HashSet<Tree>();

      public Void visitLambdaExpression(LambdaExpressionTree l, Void p) {
        Object li = a.info.get(l);
        List<Ty> saved = new ArrayList<Ty>(allowed);
        List<Frame> savedFrames = new ArrayList<Frame>(frames);
        allowed.clear();
        frames.clear();
        if (li instanceof AttrCall.LambdaInfo) {
          Tx.FnType fn = a.tx.fnType(TypeOps.resolve(((AttrCall.LambdaInfo) li).target));
          if (fn != null) {
            allowed.addAll(fn.thrown);
          }
        }
        scan(l.getBody(), p);
        allowed.clear();
        allowed.addAll(saved);
        frames.addAll(savedFrames);
        return null;
      }

      public Void visitTry(TryTree t, Void p) {
        List<Ty> caught = new ArrayList<Ty>();
        for (CatchTree c : t.getCatches()) {
          Tree pt = c.getParameter().getType();
          catchParams.add(c.getParameter());
          if (pt.getKind() == Tree.Kind.UNION_TYPE) {
            for (Tree alt : ((UnionTypeTree) pt).getTypeAlternatives()) {
              Ty at = a.typeOf(alt);
              caught.add(at == null ? Ty.tError : at);
            }
          } else {
            Ty ty = a.typeOf(pt);
            caught.add(ty == null ? Ty.tError : ty);
          }
        }
        Frame f = new Frame(caught, t);
        frames.add(f);
        for (Tree r : t.getResources()) {
          scan(r, p);
          Ty rt = null;
          if (r instanceof VariableTree) {
            Object s = a.syms.get(r);
            rt = s instanceof VSym ? ((VSym) s).type : null;
          } else {
            rt = a.typeOf(r);
          }
          if (rt != null && rt.tag == Ty.CLASS) {
            for (MSym m : a.res.methodsOf(rt, "close")) {
              if (m.params.isEmpty()) {
                for (Ty th : m.thrown) {
                  thrown(th, r);
                }
                break;
              }
            }
          }
        }
        scan(t.getBlock(), p);
        final boolean[] lib = new boolean[1];
        new com.sun.source.util.TreeScanner<Void, Void>() {
          public Void visitClass(ClassTree c, Void q) {
            return null;
          }

          public Void visitMethodInvocation(MethodInvocationTree mi, Void q) {
            Object ci = a.info.get(mi);
            if (ci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) ci).m != null
                && !((AttrCall.CallInfo) ci).m.cls().isSource) {
              lib[0] = true;
            }
            return super.visitMethodInvocation(mi, q);
          }

          public Void visitNewClass(NewClassTree nc, Void q) {
            Object ci = a.info.get(nc);
            if (ci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) ci).m != null
                && !((AttrCall.CallInfo) ci).m.cls().isSource) {
              lib[0] = true;
            }
            return super.visitNewClass(nc, q);
          }
        }.scan(t.getBlock(), null);
        boolean libCalls = lib[0] || !t.getResources().isEmpty();
        frames.remove(frames.size() - 1);
        int k = 0;
        for (CatchTree c : t.getCatches()) {
          Tree pt = c.getParameter().getType();
          int n = pt.getKind() == Tree.Kind.UNION_TYPE ? ((UnionTypeTree) pt).getTypeAlternatives().size() : 1;
          for (int i = 0; i < n; i++, k++) {
            Ty ct = caught.get(k);
            if (!f.used[k] && !libCalls && isChecked(ct) && !isThrowableOrException(ct)) {
              a.err(c, "compiler.err.except.never.thrown.in.try", "exception " + ct
                  + " is never thrown in body of corresponding try statement");
            }
          }
          scan(c.getBlock(), p);
        }
        scan(t.getFinallyBlock(), p);
        return null;
      }

      private boolean isThrowableOrException(Ty t) {
        String n = t.tag == Ty.CLASS ? t.sym.internal : "";
        return n.equals("java/lang/Exception") || n.equals("java/lang/Throwable");
      }
    }.scan(root, null);
  }
}
