/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

/** Expression code generation. */
abstract class CodeExpr extends CodeBase {
  CodeExpr(ClassGen cg, CodeBuilder b, MSym method) {
    super(cg, b, method);
  }

  abstract Ty lambdaExpr(LambdaExpressionTree l);

  abstract Ty mrefExpr(MemberReferenceTree m);

  abstract Ty switchExprGen(SwitchExpressionTree s);

  abstract void instanceOfPatternCond(InstanceOfTree io, Label target, boolean jumpIfTrue);

  /* ---------- entry points ---------- */

  Ty sem(Tree e) {
    Ty t = a.typeOf(e);
    return t == null ? Ty.tError : t;
  }

  /** Generates the expression and converts the result to the wanted type. */
  void genAs(ExpressionTree e, Ty want) {
    Ty stack = expr(e);
    coerce(stack, sem(e), want);
  }

  void genDiscard(ExpressionTree e) {
    e = Attr.unparen(e);
    switch (e.getKind()) {
      case ASSIGNMENT:
        assign((AssignmentTree) e, false);
        return;
      case POSTFIX_INCREMENT:
      case POSTFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case PREFIX_DECREMENT:
        incDec((UnaryTree) e, false);
        return;
      default:
        break;
    }
    if (e instanceof CompoundAssignmentTree) {
      compound((CompoundAssignmentTree) e, false);
      return;
    }
    Ty t = expr(e);
    popValue(t);
  }

  Ty expr(ExpressionTree e) {
    Object c = a.consts.get(e);
    Ty t = sem(e);
    if (c != null && (t.isPrim() || t.isString())) {
      pushConst(c, t);
      return t.isPrim() ? t : st.string;
    }
    switch (e.getKind()) {
      case NULL_LITERAL:
        b.aconst_null();
        return Ty.tNull;
      case IDENTIFIER:
        return ident((IdentifierTree) e);
      case MEMBER_SELECT:
        return select((MemberSelectTree) e);
      case PARENTHESIZED:
        return expr(((ParenthesizedTree) e).getExpression());
      case METHOD_INVOCATION:
        return call((MethodInvocationTree) e);
      case NEW_CLASS:
        return newClass((NewClassTree) e);
      case NEW_ARRAY:
        return newArray((NewArrayTree) e);
      case ARRAY_ACCESS: {
        ArrayAccessTree aa = (ArrayAccessTree) e;
        Ty at = erase(sem(aa.getExpression()));
        Ty ast = expr(aa.getExpression());
        if (!subtypeErased(ast, at) || ast.tag != Ty.ARRAY) {
          checkcast(at);
        }
        genAs(aa.getIndex(), Ty.tInt);
        Ty el = at.tag == Ty.ARRAY ? at.elem : obj();
        b.arrayLoad(Gen.kind(el));
        return el;
      }
      case ASSIGNMENT:
        return assign((AssignmentTree) e, true);
      case TYPE_CAST:
        return cast((TypeCastTree) e);
      case INSTANCE_OF:
        return boolValue(e);
      case CONDITIONAL_EXPRESSION:
        return conditional((ConditionalExpressionTree) e);
      case LAMBDA_EXPRESSION:
        return lambdaExpr((LambdaExpressionTree) e);
      case MEMBER_REFERENCE:
        return mrefExpr((MemberReferenceTree) e);
      case SWITCH_EXPRESSION:
        return switchExprGen((SwitchExpressionTree) e);
      case POSTFIX_INCREMENT:
      case POSTFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case PREFIX_DECREMENT:
        return incDec((UnaryTree) e, true);
      case UNARY_PLUS:
      case UNARY_MINUS:
      case BITWISE_COMPLEMENT: {
        UnaryTree u = (UnaryTree) e;
        Ty rt = sem(e);
        genAs(u.getExpression(), rt);
        if (e.getKind() == Tree.Kind.UNARY_MINUS) {
          switch (rt.tag) {
            case Ty.LONG:
              b.lneg();
              break;
            case Ty.FLOAT:
              b.fneg();
              break;
            case Ty.DOUBLE:
              b.dneg();
              break;
            default:
              b.ineg();
              break;
          }
        } else if (e.getKind() == Tree.Kind.BITWISE_COMPLEMENT) {
          if (rt.tag == Ty.LONG) {
            b.loadConstant(-1L);
            b.lxor();
          } else {
            b.loadConstant(-1);
            b.ixor();
          }
        }
        return rt;
      }
      case LOGICAL_COMPLEMENT:
        return boolValue(e);
      default:
        break;
    }
    if (e instanceof CompoundAssignmentTree) {
      return compound((CompoundAssignmentTree) e, true);
    }
    if (e instanceof BinaryTree) {
      return binary((BinaryTree) e);
    }
    throw new IllegalStateException("cannot generate " + e.getKind());
  }

  /* ---------- names and fields ---------- */

  Ty loadVar(VSym v) {
    Ty t = erase(v.type);
    Integer s = slots.get(v);
    if (s != null) {
      load(t, s.intValue());
      return t;
    }
    CSym k = cls;
    b.aload(0);
    while (!g.caps.of(k).contains(v)) {
      if (k.outerClass == null || !Captures.needsOuterThis(k)) {
        throw new IllegalStateException("variable " + v.name + " is not available in " + cls.fullName());
      }
      b.getfield(cd(k), Gen.outerFieldName(k), cd(Ty.cls(k.outerClass)));
      k = k.outerClass;
    }
    b.getfield(cd(k), "val$" + v.name, cd(erase(v.type)));
    return t;
  }

  /** Pushes the innermost enclosing instance whose class is a subclass of target. */
  boolean prologue;

  void outerThis(CSym target) {
    CSym k = cls;
    if (prologue && ctorOuterSlot() >= 0 && !g.a.res.isSubClass(cls, target) && cls.outerClass != null) {
      b.aload(ctorOuterSlot());
      k = cls.outerClass;
    } else {
      b.aload(0);
    }
    while (!g.a.res.isSubClass(k, target)) {
      if (k.outerClass == null || !Captures.needsOuterThis(k)) {
        throw new IllegalStateException("no enclosing instance of " + target.fullName() + " in " + cls.fullName());
      }
      b.getfield(cd(k), Gen.outerFieldName(k), cd(Ty.cls(k.outerClass)));
      k = k.outerClass;
    }
  }

  Ty ownerFor(Sym member, CSym declaring, Ty site, boolean isMethod) {
    if (site == null) {
      return Ty.cls(declaring);
    }
    if (member.isPrivate()) {
      return Ty.cls(declaring);
    }
    Ty es = TypeOps.resolve(site);
    if (es.tag == Ty.WILD) {
      es = tx.upper(es);
    }
    if (es.tag == Ty.TVAR || es.tag == Ty.INTER || es.tag == Ty.IVAR) {
      Ty chosen = null;
      for (Ty s : tx.memberSites(es)) {
        Ty e1 = erase(s);
        if (e1.tag == Ty.CLASS && tx.isSubtype(e1, Ty.cls(declaring))) {
          chosen = e1;
          break;
        }
      }
      return chosen != null ? chosen : Ty.cls(declaring);
    }
    es = erase(es);
    if (es.tag == Ty.ARRAY) {
      if (isMethod && member.name.equals("clone")) {
        return es;
      }
      return obj();
    }
    if (es.tag == Ty.CLASS) {
      if (!tx.isSubtype(es, Ty.cls(declaring))) {
        return Ty.cls(declaring);
      }
      if (es.sym.isInterface() && declaring == st.objectSym) {
        return obj();
      }
      return es;
    }
    return Ty.cls(declaring);
  }

  private Ty ident(IdentifierTree id) {
    String n = id.getName().toString();
    Object s = a.syms.get(id);
    if (n.equals("this")) {
      b.aload(0);
      return cls.erasedType();
    }
    if (n.equals("super")) {
      b.aload(0);
      Ty sc = cls.superclass();
      return sc == null ? obj() : erase(sc);
    }
    if (s instanceof VSym) {
      return loadVar((VSym) s);
    }
    if (s instanceof FSym) {
      FSym f = (FSym) s;
      Attr.FieldRef r = (Attr.FieldRef) a.info.get(id);
      Ty owner = ownerFor(f, f.cls(), r == null ? null : r.implicitOwner != null ? r.implicitOwner.erasedType()
          : r.site, false);
      if (f.isStatic()) {
        b.getstatic(cd(owner), f.name, cd(f.type));
      } else {
        outerThis(r.implicitOwner);
        b.getfield(cd(owner), f.name, cd(f.type));
      }
      return erase(f.type);
    }
    throw new IllegalStateException("unresolved identifier " + n);
  }

  private Ty select(MemberSelectTree ms) {
    String n = ms.getIdentifier().toString();
    Object s = a.syms.get(ms);
    ExpressionTree q = ms.getExpression();
    if (n.equals("class")) {
      Ty t = a.typeOf(q);
      Ty qt = a.types.get(q) != null ? TypeOps.resolve(a.types.get(q)) : t;
      classLiteral(qt);
      return Ty.cls(st.lookup("java/lang/Class"));
    }
    if (n.equals("this") && s instanceof CSym) {
      outerThis((CSym) s);
      return ((CSym) s).erasedType();
    }
    if (s == Attr.ArrayLength.INSTANCE) {
      Ty at = erase(sem(q));
      Ty ast = expr(q);
      if (ast.tag != Ty.ARRAY) {
        checkcast(at);
      }
      b.arraylength();
      return Ty.tInt;
    }
    if (s instanceof FSym) {
      FSym f = (FSym) s;
      Attr.FieldRef r = (Attr.FieldRef) a.info.get(ms);
      Ty owner = ownerFor(f, f.cls(), r == null ? null : r.site, false);
      boolean typeQual = isTypeQualifier(q);
      if (f.isStatic()) {
        if (!typeQual) {
          Ty qt = expr(q);
          popValue(qt);
        }
        b.getstatic(cd(owner), f.name, cd(f.type));
      } else {
        Ty qst = expr(q);
        coerce(qst, sem(q), owner);
        b.getfield(cd(owner), f.name, cd(f.type));
      }
      return erase(f.type);
    }
    throw new IllegalStateException("unresolved select " + ms);
  }

  boolean isTypeQualifier(ExpressionTree q) {
    Object s = a.syms.get(q);
    return s instanceof CSym && !(q instanceof IdentifierTree && ((IdentifierTree) q).getName().contentEquals("this"))
        || a.symOf(q) == null && q instanceof MemberSelectTree && a.types.get(q) != null && a.typeOf(q) != null
        && a.typeOf(q).tag == Ty.CLASS && isPackageSelect(q);
  }

  private boolean isPackageSelect(ExpressionTree q) {
    return a.syms.get(q) instanceof CSym;
  }

  void classLiteral(Ty t) {
    if (t.isPrim()) {
      b.getstatic(cdi("java/lang/" + Tx.BOXES[t.tag].substring(10)), "TYPE", ClassDesc.ofDescriptor("Ljava/lang/Class;"));
    } else if (t.tag == Ty.VOID) {
      b.getstatic(cdi("java/lang/Void"), "TYPE", ClassDesc.ofDescriptor("Ljava/lang/Class;"));
    } else {
      b.ldc(cd(erase(t)));
    }
  }

  /* ---------- method calls ---------- */

  private Ty call(MethodInvocationTree mi) {
    AttrCall.CallInfo ci = (AttrCall.CallInfo) a.info.get(mi);
    if (ci == null) {
      throw new IllegalStateException("unresolved call " + mi);
    }
    line(mi);
    MSym m = ci.m;
    if (ci.ctorCall) {
      ctorCall(mi, ci);
      return Ty.tVoid;
    }
    ExpressionTree sel = mi.getMethodSelect();
    boolean special = false;
    Ty owner;
    Ty site = ci.site;
    if (m.isStatic()) {
      owner = ownerFor(m, m.cls(), ci.implicitOwner != null ? ci.implicitOwner.erasedType() : site, true);
      if (sel instanceof MemberSelectTree) {
        ExpressionTree q = ((MemberSelectTree) sel).getExpression();
        if (!isTypeQualifier(q) && !isSuperQual(q)) {
          Ty qt = expr(q);
          popValue(qt);
        }
      }
    } else if (ci.superCall) {
      owner = ci.isInterfaceSuper ? Ty.cls(m.cls()) : ownerForSuper(ci);
      if (ci.superQualifier != null && !ci.isInterfaceSuper && ci.superQualifier != cls) {
        outerThis(ci.superQualifier);
      } else {
        b.aload(0);
      }
      special = true;
      if (ci.isInterfaceSuper) {
        owner = Ty.cls(ci.superQualifier);
      }
    } else if (sel instanceof IdentifierTree) {
      owner = ownerFor(m, m.cls(), ci.implicitOwner.erasedType(), true);
      outerThis(ci.implicitOwner);
      if (!tx.isSubtype(ci.implicitOwner.erasedType(), owner)) {
        checkcast(owner);
      }
    } else {
      ExpressionTree q = ((MemberSelectTree) sel).getExpression();
      owner = ownerFor(m, m.cls(), site, true);
      Ty qst = expr(q);
      coerce(qst, sem(q), owner);
    }
    if (ci.polySig) {
      StringBuilder d = new StringBuilder("(");
      for (ExpressionTree arg : mi.getArguments()) {
        Ty at = sem(arg);
        Ty stack = expr(arg);
        Ty pt = stack.isPrim() ? stack : at.tag == Ty.NULL ? Ty.cls(st.lookup("java/lang/Void")) : erase(at);
        d.append(pt.desc());
      }
      Ty rt = ci.sigRet == null ? obj() : erase(ci.sigRet);
      d.append(")").append(rt.desc());
      b.invokevirtual(cd(owner), m.name, mtd(d.toString()));
      return rt;
    }
    genArgs(ci, mi.getArguments(), m);
    emitInvoke(m, owner, special);
    if (owner.tag == Ty.ARRAY && m.name.equals("clone")) {
      return obj();
    }
    return m.name.equals("<init>") ? Ty.tVoid : erase(m.ret);
  }

  private boolean isSuperQual(ExpressionTree q) {
    return q instanceof IdentifierTree && ((IdentifierTree) q).getName().contentEquals("super");
  }

  private Ty ownerForSuper(AttrCall.CallInfo ci) {
    Ty sc = ci.superQualifier != null ? ci.superQualifier.superclass() : cls.superclass();
    if (sc == null) {
      return obj();
    }
    return erase(sc);
  }

  void emitInvoke(MSym m, Ty owner, boolean special) {
    ClassDesc od = cd(owner);
    MethodTypeDesc d = mtd(m.isCtor() ? g.ctorDesc(m) : m.erasedDesc());
    if (owner.tag == Ty.ARRAY && m.name.equals("clone")) {
      d = mtd("()Ljava/lang/Object;");
    }
    boolean iface = owner.tag == Ty.CLASS && owner.sym.isInterface();
    if (m.isStatic()) {
      b.invokestatic(od, m.name, d, iface);
    } else if (special || m.isCtor()) {
      b.invokespecial(od, m.name, d, iface);
    } else if (iface) {
      b.invokeinterface(od, m.name, d);
    } else {
      b.invokevirtual(od, m.name, d);
    }
  }

  void genArgs(AttrCall.CallInfo ci, List<? extends ExpressionTree> args, MSym m) {
    List<Ty> formals = ci.formals;
    if (!ci.varargs) {
      for (int i = 0; i < args.size(); i++) {
        genAs(args.get(i), formals.get(i));
      }
      return;
    }
    int fixed = ci.fixed;
    for (int i = 0; i < fixed; i++) {
      genAs(args.get(i), formals.get(i));
    }
    Ty elem = ci.varargElem == null ? obj() : TypeOps.resolve(ci.varargElem);
    Ty ee = erase(elem);
    int n = args.size() - fixed;
    pushInt(n);
    newArrayOf(ee);
    for (int j = 0; j < n; j++) {
      b.dup();
      pushInt(j);
      genAs(args.get(fixed + j), elem);
      b.arrayStore(Gen.kind(ee));
    }
  }

  void newArrayOf(Ty elem) {
    if (elem.isPrim()) {
      b.newarray(Gen.kind(elem));
    } else {
      b.anewarray(cd(elem));
    }
  }

  /** this(...) and super(...) calls inside constructors. */
  void ctorCall(MethodInvocationTree mi, AttrCall.CallInfo ci) {
    MSym m = ci.m;
    CSym target = m.cls();
    Gen.Layout l = g.layout(target, m);
    b.aload(0);
    ctorPrefix(target, l, ci.thisCall, mi.getMethodSelect() instanceof MemberSelectTree
        ? ((MemberSelectTree) mi.getMethodSelect()).getExpression() : null);
    genArgs(ci, mi.getArguments(), m);
    ctorSuffix(l);
    b.invokespecial(cd(target), "<init>", mtd(l.desc()));
  }

  /** Pushes the synthetic leading constructor arguments for a this or super call. */
  void ctorPrefix(CSym target, Gen.Layout l, boolean isThisCall, ExpressionTree qualifier) {
    if (l.enumPrefix) {
      b.aload(1);
      b.iload(2);
    }
    if (l.outer != null) {
      if (isThisCall) {
        b.aload(ctorOuterSlot());
      } else if (qualifier != null) {
        Ty qt = expr(qualifier);
        b.dup();
        b.invokestatic(cdi("java/util/Objects"), "requireNonNull", mtd("(Ljava/lang/Object;)Ljava/lang/Object;"));
        b.pop();
      } else {
        ctorOuterFor(target);
      }
    }
  }

  abstract int ctorOuterSlot();

  /** Pushes the enclosing instance a superclass constructor needs. */
  void ctorOuterFor(CSym target) {
    CSym needed = target.outerClass;
    if (cls.outerClass != null && Captures.needsOuterThis(cls)) {
      b.aload(ctorOuterSlot());
      CSym k = cls.outerClass;
      while (!g.a.res.isSubClass(k, needed)) {
        if (k.outerClass == null || !Captures.needsOuterThis(k)) {
          throw new IllegalStateException("no enclosing instance of " + needed.fullName());
        }
        b.getfield(cd(k), Gen.outerFieldName(k), cd(Ty.cls(k.outerClass)));
        k = k.outerClass;
      }
    } else {
      throw new IllegalStateException("no enclosing instance of " + needed.fullName());
    }
  }

  void ctorSuffix(Gen.Layout l) {
    for (VSym v : l.captured) {
      loadVar(v);
    }
  }

  /* ---------- object creation ---------- */

  private Ty newClass(NewClassTree nc) {
    AttrCall.CallInfo ci = (AttrCall.CallInfo) a.info.get(nc);
    line(nc);
    MSym m = ci.m;
    CSym target = ci.anon != null ? ci.anon : m.cls();
    Gen.Layout l = ci.anon != null ? g.layout(ci.anon, null) : g.layout(target, m);
    b.new_(cd(target));
    b.dup();
    newClassArgs(nc, ci, target, l);
    b.invokespecial(cd(target), "<init>", mtd(l.desc()));
    return target.erasedType();
  }

  void newClassArgs(NewClassTree nc, AttrCall.CallInfo ci, CSym target, Gen.Layout l) {
    MSym m = ci.m;
    if (l.outer != null) {
      if (nc.getEnclosingExpression() != null && ci.anon == null) {
        Ty qt = expr(nc.getEnclosingExpression());
        b.dup();
        b.invokestatic(cdi("java/util/Objects"), "requireNonNull", mtd("(Ljava/lang/Object;)Ljava/lang/Object;"));
        b.pop();
      } else {
        outerThis(l.outer.sym);
      }
    }
    if (ci.anon != null && m.cls() != st.objectSym && m.cls().isInner() && m.cls().outerClass != null) {
      if (nc.getEnclosingExpression() != null) {
        Ty qt = expr(nc.getEnclosingExpression());
        b.dup();
        b.invokestatic(cdi("java/util/Objects"), "requireNonNull", mtd("(Ljava/lang/Object;)Ljava/lang/Object;"));
        b.pop();
      } else {
        outerThis(m.cls().outerClass);
      }
    }
    if (m.cls() != st.objectSym || ci.anon == null) {
      genArgs(ci, nc.getArguments(), m);
    }
    for (VSym v : l.captured) {
      loadVar(v);
    }
  }

  private Ty newArray(NewArrayTree na) {
    Ty full = erase(sem(na));
    if (na.getInitializers() != null && na.getDimensions().isEmpty()) {
      Ty el = full.tag == Ty.ARRAY ? full.elem : obj();
      List<? extends ExpressionTree> inits = na.getInitializers();
      pushInt(inits.size());
      newArrayOf(el);
      for (int i = 0; i < inits.size(); i++) {
        b.dup();
        pushInt(i);
        ExpressionTree ie = inits.get(i);
        if (ie.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) ie).getType() == null) {
          newArray((NewArrayTree) ie);
        } else {
          genAs(ie, a.typeOf(ie) != null && el.isPrim() ? el : TypeOps.resolve(typeOfElem(full)));
        }
        b.arrayStore(Gen.kind(el));
      }
      return full;
    }
    int dims = na.getDimensions().size();
    for (ExpressionTree d : na.getDimensions()) {
      genAs(d, Ty.tInt);
    }
    if (dims == 1) {
      newArrayOf(erase(full.elem));
    } else {
      b.multianewarray(cd(full), dims);
    }
    return full;
  }

  private Ty typeOfElem(Ty arr) {
    return arr.tag == Ty.ARRAY ? arr.elem : obj();
  }

  /* ---------- operators ---------- */

  private Ty cast(TypeCastTree c) {
    Ty target = sem(c);
    ExpressionTree in = c.getExpression();
    Ty stack = expr(in);
    Ty s = sem(in);
    if (target.isPrim()) {
      coerce(stack, s, target);
      return target;
    }
    if (stack.isPrim()) {
      coerce(stack, s, target);
      return erase(target);
    }
    if (target.tag == Ty.INTER) {
      for (Ty comp : target.args) {
        checkcast(erase(comp));
      }
      return erase(target);
    }
    Ty et = erase(target);
    if (!subtypeErased(stack, et)) {
      checkcast(et);
    }
    return et;
  }

  private Ty conditional(ConditionalExpressionTree c) {
    Ty rt = sem(c);
    Label elseL = b.newLabel();
    Label end = b.newLabel();
    cond(c.getCondition(), elseL, false);
    genAs(c.getTrueExpression(), rt);
    b.goto_(end);
    b.labelBinding(elseL);
    genAs(c.getFalseExpression(), rt);
    b.labelBinding(end);
    return erase(rt);
  }

  Ty boolValue(ExpressionTree e) {
    Label f = b.newLabel();
    Label end = b.newLabel();
    cond(e, f, false);
    b.iconst_1();
    b.goto_(end);
    b.labelBinding(f);
    b.iconst_0();
    b.labelBinding(end);
    return Ty.tBoolean;
  }

  void cond(ExpressionTree e, Label target, boolean jumpIfTrue) {
    e = Attr.unparen(e);
    Object c = a.consts.get(e);
    if (c instanceof Boolean) {
      if (((Boolean) c).booleanValue() == jumpIfTrue) {
        b.goto_(target);
      }
      return;
    }
    switch (e.getKind()) {
      case LOGICAL_COMPLEMENT:
        cond(((UnaryTree) e).getExpression(), target, !jumpIfTrue);
        return;
      case CONDITIONAL_AND: {
        BinaryTree bt = (BinaryTree) e;
        if (jumpIfTrue) {
          Label skip = b.newLabel();
          cond(bt.getLeftOperand(), skip, false);
          cond(bt.getRightOperand(), target, true);
          b.labelBinding(skip);
        } else {
          cond(bt.getLeftOperand(), target, false);
          cond(bt.getRightOperand(), target, false);
        }
        return;
      }
      case CONDITIONAL_OR: {
        BinaryTree bt = (BinaryTree) e;
        if (jumpIfTrue) {
          cond(bt.getLeftOperand(), target, true);
          cond(bt.getRightOperand(), target, true);
        } else {
          Label skip = b.newLabel();
          cond(bt.getLeftOperand(), skip, true);
          cond(bt.getRightOperand(), target, false);
          b.labelBinding(skip);
        }
        return;
      }
      case INSTANCE_OF:
        instanceOfPatternCond((InstanceOfTree) e, target, jumpIfTrue);
        return;
      case LESS_THAN:
      case GREATER_THAN:
      case LESS_THAN_EQUAL:
      case GREATER_THAN_EQUAL:
      case EQUAL_TO:
      case NOT_EQUAL_TO:
        compare((BinaryTree) e, target, jumpIfTrue);
        return;
      default:
        break;
    }
    genAs(e, Ty.tBoolean);
    if (jumpIfTrue) {
      b.ifne(target);
    } else {
      b.ifeq(target);
    }
  }

  private void compare(BinaryTree bt, Label target, boolean jumpIfTrue) {
    Tree.Kind k = bt.getKind();
    Ty ot = (Ty) a.info.get(bt);
    ot = ot == null ? obj() : TypeOps.resolve(ot);
    ExpressionTree l = bt.getLeftOperand();
    ExpressionTree r = bt.getRightOperand();
    if (!jumpIfTrue) {
      k = negate(k, ot);
    }
    boolean fp = ot.tag == Ty.FLOAT || ot.tag == Ty.DOUBLE;
    boolean notNegated = jumpIfTrue;
    if (!ot.isPrim()) {
      boolean ln = isNullLit(l);
      boolean rn = isNullLit(r);
      if (rn || ln) {
        ExpressionTree other = rn ? l : r;
        Ty ot2 = expr(other);
        if (k == Tree.Kind.EQUAL_TO) {
          b.ifnull(target);
        } else {
          b.ifnonnull(target);
        }
        return;
      }
      expr(l);
      expr(r);
      if (k == Tree.Kind.EQUAL_TO) {
        b.if_acmpeq(target);
      } else {
        b.if_acmpne(target);
      }
      return;
    }
    if (ot.tag == Ty.BOOLEAN || ot.tag == Ty.INT || ot.tag == Ty.SHORT || ot.tag == Ty.BYTE || ot.tag == Ty.CHAR) {
      Object rc = a.consts.get(r);
      if (rc != null && isZeroConst(rc)) {
        genAs(l, ot);
        switch (k) {
          case LESS_THAN:
            b.iflt(target);
            return;
          case GREATER_THAN:
            b.ifgt(target);
            return;
          case LESS_THAN_EQUAL:
            b.ifle(target);
            return;
          case GREATER_THAN_EQUAL:
            b.ifge(target);
            return;
          case EQUAL_TO:
            b.ifeq(target);
            return;
          default:
            b.ifne(target);
            return;
        }
      }
      genAs(l, ot);
      genAs(r, ot);
      switch (k) {
        case LESS_THAN:
          b.if_icmplt(target);
          return;
        case GREATER_THAN:
          b.if_icmpgt(target);
          return;
        case LESS_THAN_EQUAL:
          b.if_icmple(target);
          return;
        case GREATER_THAN_EQUAL:
          b.if_icmpge(target);
          return;
        case EQUAL_TO:
          b.if_icmpeq(target);
          return;
        default:
          b.if_icmpne(target);
          return;
      }
    }
    genAs(l, ot);
    genAs(r, ot);
    Tree.Kind orig = bt.getKind();
    boolean lessLike = orig == Tree.Kind.LESS_THAN || orig == Tree.Kind.LESS_THAN_EQUAL;
    if (ot.tag == Ty.LONG) {
      b.lcmp();
    } else if (ot.tag == Ty.FLOAT) {
      if (lessLike) {
        b.fcmpg();
      } else {
        b.fcmpl();
      }
    } else {
      if (lessLike) {
        b.dcmpg();
      } else {
        b.dcmpl();
      }
    }
    switch (k) {
      case LESS_THAN:
        b.iflt(target);
        return;
      case GREATER_THAN:
        b.ifgt(target);
        return;
      case LESS_THAN_EQUAL:
        b.ifle(target);
        return;
      case GREATER_THAN_EQUAL:
        b.ifge(target);
        return;
      case EQUAL_TO:
        b.ifeq(target);
        return;
      default:
        b.ifne(target);
        return;
    }
  }

  private static boolean isZeroConst(Object c) {
    if (c instanceof Boolean) {
      return !((Boolean) c).booleanValue();
    }
    if (c instanceof Character) {
      return ((Character) c).charValue() == 0;
    }
    return c instanceof Number && !(c instanceof Float) && !(c instanceof Double) && ((Number) c).longValue() == 0;
  }

  private static boolean isNullLit(ExpressionTree e) {
    e = Attr.unparen(e);
    return e.getKind() == Tree.Kind.NULL_LITERAL;
  }

  private static Tree.Kind negate(Tree.Kind k, Ty operand) {
    switch (k) {
      case LESS_THAN:
        return Tree.Kind.GREATER_THAN_EQUAL;
      case GREATER_THAN:
        return Tree.Kind.LESS_THAN_EQUAL;
      case LESS_THAN_EQUAL:
        return Tree.Kind.GREATER_THAN;
      case GREATER_THAN_EQUAL:
        return Tree.Kind.LESS_THAN;
      case EQUAL_TO:
        return Tree.Kind.NOT_EQUAL_TO;
      default:
        return Tree.Kind.EQUAL_TO;
    }
  }

  private Ty binary(BinaryTree bt) {
    Tree.Kind k = bt.getKind();
    Ty rt = sem(bt);
    if (k == Tree.Kind.PLUS && rt.isString()) {
      return concat(bt);
    }
    switch (k) {
      case CONDITIONAL_AND:
      case CONDITIONAL_OR:
      case LESS_THAN:
      case GREATER_THAN:
      case LESS_THAN_EQUAL:
      case GREATER_THAN_EQUAL:
      case EQUAL_TO:
      case NOT_EQUAL_TO:
        return boolValue(bt);
      default:
        break;
    }
    Ty ot = (Ty) a.info.get(bt);
    ot = ot == null ? rt : TypeOps.resolve(ot);
    boolean shift = k == Tree.Kind.LEFT_SHIFT || k == Tree.Kind.RIGHT_SHIFT || k == Tree.Kind.UNSIGNED_RIGHT_SHIFT;
    genAs(bt.getLeftOperand(), ot);
    if (shift) {
      Ty rs = a.unboxedOrSelf(sem(bt.getRightOperand()));
      genAs(bt.getRightOperand(), rs.tag == Ty.LONG ? Ty.tLong : Ty.tInt);
      if (rs.tag == Ty.LONG) {
        b.l2i();
      }
    } else {
      genAs(bt.getRightOperand(), ot);
    }
    arith(k, ot);
    return ot;
  }

  void arith(Tree.Kind k, Ty t) {
    int w = t.tag == Ty.LONG ? 1 : t.tag == Ty.FLOAT ? 2 : t.tag == Ty.DOUBLE ? 3 : 0;
    switch (k) {
      case PLUS:
        switch (w) {
          case 0:
            b.iadd();
            break;
          case 1:
            b.ladd();
            break;
          case 2:
            b.fadd();
            break;
          default:
            b.dadd();
            break;
        }
        break;
      case MINUS:
        switch (w) {
          case 0:
            b.isub();
            break;
          case 1:
            b.lsub();
            break;
          case 2:
            b.fsub();
            break;
          default:
            b.dsub();
            break;
        }
        break;
      case MULTIPLY:
        switch (w) {
          case 0:
            b.imul();
            break;
          case 1:
            b.lmul();
            break;
          case 2:
            b.fmul();
            break;
          default:
            b.dmul();
            break;
        }
        break;
      case DIVIDE:
        switch (w) {
          case 0:
            b.idiv();
            break;
          case 1:
            b.ldiv();
            break;
          case 2:
            b.fdiv();
            break;
          default:
            b.ddiv();
            break;
        }
        break;
      case REMAINDER:
        switch (w) {
          case 0:
            b.irem();
            break;
          case 1:
            b.lrem();
            break;
          case 2:
            b.frem();
            break;
          default:
            b.drem();
            break;
        }
        break;
      case AND:
        if (w == 1) {
          b.land();
        } else {
          b.iand();
        }
        break;
      case OR:
        if (w == 1) {
          b.lor();
        } else {
          b.ior();
        }
        break;
      case XOR:
        if (w == 1) {
          b.lxor();
        } else {
          b.ixor();
        }
        break;
      case LEFT_SHIFT:
        if (w == 1) {
          b.lshl();
        } else {
          b.ishl();
        }
        break;
      case RIGHT_SHIFT:
        if (w == 1) {
          b.lshr();
        } else {
          b.ishr();
        }
        break;
      case UNSIGNED_RIGHT_SHIFT:
        if (w == 1) {
          b.lushr();
        } else {
          b.iushr();
        }
        break;
      default:
        throw new IllegalStateException("operator " + k);
    }
  }

  /* ---------- string concatenation ---------- */

  private void flatten(ExpressionTree e, List<ExpressionTree> out) {
    ExpressionTree u = Attr.unparen(e);
    if (u.getKind() == Tree.Kind.PLUS && sem(u).isString() && a.consts.get(u) == null) {
      BinaryTree bt = (BinaryTree) u;
      flatten(bt.getLeftOperand(), out);
      out.add(bt.getRightOperand());
      return;
    }
    out.add(e);
  }

  Ty concat(BinaryTree bt) {
    List<ExpressionTree> parts = new ArrayList<ExpressionTree>();
    flatten(bt, parts);
    b.new_(cdi("java/lang/StringBuilder"));
    b.dup();
    b.invokespecial(cdi("java/lang/StringBuilder"), "<init>", mtd("()V"));
    for (ExpressionTree p : parts) {
      appendTo(p);
    }
    b.invokevirtual(cdi("java/lang/StringBuilder"), "toString", mtd("()Ljava/lang/String;"));
    return st.string;
  }

  void appendTo(ExpressionTree p) {
    Ty t = sem(p);
    Ty stack = expr(p);
    appendStack(stack, t);
  }

  void valueOfString(Ty stack) {
    String d = "Ljava/lang/Object;";
    if (stack.isPrim()) {
      d = stack.tag <= Ty.INT && stack.tag != Ty.CHAR || stack.tag == Ty.INT ? "I" : stack.desc();
      if (stack.tag == Ty.BYTE || stack.tag == Ty.SHORT) {
        d = "I";
      }
    }
    b.invokestatic(cdi("java/lang/String"), "valueOf", mtd("(" + d + ")Ljava/lang/String;"));
  }

  void appendStack(Ty stack, Ty t) {
    String d;
    Ty u = stack.isPrim() ? stack : t;
    if (stack.isPrim()) {
      switch (stack.tag) {
        case Ty.BYTE:
        case Ty.SHORT:
        case Ty.INT:
          d = "I";
          break;
        case Ty.CHAR:
          d = "C";
          break;
        case Ty.LONG:
          d = "J";
          break;
        case Ty.FLOAT:
          d = "F";
          break;
        case Ty.DOUBLE:
          d = "D";
          break;
        default:
          d = "Z";
          break;
      }
    } else if (TypeOps.resolve(t).isString() && stack.isString()) {
      d = "Ljava/lang/String;";
    } else {
      d = "Ljava/lang/Object;";
    }
    b.invokevirtual(cdi("java/lang/StringBuilder"), "append", mtd("(" + d + ")Ljava/lang/StringBuilder;"));
  }

  /* ---------- assignment ---------- */

  /** Lvalue shapes. */
  static final int LV_LOCAL = 0;
  static final int LV_STATIC = 1;
  static final int LV_FIELD = 2;
  static final int LV_ARRAY = 3;

  final class Lv {
    int kind;
    int slot;
    VSym var;
    FSym field;
    Ty owner;
    Ty type;
    Ty elem;

    void dupAddress() {
      if (kind == LV_FIELD) {
        b.dup();
      } else if (kind == LV_ARRAY) {
        b.dup2();
      }
    }

    void dupValueBelow(Ty t) {
      switch (kind) {
        case LV_FIELD:
          dupX1(t);
          break;
        case LV_ARRAY:
          dupX2(t);
          break;
        default:
          dupValue(t);
          break;
      }
    }

    void load() {
      switch (kind) {
        case LV_LOCAL:
          CodeExpr.this.load(type, slot);
          break;
        case LV_STATIC:
          b.getstatic(cd(owner), field.name, cd(field.type));
          break;
        case LV_FIELD:
          b.getfield(cd(owner), field.name, cd(field.type));
          break;
        default:
          b.arrayLoad(Gen.kind(elem));
          break;
      }
    }

    void storeIt() {
      switch (kind) {
        case LV_LOCAL:
          CodeExpr.this.store(type, slot);
          break;
        case LV_STATIC:
          b.putstatic(cd(owner), field.name, cd(field.type));
          break;
        case LV_FIELD:
          b.putfield(cd(owner), field.name, cd(field.type));
          break;
        default:
          b.arrayStore(Gen.kind(elem));
          break;
      }
    }
  }

  /** Pushes the address part of an lvalue and describes it. */
  Lv lvalue(ExpressionTree target) {
    ExpressionTree t = Attr.unparen(target);
    Lv lv = new Lv();
    Object s = a.syms.get(t);
    if (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
      ArrayAccessTree aa = (ArrayAccessTree) t;
      Ty at = erase(sem(aa.getExpression()));
      Ty ast = expr(aa.getExpression());
      if (ast.tag != Ty.ARRAY) {
        checkcast(at);
      }
      genAs(aa.getIndex(), Ty.tInt);
      lv.kind = LV_ARRAY;
      lv.elem = at.tag == Ty.ARRAY ? at.elem : obj();
      lv.type = sem(t);
      return lv;
    }
    if (s instanceof VSym) {
      VSym v = (VSym) s;
      Integer slot = slots.get(v);
      if (slot == null) {
        throw new IllegalStateException("assignment to captured variable " + v.name);
      }
      lv.kind = LV_LOCAL;
      lv.slot = slot.intValue();
      lv.var = v;
      lv.type = erase(v.type);
      return lv;
    }
    if (s instanceof FSym) {
      FSym f = (FSym) s;
      lv.field = f;
      lv.type = erase(f.type);
      Attr.FieldRef r = (Attr.FieldRef) a.info.get(t);
      if (t instanceof IdentifierTree) {
        lv.owner = ownerFor(f, f.cls(), r == null ? null : r.implicitOwner != null ? r.implicitOwner.erasedType()
            : r.site, false);
        if (f.isStatic()) {
          lv.kind = LV_STATIC;
        } else {
          lv.kind = LV_FIELD;
          outerThis(r.implicitOwner);
        }
      } else {
        MemberSelectTree ms = (MemberSelectTree) t;
        lv.owner = ownerFor(f, f.cls(), r == null ? null : r.site, false);
        ExpressionTree q = ms.getExpression();
        if (f.isStatic()) {
          lv.kind = LV_STATIC;
          if (!isTypeQualifier(q)) {
            Ty qt = expr(q);
            popValue(qt);
          }
        } else {
          lv.kind = LV_FIELD;
          Ty qst = expr(q);
          coerce(qst, sem(q), lv.owner);
        }
      }
      return lv;
    }
    throw new IllegalStateException("not an lvalue: " + t);
  }

  Ty assign(AssignmentTree as, boolean wantValue) {
    Lv lv = lvalue(as.getVariable());
    Ty lt = lv.kind == LV_ARRAY ? lv.elem : lv.type;
    Ty want = lv.kind == LV_ARRAY ? erase(lv.elem) : sem(as.getVariable());
    ExpressionTree rhs = as.getExpression();
    if (rhs.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) rhs).getType() == null) {
      newArray((NewArrayTree) rhs);
    } else {
      Ty rs = expr(rhs);
      coerce(rs, sem(rhs), lv.kind == LV_ARRAY ? (sem(as.getVariable()).isPrim() ? lv.elem : sem(as.getVariable()))
          : sem(as.getVariable()));
    }
    Ty vt = lv.kind == LV_ARRAY ? (sem(as.getVariable()).isPrim() ? lv.elem : erase(sem(as.getVariable()))) : lv.type;
    if (wantValue) {
      lv.dupValueBelow(vt.isPrim() ? vt : erase(vt));
    }
    lv.storeIt();
    return vt.isPrim() ? vt : erase(vt);
  }

  Ty compound(CompoundAssignmentTree c, boolean wantValue) {
    Tree.Kind op = Attr.baseOp(c.getKind());
    Ty opType = (Ty) a.info.get(c);
    opType = opType == null ? st.string : TypeOps.resolve(opType);
    ExpressionTree target = Attr.unparen(c.getVariable());
    Ty lt = sem(target);
    Lv lv = lvalue(target);
    lv.dupAddress();
    lv.load();
    Ty stackLt = lv.kind == LV_ARRAY ? erase(lv.elem) : lv.type;
    if (opType.isString() && !opType.isPrim()) {
      valueOfString(stackLt);
      b.new_(cdi("java/lang/StringBuilder"));
      b.dup_x1();
      b.swap();
      b.invokespecial(cdi("java/lang/StringBuilder"), "<init>", mtd("(Ljava/lang/String;)V"));
      appendTo(c.getExpression());
      b.invokevirtual(cdi("java/lang/StringBuilder"), "toString", mtd("()Ljava/lang/String;"));
    } else {
      coerce(stackLt, lt, opType);
      boolean shift = op == Tree.Kind.LEFT_SHIFT || op == Tree.Kind.RIGHT_SHIFT
          || op == Tree.Kind.UNSIGNED_RIGHT_SHIFT;
      if (shift) {
        Ty rs = a.unboxedOrSelf(sem(c.getExpression()));
        genAs(c.getExpression(), rs.tag == Ty.LONG ? Ty.tLong : Ty.tInt);
        if (rs.tag == Ty.LONG) {
          b.l2i();
        }
      } else {
        genAs(c.getExpression(), opType);
      }
      arith(op, opType);
      Ty back = lt.isPrim() ? lt : a.unboxedOrSelf(lt);
      if (back.isPrim() && back.tag != opType.tag) {
        convertPrim(opType, back);
      }
      if (!lt.isPrim() && back.isPrim()) {
        box(back);
      }
    }
    Ty resT = lv.kind == LV_ARRAY ? (lt.isPrim() ? lv.elem : erase(lt)) : lv.type;
    if (wantValue) {
      lv.dupValueBelow(resT);
    }
    lv.storeIt();
    return resT;
  }

  Ty incDec(UnaryTree u, boolean wantValue) {
    Tree.Kind k = u.getKind();
    boolean inc = k == Tree.Kind.POSTFIX_INCREMENT || k == Tree.Kind.PREFIX_INCREMENT;
    boolean post = k == Tree.Kind.POSTFIX_INCREMENT || k == Tree.Kind.POSTFIX_DECREMENT;
    ExpressionTree target = Attr.unparen(u.getExpression());
    Ty lt = sem(target);
    Ty prim = lt.isPrim() ? lt : a.unboxedOrSelf(lt);
    Object s = a.syms.get(target);
    if (s instanceof VSym && lt.tag == Ty.INT && slots.containsKey(s)) {
      int slot = slots.get(s).intValue();
      if (wantValue && post) {
        b.iload(slot);
      }
      b.iinc(slot, inc ? 1 : -1);
      if (wantValue && !post) {
        b.iload(slot);
      }
      return Ty.tInt;
    }
    Lv lv = lvalue(target);
    lv.dupAddress();
    lv.load();
    Ty stackLt = lv.kind == LV_ARRAY ? erase(lv.elem) : lv.type;
    Ty resT = lv.kind == LV_ARRAY ? (lt.isPrim() ? lv.elem : erase(lt)) : lv.type;
    if (wantValue && post) {
      lv.dupValueBelow(resT);
    }
    Ty opType = prim.tag < Ty.INT ? Ty.tInt : prim;
    coerce(stackLt, lt, opType);
    switch (opType.tag) {
      case Ty.LONG:
        b.loadConstant(1L);
        break;
      case Ty.FLOAT:
        b.loadConstant(1.0f);
        break;
      case Ty.DOUBLE:
        b.loadConstant(1.0);
        break;
      default:
        b.loadConstant(1);
        break;
    }
    arith(inc ? Tree.Kind.PLUS : Tree.Kind.MINUS, opType);
    if (prim.tag != opType.tag) {
      convertPrim(opType, prim);
    }
    if (!lt.isPrim()) {
      box(prim);
    }
    if (wantValue && !post) {
      lv.dupValueBelow(resT);
    }
    lv.storeIt();
    return resT;
  }
}
