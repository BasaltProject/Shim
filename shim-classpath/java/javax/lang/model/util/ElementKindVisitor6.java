/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementKindVisitor6<R, P> extends javax.lang.model.util.SimpleElementVisitor6<R, P> {

  protected ElementKindVisitor6() {
  }
  protected ElementKindVisitor6(R r) {
    super(r);
  }

  public R visitExecutable(javax.lang.model.element.ExecutableElement executableElement, P p) {
    javax.lang.model.element.ElementKind k = executableElement.getKind();
    switch (k) {
    case CONSTRUCTOR:
      return visitExecutableAsConstructor(executableElement, p);
    case INSTANCE_INIT:
      return visitExecutableAsInstanceInit(executableElement, p);
    case METHOD:
      return visitExecutableAsMethod(executableElement, p);
    case STATIC_INIT:
      return visitExecutableAsStaticInit(executableElement, p);
    default:
      throw new AssertionError("Bad kind " + k + " for ExecutableElement" + executableElement);
    }
  }
  public R visitExecutableAsConstructor(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return defaultAction(executableElement, p);
  }
  public R visitExecutableAsInstanceInit(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return defaultAction(executableElement, p);
  }
  public R visitExecutableAsMethod(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return defaultAction(executableElement, p);
  }
  public R visitExecutableAsStaticInit(javax.lang.model.element.ExecutableElement executableElement, P p) {
    return defaultAction(executableElement, p);
  }
  public R visitPackage(javax.lang.model.element.PackageElement packageElement, P p) {
    return defaultAction(packageElement, p);
  }
  public R visitType(javax.lang.model.element.TypeElement typeElement, P p) {
    javax.lang.model.element.ElementKind k = typeElement.getKind();
    switch (k) {
    case ANNOTATION_TYPE:
      return visitTypeAsAnnotationType(typeElement, p);
    case CLASS:
      return visitTypeAsClass(typeElement, p);
    case ENUM:
      return visitTypeAsEnum(typeElement, p);
    case INTERFACE:
      return visitTypeAsInterface(typeElement, p);
    case RECORD:
      return visitTypeAsRecord(typeElement, p);
    default:
      throw new AssertionError("Bad kind " + k + " for TypeElement" + typeElement);
    }
  }
  public R visitTypeAsAnnotationType(javax.lang.model.element.TypeElement typeElement, P p) {
    return defaultAction(typeElement, p);
  }
  public R visitTypeAsClass(javax.lang.model.element.TypeElement typeElement, P p) {
    return defaultAction(typeElement, p);
  }
  public R visitTypeAsEnum(javax.lang.model.element.TypeElement typeElement, P p) {
    return defaultAction(typeElement, p);
  }
  public R visitTypeAsInterface(javax.lang.model.element.TypeElement typeElement, P p) {
    return defaultAction(typeElement, p);
  }
  public R visitTypeAsRecord(javax.lang.model.element.TypeElement typeElement, P p) {
    return visitUnknown(typeElement, p);
  }
  public R visitTypeParameter(javax.lang.model.element.TypeParameterElement typeParameterElement, P p) {
    return defaultAction(typeParameterElement, p);
  }
  public R visitVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    javax.lang.model.element.ElementKind k = variableElement.getKind();
    switch (k) {
    case ENUM_CONSTANT:
      return visitVariableAsEnumConstant(variableElement, p);
    case EXCEPTION_PARAMETER:
      return visitVariableAsExceptionParameter(variableElement, p);
    case FIELD:
      return visitVariableAsField(variableElement, p);
    case LOCAL_VARIABLE:
      return visitVariableAsLocalVariable(variableElement, p);
    case PARAMETER:
      return visitVariableAsParameter(variableElement, p);
    case RESOURCE_VARIABLE:
      return visitVariableAsResourceVariable(variableElement, p);
    case BINDING_VARIABLE:
      return visitVariableAsBindingVariable(variableElement, p);
    default:
      throw new AssertionError("Bad kind " + k + " for VariableElement" + variableElement);
    }
  }
  public R visitVariableAsBindingVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return visitUnknown(variableElement, p);
  }
  public R visitVariableAsEnumConstant(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
  public R visitVariableAsExceptionParameter(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
  public R visitVariableAsField(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
  public R visitVariableAsLocalVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
  public R visitVariableAsParameter(javax.lang.model.element.VariableElement variableElement, P p) {
    return defaultAction(variableElement, p);
  }
  public R visitVariableAsResourceVariable(javax.lang.model.element.VariableElement variableElement, P p) {
    return visitUnknown(variableElement, p);
  }
}
