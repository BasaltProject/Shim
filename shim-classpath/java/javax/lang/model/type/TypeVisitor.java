/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public interface TypeVisitor<R, P> {
  R visit(javax.lang.model.type.TypeMirror typeMirror, P p);
  default R visit(javax.lang.model.type.TypeMirror typeMirror) {
    return visit(typeMirror, null);
  }
  R visitArray(javax.lang.model.type.ArrayType arrayType, P p);
  R visitDeclared(javax.lang.model.type.DeclaredType declaredType, P p);
  R visitError(javax.lang.model.type.ErrorType errorType, P p);
  R visitExecutable(javax.lang.model.type.ExecutableType executableType, P p);
  R visitIntersection(javax.lang.model.type.IntersectionType intersectionType, P p);
  R visitNoType(javax.lang.model.type.NoType noType, P p);
  R visitNull(javax.lang.model.type.NullType nullType, P p);
  R visitPrimitive(javax.lang.model.type.PrimitiveType primitiveType, P p);
  R visitTypeVariable(javax.lang.model.type.TypeVariable typeVariable, P p);
  R visitUnion(javax.lang.model.type.UnionType unionType, P p);
  R visitUnknown(javax.lang.model.type.TypeMirror typeMirror, P p);
  R visitWildcard(javax.lang.model.type.WildcardType wildcardType, P p);
}
