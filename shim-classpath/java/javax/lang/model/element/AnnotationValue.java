/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface AnnotationValue {
  <R, P> R accept(javax.lang.model.element.AnnotationValueVisitor<R, P> annotationValueVisitor, P p);
  java.lang.Object getValue();
  java.lang.String toString();
}
