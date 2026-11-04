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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Attribution: assigns types, symbols and constants to the trees of the compilation units. */
final class Attr {
  static final class FieldRef {
    FSym f;
    CSym implicitOwner;
    Ty site;
    Ty type;
    boolean viaSuper;
  }

  static final class Qual {
    static final int PKG = 0;
    static final int TYPE = 1;
    static final int EXPR = 2;
    int kind;
    Ty type;
    String pkg;
  }

  /** Thrown to postpone the attribution of a nested call until its target type is known. */
  static final class Deferral extends RuntimeException {
    Deferral() {
      super(null, null, false, false);
    }
  }

  final Symtab st;
  final Tx tx;
  final Enter enter;
  final Resolve res;
  final Log log;
  final Map<Tree, Ty> types = new IdentityHashMap<Tree, Ty>();
  final Map<Tree, Object> syms = new IdentityHashMap<Tree, Object>();
  final Map<Tree, Object> consts = new IdentityHashMap<Tree, Object>();
  final Map<Tree, Object> info = new IdentityHashMap<Tree, Object>();
  final List<CSym> attributed = new ArrayList<CSym>();
  final java.util.Set<CSym> attribDone = new java.util.HashSet<CSym>();
  final AttrCall call;
  final AttrStat stat;
  final List<Ty> openVars = new ArrayList<Ty>();
  boolean deferNext;

  Attr(Symtab st, Tx tx, Enter enter, Log log) {
    this.st = st;
    this.tx = tx;
    this.enter = enter;
    this.res = new Resolve(st, tx, enter);
    enter.attrRef = this;
    this.log = log;
    this.call = new AttrCall(this);
    this.stat = new AttrStat(this);
  }

  Ty typeOf(Tree t) {
    Ty r = types.get(t);
    return r == null ? null : TypeOps.resolve(r);
  }

  Object constOf(Tree t) {
    return consts.get(t);
  }

  Sym symOf(Tree t) {
    Object o = syms.get(t);
    return o instanceof Sym ? (Sym) o : null;
  }

  void err(Tree at, String code, String msg) {
    log.error(at, code, msg);
  }

  /* ---------- classes ---------- */

  void attribAll() {
    for (int i = 0; i < enter.sourceClasses.size(); i++) {
      CSym c = enter.sourceClasses.get(i);
      if (c.outerClass == null && !c.local && !c.anon) {
        attribClass(c);
      }
    }
  }

  void attribClass(CSym c) {
    if (!attribDone.add(c)) {
      return;
    }
    attributed.add(c);
    c.members();
    Env cenv = enter.classEnv(c);
    log.cu = c.cu;
    ClassTree t = c.tree;
    checkClass(c, t);
    for (Tree m : t.getMembers()) {
      log.cu = c.cu;
      if (m instanceof VariableTree) {
        VariableTree vt = (VariableTree) m;
        FSym f = (FSym) enter.symOf.get(vt);
        if (f == null) {
          continue;
        }
        if (vt.getInitializer() != null) {
          Env fenv = cenv.dup();
          fenv.isClassEnv = false;
          fenv.staticCtx = f.isStatic();
          fenv.method = null;
          fenv.scopeOwner = c;
          ExpressionTree init = vt.getInitializer();
          int fmark = openVars.size();
          if (init.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) init).getType() == null) {
            attribInitializer((NewArrayTree) init, fenv, f.type);
          } else {
            boolean ec = (f.flags & Sym.ENUM) != 0;
            call.allowEnumNew = ec;
            Ty it;
            try {
              it = expr(init, fenv, f.type);
            } finally {
              call.allowEnumNew = false;
            }
            checkAssignable(init, it, f.type, consts.get(init));
          }
          closeOpen(fmark);
          if (f.isFinal() && !f.constKnown) {
            f.constKnown = true;
            f.constant = foldedConstant(init, f.type);
          }
        }
      } else if (m instanceof MethodTree) {
        MSym ms = (MSym) enter.symOf.get(m);
        if (ms != null) {
          attribMethod(c, ms, (MethodTree) m, cenv);
        }
      } else if (m instanceof BlockTree) {
        Env benv = cenv.dup();
        benv.isClassEnv = false;
        benv.staticCtx = ((BlockTree) m).isStatic();
        benv.method = null;
        benv.scopeOwner = c;
        stat.stat((BlockTree) m, benv);
      }
    }
    for (Tree m : t.getMembers()) {
      if (m instanceof ClassTree) {
        CSym mc = enter.classOf.get(m);
        if (mc != null) {
          attribClass(mc);
        }
      }
    }
    checkImplicitCtor(c, t, cenv);
  }

  private void checkClass(CSym c, ClassTree t) {
    Ty sc = c.superclass();
    if (sc != null && sc.tag == Ty.CLASS && sc.sym.isFinal() && !c.anon && (sc.sym.flags & Sym.ENUM) == 0) {
      err(t.getExtendsClause() != null ? t.getExtendsClause() : t, "compiler.err.cant.inherit.from.final",
          "cannot inherit from final " + sc.sym.fullName());
    }
    if (!c.isAbstract() && !c.isInterface() && !c.isEnum()) {
      MSym missing = findUnimplemented(c);
      if (missing != null) {
        err(t, "compiler.err.does.not.override.abstract", c.fullName() + " is not abstract and does not override"
            + " abstract method " + missing.name + "(" + paramList(missing) + ") in " + missing.cls().fullName());
      }
    }
  }

  static String paramList(MSym m) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < m.params.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(m.params.get(i));
    }
    return sb.toString();
  }

  private MSym findUnimplemented(CSym c) {
    List<MSym> abstracts = new ArrayList<MSym>();
    java.util.Set<CSym> seen = new java.util.LinkedHashSet<CSym>();
    collectAbstract(Ty.cls(c, c.type().args), abstracts, seen);
    for (MSym a : abstracts) {
      List<MSym> cands = res.methodsOf(c.type(), a.name);
      boolean impl = false;
      Tx.MType am = tx.memberType(a, c.type());
      for (MSym m : cands) {
        if (m == a || (m.flags & Sym.ABSTRACT) != 0 && (m.flags & Sym.DEFAULT) == 0 || m.isStatic()
            || m.params.size() != a.params.size()) {
          continue;
        }
        Tx.MType mm = tx.memberType(m, c.type());
        boolean same = true;
        for (int i = 0; i < mm.params.size(); i++) {
          Ty x = tx.erasure(mm.params.get(i));
          Ty y = tx.erasure(am.params.get(i));
          if (!x.desc().equals(y.desc()) && !TypeOps.equal(mm.params.get(i), am.params.get(i))) {
            same = false;
          }
        }
        if (same) {
          impl = true;
          break;
        }
      }
      if (!impl) {
        return a;
      }
    }
    return null;
  }

  private void collectAbstract(Ty t, List<MSym> out, java.util.Set<CSym> seen) {
    if (t.tag != Ty.CLASS || !seen.add(t.sym)) {
      return;
    }
    CSym c = t.sym;
    c.members();
    for (MSym m : c.allMethods) {
      if ((m.flags & Sym.ABSTRACT) != 0 && (m.flags & Sym.DEFAULT) == 0 && !m.isStatic()) {
        String n = m.name;
        if ((n.equals("equals") && m.params.size() == 1) || (n.equals("hashCode") && m.params.isEmpty())
            || (n.equals("toString") && m.params.isEmpty())) {
          continue;
        }
        out.add(m);
      }
    }
    Ty sc = c.superclass();
    if (sc != null) {
      collectAbstract(sc, out, seen);
    }
    for (Ty i : c.interfaces()) {
      collectAbstract(i, out, seen);
    }
  }

  /** Attributes an expression, then checks it against the required type. */
  Ty exprAs(ExpressionTree e, Env env, Ty req) {
    Ty t = expr(e, env, req);
    checkAssignable(e, t, req, consts.get(e));
    return t;
  }

  /** Resolves leftover inference variables since the mark. */
  void closeOpen(int mark) {
    call.solveFrom(mark);
  }

  private static boolean isExplicitCtorCall(StatementTree st) {
    if (!(st instanceof ExpressionStatementTree)) {
      return false;
    }
    ExpressionTree e = ((ExpressionStatementTree) st).getExpression();
    if (!(e instanceof MethodInvocationTree)) {
      return false;
    }
    ExpressionTree sel = ((MethodInvocationTree) e).getMethodSelect();
    String nm = sel instanceof IdentifierTree ? ((IdentifierTree) sel).getName().toString() : "";
    return nm.equals("this") || nm.equals("super")
        || sel instanceof MemberSelectTree && ((MemberSelectTree) sel).getIdentifier().toString().equals("super");
  }

  private void checkImplicitCtor(CSym c, ClassTree t, Env cenv) {
    if (c.isInterface()) {
      return;
    }
    for (MSym m : c.methodsNamed("<init>")) {
      if (m.tree == null && (m.flags & Sym.GENERATED_CTOR) != 0 && !c.isRecord() && !c.isEnum()) {
        Ty sc = c.superclass();
        if (sc != null && sc.tag == Ty.CLASS) {
          call.implicitSuperCall(c, m, t, cenv);
        }
      }
    }
  }

  void attribMethod(CSym c, MSym ms, MethodTree mt, Env cenv) {
    int mmark = openVars.size();
    Env menv = cenv.dup();
    menv.isClassEnv = false;
    menv.method = ms;
    menv.cls = c;
    menv.staticCtx = ms.isStatic();
    menv.scopeOwner = ms;
    menv.returnType = ms.ret;
    for (TVSym tv : ms.tparams) {
      menv.addTvar(tv);
    }
    int i = 0;
    List<? extends VariableTree> ps = mt.getParameters();
    if (ps != null) {
      for (VariableTree p : ps) {
        VSym v = new VSym();
        v.name = p.getName().toString();
        v.type = ms.params.get(i++);
        v.param = true;
        v.flags = Enter.modFlags(p.getModifiers());
        v.scopeOwner = ms;
        v.tree = p;
        syms.put(p, v);
        types.put(p, v.type);
        menv.addVar(v);
      }
    }
    BlockTree body = mt.getBody();
    if (body == null) {
      if ((ms.flags & (Sym.ABSTRACT | Sym.NATIVE)) == 0 && !c.isInterface()) {
        err(mt, "compiler.err.missing.meth.body.or.decl.abstract", "missing method body, or declare abstract");
      }
      return;
    }
    if ((ms.flags & Sym.ABSTRACT) != 0 && !c.isInterface()) {
      err(mt, "compiler.err.abstract.meth.cant.have.body", "abstract methods cannot have a body");
    }
    List<? extends StatementTree> stmts = body.getStatements();
    int start = 0;
    Env restEnv = null;
    if (ms.isCtor()) {
      int callAt = -1;
      for (int ci = 0; ci < stmts.size() && callAt < 0; ci++) {
        if (isExplicitCtorCall(stmts.get(ci))) {
          callAt = ci;
        }
      }
      if (callAt >= 0) {
        Env pre = menv.dup();
        pre.ctorPrologue = true;
        stat.block(stmts.subList(0, callAt), pre);
        Env penv = pre.dup();
        penv.ctorPrologue = true;
        stat.stat(stmts.get(callAt), penv);
        start = callAt + 1;
        restEnv = pre.dup();
        restEnv.ctorPrologue = false;
      } else if (!c.isEnum() && !c.isRecord() && c.superclass() != null) {
        call.implicitSuperCall(c, ms, mt, menv);
      } else if (c.isRecord() && (ms.flags & Sym.COMPACT_CTOR) == 0) {
        // canonical record constructors with a body still call Record()
        call.implicitSuperCall(c, ms, mt, menv);
      }
    }
    Env benv = restEnv != null ? restEnv : menv.dup();
    stat.block(stmts.subList(start, stmts.size()), benv);
    benv = null;
    if (ms.ret.tag != Ty.VOID && !ms.isCtor() && Flow.canComplete(this, body)) {
      err(endMarker(body), "compiler.err.missing.ret.stmt", "missing return statement");
    }
    if (log.speculative == 0) {
      new Checks(this).method(mt, ms);
    }
    closeOpen(mmark);
  }

  Tree endMarker(Tree t) {
    if (!(t instanceof N.Base)) {
      return t;
    }
    N.EmptyStatement m = new N.EmptyStatement();
    int e = ((N.Base) t).end;
    m.start = e - 1;
    m.pos = e - 1;
    m.end = e;
    return m;
  }

  /* ---------- helpers ---------- */

  Object foldedConstant(ExpressionTree init, Ty declared) {
    Object c = consts.get(init);
    if (c == null) {
      return null;
    }
    if (declared.tag == Ty.CLASS) {
      return declared.isString() && c instanceof String ? c : null;
    }
    if (declared.isPrim()) {
      return ConstFold.coerce(c, declared);
    }
    return null;
  }

  Object constantOf(FSym f) {
    if (f.constKnown) {
      return f.constant;
    }
    if (f.tree == null) {
      return st.constantOf(f);
    }
    if (!f.isFinal() || f.tree.getInitializer() == null) {
      f.constKnown = true;
      return null;
    }
    if (!(f.type.isPrim() || f.type.isString())) {
      f.constKnown = true;
      return null;
    }
    if (f.constBusy) {
      return null;
    }
    f.constBusy = true;
    try {
      CSym c = f.cls();
      Env cenv = enter.classEnv(c);
      Env fenv = cenv.dup();
      fenv.isClassEnv = false;
      fenv.staticCtx = f.isStatic();
      fenv.scopeOwner = c;
      CompilationUnitTree saved = log.cu;
      log.speculative++;
      try {
        log.cu = c.cu;
        int cmark = openVars.size();
        Ty it = expr(f.tree.getInitializer(), fenv, f.type);
        f.constant = foldedConstant(f.tree.getInitializer(), f.type);
        closeOpen(cmark);
      } finally {
        log.speculative--;
        log.cu = saved;
      }
      f.constKnown = true;
      return f.constant;
    } finally {
      f.constBusy = false;
    }
  }

  boolean isDefinitelyNotSameKind(Ty a, Ty b) {
    return false;
  }

  void checkAssignable(Tree pos, Ty found, Ty req, Object constVal) {
    if (found == null || req == null || found.tag == Ty.ERROR || req.tag == Ty.ERROR || req.tag == Ty.NONE) {
      return;
    }
    found = TypeOps.resolve(found);
    if (found.tag == Ty.VOID) {
      err(pos, "compiler.err.prob.found.req", "incompatible types: void cannot be converted to " + req);
      return;
    }
    if (tx.isLooseConvertible(found, req)) {
      return;
    }
    if (constVal != null && found.isIntegral() && found.tag <= Ty.INT && found.tag != Ty.LONG) {
      Ty target = req.isPrim() ? req : tx.unboxed(req);
      if (target != null && target.tag <= Ty.CHAR && target.tag != Ty.BOOLEAN && fitsConstant(constVal, target)) {
        if (req.isPrim() || tx.isLooseConvertible(target, req) || req.tag == Ty.CLASS && tx.unboxed(req) == target) {
          return;
        }
      }
    }
    if (found.isNumeric() && req.isNumeric()) {
      err(pos, "compiler.err.prob.found.req", "incompatible types: possible lossy conversion from " + found + " to "
          + req);
      return;
    }
    err(pos, "compiler.err.prob.found.req", "incompatible types: " + found + " cannot be converted to " + req);
  }

  static boolean fitsConstant(Object v, Ty target) {
    long x;
    if (v instanceof Character) {
      x = ((Character) v).charValue();
    } else if (v instanceof Number && !(v instanceof Float) && !(v instanceof Double)) {
      x = ((Number) v).longValue();
    } else {
      return false;
    }
    switch (target.tag) {
      case Ty.BYTE:
        return x >= Byte.MIN_VALUE && x <= Byte.MAX_VALUE;
      case Ty.SHORT:
        return x >= Short.MIN_VALUE && x <= Short.MAX_VALUE;
      case Ty.CHAR:
        return x >= 0 && x <= 0xffff;
      default:
        return false;
    }
  }

  Ty binaryPromote(Ty a, Ty b) {
    if (a.tag == Ty.DOUBLE || b.tag == Ty.DOUBLE) {
      return Ty.tDouble;
    }
    if (a.tag == Ty.FLOAT || b.tag == Ty.FLOAT) {
      return Ty.tFloat;
    }
    if (a.tag == Ty.LONG || b.tag == Ty.LONG) {
      return Ty.tLong;
    }
    return Ty.tInt;
  }

  Ty unaryPromote(Ty a) {
    return a.tag < Ty.INT ? Ty.tInt : a;
  }

  Ty unboxedOrSelf(Ty t) {
    if (t.isPrim()) {
      return t;
    }
    Ty u = tx.unboxed(t);
    return u == null ? t : u;
  }

  /** Static type of an expression's value after the usual capture-free projections. */
  Ty valueType(Ty t) {
    if (t.tag == Ty.WILD) {
      return tx.upper(t);
    }
    return t;
  }

  /* ---------- expressions ---------- */

  Ty expr(ExpressionTree e, Env env, Ty pt) {
    Ty t = expr0(e, env, pt);
    if (t == null) {
      t = Ty.tError;
    }
    t = TypeOps.resolve(t);
    types.put(e, t);
    return t;
  }

  private Ty expr0(ExpressionTree e, Env env, Ty pt) {
    switch (e.getKind()) {
      case INT_LITERAL:
        return lit(e, Ty.tInt);
      case LONG_LITERAL:
        return lit(e, Ty.tLong);
      case FLOAT_LITERAL:
        return lit(e, Ty.tFloat);
      case DOUBLE_LITERAL:
        return lit(e, Ty.tDouble);
      case BOOLEAN_LITERAL:
        return lit(e, Ty.tBoolean);
      case CHAR_LITERAL:
        return lit(e, Ty.tChar);
      case STRING_LITERAL:
        return lit(e, st.string);
      case NULL_LITERAL:
        return Ty.tNull;
      case IDENTIFIER:
        return ident((IdentifierTree) e, env);
      case MEMBER_SELECT:
        return select((MemberSelectTree) e, env);
      case PARENTHESIZED: {
        ExpressionTree in = ((ParenthesizedTree) e).getExpression();
        Ty t = expr(in, env, pt);
        Object c = consts.get(in);
        if (c != null) {
          consts.put(e, c);
        }
        return t;
      }
      case METHOD_INVOCATION:
        return call.invoke((MethodInvocationTree) e, env, pt);
      case NEW_CLASS:
        return call.newClass((NewClassTree) e, env, pt);
      case NEW_ARRAY:
        return newArray((NewArrayTree) e, env, pt);
      case ARRAY_ACCESS: {
        ArrayAccessTree aa = (ArrayAccessTree) e;
        Ty at = expr(aa.getExpression(), env, null);
        Ty it = expr(aa.getIndex(), env, null);
        Ty iu = unboxedOrSelf(it);
        if (!(iu.isPrim() && iu.tag <= Ty.INT && iu.tag != Ty.BOOLEAN) && it.tag != Ty.ERROR) {
          checkAssignable(aa.getIndex(), it, Ty.tInt, consts.get(aa.getIndex()));
        }
        at = valueType(at);
        if (at.tag == Ty.ARRAY) {
          return at.elem;
        }
        if (at.tag != Ty.ERROR) {
          err(e, "compiler.err.array.req.but.found", "array required, but " + at + " found");
        }
        return Ty.tError;
      }
      case ASSIGNMENT:
        return assign((AssignmentTree) e, env);
      case TYPE_CAST:
        return cast((TypeCastTree) e, env);
      case INSTANCE_OF:
        return instanceOf((InstanceOfTree) e, env);
      case CONDITIONAL_EXPRESSION:
        return conditional((ConditionalExpressionTree) e, env, pt);
      case LAMBDA_EXPRESSION:
        return call.lambda((LambdaExpressionTree) e, env, pt);
      case MEMBER_REFERENCE:
        return call.memberRef((MemberReferenceTree) e, env, pt);
      case SWITCH_EXPRESSION:
        return stat.switchExpr((SwitchExpressionTree) e, env, pt);
      case POSTFIX_INCREMENT:
      case POSTFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case PREFIX_DECREMENT:
      case UNARY_PLUS:
      case UNARY_MINUS:
      case BITWISE_COMPLEMENT:
      case LOGICAL_COMPLEMENT:
        return unary((UnaryTree) e, env);
      case ERRONEOUS:
        return Ty.tError;
      default:
        break;
    }
    if (e instanceof BinaryTree) {
      return binary((BinaryTree) e, env);
    }
    if (e instanceof CompoundAssignmentTree) {
      return compound((CompoundAssignmentTree) e, env);
    }
    if (e instanceof AnnotatedTypeTree) {
      return expr(((AnnotatedTypeTree) e).getUnderlyingType(), env, pt);
    }
    err(e, "compiler.err.illegal.start.of.expr", "illegal start of expression");
    return Ty.tError;
  }

  private Ty lit(ExpressionTree e, Ty t) {
    Object v = ((LiteralTree) e).getValue();
    if (v != null) {
      consts.put(e, v);
    }
    return t;
  }

  /* ---------- names ---------- */

  Object findVar(Env env, String n) {
    boolean isStatic = env.staticCtx;
    for (Env e = env; e != null; e = e.outer) {
      if (e.vars != null) {
        VSym v = e.vars.get(n);
        if (v != null) {
          return v;
        }
      }
      if (e.isClassEnv) {
        FSym f = res.findField(e.cls, n, env);
        if (f != null) {
          FieldRef r = new FieldRef();
          r.f = f;
          r.implicitOwner = e.cls;
          r.site = e.cls.type();
          r.type = tx.fieldType(f, r.site);
          if (!f.isStatic() && isStatic) {
            r.viaSuper = true;
          }
          return r;
        }
        CSym c = e.cls;
        isStatic = c.isStatic() || c.isInterface() || c.isEnum() || c.isRecord()
            || ((c.local || c.anon) && e.outer != null && e.outer.staticCtx);
      }
    }
    Env.CuEnv ce = env.cu;
    enter.resolveImports(ce);
    java.util.List<CSym> sl = ce.staticSingle.get(n);
    if (sl != null) {
      for (CSym c : sl) {
        FSym f = res.findField(c, n);
        if (f != null && f.isStatic()) {
          return staticRef(f, c);
        }
      }
    }
    for (CSym c : ce.staticOnDemand) {
      FSym f = res.findField(c, n);
      if (f != null && f.isStatic()) {
        return staticRef(f, c);
      }
    }
    return null;
  }

  private FieldRef staticRef(FSym f, CSym c) {
    FieldRef r = new FieldRef();
    r.f = f;
    r.implicitOwner = null;
    r.site = c.erasedType();
    r.type = f.type;
    return r;
  }

  private Ty ident(IdentifierTree id, Env env) {
    String n = id.getName().toString();
    if (n.equals("this")) {
      if (env.cls == null || env.staticCtx) {
        err(id, "compiler.err.non-static.cant.be.ref", "non-static variable this cannot be referenced from a static"
            + " context");
        return Ty.tError;
      }
      syms.put(id, env.cls);
      return env.cls.type();
    }
    if (n.equals("super")) {
      Ty sc = env.cls == null ? null : env.cls.superclass();
      syms.put(id, env.cls);
      return sc == null ? st.object : sc;
    }
    Object v = findVar(env, n);
    if (v == null) {
      err(id, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   variable " + n + "\n  location: "
          + enter.locationOf(env));
      return Ty.tError;
    }
    return useVar(id, v, env, n);
  }

  Ty useVar(Tree id, Object v, Env env, String n) {
    if (v instanceof VSym) {
      VSym vs = (VSym) v;
      syms.put(id, vs);
      if (vs.constant != null) {
        consts.put(id, vs.constant);
      }
      if (vs.scopeOwner != env.scopeOwner) {
        vs.captured = true;
      }
      return vs.type;
    }
    FieldRef r = (FieldRef) v;
    FSym f = r.f;
    syms.put(id, f);
    info.put(id, r);
    if (!f.isStatic() && (env.staticCtx && r.implicitOwner == env.cls || r.viaSuper)) {
      err(id, "compiler.err.non-static.cant.be.ref", "non-static variable " + n
          + " cannot be referenced from a static context");
    }
    if (env.ctorPrologue && !f.isStatic() && r.implicitOwner == env.cls) {
      err(id, "compiler.err.cant.ref.before.ctor.called", "cannot reference " + n
          + " before supertype constructor has been called");
    }
    Object c = constantOf(f);
    if (c != null) {
      consts.put(id, c);
    }
    return r.type;
  }

  Qual qual(Tree t, Env env) {
    Qual q = new Qual();
    if (t.getKind() == Tree.Kind.IDENTIFIER) {
      String n = ((IdentifierTree) t).getName().toString();
      if (!n.equals("this") && !n.equals("super")) {
        Object v = findVar(env, n);
        if (v != null) {
          q.kind = Qual.EXPR;
          q.type = expr((ExpressionTree) t, env, null);
          return q;
        }
        Sym s = enter.findType(env, n);
        if (s instanceof CSym) {
          q.kind = Qual.TYPE;
          q.type = Ty.cls((CSym) s);
          types.put(t, q.type);
          syms.put(t, s);
          return q;
        }
        if (s instanceof TVSym) {
          q.kind = Qual.TYPE;
          q.type = ((TVSym) s).type;
          types.put(t, q.type);
          return q;
        }
        q.kind = Qual.PKG;
        q.pkg = n;
        return q;
      }
    } else if (t.getKind() == Tree.Kind.MEMBER_SELECT) {
      MemberSelectTree ms = (MemberSelectTree) t;
      String n = ms.getIdentifier().toString();
      Qual b = qual(ms.getExpression(), env);
      if (b.kind == Qual.PKG) {
        CSym c = st.lookup(b.pkg.replace('.', '/') + "/" + n);
        if (c != null) {
          q.kind = Qual.TYPE;
          q.type = Ty.cls(c);
          types.put(t, q.type);
          syms.put(t, c);
          return q;
        }
        q.kind = Qual.PKG;
        q.pkg = b.pkg + "." + n;
        return q;
      }
      if (b.kind == Qual.TYPE && b.type.tag == Ty.CLASS) {
        FSym f = res.findField(b.type.sym, n);
        if (f == null) {
          CSym m = enter.memberClassOf(b.type.sym, n);
          if (m != null) {
            q.kind = Qual.TYPE;
            q.type = Ty.cls(m);
            types.put(t, q.type);
            syms.put(t, m);
            return q;
          }
        }
      }
      q.kind = Qual.EXPR;
      q.type = selectOn(ms, b, env);
      types.put(t, q.type);
      return q;
    } else if (t.getKind() == Tree.Kind.PARAMETERIZED_TYPE || t.getKind() == Tree.Kind.ARRAY_TYPE
        || t.getKind() == Tree.Kind.PRIMITIVE_TYPE || t.getKind() == Tree.Kind.ANNOTATED_TYPE) {
      q.kind = Qual.TYPE;
      q.type = enter.resolveType(t, env);
      types.put(t, q.type);
      return q;
    }
    q.kind = Qual.EXPR;
    q.type = expr((ExpressionTree) t, env, null);
    return q;
  }

  private Ty select(MemberSelectTree ms, Env env) {
    Qual q;
    if (ms.getIdentifier().contentEquals("class")) {
      q = new Qual();
      q.kind = Qual.TYPE;
      q.type = enter.resolveType(ms.getExpression(), env);
      types.put(ms.getExpression(), q.type);
      if (q.type.tag == Ty.CLASS) {
        syms.put(ms.getExpression(), q.type.sym);
      }
    } else {
      q = qual(ms.getExpression(), env);
    }
    return selectOn(ms, q, env);
  }

  Ty selectOn(MemberSelectTree ms, Qual q, Env env) {
    String n = ms.getIdentifier().toString();
    if (q.kind == Qual.PKG) {
      err(ms, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class " + n
          + "\n  location: package " + q.pkg);
      return Ty.tError;
    }
    Ty qt = q.type;
    if (n.equals("class") && q.kind == Qual.TYPE) {
      Ty arg = qt.isPrim() ? tx.box(qt) : qt.tag == Ty.VOID ? Ty.cls(st.lookup("java/lang/Void")) : TypeOps.erasure(qt);
      return Ty.cls(st.lookup("java/lang/Class"), arg);
    }
    if (n.equals("this") && q.kind == Qual.TYPE) {
      syms.put(ms, qt.sym);
      return qt.sym.type();
    }
    if (n.equals("super") && q.kind == Qual.TYPE) {
      syms.put(ms, qt.sym);
      Ty sc = qt.sym.superclass();
      return sc == null ? st.object : sc;
    }
    if (qt.tag == Ty.ERROR) {
      return Ty.tError;
    }
    qt = valueType(qt);
    if (qt.tag == Ty.ARRAY && n.equals("length")) {
      syms.put(ms, ArrayLength.INSTANCE);
      return Ty.tInt;
    }
    if (qt.isPrim()) {
      err(ms, "compiler.err.cant.deref", qt + " cannot be dereferenced");
      return Ty.tError;
    }
    FSym f = res.findFieldIn(qt, n, env);
    if (f == null) {
      err(ms, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   variable " + n
          + "\n  location: " + (q.kind == Qual.TYPE ? "class " + qt : "variable " + ms.getExpression() + " of type "
              + qt));
      return Ty.tError;
    }
    if (!res.accessible(f, f.cls(), env)) {
      err(ms, "compiler.err.report.access", n + " has private access in " + f.cls().fullName());
    }
    if (q.kind == Qual.TYPE && !f.isStatic()) {
      err(ms, "compiler.err.non-static.cant.be.ref", "non-static variable " + n
          + " cannot be referenced from a static context");
    }
    FieldRef r = new FieldRef();
    r.f = f;
    r.site = qt;
    r.type = tx.fieldType(f, qt);
    syms.put(ms, f);
    info.put(ms, r);
    Object c = constantOf(f);
    if (c != null) {
      consts.put(ms, c);
    }
    return r.type;
  }

  /** Marker symbol for array length. */
  static final class ArrayLength extends Sym {
    static final ArrayLength INSTANCE = new ArrayLength();

    private ArrayLength() {
      name = "length";
    }

    public javax.lang.model.element.ElementKind getKind() {
      return javax.lang.model.element.ElementKind.FIELD;
    }

    public <R, P> R accept(javax.lang.model.element.ElementVisitor<R, P> v, P p) {
      return v.visitUnknown(this, p);
    }
  }

  /* ---------- operators ---------- */

  private Ty assign(AssignmentTree a, Env env) {
    Ty lt = lhs(a.getVariable(), env);
    ExpressionTree rhs = a.getExpression();
    Ty rt;
    if (rhs.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) rhs).getType() == null) {
      rt = attribInitializer((NewArrayTree) rhs, env, lt);
    } else {
      rt = expr(rhs, env, lt);
      checkAssignable(rhs, rt, lt, consts.get(rhs));
    }
    return lt;
  }

  /** Attributes the target of an assignment. */
  Ty lhs(ExpressionTree v, Env env) {
    Ty t = expr(v, env, null);
    ExpressionTree x = v;
    while (x instanceof ParenthesizedTree) {
      x = ((ParenthesizedTree) x).getExpression();
    }
    Object s = syms.get(x);
    if (s instanceof VSym) {
      VSym vs = (VSym) s;
      if ((vs.flags & Sym.FINAL) != 0 && (vs.param || vs.tree != null && vs.tree.getInitializer() != null)) {
        err(v, "compiler.err.cant.assign.val.to.var", "cannot assign a value to final variable " + vs.name);
      }
      vs.assignedAfterInit = true;
      vs.constant = null;
      if (vs.scopeOwner != env.scopeOwner) {
        err(v, "compiler.err.cant.ref.non.effectively.final.var", "local variables referenced from "
            + (env.lambda != null ? "a lambda expression" : "an inner class") + " must be final or effectively final");
      }
    } else if (s instanceof FSym) {
      FSym f = (FSym) s;
      if (f.isFinal() && !(env.method != null && f.cls() == env.cls && (env.method.isCtor() || false)
          || env.method == null && env.cls == f.cls())) {
        err(v, "compiler.err.cant.assign.val.to.var", "cannot assign a value to final variable " + f.name);
      }
    } else if (s instanceof ArrayLength) {
      err(v, "compiler.err.cant.assign.val.to.var", "cannot assign a value to final variable length");
    } else if (x.getKind() != Tree.Kind.ARRAY_ACCESS && t.tag != Ty.ERROR) {
      err(v, "compiler.err.unexpected.type", "unexpected type\n  required: variable\n  found:    value");
    }
    return t;
  }

  private Ty compound(CompoundAssignmentTree c, Env env) {
    Ty lt = lhs(c.getVariable(), env);
    Ty rt = expr(c.getExpression(), env, null);
    Tree.Kind op = baseOp(c.getKind());
    if (lt.tag == Ty.ERROR || rt.tag == Ty.ERROR) {
      return lt;
    }
    if (op == Tree.Kind.PLUS && lt.isString()) {
      if (rt.tag == Ty.VOID) {
        err(c, "compiler.err.void.not.allowed.here", "'void' type not allowed here");
      }
      info.put(c, st.string);
      return lt;
    }
    Ty l = unboxedOrSelf(lt);
    Ty r = unboxedOrSelf(rt);
    Ty opType;
    if (l.tag == Ty.BOOLEAN && r.tag == Ty.BOOLEAN && (op == Tree.Kind.AND || op == Tree.Kind.OR
        || op == Tree.Kind.XOR)) {
      opType = Ty.tBoolean;
    } else if (l.isNumeric() && r.isNumeric()) {
      if (op == Tree.Kind.LEFT_SHIFT || op == Tree.Kind.RIGHT_SHIFT || op == Tree.Kind.UNSIGNED_RIGHT_SHIFT) {
        opType = unaryPromote(l);
      } else {
        opType = binaryPromote(l, r);
      }
    } else {
      err(c, "compiler.err.operator.cant.be.applied.1", "bad operand types for binary operator '" + opSymbol(op)
          + "'\n  first type:  " + lt + "\n  second type: " + rt);
      return lt;
    }
    info.put(c, opType);
    return lt;
  }

  static Tree.Kind baseOp(Tree.Kind k) {
    switch (k) {
      case MULTIPLY_ASSIGNMENT:
        return Tree.Kind.MULTIPLY;
      case DIVIDE_ASSIGNMENT:
        return Tree.Kind.DIVIDE;
      case REMAINDER_ASSIGNMENT:
        return Tree.Kind.REMAINDER;
      case PLUS_ASSIGNMENT:
        return Tree.Kind.PLUS;
      case MINUS_ASSIGNMENT:
        return Tree.Kind.MINUS;
      case LEFT_SHIFT_ASSIGNMENT:
        return Tree.Kind.LEFT_SHIFT;
      case RIGHT_SHIFT_ASSIGNMENT:
        return Tree.Kind.RIGHT_SHIFT;
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
        return Tree.Kind.UNSIGNED_RIGHT_SHIFT;
      case AND_ASSIGNMENT:
        return Tree.Kind.AND;
      case XOR_ASSIGNMENT:
        return Tree.Kind.XOR;
      case OR_ASSIGNMENT:
        return Tree.Kind.OR;
      default:
        return k;
    }
  }

  static String opSymbol(Tree.Kind k) {
    switch (k) {
      case MULTIPLY:
        return "*";
      case DIVIDE:
        return "/";
      case REMAINDER:
        return "%";
      case PLUS:
        return "+";
      case MINUS:
        return "-";
      case LEFT_SHIFT:
        return "<<";
      case RIGHT_SHIFT:
        return ">>";
      case UNSIGNED_RIGHT_SHIFT:
        return ">>>";
      case LESS_THAN:
        return "<";
      case GREATER_THAN:
        return ">";
      case LESS_THAN_EQUAL:
        return "<=";
      case GREATER_THAN_EQUAL:
        return ">=";
      case EQUAL_TO:
        return "==";
      case NOT_EQUAL_TO:
        return "!=";
      case AND:
        return "&";
      case XOR:
        return "^";
      case OR:
        return "|";
      case CONDITIONAL_AND:
        return "&&";
      case CONDITIONAL_OR:
        return "||";
      case POSTFIX_INCREMENT:
      case PREFIX_INCREMENT:
        return "++";
      case POSTFIX_DECREMENT:
      case PREFIX_DECREMENT:
        return "--";
      case LOGICAL_COMPLEMENT:
        return "!";
      case BITWISE_COMPLEMENT:
        return "~";
      case UNARY_MINUS:
        return "-";
      case UNARY_PLUS:
        return "+";
      default:
        return k.toString();
    }
  }

  private Ty unary(UnaryTree u, Env env) {
    Tree.Kind k = u.getKind();
    boolean incDec = k == Tree.Kind.POSTFIX_INCREMENT || k == Tree.Kind.POSTFIX_DECREMENT
        || k == Tree.Kind.PREFIX_INCREMENT || k == Tree.Kind.PREFIX_DECREMENT;
    Ty t = incDec ? lhs(u.getExpression(), env) : expr(u.getExpression(), env, null);
    if (t.tag == Ty.ERROR) {
      return t;
    }
    Ty p = unboxedOrSelf(t);
    if (incDec) {
      if (!p.isNumeric()) {
        err(u, "compiler.err.operator.cant.be.applied", "bad operand type " + t + " for unary operator '"
            + opSymbol(k) + "'");
        return Ty.tError;
      }
      return t;
    }
    if (k == Tree.Kind.LOGICAL_COMPLEMENT) {
      if (p.tag != Ty.BOOLEAN) {
        err(u, "compiler.err.operator.cant.be.applied", "bad operand type " + t + " for unary operator '!'");
        return Ty.tError;
      }
      Object c = ConstFold.unary(k, consts.get(u.getExpression()), Ty.tBoolean);
      if (c != null) {
        consts.put(u, c);
      }
      return Ty.tBoolean;
    }
    if (!p.isNumeric() || (k == Tree.Kind.BITWISE_COMPLEMENT && !p.isIntegral())) {
      err(u, "compiler.err.operator.cant.be.applied", "bad operand type " + t + " for unary operator '"
          + opSymbol(k) + "'");
      return Ty.tError;
    }
    Ty rt = unaryPromote(p);
    Object c = ConstFold.unary(k, consts.get(u.getExpression()), rt);
    if (c != null) {
      consts.put(u, c);
    }
    return rt;
  }

  private Ty binary(BinaryTree b, Env env) {
    Tree.Kind k = b.getKind();
    ExpressionTree le = b.getLeftOperand();
    ExpressionTree re = b.getRightOperand();
    Ty lt = expr(le, env, null);
    Env renv = env;
    if (k == Tree.Kind.CONDITIONAL_AND || k == Tree.Kind.CONDITIONAL_OR) {
      List<VSym> bs = bindings(le, k == Tree.Kind.CONDITIONAL_AND);
      if (!bs.isEmpty()) {
        renv = env.dup();
        for (VSym v : bs) {
          renv.addVar(v);
        }
      }
    }
    Ty rt = expr(re, renv, null);
    if (lt.tag == Ty.ERROR || rt.tag == Ty.ERROR) {
      return k == Tree.Kind.PLUS && (lt.isString() || rt.isString()) ? st.string : isComparison(k) ? Ty.tBoolean
          : Ty.tError;
    }
    Object lc = consts.get(le);
    Object rc = consts.get(re);
    if (k == Tree.Kind.PLUS && (lt.isString() || rt.isString())) {
      if (lt.tag == Ty.VOID || rt.tag == Ty.VOID) {
        err(b, "compiler.err.void.not.allowed.here", "'void' type not allowed here");
      }
      if (lc != null && rc != null) {
        consts.put(b, ConstFold.str(lc) + ConstFold.str(rc));
      }
      return st.string;
    }
    Ty l = unboxedOrSelf(lt);
    Ty r = unboxedOrSelf(rt);
    Ty result;
    Ty operand;
    switch (k) {
      case MULTIPLY:
      case DIVIDE:
      case REMAINDER:
      case PLUS:
      case MINUS:
        if (!l.isNumeric() || !r.isNumeric()) {
          return badBinary(b, k, lt, rt);
        }
        operand = binaryPromote(l, r);
        result = operand;
        break;
      case LEFT_SHIFT:
      case RIGHT_SHIFT:
      case UNSIGNED_RIGHT_SHIFT:
        if (!l.isIntegral() || !r.isIntegral()) {
          return badBinary(b, k, lt, rt);
        }
        operand = unaryPromote(l);
        result = operand;
        break;
      case LESS_THAN:
      case GREATER_THAN:
      case LESS_THAN_EQUAL:
      case GREATER_THAN_EQUAL:
        if (!l.isNumeric() || !r.isNumeric()) {
          return badBinary(b, k, lt, rt);
        }
        operand = binaryPromote(l, r);
        result = Ty.tBoolean;
        break;
      case EQUAL_TO:
      case NOT_EQUAL_TO: {
        if (l.isNumeric() && r.isNumeric() && (lt.isPrim() || rt.isPrim())) {
          operand = binaryPromote(l, r);
        } else if (l.tag == Ty.BOOLEAN && r.tag == Ty.BOOLEAN && (lt.isPrim() || rt.isPrim())) {
          operand = Ty.tBoolean;
        } else if (!lt.isPrim() && !rt.isPrim() && lt.tag != Ty.VOID && rt.tag != Ty.VOID) {
          if (!tx.isCastable(lt, rt) && !tx.isCastable(rt, lt)) {
            err(b, "compiler.err.incomparable.types", "incomparable types: " + lt + " and " + rt);
          }
          operand = st.object;
        } else if (lt.isPrim() && !rt.isPrim() || rt.isPrim() && !lt.isPrim()) {
          return badBinary(b, k, lt, rt);
        } else {
          return badBinary(b, k, lt, rt);
        }
        result = Ty.tBoolean;
        break;
      }
      case AND:
      case OR:
      case XOR:
        if (l.tag == Ty.BOOLEAN && r.tag == Ty.BOOLEAN) {
          operand = Ty.tBoolean;
        } else if (l.isIntegral() && r.isIntegral()) {
          operand = binaryPromote(l, r);
        } else {
          return badBinary(b, k, lt, rt);
        }
        result = operand;
        break;
      case CONDITIONAL_AND:
      case CONDITIONAL_OR:
        if (l.tag != Ty.BOOLEAN || r.tag != Ty.BOOLEAN) {
          return badBinary(b, k, lt, rt);
        }
        operand = Ty.tBoolean;
        result = Ty.tBoolean;
        break;
      default:
        return badBinary(b, k, lt, rt);
    }
    info.put(b, operand);
    if (lc != null && rc != null && (operand.isPrim() || operand.isObject() && lc instanceof String)) {
      Object a = operand.isPrim() ? ConstFold.coerce(lc, k == Tree.Kind.LEFT_SHIFT
          || k == Tree.Kind.RIGHT_SHIFT || k == Tree.Kind.UNSIGNED_RIGHT_SHIFT ? operand : operand) : lc;
      Object bb = operand.isPrim() ? (k == Tree.Kind.LEFT_SHIFT || k == Tree.Kind.RIGHT_SHIFT
          || k == Tree.Kind.UNSIGNED_RIGHT_SHIFT ? rc : ConstFold.coerce(rc, operand)) : rc;
      Object folded = a == null || bb == null ? null : ConstFold.binary(k, a, bb, operand);
      if (folded != null) {
        consts.put(b, folded);
      }
    }
    return result;
  }

  static boolean isComparison(Tree.Kind k) {
    return k == Tree.Kind.LESS_THAN || k == Tree.Kind.GREATER_THAN || k == Tree.Kind.LESS_THAN_EQUAL
        || k == Tree.Kind.GREATER_THAN_EQUAL || k == Tree.Kind.EQUAL_TO || k == Tree.Kind.NOT_EQUAL_TO;
  }

  private Ty badBinary(BinaryTree b, Tree.Kind k, Ty lt, Ty rt) {
    err(b, "compiler.err.operator.cant.be.applied.1", "bad operand types for binary operator '" + opSymbol(k)
        + "'\n  first type:  " + lt + "\n  second type: " + rt);
    return isComparison(k) ? Ty.tBoolean : Ty.tError;
  }

  /* ---------- binding variables ---------- */

  /** Pattern variables introduced when the expression is true (or false). */
  List<VSym> bindings(ExpressionTree e, boolean whenTrue) {
    List<VSym> out = new ArrayList<VSym>();
    collectBindings(e, whenTrue, out);
    return out;
  }

  private void collectBindings(ExpressionTree e, boolean whenTrue, List<VSym> out) {
    switch (e.getKind()) {
      case PARENTHESIZED:
        collectBindings(((ParenthesizedTree) e).getExpression(), whenTrue, out);
        break;
      case LOGICAL_COMPLEMENT:
        collectBindings(((UnaryTree) e).getExpression(), !whenTrue, out);
        break;
      case CONDITIONAL_AND:
        if (whenTrue) {
          collectBindings(((BinaryTree) e).getLeftOperand(), true, out);
          collectBindings(((BinaryTree) e).getRightOperand(), true, out);
        }
        break;
      case CONDITIONAL_OR:
        if (!whenTrue) {
          collectBindings(((BinaryTree) e).getLeftOperand(), false, out);
          collectBindings(((BinaryTree) e).getRightOperand(), false, out);
        }
        break;
      case INSTANCE_OF: {
        if (whenTrue) {
          Object o = info.get(e);
          if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<VSym> l = (List<VSym>) o;
            out.addAll(l);
          }
        }
        break;
      }
      default:
        break;
    }
  }

  private Ty instanceOf(InstanceOfTree io, Env env) {
    Ty et = expr(io.getExpression(), env, null);
    Tree pat = io.getPattern();
    List<VSym> bs = new ArrayList<VSym>();
    if (pat != null) {
      stat.pattern((PatternTree) pat, env, et, bs);
      info.put(io, bs);
      Ty pt = typeOf(pat) != null ? typeOf(pat) : Ty.tError;
      if (et.isPrim()) {
        err(io.getExpression(), "compiler.err.unexpected.type", "unexpected type\n  required: reference\n  found:    "
            + et);
      } else if (!tx.isCastable(et, pt)) {
        err(io, "compiler.err.prob.found.req", "incompatible types: " + et + " cannot be converted to " + pt);
      }
    } else {
      Ty tt = enter.resolveType(io.getType(), env);
      types.put(io.getType(), tt);
      if (et.isPrim()) {
        err(io.getExpression(), "compiler.err.unexpected.type", "unexpected type\n  required: reference\n  found:    "
            + et);
      } else if (!tx.isCastable(et, tt)) {
        err(io, "compiler.err.prob.found.req", "incompatible types: " + et + " cannot be converted to " + tt);
      }
    }
    return Ty.tBoolean;
  }

  private Ty cast(TypeCastTree c, Env env) {
    Ty target = enter.resolveType(c.getType(), env);
    types.put(c.getType(), target);
    ExpressionTree inner = c.getExpression();
    Tree.Kind ik = unparen(inner).getKind();
    Ty et;
    if (ik == Tree.Kind.LAMBDA_EXPRESSION || ik == Tree.Kind.MEMBER_REFERENCE) {
      et = expr(inner, env, target);
    } else {
      et = expr(inner, env, null);
    }
    if (et.tag != Ty.ERROR && target.tag != Ty.ERROR && !tx.isCastable(et, target)) {
      err(c, "compiler.err.prob.found.req", "incompatible types: " + et + " cannot be converted to " + target);
    }
    Object pci = info.get(unparen(inner));
    if (pci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) pci).polySig
        && ((AttrCall.CallInfo) pci).ret == st.object) {
      ((AttrCall.CallInfo) pci).sigRet = target;
      types.put(unparen(inner), target);
    }
    Object cv = consts.get(inner);
    if (cv != null && (target.isPrim() || target.isString())) {
      Object r = ConstFold.coerce(cv, target);
      if (r != null) {
        consts.put(c, r);
      }
    }
    return target;
  }

  static ExpressionTree unparen(ExpressionTree e) {
    while (e instanceof ParenthesizedTree) {
      e = ((ParenthesizedTree) e).getExpression();
    }
    return e;
  }

  private static boolean isCallKind(ExpressionTree e) {
    e = unparen(e);
    return e.getKind() == Tree.Kind.METHOD_INVOCATION || e.getKind() == Tree.Kind.NEW_CLASS
        || e.getKind() == Tree.Kind.CONDITIONAL_EXPRESSION;
  }

  private Ty conditional(ConditionalExpressionTree c, Env env, Ty pt) {
    boolean cd = deferNext;
    deferNext = false;
    ExpressionTree cond = c.getCondition();
    Ty ct = expr(cond, env, null);
    checkAssignable(cond, ct, Ty.tBoolean, null);
    Env tenv = env;
    List<VSym> tb = bindings(cond, true);
    if (!tb.isEmpty()) {
      tenv = env.dup();
      for (VSym v : tb) {
        tenv.addVar(v);
      }
    }
    Env fenv = env;
    List<VSym> fb = bindings(cond, false);
    if (!fb.isEmpty()) {
      fenv = env.dup();
      for (VSym v : fb) {
        fenv.addVar(v);
      }
    }
    ExpressionTree a = c.getTrueExpression();
    ExpressionTree b = c.getFalseExpression();
    boolean poly = isPolyOperand(a) || isPolyOperand(b);
    deferNext = cd && isCallKind(a);
    Ty at = expr(a, tenv, pt);
    deferNext = cd && isCallKind(b);
    Ty bt = expr(b, fenv, pt);
    deferNext = false;
    Object cc = consts.get(cond);
    Ty result = conditionalType(c, at, bt, pt, poly, consts.get(a), consts.get(b));
    if (result == pt && pt != null && !pt.isPrim() && at.tag != Ty.ERROR && bt.tag != Ty.ERROR) {
      if (TypeOps.hasIvar(pt)) {
        tx.isLooseConvertible(at, pt);
        tx.isLooseConvertible(bt, pt);
      } else {
        checkAssignable(a, at, pt, consts.get(a));
        checkAssignable(b, bt, pt, consts.get(b));
      }
    }
    if (cc instanceof Boolean && consts.get(a) != null && consts.get(b) != null && result.isPrim()) {
      Object v = ((Boolean) cc).booleanValue() ? consts.get(a) : consts.get(b);
      Object co = ConstFold.coerce(v, result);
      if (co != null) {
        consts.put(c, co);
      }
    } else if (cc instanceof Boolean && consts.get(a) instanceof String && consts.get(b) instanceof String) {
      consts.put(c, ((Boolean) cc).booleanValue() ? consts.get(a) : consts.get(b));
    }
    info.put(c, result);
    return result;
  }

  private boolean isPolyOperand(ExpressionTree e) {
    e = unparen(e);
    return e.getKind() == Tree.Kind.LAMBDA_EXPRESSION || e.getKind() == Tree.Kind.MEMBER_REFERENCE;
  }

  private Ty conditionalType(ConditionalExpressionTree c, Ty at, Ty bt, Ty pt, boolean poly, Object ac, Object bc) {
    if (at.tag == Ty.ERROR || bt.tag == Ty.ERROR) {
      return at.tag == Ty.ERROR ? bt : at;
    }
    if (poly && pt != null) {
      return pt;
    }
    Ty au = unboxedOrSelf(at);
    Ty bu = unboxedOrSelf(bt);
    if (at.tag == Ty.VOID || bt.tag == Ty.VOID) {
      err(c, "compiler.err.neither.conditional.subtype", "incompatible types in conditional expression");
      return Ty.tError;
    }
    if (au.tag == Ty.BOOLEAN && bu.tag == Ty.BOOLEAN) {
      return at.isPrim() || bt.isPrim() ? Ty.tBoolean : (at.tag == Ty.CLASS && bt.tag == Ty.CLASS ? at : Ty.tBoolean);
    }
    if (au.isNumeric() && bu.isNumeric()) {
      if (TypeOps.equal(at, bt)) {
        return at;
      }
      if (au.tag == bu.tag) {
        return au;
      }
      if (au.tag == Ty.BYTE && bu.tag == Ty.SHORT || au.tag == Ty.SHORT && bu.tag == Ty.BYTE) {
        return Ty.tShort;
      }
      if (au.tag <= Ty.CHAR && bu.tag == Ty.INT && bc != null && at.isPrim() && Attr.fitsConstant(bc, au)) {
        return au;
      }
      if (bu.tag <= Ty.CHAR && au.tag == Ty.INT && ac != null && bt.isPrim() && Attr.fitsConstant(ac, bu)) {
        return bu;
      }
      if (au.tag == Ty.BYTE && bu.tag == Ty.INT && bc != null && at.isPrim() && fitsConstant(bc, au)) {
        return au;
      }
      return binaryPromote(au, bu);
    }
    if (pt != null && pt.tag != Ty.NONE && !pt.isPrim() && !(at.isPrim() && bt.isPrim())) {
      return pt;
    }
    if (TypeOps.hasIvar(at) || TypeOps.hasIvar(bt)) {
      return TypeOps.hasIvar(at) ? bt.tag == Ty.NULL ? at : at : bt;
    }
    Ty ab = at.isPrim() ? tx.box(at) : at;
    Ty bb = bt.isPrim() ? tx.box(bt) : bt;
    List<Ty> l = new ArrayList<Ty>();
    l.add(ab);
    l.add(bb);
    return tx.lub(l);
  }

  /* ---------- arrays ---------- */

  private Ty newArray(NewArrayTree na, Env env, Ty pt) {
    if (na.getType() == null) {
      return attribInitializer(na, env, pt);
    }
    Ty elem = enter.resolveType(na.getType(), env);
    types.put(na.getType(), elem);
    int dims = na.getDimensions().size();
    Ty full = elem;
    int extra = 0;
    Tree tt = na.getType();
    // the parser stores additional [] on the type tree element for dims beyond the specified ones
    for (int i = 0; i < dims; i++) {
      full = Ty.array(full);
    }
    for (ExpressionTree d : na.getDimensions()) {
      Ty dt = expr(d, env, null);
      Ty du = unboxedOrSelf(dt);
      if (!(du.isIntegral() && du.tag <= Ty.INT) && dt.tag != Ty.ERROR) {
        checkAssignable(d, dt, Ty.tInt, consts.get(d));
      }
    }
    if (na.getInitializers() != null) {
      full = Ty.array(elem);
      for (ExpressionTree i : na.getInitializers()) {
        initElem(i, env, elem);
      }
    }
    return full;
  }

  private void initElem(ExpressionTree i, Env env, Ty elem) {
    if (i.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) i).getType() == null) {
      attribInitializer((NewArrayTree) i, env, elem);
      return;
    }
    Ty t = expr(i, env, elem);
    checkAssignable(i, t, elem, consts.get(i));
  }

  Ty attribInitializer(NewArrayTree na, Env env, Ty arrayType) {
    types.put(na, arrayType);
    if (arrayType == null || arrayType.tag != Ty.ARRAY) {
      if (arrayType != null && arrayType.tag != Ty.ERROR) {
        err(na, "compiler.err.illegal.initializer.for.type", "illegal initializer for " + arrayType);
      }
      if (na.getInitializers() != null) {
        for (ExpressionTree i : na.getInitializers()) {
          expr(i, env, null);
        }
      }
      return Ty.tError;
    }
    if (na.getInitializers() != null) {
      for (ExpressionTree i : na.getInitializers()) {
        initElem(i, env, arrayType.elem);
      }
    }
    return arrayType;
  }
}
