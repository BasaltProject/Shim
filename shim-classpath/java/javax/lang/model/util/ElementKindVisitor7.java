/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementKindVisitor7<R, P> extends javax.lang.model.util.ElementKindVisitor6<R, P> {
  protected ElementKindVisitor7() {
  }
  protected ElementKindVisitor7(R r) {
    super(r);
  }

  public R visitVariableAsResourceVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
}
