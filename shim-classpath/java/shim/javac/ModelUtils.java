/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** Implementations of javax.lang.model.util.Elements and Types over the attribution layer. */
final class ModelUtils {
  private ModelUtils() {
  }

  /** Immutable list printing like javac's list: elements joined by commas. */
  static final class JList<E> extends java.util.AbstractList<E> {
    private final List<E> l;

    JList(List<E> l) {
      this.l = l;
    }

    public E get(int i) {
      return l.get(i);
    }

    public int size() {
      return l.size();
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < l.size(); i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(l.get(i));
      }
      return sb.toString();
    }
  }

  static <E> List<E> j(List<E> l) {
    return new JList<E>(l);
  }

  static final class ElementsImpl implements Elements {
    final Pipeline p;
    final JavacTaskImpl task;

    ElementsImpl(Pipeline p, JavacTaskImpl task) {
      this.p = p;
      this.task = task;
    }

    public PackageElement getPackageElement(CharSequence name) {
      return new PkgSym(name.toString());
    }

    public TypeElement getTypeElement(CharSequence name) {
      return p.enter.qualifiedClass(name.toString());
    }

    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
        AnnotationMirror a) {
      return Collections.emptyMap();
    }

    public String getDocComment(Element e) {
      return null;
    }

    public boolean isDeprecated(Element e) {
      return false;
    }

    public Name getBinaryName(TypeElement type) {
      return NameImpl.of(((CSym) type).flatName());
    }

    public PackageElement getPackageOf(Element e) {
      Element x = e;
      while (x != null && !(x instanceof PackageElement)) {
        x = x.getEnclosingElement();
      }
      if (x == null && e instanceof CSym) {
        return new PkgSym(((CSym) e).pkg);
      }
      return (PackageElement) x;
    }

    public List<? extends Element> getAllMembers(TypeElement type) {
      List<Element> out = new ArrayList<Element>();
      java.util.Set<CSym> seen = new java.util.LinkedHashSet<CSym>();
      collect((CSym) type, out, seen);
      return out;
    }

    private void collect(CSym c, List<Element> out, java.util.Set<CSym> seen) {
      if (!seen.add(c)) {
        return;
      }
      for (Element e : c.getEnclosedElements()) {
        if (!(e instanceof Sym) || !((Sym) e).isPrivate() || c == seen.iterator().next()) {
          out.add(e);
        }
      }
      Ty sc = c.superclass();
      if (sc != null && sc.tag == Ty.CLASS) {
        collect(sc.sym, out, seen);
      }
      for (Ty i : c.interfaces()) {
        if (i.tag == Ty.CLASS) {
          collect(i.sym, out, seen);
        }
      }
    }

    public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
      return Collections.emptyList();
    }

    public boolean hides(Element hider, Element hidden) {
      return false;
    }

    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
      MSym a = (MSym) overrider;
      MSym b = (MSym) overridden;
      if (a == b || !a.name.equals(b.name) || a.params.size() != b.params.size() || a.isStatic() || b.isStatic()) {
        return false;
      }
      CSym t = (CSym) type;
      if (!p.attr.res.isSubClass(a.cls(), b.cls()) && !p.attr.res.isSubClass(t, b.cls())) {
        return false;
      }
      Tx.MType ma = p.tx.memberType(a, t.type());
      Tx.MType mb = p.tx.memberType(b, t.type());
      for (int i = 0; i < ma.params.size(); i++) {
        if (!p.tx.erasure(ma.params.get(i)).desc().equals(p.tx.erasure(mb.params.get(i)).desc())) {
          return false;
        }
      }
      return !b.isPrivate();
    }

    public String getConstantExpression(Object value) {
      if (value instanceof String) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : ((String) value).toCharArray()) {
          sb.append(quote(c, '"'));
        }
        return sb.append('"').toString();
      }
      if (value instanceof Character) {
        return "'" + quote(((Character) value).charValue(), '\'') + "'";
      }
      if (value instanceof Long) {
        return value + "L";
      }
      if (value instanceof Float) {
        float f = ((Float) value).floatValue();
        return Float.isNaN(f) ? "0.0f/0.0f" : Float.isInfinite(f) ? (f < 0 ? "-1.0f/0.0f" : "1.0f/0.0f") : value + "f";
      }
      if (value instanceof Double) {
        double d = ((Double) value).doubleValue();
        return Double.isNaN(d) ? "0.0/0.0" : Double.isInfinite(d) ? (d < 0 ? "-1.0/0.0" : "1.0/0.0") : value.toString();
      }
      if (value instanceof Byte) {
        return "(byte)0x" + String.format("%02x", ((Byte) value).byteValue());
      }
      if (value instanceof Short) {
        return "(short)" + value;
      }
      if (value instanceof Integer || value instanceof Boolean) {
        return value.toString();
      }
      throw new IllegalArgumentException("Argument is not a primitive type or a string; it " + (value == null
          ? "is a null value." : "has class " + value.getClass().getName()) + ".");
    }

    private static String quote(char c, char q) {
      switch (c) {
        case '\b':
          return "\\b";
        case '\f':
          return "\\f";
        case '\n':
          return "\\n";
        case '\r':
          return "\\r";
        case '\t':
          return "\\t";
        case '\'':
          return q == '\'' ? "\\'" : "'";
        case '"':
          return q == '"' ? "\\\"" : "\"";
        case '\\':
          return "\\\\";
        default:
          return c >= ' ' && c < 127 ? String.valueOf(c) : String.format("\\u%04x", (int) c);
      }
    }

    public void printElements(Writer w, Element... elements) {
      java.io.PrintWriter pw = new java.io.PrintWriter(w);
      for (Element e : elements) {
        pw.println(e);
      }
      pw.flush();
    }

    public Name getName(CharSequence cs) {
      return NameImpl.of(cs.toString());
    }

    public boolean isFunctionalInterface(TypeElement type) {
      return p.tx.sam((CSym) type) != null;
    }
  }

  static final class TypesImpl implements Types {
    final Pipeline p;

    TypesImpl(Pipeline p) {
      this.p = p;
    }

    private static Ty t(TypeMirror m) {
      return (Ty) m;
    }

    public Element asElement(TypeMirror t) {
      return t(t).asElement();
    }

    public boolean isSameType(TypeMirror a, TypeMirror b) {
      if (a.getKind() == TypeKind.WILDCARD || b.getKind() == TypeKind.WILDCARD) {
        return false;
      }
      return TypeOps.equal(t(a), t(b));
    }

    public boolean isSubtype(TypeMirror a, TypeMirror b) {
      return p.tx.isSubtype(t(a), t(b));
    }

    public boolean isAssignable(TypeMirror a, TypeMirror b) {
      return p.tx.isLooseConvertible(t(a), t(b));
    }

    public boolean contains(TypeMirror a, TypeMirror b) {
      return p.tx.containsType(t(a), t(b));
    }

    public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
      List<? extends TypeMirror> a = m1.getParameterTypes();
      List<? extends TypeMirror> b = m2.getParameterTypes();
      if (a.size() != b.size()) {
        return false;
      }
      for (int i = 0; i < a.size(); i++) {
        if (!p.tx.erasure(t(a.get(i))).desc().equals(p.tx.erasure(t(b.get(i))).desc())) {
          return false;
        }
      }
      return true;
    }

    public List<? extends TypeMirror> directSupertypes(TypeMirror m) {
      Ty x = t(m);
      List<TypeMirror> out = new ArrayList<TypeMirror>();
      if (x.tag == Ty.CLASS) {
        CSym c = x.sym;
        boolean raw = x.args.length == 0 && !c.typeParams().isEmpty();
        Ty sc = c.superclass();
        if (sc != null) {
          out.add(raw ? TypeOps.erasure(sc) : TypeOps.subst(sc, c.tparams, java.util.Arrays.asList(x.args)));
        } else if (c.isInterface()) {
          out.add(p.st.object);
        }
        for (Ty i : c.interfaces()) {
          out.add(raw ? TypeOps.erasure(i) : TypeOps.subst(i, c.tparams, java.util.Arrays.asList(x.args)));
        }
      } else if (x.tag == Ty.TVAR) {
        out.addAll(x.tv.bounds());
      } else if (x.tag == Ty.ARRAY) {
        out.add(p.st.object);
      }
      return out;
    }

    public TypeMirror erasure(TypeMirror m) {
      return p.tx.erasure(t(m));
    }

    public TypeElement boxedClass(PrimitiveType prim) {
      return p.st.lookup(Tx.BOXES[t(prim).tag]);
    }

    public PrimitiveType unboxedType(TypeMirror m) {
      Ty u = p.tx.unboxed(t(m));
      if (u == null) {
        throw new IllegalArgumentException(m.toString());
      }
      return u;
    }

    public TypeMirror capture(TypeMirror m) {
      return m;
    }

    public PrimitiveType getPrimitiveType(TypeKind kind) {
      Ty r = Ty.primByName(kind.name().toLowerCase());
      if (r == null || kind == TypeKind.VOID) {
        throw new IllegalArgumentException("Not a primitive type: " + kind);
      }
      return r;
    }

    public NullType getNullType() {
      return Ty.tNull;
    }

    public NoType getNoType(TypeKind kind) {
      if (kind == TypeKind.VOID) {
        return Ty.tVoid;
      }
      if (kind == TypeKind.NONE) {
        return Ty.tNone;
      }
      throw new IllegalArgumentException(kind.toString());
    }

    public ArrayType getArrayType(TypeMirror component) {
      return Ty.array(t(component));
    }

    public WildcardType getWildcardType(TypeMirror ext, TypeMirror sup) {
      if (ext != null && sup != null) {
        throw new IllegalArgumentException("Extends and super bounds cannot both be provided");
      }
      return ext != null ? Ty.wild(1, t(ext)) : sup != null ? Ty.wild(2, t(sup)) : Ty.wild(0, null);
    }

    public DeclaredType getDeclaredType(TypeElement te, TypeMirror... args) {
      Ty[] as = new Ty[args.length];
      for (int i = 0; i < as.length; i++) {
        as[i] = t(args[i]);
      }
      if (as.length != 0 && as.length != ((CSym) te).typeParams().size()) {
        throw new IllegalArgumentException("Incorrect number of type arguments");
      }
      return Ty.cls((CSym) te, as);
    }

    public DeclaredType getDeclaredType(DeclaredType containing, TypeElement te, TypeMirror... args) {
      return getDeclaredType(te, args);
    }

    public TypeMirror asMemberOf(DeclaredType containing, Element element) {
      Ty site = t(containing);
      if (element instanceof FSym) {
        return p.tx.fieldType((FSym) element, site);
      }
      if (element instanceof MSym) {
        MSym m = (MSym) element;
        Tx.MType mt = p.tx.memberType(m, site);
        return new ExecMirror(m, mt);
      }
      return ((Sym) element).asType();
    }
  }

  static final class ExecMirror implements ExecutableType {
    final MSym m;
    final Tx.MType mt;

    ExecMirror(MSym m, Tx.MType mt) {
      this.m = m;
      this.mt = mt;
    }

    public List<? extends TypeVariable> getTypeVariables() {
      List<TypeVariable> out = new ArrayList<TypeVariable>();
      for (TVSym tv : mt.tparams) {
        out.add(tv.type);
      }
      return out;
    }

    public TypeMirror getReturnType() {
      return m.isCtor() ? Ty.tVoid : mt.ret;
    }

    public List<? extends TypeMirror> getParameterTypes() {
      return j(mt.params);
    }

    public TypeMirror getReceiverType() {
      return Ty.tNone;
    }

    public List<? extends TypeMirror> getThrownTypes() {
      return j(mt.thrown);
    }

    public TypeKind getKind() {
      return TypeKind.EXECUTABLE;
    }

    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitExecutable(this, p);
    }

    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return Collections.emptyList();
    }

    public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> c) {
      return null;
    }

    @SuppressWarnings("unchecked")
    public <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(Class<A> c) {
      return (A[]) java.lang.reflect.Array.newInstance(c, 0);
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (!mt.tparams.isEmpty()) {
        sb.append('<');
        for (int i = 0; i < mt.tparams.size(); i++) {
          if (i > 0) {
            sb.append(',');
          }
          sb.append(mt.tparams.get(i).name);
        }
        sb.append('>');
      }
      sb.append("(");
      for (int i = 0; i < mt.params.size(); i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(mt.params.get(i));
      }
      return sb.append(')').append(getReturnType()).toString();
    }
  }
}
