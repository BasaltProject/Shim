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
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

/** Statement code generation: control flow, exceptions and finalizers. */
abstract class CodeStmt extends CodeExpr {
  static final class Target {
    String label;
    Label brk;
    Label cont;
    int finDepth;
    boolean isLoop;
    boolean isSwitchExpr;
    Ty resultTy;
    boolean isSwitch;
  }

  static final class Fin {
    Runnable emitter;
    final List<Label> gapStarts = new ArrayList<Label>();
    final List<Label> gapEnds = new ArrayList<Label>();
    boolean open;
    Label openStart;
  }

  final List<Target> targets = new ArrayList<Target>();
  List<Fin> fins = new ArrayList<Fin>();
  String pendingLabel;
  Ty returnTy;
  int outerSlot = -1;

  CodeStmt(ClassGen cg, CodeBuilder b, MSym method) {
    super(cg, b, method);
  }

  int ctorOuterSlot() {
    return outerSlot;
  }

  abstract void switchStmt(SwitchTree s);

  abstract void classDecl(ClassTree c);

  /* ---------- statements ---------- */

  void stmt(StatementTree s) {
    if (s == null) {
      return;
    }
    switch (s.getKind()) {
      case BLOCK: {
        for (StatementTree st1 : ((BlockTree) s).getStatements()) {
          stmt(st1);
          if (!Flow.canComplete(a, st1) && st1.getKind() != Tree.Kind.VARIABLE) {
            break;
          }
        }
        return;
      }
      case VARIABLE:
        localVar((VariableTree) s);
        return;
      case EXPRESSION_STATEMENT:
        line(s);
        genDiscard(((ExpressionStatementTree) s).getExpression());
        return;
      case IF:
        ifStmt((IfTree) s);
        return;
      case WHILE_LOOP: {
        WhileLoopTree w = (WhileLoopTree) s;
        Target t = loopTarget();
        Label top = b.newBoundLabel();
        line(s);
        cond(w.getCondition(), t.brk, false);
        stmt(w.getStatement());
        b.labelBinding(t.cont);
        b.goto_(top);
        b.labelBinding(t.brk);
        pop(t);
        return;
      }
      case DO_WHILE_LOOP: {
        DoWhileLoopTree d = (DoWhileLoopTree) s;
        Target t = loopTarget();
        Label top = b.newBoundLabel();
        stmt(d.getStatement());
        b.labelBinding(t.cont);
        line(s);
        cond(d.getCondition(), top, true);
        b.labelBinding(t.brk);
        pop(t);
        return;
      }
      case FOR_LOOP: {
        ForLoopTree f = (ForLoopTree) s;
        for (StatementTree i : f.getInitializer()) {
          stmt(i);
        }
        Target t = loopTarget();
        Label top = b.newBoundLabel();
        line(s);
        if (f.getCondition() != null) {
          cond(f.getCondition(), t.brk, false);
        }
        stmt(f.getStatement());
        b.labelBinding(t.cont);
        for (ExpressionStatementTree u : f.getUpdate()) {
          stmt(u);
        }
        b.goto_(top);
        b.labelBinding(t.brk);
        pop(t);
        return;
      }
      case ENHANCED_FOR_LOOP:
        foreach((EnhancedForLoopTree) s);
        return;
      case LABELED_STATEMENT: {
        LabeledStatementTree l = (LabeledStatementTree) s;
        StatementTree body = l.getStatement();
        String label = l.getLabel().toString();
        Tree.Kind k = body.getKind();
        if (k == Tree.Kind.WHILE_LOOP || k == Tree.Kind.DO_WHILE_LOOP || k == Tree.Kind.FOR_LOOP
            || k == Tree.Kind.ENHANCED_FOR_LOOP) {
          pendingLabel = label;
          stmt(body);
        } else {
          Target t = new Target();
          t.label = label;
          t.brk = b.newLabel();
          t.finDepth = fins.size();
          targets.add(t);
          stmt(body);
          targets.remove(targets.size() - 1);
          b.labelBinding(t.brk);
        }
        return;
      }
      case RETURN:
        returnStmt((ReturnTree) s);
        return;
      case THROW: {
        line(s);
        ExpressionTree e = ((ThrowTree) s).getExpression();
        Ty st1 = expr(e);
        Ty thr = Ty.cls(st.lookup("java/lang/Throwable"));
        coerce(st1, sem(e), thr);
        b.athrow();
        return;
      }
      case BREAK: {
        BreakTree bt = (BreakTree) s;
        Target t = findTarget(bt.getLabel() == null ? null : bt.getLabel().toString(), false);
        jump(t.brk, t.finDepth);
        return;
      }
      case CONTINUE: {
        ContinueTree ct = (ContinueTree) s;
        Target t = findTarget(ct.getLabel() == null ? null : ct.getLabel().toString(), true);
        jump(t.cont, t.finDepth);
        return;
      }
      case SWITCH:
        switchStmt((SwitchTree) s);
        return;
      case YIELD:
        yieldStmt((YieldTree) s);
        return;
      case TRY:
        tryStmt((TryTree) s);
        return;
      case SYNCHRONIZED:
        syncStmt((SynchronizedTree) s);
        return;
      case ASSERT:
        assertStmt((AssertTree) s);
        return;
      case EMPTY_STATEMENT:
        return;
      case CLASS:
      case INTERFACE:
      case ENUM:
      case RECORD:
      case ANNOTATION_TYPE:
        classDecl((ClassTree) s);
        return;
      default:
        throw new IllegalStateException("cannot generate statement " + s.getKind());
    }
  }

  private Target loopTarget() {
    Target t = new Target();
    t.brk = b.newLabel();
    t.cont = b.newLabel();
    t.isLoop = true;
    t.finDepth = fins.size();
    t.label = pendingLabel;
    pendingLabel = null;
    targets.add(t);
    return t;
  }

  void pop(Target t) {
    targets.remove(targets.size() - 1);
  }

  private Target findTarget(String label, boolean cont) {
    for (int i = targets.size() - 1; i >= 0; i--) {
      Target t = targets.get(i);
      if (label == null) {
        if (cont ? t.isLoop : (t.isLoop || t.isSwitch)) {
          return t;
        }
      } else if (label.equals(t.label)) {
        return t;
      }
    }
    throw new IllegalStateException("no jump target for " + label);
  }

  void jump(Label target, int toDepth) {
    unwind(toDepth);
    b.goto_(target);
    endGaps(toDepth);
  }

  void localVar(VariableTree vt) {
    VSym v = (VSym) a.syms.get(vt);
    int slot = declare(v);
    ExpressionTree init = vt.getInitializer();
    if (init != null) {
      line(vt);
      if (init.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) init).getType() == null) {
        expr(init);
      } else {
        genAs(init, v.type);
      }
      store(erase(v.type), slot);
    }
  }

  private void ifStmt(IfTree i) {
    line(i);
    Object c = a.consts.get(Attr.unparen(i.getCondition()));
    if (c instanceof Boolean) {
      if (((Boolean) c).booleanValue()) {
        stmt(i.getThenStatement());
      } else if (i.getElseStatement() != null) {
        stmt(i.getElseStatement());
      }
      return;
    }
    Label elseL = b.newLabel();
    cond(i.getCondition(), elseL, false);
    stmt(i.getThenStatement());
    if (i.getElseStatement() != null) {
      boolean thenCompletes = Flow.canComplete(a, i.getThenStatement());
      Label end = b.newLabel();
      if (thenCompletes) {
        b.goto_(end);
      }
      b.labelBinding(elseL);
      stmt(i.getElseStatement());
      b.labelBinding(end);
    } else {
      b.labelBinding(elseL);
    }
  }

  private void foreach(EnhancedForLoopTree f) {
    line(f);
    ExpressionTree ex = f.getExpression();
    Ty ety = erase(sem(ex));
    VSym v = (VSym) a.syms.get(f.getVariable());
    int vslot = declare(v);
    Target t = loopTarget();
    if (ety.tag == Ty.ARRAY) {
      Ty est = expr(ex);
      if (est.tag != Ty.ARRAY) {
        checkcast(ety);
      }
      int arr = newLocal(ety);
      b.astore(arr);
      int len = newLocal(Ty.tInt);
      b.aload(arr);
      b.arraylength();
      b.istore(len);
      int idx = newLocal(Ty.tInt);
      b.iconst_0();
      b.istore(idx);
      Label top = b.newBoundLabel();
      b.iload(idx);
      b.iload(len);
      b.if_icmpge(t.brk);
      b.aload(arr);
      b.iload(idx);
      Ty el = ety.elem;
      b.arrayLoad(Gen.kind(el));
      coerce(el, sem(ex).tag == Ty.ARRAY ? TypeOps.resolve(sem(ex)).elem : el, v.type);
      store(erase(v.type), vslot);
      stmt(f.getStatement());
      b.labelBinding(t.cont);
      b.iinc(idx, 1);
      b.goto_(top);
    } else {
      Ty est = expr(ex);
      Ty iterable = Ty.cls(st.lookup("java/lang/Iterable"));
      coerce(est, sem(ex), iterable);
      b.invokeinterface(cdi("java/lang/Iterable"), "iterator", mtd("()Ljava/util/Iterator;"));
      int it = newLocal(obj());
      b.astore(it);
      Label top = b.newBoundLabel();
      b.aload(it);
      b.invokeinterface(cdi("java/util/Iterator"), "hasNext", mtd("()Z"));
      b.ifeq(t.brk);
      b.aload(it);
      b.invokeinterface(cdi("java/util/Iterator"), "next", mtd("()Ljava/lang/Object;"));
      Ty elem = (Ty) a.info.get(f);
      coerce(obj(), elem == null ? obj() : TypeOps.resolve(elem), v.type);
      store(erase(v.type), vslot);
      stmt(f.getStatement());
      b.labelBinding(t.cont);
      b.goto_(top);
    }
    b.labelBinding(t.brk);
    pop(t);
  }

  /* ---------- return and yield ---------- */

  private void returnStmt(ReturnTree r) {
    line(r);
    ExpressionTree e = r.getExpression();
    if (e == null) {
      unwind(0);
      b.return_();
      endGaps(0);
      return;
    }
    genAs(e, returnTy);
    Ty rt = erase(returnTy);
    if (!fins.isEmpty()) {
      int tmp = newLocal(rt);
      store(rt, tmp);
      unwind(0);
      load(rt, tmp);
      b.return_(Gen.stackKind(rt));
      endGaps(0);
    } else {
      b.return_(Gen.stackKind(rt));
    }
  }

  private void yieldStmt(YieldTree y) {
    Target t = null;
    for (int i = targets.size() - 1; i >= 0; i--) {
      if (targets.get(i).isSwitchExpr) {
        t = targets.get(i);
        break;
      }
    }
    if (t == null) {
      throw new IllegalStateException("yield outside switch expression");
    }
    line(y);
    genAs(y.getValue(), t.resultTy);
    Ty rt = erase(t.resultTy);
    if (fins.size() > t.finDepth) {
      int tmp = newLocal(rt);
      store(rt, tmp);
      unwind(t.finDepth);
      load(rt, tmp);
      b.goto_(t.brk);
      endGaps(t.finDepth);
    } else {
      b.goto_(t.brk);
    }
  }

  /* ---------- finalizers ---------- */

  void unwind(int toDepth) {
    List<Fin> saved = fins;
    for (int i = saved.size() - 1; i >= toDepth; i--) {
      Fin f = saved.get(i);
      Label gs = b.newBoundLabel();
      f.gapStarts.add(gs);
      f.open = true;
      fins = new ArrayList<Fin>(saved.subList(0, i));
      f.emitter.run();
      fins = saved;
    }
  }

  void endGaps(int toDepth) {
    for (int i = fins.size() - 1; i >= toDepth; i--) {
      Fin f = fins.get(i);
      if (f.open) {
        f.gapEnds.add(b.newBoundLabel());
        f.open = false;
        b.nop();
      }
    }
  }

  /** Registers an exception handler over [start, end) excluding gaps [from, to) of the given finalizer. */
  void registerRange(Label start, Label end, Label handler, ClassDesc type, Fin f, int from, int to) {
    Label cur = start;
    if (f != null) {
      for (int i = from; i < to && i < f.gapStarts.size() && i < f.gapEnds.size(); i++) {
        addCatch(cur, f.gapStarts.get(i), handler, type);
        cur = f.gapEnds.get(i);
      }
    }
    addCatch(cur, end, handler, type);
  }

  void addCatch(Label s, Label e, Label h, ClassDesc type) {
    if (type == null) {
      b.exceptionCatchAll(s, e, h);
    } else {
      b.exceptionCatch(s, e, h, type);
    }
  }

  /* ---------- try ---------- */

  private void tryStmt(TryTree t) {
    line(t);
    if (t.getResources().isEmpty()) {
      plainTry(t, new Runnable() {
        public void run() {
          stmt(t0(t));
        }
      });
      return;
    }
    if (t.getCatches().isEmpty() && t.getFinallyBlock() == null) {
      resources(t.getResources(), 0, t.getBlock());
      return;
    }
    plainTry(t, new Runnable() {
      public void run() {
        resources(t.getResources(), 0, t.getBlock());
      }
    });
  }

  private static BlockTree t0(TryTree t) {
    return t.getBlock();
  }

  private void plainTry(final TryTree t, Runnable body) {
    final BlockTree fin = t.getFinallyBlock();
    List<? extends CatchTree> catches = t.getCatches();
    Label start = b.newBoundLabel();
    b.nop();
    Label bodyEnd = b.newLabel();
    Label exit = b.newLabel();
    final Fin f = new Fin();
    f.emitter = new Runnable() {
      public void run() {
        if (fin != null) {
          stmt(fin);
        }
      }
    };
    fins.add(f);
    body.run();
    b.labelBinding(bodyEnd);
    int bodyGaps = f.gapStarts.size();
    boolean bodyC = Flow.canComplete(a, t.getBlock());
    if (bodyC) {
      fins.remove(fins.size() - 1);
      f.emitter.run();
      fins.add(f);
      b.goto_(exit);
    }
    List<Label> handlerLabels = new ArrayList<Label>();
    List<Label> handlerEnds = new ArrayList<Label>();
    List<int[]> handlerGaps = new ArrayList<int[]>();
    List<Label> handlerStarts = new ArrayList<Label>();
    for (CatchTree c : catches) {
      Label h = b.newLabel();
      Label hEnd = b.newLabel();
      handlerLabels.add(h);
      handlerEnds.add(hEnd);
      VSym v = (VSym) a.syms.get(c.getParameter());
      List<Ty> alts = new ArrayList<Ty>();
      Tree pt = c.getParameter().getType();
      if (pt.getKind() == Tree.Kind.UNION_TYPE) {
        for (Tree alt : ((UnionTypeTree) pt).getTypeAlternatives()) {
          alts.add(erase(a.typeOf(alt)));
        }
      } else {
        alts.add(erase(v.type));
      }
      for (Ty alt : alts) {
        registerRange(start, bodyEnd, h, cd(alt), f, 0, bodyGaps);
      }
      b.labelBinding(h);
      int slot = declare(v);
      b.astore(slot);
      int g0 = f.gapStarts.size();
      Label hs = b.newBoundLabel();
      b.nop();
      stmt(c.getBlock());
      b.labelBinding(hEnd);
      handlerGaps.add(new int[] {g0, f.gapStarts.size()});
      handlerStarts.add(hs);
      if (Flow.canComplete(a, c.getBlock())) {
        fins.remove(fins.size() - 1);
        f.emitter.run();
        fins.add(f);
        b.goto_(exit);
      }
    }
    fins.remove(fins.size() - 1);
    if (fin != null) {
      Label all = b.newLabel();
      registerRange(start, bodyEnd, all, null, f, 0, bodyGaps);
      for (int i = 0; i < handlerLabels.size(); i++) {
        registerRange(handlerStarts.get(i), handlerEnds.get(i), all, null, f, handlerGaps.get(i)[0],
            handlerGaps.get(i)[1]);
      }
      b.labelBinding(all);
      int ex = newLocal(obj());
      b.astore(ex);
      f.emitter.run();
      b.aload(ex);
      b.athrow();
    }
    b.labelBinding(exit);
  }

  private void resources(final List<? extends Tree> res, final int i, final BlockTree body) {
    if (i == res.size()) {
      stmt(body);
      return;
    }
    Tree r = res.get(i);
    final int slot;
    final Ty rty;
    boolean nonNull = false;
    if (r instanceof VariableTree) {
      VariableTree vt = (VariableTree) r;
      VSym v = (VSym) a.syms.get(vt);
      slot = declare(v);
      rty = erase(v.type);
      ExpressionTree init = vt.getInitializer();
      genAs(init, v.type);
      store(rty, slot);
      nonNull = Attr.unparen(init).getKind() == Tree.Kind.NEW_CLASS;
    } else {
      ExpressionTree e = (ExpressionTree) r;
      rty = erase(sem(e));
      slot = newLocal(rty);
      Ty st1 = expr(e);
      coerce(st1, sem(e), rty);
      store(rty, slot);
    }
    final boolean nn = nonNull;
    Label start = b.newBoundLabel();
    b.nop();
    Label bodyEnd = b.newLabel();
    Label exit = b.newLabel();
    final Fin f = new Fin();
    f.emitter = new Runnable() {
      public void run() {
        closeResource(slot, rty, nn);
      }
    };
    fins.add(f);
    resources(res, i + 1, body);
    b.labelBinding(bodyEnd);
    fins.remove(fins.size() - 1);
    if (Flow.canComplete(a, body)) {
      closeResource(slot, rty, nn);
      b.goto_(exit);
    }
    Label handler = b.newLabel();
    registerRange(start, bodyEnd, handler, cdi("java/lang/Throwable"), f, 0, f.gapStarts.size());
    b.labelBinding(handler);
    int t = newLocal(obj());
    b.astore(t);
    Label skip = b.newLabel();
    if (!nn) {
      b.aload(slot);
      b.ifnull(skip);
    }
    Label cs = b.newBoundLabel();
    b.nop();
    callClose(slot, rty);
    Label ce = b.newBoundLabel();
    b.goto_(skip);
    Label ch = b.newLabel();
    b.exceptionCatch(cs, ce, ch, cdi("java/lang/Throwable"));
    b.labelBinding(ch);
    int x = newLocal(obj());
    b.astore(x);
    b.aload(t);
    b.aload(x);
    b.invokevirtual(cdi("java/lang/Throwable"), "addSuppressed", mtd("(Ljava/lang/Throwable;)V"));
    b.labelBinding(skip);
    b.aload(t);
    b.athrow();
    b.labelBinding(exit);
  }

  private void closeResource(int slot, Ty rty, boolean nonNull) {
    Label skip = b.newLabel();
    if (!nonNull) {
      b.aload(slot);
      b.ifnull(skip);
    }
    callClose(slot, rty);
    b.labelBinding(skip);
  }

  private void callClose(int slot, Ty rty) {
    b.aload(slot);
    Ty owner = rty;
    boolean iface = rty.tag == Ty.CLASS && rty.sym.isInterface();
    if (rty.tag == Ty.CLASS) {
      MSym close = null;
      for (MSym m : g.a.res.methodsOf(rty, "close")) {
        if (m.params.isEmpty()) {
          close = m;
          break;
        }
      }
      if (close != null) {
        emitInvoke(close, ownerFor(close, close.cls(), rty, true), false);
        Ty ret = erase(close.ret);
        if (ret.tag != Ty.VOID) {
          popValue(ret);
        }
        return;
      }
    }
    b.invokeinterface(cdi("java/lang/AutoCloseable"), "close", mtd("()V"));
  }

  private void syncStmt(SynchronizedTree s) {
    line(s);
    Ty lt = expr(s.getExpression());
    b.dup();
    final int lock = newLocal(obj());
    b.astore(lock);
    b.monitorenter();
    Label start = b.newBoundLabel();
    b.nop();
    Label bodyEnd = b.newLabel();
    Label exit = b.newLabel();
    final Fin f = new Fin();
    f.emitter = new Runnable() {
      public void run() {
        b.aload(lock);
        b.monitorexit();
      }
    };
    fins.add(f);
    stmt(s.getBlock());
    b.labelBinding(bodyEnd);
    fins.remove(fins.size() - 1);
    if (Flow.canComplete(a, s.getBlock())) {
      f.emitter.run();
      b.goto_(exit);
    }
    Label all = b.newLabel();
    registerRange(start, bodyEnd, all, null, f, 0, f.gapStarts.size());
    b.labelBinding(all);
    int ex = newLocal(obj());
    b.astore(ex);
    b.aload(lock);
    b.monitorexit();
    b.aload(ex);
    b.athrow();
    b.labelBinding(exit);
  }

  private void assertStmt(AssertTree s) {
    line(s);
    cg.needsAssertField = true;
    Label end = b.newLabel();
    b.getstatic(cdi(cls.internal), "$assertionsDisabled", ClassDesc.ofDescriptor("Z"));
    b.ifne(end);
    cond(s.getCondition(), end, true);
    b.new_(cdi("java/lang/AssertionError"));
    b.dup();
    ExpressionTree d = s.getDetail();
    if (d == null) {
      b.invokespecial(cdi("java/lang/AssertionError"), "<init>", mtd("()V"));
    } else {
      Ty t = a.unboxedOrSelf(sem(d));
      String ds;
      if (sem(d).isPrim()) {
        switch (t.tag) {
          case Ty.BOOLEAN:
            ds = "Z";
            break;
          case Ty.CHAR:
            ds = "C";
            break;
          case Ty.LONG:
            ds = "J";
            break;
          case Ty.FLOAT:
            ds = "F";
            break;
          case Ty.DOUBLE:
            ds = "D";
            break;
          default:
            ds = "I";
            break;
        }
        genAs(d, ds.equals("I") ? Ty.tInt : t);
      } else {
        ds = "Ljava/lang/Object;";
        genAs(d, obj());
      }
      b.invokespecial(cdi("java/lang/AssertionError"), "<init>", mtd("(" + ds + ")V"));
    }
    b.athrow();
    b.labelBinding(end);
  }
}
