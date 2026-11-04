/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public abstract class AbstractTypeVisitor6<R, P> implements javax.lang.model.type.TypeVisitor<R, P> {
  protected AbstractTypeVisitor6() {
  }

  public final R visit(javax.lang.model.type.TypeMirror typeMirror, P p) {
    return typeMirror.accept(this, p);
  }
  public final R visit(javax.lang.model.type.TypeMirror typeMirror) {
    return typeMirror.accept(this, null);
  }
  public R visitIntersection(javax.lang.model.type.IntersectionType intersectionType, P p) {
    return visitUnknown(intersectionType, p);
  }
  public R visitUnion(javax.lang.model.type.UnionType unionType, P p) {
    return visitUnknown(unionType, p);
  }
  public R visitUnknown(javax.lang.model.type.TypeMirror typeMirror, P p) {
    throw new javax.lang.model.type.UnknownTypeException(typeMirror, p);
  }
}
