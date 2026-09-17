/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.attribute.AnnotationDefaultAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

/** Annotation emission. */
final class AnnotationGen {
  private AnnotationGen() {
  }

  static final class Result {
    final List<Annotation> visible = new ArrayList<Annotation>();
    final List<Annotation> invisible = new ArrayList<Annotation>();
  }

  static void classAnnotations(ClassGen cg, CSym c) {
    Result r = build(cg, c.tree.getModifiers().getAnnotations(), cg.g.enter.classEnv(c));
    apply(cg.cb, r);
  }

  static void fieldAnnotations(ClassGen cg, FSym f, FieldBuilder fb) {
    if (f.tree == null) {
      return;
    }
    Result r = build(cg, f.tree.getModifiers().getAnnotations(), cg.g.enter.classEnv(f.cls()));
    if (!r.visible.isEmpty()) {
      fb.with(RuntimeVisibleAnnotationsAttribute.of(r.visible));
    }
    if (!r.invisible.isEmpty()) {
      fb.with(RuntimeInvisibleAnnotationsAttribute.of(r.invisible));
    }
  }

  static void methodAnnotations(ClassGen cg, MSym m, MethodBuilder mb) {
    if (m.tree == null) {
      return;
    }
    Env env = cg.g.enter.classEnv(m.cls());
    Result r = build(cg, m.tree.getModifiers().getAnnotations(), env);
    if (!r.visible.isEmpty()) {
      mb.with(RuntimeVisibleAnnotationsAttribute.of(r.visible));
    }
    if (!r.invisible.isEmpty()) {
      mb.with(RuntimeInvisibleAnnotationsAttribute.of(r.invisible));
    }
    if (m.tree.getParameters() != null && !m.tree.getParameters().isEmpty()) {
      List<List<Annotation>> vis = new ArrayList<List<Annotation>>();
      List<List<Annotation>> inv = new ArrayList<List<Annotation>>();
      boolean anyV = false;
      boolean anyI = false;
      for (VariableTree p : m.tree.getParameters()) {
        Result pr = build(cg, p.getModifiers().getAnnotations(), env);
        vis.add(pr.visible);
        inv.add(pr.invisible);
        anyV |= !pr.visible.isEmpty();
        anyI |= !pr.invisible.isEmpty();
      }
      if (anyV) {
        mb.with(java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute.of(vis));
      }
      if (anyI) {
        mb.with(java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute.of(inv));
      }
    }
    if (m.tree.getDefaultValue() instanceof ExpressionTree) {
      try {
        Env menv = env.dup();
        menv.isClassEnv = false;
        menv.staticCtx = true;
        AnnotationValue v = value(cg, (ExpressionTree) m.tree.getDefaultValue(), m.ret, menv);
        if (v != null) {
          mb.with(AnnotationDefaultAttribute.of(v));
        }
      } catch (RuntimeException e) {
        // an unsupported default value is skipped
      }
    }
  }

  private static void apply(ClassBuilder cb, Result r) {
    if (!r.visible.isEmpty()) {
      cb.with(RuntimeVisibleAnnotationsAttribute.of(r.visible));
    }
    if (!r.invisible.isEmpty()) {
      cb.with(RuntimeInvisibleAnnotationsAttribute.of(r.invisible));
    }
  }

  private static CSym containerOf(ClassGen cg, CSym type, Env env) {
    if (type.reflect != null) {
      java.lang.annotation.Repeatable rp = type.reflect.getAnnotation(java.lang.annotation.Repeatable.class);
      return rp == null ? null : cg.g.st.reflected(rp.value());
    }
    if (type.tree != null) {
      for (AnnotationTree t : type.tree.getModifiers().getAnnotations()) {
        if (t.getAnnotationType().toString().endsWith("Repeatable") && !t.getArguments().isEmpty()) {
          ExpressionTree arg = t.getArguments().get(0);
          if (arg instanceof AssignmentTree) {
            arg = ((AssignmentTree) arg).getExpression();
          }
          if (arg instanceof MemberSelectTree) {
            Ty ct = cg.g.enter.resolveType(((MemberSelectTree) arg).getExpression(), cg.g.enter.classEnv(type));
            return ct.tag == Ty.CLASS ? ct.sym : null;
          }
        }
      }
    }
    return null;
  }

  private static void groupRepeated(ClassGen cg, Result r, List<Annotation> list, Env env) {
    java.util.Map<String, List<Annotation>> by = new java.util.LinkedHashMap<String, List<Annotation>>();
    for (Annotation an : list) {
      String k = an.className().stringValue();
      List<Annotation> l = by.get(k);
      if (l == null) {
        l = new ArrayList<Annotation>();
        by.put(k, l);
      }
      l.add(an);
    }
    List<Annotation> out = new ArrayList<Annotation>();
    for (java.util.Map.Entry<String, List<Annotation>> e : by.entrySet()) {
      if (e.getValue().size() == 1) {
        out.add(e.getValue().get(0));
        continue;
      }
      String d = e.getKey();
      CSym t = cg.g.st.lookup(d.substring(1, d.length() - 1));
      CSym cont = t == null ? null : containerOf(cg, t, env);
      if (cont == null) {
        out.addAll(e.getValue());
        continue;
      }
      List<AnnotationValue> vs = new ArrayList<AnnotationValue>();
      for (Annotation an : e.getValue()) {
        vs.add(AnnotationValue.ofAnnotation(an));
      }
      out.add(Annotation.of(ClassDesc.ofInternalName(cont.internal),
          AnnotationElement.of("value", AnnotationValue.ofArray(vs))));
    }
    list.clear();
    list.addAll(out);
  }

  static Result build(ClassGen cg, List<? extends AnnotationTree> anns, Env env) {
    Result r = build0(cg, anns, env);
    groupRepeated(cg, r, r.visible, env);
    groupRepeated(cg, r, r.invisible, env);
    return r;
  }

  static Result build0(ClassGen cg, List<? extends AnnotationTree> anns, Env env) {
    Result r = new Result();
    for (AnnotationTree at : anns) {
      try {
        Ty t = cg.g.enter.resolveType(at.getAnnotationType(), env);
        if (t.tag != Ty.CLASS) {
          continue;
        }
        int ret = retention(cg, t.sym);
        if (ret == 0) {
          continue;
        }
        Annotation an = annotation(cg, at, t.sym, env);
        if (ret == 2) {
          r.visible.add(an);
        } else {
          r.invisible.add(an);
        }
      } catch (RuntimeException e) {
        // annotations that cannot be evaluated are skipped
      }
    }
    return r;
  }

  /** 0 source, 1 class, 2 runtime. */
  static int retention(ClassGen cg, CSym a) {
    if (a.reflect != null) {
      java.lang.annotation.Retention r = a.reflect.getAnnotation(java.lang.annotation.Retention.class);
      if (r == null) {
        return 1;
      }
      switch (r.value()) {
        case SOURCE:
          return 0;
        case RUNTIME:
          return 2;
        default:
          return 1;
      }
    }
    if (a.tree != null) {
      for (AnnotationTree t : a.tree.getModifiers().getAnnotations()) {
        if (t.toString().contains("Retention")) {
          String s = t.toString();
          if (s.contains("RUNTIME")) {
            return 2;
          }
          if (s.contains("SOURCE")) {
            return 0;
          }
          return 1;
        }
      }
    }
    return 1;
  }

  static Annotation annotation(ClassGen cg, AnnotationTree at, CSym type, Env env) {
    List<AnnotationElement> els = new ArrayList<AnnotationElement>();
    for (ExpressionTree arg : at.getArguments()) {
      String name = "value";
      ExpressionTree v = arg;
      if (arg instanceof AssignmentTree) {
        name = ((AssignmentTree) arg).getVariable().toString();
        v = ((AssignmentTree) arg).getExpression();
      }
      Ty et = Ty.tError;
      for (MSym m : type.methodsNamed(name)) {
        et = m.ret;
      }
      AnnotationValue av = value(cg, v, et, env);
      if (av != null) {
        els.add(AnnotationElement.of(name, av));
      }
    }
    return Annotation.of(ClassDesc.ofInternalName(type.internal), els);
  }

  static AnnotationValue value(ClassGen cg, ExpressionTree e, Ty et, Env env) {
    Attr a = cg.a;
    if (e instanceof AnnotationTree) {
      Ty t = cg.g.enter.resolveType(((AnnotationTree) e).getAnnotationType(), env);
      return AnnotationValue.ofAnnotation(annotation(cg, (AnnotationTree) e, t.sym, env));
    }
    if (et.tag == Ty.ARRAY) {
      List<AnnotationValue> vs = new ArrayList<AnnotationValue>();
      if (e.getKind() == Tree.Kind.NEW_ARRAY) {
        for (ExpressionTree i : ((NewArrayTree) e).getInitializers()) {
          vs.add(value(cg, i, et.elem, env));
        }
      } else {
        vs.add(value(cg, e, et.elem, env));
      }
      return AnnotationValue.ofArray(vs);
    }
    Ty t = a.expr(e, env, et);
    Object c = a.consts.get(e);
    if (et.isPrim()) {
      switch (et.tag) {
        case Ty.BOOLEAN:
          return AnnotationValue.ofBoolean(((Boolean) c).booleanValue());
        case Ty.BYTE:
          return AnnotationValue.ofByte((byte) ConstFold.intOf(c));
        case Ty.SHORT:
          return AnnotationValue.ofShort((short) ConstFold.intOf(c));
        case Ty.CHAR:
          return AnnotationValue.ofChar((char) ConstFold.intOf(c));
        case Ty.INT:
          return AnnotationValue.ofInt(ConstFold.intOf(c));
        case Ty.LONG:
          return AnnotationValue.ofLong(ConstFold.longOf(c));
        case Ty.FLOAT:
          return AnnotationValue.ofFloat(ConstFold.floatOf(c));
        default:
          return AnnotationValue.ofDouble(ConstFold.doubleOf(c));
      }
    }
    if (et.isString()) {
      return AnnotationValue.ofString((String) c);
    }
    if (et.tag == Ty.CLASS && et.sym.internal.equals("java/lang/Class")) {
      Tree q = ((MemberSelectTree) e).getExpression();
      Ty qt = a.types.get(q);
      return AnnotationValue.ofClass(ClassDesc.ofDescriptor(TypeOps.erasure(TypeOps.resolve(qt), a.st.object)
          .desc()));
    }
    Sym s = a.symOf(Attr.unparen(e));
    if (s instanceof FSym) {
      FSym f = (FSym) s;
      return AnnotationValue.ofEnum(ClassDesc.ofInternalName(f.cls().internal), f.name);
    }
    return null;
  }
}
