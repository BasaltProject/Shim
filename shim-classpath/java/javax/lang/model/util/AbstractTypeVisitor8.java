/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public abstract class AbstractTypeVisitor8<R, P> extends javax.lang.model.util.AbstractTypeVisitor7<R, P> {
  protected AbstractTypeVisitor8() {
  }

  public abstract R visitIntersection(javax.lang.model.type.IntersectionType intersectionType, P p);
}
