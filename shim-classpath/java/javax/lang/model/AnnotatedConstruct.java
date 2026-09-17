/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model;

public interface AnnotatedConstruct {
  <A extends java.lang.annotation.Annotation> A getAnnotation(java.lang.Class<A> p0);
  java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors();
  <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(java.lang.Class<A> p0);
}
