/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class SimpleElementVisitor6<R, P> extends javax.lang.model.util.AbstractElementVisitor6<R, P> {
  protected final R DEFAULT_VALUE;

  protected SimpleElementVisitor6() {
    DEFAULT_VALUE = null;
  }
  protected SimpleElementVisitor6(R r) {
    DEFAULT_VALUE = r;
  }

  protected R defaultAction(javax.lang.model.element.Element element, P p) {
    return DEFAULT_VALUE;
  }
  public R visitExecutable(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return defaultAction(executableElement, p);
  }
  public R visitPackage(javax.lang.model.element.PackageElement packageElement, P p) {
    return defaultAction(packageElement, p);
  }
  public R visitType(javax.lang.model.element.TypeElement typeElement, P p) {
    return defaultAction(typeElement, p);
  }
  public R visitTypeParameter(javax.lang.model.element.TypeParameterElement typeParameterElement, P p) {
    return defaultAction(typeParameterElement, p);
  }
  public R visitVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
}
