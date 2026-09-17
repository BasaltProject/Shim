/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleElementVisitor9<R, P> extends javax.lang.model.util.SimpleElementVisitor8<R, P> {
  protected SimpleElementVisitor9() {
  }
  protected SimpleElementVisitor9(R r) {
    super(r);
  }

  public R visitModule(javax.lang.model.element.ModuleElement moduleElement, P p) {
    return defaultAction(moduleElement, p);
  }
}
