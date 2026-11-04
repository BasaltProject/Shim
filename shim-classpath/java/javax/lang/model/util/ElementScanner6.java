/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementScanner6<R, P> extends javax.lang.model.util.AbstractElementVisitor6<R, P> {
  protected final R DEFAULT_VALUE;

  protected ElementScanner6() {
    DEFAULT_VALUE = null;
  }
  protected ElementScanner6(R r) {
    DEFAULT_VALUE = r;
  }

  public final R scan(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable, P p) {
    R result = DEFAULT_VALUE;
    for (javax.lang.model.element.Element element : iterable) {
      result = scan(element, p);
    }
    return result;
  }
  public R scan(javax.lang.model.element.Element element, P p) {
    return element.accept(this, p);
  }
  public final R scan(javax.lang.model.element.Element element) {
    return scan(element, null);
  }
  public R visitExecutable(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return scan(executableElement.getParameters(), p);
  }
  public R visitPackage(javax.lang.model.element.PackageElement packageElement, P p) {
    return scan(packageElement.getEnclosedElements(), p);
  }
  public R visitRecordComponent(javax.lang.model.element.RecordComponentElement recordComponentElement, P p) {
    return scan(recordComponentElement.getEnclosedElements(), p);
  }
  public R visitType(javax.lang.model.element.TypeElement typeElement, P p) {
    return scan(typeElement.getEnclosedElements(), p);
  }
  public R visitTypeParameter(javax.lang.model.element.TypeParameterElement typeParameterElement, P p) {
    return scan(typeParameterElement.getEnclosedElements(), p);
  }
  public R visitVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    if (variableElement.getKind() != javax.lang.model.element.ElementKind.RESOURCE_VARIABLE) {
      return scan(variableElement.getEnclosedElements(), p);
    }
    return visitUnknown(variableElement, p);
  }
}
