/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public enum ElementKind {
  PACKAGE, ENUM, CLASS, ANNOTATION_TYPE, INTERFACE, ENUM_CONSTANT, FIELD, PARAMETER, LOCAL_VARIABLE, EXCEPTION_PARAMETER, METHOD, CONSTRUCTOR, STATIC_INIT, INSTANCE_INIT, TYPE_PARAMETER, OTHER, RESOURCE_VARIABLE, MODULE, RECORD, RECORD_COMPONENT, BINDING_VARIABLE;


  public boolean isClass() {
    return this == CLASS || this == ENUM || this == RECORD;
  }
  public boolean isDeclaredType() {
    return isClass() || isInterface();
  }
  public boolean isExecutable() {
    return this == METHOD || this == CONSTRUCTOR || this == STATIC_INIT || this == INSTANCE_INIT;
  }
  public boolean isField() {
    return this == FIELD || this == ENUM_CONSTANT;
  }
  public boolean isInitializer() {
    return this == STATIC_INIT || this == INSTANCE_INIT;
  }
  public boolean isInterface() {
    return this == INTERFACE || this == ANNOTATION_TYPE;
  }
  public boolean isVariable() {
    return this == ENUM_CONSTANT || this == FIELD || this == PARAMETER || this == LOCAL_VARIABLE || this == EXCEPTION_PARAMETER || this == RESOURCE_VARIABLE || this == BINDING_VARIABLE;
  }
}
