/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleTypeVisitor7<R, P> extends javax.lang.model.util.SimpleTypeVisitor6<R, P> {
  protected SimpleTypeVisitor7() {
  }
  protected SimpleTypeVisitor7(R r) {
    super(r);
  }

  public R visitUnion(javax.lang.model.type.UnionType unionType, P p) {
    return defaultAction(unionType, p);
  }
}
