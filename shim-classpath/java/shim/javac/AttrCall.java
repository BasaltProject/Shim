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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Attribution of invocations, instance creation, lambdas, method references and type inference. */
final class AttrCall {
  private final Attr a;
  private final Tx tx;
  private final Symtab st;

  static final class Arg {
    ExpressionTree tree;
    Ty type;
    boolean lambda;
    boolean mref;
    boolean poly;

    boolean deferred() {
      return type == null;
    }
  }

  static final class CallInfo {
    MSym m;
    Ty site;
    List<Ty> formals;
    Ty ret;
    boolean varargs;
    Ty varargElem;
    int fixed;
    CSym implicitOwner;
    boolean superCall;
    boolean thisCall;
    boolean ctorCall;
    CSym superQualifier;
    boolean staticQual;
    CSym anon;
    Ty classType;
    ExpressionTree outerExpr;
    boolean isInterfaceSuper;
    boolean polySig;
    Ty sigRet;
  }

  static final class LambdaInfo {
    Ty target;
    List<VSym> params = new ArrayList<VSym>();
  }

  static final class RefInfo {
    int kind;
    CallInfo call;
    Ty target;
    Ty qualType;
    Ty arrayType;
    Ty superSite;
  }

  static final int REF_STATIC = 0;
  static final int REF_BOUND = 1;
  static final int REF_UNBOUND = 2;
  static final int REF_SUPER = 3;
  static final int REF_CTOR = 4;
  static final int REF_ARRAY_CTOR = 5;

  AttrCall(Attr a) {
    this.a = a;
    this.tx = a.tx;
    this.st = a.st;
  }

  /* ---------- inference variables ---------- */

  private int[] snapshot() {
    int n = a.openVars.size();
    int[] s = new int[n * 3];
    for (int i = 0; i < n; i++) {
      Ty.IVar v = a.openVars.get(i).iv;
      s[i * 3] = v.lower.size();
      s[i * 3 + 1] = v.upper.size();
      s[i * 3 + 2] = v.eq.size();
    }
    return s;
  }

  private void restore(int[] s) {
    int n = s.length / 3;
    for (int i = 0; i < n; i++) {
      Ty.IVar v = a.openVars.get(i).iv;
      trunc(v.lower, s[i * 3]);
      trunc(v.upper, s[i * 3 + 1]);
      trunc(v.eq, s[i * 3 + 2]);
    }
    while (a.openVars.size() > n) {
      a.openVars.remove(a.openVars.size() - 1);
    }
  }

  private static void trunc(List<Ty> l, int n) {
    while (l.size() > n) {
      l.remove(l.size() - 1);
    }
  }

  private Ty newIvar(TVSym tv) {
    Ty v = Ty.ivar(tv);
    a.openVars.add(v);
    return v;
  }

  void solveFrom(int mark) {
    if (a.openVars.size() <= mark) {
      return;
    }
    List<Ty> vs = new ArrayList<Ty>(a.openVars.subList(mark, a.openVars.size()));
    while (a.openVars.size() > mark) {
      a.openVars.remove(a.openVars.size() - 1);
    }
    solve(vs);
  }

  private boolean mentionsOther(Ty t, Set<Ty> todo, Ty self) {
    Ty r = TypeOps.resolve(t);
    return mentions(r, todo, self);
  }

  private boolean mentions(Ty t, Set<Ty> todo, Ty self) {
    switch (t.tag) {
      case Ty.IVAR:
        return t != self && t.iv.inst == null && todo.contains(t);
      case Ty.CLASS:
      case Ty.INTER:
        for (Ty x : t.args) {
          if (mentions(x, todo, self)) {
            return true;
          }
        }
        return false;
      case Ty.ARRAY:
        return mentions(t.elem, todo, self);
      case Ty.WILD:
        return t.bound != null && mentions(t.bound, todo, self);
      default:
        return false;
    }
  }

  void solve(List<Ty> vars) {
    Set<Ty> todo = new LinkedHashSet<Ty>();
    for (Ty v : vars) {
      Ty r = v.tag == Ty.IVAR ? v : null;
      if (r != null && r.iv.inst == null) {
        todo.add(r);
      }
    }
    int guard = 0;
    while (!todo.isEmpty() && guard++ < 10000) {
      Ty pick = null;
      for (Ty v : todo) {
        boolean dep = false;
        for (Ty b : v.iv.eq) {
          dep |= mentionsOther(b, todo, v);
        }
        for (Ty b : v.iv.lower) {
          dep |= mentionsOther(b, todo, v);
        }
        if (!dep) {
          pick = v;
          break;
        }
      }
      if (pick == null) {
        for (Ty v : todo) {
          boolean dep = false;
          for (Ty b : v.iv.eq) {
            dep |= mentionsOther(b, todo, v);
          }
          if (!dep) {
            pick = v;
            break;
          }
        }
      }
      if (pick == null) {
        pick = todo.iterator().next();
      }
      todo.remove(pick);
      pick.iv.inst = instantiate(pick);
      if (pick.iv.inst == pick) {
        pick.iv.inst = st.object;
      }
    }
  }

  private boolean hasWrapperBound(Ty v) {
    List<Ty> all = new ArrayList<Ty>();
    all.addAll(v.iv.eq);
    all.addAll(v.iv.lower);
    all.addAll(v.iv.upper);
    for (Ty b : all) {
      Ty r = TypeOps.resolve(b);
      if (r.tag == Ty.CLASS && r.sym != null && tx.unboxed(r) != null) {
        return true;
      }
    }
    return false;
  }

  private Ty instantiate(Ty v) {
    Ty.IVar iv = v.iv;
    Ty best = null;
    for (Ty e : iv.eq) {
      Ty r = TypeOps.resolve(e);
      if (r == v || r.tag == Ty.NULL) {
        continue;
      }
      if (!TypeOps.hasIvar(r)) {
        return r.tag == Ty.WILD ? tx.upper(r) : r;
      }
      if (best == null) {
        best = r;
      }
    }
    List<Ty> lowers = new ArrayList<Ty>();
    for (Ty l : iv.lower) {
      Ty r = TypeOps.resolve(l);
      if (r == v || r.tag == Ty.NULL) {
        continue;
      }
      lowers.add(r.isPrim() ? tx.box(r) : r);
    }
    if (!lowers.isEmpty()) {
      List<Ty> proper = new ArrayList<Ty>();
      for (Ty l : lowers) {
        if (!TypeOps.hasIvar(l)) {
          proper.add(l);
        }
      }
      Ty r = tx.lub(proper.isEmpty() ? lowers : proper);
      if (r.tag != Ty.NULL) {
        return r;
      }
    }
    if (best != null) {
      return best;
    }
    Ty glb = null;
    for (Ty u : iv.upper) {
      Ty r = TypeOps.resolve(u);
      if (r == v || r.tag == Ty.NULL) {
        continue;
      }
      if (mentionsSelf(r, v)) {
        r = TypeOps.erasure(r, st.object);
      }
      if (r.tag == Ty.WILD) {
        r = tx.upper(r);
      }
      if (glb == null) {
        glb = r;
      } else if (!r.isObject() && !(r.tag == Ty.IVAR)) {
        if (glb.isObject() || glb.tag == Ty.IVAR) {
          glb = r;
        } else {
          glb = tx.glb(glb, r);
        }
      }
    }
    return glb != null ? glb : st.object;
  }

  private boolean mentionsSelf(Ty t, Ty self) {
    Set<Ty> one = new LinkedHashSet<Ty>();
    one.add(self);
    return mentions(t, one, null);
  }

  private void collectFree(Ty t, List<Ty> out) {
    t = TypeOps.resolve(t);
    switch (t.tag) {
      case Ty.IVAR:
        if (!out.contains(t)) {
          out.add(t);
        }
        break;
      case Ty.CLASS:
      case Ty.INTER:
        for (Ty x : t.args) {
          collectFree(x, out);
        }
        break;
      case Ty.ARRAY:
        collectFree(t.elem, out);
        break;
      case Ty.WILD:
        if (t.bound != null) {
          collectFree(t.bound, out);
        }
        break;
      default:
        break;
    }
  }

  private boolean informative(Ty v) {
    Ty.IVar iv = v.iv;
    for (Ty e : iv.eq) {
      if (!TypeOps.hasIvar(e)) {
        return true;
      }
    }
    for (Ty l : iv.lower) {
      if (!TypeOps.hasIvar(l)) {
        return true;
      }
    }
    for (int i = iv.declared; i < iv.upper.size(); i++) {
      if (!TypeOps.hasIvar(iv.upper.get(i))) {
        return true;
      }
    }
    return false;
  }

  /** Resolves the free inference variables of the given types; defers if they carry no information. */
  private void solveInputs(List<Ty> inputs, boolean canDefer) {
    List<Ty> free = new ArrayList<Ty>();
    for (Ty t : inputs) {
      collectFree(t, free);
    }
    if (free.isEmpty()) {
      return;
    }
    if (canDefer) {
      for (Ty v : free) {
        if (!informative(v)) {
          throw new Attr.Deferral();
        }
      }
    }
    solve(free);
  }

  /* ---------- arguments ---------- */

  private ExpressionTree strip(ExpressionTree e) {
    return Attr.unparen(e);
  }

  private boolean hasFunctionalBranch(ExpressionTree e) {
    e = strip(e);
    if (e.getKind() == Tree.Kind.LAMBDA_EXPRESSION || e.getKind() == Tree.Kind.MEMBER_REFERENCE) {
      return true;
    }
    if (e.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION) {
      ConditionalExpressionTree c = (ConditionalExpressionTree) e;
      return hasFunctionalBranch(c.getTrueExpression()) || hasFunctionalBranch(c.getFalseExpression());
    }
    if (e.getKind() == Tree.Kind.SWITCH_EXPRESSION) {
      return true;
    }
    return false;
  }

  private static boolean isAnonDiamond(ExpressionTree s) {
    if (s.getKind() != Tree.Kind.NEW_CLASS) {
      return false;
    }
    NewClassTree nc = (NewClassTree) s;
    return nc.getClassBody() != null && nc.getIdentifier() instanceof ParameterizedTypeTree
        && ((ParameterizedTypeTree) nc.getIdentifier()).getTypeArguments().isEmpty();
  }

  List<Arg> attribArgs(List<? extends ExpressionTree> trees, Env env) {
    List<Arg> out = new ArrayList<Arg>();
    for (ExpressionTree e : trees) {
      Arg arg = new Arg();
      arg.tree = e;
      ExpressionTree s = strip(e);
      if (s.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
        arg.lambda = true;
      } else if (s.getKind() == Tree.Kind.MEMBER_REFERENCE) {
        arg.mref = true;
      } else if (hasFunctionalBranch(s) || isAnonDiamond(s)) {
        arg.poly = true;
      } else {
        boolean call = s.getKind() == Tree.Kind.METHOD_INVOCATION || s.getKind() == Tree.Kind.NEW_CLASS
            || s.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION;
        int mark = a.openVars.size();
        a.log.begin();
        try {
          a.deferNext = call;
          arg.type = a.expr(e, env, null);
          a.deferNext = false;
          a.log.commit();
        } catch (Attr.Deferral d) {
          a.deferNext = false;
          a.log.discard();
          while (a.openVars.size() > mark) {
            a.openVars.remove(a.openVars.size() - 1);
          }
          arg.poly = true;
          arg.type = null;
        }
      }
      out.add(arg);
    }
    return out;
  }

  /* ---------- applicability ---------- */

  static final class Inst {
    MSym m;
    Tx.MType mt;
    List<Ty> ivs = new ArrayList<Ty>();
    List<TVSym> tps;
    List<Ty> formals = new ArrayList<Ty>();
    Ty ret;
    boolean varargs;
    Ty varargElem;
    int fixed;
    List<Ty> params;
  }

  private Inst prepare(MSym m, Ty site, int nargs, boolean va, List<Ty> explicit, List<TVSym> classIvarTvs,
      List<Ty> classIvars) {
    Inst in = new Inst();
    in.m = m;
    in.mt = tx.memberType(m, site);
    in.varargs = va;
    List<Ty> params = new ArrayList<Ty>(in.mt.params);
    in.tps = in.mt.tparams;
    Ty ret = in.mt.ret;
    if (!in.tps.isEmpty()) {
      List<Ty> to = new ArrayList<Ty>();
      if (explicit != null && explicit.size() == in.tps.size()) {
        to.addAll(explicit);
      } else {
        for (TVSym tv : in.tps) {
          Ty iv = newIvar(tv);
          to.add(iv);
          in.ivs.add(iv);
        }
        for (int i = 0; i < in.tps.size(); i++) {
          addDeclaredBounds(in.ivs.get(i), in.tps.get(i), in.tps, to, in.mt.cfrom, in.mt.cto);
        }
      }
      params = TypeOps.subst(params, in.tps, to);
      ret = TypeOps.subst(ret, in.tps, to);
    }
    in.params = params;
    in.ret = ret;
    int n = params.size();
    in.fixed = va ? n - 1 : n;
    if (va && n > 0) {
      Ty last = params.get(n - 1);
      in.varargElem = last.tag == Ty.ARRAY ? last.elem : st.object;
    }
    for (int i = 0; i < nargs; i++) {
      if (va && i >= n - 1) {
        Ty last = params.get(n - 1);
        in.varargElem = last.tag == Ty.ARRAY ? last.elem : st.object;
        in.formals.add(in.varargElem);
      } else if (i < n) {
        in.formals.add(params.get(i));
      } else {
        in.formals.add(st.object);
      }
    }
    return in;
  }

  private void addDeclaredBounds(Ty iv, TVSym tv, List<TVSym> from, List<Ty> to) {
    addDeclaredBounds(iv, tv, from, to, null, null);
  }

  private void addDeclaredBounds(Ty iv, TVSym tv, List<TVSym> from, List<Ty> to, List<TVSym> cfrom, List<Ty> cto) {
    int n = 0;
    for (Ty b : tv.bounds()) {
      Ty sb = cfrom == null ? b : TypeOps.subst(b, cfrom, cto);
      sb = TypeOps.subst(sb, from, to);
      if (!sb.isObject()) {
        tx.isSubtype(iv, sb);
        n++;
      }
    }
    iv.iv.declared = iv.iv.upper.size();
  }

  private boolean argCompatible(Arg arg, Ty formal, int phase, Env env) {
    if (arg.type != null) {
      ExpressionTree ce = arg.tree == null ? null : strip(arg.tree);
      if (ce != null && ce.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION && !arg.type.isPrim()) {
        ConditionalExpressionTree cet = (ConditionalExpressionTree) ce;
        Ty t1 = a.typeOf(cet.getTrueExpression());
        Ty t2 = a.typeOf(cet.getFalseExpression());
        if (t1 != null && t2 != null && !t1.isPrim() && !t2.isPrim() && t1.tag != Ty.ERROR && t2.tag != Ty.ERROR) {
          Arg x = new Arg();
          x.tree = cet.getTrueExpression();
          x.type = t1;
          Arg y = new Arg();
          y.tree = cet.getFalseExpression();
          y.type = t2;
          return argCompatible(x, formal, phase, env) && argCompatible(y, formal, phase, env);
        }
      }
      Ty t = arg.type;
      if (t.tag == Ty.ERROR) {
        return true;
      }
      if (formal.isPrim()) {
        Ty rt = TypeOps.resolve(t);
        if (rt.tag == Ty.IVAR && rt.iv.inst == null && hasWrapperBound(rt)) {
          List<Ty> one = new ArrayList<Ty>();
          one.add(rt);
          solve(one);
          t = arg.type = TypeOps.resolve(rt);
        }
      }
      if (phase == 1) {
        if (t.isPrim() != formal.isPrim() && formal.tag != Ty.ERROR) {
          return false;
        }
        return tx.isSubtype(t, formal);
      }
      return tx.isLooseConvertible(t, formal);
    }
    return shapeCompatible(arg, formal, env);
  }

  private boolean shapeCompatible(Arg arg, Ty formal, Env env) {
    ExpressionTree e = strip(arg.tree);
    Ty f = TypeOps.resolve(formal);
    if (arg.poly) {
      if (e.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION) {
        ConditionalExpressionTree c = (ConditionalExpressionTree) e;
        return shapeOk(c.getTrueExpression(), f, env) && shapeOk(c.getFalseExpression(), f, env);
      }
      return true;
    }
    return shapeOk(e, f, env);
  }

  private boolean shapeOk(ExpressionTree e, Ty f, Env env) {
    e = strip(e);
    if (e.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
      LambdaExpressionTree lam = (LambdaExpressionTree) e;
      if (f.tag == Ty.IVAR) {
        return true;
      }
      if (f.tag == Ty.TVAR) {
        return false;
      }
      Tx.FnType fn = tx.fnType(f);
      if (fn == null) {
        return false;
      }
      if (fn.params.size() != lam.getParameters().size()) {
        return false;
      }
      boolean voidRet = fn.ret.tag == Ty.VOID;
      if (lam.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
        if (voidRet) {
          return isStatementExpression((ExpressionTree) lam.getBody());
        }
        ExpressionTree body = (ExpressionTree) lam.getBody();
        if (lam.getParameters().isEmpty() && body.getKind() == Tree.Kind.METHOD_INVOCATION && simpleArgs(body)) {
          try {
            a.log.speculative++;
            Ty bt = a.expr(body, env, null);
            if (bt != null && bt.tag == Ty.VOID) {
              return false;
            }
          } catch (RuntimeException ex) {
            return true;
          } finally {
            a.log.speculative--;
          }
        }
        return true;
      }
      boolean[] shape = blockShape((BlockTree) lam.getBody());
      if (voidRet) {
        return !shape[0];
      }
      return shape[0] || !shape[2];
    }
    if (e.getKind() == Tree.Kind.MEMBER_REFERENCE) {
      if (f.tag == Ty.IVAR) {
        return true;
      }
      if (f.tag == Ty.TVAR) {
        return false;
      }
      Tx.FnType fn = tx.fnType(f);
      return fn != null && mrefArityOk((MemberReferenceTree) e, fn.params.size(), env);
    }
    if (e.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION) {
      ConditionalExpressionTree c = (ConditionalExpressionTree) e;
      return shapeOk(c.getTrueExpression(), f, env) && shapeOk(c.getFalseExpression(), f, env);
    }
    return true;
  }

  private static boolean simpleArgs(ExpressionTree call) {
    for (ExpressionTree x : ((MethodInvocationTree) call).getArguments()) {
      switch (strip0(x).getKind()) {
        case METHOD_INVOCATION:
        case NEW_CLASS:
        case CONDITIONAL_EXPRESSION:
          return false;
        default:
          break;
      }
    }
    return true;
  }

  private boolean mrefArityOk(MemberReferenceTree mr, int n, Env env) {
    try {
      a.log.speculative++;
      ExpressionTree qt = mr.getQualifierExpression();
      boolean isSuper = qt.getKind() == Tree.Kind.IDENTIFIER && ((IdentifierTree) qt).getName().contentEquals("super");
      if (isSuper) {
        return true;
      }
      Attr.Qual q = a.qual(qt, env);
      if (q.kind == Attr.Qual.PKG || q.type == null || q.type.tag == Ty.ERROR) {
        return true;
      }
      Ty site = a.valueType(q.type);
      if (mr.getMode() == MemberReferenceTree.ReferenceMode.NEW) {
        if (site.tag != Ty.CLASS) {
          return true;
        }
        for (MSym m : site.sym.methodsNamed("<init>")) {
          if (m.params.size() == n || m.isVarargs() && n >= m.params.size() - 1) {
            return true;
          }
        }
        return false;
      }
      boolean typeQual = q.kind == Attr.Qual.TYPE;
      List<MSym> ms = site.tag == Ty.ARRAY ? a.res.methodsOf(st.object, mr.getName().toString())
          : a.res.methodsOf(site, mr.getName().toString());
      if (ms.isEmpty()) {
        return true;
      }
      for (MSym m : ms) {
        int pc = m.params.size();
        boolean va = m.isVarargs();
        if (m.isStatic() || !typeQual) {
          if (pc == n || va && n >= pc - 1) {
            return true;
          }
        }
        if (typeQual && !m.isStatic() && (pc == n - 1 || va && n - 1 >= pc - 1)) {
          return true;
        }
        if (typeQual && m.isStatic() && false) {
          return true;
        }
      }
      return false;
    } catch (RuntimeException e) {
      return true;
    } finally {
      a.log.speculative--;
    }
  }

  static boolean isStatementExpression(ExpressionTree e) {
    switch (strip0(e).getKind()) {
      case METHOD_INVOCATION:
      case NEW_CLASS:
      case ASSIGNMENT:
      case POSTFIX_INCREMENT:
      case POSTFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case PREFIX_DECREMENT:
        return true;
      default:
        return strip0(e) instanceof CompoundAssignmentTree;
    }
  }

  private static ExpressionTree strip0(ExpressionTree e) {
    return e instanceof ParenthesizedTree ? e : e;
  }

  /** Returns {hasValueReturn, hasVoidReturn, canCompleteNormally}. */
  private boolean[] blockShape(BlockTree b) {
    boolean[] r = new boolean[3];
    scanReturns(b, r);
    r[2] = Flow.canComplete(a, b);
    return r;
  }

  private void scanReturns(Tree t, boolean[] r) {
    if (t == null) {
      return;
    }
    switch (t.getKind()) {
      case RETURN:
        if (((ReturnTree) t).getExpression() != null) {
          r[0] = true;
        } else {
          r[1] = true;
        }
        return;
      case BLOCK:
        for (StatementTree s : ((BlockTree) t).getStatements()) {
          scanReturns(s, r);
        }
        return;
      case IF:
        scanReturns(((IfTree) t).getThenStatement(), r);
        scanReturns(((IfTree) t).getElseStatement(), r);
        return;
      case WHILE_LOOP:
        scanReturns(((WhileLoopTree) t).getStatement(), r);
        return;
      case DO_WHILE_LOOP:
        scanReturns(((DoWhileLoopTree) t).getStatement(), r);
        return;
      case FOR_LOOP:
        scanReturns(((ForLoopTree) t).getStatement(), r);
        return;
      case ENHANCED_FOR_LOOP:
        scanReturns(((EnhancedForLoopTree) t).getStatement(), r);
        return;
      case LABELED_STATEMENT:
        scanReturns(((LabeledStatementTree) t).getStatement(), r);
        return;
      case SYNCHRONIZED:
        scanReturns(((SynchronizedTree) t).getBlock(), r);
        return;
      case TRY: {
        TryTree tr = (TryTree) t;
        scanReturns(tr.getBlock(), r);
        for (CatchTree c : tr.getCatches()) {
          scanReturns(c.getBlock(), r);
        }
        scanReturns(tr.getFinallyBlock(), r);
        return;
      }
      case SWITCH:
        for (CaseTree c : ((SwitchTree) t).getCases()) {
          if (c.getStatements() != null) {
            for (StatementTree s : c.getStatements()) {
              scanReturns(s, r);
            }
          } else if (c.getBody() instanceof StatementTree) {
            scanReturns(c.getBody(), r);
          }
        }
        return;
      default:
        return;
    }
  }

  /** Checks applicability of m for the phase (1 strict, 2 loose, 3 varargs); bounds are rolled back by callers. */
  private Inst applicable(MSym m, Ty site, List<Arg> args, int phase, List<Ty> explicit, Env env, Ty classSite) {
    boolean va = phase == 3;
    int n = m.params.size();
    if (!va && args.size() != n) {
      return null;
    }
    if (va && (!m.isVarargs() || args.size() < n - 1)) {
      return null;
    }
    tx.conflict = false;
    Inst in = prepare(m, classSite != null ? classSite : site, args.size(), va, explicit, null, null);
    for (int i = 0; i < args.size(); i++) {
      if (!argCompatible(args.get(i), in.formals.get(i), phase, env)) {
        if (System.getProperty("javac.debug") != null) {
          System.err.println("arg " + i + " of " + m + " phase " + phase + ": " + args.get(i).type + " vs "
              + in.formals.get(i));
        }
        return null;
      }
    }
    if (tx.conflict) {
      if (System.getProperty("javac.debug") != null) {
        System.err.println("conflict in " + m + " phase " + phase);
      }
      return null;
    }
    return in;
  }

  private boolean moreSpecific(Inst x, Inst y, List<Arg> args, int phase) {
    int n = args.size();
    List<Ty> fx = specificFormals(x, n, phase);
    List<Ty> fy = specificFormals(y, n, phase);
    for (int i = 0; i < n; i++) {
      Ty p = fx.get(i);
      Ty q = fy.get(i);
      Arg arg = args.get(i);
      if (TypeOps.equal(p, q)) {
        continue;
      }
      if ((arg.lambda || arg.mref) && p.tag == Ty.CLASS && q.tag == Ty.CLASS && !tx.isSubtype(p, q)
          && !tx.isSubtype(q, p)) {
        Tx.FnType f1 = tx.fnType(p);
        Tx.FnType f2 = tx.fnType(q);
        if (f1 != null && f2 != null) {
          if (f2.ret.tag == Ty.VOID) {
            continue;
          }
          if (f1.ret.tag == Ty.VOID) {
            return false;
          }
          if (f1.ret.isPrim() && !f2.ret.isPrim() && lambdaReturnsPrim(arg)) {
            continue;
          }
          if (!f1.ret.isPrim() && f2.ret.isPrim() && !lambdaReturnsPrim(arg)) {
            continue;
          }
          if (tx.isSubtype(f1.ret, f2.ret)) {
            continue;
          }
          return false;
        }
      }
      if (!specificSub(p, q)) {
        return false;
      }
    }
    return true;
  }

  private boolean lambdaReturnsPrim(Arg arg) {
    ExpressionTree e = strip(arg.tree);
    if (e instanceof LambdaExpressionTree) {
      Tree b = ((LambdaExpressionTree) e).getBody();
      if (b instanceof ExpressionTree) {
        Object c = a.consts.get(b);
        return c != null && !(c instanceof String);
      }
    }
    return false;
  }

  private boolean specificSub(Ty p, Ty q) {
    p = specificErase(p);
    q = specificErase(q);
    if (p.isPrim() && q.isPrim()) {
      return Tx.primWiden(p.tag, q.tag);
    }
    if (p.isPrim() != q.isPrim()) {
      return false;
    }
    return tx.isSubtype(p, q);
  }

  private Ty specificErase(Ty t) {
    t = TypeOps.resolve(t);
    if (t.tag == Ty.IVAR) {
      Ty u = st.object;
      for (Ty b : t.iv.upper) {
        if (!TypeOps.hasIvar(b)) {
          u = b;
          break;
        }
      }
      return TypeOps.erasure(u, st.object);
    }
    if (TypeOps.hasIvar(t)) {
      return TypeOps.erasure(t, st.object);
    }
    return t;
  }

  private List<Ty> specificFormals(Inst x, int n, int phase) {
    List<Ty> out = new ArrayList<Ty>();
    List<Ty> ps = x.mt.params;
    for (int i = 0; i < n; i++) {
      Ty f;
      if (phase == 3 && i >= ps.size() - 1) {
        Ty last = ps.get(ps.size() - 1);
        f = last.tag == Ty.ARRAY ? last.elem : last;
      } else {
        f = i < ps.size() ? ps.get(i) : st.object;
      }
      out.add(eraseMethodTvars(f, x));
    }
    return out;
  }

  private Ty eraseMethodTvars(Ty t, Inst x) {
    if (x.tps.isEmpty()) {
      return t;
    }
    List<Ty> bounds = new ArrayList<Ty>();
    for (TVSym tv : x.tps) {
      bounds.add(TypeOps.erasure(tv.type, st.object));
    }
    return TypeOps.subst(t, x.tps, bounds);
  }

  static final class Chosen {
    MSym m;
    int phase;
    List<Inst> ambiguous;
  }

  private List<MSym> dropOverridden(List<MSym> cands, Ty site) {
    if (cands.size() < 2) {
      return cands;
    }
    List<MSym> out = new ArrayList<MSym>();
    for (MSym m : cands) {
      boolean overridden = false;
      for (MSym o : cands) {
        if (o == m || o.isStatic() || m.isStatic() || o.params.size() != m.params.size() || o.cls() == m.cls()
            || m.isCtor() || !a.res.isSubClass(o.cls(), m.cls())) {
          continue;
        }
        if (sameParamsAt(o, m, site)) {
          overridden = true;
          break;
        }
      }
      if (!overridden) {
        out.add(m);
      }
    }
    return out;
  }

  private boolean sameParamsAt(MSym o, MSym m, Ty site) {
    Ty s = site == null ? o.cls().type() : site;
    Tx.MType mo = tx.memberType(o, s);
    Tx.MType mm = tx.memberType(m, s);
    for (int i = 0; i < mo.params.size(); i++) {
      if (!tx.erasure(mo.params.get(i)).desc().equals(tx.erasure(mm.params.get(i)).desc())) {
        return false;
      }
    }
    return true;
  }

  /** Overload resolution: returns null when nothing is applicable. */
  private Chosen select(Env env, Ty site, List<MSym> cands0, List<Arg> args, List<Ty> explicit, Ty classSite) {
    List<MSym> cands = dropOverridden(cands0, site);
    for (int phase = 1; phase <= 3; phase++) {
      List<Inst> ok = new ArrayList<Inst>();
      for (MSym m : cands) {
        int[] snap = snapshot();
        Inst in = applicable(m, site, args, phase, explicit, env, classSite);
        restore(snap);
        if (in != null) {
          ok.add(in);
        }
      }
      if (ok.isEmpty()) {
        continue;
      }
      Chosen c = new Chosen();
      c.phase = phase;
      Inst best = null;
      List<Inst> maximal = new ArrayList<Inst>();
      for (Inst x : ok) {
        boolean max = true;
        for (Inst y : ok) {
          if (x != y && !moreSpecific(x, y, args, phase)) {
            max = false;
            break;
          }
        }
        if (max) {
          maximal.add(x);
        }
      }
      if (maximal.size() == 1) {
        c.m = maximal.get(0).m;
        return c;
      }
      List<Inst> pool = maximal.isEmpty() ? ok : maximal;
      Inst pick = pool.get(0);
      boolean sameSig = true;
      for (Inst x : pool) {
        if (!sameErased(x.m, pick.m)) {
          sameSig = false;
        }
      }
      if (sameSig) {
        for (Inst x : pool) {
          if ((x.m.flags & Sym.ABSTRACT) == 0) {
            pick = x;
            break;
          }
        }
        c.m = pick.m;
        return c;
      }
      c.m = pick.m;
      c.ambiguous = pool;
      return c;
    }
    return null;
  }

  private boolean sameErased(MSym x, MSym y) {
    if (x.params.size() != y.params.size()) {
      return false;
    }
    for (int i = 0; i < x.params.size(); i++) {
      if (!tx.erasure(x.params.get(i)).desc().equals(tx.erasure(y.params.get(i)).desc())) {
        return false;
      }
    }
    return true;
  }

  /* ---------- finishing a call ---------- */

  /** Final pass for the chosen method: adds constraints, attributes deferred arguments, computes the types. */
  private CallInfo finish(Env env, MSym m, Ty site, List<Arg> args, int phase, List<Ty> explicit, Ty pt,
      boolean canDefer, Tree pos, Ty classSite, boolean diamond) {
    boolean va = phase == 3;
    Inst in = prepare(m, classSite != null ? classSite : site, args.size(), va, explicit, null, null);
    for (int i = 0; i < args.size(); i++) {
      Arg arg = args.get(i);
      if (arg.type != null) {
        Ty f = in.formals.get(i);
        if (arg.type.tag != Ty.ERROR && !argCompatible(arg, f, phase, env)) {
          a.err(arg.tree, "compiler.err.prob.found.req", "incompatible types: " + arg.type + " cannot be converted to "
              + TypeOps.resolve(f));
        }
      }
    }
    Ty ret = in.ret;
    if (m.name.equals("getClass") && m.params.isEmpty() && m.owner == st.objectSym) {
      Ty s = site == null ? st.object : TypeOps.erasure(site, st.object);
      ret = Ty.cls(st.lookup("java/lang/Class"), Ty.wild(1, s));
    }
    if (diamond && classSite != null) {
      ret = classSite;
    }
    if (pt != null && pt.tag != Ty.NONE && pt.tag != Ty.VOID && TypeOps.hasIvar(ret)) {
      Ty rv = TypeOps.resolve(ret);
      if (pt.isPrim() && rv.tag == Ty.IVAR && rv.iv.inst == null && hasWrapperBound(rv)) {
        List<Ty> one = new ArrayList<Ty>();
        one.add(rv);
        solve(one);
      } else {
        Ty target = pt.isPrim() ? tx.box(pt) : pt;
        tx.isSubtype(ret.isPrim() ? tx.box(ret) : ret, target);
      }
    }
    for (int i = 0; i < args.size(); i++) {
      Arg arg = args.get(i);
      if (arg.type == null) {
        Ty f = in.formals.get(i);
        boolean savedCd = lambdaCanDefer;
        lambdaCanDefer = canDefer;
        Ty at;
        try {
          at = a.expr(arg.tree, env, f);
        } finally {
          lambdaCanDefer = savedCd;
        }
        arg.type = at;
        a.checkAssignable(arg.tree, at, f, a.consts.get(arg.tree));
        if (!a.deferNext) {
          // keep deferred results typed
        }
      }
    }
    CallInfo ci = new CallInfo();
    ci.m = m;
    ci.site = site;
    ci.formals = in.formals;
    ci.ret = ret;
    ci.varargs = va;
    ci.varargElem = in.varargElem;
    ci.fixed = in.fixed;
    return ci;
  }

  /* ---------- diagnostics ---------- */

  private String argTypes(List<Arg> args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      Arg g = args.get(i);
      if (g.type != null) {
        sb.append(TypeOps.resolve(g.type) == null ? "?" : typeName(TypeOps.resolve(g.type)));
      } else {
        sb.append(g.lambda ? "(lambda)" : g.mref ? "(method reference)" : "(deferred)");
      }
    }
    return sb.toString();
  }

  static String typeName(Ty t) {
    return t.tag == Ty.NULL ? "<null>" : t.toString();
  }

  private void reportNotFound(Tree pos, Env env, String kind, String name, List<Arg> args, String location) {
    a.err(pos, "compiler.err.cant.resolve.location.args", "cannot find symbol\n  symbol:   " + kind + " " + name + "("
        + argTypes(args) + ")\n  location: " + location);
  }

  private void reportInapplicable(Tree pos, List<MSym> cands, String name, List<Arg> args, boolean ctor, CSym owner,
      Ty site, Env env) {
    if (cands.size() == 1) {
      MSym m = cands.get(0);
      String reason = mismatchReason(m, site, args, env);
      String what = ctor ? "constructor " + m.owner.name + " in " + (m.cls().isEnum() ? "enum " : m.cls().isRecord()
          ? "record " : "class ") + m.cls().fullName() : "method " + name + " in " + (m.cls().isInterface()
          ? "interface " : "class ") + m.cls().fullName();
      StringBuilder req = new StringBuilder();
      for (int i = 0; i < m.params.size(); i++) {
        if (i > 0) {
          req.append(',');
        }
        req.append(m.params.get(i));
      }
      a.err(pos, "compiler.err.cant.apply.symbol", what + " cannot be applied to given types;\n  required: "
          + (m.params.isEmpty() ? "no arguments" : req.toString()) + "\n  found:    "
          + (args.isEmpty() ? "no arguments" : argTypes(args)) + "\n  reason: " + reason);
      return;
    }
    StringBuilder sb = new StringBuilder("no suitable " + (ctor ? "constructor" : "method") + " found for "
        + (ctor ? owner.name : name) + "(" + argTypes(args) + ")");
    for (MSym m : cands) {
      sb.append("\n    ").append(ctor ? "constructor " : "method ").append(m.cls().fullName()).append('.');
      sb.append(ctor ? m.cls().name : m.name).append('(');
      sb.append(Attr.paramList(m)).append(") is not applicable\n      (").append(mismatchReason(m, site, args, env))
          .append(')');
    }
    a.err(pos, "compiler.err.cant.apply.symbols", sb.toString());
  }

  private String mismatchReason(MSym m, Ty site, List<Arg> args, Env env) {
    int n = m.params.size();
    if (args.size() != n && !(m.isVarargs() && args.size() >= n - 1)) {
      return "actual and formal argument lists differ in length";
    }
    int phase = m.isVarargs() && args.size() != n ? 3 : 2;
    int[] snap = snapshot();
    try {
      tx.conflict = false;
      Inst in = prepare(m, site, args.size(), phase == 3, null, null, null);
      for (int i = 0; i < args.size(); i++) {
        if (!argCompatible(args.get(i), in.formals.get(i), phase, env)) {
          Arg g = args.get(i);
          Ty f = TypeOps.resolve(in.formals.get(i));
          if (g.type != null) {
            return "argument mismatch; " + typeName(TypeOps.resolve(g.type)) + " cannot be converted to " + f;
          }
          return "argument mismatch; " + f + " is not a functional interface";
        }
      }
      if (tx.conflict) {
        return "inference variable has incompatible bounds";
      }
    } finally {
      restore(snap);
    }
    return "argument mismatch";
  }

  /* ---------- method invocation ---------- */

  Ty invoke(MethodInvocationTree mi, Env env, Ty pt) {
    boolean canDefer = a.deferNext;
    a.deferNext = false;
    int mark = a.openVars.size();
    Ty r = invoke0(mi, env, pt, canDefer);
    if (!canDefer || !TypeOps.hasIvar(r)) {
      a.closeOpen(mark);
    }
    return r;
  }

  private Ty invoke0(MethodInvocationTree mi, Env env, Ty pt, boolean canDefer) {
    ExpressionTree sel = mi.getMethodSelect();
    List<Ty> explicit = null;
    if (!mi.getTypeArguments().isEmpty()) {
      explicit = new ArrayList<Ty>();
      for (Tree ta : mi.getTypeArguments()) {
        Ty t = a.enter.resolveType(ta, env);
        a.types.put(ta, t);
        explicit.add(t);
      }
    }
    String name;
    Ty site = null;
    boolean staticOnly = false;
    CSym implicitOwner = null;
    boolean isSuper = false;
    boolean ifaceSuper = false;
    CSym superQual = null;
    List<MSym> cands;
    String location;
    if (sel.getKind() == Tree.Kind.IDENTIFIER) {
      name = ((IdentifierTree) sel).getName().toString();
      if (name.equals("this") || name.equals("super")) {
        return ctorCall(mi, name.equals("super"), null, env, pt);
      }
      cands = null;
      site = null;
      boolean isStatic = env.staticCtx;
      for (Env e = env; e != null; e = e.outer) {
        if (e.isClassEnv) {
          List<MSym> l0 = a.res.methodsOf(e.cls.type(), name);
          List<MSym> l = new ArrayList<MSym>();
          for (MSym cm : l0) {
            if (a.res.accessible(cm, cm.cls(), env)) {
              l.add(cm);
            }
          }
          if (!l.isEmpty()) {
            cands = l;
            site = e.cls.type();
            implicitOwner = e.cls;
            staticOnly = isStatic;
            break;
          }
          CSym c = e.cls;
          isStatic = c.isStatic() || c.isInterface() || c.isEnum() || c.isRecord()
              || ((c.local || c.anon) && e.outer != null && e.outer.staticCtx);
        }
      }
      if (cands == null) {
        Env.CuEnv ce = env.cu;
        a.enter.resolveImports(ce);
        List<MSym> l = new ArrayList<MSym>();
        List<CSym> from = ce.staticSingle.get(name);
        if (from != null) {
          for (CSym c : from) {
            for (MSym m : a.res.methodsOf(c.erasedType(), name)) {
              if (m.isStatic()) {
                l.add(m);
              }
            }
          }
        }
        if (l.isEmpty()) {
          for (CSym c : ce.staticOnDemand) {
            for (MSym m : a.res.methodsOf(c.erasedType(), name)) {
              if (m.isStatic()) {
                l.add(m);
              }
            }
          }
        }
        if (!l.isEmpty()) {
          cands = l;
          site = l.get(0).cls().erasedType();
          staticOnly = true;
        }
      }
      location = enterLocation(env);
    } else if (sel.getKind() == Tree.Kind.MEMBER_SELECT) {
      MemberSelectTree ms = (MemberSelectTree) sel;
      name = ms.getIdentifier().toString();
      ExpressionTree q = ms.getExpression();
      if (q.getKind() == Tree.Kind.IDENTIFIER && ((IdentifierTree) q).getName().toString().equals("super")) {
        isSuper = true;
        site = env.cls.superclass();
        if (site == null) {
          site = st.object;
        }
        a.types.put(q, site);
        a.syms.put(q, env.cls);
        location = "class " + site;
        if (env.staticCtx) {
          a.err(q, "compiler.err.non-static.cant.be.ref", "non-static variable super cannot be referenced from a static"
              + " context");
        }
      } else if (q.getKind() == Tree.Kind.MEMBER_SELECT
          && ((MemberSelectTree) q).getIdentifier().toString().equals("super")) {
        Attr.Qual oq = a.qual(((MemberSelectTree) q).getExpression(), env);
        isSuper = true;
        if (oq.kind == Attr.Qual.TYPE && oq.type.tag == Ty.CLASS) {
          CSym oc = oq.type.sym;
          superQual = oc;
          if (oc.isInterface()) {
            ifaceSuper = true;
            site = Ty.cls(oc, findIfaceArgs(env.cls, oc));
          } else {
            site = oc.superclass() == null ? st.object : oc.superclass();
          }
          a.types.put(q, site);
          a.syms.put(q, oc);
        } else {
          site = Ty.tError;
        }
        location = "class " + site;
      } else {
        Attr.Qual qu = a.qual(q, env);
        if (qu.kind == Attr.Qual.PKG) {
          a.err(q, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   variable "
              + qu.pkg.substring(qu.pkg.lastIndexOf('.') + 1) + "\n  location: " + enterLocation(env));
          attribArgsQuiet(mi.getArguments(), env);
          return Ty.tError;
        }
        site = qu.type;
        a.types.put(q, site);
        staticOnly = qu.kind == Attr.Qual.TYPE;
        location = qu.kind == Attr.Qual.TYPE ? "class " + site : "variable " + q + " of type " + site;
        if (site.tag == Ty.ERROR) {
          attribArgsQuiet(mi.getArguments(), env);
          return Ty.tError;
        }
        site = a.valueType(site);
        if (site.isPrim()) {
          a.err(sel, "compiler.err.cant.deref", site + " cannot be dereferenced");
          attribArgsQuiet(mi.getArguments(), env);
          return Ty.tError;
        }
      }
      cands = null;
    } else {
      a.err(sel, "compiler.err.illegal.start.of.expr", "illegal start of expression");
      return Ty.tError;
    }
    List<Arg> args = attribArgs(mi.getArguments(), env);
    if (cands == null) {
      if (site == null) {
        cands = new ArrayList<MSym>();
      } else if (site.tag == Ty.ARRAY) {
        cands = new ArrayList<MSym>();
        if (name.equals("clone") && args.isEmpty()) {
          MSym cl = new MSym();
          cl.name = "clone";
          cl.flags = Sym.PUBLIC;
          cl.owner = st.objectSym;
          cl.ret = site;
          cands.add(cl);
        } else {
          cands.addAll(a.res.methodsOf(st.object, name));
        }
      } else {
        cands = a.res.methodsOf(site, name);
      }
    }
    List<MSym> acc = new ArrayList<MSym>();
    for (MSym m : cands) {
      if (a.res.accessible(m, m.cls(), env) || m.owner == st.objectSym && site != null && site.tag == Ty.ARRAY) {
        acc.add(m);
      }
    }
    if (acc.isEmpty()) {
      if (cands.isEmpty()) {
        reportNotFound(sel, env, "method", name, args, location);
      } else {
        MSym f = cands.get(0);
        a.err(sel, "compiler.err.report.access", name + "(" + Attr.paramList(f) + ") has "
            + (f.isPrivate() ? "private" : f.flags == 0 ? "package" : "protected") + " access in "
            + f.cls().fullName());
      }
      for (Arg g : args) {
        quietDeferred(g, env);
      }
      return Ty.tError;
    }
    Chosen ch = select(env, site, acc, args, explicit, null);
    if (ch == null) {
      reportInapplicable(sel, dropOverridden(acc, site), name, args, false, null, site, env);
      for (Arg g : args) {
        quietDeferred(g, env);
      }
      return Ty.tError;
    }
    if (ch.ambiguous != null) {
      MSym m1 = ch.ambiguous.get(0).m;
      MSym m2 = ch.ambiguous.get(1).m;
      a.err(sel, "compiler.err.ref.ambiguous", "reference to " + name + " is ambiguous\n  both method " + name + "("
          + Attr.paramList(m1) + ") in " + m1.cls().fullName() + " and method " + name + "(" + Attr.paramList(m2)
          + ") in " + m2.cls().fullName() + " match");
    }
    MSym m = ch.m;
    CallInfo ci = finish(env, m, site, args, ch.phase, explicit, pt, canDefer, sel, null, false);
    ci.implicitOwner = implicitOwner;
    ci.superCall = isSuper;
    ci.superQualifier = superQual;
    ci.isInterfaceSuper = ifaceSuper;
    ci.staticQual = staticOnly && sel.getKind() == Tree.Kind.MEMBER_SELECT;
    if (!m.isStatic() && staticOnly) {
      if (sel.getKind() == Tree.Kind.MEMBER_SELECT) {
        a.err(sel, "compiler.err.non-static.cant.be.ref", "non-static method " + m.name + "(" + Attr.paramList(m)
            + ") cannot be referenced from a static context");
      } else {
        a.err(sel, "compiler.err.non-static.cant.be.ref", "non-static method " + m.name + "(" + Attr.paramList(m)
            + ") cannot be referenced from a static context");
      }
    }
    if (env.ctorPrologue && !m.isStatic() && implicitOwner == env.cls) {
      a.err(sel, "compiler.err.cant.ref.before.ctor.called", "cannot reference this before supertype constructor has"
          + " been called");
    }
    if (isPolySig(m)) {
      ci.polySig = true;
      ci.ret = m.ret.tag == Ty.CLASS && m.ret.sym == st.object.sym ? st.object : m.ret;
      if (ci.ret != st.object) {
        ci.sigRet = ci.ret;
      }
    }
    a.syms.put(mi, m);
    a.info.put(mi, ci);
    return ci.ret;
  }

  static boolean isPolySig(MSym m) {
    String o = m.cls().internal;
    if (m.isStatic() || !m.isVarargs() || m.params.size() != 1) {
      return false;
    }
    if (o.equals("java/lang/invoke/MethodHandle")) {
      return m.name.equals("invoke") || m.name.equals("invokeExact");
    }
    return o.equals("java/lang/invoke/VarHandle");
  }

  private Ty[] findIfaceArgs(CSym from, CSym iface) {
    Ty t = TypeOps.asSuper(from.type(), iface);
    return t == null ? Ty.NO_ARGS : t.args;
  }

  private String enterLocation(Env env) {
    return a.enter.locationOf(env);
  }

  private void attribArgsQuiet(List<? extends ExpressionTree> trees, Env env) {
    a.log.speculative++;
    try {
      List<Arg> args = attribArgs(trees, env);
      for (Arg g : args) {
        quietDeferredNoLog(g, env);
      }
    } catch (RuntimeException e) {
      // ignore follow-up failures
    } finally {
      a.log.speculative--;
    }
  }

  private void quietDeferred(Arg g, Env env) {
    if (g.type == null) {
      quietDeferredNoLog(g, env);
    }
  }

  private void quietDeferredNoLog(Arg g, Env env) {
    if (g.type != null) {
      return;
    }
    a.log.speculative++;
    try {
      a.types.put(g.tree, Ty.tError);
    } finally {
      a.log.speculative--;
    }
  }

  /* ---------- constructors ---------- */

  private Ty ctorCall(MethodInvocationTree mi, boolean isSuper, ExpressionTree outer, Env env, Ty pt) {
    CSym c = env.cls;
    if (env.method == null || !env.method.isCtor()) {
      a.err(mi, "compiler.err.call.must.be.first.stmt.in.ctor", "call to " + (isSuper ? "super" : "this")
          + " must be first statement in constructor");
    }
    Ty target = isSuper ? c.superclass() : c.type();
    if (target == null || target.tag != Ty.CLASS) {
      return Ty.tVoid;
    }
    List<Arg> args = attribArgs(mi.getArguments(), env);
    List<MSym> cands = new ArrayList<MSym>(target.sym.methodsNamed("<init>"));
    Chosen ch = select(env, target, cands, args, null, null);
    if (ch == null) {
      reportInapplicable(mi, cands, "<init>", args, true, target.sym, target, env);
      return Ty.tVoid;
    }
    CallInfo ci = finish(env, ch.m, target, args, ch.phase, null, null, false, mi, null, false);
    ci.ctorCall = true;
    ci.superCall = isSuper;
    ci.thisCall = !isSuper;
    ci.classType = target;
    a.syms.put(mi, ch.m);
    a.info.put(mi, ci);
    return Ty.tVoid;
  }

  void implicitSuperCall(CSym c, MSym ctor, Tree tree, Env env) {
    Ty target = c.superclass();
    if (target == null || target.tag != Ty.CLASS) {
      return;
    }
    List<Arg> args = new ArrayList<Arg>();
    List<MSym> cands = new ArrayList<MSym>();
    for (MSym m : target.sym.methodsNamed("<init>")) {
      if (a.res.accessible(m, m.cls(), env)) {
        cands.add(m);
      }
    }
    Chosen ch = select(env, target, cands, args, null, null);
    Tree key = ctor.tree != null ? ctor.tree : c.tree;
    if (ch == null) {
      if (!cands.isEmpty() || target.sym.methodsNamed("<init>").isEmpty()) {
        reportInapplicable(ctor.tree != null ? (Tree) ctor.tree : c.tree, cands, "<init>", args, true, target.sym,
            target, env);
      } else {
        a.err(tree, "compiler.err.report.access", target.sym.name + "() has private access in "
            + target.sym.fullName());
      }
      return;
    }
    CallInfo ci = finish(env, ch.m, target, args, ch.phase, null, null, false, tree, null, false);
    ci.ctorCall = true;
    ci.superCall = true;
    ci.classType = target;
    implicitSupers.put(key, ci);
  }

  final java.util.Map<Tree, CallInfo> implicitSupers = new java.util.IdentityHashMap<Tree, CallInfo>();

  static final class ImplicitKey {
    final Tree t;

    ImplicitKey(Tree t) {
      this.t = t;
    }
  }

  Ty newClass(NewClassTree nc, Env env, Ty pt) {
    boolean canDefer = a.deferNext;
    a.deferNext = false;
    int mark = a.openVars.size();
    Ty r = newClass0(nc, env, pt, canDefer);
    if (!canDefer || !TypeOps.hasIvar(r)) {
      a.closeOpen(mark);
    }
    return r;
  }

  private Ty newClass0(NewClassTree nc, Env env, Ty pt, boolean canDefer) {
    ExpressionTree outerExpr = nc.getEnclosingExpression();
    Ty ct;
    Ty outerTy = null;
    if (outerExpr != null) {
      outerTy = a.expr(outerExpr, env, null);
      Tree id = nc.getIdentifier();
      String n = id instanceof IdentifierTree ? ((IdentifierTree) id).getName().toString()
          : id instanceof ParameterizedTypeTree ? ((ParameterizedTypeTree) id).getType().toString() : id.toString();
      Ty ot = a.valueType(outerTy);
      CSym mc = ot.tag == Ty.CLASS ? a.enter.memberClassOf(ot.sym, n) : null;
      if (mc == null) {
        a.err(id, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class " + n + "\n  location: "
            + "class " + ot);
        attribArgsQuiet(nc.getArguments(), env);
        return Ty.tError;
      }
      ct = Ty.cls(mc);
      if (id instanceof ParameterizedTypeTree) {
        List<? extends Tree> targs = ((ParameterizedTypeTree) id).getTypeArguments();
        if (targs.isEmpty()) {
          ct = Ty.cls(mc, Enter.diamondMarker());
        } else {
          Ty[] as = new Ty[targs.size()];
          for (int i = 0; i < as.length; i++) {
            as[i] = a.enter.resolveType(targs.get(i), env);
          }
          ct = Ty.cls(mc, as);
        }
      }
    } else {
      ct = a.enter.resolveType(nc.getIdentifier(), env);
    }
    a.types.put(nc.getIdentifier(), ct);
    if (ct.tag != Ty.CLASS) {
      attribArgsQuiet(nc.getArguments(), env);
      if (nc.getClassBody() != null) {
        // still enter the body so later phases see consistent trees
      }
      return Ty.tError;
    }
    CSym cls = ct.sym;
    boolean diamond = Enter.isDiamond(ct);
    ClassTree body = nc.getClassBody();
    if (body == null) {
      if (cls.isAbstract() || cls.isInterface()) {
        a.err(nc, "compiler.err.abstract.cant.be.instantiated", cls.fullName() + " is abstract; cannot be instantiated");
      }
      if (cls.isEnum() && !allowEnumNew) {
        a.err(nc, "compiler.err.enum.cant.be.instantiated", "enum classes may not be instantiated");
      }
    }
    List<Arg> args = attribArgs(nc.getArguments(), env);
    List<MSym> cands = new ArrayList<MSym>();
    boolean iface = cls.isInterface();
    if (iface) {
      if (body == null) {
        quiet(args, env);
        return Ty.tError;
      }
      if (!args.isEmpty()) {
        a.err(nc, "compiler.err.anon.class.impl.intf.no.args", "anonymous class implements interface; cannot have"
            + " arguments");
      }
      MSym om = null;
      for (MSym m : st.objectSym.methodsNamed("<init>")) {
        om = m;
      }
      cands.add(om);
    } else {
      for (MSym m : cls.methodsNamed("<init>")) {
        if (a.res.accessible(m, cls, env) || body != null && !m.isPrivate()) {
          cands.add(m);
        }
      }
    }
    Ty classSite = ct;
    List<Ty> ivs = null;
    if (diamond) {
      ivs = new ArrayList<Ty>();
      List<TVSym> tps = cls.typeParams();
      for (TVSym tv : tps) {
        ivs.add(newIvar(tv));
      }
      for (int i = 0; i < tps.size(); i++) {
        addDeclaredBounds(ivs.get(i), tps.get(i), tps, ivs);
      }
      classSite = Ty.cls(cls, ivs.toArray(new Ty[0]));
    }
    if (iface && !diamond) {
      classSite = null;
    }
    Chosen ch = select(env, ct, cands, args, null, classSite);
    if (ch == null) {
      if (cands.isEmpty() && cls.methodsNamed("<init>").size() > 0) {
        MSym f = cls.methodsNamed("<init>").get(0);
        a.err(nc, "compiler.err.report.access", cls.name + "(" + Attr.paramList(f) + ") has private access in "
            + cls.fullName());
      } else {
        reportInapplicable(nc, cands, "<init>", args, true, cls, ct, env);
      }
      quiet(args, env);
      return Ty.tError;
    }
    if (ch.ambiguous != null) {
      MSym m1 = ch.ambiguous.get(0).m;
      MSym m2 = ch.ambiguous.get(1).m;
      a.err(nc, "compiler.err.ref.ambiguous", "reference to " + cls.name + " is ambiguous\n  both constructor "
          + cls.name + "(" + Attr.paramList(m1) + ") in " + cls.fullName() + " and constructor " + cls.name + "("
          + Attr.paramList(m2) + ") in " + cls.fullName() + " match");
    }
    CallInfo ci = finish(env, ch.m, ct, args, ch.phase, null, pt, canDefer, nc, classSite, diamond);
    ci.ctorCall = true;
    ci.outerExpr = outerExpr;
    Ty result = diamond ? classSite : ct;
    if (diamond) {
      ci.ret = classSite;
    } else {
      ci.ret = ct;
    }
    ci.classType = result;
    a.syms.put(nc, ch.m);
    a.info.put(nc, ci);
    if (body != null) {
      Ty superTy = diamond ? solveNow(classSite) : ct;
      CSym anon = a.enter.enterAnon(body, env, superTy);
      ci.anon = anon;
      anon.attrData = nc;
      a.syms.put(nc, ch.m);
      a.attribClass(anon);
      return anon.type();
    }
    return result;
  }

  private Ty solveNow(Ty t) {
    List<Ty> free = new ArrayList<Ty>();
    collectFree(t, free);
    solve(free);
    return TypeOps.resolve(t);
  }

  private void quiet(List<Arg> args, Env env) {
    for (Arg g : args) {
      quietDeferred(g, env);
    }
  }

  /* ---------- lambdas ---------- */

  private boolean isFunctionalOrVar(Ty t) {
    return tx.fnType(t) != null;
  }

  Ty lambda(LambdaExpressionTree lam, Env env, Ty pt) {
    boolean cd = lambdaCanDefer;
    lambdaCanDefer = false;
    if (pt == null || pt.tag == Ty.NONE) {
      a.err(lam, "compiler.err.unexpected.lambda", "lambda expression not expected here");
      return Ty.tError;
    }
    Ty target = TypeOps.resolve(pt);
    if (target.tag == Ty.IVAR) {
      if (cd && !informative(target)) {
        throw new Attr.Deferral();
      }
      List<Ty> one = new ArrayList<Ty>();
      one.add(target);
      solve(one);
      target = TypeOps.resolve(target);
    }
    if (target.tag == Ty.WILD) {
      target = tx.upper(target);
    }
    Tx.FnType fn = tx.fnType(target);
    if (fn == null) {
      if (target.tag != Ty.ERROR) {
        a.err(lam, "compiler.err.prob.found.req", "incompatible types: " + target + " is not a functional interface");
      }
      attribLambdaBodyQuiet(lam, env);
      return Ty.tError;
    }
    List<? extends VariableTree> ps = lam.getParameters();
    if (ps.size() != fn.params.size()) {
      a.err(lam, "compiler.err.prob.found.req", "incompatible types: incompatible parameter types in lambda expression");
      attribLambdaBodyQuiet(lam, env);
      return target;
    }
    boolean explicitParams = !ps.isEmpty() && ps.get(0).getType() != null;
    if (!explicitParams) {
      solveInputs(fn.params, cd);
    }
    LambdaInfo li = new LambdaInfo();
    li.target = target;
    Env le = env.dup();
    le.lambda = lam;
    le.scopeOwner = lam;
    le.returnType = fn.ret;
    le.inLambdaBody = true;
    le.yieldCtx = null;
    List<Ty> returns = new ArrayList<Ty>();
    le.lambdaReturns = returns;
    for (int i = 0; i < ps.size(); i++) {
      VariableTree p = ps.get(i);
      VSym v = new VSym();
      v.name = p.getName().toString();
      v.flags = Enter.modFlags(p.getModifiers());
      v.param = true;
      v.scopeOwner = lam;
      v.tree = p;
      Ty ft = fn.params.get(i);
      if (p.getType() != null) {
        Ty dt = a.enter.resolveType(p.getType(), le);
        a.types.put(p.getType(), dt);
        tx.sameType(dt, ft);
        v.type = dt;
      } else {
        v.type = a.tx.upper(TypeOps.resolve(ft));
        if (v.type.tag == Ty.IVAR) {
          v.type = st.object;
        }
      }
      VSym prev = env.findLocal(v.name);
      if (prev != null && !v.name.equals("_") && !v.name.isEmpty()) {
        a.err(p, "compiler.err.already.defined", "variable " + v.name + " is already defined in method "
            + (env.method == null ? "<init>" : env.method.name) + "(" + (env.method == null ? ""
            : Attr.paramList(env.method)) + ")");
      }
      a.syms.put(p, v);
      a.types.put(p, v.type);
      le.addVar(v);
      li.params.add(v);
    }
    Tree body = lam.getBody();
    if (lam.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
      ExpressionTree be = (ExpressionTree) body;
      if (fn.ret.tag == Ty.VOID) {
        a.expr(be, le, null);
        if (!isStatementExpression(be)) {
          a.err(be, "compiler.err.prob.found.req", "incompatible types: bad return type in lambda expression\n    "
              + a.typeOf(be) + " cannot be converted to void");
        }
      } else {
        Ty bt = a.expr(be, le, fn.ret);
        lambdaReturnCompat(be, bt, fn.ret, le);
      }
    } else {
      a.stat.stat((BlockTree) body, le);
      if (fn.ret.tag != Ty.VOID && Flow.canComplete(a, (BlockTree) body)) {
        a.err(body, "compiler.err.prob.found.req", "incompatible types: bad return type in lambda expression\n    "
            + "missing return value");
      }
    }
    a.info.put(lam, li);
    return target;
  }

  boolean lambdaCanDefer;
  boolean allowEnumNew;
  boolean refCanDefer;

  private void attribLambdaBodyQuiet(LambdaExpressionTree lam, Env env) {
    a.types.put(lam, Ty.tError);
  }

  void lambdaReturnCompat(ExpressionTree e, Ty t, Ty r, Env env) {
    if (r == null || r.tag == Ty.VOID || t == null || t.tag == Ty.ERROR) {
      return;
    }
    if (t.tag == Ty.VOID) {
      a.err(e, "compiler.err.prob.found.req", "incompatible types: bad return type in lambda expression\n    void "
          + "cannot be converted to " + TypeOps.resolve(r));
      return;
    }
    if (tx.isLooseConvertible(t, r)) {
      return;
    }
    Object cv = a.consts.get(e);
    Ty rr = TypeOps.resolve(r);
    if (cv != null && rr.isPrim() && t.isIntegral() && t.tag <= Ty.INT && Attr.fitsConstant(cv, rr)) {
      return;
    }
    a.err(e, "compiler.err.prob.found.req", "incompatible types: bad return type in lambda expression\n    "
        + TypeOps.resolve(t) + " cannot be converted to " + rr);
  }

  /* ---------- method references ---------- */

  Ty memberRef(MemberReferenceTree mr, Env env, Ty pt) {
    boolean cd = lambdaCanDefer;
    lambdaCanDefer = false;
    refCanDefer = cd;
    if (pt == null || pt.tag == Ty.NONE) {
      a.err(mr, "compiler.err.unexpected.mref", "method reference not expected here");
      return Ty.tError;
    }
    Ty target = TypeOps.resolve(pt);
    if (target.tag == Ty.IVAR) {
      if (cd && !informative(target)) {
        throw new Attr.Deferral();
      }
      List<Ty> one = new ArrayList<Ty>();
      one.add(target);
      solve(one);
      target = TypeOps.resolve(target);
    }
    Tx.FnType fn = tx.fnType(target);
    if (fn == null) {
      if (target.tag != Ty.ERROR) {
        a.err(mr, "compiler.err.prob.found.req", "incompatible types: " + target + " is not a functional interface");
      }
      return Ty.tError;
    }
    String name = mr.getName().toString();
    boolean isNew = mr.getMode() == MemberReferenceTree.ReferenceMode.NEW;
    ExpressionTree qt = mr.getQualifierExpression();
    RefInfo ri = new RefInfo();
    ri.target = target;
    boolean isSuper = qt.getKind() == Tree.Kind.IDENTIFIER && ((IdentifierTree) qt).getName().toString().equals("super");
    Attr.Qual q;
    boolean qualifiedSuper = qt.getKind() == Tree.Kind.MEMBER_SELECT
        && ((MemberSelectTree) qt).getIdentifier().toString().equals("super");
    if (qualifiedSuper) {
      Attr.Qual oq = a.qual(((MemberSelectTree) qt).getExpression(), env);
      if (oq.kind != Attr.Qual.TYPE || oq.type.tag != Ty.CLASS) {
        return Ty.tError;
      }
      CSym oc = oq.type.sym;
      Ty site;
      if (oc.isInterface()) {
        site = Ty.cls(oc, findIfaceArgs(env.cls, oc));
      } else {
        site = oc.superclass() == null ? st.object : oc.superclass();
      }
      isSuper = true;
      q = new Attr.Qual();
      q.kind = Attr.Qual.EXPR;
      q.type = site;
      ri.superSite = site;
      a.types.put(qt, site);
      a.syms.put(qt, oc);
    } else if (isSuper) {
      q = new Attr.Qual();
      q.kind = Attr.Qual.EXPR;
      q.type = env.cls.superclass() == null ? st.object : env.cls.superclass();
      a.types.put(qt, q.type);
      a.syms.put(qt, env.cls);
    } else {
      q = a.qual(qt, env);
    }
    if (q.kind == Attr.Qual.PKG) {
      a.err(qt, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class " + q.pkg
          + "\n  location: " + enterLocation(env));
      return Ty.tError;
    }
    Ty qty = q.type;
    ri.qualType = qty;
    List<Ty> memberTargs = null;
    if (mr.getTypeArguments() != null && !mr.getTypeArguments().isEmpty()) {
      memberTargs = new ArrayList<Ty>();
      for (ExpressionTree ta : mr.getTypeArguments()) {
        memberTargs.add(a.enter.resolveType(ta, env));
      }
    }
    if (isNew) {
      if (qty.tag == Ty.ARRAY) {
        ri.kind = REF_ARRAY_CTOR;
        ri.arrayType = qty;
        a.info.put(mr, ri);
        tx.isLooseConvertible(qty, fn.ret);
        return target;
      }
      ri.kind = REF_CTOR;
      mrefInputs(fn, qty, null, name, true, false);
      CSym cls = qty.sym;
      List<Arg> args = argsFor(fn.params, 0);
      List<MSym> cands = new ArrayList<MSym>();
      for (MSym m : cls.methodsNamed("<init>")) {
        if (a.res.accessible(m, cls, env)) {
          cands.add(m);
        }
      }
      Ty classSite = qty;
      boolean raw = qty.args.length == 0 && !cls.typeParams().isEmpty();
      List<Ty> ivs = null;
      if (raw) {
        ivs = new ArrayList<Ty>();
        for (TVSym tv : cls.typeParams()) {
          ivs.add(newIvar(tv));
        }
        for (int i = 0; i < ivs.size(); i++) {
          addDeclaredBounds(ivs.get(i), cls.tparams.get(i), cls.tparams, ivs);
        }
        classSite = Ty.cls(cls, ivs.toArray(new Ty[0]));
      }
      Chosen ch = select(env, qty, cands, args, null, classSite);
      if (ch == null) {
        reportRefFail(mr, name, args, cands, true, cls, env);
        return Ty.tError;
      }
      CallInfo ci = finish(env, ch.m, qty, args, ch.phase, null, null, false, mr, classSite, raw);
      ci.ctorCall = true;
      ci.classType = classSite;
      ci.ret = classSite;
      ri.call = ci;
      if (fn.ret.tag != Ty.VOID) {
        tx.isLooseConvertible(classSite, fn.ret);
      }
      a.syms.put(mr, ch.m);
      a.info.put(mr, ri);
      a.types.put(mr, target);
      return target;
    }
    boolean typeQual = q.kind == Attr.Qual.TYPE;
    Ty site = a.valueType(qty);
    if (site.tag == Ty.ARRAY && !typeQual) {
      site = qty;
    }
    List<MSym> cands = site.tag == Ty.ARRAY ? new ArrayList<MSym>(a.res.methodsOf(st.object, name))
        : a.res.methodsOf(site, name);
    if (site.tag == Ty.ARRAY && name.equals("clone")) {
      MSym cl = new MSym();
      cl.name = "clone";
      cl.flags = Sym.PUBLIC;
      cl.owner = st.objectSym;
      cl.ret = site;
      cands.add(cl);
    }
    List<MSym> acc = new ArrayList<MSym>();
    for (MSym m : cands) {
      if (a.res.accessible(m, m.cls(), env)) {
        acc.add(m);
      }
    }
    if (acc.isEmpty()) {
      reportRefFail(mr, name, new ArrayList<Arg>(), cands, false, null, env);
      return Ty.tError;
    }
    acc = dropOverridden(acc, site);
    mrefInputs(fn, site, acc, name, false, typeQual);
    List<Ty> ps = new ArrayList<Ty>();
    for (Ty p : fn.params) {
      ps.add(TypeOps.resolve(p));
    }
    Chosen first = null;
    Chosen second = null;
    List<Arg> argsAll = argsFor(ps, 0);
    first = select(env, site, acc, argsAll, memberTargs, null);
    Ty recvSite = site;
    if (typeQual && !ps.isEmpty()) {
      Ty p1 = a.tx.upper(ps.get(0));
      Ty erasedQ = TypeOps.erasure(site, st.object);
      if (p1.tag != Ty.ERROR && tx.isSubtype(p1, erasedQ)) {
        Ty sup = TypeOps.asSuper(p1, site.tag == Ty.CLASS ? site.sym : st.objectSym);
        if (sup != null && (site.args.length == 0 || true) && site.args.length == 0) {
          recvSite = sup;
        }
        List<Arg> argsRest = argsFor(ps, 1);
        second = select(env, recvSite, acc, argsRest, memberTargs, null);
      }
    }
    Chosen pick;
    boolean useSecond;
    if (first != null && second != null) {
      boolean fs = first.m.isStatic();
      boolean ss = second.m.isStatic();
      if (fs && !ss) {
        useSecond = false;
        if (!first.m.isStatic()) {
          useSecond = true;
        }
      } else if (!fs && ss) {
        useSecond = false;
      } else if (!fs && !ss) {
        useSecond = true;
      } else {
        useSecond = false;
      }
      if (fs && !ss) {
        useSecond = false;
        if (!fs) {
          useSecond = true;
        }
      }
      if (fs && !ss) {
        useSecond = true;
        a.err(mr, "compiler.err.ref.ambiguous", "incompatible types: invalid method reference\n    reference to "
            + name + " is ambiguous");
        useSecond = false;
      }
      pick = useSecond ? second : first;
    } else if (first != null) {
      pick = first;
      useSecond = false;
    } else if (second != null) {
      pick = second;
      useSecond = true;
    } else {
      reportRefFail(mr, name, argsAll, acc, false, null, env);
      return Ty.tError;
    }
    if (typeQual) {
      if (useSecond && pick.m.isStatic()) {
        useSecond = false;
      }
      if (!useSecond && !pick.m.isStatic()) {
        a.err(mr, "compiler.err.prob.found.req", "incompatible types: invalid method reference\n    unexpected "
            + "instance method " + name + "(" + Attr.paramList(pick.m) + ") found in unbound lookup");
      }
    }
    List<Arg> useArgs = useSecond ? argsFor(ps, 1) : argsAll;
    Ty useSite = useSecond ? recvSite : site;
    CallInfo ci = finish(env, pick.m, useSite, useArgs, pick.phase, memberTargs, null, false, mr, null, false);
    ci.superCall = isSuper;
    ri.call = ci;
    ri.kind = isSuper ? REF_SUPER : pick.m.isStatic() ? REF_STATIC : useSecond ? REF_UNBOUND : REF_BOUND;
    if (fn.ret.tag != Ty.VOID) {
      Ty r = ci.ret;
      if (r.tag == Ty.VOID) {
        a.err(mr, "compiler.err.prob.found.req", "incompatible types: bad return type in method reference\n    void "
            + "cannot be converted to " + TypeOps.resolve(fn.ret));
      } else if (!tx.isLooseConvertible(r, fn.ret)) {
        a.err(mr, "compiler.err.prob.found.req", "incompatible types: bad return type in method reference\n    "
            + TypeOps.resolve(r) + " cannot be converted to " + TypeOps.resolve(fn.ret));
      }
    }
    a.syms.put(mr, pick.m);
    a.info.put(mr, ri);
    return target;
  }

  private void reportRefFail(MemberReferenceTree mr, String name, List<Arg> args, List<MSym> cands, boolean ctor,
      CSym cls, Env env) {
    if (cands.isEmpty()) {
      a.err(mr, "compiler.err.invalid.mref", "invalid method reference\n  cannot find symbol\n    symbol:   method "
          + name + "(" + argTypes(args) + ")");
    } else {
      a.err(mr, "compiler.err.invalid.mref", "incompatible types: invalid " + (ctor ? "constructor" : "method")
          + " reference\n    no suitable " + (ctor ? "constructor" : "method") + " found for " + (ctor ? cls.name : name)
          + "(" + argTypes(args) + ")");
    }
  }

  private List<Arg> argsFor(List<Ty> ps, int from) {
    List<Arg> out = new ArrayList<Arg>();
    for (int i = from; i < ps.size(); i++) {
      Arg g = new Arg();
      Ty t = TypeOps.resolve(ps.get(i));
      if (t.tag == Ty.WILD) {
        t = tx.upper(t);
      }
      g.type = t;
      out.add(g);
    }
    return out;
  }

  /** Makes the input types of a method reference proper, using exactness when they are unconstrained. */
  private void mrefInputs(Tx.FnType fn, Ty site, List<MSym> cands, String name, boolean isNew, boolean typeQual) {
    List<Ty> free = new ArrayList<Ty>();
    for (Ty p : fn.params) {
      collectFree(p, free);
    }
    if (free.isEmpty()) {
      return;
    }
    boolean uninformative = false;
    for (Ty v : free) {
      if (!informative(v)) {
        uninformative = true;
      }
    }
    if (uninformative) {
      if (isNew && site.tag == Ty.CLASS) {
        List<MSym> ctors = new ArrayList<MSym>(site.sym.methodsNamed("<init>"));
        if (ctors.size() == 1 && !ctors.get(0).isVarargs() && ctors.get(0).tparams.isEmpty()
            && ctors.get(0).params.size() == fn.params.size()) {
          for (int i = 0; i < fn.params.size(); i++) {
            constrainInput(fn.params.get(i), ctors.get(0).params.get(i));
          }
        }
      } else if (cands != null && cands.size() == 1 && !cands.get(0).isVarargs() && cands.get(0).tparams.isEmpty()) {
        MSym m = cands.get(0);
        if (typeQual && !m.isStatic() && fn.params.size() == m.params.size() + 1) {
          tx.isSubtype(fn.params.get(0), TypeOps.erasure(site, st.object));
          Tx.MType mt = tx.memberType(m, site);
          for (int i = 0; i < m.params.size(); i++) {
            constrainInput(fn.params.get(i + 1), mt.params.get(i));
          }
        } else if (fn.params.size() == m.params.size()) {
          Tx.MType mt = tx.memberType(m, site);
          for (int i = 0; i < m.params.size(); i++) {
            constrainInput(fn.params.get(i), mt.params.get(i));
          }
        }
      }
    }
    solveInputs(fn.params, refCanDefer && !allInformative(fn.params));
  }

  private void constrainInput(Ty p, Ty mp) {
    if (mp.isPrim()) {
      tx.sameType(p, tx.box(mp));
    } else {
      tx.isSubtype(p, tx.erasure(mp));
    }
  }

  private boolean allInformative(List<Ty> ps) {
    List<Ty> free = new ArrayList<Ty>();
    for (Ty p : ps) {
      collectFree(p, free);
    }
    for (Ty v : free) {
      if (!informative(v)) {
        return false;
      }
    }
    return true;
  }
}
