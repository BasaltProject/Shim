/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface ExecutableElement extends javax.lang.model.element.Element, javax.lang.model.element.Parameterizable {
  javax.lang.model.type.TypeMirror asType();
  javax.lang.model.element.AnnotationValue getDefaultValue();
  javax.lang.model.element.Element getEnclosingElement();
  java.util.List<? extends javax.lang.model.element.VariableElement> getParameters();
  javax.lang.model.type.TypeMirror getReceiverType();
  javax.lang.model.type.TypeMirror getReturnType();
  javax.lang.model.element.Name getSimpleName();
  java.util.List<? extends javax.lang.model.type.TypeMirror> getThrownTypes();
  java.util.List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters();
  boolean isDefault();
  boolean isVarArgs();
}
