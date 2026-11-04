/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface RecordComponentElement extends javax.lang.model.element.Element {
  javax.lang.model.type.TypeMirror asType();
  javax.lang.model.element.ExecutableElement getAccessor();
  javax.lang.model.element.Element getEnclosingElement();
  javax.lang.model.element.Name getSimpleName();
}
