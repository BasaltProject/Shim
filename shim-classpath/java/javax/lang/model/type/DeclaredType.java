/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public interface DeclaredType extends javax.lang.model.type.ReferenceType {
  javax.lang.model.element.Element asElement();
  javax.lang.model.type.TypeMirror getEnclosingType();
  java.util.List<? extends javax.lang.model.type.TypeMirror> getTypeArguments();
}
