/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public interface Elements {
  java.util.List<? extends javax.lang.model.element.AnnotationMirror> getAllAnnotationMirrors(javax.lang.model.element.Element element);
  java.util.List<? extends javax.lang.model.element.Element> getAllMembers(javax.lang.model.element.TypeElement typeElement);
  default java.util.Set<? extends javax.lang.model.element.ModuleElement> getAllModuleElements() {
    return java.util.Collections.emptySet();
  }
  default java.util.Set<? extends javax.lang.model.element.PackageElement> getAllPackageElements(java.lang.CharSequence charSequence) {
    java.util.Set<javax.lang.model.element.PackageElement> result = new java.util.LinkedHashSet<javax.lang.model.element.PackageElement>();
    for (javax.lang.model.element.ModuleElement module : getAllModuleElements()) {
      javax.lang.model.element.PackageElement pe = getPackageElement(module, charSequence);
      if (pe != null) {
        result.add(pe);
      }
    }
    return java.util.Collections.unmodifiableSet(result);
  }
  default java.util.Set<? extends javax.lang.model.element.TypeElement> getAllTypeElements(java.lang.CharSequence charSequence) {
    java.util.Set<javax.lang.model.element.TypeElement> result = new java.util.LinkedHashSet<javax.lang.model.element.TypeElement>();
    for (javax.lang.model.element.ModuleElement module : getAllModuleElements()) {
      javax.lang.model.element.TypeElement te = getTypeElement(module, charSequence);
      if (te != null) {
        result.add(te);
      }
    }
    return java.util.Collections.unmodifiableSet(result);
  }
  javax.lang.model.element.Name getBinaryName(javax.lang.model.element.TypeElement typeElement);
  java.lang.String getConstantExpression(java.lang.Object object);
  java.lang.String getDocComment(javax.lang.model.element.Element element);
  default javax.lang.model.util.Elements.DocCommentKind getDocCommentKind(javax.lang.model.element.Element element) {
    return getDocComment(element) == null ? null : DocCommentKind.TRADITIONAL;
  }
  java.util.Map<? extends javax.lang.model.element.ExecutableElement, ? extends javax.lang.model.element.AnnotationValue> getElementValuesWithDefaults(javax.lang.model.element.AnnotationMirror annotationMirror);
  default javax.lang.model.element.TypeElement getEnumConstantBody(javax.lang.model.element.VariableElement variableElement) {
    return null;
  }
  default javax.tools.JavaFileObject getFileObjectOf(javax.lang.model.element.Element element) {
    throw new UnsupportedOperationException();
  }
  default javax.lang.model.element.ModuleElement getModuleElement(java.lang.CharSequence charSequence) {
    return null;
  }
  default javax.lang.model.element.ModuleElement getModuleOf(javax.lang.model.element.Element element) {
    return null;
  }
  javax.lang.model.element.Name getName(java.lang.CharSequence charSequence);
  default javax.lang.model.util.Elements.Origin getOrigin(javax.lang.model.AnnotatedConstruct annotatedConstruct, javax.lang.model.element.AnnotationMirror annotationMirror) {
    return Origin.EXPLICIT;
  }
  default javax.lang.model.util.Elements.Origin getOrigin(javax.lang.model.element.Element element) {
    return Origin.EXPLICIT;
  }
  default javax.lang.model.util.Elements.Origin getOrigin(javax.lang.model.element.ModuleElement moduleElement, javax.lang.model.element.ModuleElement.Directive directive) {
    return Origin.EXPLICIT;
  }
  default javax.lang.model.element.TypeElement getOutermostTypeElement(javax.lang.model.element.Element element) {
    javax.lang.model.element.ElementKind kind = element.getKind();
    if (kind == javax.lang.model.element.ElementKind.PACKAGE || kind == javax.lang.model.element.ElementKind.MODULE) {
      return null;
    }
    javax.lang.model.element.Element enclosing = element;
    javax.lang.model.element.TypeElement outermost = null;
    while (enclosing != null) {
      if (enclosing instanceof javax.lang.model.element.TypeElement) {
        outermost = (javax.lang.model.element.TypeElement) enclosing;
        if (outermost.getNestingKind() == javax.lang.model.element.NestingKind.TOP_LEVEL) {
          return outermost;
        }
      }
      enclosing = enclosing.getEnclosingElement();
    }
    return outermost;
  }
  javax.lang.model.element.PackageElement getPackageElement(java.lang.CharSequence charSequence);
  default javax.lang.model.element.PackageElement getPackageElement(javax.lang.model.element.ModuleElement moduleElement, java.lang.CharSequence charSequence) {
    return null;
  }
  javax.lang.model.element.PackageElement getPackageOf(javax.lang.model.element.Element element);
  javax.lang.model.element.TypeElement getTypeElement(java.lang.CharSequence charSequence);
  default javax.lang.model.element.TypeElement getTypeElement(javax.lang.model.element.ModuleElement moduleElement, java.lang.CharSequence charSequence) {
    return null;
  }
  boolean hides(javax.lang.model.element.Element element, javax.lang.model.element.Element element2);
  default boolean isAutomaticModule(javax.lang.model.element.ModuleElement moduleElement) {
    return false;
  }
  default boolean isBridge(javax.lang.model.element.ExecutableElement executableElement) {
    return false;
  }
  default boolean isCanonicalConstructor(javax.lang.model.element.ExecutableElement executableElement) {
    return false;
  }
  default boolean isCompactConstructor(javax.lang.model.element.ExecutableElement executableElement) {
    return false;
  }
  boolean isDeprecated(javax.lang.model.element.Element element);
  boolean isFunctionalInterface(javax.lang.model.element.TypeElement typeElement);
  boolean overrides(javax.lang.model.element.ExecutableElement executableElement, javax.lang.model.element.ExecutableElement executableElement2, javax.lang.model.element.TypeElement typeElement);
  void printElements(java.io.Writer writer, javax.lang.model.element.Element... elements);
  default javax.lang.model.element.RecordComponentElement recordComponentFor(javax.lang.model.element.ExecutableElement executableElement) {
    if (executableElement.getEnclosingElement().getKind() == javax.lang.model.element.ElementKind.RECORD) {
      for (javax.lang.model.element.RecordComponentElement rec : ElementFilter.recordComponentsIn(executableElement.getEnclosingElement().getEnclosedElements())) {
        if (java.util.Objects.equals(rec.getAccessor(), executableElement)) {
          return rec;
        }
      }
    }
    return null;
  }

  public enum DocCommentKind {
    END_OF_LINE, TRADITIONAL;


  }

  public enum Origin {
    EXPLICIT, MANDATED, SYNTHETIC;


    public boolean isDeclared() {
      return this != SYNTHETIC;
    }
  }
}
