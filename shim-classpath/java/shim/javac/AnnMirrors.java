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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/** Annotation mirrors for source elements. */
final class AnnMirrors {
  private AnnMirrors() {
  }

  static List<AnnotationMirror> of(Sym s) {
    Attr a = s instanceof CSym ? attrOf((CSym) s) : s.owner instanceof CSym ? attrOf((CSym) s.owner) : null;
    ModifiersTree mods = null;
    CSym ctx = null;
    if (s instanceof CSym && ((CSym) s).tree != null) {
      mods = ((CSym) s).tree.getModifiers();
      ctx = (CSym) s;
    } else if (s instanceof MSym && ((MSym) s).tree != null) {
      mods = ((MSym) s).tree.getModifiers();
      ctx = ((MSym) s).cls();
    } else if (s instanceof FSym && ((FSym) s).tree != null) {
      mods = ((FSym) s).tree.getModifiers();
      ctx = ((FSym) s).cls();
    } else if (s instanceof VSym && ((VSym) s).tree != null) {
      mods = ((VSym) s).tree.getModifiers();
    }
    if (mods == null || a == null || mods.getAnnotations().isEmpty()) {
      return Collections.emptyList();
    }
    Env env = ctx != null ? a.enter.classEnv(ctx) : null;
    if (env == null) {
      return Collections.emptyList();
    }
    List<AnnotationMirror> out = new ArrayList<AnnotationMirror>();
    for (AnnotationTree t : mods.getAnnotations()) {
      AnnotationMirror m = mirror(a, t, env);
      if (m != null) {
        out.add(m);
      }
    }
    return out;
  }

  private static Attr attrOf(CSym c) {
    return c.enterRef == null ? null : c.enterRef.attrRef;
  }

  static AnnotationMirror mirror(Attr a, AnnotationTree t, Env env) {
    Ty at = a.enter.resolveType(t.getAnnotationType(), env);
    if (at.tag != Ty.CLASS) {
      return null;
    }
    final Map<ExecutableElement, AnnotationValue> vals = new LinkedHashMap<ExecutableElement, AnnotationValue>();
    for (ExpressionTree arg : t.getArguments()) {
      String name = "value";
      ExpressionTree v = arg;
      if (arg instanceof AssignmentTree) {
        name = ((AssignmentTree) arg).getVariable().toString();
        v = ((AssignmentTree) arg).getExpression();
      }
      MSym el = null;
      for (MSym m : at.sym.methodsNamed(name)) {
        el = m;
      }
      if (el != null) {
        vals.put(el, value(a, v, el.ret, env));
      }
    }
    return new Mirror(at, vals);
  }

  static AnnotationValue value(Attr a, ExpressionTree e, Ty et, Env env) {
    if (e instanceof AnnotationTree) {
      return new Val(mirror(a, (AnnotationTree) e, env));
    }
    if (et.tag == Ty.ARRAY) {
      List<AnnotationValue> vs = new ArrayList<AnnotationValue>();
      if (e.getKind() == Tree.Kind.NEW_ARRAY) {
        for (ExpressionTree i : ((NewArrayTree) e).getInitializers()) {
          vs.add(value(a, i, et.elem, env));
        }
      } else {
        vs.add(value(a, e, et.elem, env));
      }
      return new Val(vs);
    }
    a.expr(e, env, et);
    Object c = a.consts.get(e);
    if (c != null) {
      return new Val(et.isPrim() ? ConstFold.coerce(c, et) : c);
    }
    if (et.tag == Ty.CLASS && et.sym.internal.equals("java/lang/Class") && e instanceof MemberSelectTree) {
      Ty qt = a.types.get(((MemberSelectTree) e).getExpression());
      return new Val(qt == null ? Ty.tError : TypeOps.resolve(qt));
    }
    Sym s = a.symOf(Attr.unparen(e));
    if (s instanceof FSym) {
      return new Val(s);
    }
    return new Val("<error>");
  }

  static final class Mirror implements AnnotationMirror {
    final Ty type;
    final Map<ExecutableElement, AnnotationValue> vals;

    Mirror(Ty t, Map<ExecutableElement, AnnotationValue> v) {
      type = t;
      vals = v;
    }

    public DeclaredType getAnnotationType() {
      return type;
    }

    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
      return vals;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder("@").append(type);
      if (!vals.isEmpty()) {
        sb.append('(');
        boolean first = true;
        boolean single = vals.size() == 1 && vals.keySet().iterator().next().getSimpleName().contentEquals("value");
        for (Map.Entry<ExecutableElement, AnnotationValue> e : vals.entrySet()) {
          if (!first) {
            sb.append(", ");
          }
          first = false;
          if (!single) {
            sb.append(e.getKey().getSimpleName()).append('=');
          }
          sb.append(e.getValue());
        }
        sb.append(')');
      }
      return sb.toString();
    }
  }

  static final class Val implements AnnotationValue {
    final Object v;

    Val(Object v) {
      this.v = v;
    }

    public Object getValue() {
      return v;
    }

    @SuppressWarnings("unchecked")
    public <R, P> R accept(AnnotationValueVisitor<R, P> vis, P p) {
      if (v instanceof Boolean) {
        return vis.visitBoolean(((Boolean) v).booleanValue(), p);
      }
      if (v instanceof Byte) {
        return vis.visitByte(((Byte) v).byteValue(), p);
      }
      if (v instanceof Character) {
        return vis.visitChar(((Character) v).charValue(), p);
      }
      if (v instanceof Double) {
        return vis.visitDouble(((Double) v).doubleValue(), p);
      }
      if (v instanceof Float) {
        return vis.visitFloat(((Float) v).floatValue(), p);
      }
      if (v instanceof Integer) {
        return vis.visitInt(((Integer) v).intValue(), p);
      }
      if (v instanceof Long) {
        return vis.visitLong(((Long) v).longValue(), p);
      }
      if (v instanceof Short) {
        return vis.visitShort(((Short) v).shortValue(), p);
      }
      if (v instanceof String) {
        return vis.visitString((String) v, p);
      }
      if (v instanceof TypeMirror) {
        return vis.visitType((TypeMirror) v, p);
      }
      if (v instanceof FSym) {
        return vis.visitEnumConstant((FSym) v, p);
      }
      if (v instanceof AnnotationMirror) {
        return vis.visitAnnotation((AnnotationMirror) v, p);
      }
      if (v instanceof List) {
        return vis.visitArray((List<? extends AnnotationValue>) v, p);
      }
      return vis.visitUnknown(this, p);
    }

    public String toString() {
      if (v instanceof String) {
        return "\"" + ((String) v).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
      }
      if (v instanceof Character) {
        return "'" + v + "'";
      }
      if (v instanceof Long) {
        return v + "L";
      }
      if (v instanceof Float) {
        return v + "f";
      }
      if (v instanceof TypeMirror) {
        return v + ".class";
      }
      if (v instanceof FSym) {
        return ((FSym) v).name;
      }
      if (v instanceof List) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Object o : (List<?>) v) {
          if (!first) {
            sb.append(", ");
          }
          first = false;
          sb.append(o);
        }
        return sb.append('}').toString();
      }
      return String.valueOf(v);
    }
  }
}
