/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

/** Symbols of the attribution layer. */
abstract class Sym implements Element {
  static final int PUBLIC = 0x1;
  static final int PRIVATE = 0x2;
  static final int PROTECTED = 0x4;
  static final int STATIC = 0x8;
  static final int FINAL = 0x10;
  static final int SYNCHRONIZED = 0x20;
  static final int VOLATILE = 0x40;
  static final int BRIDGE = 0x40;
  static final int TRANSIENT = 0x80;
  static final int VARARGS = 0x80;
  static final int NATIVE = 0x100;
  static final int INTERFACE = 0x200;
  static final int ABSTRACT = 0x400;
  static final int STRICT = 0x800;
  static final int SYNTHETIC = 0x1000;
  static final int ANNOTATION = 0x2000;
  static final int ENUM = 0x4000;
  static final int DEFAULT = 1 << 20;
  static final int RECORD = 1 << 21;
  static final int SEALED = 1 << 22;
  static final int NON_SEALED = 1 << 23;
  static final int COMPACT_CTOR = 1 << 24;
  static final int GENERATED_CTOR = 1 << 25;
  static final int EFFECTIVELY_FINAL = 1 << 26;
  static final int MANDATED = 1 << 27;

  String name;
  int flags;
  Sym owner;

  boolean isStatic() {
    return (flags & STATIC) != 0;
  }

  boolean isPrivate() {
    return (flags & PRIVATE) != 0;
  }

  boolean isFinal() {
    return (flags & FINAL) != 0;
  }

  boolean isAbstract() {
    return (flags & ABSTRACT) != 0;
  }

  public javax.lang.model.element.Name getSimpleName() {
    return NameImpl.of(name == null ? "" : name);
  }

  public Set<Modifier> getModifiers() {
    Set<Modifier> m = EnumSet.noneOf(Modifier.class);
    if ((flags & PUBLIC) != 0) {
      m.add(Modifier.PUBLIC);
    }
    if ((flags & PROTECTED) != 0) {
      m.add(Modifier.PROTECTED);
    }
    if ((flags & PRIVATE) != 0) {
      m.add(Modifier.PRIVATE);
    }
    boolean isMethod = this instanceof MSym;
    boolean isClass = this instanceof CSym;
    if ((flags & ABSTRACT) != 0 && !(isClass && (flags & INTERFACE) != 0 && false)) {
      m.add(Modifier.ABSTRACT);
    }
    if ((flags & DEFAULT) != 0 && isMethod) {
      m.add(Modifier.DEFAULT);
    }
    if ((flags & STATIC) != 0) {
      m.add(Modifier.STATIC);
    }
    if ((flags & SEALED) != 0) {
      m.add(Modifier.SEALED);
    }
    if ((flags & NON_SEALED) != 0) {
      m.add(Modifier.NON_SEALED);
    }
    if ((flags & FINAL) != 0) {
      m.add(Modifier.FINAL);
    }
    if (isMethod) {
      if ((flags & SYNCHRONIZED) != 0) {
        m.add(Modifier.SYNCHRONIZED);
      }
      if ((flags & NATIVE) != 0) {
        m.add(Modifier.NATIVE);
      }
    } else if (this instanceof FSym) {
      if ((flags & TRANSIENT) != 0) {
        m.add(Modifier.TRANSIENT);
      }
      if ((flags & VOLATILE) != 0) {
        m.add(Modifier.VOLATILE);
      }
    }
    return m;
  }

  public Element getEnclosingElement() {
    return owner;
  }

  public List<? extends Element> getEnclosedElements() {
    return Collections.emptyList();
  }

  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return AnnMirrors.of(this);
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return (A[]) java.lang.reflect.Array.newInstance(annotationType, 0);
  }

  public TypeMirror asType() {
    return Ty.tNone;
  }
}

final class PkgSym extends Sym implements PackageElement {
  final String full;

  PkgSym(String full) {
    this.full = full;
    this.name = full.substring(full.lastIndexOf('.') + 1);
  }

  public javax.lang.model.element.Name getQualifiedName() {
    return NameImpl.of(full);
  }

  public boolean isUnnamed() {
    return full.isEmpty();
  }

  public ElementKind getKind() {
    return ElementKind.PACKAGE;
  }

  public Element getEnclosingElement() {
    return null;
  }

  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitPackage(this, p);
  }

  public boolean equals(Object o) {
    return o instanceof PkgSym && ((PkgSym) o).full.equals(full);
  }

  public int hashCode() {
    return full.hashCode();
  }

  public String toString() {
    return full.isEmpty() ? "unnamed package" : full;
  }
}

final class TVSym extends Sym implements TypeParameterElement {
  List<Ty> bounds = new ArrayList<Ty>();
  Runnable lazyBounds;
  Ty type;
  Object decl;

  TVSym(String name, Object decl) {
    this.name = name;
    this.decl = decl;
    this.type = Ty.tvar(this);
  }

  List<Ty> bounds() {
    if (lazyBounds != null) {
      Runnable r = lazyBounds;
      lazyBounds = null;
      r.run();
    }
    return bounds;
  }

  public Element getGenericElement() {
    return decl instanceof Element ? (Element) decl : null;
  }

  public List<? extends TypeMirror> getBounds() {
    return ModelUtils.j(bounds());
  }

  public ElementKind getKind() {
    return ElementKind.TYPE_PARAMETER;
  }

  public TypeMirror asType() {
    return type;
  }

  public Element getEnclosingElement() {
    return getGenericElement();
  }

  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitTypeParameter(this, p);
  }

  public String toString() {
    return name;
  }
}

final class FSym extends Sym implements VariableElement {
  Ty type;
  VariableTree tree;
  Object constant;
  boolean constKnown;
  boolean constBusy;
  java.lang.reflect.Field reflect;

  CSym cls() {
    return (CSym) owner;
  }

  public ElementKind getKind() {
    return (flags & ENUM) != 0 ? ElementKind.ENUM_CONSTANT : ElementKind.FIELD;
  }

  public TypeMirror asType() {
    return type;
  }

  public Object getConstantValue() {
    return isFinal() && owner instanceof CSym ? (tree != null && constKnown ? constant : constKnown ? constant : null)
        : null;
  }

  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  public String toString() {
    return name;
  }
}

final class VSym extends Sym implements VariableElement {
  Ty type;
  int slot = -1;
  boolean param;
  boolean assignedAfterInit;
  Object scopeOwner;
  Object constant;
  boolean captured;
  boolean isPattern;
  VariableTree tree;

  public ElementKind getKind() {
    return param ? ElementKind.PARAMETER : isPattern ? ElementKind.BINDING_VARIABLE : ElementKind.LOCAL_VARIABLE;
  }

  public TypeMirror asType() {
    return type;
  }

  public Object getConstantValue() {
    return constant;
  }

  public Element getEnclosingElement() {
    return scopeOwner instanceof Element ? (Element) scopeOwner : null;
  }

  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  public String toString() {
    return name;
  }
}

final class MSym extends Sym implements ExecutableElement {
  List<TVSym> tparams = new ArrayList<TVSym>();
  Ty ret = Ty.tVoid;
  List<Ty> params = new ArrayList<Ty>();
  List<String> paramNames;
  List<Ty> thrown = new ArrayList<Ty>();
  MethodTree tree;
  Object defaultValue;
  boolean hasDefaultValue;
  boolean inheritsOuterThis;
  java.lang.reflect.Executable reflect;
  Sym origin;

  CSym cls() {
    return (CSym) owner;
  }

  boolean isCtor() {
    return name.equals("<init>");
  }

  boolean isVarargs() {
    return (flags & VARARGS) != 0;
  }

  String erasedDesc() {
    StringBuilder sb = new StringBuilder("(");
    for (Ty p : params) {
      sb.append(TypeOps.erasure(p).desc());
    }
    sb.append(')').append(isCtor() ? "V" : TypeOps.erasure(ret).desc());
    return sb.toString();
  }

  public ElementKind getKind() {
    if (isCtor()) {
      return ElementKind.CONSTRUCTOR;
    }
    if (name.equals("<clinit>")) {
      return ElementKind.STATIC_INIT;
    }
    return ElementKind.METHOD;
  }

  public TypeMirror asType() {
    Tx.MType mt = new Tx.MType();
    mt.tparams = tparams;
    mt.params = params;
    mt.ret = ret;
    mt.thrown = thrown;
    return new ModelUtils.ExecMirror(this, mt);
  }

  public List<? extends TypeParameterElement> getTypeParameters() {
    return ModelUtils.j(tparams);
  }

  public TypeMirror getReturnType() {
    return isCtor() ? Ty.tVoid : ret;
  }

  public List<? extends VariableElement> getParameters() {
    List<VariableElement> out = new ArrayList<VariableElement>();
    for (int i = 0; i < params.size(); i++) {
      VSym v = new VSym();
      v.name = paramNames != null && i < paramNames.size() ? paramNames.get(i) : "arg" + i;
      v.type = params.get(i);
      v.param = true;
      v.scopeOwner = this;
      out.add(v);
    }
    return ModelUtils.j(out);
  }

  public TypeMirror getReceiverType() {
    return Ty.tNone;
  }

  public boolean isVarArgs() {
    return isVarargs();
  }

  public boolean isDefault() {
    return (flags & DEFAULT) != 0;
  }

  public List<? extends TypeMirror> getThrownTypes() {
    return ModelUtils.j(thrown);
  }

  public AnnotationValue getDefaultValue() {
    return null;
  }

  public javax.lang.model.element.Name getSimpleName() {
    return NameImpl.of(name);
  }

  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (!tparams.isEmpty()) {
      sb.append('<');
      for (int i = 0; i < tparams.size(); i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(tparams.get(i).name);
      }
      sb.append('>');
    }
    sb.append(isCtor() ? owner.name : name).append('(');
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(params.get(i));
    }
    return sb.append(')').toString();
  }
}

final class CSym extends Sym implements TypeElement {
  interface Completer {
    void completeHeader(CSym c);

    void completeMembers(CSym c);
  }

  String internal;
  CSym outerClass;
  boolean local;
  boolean anon;
  Sym enclosingMethod;
  List<TVSym> tparams = new ArrayList<TVSym>();
  Ty superclass;
  List<Ty> interfaces = new ArrayList<Ty>();
  List<Ty> permitted = new ArrayList<Ty>();
  final Map<String, FSym> fields = new LinkedHashMap<String, FSym>();
  final Map<String, List<MSym>> methods = new LinkedHashMap<String, List<MSym>>();
  final List<MSym> allMethods = new ArrayList<MSym>();
  final Map<String, CSym> memberClasses = new LinkedHashMap<String, CSym>();
  final List<FSym> recordComponents = new ArrayList<FSym>();
  ClassTree tree;
  CompilationUnitTree cu;
  Class<?> reflect;
  Completer completer;
  Completer membersCompleter;
  int headerState;
  int memberState;
  Ty selfType;
  boolean isSource;
  String pkg;
  int localCounter;
  int anonCounter;
  Tree enclosingTree;
  Object attrData;

  CSym(String internal) {
    this.internal = internal;
    int slash = internal.lastIndexOf('/');
    this.pkg = slash < 0 ? "" : internal.substring(0, slash).replace('/', '.');
  }

  boolean isInterface() {
    return (flags & INTERFACE) != 0;
  }

  boolean isEnum() {
    return (flags & ENUM) != 0;
  }

  boolean isRecord() {
    return (flags & RECORD) != 0;
  }

  boolean isInner() {
    return outerClass != null && !isStatic() && !isInterface() && !isEnum() && !isRecord()
        && !(outerClass.isInterface());
  }

  void header() {
    if (headerState == 0 && completer != null) {
      headerState = 1;
      completer.completeHeader(this);
      headerState = 2;
    }
  }

  void members() {
    header();
    if (memberState == 0) {
      memberState = 1;
      Completer c = membersCompleter != null ? membersCompleter : completer;
      if (c != null) {
        c.completeMembers(this);
      }
      memberState = 2;
    }
  }

  List<TVSym> typeParams() {
    header();
    return tparams;
  }

  Ty superclass() {
    header();
    return superclass;
  }

  List<Ty> interfaces() {
    header();
    return interfaces;
  }

  Ty type() {
    if (selfType == null) {
      List<TVSym> tps = typeParams();
      Ty[] a = new Ty[tps.size()];
      for (int i = 0; i < a.length; i++) {
        a[i] = tps.get(i).type;
      }
      selfType = Ty.cls(this, a);
    }
    return selfType;
  }

  Ty erasedType() {
    return Ty.cls(this);
  }

  FSym field(String n) {
    members();
    return fields.get(n);
  }

  List<MSym> methodsNamed(String n) {
    members();
    List<MSym> l = methods.get(n);
    return l == null ? java.util.Collections.<MSym>emptyList() : l;
  }

  void addMethod(MSym m) {
    m.owner = this;
    List<MSym> l = methods.get(m.name);
    if (l == null) {
      l = new ArrayList<MSym>();
      methods.put(m.name, l);
    }
    l.add(m);
    allMethods.add(m);
  }

  void addField(FSym f) {
    f.owner = this;
    fields.put(f.name, f);
  }

  CSym memberClass(String n) {
    members();
    return memberClasses.get(n);
  }

  /** The javac-style source name, such as java.util.Map.Entry. */
  String fullName() {
    if (local || anon) {
      return name;
    }
    if (outerClass != null) {
      return outerClass.fullName() + "." + name;
    }
    return pkg.isEmpty() ? name : pkg + "." + name;
  }

  String displayName() {
    return fullName();
  }

  String flatName() {
    return internal.replace('/', '.');
  }

  CSym outermost() {
    CSym c = this;
    while (c.outerClass != null) {
      c = c.outerClass;
    }
    return c;
  }

  Enter enterRef;

  public ElementKind getKind() {
    if ((flags & ANNOTATION) != 0) {
      return ElementKind.ANNOTATION_TYPE;
    }
    if (isInterface()) {
      return ElementKind.INTERFACE;
    }
    if (isEnum()) {
      return ElementKind.ENUM;
    }
    if (isRecord()) {
      return ElementKind.RECORD;
    }
    return ElementKind.CLASS;
  }

  public NestingKind getNestingKind() {
    if (anon) {
      return NestingKind.ANONYMOUS;
    }
    if (local) {
      return NestingKind.LOCAL;
    }
    return outerClass != null ? NestingKind.MEMBER : NestingKind.TOP_LEVEL;
  }

  public javax.lang.model.element.Name getQualifiedName() {
    return NameImpl.of(local || anon ? "" : fullName());
  }

  public TypeMirror getSuperclass() {
    Ty s = superclass();
    return s == null ? Ty.tNone : s;
  }

  public List<? extends TypeMirror> getInterfaces() {
    return ModelUtils.j(interfaces());
  }

  public List<? extends TypeParameterElement> getTypeParameters() {
    return ModelUtils.j(typeParams());
  }

  public List<? extends TypeMirror> getPermittedSubclasses() {
    header();
    return permitted;
  }

  public TypeMirror asType() {
    return type();
  }

  public Element getEnclosingElement() {
    if (outerClass != null) {
      return outerClass;
    }
    return new PkgSym(pkg);
  }

  public List<? extends Element> getEnclosedElements() {
    members();
    List<Element> out = new ArrayList<Element>();
    if (tree != null && enterRef != null) {
      out.addAll(memberClasses.values());
      for (Tree m : tree.getMembers()) {
        Object s = enterRef.symOf.get(m);
        if (s instanceof Element && !(s instanceof CSym)) {
          out.add((Element) s);
        }
      }
      for (MSym m : allMethods) {
        if (m.tree == null) {
          out.add(m);
        }
      }
      return out;
    }
    out.addAll(memberClasses.values());
    out.addAll(fields.values());
    out.addAll(allMethods);
    return out;
  }

  public List<? extends RecordComponentElement> getRecordComponents() {
    return Collections.emptyList();
  }

  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

  public String toString() {
    return fullName();
  }
}
