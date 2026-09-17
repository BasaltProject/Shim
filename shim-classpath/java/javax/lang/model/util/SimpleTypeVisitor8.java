/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleTypeVisitor8<R, P> extends javax.lang.model.util.SimpleTypeVisitor7<R, P> {
  protected SimpleTypeVisitor8() {
  }
  protected SimpleTypeVisitor8(R r) {
    super(r);
  }

  public R visitIntersection(javax.lang.model.type.IntersectionType intersectionType, P p) {
    return defaultAction(intersectionType, p);
  }
}
