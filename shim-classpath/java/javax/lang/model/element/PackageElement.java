/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface PackageElement extends javax.lang.model.element.Element, javax.lang.model.element.QualifiedNameable {
  javax.lang.model.type.TypeMirror asType();
  java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements();
  javax.lang.model.element.Element getEnclosingElement();
  javax.lang.model.element.Name getQualifiedName();
  javax.lang.model.element.Name getSimpleName();
  boolean isUnnamed();
}
