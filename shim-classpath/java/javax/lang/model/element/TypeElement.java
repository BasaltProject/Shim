/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface TypeElement extends javax.lang.model.element.Element, javax.lang.model.element.Parameterizable, javax.lang.model.element.QualifiedNameable {
  javax.lang.model.type.TypeMirror asType();
  java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements();
  javax.lang.model.element.Element getEnclosingElement();
  java.util.List<? extends javax.lang.model.type.TypeMirror> getInterfaces();
  javax.lang.model.element.NestingKind getNestingKind();
  default java.util.List<? extends javax.lang.model.type.TypeMirror> getPermittedSubclasses() {
    return java.util.Collections.emptyList();
  }
  javax.lang.model.element.Name getQualifiedName();
  default java.util.List<? extends javax.lang.model.element.RecordComponentElement> getRecordComponents() {
    return java.util.Collections.emptyList();
  }
  javax.lang.model.element.Name getSimpleName();
  javax.lang.model.type.TypeMirror getSuperclass();
  java.util.List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters();
}
