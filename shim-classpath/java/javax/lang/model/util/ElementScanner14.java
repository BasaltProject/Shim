/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementScanner14<R, P> extends javax.lang.model.util.ElementScanner9<R, P> {
  protected ElementScanner14() {
  }
  protected ElementScanner14(R r) {
    super(r);
  }

  public R visitExecutable(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return scan(createScanningList(executableElement, executableElement.getParameters()), p);
  }
  public R visitRecordComponent(javax.lang.model.element.RecordComponentElement recordComponentElement, P p) {
    return scan(recordComponentElement.getEnclosedElements(), p);
  }
  public R visitType(javax.lang.model.element.TypeElement typeElement, P p) {
    return scan(createScanningList(typeElement, typeElement.getEnclosedElements()), p);
  }

  private java.util.List<? extends javax.lang.model.element.Element> createScanningList(
      javax.lang.model.element.Parameterizable element,
      java.util.List<? extends javax.lang.model.element.Element> toBeScanned) {
    java.util.List<? extends javax.lang.model.element.TypeParameterElement> typeParameters = element.getTypeParameters();
    if (typeParameters.isEmpty()) {
      return toBeScanned;
    }
    java.util.List<javax.lang.model.element.Element> scanningList = new java.util.ArrayList<>(typeParameters);
    scanningList.addAll(toBeScanned);
    return scanningList;
  }
}
