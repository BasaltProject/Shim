/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface Element extends javax.lang.model.AnnotatedConstruct {
  <R, P> R accept(javax.lang.model.element.ElementVisitor<R, P> elementVisitor, P p);
  javax.lang.model.type.TypeMirror asType();
  boolean equals(java.lang.Object object);
  <A extends java.lang.annotation.Annotation> A getAnnotation(java.lang.Class<A> p0);
  java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAnnotationMirrors();
  <A extends java.lang.annotation.Annotation> A[] getAnnotationsByType(java.lang.Class<A> p0);
  java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements();
  javax.lang.model.element.Element getEnclosingElement();
  javax.lang.model.element.ElementKind getKind();
  java.util.Set<javax.lang.model.element.Modifier> getModifiers();
  javax.lang.model.element.Name getSimpleName();
  int hashCode();
}
