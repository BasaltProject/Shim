/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.type.*;

/** A type of the attribution layer: primitive, class, array, type variable, wildcard, intersection or inference variable. */
final class Ty implements ErrorType, ArrayType, TypeVariable, WildcardType, PrimitiveType, NoType, NullType,
    IntersectionType {
  static final int BYTE = 0;
  static final int SHORT = 1;
  static final int CHAR = 2;
  static final int INT = 3;
  static final int LONG = 4;
  static final int FLOAT = 5;
  static final int DOUBLE = 6;
  static final int BOOLEAN = 7;
  static final int VOID = 8;
  static final int NULL = 9;
  static final int CLASS = 10;
  static final int ARRAY = 11;
  static final int TVAR = 12;
  static final int WILD = 13;
  static final int INTER = 14;
  static final int ERROR = 15;
  static final int IVAR = 16;
  static final int NONE = 17;

  static final Ty[] NO_ARGS = new Ty[0];

  static final Ty tByte = new Ty(BYTE);
  static final Ty tShort = new Ty(SHORT);
  static final Ty tChar = new Ty(CHAR);
  static final Ty tInt = new Ty(INT);
  static final Ty tLong = new Ty(LONG);
  static final Ty tFloat = new Ty(FLOAT);
  static final Ty tDouble = new Ty(DOUBLE);
  static final Ty tBoolean = new Ty(BOOLEAN);
  static final Ty tVoid = new Ty(VOID);
  static final Ty tNull = new Ty(NULL);
  static final Ty tError = new Ty(ERROR);
  static final Ty tNone = new Ty(NONE);

  final int tag;
  CSym sym;
  Ty[] args = NO_ARGS;
  Ty elem;
  TVSym tv;
  int wildKind;
  Ty bound;
  IVar iv;

  Ty(int tag) {
    this.tag = tag;
  }

  static Ty cls(CSym sym, Ty... args) {
    Ty t = new Ty(CLASS);
    t.sym = sym;
    t.args = args == null ? NO_ARGS : args;
    return t;
  }

  static Ty array(Ty elem) {
    Ty t = new Ty(ARRAY);
    t.elem = elem;
    return t;
  }

  static Ty tvar(TVSym tv) {
    Ty t = new Ty(TVAR);
    t.tv = tv;
    return t;
  }

  static Ty wild(int kind, Ty bound) {
    Ty t = new Ty(WILD);
    t.wildKind = kind;
    t.bound = bound;
    return t;
  }

  static Ty inter(List<Ty> bounds) {
    if (bounds.size() == 1) {
      return bounds.get(0);
    }
    Ty t = new Ty(INTER);
    t.args = bounds.toArray(new Ty[0]);
    return t;
  }

  boolean isPrim() {
    return tag <= BOOLEAN;
  }

  boolean isNumeric() {
    return tag <= DOUBLE;
  }

  boolean isIntegral() {
    return tag <= LONG;
  }

  boolean isRef() {
    return tag == CLASS || tag == ARRAY || tag == TVAR || tag == INTER || tag == NULL || tag == IVAR || tag == WILD;
  }

  boolean isErr() {
    return tag == ERROR;
  }

  boolean isRaw() {
    return tag == CLASS && args.length == 0 && sym != null && !sym.typeParams().isEmpty();
  }

  boolean isParameterized() {
    return tag == CLASS && args.length > 0;
  }

  boolean is(String internal) {
    return tag == CLASS && sym.internal.equals(internal);
  }

  boolean isObject() {
    return is("java/lang/Object");
  }

  boolean isString() {
    return is("java/lang/String");
  }

  /** Stack slots occupied by a value of this type. */
  int width() {
    return tag == LONG || tag == DOUBLE ? 2 : tag == VOID ? 0 : 1;
  }

  static Ty primByName(String n) {
    switch (n) {
      case "byte":
        return tByte;
      case "short":
        return tShort;
      case "char":
        return tChar;
      case "int":
        return tInt;
      case "long":
        return tLong;
      case "float":
        return tFloat;
      case "double":
        return tDouble;
      case "boolean":
        return tBoolean;
      case "void":
        return tVoid;
      default:
        return null;
    }
  }

  String primName() {
    switch (tag) {
      case BYTE:
        return "byte";
      case SHORT:
        return "short";
      case CHAR:
        return "char";
      case INT:
        return "int";
      case LONG:
        return "long";
      case FLOAT:
        return "float";
      case DOUBLE:
        return "double";
      case BOOLEAN:
        return "boolean";
      case VOID:
        return "void";
      default:
        return null;
    }
  }

  /** Descriptor of the erasure. */
  String desc() {
    switch (tag) {
      case BYTE:
        return "B";
      case SHORT:
        return "S";
      case CHAR:
        return "C";
      case INT:
        return "I";
      case LONG:
        return "J";
      case FLOAT:
        return "F";
      case DOUBLE:
        return "D";
      case BOOLEAN:
        return "Z";
      case VOID:
        return "V";
      case CLASS:
        return "L" + sym.internal + ";";
      case ARRAY:
        return "[" + elem.desc();
      case TVAR:
        return tv.bounds().isEmpty() ? "Ljava/lang/Object;" : tv.bounds().get(0).desc();
      case INTER:
        return args[0].desc();
      case WILD:
        return wildKind == 1 ? bound.desc() : "Ljava/lang/Object;";
      case IVAR:
        return iv.inst != null ? iv.inst.desc() : "Ljava/lang/Object;";
      case NULL:
        return "Ljava/lang/Object;";
      default:
        return "Ljava/lang/Object;";
    }
  }

  /** Internal name for class and array types as used by checkcast and anewarray. */
  String internalName() {
    String d = desc();
    return d.charAt(0) == 'L' ? d.substring(1, d.length() - 1) : d;
  }

  public String toString() {
    switch (tag) {
      case CLASS: {
        StringBuilder sb = new StringBuilder(sym.displayName());
        if (args.length > 0) {
          sb.append('<');
          for (int i = 0; i < args.length; i++) {
            if (i > 0) {
              sb.append(',');
            }
            sb.append(args[i]);
          }
          sb.append('>');
        }
        return sb.toString();
      }
      case ARRAY:
        return elem + "[]";
      case TVAR:
        return tv.name;
      case WILD:
        return wildKind == 0 ? "?" : wildKind == 1 ? "? extends " + bound : "? super " + bound;
      case INTER: {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
          if (i > 0) {
            sb.append('&');
          }
          sb.append(args[i]);
        }
        return sb.toString();
      }
      case NULL:
        return "<null>";
      case ERROR:
        return "<any>";
      case IVAR:
        return iv.inst != null ? iv.inst.toString() : iv.tv.name;
      case NONE:
        return "none";
      default:
        return primName();
    }
  }

  static final class IVar {
    final TVSym tv;
    final List<Ty> lower = new ArrayList<Ty>();
    final List<Ty> upper = new ArrayList<Ty>();
    final List<Ty> eq = new ArrayList<Ty>();
    Ty inst;
    Ty self;
    boolean capturedFromWildcard;
    int declared;

    IVar(TVSym tv) {
      this.tv = tv;
    }
  }

  static Ty ivar(TVSym tv) {
    Ty t = new Ty(IVAR);
    t.iv = new IVar(tv);
    t.iv.self = t;
    return t;
  }

  /* ---------- javax.lang.model.type ---------- */

  public TypeKind getKind() {
    switch (tag) {
      case BYTE:
        return TypeKind.BYTE;
      case SHORT:
        return TypeKind.SHORT;
      case CHAR:
        return TypeKind.CHAR;
      case INT:
        return TypeKind.INT;
      case LONG:
        return TypeKind.LONG;
      case FLOAT:
        return TypeKind.FLOAT;
      case DOUBLE:
        return TypeKind.DOUBLE;
      case BOOLEAN:
        return TypeKind.BOOLEAN;
      case VOID:
        return TypeKind.VOID;
      case NULL:
        return TypeKind.NULL;
      case CLASS:
        return TypeKind.DECLARED;
      case ARRAY:
        return TypeKind.ARRAY;
      case TVAR:
        return TypeKind.TYPEVAR;
      case WILD:
        return TypeKind.WILDCARD;
      case INTER:
        return TypeKind.INTERSECTION;
      case ERROR:
        return TypeKind.ERROR;
      case IVAR:
        return iv.inst != null ? iv.inst.getKind() : TypeKind.OTHER;
      default:
        return TypeKind.NONE;
    }
  }

  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    switch (tag) {
      case CLASS:
        return v.visitDeclared(this, p);
      case ARRAY:
        return v.visitArray(this, p);
      case TVAR:
        return v.visitTypeVariable(this, p);
      case WILD:
        return v.visitWildcard(this, p);
      case INTER:
        return v.visitIntersection(this, p);
      case ERROR:
        return v.visitError(this, p);
      case NULL:
        return v.visitNull(this, p);
      case VOID:
      case NONE:
        return v.visitNoType(this, p);
      case IVAR:
        return iv.inst != null ? iv.inst.accept(v, p) : v.visitUnknown(this, p);
      default:
        return v.visitPrimitive(this, p);
    }
  }

  public Element asElement() {
    return tag == CLASS ? sym : tag == TVAR ? tv : null;
  }

  public TypeMirror getEnclosingType() {
    return tNone;
  }

  public List<? extends TypeMirror> getTypeArguments() {
    return ModelUtils.j(java.util.Arrays.asList(args));
  }

  public TypeMirror getComponentType() {
    return elem;
  }

  public TypeMirror getUpperBound() {
    if (tag == TVAR) {
      List<Ty> bs = tv.bounds();
      return bs.size() == 1 ? bs.get(0) : bs.isEmpty() ? tNull : inter(new ArrayList<Ty>(bs));
    }
    return tNull;
  }

  public TypeMirror getLowerBound() {
    return tNull;
  }

  public TypeMirror getExtendsBound() {
    return wildKind == 1 ? bound : null;
  }

  public TypeMirror getSuperBound() {
    return wildKind == 2 ? bound : null;
  }

  public List<? extends TypeMirror> getBounds() {
    return Collections.unmodifiableList(java.util.Arrays.asList(args));
  }

  public List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors() {
    return Collections.emptyList();
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return (A[]) java.lang.reflect.Array.newInstance(annotationType, 0);
  }
}
