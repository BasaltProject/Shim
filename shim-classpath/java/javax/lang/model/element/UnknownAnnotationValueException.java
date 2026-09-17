/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public class UnknownAnnotationValueException extends javax.lang.model.UnknownEntityException {
  private static final long serialVersionUID = 269L;

  private transient javax.lang.model.element.AnnotationValue entity;
  private transient java.lang.Object parameter;

  public UnknownAnnotationValueException(javax.lang.model.element.AnnotationValue e, java.lang.Object object) {
    super("Unknown annotation value: " + e);
    entity = e;
    parameter = object;
  }

  public javax.lang.model.element.AnnotationValue getUnknownAnnotationValue() {
    return entity;
  }

  public java.lang.Object getArgument() {
    return parameter;
  }
}
