/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public abstract class AbstractElementVisitor6<R, P> implements javax.lang.model.element.ElementVisitor<R, P> {
  protected AbstractElementVisitor6() {
  }

  public final R visit(javax.lang.model.element.Element element, P p) {
    return element.accept(this, p);
  }
  public final R visit(javax.lang.model.element.Element element) {
    return element.accept(this, null);
  }
  public R visitModule(javax.lang.model.element.ModuleElement moduleElement, P p) {
    return visitUnknown(moduleElement, p);
  }
  public R visitRecordComponent(javax.lang.model.element.RecordComponentElement recordComponentElement, P p) {
    return visitUnknown(recordComponentElement, p);
  }
  public R visitUnknown(javax.lang.model.element.Element element, P p) {
    throw new javax.lang.model.element.UnknownElementException(element, p);
  }
}
