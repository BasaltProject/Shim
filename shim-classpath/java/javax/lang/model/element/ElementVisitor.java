/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface ElementVisitor<R, P> {
  R visit(javax.lang.model.element.Element element, P p);
  default R visit(javax.lang.model.element.Element element) {
    return visit(element, null);
  }
  R visitExecutable(javax.lang.model.element.ExecutableElement executableElement, P p);
  default R visitModule(javax.lang.model.element.ModuleElement moduleElement, P p) {
    return visitUnknown(moduleElement, p);
  }
  R visitPackage(javax.lang.model.element.PackageElement packageElement, P p);
  default R visitRecordComponent(javax.lang.model.element.RecordComponentElement recordComponentElement, P p) {
    return visitUnknown(recordComponentElement, p);
  }
  R visitType(javax.lang.model.element.TypeElement typeElement, P p);
  R visitTypeParameter(javax.lang.model.element.TypeParameterElement typeParameterElement, P p);
  R visitUnknown(javax.lang.model.element.Element element, P p);
  R visitVariable(javax.lang.model.element.VariableElement variableElement, P p);
}
