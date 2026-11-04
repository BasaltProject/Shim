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
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Method-level code generation: bodies, constructors, lambdas, switches and patterns. */
final class Code extends CodeStmt {
  Code(ClassGen cg, CodeBuilder b, MSym method) {
    super(cg, b, method);
  }

  /* ---------- methods ---------- */

  void genMethod(MSym m, MethodTree tree) {
    method = m;
    isStatic = m.isStatic();
    nextSlot = isStatic ? 0 : 1;
    if (m.isCtor()) {
      genCtor(m, tree);
      return;
    }
    if (tree == null) {
      genGenerated(m);
      return;
    }
    returnTy = m.ret;
    List<? extends VariableTree> ps = tree.getParameters();
    for (VariableTree p : ps) {
      declare((VSym) a.syms.get(p));
    }
    BlockTree body = tree.getBody();
    line(tree);
    stmt(body);
    if (Flow.canComplete(a, body)) {
      b.return_();
    }
  }

  private void genGenerated(MSym m) {
    String self = cls.internal;
    if (cls.isEnum() && m.name.equals("values")) {
      b.getstatic(cdi(self), "$VALUES", ClassDesc.ofDescriptor("[L" + self + ";"));
      b.invokevirtual(ClassDesc.ofDescriptor("[L" + self + ";"), "clone", mtd("()Ljava/lang/Object;"));
      b.checkcast(ClassDesc.ofDescriptor("[L" + self + ";"));
      b.areturn();
    } else if (cls.isEnum() && m.name.equals("valueOf")) {
      b.ldc(cdi(self));
      b.aload(0);
      b.invokestatic(cdi("java/lang/Enum"), "valueOf", mtd("(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"));
      b.checkcast(cdi(self));
      b.areturn();
    } else if (cls.isRecord() && m.origin instanceof FSym) {
      FSym f = (FSym) m.origin;
      b.aload(0);
      b.getfield(cdi(self), f.name, cd(f.type));
      b.return_(Gen.stackKind(erase(f.type)));
    } else if (cls.isRecord()) {
      genRecordObjectMethod(m);
    } else {
      throw new IllegalStateException("cannot generate " + m.name);
    }
  }

  private void genRecordObjectMethod(MSym m) {
    String self = cls.internal;
    List<ConstDescHolder> args = new ArrayList<ConstDescHolder>();
    StringBuilder names = new StringBuilder();
    List<ConstantDesc> bs = new ArrayList<ConstantDesc>();
    bs.add(cdi(self));
    for (FSym f : cls.recordComponents) {
      if (names.length() > 0) {
        names.append(';');
      }
      names.append(f.name);
    }
    bs.add(names.toString());
    for (FSym f : cls.recordComponents) {
      bs.add(MethodHandleDesc.ofField(DirectMethodHandleDesc.Kind.GETTER, cdi(self), f.name, cd(f.type)));
    }
    DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
        cdi("java/lang/runtime/ObjectMethods"), "bootstrap", mtd("(Ljava/lang/invoke/MethodHandles$Lookup;"
            + "Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;"
            + "[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;"));
    String desc;
    b.aload(0);
    if (m.name.equals("equals")) {
      b.aload(1);
      desc = "(L" + self + ";Ljava/lang/Object;)Z";
    } else if (m.name.equals("hashCode")) {
      desc = "(L" + self + ";)I";
    } else {
      desc = "(L" + self + ";)Ljava/lang/String;";
    }
    b.invokedynamic(DynamicCallSiteDesc.of(bsm, m.name, mtd(desc), bs.toArray(new ConstantDesc[0])));
    String r = m.name.equals("equals") ? "Z" : m.name.equals("hashCode") ? "I" : "L";
    if (r.equals("L")) {
      b.areturn();
    } else {
      b.ireturn();
    }
  }

  static final class ConstDescHolder {
  }

  /* ---------- constructors ---------- */

  private void genCtor(MSym m, MethodTree tree) {
    Gen.Layout l = g.layout(cls, m);
    nextSlot = 1;
    if (l.enumPrefix) {
      nextSlot = 3;
    }
    if (l.outer != null) {
      outerSlot = nextSlot++;
    }
    List<Integer> paramSlots = new ArrayList<Integer>();
    if (tree != null && tree.getParameters() != null) {
      for (VariableTree p : tree.getParameters()) {
        VSym v = (VSym) a.syms.get(p);
        paramSlots.add(Integer.valueOf(declare(v)));
      }
    } else {
      for (Ty p : l.declared) {
        paramSlots.add(Integer.valueOf(nextSlot));
        nextSlot += erase(p).width();
      }
    }
    for (VSym v : l.captured) {
      slots.put(v, Integer.valueOf(nextSlot));
      nextSlot += erase(v.type).width();
    }
    prologueFields(l);
    returnTy = Ty.tVoid;
    List<? extends StatementTree> stmts = tree != null && tree.getBody() != null ? tree.getBody().getStatements()
        : Collections.<StatementTree>emptyList();
    int start = 0;
    boolean thisCall = false;
    int callAt = -1;
    for (int i = 0; i < stmts.size() && callAt < 0; i++) {
      if (isCtorCall(stmts.get(i))) {
        callAt = i;
      }
    }
    if (callAt >= 0) {
      for (int i = 0; i < callAt; i++) {
        stmt(stmts.get(i));
      }
      Object ci = a.info.get(((ExpressionStatementTree) stmts.get(callAt)).getExpression());
      thisCall = ci instanceof AttrCall.CallInfo && ((AttrCall.CallInfo) ci).thisCall;
      prologue = true;
      stmt(stmts.get(callAt));
      prologue = false;
      start = callAt + 1;
    } else {
      implicitSuper(tree);
    }
    if (!thisCall) {
      instanceInit();
    }
    for (int i = start; i < stmts.size(); i++) {
      stmt(stmts.get(i));
    }
    boolean compact = tree != null && (m.flags & Sym.COMPACT_CTOR) != 0;
    if (cls.isRecord() && (tree == null || compact)) {
      for (int i = 0; i < cls.recordComponents.size(); i++) {
        FSym f = cls.recordComponents.get(i);
        b.aload(0);
        load(erase(f.type), paramSlots.get(i).intValue());
        b.putfield(cdi(cls.internal), f.name, cd(f.type));
      }
    }
    b.return_();
  }

  private static boolean isCtorCall(StatementTree s) {
    if (!(s instanceof ExpressionStatementTree)) {
      return false;
    }
    ExpressionTree e = ((ExpressionStatementTree) s).getExpression();
    if (!(e instanceof MethodInvocationTree)) {
      return false;
    }
    ExpressionTree sel = ((MethodInvocationTree) e).getMethodSelect();
    if (sel instanceof IdentifierTree) {
      String n = ((IdentifierTree) sel).getName().toString();
      return n.equals("this") || n.equals("super");
    }
    return false;
  }

  void prologueFields(Gen.Layout l) {
    if (l.outer != null) {
      b.aload(0);
      b.aload(outerSlot);
      b.putfield(cdi(cls.internal), Gen.outerFieldName(cls), cd(l.outer));
    }
    for (VSym v : l.captured) {
      b.aload(0);
      load(erase(v.type), slots.get(v).intValue());
      b.putfield(cdi(cls.internal), "val$" + v.name, cd(erase(v.type)));
    }
  }

  private void implicitSuper(MethodTree tree) {
    if (cls == st.objectSym) {
      return;
    }
    if (cls.isEnum()) {
      b.aload(0);
      b.aload(1);
      b.iload(2);
      b.invokespecial(cdi("java/lang/Enum"), "<init>", mtd("(Ljava/lang/String;I)V"));
      return;
    }
    if (cls.isRecord()) {
      b.aload(0);
      b.invokespecial(cdi("java/lang/Record"), "<init>", mtd("()V"));
      return;
    }
    Ty sc = cls.superclass();
    AttrCall.CallInfo ci = a.call.implicitSupers.get(tree != null ? (Tree) tree : cls.tree);
    b.aload(0);
    if (ci == null || sc == null) {
      b.invokespecial(cdi("java/lang/Object"), "<init>", mtd("()V"));
      return;
    }
    CSym sup = ci.m.cls();
    Gen.Layout sl = g.layout(sup, ci.m);
    if (sl.outer != null) {
      ctorOuterFor(sup);
    }
    ctorSuffix(sl);
    b.invokespecial(cd(sup), "<init>", mtd(sl.desc()));
  }

  void genAnonCtor(Gen.Layout l) {
    method = null;
    lambdaOwnerName = "new";
    isStatic = false;
    nextSlot = 1;
    if (l.enumPrefix) {
      nextSlot = 3;
    }
    if (l.outer != null) {
      outerSlot = nextSlot++;
    }
    List<Integer> declSlots = new ArrayList<Integer>();
    for (Ty p : l.declared) {
      declSlots.add(Integer.valueOf(nextSlot));
      nextSlot += erase(p).width();
    }
    for (VSym v : l.captured) {
      slots.put(v, Integer.valueOf(nextSlot));
      nextSlot += erase(v.type).width();
    }
    prologueFields(l);
    Ty sc = cls.superclass();
    NewClassTree nc = (NewClassTree) cls.attrData;
    AttrCall.CallInfo ci = (AttrCall.CallInfo) a.info.get(nc);
    CSym sup = sc.sym;
    b.aload(0);
    if (sup.isEnum()) {
      b.aload(1);
      b.iload(2);
    }
    MSym sm = ci.m;
    Gen.Layout sl = g.layout(sup, sm);
    int di = 0;
    if (sup != st.objectSym && sl.outer != null) {
      b.aload(declSlots.get(0).intValue());
      di = 1;
    }
    if (sup != st.objectSym) {
      for (int i = 0; i < sm.params.size(); i++) {
        Ty pt = erase(sm.params.get(i));
        load(pt, declSlots.get(di + i).intValue());
      }
    }
    ctorSuffix(sl);
    if (sup == st.objectSym) {
      b.invokespecial(cdi("java/lang/Object"), "<init>", mtd("()V"));
    } else {
      b.invokespecial(cd(sup), "<init>", mtd(sl.desc()));
    }
    returnTy = Ty.tVoid;
    instanceInit();
    b.return_();
  }

  void instanceInit() {
    for (Tree m : cls.tree.getMembers()) {
      if (m instanceof VariableTree) {
        VariableTree vt = (VariableTree) m;
        FSym f = (FSym) g.enter.symOf.get(vt);
        if (f == null || f.isStatic() || vt.getInitializer() == null) {
          continue;
        }
        line(vt);
        b.aload(0);
        initValue(vt.getInitializer(), f.type);
        b.putfield(cdi(cls.internal), f.name, cd(f.type));
      } else if (m instanceof BlockTree && !((BlockTree) m).isStatic()) {
        stmt((BlockTree) m);
      }
    }
  }

  private void initValue(ExpressionTree init, Ty type) {
    if (init.getKind() == Tree.Kind.NEW_ARRAY && ((NewArrayTree) init).getType() == null) {
      expr(init);
    } else {
      genAs(init, type);
    }
  }

  void genClinit() {
    method = null;
    isStatic = true;
    nextSlot = 0;
    returnTy = Ty.tVoid;
    if (cg.needsAssertField || AssertScan.hasAssert(a, cls)) {
      cg.needsAssertField = true;
      Label t = b.newLabel();
      Label e = b.newLabel();
      b.ldc(cdi(cls.outermost().internal));
      b.invokevirtual(cdi("java/lang/Class"), "desiredAssertionStatus", mtd("()Z"));
      b.ifne(t);
      b.iconst_1();
      b.goto_(e);
      b.labelBinding(t);
      b.iconst_0();
      b.labelBinding(e);
      b.putstatic(cdi(cls.internal), "$assertionsDisabled", ClassDesc.ofDescriptor("Z"));
    }
    boolean valuesDone = !cls.isEnum();
    int ordinal = 0;
    for (Tree m : cls.tree.getMembers()) {
      if (m instanceof VariableTree) {
        VariableTree vt = (VariableTree) m;
        FSym f = (FSym) g.enter.symOf.get(vt);
        if (f == null) {
          continue;
        }
        boolean enumConst = (f.flags & Sym.ENUM) != 0;
        if (!enumConst && !valuesDone) {
          emitValues();
          valuesDone = true;
        }
        if (enumConst) {
          line(vt);
          enumConstant(f, vt, ordinal++);
        } else if (f.isStatic() && vt.getInitializer() != null && cg.staticInitFields.contains(f)) {
          line(vt);
          initValue(vt.getInitializer(), f.type);
          b.putstatic(cdi(cls.internal), f.name, cd(f.type));
        }
      } else if (m instanceof BlockTree && ((BlockTree) m).isStatic()) {
        if (!valuesDone) {
          emitValues();
          valuesDone = true;
        }
        stmt((BlockTree) m);
      }
    }
    if (!valuesDone) {
      emitValues();
    }
    b.return_();
  }

  private void emitValues() {
    String self = cls.internal;
    b.invokestatic(cdi(self), "$values", mtd("()[L" + self + ";"));
    b.putstatic(cdi(self), "$VALUES", ClassDesc.ofDescriptor("[L" + self + ";"));
  }

  private void enumConstant(FSym f, VariableTree vt, int ordinal) {
    NewClassTree nc = (NewClassTree) vt.getInitializer();
    AttrCall.CallInfo ci = (AttrCall.CallInfo) a.info.get(nc);
    CSym target = ci.anon != null ? ci.anon : cls;
    Gen.Layout l = ci.anon != null ? g.layout(ci.anon, null) : g.layout(cls, ci.m);
    b.new_(cd(target));
    b.dup();
    b.ldc(f.name);
    pushInt(ordinal);
    genArgs(ci, nc.getArguments(), ci.m);
    b.invokespecial(cd(target), "<init>", mtd(l.desc()));
    b.putstatic(cdi(cls.internal), f.name, ClassDesc.ofDescriptor("L" + cls.internal + ";"));
  }

  void classDecl(ClassTree c) {
  }

  /* ---------- lambdas ---------- */

  private int lambdaCounter() {
    String n = enclosingName();
    Integer c = cg.lambdaCounts.get(n);
    int v = c == null ? 0 : c.intValue();
    cg.lambdaCounts.put(n, Integer.valueOf(v + 1));
    return v;
  }

  String lambdaOwnerName;

  private String enclosingName() {
    if (lambdaOwnerName != null) {
      return lambdaOwnerName;
    }
    if (method != null) {
      return method.isCtor() ? "new" : method.name;
    }
    return "static";
  }

  Ty lambdaExpr(final LambdaExpressionTree l) {
    line(l);
    AttrCall.LambdaInfo li = (AttrCall.LambdaInfo) a.info.get(l);
    final Ty target = TypeOps.resolve(li.target);
    final Tx.FnType fn = tx.fnType(target);
    Captures.Free f = g.caps.scan(l);
    final List<VSym> cap = new ArrayList<VSym>();
    boolean usesThis = f.usesThis;
    List<VSym> refs = new ArrayList<VSym>(f.referenced);
    for (CSym lc : f.localClasses) {
      if (!f.declaredClasses.contains(lc)) {
        for (VSym v : g.caps.of(lc)) {
          if (!f.declared.contains(v) && !refs.contains(v)) {
            refs.add(v);
          }
        }
      }
    }
    for (VSym v : refs) {
      if (f.declared.contains(v)) {
        continue;
      }
      if (slots.containsKey(v)) {
        cap.add(v);
      } else {
        usesThis = true;
      }
    }
    final boolean inst = usesThis && !isStatic;
    final String ownerName = enclosingName();
    final String name = cg.lambdaNames.containsKey(l) ? cg.lambdaNames.get(l)
        : "lambda$" + ownerName + "$" + lambdaCounter();
    final List<Ty> ptypes = new ArrayList<Ty>();
    StringBuilder desc = new StringBuilder("(");
    for (VSym v : cap) {
      Ty t = erase(v.type);
      desc.append(t.desc());
    }
    final List<VSym> params = new ArrayList<VSym>();
    for (VariableTree p : l.getParameters()) {
      VSym v = (VSym) a.syms.get(p);
      params.add(v);
      desc.append(erase(v.type).desc());
    }
    final Ty retTy = TypeOps.resolve(fn.ret);
    desc.append(')').append(retTy.tag == Ty.VOID ? "V" : erase(retTy).desc());
    final String mdesc = desc.toString();
    final List<VSym> capF = cap;
    cg.pending.add(new Runnable() {
      public void run() {
        int flags = 0x1002 | (inst ? 0 : 0x8);
        cg.cb.withMethod(name, mtd(mdesc), flags, new java.util.function.Consumer<java.lang.classfile.MethodBuilder>() {
          public void accept(java.lang.classfile.MethodBuilder mb) {
            mb.withCode(new java.util.function.Consumer<CodeBuilder>() {
              public void accept(CodeBuilder cb2) {
                Code c = new Code(cg, cb2, null);
                c.lambdaOwnerName = ownerName;
                c.isStatic = !inst;
                c.nextSlot = inst ? 1 : 0;
                for (VSym v : capF) {
                  c.slots.put(v, Integer.valueOf(c.nextSlot));
                  c.nextSlot += erase(v.type).width();
                }
                for (VSym v : params) {
                  c.declare(v);
                }
                c.returnTy = retTy;
                c.genLambdaBody(l, retTy);
              }
            });
          }
        });
      }
    });
    // invokedynamic
    StringBuilder inv = new StringBuilder("(");
    if (inst) {
      b.aload(0);
      inv.append("L").append(cls.internal).append(";");
    }
    for (VSym v : cap) {
      loadVar(v);
      inv.append(erase(v.type).desc());
    }
    Ty fiErased = erase(target);
    inv.append(")").append(fiErased.desc());
    lambdaTarget = target;
    indyLambda(fn, inv.toString(), cdi(cls.internal), name, mdesc, inst);
    return fiErased;
  }

  private void indyLambda(Tx.FnType fn, String invDesc, ClassDesc owner, String implName, String implDesc,
      boolean inst) {
    StringBuilder inst2 = new StringBuilder("(");
    for (Ty p : fn.params) {
      inst2.append(erase(p).desc());
    }
    Ty r = TypeOps.resolve(fn.ret);
    inst2.append(")").append(r.tag == Ty.VOID ? "V" : erase(r).desc());
    DirectMethodHandleDesc.Kind kind = forcedKind != null ? forcedKind : inst ? (cls.isInterface()
        ? DirectMethodHandleDesc.Kind.INTERFACE_SPECIAL : DirectMethodHandleDesc.Kind.SPECIAL)
        : (cls.isInterface() ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC);
    forcedKind = null;
    DirectMethodHandleDesc impl = MethodHandleDesc.ofMethod(kind, owner, implName, mtd(implDesc));
    Ty serial = Ty.cls(st.lookup("java/io/Serializable"));
    boolean serializable = false;
    List<Ty> markers = new ArrayList<Ty>();
    Ty lt = lambdaTarget == null ? null : TypeOps.resolve(lambdaTarget);
    if (lt != null) {
      List<Ty> comps = new ArrayList<Ty>();
      if (lt.tag == Ty.INTER) {
        comps.addAll(java.util.Arrays.asList(lt.args));
      } else {
        comps.add(lt);
      }
      for (Ty c : comps) {
        Ty e = erase(c);
        if (e.tag != Ty.CLASS) {
          continue;
        }
        if (tx.isSubtype(e, serial)) {
          serializable = true;
        }
        if (tx.fnType(c) == null || (lt.tag == Ty.INTER && tx.fnType(c).method != fn.method)) {
          if (!e.sym.internal.equals("java/io/Serializable")) {
            markers.add(e);
          }
        }
      }
    }
    if (!serializable && markers.isEmpty()) {
      DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
          cdi("java/lang/invoke/LambdaMetafactory"), "metafactory", mtd("(Ljava/lang/invoke/MethodHandles$Lookup;"
              + "Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;"
              + "Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"));
      b.invokedynamic(DynamicCallSiteDesc.of(bsm, fn.method.name, mtd(invDesc), mtd(fn.method.erasedDesc()), impl,
          mtd(inst2.toString())));
      return;
    }
    DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
        cdi("java/lang/invoke/LambdaMetafactory"), "altMetafactory", mtd("(Ljava/lang/invoke/MethodHandles$Lookup;"
            + "Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"));
    List<ConstantDesc> args = new ArrayList<ConstantDesc>();
    args.add(mtd(fn.method.erasedDesc()));
    args.add(impl);
    args.add(mtd(inst2.toString()));
    int flags = (serializable ? 1 : 0) | (markers.isEmpty() ? 0 : 2);
    args.add(Integer.valueOf(flags));
    if (!markers.isEmpty()) {
      args.add(Integer.valueOf(markers.size()));
      for (Ty m : markers) {
        args.add(cd(m));
      }
    }
    DynamicCallSiteDesc site = DynamicCallSiteDesc.of(bsm, fn.method.name, mtd(invDesc),
        args.toArray(new ConstantDesc[0]));
    b.invokedynamic(site);
    if (serializable) {
      ClassGen.SerialLambda sl = new ClassGen.SerialLambda();
      sl.site = site;
      sl.implName = implName;
      sl.implClass = owner.descriptorString().substring(1, owner.descriptorString().length() - 1);
      sl.implDesc = implDesc;
      sl.implKind = kind;
      sl.ifaceClass = cdi(erase(lt == null ? Ty.cls(fn.method.cls()) : lt.tag == Ty.INTER ? lt.args[0] : lt)
          .sym.internal).descriptorString();
      sl.ifaceMethod = fn.method.name;
      sl.ifaceSig = fn.method.erasedDesc();
      sl.invDesc = invDesc;
      cg.serialLambdas.add(sl);
    }
  }

  private Ty lambdaTarget;
  private DirectMethodHandleDesc.Kind forcedKind;

  void genLambdaBody(LambdaExpressionTree l, Ty retTy) {
    line(l);
    if (l.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
      ExpressionTree e = (ExpressionTree) l.getBody();
      if (retTy.tag == Ty.VOID) {
        genDiscard(e);
        b.return_();
      } else {
        genAs(e, retTy);
        b.return_(Gen.stackKind(erase(retTy)));
      }
    } else {
      BlockTree body = (BlockTree) l.getBody();
      stmt(body);
      if (Flow.canComplete(a, body)) {
        b.return_();
      }
    }
  }

  /* ---------- method references ---------- */

  Ty mrefExpr(MemberReferenceTree mr) {
    line(mr);
    final AttrCall.RefInfo ri = (AttrCall.RefInfo) a.info.get(mr);
    final Ty target = TypeOps.resolve(ri.target);
    final Tx.FnType fn = tx.fnType(target);
    Ty directed = directRef(mr, ri, fn, target);
    if (directed != null) {
      return directed;
    }
    final String enclosingNameF = enclosingName();
    final String name = "lambda$" + enclosingNameF + "$ref" + cg.refCount++;
    final List<Ty> fparams = new ArrayList<Ty>();
    for (Ty p : fn.params) {
      fparams.add(TypeOps.resolve(p));
    }
    final Ty retTy = TypeOps.resolve(fn.ret);
    boolean inst = ri.kind == AttrCall.REF_SUPER || ri.kind == AttrCall.REF_CTOR && ri.call != null
        && ri.call.m != null && Captures.needsOuterThis(ri.call.m.cls()) && !isStatic;
    Ty recvTy = null;
    final ExpressionTree qual = mr.getQualifierExpression();
    StringBuilder inv = new StringBuilder("(");
    if (inst) {
      b.aload(0);
      inv.append("L").append(cls.internal).append(";");
    }
    final List<VSym> capVars = new ArrayList<VSym>();
    if (ri.kind == AttrCall.REF_CTOR && ri.call != null && ri.call.m != null) {
      CSym tc = ri.call.m.cls();
      if (tc.local || tc.anon) {
        capVars.addAll(g.caps.of(tc));
      }
    }
    for (VSym v : capVars) {
      loadVar(v);
      inv.append(erase(v.type).desc());
    }
    if (ri.kind == AttrCall.REF_BOUND) {
      Ty q = expr(qual);
      recvTy = erase(sem(qual));
      coerce(q, sem(qual), recvTy);
      b.dup();
      b.invokestatic(cdi("java/util/Objects"), "requireNonNull", mtd("(Ljava/lang/Object;)Ljava/lang/Object;"));
      b.pop();
      inv.append(recvTy.desc());
    }
    final Ty rt = recvTy;
    StringBuilder desc = new StringBuilder("(");
    for (VSym v : capVars) {
      desc.append(erase(v.type).desc());
    }
    if (rt != null) {
      desc.append(rt.desc());
    }
    for (Ty p : fparams) {
      desc.append(erase(p).desc());
    }
    desc.append(")").append(retTy.tag == Ty.VOID ? "V" : erase(retTy).desc());
    final String mdesc = desc.toString();
    final boolean fi = inst;
    cg.pending.add(new Runnable() {
      public void run() {
        int flags = 0x1002 | (fi ? 0 : 0x8);
        cg.cb.withMethod(name, mtd(mdesc), flags, new java.util.function.Consumer<java.lang.classfile.MethodBuilder>() {
          public void accept(java.lang.classfile.MethodBuilder mb) {
            mb.withCode(new java.util.function.Consumer<CodeBuilder>() {
              public void accept(CodeBuilder cb2) {
                Code c = new Code(cg, cb2, null);
                c.lambdaOwnerName = enclosingNameF;
                c.isStatic = !fi;
                c.nextSlot = fi ? 1 : 0;
                c.genRefBody(ri, fparams, retTy, rt, qual, capVars);
              }
            });
          }
        });
      }
    });
    Ty fiErased = erase(target);
    inv.append(")").append(fiErased.desc());
    lambdaTarget = target;
    indyLambda(fn, inv.toString(), cdi(cls.internal), name, mdesc, inst);
    return fiErased;
  }

  /** Emits a method reference as a direct method handle when no adaptation lambda is needed. */
  private Ty directRef(MemberReferenceTree mr, AttrCall.RefInfo ri, Tx.FnType fn, Ty target) {
    AttrCall.CallInfo ci = ri.call;
    if (ci == null || ci.m == null || ci.varargs || ri.kind == AttrCall.REF_SUPER || ri.kind
        == AttrCall.REF_ARRAY_CTOR) {
      return null;
    }
    MSym m = ci.m;
    if (m.isPrivate() || (m.flags & Sym.PROTECTED) != 0 || (m.flags & Sym.PUBLIC) == 0 && !m.cls().pkg.equals(cls.pkg)) {
      return null;
    }
    if (target.tag == Ty.INTER) {
      return null;
    }
    Ty owner;
    DirectMethodHandleDesc.Kind kind;
    if (ri.kind == AttrCall.REF_CTOR) {
      CSym k = m.cls();
      if (k.isAbstract() || k.isInner() || k.local || k.anon || k.isEnum() || k.isInterface()) {
        return null;
      }
      owner = Ty.cls(k);
      kind = DirectMethodHandleDesc.Kind.CONSTRUCTOR;
    } else {
      owner = ownerFor(m, m.cls(), ri.kind == AttrCall.REF_STATIC ? ri.qualType : ci.site, true);
      if (owner.tag != Ty.CLASS) {
        return null;
      }
      boolean iface = owner.sym.isInterface();
      kind = m.isStatic() ? (iface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC
          : DirectMethodHandleDesc.Kind.STATIC) : (iface ? DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL
          : DirectMethodHandleDesc.Kind.VIRTUAL);
      if (!m.isStatic() && !m.isCtor() && m.cls() == st.objectSym && iface) {
        return null;
      }
    }
    if (!(owner.sym.flags == (owner.sym.flags | 0)) || (owner.sym.flags & Sym.PUBLIC) == 0
        && !owner.sym.pkg.equals(cls.pkg)) {
      return null;
    }
    StringBuilder inv = new StringBuilder("(");
    if (ri.kind == AttrCall.REF_BOUND) {
      ExpressionTree q = mr.getQualifierExpression();
      Ty recv = erase(sem(q));
      Ty qs = expr(q);
      coerce(qs, sem(q), owner);
      b.dup();
      b.invokestatic(cdi("java/util/Objects"), "requireNonNull", mtd("(Ljava/lang/Object;)Ljava/lang/Object;"));
      b.pop();
      inv.append(owner.desc());
    } else if (ri.kind == AttrCall.REF_UNBOUND) {
      Ty p0 = erase(TypeOps.resolve(fn.params.get(0)));
      if (!tx.isSubtype(p0, owner)) {
        return null;
      }
    }
    Ty fiErased = erase(target);
    inv.append(")").append(fiErased.desc());
    String desc = m.isCtor() ? g.ctorDesc(m) : m.erasedDesc();
    forcedKind = kind;
    lambdaTarget = target;
    indyLambda(fn, inv.toString(), cd(owner), m.isCtor() ? "<init>" : m.name, desc, false);
    return fiErased;
  }

  /** Body of the synthetic method that implements a method reference. */
  void genRefBody(AttrCall.RefInfo ri, List<Ty> fparams, Ty retTy, Ty recvTy, ExpressionTree qual,
      List<VSym> capVars) {
    returnTy = retTy;
    for (VSym v : capVars) {
      slots.put(v, Integer.valueOf(nextSlot));
      nextSlot += erase(v.type).width();
    }
    List<Integer> ps = new ArrayList<Integer>();
    int recvSlot = -1;
    if (recvTy != null) {
      recvSlot = nextSlot;
      nextSlot += recvTy.width();
    }
    for (Ty p : fparams) {
      ps.add(Integer.valueOf(nextSlot));
      nextSlot += erase(p).width();
    }
    AttrCall.CallInfo ci = ri.call;
    Ty result;
    if (ri.kind == AttrCall.REF_ARRAY_CTOR) {
      Ty at = erase(ri.arrayType);
      load(erase(fparams.get(0)), ps.get(0).intValue());
      coerce(erase(fparams.get(0)), fparams.get(0), Ty.tInt);
      newArrayOf(at.elem);
      result = at;
    } else if (ri.kind == AttrCall.REF_CTOR) {
      CSym target = ci.m.cls();
      Gen.Layout l = g.layout(target, ci.m);
      b.new_(cd(target));
      b.dup();
      if (l.outer != null) {
        outerThisForRef(target);
      }
      pushRefArgs(ci, fparams, ps, 0);
      for (VSym v : l.captured) {
        loadVar(v);
      }
      b.invokespecial(cd(target), "<init>", mtd(l.desc()));
      result = target.erasedType();
    } else {
      MSym m = ci.m;
      int argStart = 0;
      Ty owner;
      boolean special = false;
      if (ri.kind == AttrCall.REF_STATIC) {
        owner = ownerFor(m, m.cls(), ri.qualType, true);
      } else if (ri.kind == AttrCall.REF_BOUND) {
        owner = ownerFor(m, m.cls(), ci.site, true);
        load(recvTy, recvSlot);
        coerce(recvTy, recvTy, owner);
      } else if (ri.kind == AttrCall.REF_SUPER) {
        owner = ri.superSite != null ? erase(ri.superSite) : erase(cls.superclass() == null ? obj() : cls.superclass());
        b.aload(0);
        special = true;
      } else {
        owner = ownerFor(m, m.cls(), ci.site, true);
        Ty p0 = fparams.get(0);
        load(erase(p0), ps.get(0).intValue());
        coerce(erase(p0), p0, owner);
        argStart = 1;
      }
      pushRefArgs(ci, fparams, ps, argStart);
      emitInvoke(m, owner, special);
      result = m.isCtor() ? Ty.tVoid : owner.tag == Ty.ARRAY && m.name.equals("clone") ? obj() : erase(m.ret);
      if (retTy.tag == Ty.VOID) {
        popValue(result);
        b.return_();
        return;
      }
      coerce(result, ci.ret, retTy);
      b.return_(Gen.stackKind(erase(retTy)));
      return;
    }
    if (retTy.tag == Ty.VOID) {
      popValue(result);
      b.return_();
      return;
    }
    coerce(result, ci != null && ci.ret != null ? ci.ret : result, retTy);
    b.return_(Gen.stackKind(erase(retTy)));
  }

  private void outerThisForRef(CSym target) {
    if (Captures.needsOuterThis(target) && target.outerClass != null) {
      b.aload(0);
      CSym k = cls;
      while (!g.a.res.isSubClass(k, target.outerClass)) {
        b.getfield(cd(k), Gen.outerFieldName(k), cd(Ty.cls(k.outerClass)));
        k = k.outerClass;
      }
    }
  }

  private void pushRefArgs(AttrCall.CallInfo ci, List<Ty> fparams, List<Integer> ps, int from) {
    List<Ty> formals = ci.formals;
    int n = fparams.size() - from;
    if (!ci.varargs) {
      for (int i = 0; i < n; i++) {
        Ty p = fparams.get(from + i);
        load(erase(p), ps.get(from + i).intValue());
        coerce(erase(p), p, formals.get(i));
      }
      return;
    }
    int fixed = ci.fixed;
    for (int i = 0; i < fixed; i++) {
      Ty p = fparams.get(from + i);
      load(erase(p), ps.get(from + i).intValue());
      coerce(erase(p), p, formals.get(i));
    }
    Ty elem = ci.varargElem == null ? obj() : TypeOps.resolve(ci.varargElem);
    int cnt = n - fixed;
    pushInt(cnt);
    newArrayOf(erase(elem));
    for (int j = 0; j < cnt; j++) {
      b.dup();
      pushInt(j);
      Ty p = fparams.get(from + fixed + j);
      load(erase(p), ps.get(from + fixed + j).intValue());
      coerce(erase(p), p, elem);
      b.arrayStore(Gen.kind(erase(elem)));
    }
  }

  /* ---------- switch ---------- */

  void switchStmt(SwitchTree s) {
    line(s);
    genSwitch(s, s.getExpression(), s.getCases(), false, null);
  }

  Ty switchExprGen(SwitchExpressionTree s) {
    line(s);
    Ty rt = sem(s);
    genSwitch(s, s.getExpression(), s.getCases(), true, rt);
    return erase(rt);
  }

  private void throwMatchException() {
    b.new_(cdi("java/lang/MatchException"));
    b.dup();
    b.aconst_null();
    b.aconst_null();
    b.invokespecial(cdi("java/lang/MatchException"), "<init>", mtd("(Ljava/lang/String;Ljava/lang/Throwable;)V"));
    b.athrow();
  }

  private void genSwitch(Tree sw, ExpressionTree selector, List<? extends CaseTree> cases, boolean isExpr, Ty resTy) {
    AttrStat.SwitchData sd = (AttrStat.SwitchData) a.syms.get(sw);
    Target t = new Target();
    t.brk = b.newLabel();
    t.isSwitch = true;
    t.isSwitchExpr = isExpr;
    t.resultTy = resTy;
    t.finDepth = fins.size();
    t.label = pendingLabel;
    pendingLabel = null;
    targets.add(t);
    boolean arrow = !cases.isEmpty() && cases.get(0).getCaseKind() == CaseTree.CaseKind.RULE;
    int n = cases.size();
    Label[] bodyL = new Label[n];
    for (int i = 0; i < n; i++) {
      bodyL[i] = b.newLabel();
    }
    Label defaultL = b.newLabel();
    boolean hasDefault = false;
    for (CaseTree c : cases) {
      for (CaseLabelTree cl : c.getLabels()) {
        if (cl instanceof DefaultCaseLabelTree) {
          hasDefault = true;
        }
      }
    }
    boolean patternKind = sd.hasPattern || sd.hasNull;
    Label[] failL = new Label[n];
    Label top = null;
    Map<Integer, Integer> switchLabelIndex = null;
    Ty switchSelType = null;
    int restartSlot = -1;
    int selSlot = -1;
    if (patternKind) {
      top = patternDispatch(selector, cases, sd, bodyL, defaultL, failL);
      switchLabelIndex = labelIndexOfCase;
      switchSelType = patternSelType;
      restartSlot = patternRestart;
      selSlot = patternSel;
    } else if (sd.isString) {
      stringDispatch(selector, cases, bodyL, defaultL);
    } else if (sd.isEnum) {
      enumDispatch(selector, cases, sd, bodyL, defaultL);
    } else {
      intDispatch(selector, cases, bodyL, defaultL);
    }
    if (hasDefault) {
      b.labelBinding(defaultL);
      for (int i = 0; i < n; i++) {
        for (CaseLabelTree cl : cases.get(i).getLabels()) {
          if (cl instanceof DefaultCaseLabelTree) {
            b.goto_(bodyL[i]);
            i = n;
            break;
          }
        }
      }
    }
    int[] caseLabelStart = new int[n];
    for (int i = 0; i < n; i++) {
      CaseTree c = cases.get(i);
      b.labelBinding(bodyL[i]);
      Env dummy = null;
      if (patternKind) {
        patternSelType = switchSelType;
        patternPrologue(c, i, selSlot, failL[i], restartSlot, top, switchLabelIndex);
      }
      if (arrow) {
        Tree body = c.getBody();
        if (body instanceof ExpressionTree) {
          ExpressionTree be = (ExpressionTree) body;
          line(be);
          if (isExpr) {
            genAs(be, resTy);
          } else {
            genDiscard(be);
          }
          b.goto_(t.brk);
        } else if (body instanceof BlockTree) {
          stmt((BlockTree) body);
          if (Flow.canComplete(a, (BlockTree) body)) {
            b.goto_(t.brk);
          }
        } else {
          stmt((StatementTree) body);
          if (Flow.canComplete(a, (StatementTree) body)) {
            b.goto_(t.brk);
          }
        }
      } else if (c.getStatements() != null) {
        for (StatementTree st1 : c.getStatements()) {
          stmt(st1);
          if (!Flow.canComplete(a, st1) && st1.getKind() != Tree.Kind.VARIABLE) {
            break;
          }
        }
      }
      if (patternKind && failL[i] != null) {
        // jumps for guard failure are emitted at the end of the case via failL; handled in patternPrologue
      }
    }
    boolean needDefaultBody = !hasDefault;
    if (!hasDefault) {
      b.labelBinding(defaultL);
      if (isExpr || sd.exhaustive) {
        throwMatchException();
      } else {
        b.goto_(t.brk);
      }
    }
    targets.remove(targets.size() - 1);
    b.labelBinding(t.brk);
  }

  private void emitIntSwitch(TreeMap<Integer, Label> keys, Label dflt) {
    if (keys.isEmpty()) {
      b.pop();
      b.goto_(dflt);
      return;
    }
    int lo = keys.firstKey().intValue();
    int hi = keys.lastKey().intValue();
    long tableSpace = 4L + ((long) hi - lo + 1);
    long tableTime = 3;
    long lookupSpace = 3L + 2L * keys.size();
    long lookupTime = keys.size();
    List<SwitchCase> cs = new ArrayList<SwitchCase>();
    if (tableSpace + 3 * tableTime <= lookupSpace + 3 * lookupTime) {
      for (int k = lo; k <= hi; k++) {
        Label l = keys.get(Integer.valueOf(k));
        cs.add(SwitchCase.of(k, l == null ? dflt : l));
      }
      b.tableswitch(lo, hi, dflt, cs);
    } else {
      for (Map.Entry<Integer, Label> e : keys.entrySet()) {
        cs.add(SwitchCase.of(e.getKey().intValue(), e.getValue()));
      }
      b.lookupswitch(dflt, cs);
    }
  }

  private void intDispatch(ExpressionTree selector, List<? extends CaseTree> cases, Label[] bodyL, Label defaultL) {
    Ty selTy = sem(selector);
    Ty prim = a.unboxedOrSelf(selTy);
    genAs(selector, Ty.tInt);
    TreeMap<Integer, Label> keys = new TreeMap<Integer, Label>();
    Label dflt = defaultL;
    for (int i = 0; i < cases.size(); i++) {
      for (CaseLabelTree cl : cases.get(i).getLabels()) {
        if (cl instanceof ConstantCaseLabelTree) {
          Object cv = a.consts.get(((ConstantCaseLabelTree) cl).getConstantExpression());
          keys.put(Integer.valueOf(ConstFold.intOf(cv)), bodyL[i]);
        } else if (cl instanceof DefaultCaseLabelTree) {
          dflt = bodyL[i];
        }
      }
    }
    emitIntSwitch(keys, dflt);
  }

  private void enumDispatch(ExpressionTree selector, List<? extends CaseTree> cases, AttrStat.SwitchData sd,
      Label[] bodyL, Label defaultL) {
    Ty et = erase(sd.selectorType);
    Ty est = expr(selector);
    coerce(est, sem(selector), et);
    b.invokevirtual(cd(et), "ordinal", mtd("()I"));
    TreeMap<Integer, Label> keys = new TreeMap<Integer, Label>();
    Label dflt = defaultL;
    List<FSym> consts = ClassGen.enumConstants(et.sym);
    for (int i = 0; i < cases.size(); i++) {
      for (CaseLabelTree cl : cases.get(i).getLabels()) {
        if (cl instanceof ConstantCaseLabelTree) {
          Sym s = a.symOf(((ConstantCaseLabelTree) cl).getConstantExpression());
          keys.put(Integer.valueOf(consts.indexOf(s)), bodyL[i]);
        } else if (cl instanceof DefaultCaseLabelTree) {
          dflt = bodyL[i];
        }
      }
    }
    emitIntSwitch(keys, dflt);
  }

  private void stringDispatch(ExpressionTree selector, List<? extends CaseTree> cases, Label[] bodyL,
      Label defaultL) {
    Ty est = expr(selector);
    coerce(est, sem(selector), st.string);
    int s = newLocal(obj());
    b.astore(s);
    int idx = newLocal(Ty.tInt);
    b.iconst_m1();
    b.istore(idx);
    TreeMap<Integer, List<Object[]>> byHash = new TreeMap<Integer, List<Object[]>>();
    Label dflt = defaultL;
    TreeMap<Integer, Label> second = new TreeMap<Integer, Label>();
    for (int i = 0; i < cases.size(); i++) {
      for (CaseLabelTree cl : cases.get(i).getLabels()) {
        if (cl instanceof ConstantCaseLabelTree) {
          String str = (String) a.consts.get(((ConstantCaseLabelTree) cl).getConstantExpression());
          Integer h = Integer.valueOf(str.hashCode());
          List<Object[]> l = byHash.get(h);
          if (l == null) {
            l = new ArrayList<Object[]>();
            byHash.put(h, l);
          }
          l.add(new Object[] {str, Integer.valueOf(i)});
          second.put(Integer.valueOf(i), bodyL[i]);
        } else if (cl instanceof DefaultCaseLabelTree) {
          dflt = bodyL[i];
        }
      }
    }
    Label afterFirst = b.newLabel();
    b.aload(s);
    b.invokevirtual(cdi("java/lang/String"), "hashCode", mtd("()I"));
    TreeMap<Integer, Label> hashLabels = new TreeMap<Integer, Label>();
    for (Integer h : byHash.keySet()) {
      hashLabels.put(h, b.newLabel());
    }
    emitIntSwitch(hashLabels, afterFirst);
    for (Map.Entry<Integer, List<Object[]>> e : byHash.entrySet()) {
      b.labelBinding(hashLabels.get(e.getKey()));
      for (Object[] p : e.getValue()) {
        Label next = b.newLabel();
        b.aload(s);
        b.ldc((String) p[0]);
        b.invokevirtual(cdi("java/lang/String"), "equals", mtd("(Ljava/lang/Object;)Z"));
        b.ifeq(next);
        pushInt(((Integer) p[1]).intValue());
        b.istore(idx);
        b.goto_(afterFirst);
        b.labelBinding(next);
      }
      b.goto_(afterFirst);
    }
    b.labelBinding(afterFirst);
    b.iload(idx);
    emitIntSwitch(second, dflt);
  }

  /* ---------- pattern switch ---------- */

  Ty patternSelType;
  int patternRestart;
  int patternSel;

  private Label patternDispatch(ExpressionTree selector, List<? extends CaseTree> cases, AttrStat.SwitchData sd,
      Label[] bodyL, Label defaultL, Label[] failL) {
    Ty selTy = erase(sd.selectorType);
    Ty est = expr(selector);
    Ty selBox = selTy.isPrim() ? tx.box(selTy) : selTy;
    coerce(est, sem(selector), selBox);
    patternSelType = selBox;
    int sel = newLocal(obj());
    b.astore(sel);
    int restart = newLocal(Ty.tInt);
    patternSel = sel;
    patternRestart = restart;
    b.iconst_0();
    b.istore(restart);
    List<ConstantDesc> labels = new ArrayList<ConstantDesc>();
    List<Integer> caseOfLabel = new ArrayList<Integer>();
    Label nullL = null;
    Label dflt = defaultL;
    for (int i = 0; i < cases.size(); i++) {
      CaseTree c = cases.get(i);
      for (CaseLabelTree cl : c.getLabels()) {
        if (cl instanceof DefaultCaseLabelTree) {
          dflt = bodyL[i];
        } else if (cl instanceof PatternCaseLabelTree) {
          PatternTree p = ((PatternCaseLabelTree) cl).getPattern();
          Ty pt = erase(a.typeOf(p));
          labels.add(cdi(pt.tag == Ty.CLASS ? pt.sym.internal : "java/lang/Object").equals(null) ? null : cd(pt));
          caseOfLabel.add(Integer.valueOf(i));
          failL[i] = b.newLabel();
        } else if (cl instanceof ConstantCaseLabelTree) {
          ExpressionTree ce = ((ConstantCaseLabelTree) cl).getConstantExpression();
          if (ce.getKind() == Tree.Kind.NULL_LITERAL) {
            nullL = bodyL[i];
            continue;
          }
          Object cv = a.consts.get(ce);
          Sym cs = a.symOf(ce);
          if (cs instanceof FSym && (((FSym) cs).flags & Sym.ENUM) != 0) {
            FSym ef = (FSym) cs;
            labels.add(java.lang.Enum.EnumDesc.of(cdi(ef.cls().internal), ef.name));
          } else if (cv instanceof String) {
            labels.add((String) cv);
          } else {
            labels.add(Integer.valueOf(ConstFold.intOf(cv)));
          }
          caseOfLabel.add(Integer.valueOf(i));
        }
      }
    }
    top = b.newBoundLabel();
    if (nullL == null) {
      b.aload(sel);
      b.invokestatic(cdi("java/util/Objects"), "requireNonNull", mtd("(Ljava/lang/Object;)Ljava/lang/Object;"));
      b.pop();
    }
    b.aload(sel);
    b.iload(restart);
    DirectMethodHandleDesc bsm = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
        cdi("java/lang/runtime/SwitchBootstraps"), "typeSwitch", mtd("(Ljava/lang/invoke/MethodHandles$Lookup;"
            + "Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"));
    b.invokedynamic(DynamicCallSiteDesc.of(bsm, "typeSwitch", mtd("(Ljava/lang/Object;I)I"),
        labels.toArray(new ConstantDesc[0])));
    TreeMap<Integer, Label> keys = new TreeMap<Integer, Label>();
    if (nullL != null) {
      keys.put(Integer.valueOf(-1), nullL);
    }
    for (int li = 0; li < caseOfLabel.size(); li++) {
      keys.put(Integer.valueOf(li), bodyL[caseOfLabel.get(li).intValue()]);
    }
    labelIndexOfCase = new LinkedHashMap<Integer, Integer>();
    for (int li = 0; li < caseOfLabel.size(); li++) {
      if (!labelIndexOfCase.containsKey(caseOfLabel.get(li))) {
        labelIndexOfCase.put(caseOfLabel.get(li), Integer.valueOf(li));
      }
    }
    emitIntSwitch(keys, dflt);
    return top;
  }

  Label top;
  Map<Integer, Integer> labelIndexOfCase;

  private void patternPrologue(CaseTree c, int caseIdx, int selSlot, Label fail, int restartSlot, Label topL,
      Map<Integer, Integer> labelIndexOfCase) {
    PatternTree pat = null;
    for (CaseLabelTree cl : c.getLabels()) {
      if (cl instanceof PatternCaseLabelTree) {
        pat = ((PatternCaseLabelTree) cl).getPattern();
      }
    }
    if (pat == null) {
      return;
    }
    Label failHere = fail;
    patternMatch(pat, selSlot, obj(), patternSelType, failHere, false);
    if (c.getGuard() != null) {
      cond(c.getGuard(), failHere, false);
    }
    // continue into the case body; on failure restart after this label
    Label ok = b.newLabel();
    b.goto_(ok);
    b.labelBinding(failHere);
    Integer li = labelIndexOfCase.get(Integer.valueOf(caseIdx));
    pushInt(li.intValue() + 1);
    b.istore(restartSlot);
    b.goto_(topL);
    b.labelBinding(ok);
  }

  /* ---------- patterns ---------- */

  void patternMatch(PatternTree p, int slot, Ty slotTy, Ty semTy, Label fail, boolean topLevel) {
    if (p instanceof BindingPatternTree) {
      VSym v = (VSym) a.syms.get(((BindingPatternTree) p).getVariable());
      Ty vt = erase(v.type);
      if (slotTy.isPrim()) {
        int vs = declare(v);
        load(slotTy, slot);
        convertPrim(slotTy, vt);
        store(vt, vs);
        return;
      }
      boolean total = tx.isSubtype(erase(semTy == null ? slotTy : semTy), vt) && !vt.isPrim();
      if (vt.isPrim()) {
        int vs = declare(v);
        b.aload(slot);
        coerce(slotTy, semTy, vt);
        store(vt, vs);
        return;
      }
      if (!total) {
        b.aload(slot);
        b.instanceOf(cd(vt));
        b.ifeq(fail);
      } else if (topLevel) {
        b.aload(slot);
        b.ifnull(fail);
      }
      int vs = declare(v);
      b.aload(slot);
      if (!subtypeErased(slotTy, vt)) {
        checkcast(vt);
      }
      b.astore(vs);
      return;
    }
    if (p instanceof DeconstructionPatternTree) {
      DeconstructionPatternTree d = (DeconstructionPatternTree) p;
      Ty rt = TypeOps.resolve(a.typeOf(p));
      Ty R = erase(rt);
      CSym rc = R.sym;
      rc.members();
      b.aload(slot);
      b.instanceOf(cd(R));
      b.ifeq(fail);
      int tmp = newLocal(obj());
      b.aload(slot);
      b.checkcast(cd(R));
      b.astore(tmp);
      List<? extends PatternTree> nested = d.getNestedPatterns();
      for (int i = 0; i < nested.size() && i < rc.recordComponents.size(); i++) {
        FSym comp = rc.recordComponents.get(i);
        Ty ct = erase(comp.type);
        b.aload(tmp);
        b.invokevirtual(cd(R), comp.name, mtd("()" + ct.desc()));
        int cs = newLocal(ct);
        store(ct, cs);
        Ty inst = tx.fieldType(comp, rt);
        patternMatchNested(nested.get(i), cs, ct, TypeOps.resolve(inst), fail);
      }
      return;
    }
  }

  private void patternMatchNested(PatternTree p, int slot, Ty slotTy, Ty semTy, Label fail) {
    if (p instanceof BindingPatternTree) {
      VSym v = (VSym) a.syms.get(((BindingPatternTree) p).getVariable());
      Ty vt = erase(v.type);
      if (slotTy.isPrim() || vt.isPrim()) {
        int vs = declare(v);
        load(slotTy, slot);
        coerce(slotTy, semTy, vt);
        store(vt, vs);
        return;
      }
      boolean total = tx.isSubtype(erase(semTy), vt);
      if (!total) {
        b.aload(slot);
        b.instanceOf(cd(vt));
        b.ifeq(fail);
      }
      int vs = declare(v);
      b.aload(slot);
      if (!subtypeErased(slotTy, vt)) {
        checkcast(vt);
      }
      b.astore(vs);
      return;
    }
    patternMatch(p, slot, slotTy, semTy, fail, false);
  }

  void instanceOfPatternCond(InstanceOfTree io, Label target, boolean jumpIfTrue) {
    Tree pat = io.getPattern();
    ExpressionTree ex = io.getExpression();
    if (pat == null) {
      Ty t = erase(a.types.get(io.getType()) == null ? Ty.tError : TypeOps.resolve(a.types.get(io.getType())));
      Ty st1 = expr(ex);
      b.instanceOf(cd(t));
      if (jumpIfTrue) {
        b.ifne(target);
      } else {
        b.ifeq(target);
      }
      return;
    }
    Ty et = erase(sem(ex));
    Ty est = expr(ex);
    int tmp = newLocal(obj());
    coerce(est, sem(ex), et.isPrim() ? tx.box(et) : et);
    b.astore(tmp);
    PatternTree p = (PatternTree) pat;
    if (jumpIfTrue) {
      Label fail = b.newLabel();
      patternMatch(p, tmp, obj(), sem(ex), fail, true);
      b.goto_(target);
      b.labelBinding(fail);
    } else {
      patternMatch(p, tmp, obj(), sem(ex), target, true);
    }
  }
}
