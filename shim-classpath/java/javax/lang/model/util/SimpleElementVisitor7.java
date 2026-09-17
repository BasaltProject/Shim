/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleElementVisitor7<R, P> extends javax.lang.model.util.SimpleElementVisitor6<R, P> {
  protected SimpleElementVisitor7() {
  }
  protected SimpleElementVisitor7(R r) {
    super(r);
  }

  public R visitVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
}
