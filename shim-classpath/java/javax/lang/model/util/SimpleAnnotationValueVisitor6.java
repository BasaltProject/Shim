/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleAnnotationValueVisitor6<R, P> extends javax.lang.model.util.AbstractAnnotationValueVisitor6<R, P> {
  protected final R DEFAULT_VALUE;

  protected SimpleAnnotationValueVisitor6() {
    DEFAULT_VALUE = null;
  }
  protected SimpleAnnotationValueVisitor6(R r) {
    DEFAULT_VALUE = r;
  }

  protected R defaultAction(java.lang.Object object, P p) {
    return DEFAULT_VALUE;
  }
  public R visitAnnotation(javax.lang.model.element.AnnotationMirror annotationMirror, P p) {
    return defaultAction(annotationMirror, p);
  }
  public R visitArray(java.util.List<? extends javax.lang.model.element.AnnotationValue> list, P p) {
    return defaultAction(list, p);
  }
  public R visitBoolean(boolean flag, P p) {
    return defaultAction(flag, p);
  }
  public R visitByte(byte b, P p) {
    return defaultAction(b, p);
  }
  public R visitChar(char c, P p) {
    return defaultAction(c, p);
  }
  public R visitDouble(double d, P p) {
    return defaultAction(d, p);
  }
  public R visitEnumConstant(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
  public R visitFloat(float f, P p) {
    return defaultAction(f, p);
  }
  public R visitInt(int i, P p) {
    return defaultAction(i, p);
  }
  public R visitLong(long l, P p) {
    return defaultAction(l, p);
  }
  public R visitShort(short s, P p) {
    return defaultAction(s, p);
  }
  public R visitString(java.lang.String string, P p) {
    return defaultAction(string, p);
  }
  public R visitType(javax.lang.model.type.TypeMirror typeMirror, P p) {
    return defaultAction(typeMirror, p);
  }
}
