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
import java.util.List;
import java.util.Set;

/** Attribution of statements, switches and patterns. */
final class AttrStat {
  private final Attr a;

  /** Collects yielded values of the innermost switch expression. */
  static final class YieldCtx {
    final List<ExpressionTree> values = new ArrayList<ExpressionTree>();
    Ty pt;
  }

  AttrStat(Attr a) {
    this.a = a;
  }

  Ty top(ExpressionTree e, Env env, Ty pt) {
    int mark = a.openVars.size();
    Ty t = a.expr(e, env, pt);
    a.closeOpen(mark);
    return t;
  }

  Ty topAs(ExpressionTree e, Env env, Ty req) {
    int mark = a.openVars.size();
    Ty t = a.expr(e, env, req);
    a.checkAssignable(e, t, req, a.consts.get(e));
    a.closeOpen(mark);
    return t;
  }

  void stat(StatementTree s, Env env) {
    if (s == null) {
      return;
    }
    switch (s.getKind()) {
      case BLOCK: {
        Env b = env.dup();
        block(((BlockTree) s).getStatements(), b);
        return;
      }
      case VARIABLE:
        localVar((VariableTree) s, env);
        return;
      case EXPRESSION_STATEMENT: {
        ExpressionTree es = ((ExpressionStatementTree) s).getExpression();
        top(es, env, null);
        Object pci = a.info.get(es);
        if (pci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) pci).polySig
            && ((AttrCall.CallInfo) pci).ret == a.st.object) {
          ((AttrCall.CallInfo) pci).sigRet = Ty.tVoid;
        }
        return;
      }
      case IF: {
        IfTree i = (IfTree) s;
        Ty ct = top(i.getCondition(), env, null);
        a.checkAssignable(i.getCondition(), ct, Ty.tBoolean, null);
        stat(i.getThenStatement(), scoped(env, a.bindings(i.getCondition(), true)));
        if (i.getElseStatement() != null) {
          stat(i.getElseStatement(), scoped(env, a.bindings(i.getCondition(), false)));
        }
        return;
      }
      case WHILE_LOOP: {
        WhileLoopTree w = (WhileLoopTree) s;
        Ty ct = top(w.getCondition(), env, null);
        a.checkAssignable(w.getCondition(), ct, Ty.tBoolean, null);
        stat(w.getStatement(), scoped(env, a.bindings(w.getCondition(), true)));
        return;
      }
      case DO_WHILE_LOOP: {
        DoWhileLoopTree d = (DoWhileLoopTree) s;
        stat(d.getStatement(), env.dup());
        Ty ct = top(d.getCondition(), env, null);
        a.checkAssignable(d.getCondition(), ct, Ty.tBoolean, null);
        return;
      }
      case FOR_LOOP: {
        ForLoopTree f = (ForLoopTree) s;
        Env fe = env.dup();
        for (StatementTree i : f.getInitializer()) {
          stat(i, fe);
        }
        Env be = fe;
        if (f.getCondition() != null) {
          Ty ct = top(f.getCondition(), fe, null);
          a.checkAssignable(f.getCondition(), ct, Ty.tBoolean, null);
          be = scoped(fe, a.bindings(f.getCondition(), true));
        }
        for (ExpressionStatementTree u : f.getUpdate()) {
          stat(u, be);
        }
        stat(f.getStatement(), be.dup());
        return;
      }
      case ENHANCED_FOR_LOOP:
        foreach((EnhancedForLoopTree) s, env);
        return;
      case LABELED_STATEMENT:
        stat(((LabeledStatementTree) s).getStatement(), env);
        return;
      case RETURN:
        ret((ReturnTree) s, env);
        return;
      case THROW: {
        ExpressionTree e = ((ThrowTree) s).getExpression();
        Ty t = top(e, env, null);
        Ty thr = Ty.cls(a.st.lookup("java/lang/Throwable"));
        if (t.tag != Ty.ERROR && !a.tx.isSubtype(t, thr)) {
          a.err(e, "compiler.err.prob.found.req", "incompatible types: " + t + " cannot be converted to Throwable");
        }
        return;
      }
      case BREAK:
      case CONTINUE:
      case EMPTY_STATEMENT:
        return;
      case SWITCH:
        switchStat((SwitchTree) s, env);
        return;
      case YIELD: {
        YieldTree y = (YieldTree) s;
        YieldCtx yc = (YieldCtx) env.switchEnv_ctx();
        int mark = a.openVars.size();
        Ty t = a.expr(y.getValue(), env, yc == null ? null : yc.pt);
        if (yc != null) {
          yc.values.add(y.getValue());
        }
        a.closeOpen(mark);
        return;
      }
      case TRY:
        tryStat((TryTree) s, env);
        return;
      case SYNCHRONIZED: {
        SynchronizedTree sy = (SynchronizedTree) s;
        Ty t = top(sy.getExpression(), env, null);
        if (t.isPrim()) {
          a.err(sy.getExpression(), "compiler.err.type.found.req", "unexpected type\n  required: reference\n  found:    "
              + t);
        }
        stat(sy.getBlock(), env);
        return;
      }
      case ASSERT: {
        AssertTree as = (AssertTree) s;
        Ty ct = top(as.getCondition(), env, null);
        a.checkAssignable(as.getCondition(), ct, Ty.tBoolean, null);
        if (as.getDetail() != null) {
          top(as.getDetail(), env, null);
        }
        return;
      }
      case CLASS:
      case INTERFACE:
      case ENUM:
      case RECORD:
      case ANNOTATION_TYPE: {
        ClassTree ct = (ClassTree) s;
        CSym c = a.enter.enterLocal(ct, env);
        env.addLocalClass(c);
        a.attribClass(c);
        return;
      }
      default:
        a.err(s, "compiler.err.not.stmt", "not a statement");
    }
  }

  private Env scoped(Env env, List<VSym> bindings) {
    Env e = env.dup();
    for (VSym v : bindings) {
      e.addVar(v);
    }
    return e;
  }

  void block(List<? extends StatementTree> stmts, Env env) {
    boolean dead = false;
    for (StatementTree st : stmts) {
      if (dead && st.getKind() != Tree.Kind.CLASS) {
        a.err(st, "compiler.err.unreachable.stmt", "unreachable statement");
        dead = false;
      }
      stat(st, env);
      if (!Flow.canComplete(a, st)) {
        dead = true;
      }
      if (st instanceof IfTree) {
        IfTree i = (IfTree) st;
        if (i.getElseStatement() == null && !Flow.canComplete(a, i.getThenStatement())) {
          for (VSym v : a.bindings(i.getCondition(), false)) {
            env.addVar(v);
          }
        } else if (i.getElseStatement() != null) {
          boolean tc = Flow.canComplete(a, i.getThenStatement());
          boolean ec = Flow.canComplete(a, i.getElseStatement());
          if (!tc && ec) {
            for (VSym v : a.bindings(i.getCondition(), false)) {
              env.addVar(v);
            }
          } else if (tc && !ec) {
            for (VSym v : a.bindings(i.getCondition(), true)) {
              env.addVar(v);
            }
          }
        }
      } else if (st instanceof WhileLoopTree) {
        WhileLoopTree w = (WhileLoopTree) st;
        if (!Flow.breaksOut(w.getStatement(), null, 0)) {
          for (VSym v : a.bindings(w.getCondition(), false)) {
            env.addVar(v);
          }
        }
      }
    }
  }

  private void checkDup(Env env, String name, Tree at) {
    VSym prev = env.findLocal(name);
    if (prev != null && !name.equals("_") && !name.isEmpty()) {
      a.err(at, "compiler.err.already.defined", "variable " + name + " is already defined in method "
          + (env.method == null ? "<init>" : env.method.isCtor() ? env.method.owner.name : env.method.name) + "("
          + (env.method == null ? "" : Attr.paramList(env.method)) + ")");
    }
  }

  private void localVar(VariableTree vt, Env env) {
    VSym v = new VSym();
    v.name = vt.getName().toString();
    v.flags = Enter.modFlags(vt.getModifiers());
    v.scopeOwner = env.scopeOwner;
    v.tree = vt;
    ExpressionTree init = vt.getInitializer();
    Tree tt = vt.getType();
    int mark = a.openVars.size();
    if (tt == null) {
      if (init == null) {
        a.err(vt, "compiler.err.cant.infer.local.var.type", "cannot infer type for local variable " + v.name
            + "\n  (cannot use 'var' on variable without initializer)");
        v.type = Ty.tError;
      } else {
        Ty it = a.expr(init, env, null);
        a.closeOpen(mark);
        it = TypeOps.resolve(it);
        if (it.tag == Ty.NULL) {
          a.err(vt, "compiler.err.cant.infer.local.var.type", "cannot infer type for local variable " + v.name
              + "\n  (variable initializer is 'null')");
          it = Ty.tError;
        } else if (it.tag == Ty.VOID) {
          a.err(vt, "compiler.err.cant.infer.local.var.type", "cannot infer type for local variable " + v.name
              + "\n  (variable initializer is 'void')");
          it = Ty.tError;
        }
        v.type = upwardProject(it);
      }
    } else {
      v.type = a.enter.resolveType(tt, env);
      a.types.put(tt, v.type);
      if (init != null) {
        if (init.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) init).getType() == null) {
          a.attribInitializer((NewArrayTree) init, env, v.type);
        } else {
          Ty it = a.expr(init, env, v.type);
          a.checkAssignable(init, it, v.type, a.consts.get(init));
          if ((v.flags & Sym.FINAL) != 0 && (v.type.isPrim() || v.type.isString())) {
            v.constant = a.foldedConstant(init, v.type);
          }
        }
        a.closeOpen(mark);
      }
    }
    checkDup(env, v.name, vt);
    a.syms.put(vt, v);
    a.types.put(vt, v.type);
    env.addVar(v);
  }

  Ty upwardProject(Ty t) {
    if (t.tag == Ty.WILD) {
      return a.tx.upper(t);
    }
    if (t.tag == Ty.INTER) {
      return t;
    }
    return t;
  }

  private void foreach(EnhancedForLoopTree f, Env env) {
    Env fe = env.dup();
    Ty et = top(f.getExpression(), env, null);
    et = TypeOps.resolve(et);
    Ty elem;
    Ty vt = et.tag == Ty.WILD ? a.tx.upper(et) : et;
    if (vt.tag == Ty.ARRAY) {
      elem = vt.elem;
    } else if (vt.tag == Ty.ERROR) {
      elem = Ty.tError;
    } else {
      CSym it = a.st.lookup("java/lang/Iterable");
      Ty sup = TypeOps.asSuper(vt, it);
      if (sup == null) {
        a.err(f.getExpression(), "compiler.err.foreach.not.applicable.to.type", "for-each not applicable to expression"
            + " type\n  required: array or java.lang.Iterable\n  found:    " + et);
        elem = Ty.tError;
      } else if (sup.args.length == 0) {
        elem = a.st.object;
      } else {
        elem = a.tx.upper(sup.args[0]);
        if (elem.tag == Ty.WILD) {
          elem = a.st.object;
        }
      }
    }
    a.info.put(f, elem);
    VariableTree vtree = f.getVariable();
    VSym v = new VSym();
    v.name = vtree.getName().toString();
    v.flags = Enter.modFlags(vtree.getModifiers());
    v.scopeOwner = env.scopeOwner;
    v.tree = vtree;
    if (vtree.getType() == null) {
      v.type = elem;
    } else {
      v.type = a.enter.resolveType(vtree.getType(), fe);
      a.types.put(vtree.getType(), v.type);
      if (elem.tag != Ty.ERROR) {
        a.checkAssignable(vtree, elem, v.type, null);
      }
    }
    checkDup(fe, v.name, vtree);
    a.syms.put(vtree, v);
    a.types.put(vtree, v.type);
    fe.addVar(v);
    stat(f.getStatement(), fe.dup());
  }

  private void ret(ReturnTree r, Env env) {
    ExpressionTree e = r.getExpression();
    if (env.lambda != null && env.inLambdaBody) {
      Ty rt = env.returnType;
      if (e == null) {
        return;
      }
      int mark = a.openVars.size();
      Ty t = a.expr(e, env, rt != null && rt.tag != Ty.VOID ? rt : null);
      if (env.lambdaReturns != null) {
        env.lambdaReturns.add(t);
      }
      a.call.lambdaReturnCompat(e, t, rt, env);
      a.closeOpen(mark);
      return;
    }
    Ty rt = env.returnType;
    if (e == null) {
      if (rt != null && rt.tag != Ty.VOID && env.method != null && !env.method.isCtor()) {
        a.err(r, "compiler.err.prob.found.req", "incompatible types: missing return value");
      }
      return;
    }
    if (rt == null || rt.tag == Ty.VOID) {
      top(e, env, null);
      a.err(e, "compiler.err.prob.found.req", "incompatible types: unexpected return value");
      return;
    }
    topAs(e, env, rt);
  }

  private void tryStat(TryTree t, Env env) {
    Env te = env.dup();
    for (Tree res : t.getResources()) {
      if (res instanceof VariableTree) {
        VariableTree vt = (VariableTree) res;
        localVar(vt, te);
        VSym v = (VSym) a.syms.get(vt);
        v.flags |= Sym.FINAL;
        Ty ac = Ty.cls(a.st.lookup("java/lang/AutoCloseable"));
        if (v.type.tag != Ty.ERROR && !a.tx.isSubtype(v.type, ac)) {
          a.err(vt, "compiler.err.prob.found.req", "incompatible types: try-with-resources not applicable to"
              + " variable type\n    (" + v.type + " cannot be converted to AutoCloseable)");
        }
      } else {
        Ty rt = top((ExpressionTree) res, te, null);
      }
    }
    stat(t.getBlock(), te);
    for (CatchTree c : t.getCatches()) {
      Env ce = env.dup();
      VariableTree p = c.getParameter();
      VSym v = new VSym();
      v.name = p.getName().toString();
      v.flags = Enter.modFlags(p.getModifiers());
      v.scopeOwner = env.scopeOwner;
      v.tree = p;
      Tree pt = p.getType();
      Ty ty = a.enter.resolveType(pt, ce);
      a.types.put(pt, ty);
      if (pt.getKind() == Tree.Kind.UNION_TYPE) {
        for (Tree alt : ((UnionTypeTree) pt).getTypeAlternatives()) {
          Ty at = a.enter.resolveType(alt, ce);
          a.types.put(alt, at);
        }
      }
      v.type = ty;
      Ty thr = Ty.cls(a.st.lookup("java/lang/Throwable"));
      if (ty.tag != Ty.ERROR && !a.tx.isSubtype(ty, thr)) {
        a.err(pt, "compiler.err.prob.found.req", "incompatible types: " + ty + " cannot be converted to Throwable");
      }
      checkDup(ce, v.name, p);
      a.syms.put(p, v);
      a.types.put(p, ty);
      ce.addVar(v);
      stat(c.getBlock(), ce);
    }
    if (t.getFinallyBlock() != null) {
      stat(t.getFinallyBlock(), env);
    }
  }

  /* ---------- switch ---------- */

  private void switchStat(SwitchTree sw, Env env) {
    switchCommon(sw, sw.getExpression(), sw.getCases(), env, false, null);
  }

  Ty switchExpr(SwitchExpressionTree sw, Env env, Ty pt) {
    return switchCommon(sw, sw.getExpression(), sw.getCases(), env, true, pt);
  }

  private Ty switchCommon(Tree sw, ExpressionTree selector, List<? extends CaseTree> cases, Env env, boolean isExpr,
      Ty pt) {
    Ty sel = top(selector, env, null);
    sel = TypeOps.resolve(sel);
    Ty selU = sel.isPrim() ? sel : a.unboxedOrSelf(sel);
    boolean isEnum = sel.tag == Ty.CLASS && sel.sym.isEnum();
    boolean isString = sel.isString();
    boolean isIntLike = selU.isPrim() && selU.tag <= Ty.INT && selU.tag != Ty.BOOLEAN;
    boolean hasPattern = false;
    boolean hasNull = false;
    boolean hasDefault = false;
    boolean unguardedTotal = false;
    Set<Object> seen = new HashSet<Object>();
    Env se = env.dup();
    YieldCtx yc = null;
    if (isExpr) {
      yc = new YieldCtx();
      yc.pt = pt != null && pt.tag != Ty.NONE ? pt : null;
      se.setYieldCtx(yc);
    }
    se.breakableSwitch = true;
    List<Ty> resultTypes = new ArrayList<Ty>();
    List<ExpressionTree> resultExprs = new ArrayList<ExpressionTree>();
    List<Ty> coveringTypes = new ArrayList<Ty>();
    Set<String> enumSeen = new HashSet<String>();
    boolean arrowForm = !cases.isEmpty() && cases.get(0).getCaseKind() == CaseTree.CaseKind.RULE;
    Env shared = se.dup();
    for (CaseTree c : cases) {
      Env ce = arrowForm ? se.dup() : shared;
      Env caseScope = ce.dup();
      for (CaseLabelTree l : c.getLabels()) {
        if (l instanceof DefaultCaseLabelTree) {
          hasDefault = true;
        } else if (l instanceof PatternCaseLabelTree) {
          hasPattern = true;
          List<VSym> bs = new ArrayList<VSym>();
          PatternTree p = ((PatternCaseLabelTree) l).getPattern();
          pattern(p, caseScope, sel, bs);
          for (VSym v : bs) {
            caseScope.addVar(v);
          }
          Ty patTy = a.typeOf(p);
          if (c.getGuard() == null && patTy != null) {
            coveringTypes.add(coveredType(p));
          }
          if (patTy != null && sel.tag != Ty.ERROR && !a.tx.isCastable(sel, patTy)) {
            a.err(l, "compiler.err.prob.found.req", "incompatible types: " + sel + " cannot be converted to " + patTy);
          }
        } else if (l instanceof ConstantCaseLabelTree) {
          ExpressionTree ce2 = ((ConstantCaseLabelTree) l).getConstantExpression();
          if (ce2.getKind() == Tree.Kind.NULL_LITERAL) {
            hasNull = true;
            a.types.put(ce2, Ty.tNull);
            continue;
          }
          if (isEnum && ce2 instanceof IdentifierTree) {
            String n = ((IdentifierTree) ce2).getName().toString();
            FSym f = a.res.findField(sel.sym, n);
            if (f == null || (f.flags & Sym.ENUM) == 0) {
              a.err(ce2, "compiler.err.enum.label.must.be.enum.constant", "an enum switch case label must be the"
                  + " unqualified name of an enumeration constant");
            } else {
              a.syms.put(ce2, f);
              a.types.put(ce2, sel);
              if (!enumSeen.add(n)) {
                a.err(ce2, "compiler.err.duplicate.case.label", "duplicate case label");
              }
            }
          } else {
            Ty ct = a.expr(ce2, caseScope, null);
            Object cv = a.consts.get(ce2);
            Sym cs = ce2 instanceof IdentifierTree || ce2 instanceof MemberSelectTree ? a.symOf(ce2) : null;
            if (cs instanceof FSym && (((FSym) cs).flags & Sym.ENUM) != 0) {
              enumSeen.add(cs.name);
              hasPattern = hasPattern || !isEnum;
            } else if (cv == null) {
              if (ct.tag != Ty.ERROR) {
                a.err(ce2, "compiler.err.const.expr.req", "constant expression required");
              }
            } else {
              Object key = cv instanceof Character ? (Object) Integer.valueOf(((Character) cv).charValue())
                  : cv instanceof Number ? (Object) Long.valueOf(((Number) cv).longValue()) : cv;
              if (!seen.add(key)) {
                a.err(ce2, "compiler.err.duplicate.case.label", "duplicate case label");
              }
              if (isString && !(cv instanceof String)) {
                a.err(ce2, "compiler.err.prob.found.req", "incompatible types: " + ct + " cannot be converted to String");
              } else if (isIntLike) {
                a.checkAssignable(ce2, ct, selU, cv);
              }
            }
          }
        }
      }
      if (c.getGuard() != null) {
        Ty gt = a.expr(c.getGuard(), caseScope, null);
        a.checkAssignable(c.getGuard(), gt, Ty.tBoolean, null);
        for (VSym v : a.bindings(c.getGuard(), true)) {
          caseScope.addVar(v);
        }
        Object gc = a.consts.get(c.getGuard());
        if (gc instanceof Boolean && ((Boolean) gc).booleanValue() && !c.getLabels().isEmpty()) {
          // an always-true guard behaves like no guard
          for (CaseLabelTree l : c.getLabels()) {
            if (l instanceof PatternCaseLabelTree) {
              coveringTypes.add(coveredType(((PatternCaseLabelTree) l).getPattern()));
            }
          }
        }
      }
      if (arrowForm) {
        Tree body = c.getBody();
        if (body instanceof ExpressionTree) {
          ExpressionTree be = (ExpressionTree) body;
          if (isExpr) {
            int mark = a.openVars.size();
            Ty bt = a.expr(be, caseScope, yc.pt);
            yc.values.add(be);
            a.closeOpen(mark);
          } else {
            top(be, caseScope, null);
          }
        } else if (body instanceof StatementTree) {
          stat((StatementTree) body, caseScope);
        }
      } else if (c.getStatements() != null) {
        block(c.getStatements(), caseScope);
        for (VSym v : caseScope.vars == null ? new ArrayList<VSym>() : new ArrayList<VSym>(caseScope.vars.values())) {
          if (!v.isPattern) {
            shared.addVar(v);
          }
        }
      }
    }
    boolean newStyle = hasPattern || hasNull || isExpr || (!isEnum && !isString && !isIntLike);
    boolean exhaustive = hasDefault;
    if (!exhaustive) {
      if (isEnum) {
        int total = 0;
        sel.sym.members();
        for (FSym f : sel.sym.fields.values()) {
          if ((f.flags & Sym.ENUM) != 0) {
            total++;
          }
        }
        exhaustive = enumSeen.size() == total;
      }
      if (!exhaustive && !coveringTypes.isEmpty()) {
        exhaustive = covers(sel, coveringTypes, enumSeen);
      }
      if (!exhaustive && sel.tag == Ty.CLASS && sel.sym.isInterface() == false && sel.isRef() && isBoolBox(sel)) {
        exhaustive = false;
      }
    }
    if (!exhaustive && sel.tag != Ty.ERROR) {
      if (isExpr) {
        a.err(sw, "compiler.err.not.exhaustive", "the switch expression does not cover all possible input values");
      } else if (hasPattern || hasNull || (!isEnum && !isString && !isIntLike)) {
        a.err(sw, "compiler.err.not.exhaustive.statement", "the switch statement does not cover all possible input"
            + " values");
      }
    }
    a.info.put(selector, sel);
    SwitchData sd = new SwitchData();
    sd.selectorType = sel;
    sd.exhaustive = exhaustive && !hasDefault;
    sd.hasPattern = hasPattern;
    sd.hasNull = hasNull;
    sd.isEnum = isEnum;
    sd.isString = isString;
    sd.isIntLike = isIntLike;
    a.syms.put(sw, sd);
    if (!isExpr) {
      return null;
    }
    List<Ty> rts = new ArrayList<Ty>();
    for (ExpressionTree e : yc.values) {
      Ty t = a.typeOf(e);
      if (t != null) {
        rts.add(t);
      }
    }
    Ty result;
    if (yc.pt != null) {
      result = yc.pt;
      for (ExpressionTree e : yc.values) {
        Ty t = a.typeOf(e);
        if (t != null) {
          a.checkAssignable(e, t, yc.pt, a.consts.get(e));
        }
      }
    } else {
      result = standaloneResult(rts, yc.values);
    }
    return result;
  }

  private boolean isBoolBox(Ty t) {
    return false;
  }

  static final class SwitchData {
    Ty selectorType;
    boolean exhaustive;
    boolean hasPattern;
    boolean hasNull;
    boolean isEnum;
    boolean isString;
    boolean isIntLike;
  }

  private Ty standaloneResult(List<Ty> ts, List<ExpressionTree> es) {
    if (ts.isEmpty()) {
      return Ty.tError;
    }
    boolean allNum = true;
    boolean allBool = true;
    for (Ty t : ts) {
      Ty u = a.unboxedOrSelf(t);
      if (!u.isNumeric()) {
        allNum = false;
      }
      if (u.tag != Ty.BOOLEAN) {
        allBool = false;
      }
    }
    if (allBool) {
      return Ty.tBoolean;
    }
    if (allNum) {
      Ty r = a.unboxedOrSelf(ts.get(0));
      boolean same = true;
      for (Ty t : ts) {
        if (a.unboxedOrSelf(t).tag != r.tag) {
          same = false;
        }
      }
      if (same) {
        return r;
      }
      for (Ty t : ts) {
        r = a.binaryPromote(r, a.unboxedOrSelf(t));
      }
      return r;
    }
    List<Ty> boxed = new ArrayList<Ty>();
    for (Ty t : ts) {
      boxed.add(t.isPrim() ? a.tx.box(t) : t);
    }
    return a.tx.lub(boxed);
  }

  private Ty coveredType(PatternTree p) {
    if (p instanceof BindingPatternTree) {
      return a.typeOf(p);
    }
    if (p instanceof DeconstructionPatternTree) {
      DeconstructionPatternTree d = (DeconstructionPatternTree) p;
      Ty rt = a.typeOf(p);
      if (rt == null) {
        return null;
      }
      Ty[] holder = {rt};
      CSym rc = rt.sym;
      rc.members();
      List<? extends PatternTree> nested = d.getNestedPatterns();
      for (int i = 0; i < nested.size() && i < rc.recordComponents.size(); i++) {
        Ty ct = a.tx.fieldType(rc.recordComponents.get(i), rt);
        Ty cv = coveredType(nested.get(i));
        if (cv == null || !coversType(ct, cv)) {
          return Ty.tError;
        }
      }
      return rt;
    }
    return a.st.object;
  }

  private boolean coversType(Ty target, Ty cover) {
    Ty et = a.tx.erasure(target);
    Ty ec = a.tx.erasure(cover);
    if (target.isPrim()) {
      return cover.isPrim() && cover.tag == target.tag || a.tx.isSubtype(a.tx.box(target), ec);
    }
    if (a.tx.isSubtype(et, ec)) {
      return true;
    }
    return covers(target, java.util.Collections.singletonList(cover), new HashSet<String>());
  }

  /** Whether the types cover every value of the selector type, descending into sealed hierarchies. */
  private boolean covers(Ty sel, List<Ty> covering, Set<String> enumSeen) {
    return covers(sel, covering, 0);
  }

  private boolean covers(Ty sel, List<Ty> covering, int depth) {
    if (depth > 12) {
      return false;
    }
    Ty es = a.tx.erasure(sel);
    for (Ty c : covering) {
      if (c == null || c.tag == Ty.ERROR) {
        continue;
      }
      if (a.tx.isSubtype(es, a.tx.erasure(c))) {
        return true;
      }
    }
    if (sel.tag == Ty.TVAR || sel.tag == Ty.INTER) {
      for (Ty b : a.tx.memberSites(sel)) {
        if (covers(b, covering, depth + 1)) {
          return true;
        }
      }
      return false;
    }
    if (es.tag != Ty.CLASS) {
      return false;
    }
    CSym c = es.sym;
    if ((c.flags & Sym.SEALED) != 0 || (c.reflect != null && c.reflect.isSealed())) {
      List<Ty> subs = permitted(c);
      if (subs.isEmpty()) {
        return false;
      }
      for (Ty s : subs) {
        if (!covers(s, covering, depth + 1)) {
          return false;
        }
      }
      return true;
    }
    if (c.isEnum()) {
      return false;
    }
    return false;
  }

  List<Ty> permitted(CSym c) {
    List<Ty> out = new ArrayList<Ty>();
    c.header();
    if (c.reflect != null) {
      try {
        for (Class<?> k : c.reflect.getPermittedSubclasses()) {
          out.add(Ty.cls(a.st.reflected(k)));
        }
      } catch (Throwable t) {
        return out;
      }
      return out;
    }
    if (!c.permitted.isEmpty()) {
      return c.permitted;
    }
    for (CSym s : a.enter.sourceClasses) {
      if (s.cu == c.cu || true) {
        s.header();
        Ty sc = s.superclass();
        boolean direct = sc != null && sc.tag == Ty.CLASS && sc.sym == c;
        for (Ty i : s.interfaces()) {
          if (i.tag == Ty.CLASS && i.sym == c) {
            direct = true;
          }
        }
        if (direct) {
          out.add(Ty.cls(s));
        }
      }
    }
    return out;
  }

  /* ---------- patterns ---------- */

  void pattern(PatternTree p, Env env, Ty scrutinee, List<VSym> out) {
    if (p instanceof BindingPatternTree) {
      VariableTree vt = ((BindingPatternTree) p).getVariable();
      VSym v = new VSym();
      v.name = vt.getName().toString();
      v.flags = Enter.modFlags(vt.getModifiers());
      v.scopeOwner = env.scopeOwner;
      v.isPattern = true;
      v.tree = vt;
      Ty t;
      if (vt.getType() == null) {
        t = scrutinee == null ? Ty.tError : a.tx.upper(scrutinee);
        if (t.tag == Ty.WILD) {
          t = a.st.object;
        }
      } else {
        t = a.enter.resolveType(vt.getType(), env);
        a.types.put(vt.getType(), t);
      }
      v.type = t;
      if (!v.name.equals("_")) {
        checkDup(env, v.name, vt);
        out.add(v);
      }
      a.syms.put(vt, v);
      a.types.put(vt, t);
      a.types.put(p, t);
    } else if (p instanceof DeconstructionPatternTree) {
      DeconstructionPatternTree d = (DeconstructionPatternTree) p;
      Ty rt = a.enter.resolveType(d.getDeconstructor(), env);
      a.types.put(d.getDeconstructor(), rt);
      if (rt.tag != Ty.CLASS || !rt.sym.isRecord()) {
        if (rt.tag != Ty.ERROR) {
          a.err(p, "compiler.err.deconstruction.pattern.only.records", "deconstruction patterns can only be applied to"
              + " records, " + rt + " is not a record class");
        }
        a.types.put(p, Ty.tError);
        for (PatternTree n : d.getNestedPatterns()) {
          pattern(n, env, null, out);
        }
        return;
      }
      CSym rc = rt.sym;
      rc.members();
      if (rt.args.length == 0 && !rc.typeParams().isEmpty()) {
        rt = inferRecordType(rc, scrutinee);
      }
      a.types.put(p, rt);
      List<? extends PatternTree> nested = d.getNestedPatterns();
      if (nested.size() != rc.recordComponents.size()) {
        a.err(p, "compiler.err.incorrect.number.of.nested.patterns", "incorrect number of nested patterns\n"
            + "  required: " + rc.recordComponents.size() + "\n  found: " + nested.size());
      }
      for (int i = 0; i < nested.size(); i++) {
        Ty ct = i < rc.recordComponents.size() ? a.tx.fieldType(rc.recordComponents.get(i), rt) : null;
        pattern(nested.get(i), env, ct, out);
      }
      a.info.put(p, rt);
    } else {
      a.types.put(p, scrutinee == null ? a.st.object : scrutinee);
    }
  }

  private Ty inferRecordType(CSym rc, Ty scrutinee) {
    List<Ty> ivs = new ArrayList<Ty>();
    for (TVSym tv : rc.tparams) {
      ivs.add(Ty.ivar(tv));
    }
    Ty cand = Ty.cls(rc, ivs.toArray(new Ty[0]));
    if (scrutinee != null) {
      Ty up = a.tx.upper(scrutinee);
      if (up.tag == Ty.CLASS && up.args.length > 0) {
        a.tx.isSubtype(cand, up);
      }
    }
    a.call.solve(ivs);
    return TypeOps.resolve(cand);
  }
}
