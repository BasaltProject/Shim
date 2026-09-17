/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface TypeParameterElement extends javax.lang.model.element.Element {
  javax.lang.model.type.TypeMirror asType();
  java.util.List<? extends javax.lang.model.type.TypeMirror> getBounds();
  javax.lang.model.element.Element getEnclosingElement();
  javax.lang.model.element.Element getGenericElement();
}
