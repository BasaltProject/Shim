/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface AnnotationMirror {
  javax.lang.model.type.DeclaredType getAnnotationType();
  java.util.Map<? extends javax.lang.model.element.ExecutableElement, ? extends javax.lang.model.element.AnnotationValue> getElementValues();
}
