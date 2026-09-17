/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementKindVisitor9<R, P> extends javax.lang.model.util.ElementKindVisitor8<R, P> {
  protected ElementKindVisitor9() {
  }
  protected ElementKindVisitor9(R r) {
    super(r);
  }

  public R visitModule(javax.lang.model.element.ModuleElement moduleElement, P p) {
    return defaultAction(moduleElement, p);
  }
}
