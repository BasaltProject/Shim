/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementKindVisitor14<R, P> extends javax.lang.model.util.ElementKindVisitor9<R, P> {
  protected ElementKindVisitor14() {
  }
  protected ElementKindVisitor14(R r) {
    super(r);
  }

  public R visitRecordComponent(javax.lang.model.element.RecordComponentElement recordComponentElement, P p) {
    return defaultAction(recordComponentElement, p);
  }
  public R visitTypeAsRecord(javax.lang.model.element.TypeElement typeElement, P p) {
    return defaultAction(typeElement, p);
  }
  public R visitVariableAsBindingVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
}
