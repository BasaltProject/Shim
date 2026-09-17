/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public abstract class AbstractAnnotationValueVisitor6<R, P> implements javax.lang.model.element.AnnotationValueVisitor<R, P> {
  protected AbstractAnnotationValueVisitor6() {
  }

  public final R visit(javax.lang.model.element.AnnotationValue annotationValue, P p) {
    return annotationValue.accept(this, p);
  }
  public final R visit(javax.lang.model.element.AnnotationValue annotationValue) {
    return annotationValue.accept(this, null);
  }
  public R visitUnknown(javax.lang.model.element.AnnotationValue annotationValue, P p) {
    throw new javax.lang.model.element.UnknownAnnotationValueException(annotationValue, p);
  }
}
