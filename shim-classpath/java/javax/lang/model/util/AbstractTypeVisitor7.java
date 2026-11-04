/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public abstract class AbstractTypeVisitor7<R, P> extends javax.lang.model.util.AbstractTypeVisitor6<R, P> {
  protected AbstractTypeVisitor7() {
  }

  public abstract R visitUnion(javax.lang.model.type.UnionType unionType, P p);
}
