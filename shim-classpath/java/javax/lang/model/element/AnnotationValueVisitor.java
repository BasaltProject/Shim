/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface AnnotationValueVisitor<R, P> {
  R visit(javax.lang.model.element.AnnotationValue annotationValue, P p);
  default R visit(javax.lang.model.element.AnnotationValue annotationValue) {
    return visit(annotationValue, null);
  }
  R visitAnnotation(javax.lang.model.element.AnnotationMirror annotationMirror, P p);
  R visitArray(java.util.List<? extends javax.lang.model.element.AnnotationValue> list, P p);
  R visitBoolean(boolean flag, P p);
  R visitByte(byte b, P p);
  R visitChar(char c, P p);
  R visitDouble(double d, P p);
  R visitEnumConstant(javax.lang.model.element.VariableElement variableElement, P p);
  R visitFloat(float f, P p);
  R visitInt(int i, P p);
  R visitLong(long l, P p);
  R visitShort(short s, P p);
  R visitString(java.lang.String string, P p);
  R visitType(javax.lang.model.type.TypeMirror typeMirror, P p);
  R visitUnknown(javax.lang.model.element.AnnotationValue annotationValue, P p);
}
