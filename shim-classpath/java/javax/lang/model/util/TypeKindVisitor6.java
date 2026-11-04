/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class TypeKindVisitor6<R, P> extends javax.lang.model.util.SimpleTypeVisitor6<R, P> {
  protected TypeKindVisitor6() {
  }
  protected TypeKindVisitor6(R r) {
    super(r);
  }

  public R visitNoType(javax.lang.model.type.NoType noType, P p) {
    javax.lang.model.type.TypeKind k = noType.getKind();
    switch (k) {
    case VOID:
      return visitNoTypeAsVoid(noType, p);
    case PACKAGE:
      return visitNoTypeAsPackage(noType, p);
    case MODULE:
      return visitNoTypeAsModule(noType, p);
    case NONE:
      return visitNoTypeAsNone(noType, p);
    default:
      throw new AssertionError("Bad kind " + k + " for NoType" + noType);
    }
  }
  public R visitNoTypeAsModule(javax.lang.model.type.NoType noType, P p) {
    return visitUnknown(noType, p);
  }
  public R visitNoTypeAsNone(javax.lang.model.type.NoType noType, P p) {
    return defaultAction(noType, p);
  }
  public R visitNoTypeAsPackage(javax.lang.model.type.NoType noType, P p) {
    return defaultAction(noType, p);
  }
  public R visitNoTypeAsVoid(javax.lang.model.type.NoType noType, P p) {
    return defaultAction(noType, p);
  }
  public R visitPrimitive(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    javax.lang.model.type.TypeKind k = primitiveType.getKind();
    switch (k) {
    case BOOLEAN:
      return visitPrimitiveAsBoolean(primitiveType, p);
    case BYTE:
      return visitPrimitiveAsByte(primitiveType, p);
    case SHORT:
      return visitPrimitiveAsShort(primitiveType, p);
    case INT:
      return visitPrimitiveAsInt(primitiveType, p);
    case LONG:
      return visitPrimitiveAsLong(primitiveType, p);
    case CHAR:
      return visitPrimitiveAsChar(primitiveType, p);
    case FLOAT:
      return visitPrimitiveAsFloat(primitiveType, p);
    case DOUBLE:
      return visitPrimitiveAsDouble(primitiveType, p);
    default:
      throw new AssertionError("Bad kind " + k + " for PrimitiveType" + primitiveType);
    }
  }
  public R visitPrimitiveAsBoolean(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsByte(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsChar(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsDouble(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsFloat(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsInt(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsLong(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitPrimitiveAsShort(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
}
