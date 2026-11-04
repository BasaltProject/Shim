/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public interface IntersectionType extends javax.lang.model.type.TypeMirror {
  java.util.List<? extends javax.lang.model.type.TypeMirror> getBounds();
}
