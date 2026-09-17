/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface Parameterizable extends javax.lang.model.element.Element {
  java.util.List<? extends javax.lang.model.element.TypeParameterElement> getTypeParameters();
}
