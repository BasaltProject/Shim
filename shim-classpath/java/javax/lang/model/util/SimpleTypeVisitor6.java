/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleTypeVisitor6<R, P> extends javax.lang.model.util.AbstractTypeVisitor6<R, P> {
  protected final R DEFAULT_VALUE;

  protected SimpleTypeVisitor6() {
    DEFAULT_VALUE = null;
  }
  protected SimpleTypeVisitor6(R r) {
    DEFAULT_VALUE = r;
  }

  protected R defaultAction(javax.lang.model.type.TypeMirror typeMirror, P p) {
    return DEFAULT_VALUE;
  }
  public R visitArray(javax.lang.model.type.ArrayType arrayType, P p) {
    return defaultAction(arrayType, p);
  }
  public R visitDeclared(javax.lang.model.type.DeclaredType declaredType, P p) {
    return defaultAction(declaredType, p);
  }
  public R visitError(javax.lang.model.type.ErrorType errorType, P p) {
    return defaultAction(errorType, p);
  }
  public R visitExecutable(javax.lang.model.type.ExecutableType executableType, P p) {
    return defaultAction(executableType, p);
  }
  public R visitNoType(javax.lang.model.type.NoType noType, P p) {
    return defaultAction(noType, p);
  }
  public R visitNull(javax.lang.model.type.NullType nullType, P p) {
    return defaultAction(nullType, p);
  }
  public R visitPrimitive(javax.lang.model.type.PrimitiveType primitiveType, P p) {
    return defaultAction(primitiveType, p);
  }
  public R visitTypeVariable(javax.lang.model.type.TypeVariable typeVariable, P p) {
    return defaultAction(typeVariable, p);
  }
  public R visitWildcard(javax.lang.model.type.WildcardType wildcardType, P p) {
    return defaultAction(wildcardType, p);
  }
}
