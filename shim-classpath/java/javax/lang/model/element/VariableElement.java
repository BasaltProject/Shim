/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface VariableElement extends javax.lang.model.element.Element {
  javax.lang.model.type.TypeMirror asType();
  java.lang.Object getConstantValue();
  javax.lang.model.element.Element getEnclosingElement();
  javax.lang.model.element.Name getSimpleName();
  default boolean isUnnamed() {
    return false;
  }
}
