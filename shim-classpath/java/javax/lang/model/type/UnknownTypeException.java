/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public class UnknownTypeException extends javax.lang.model.UnknownEntityException {
  private static final long serialVersionUID = 269L;

  private transient javax.lang.model.type.TypeMirror entity;
  private transient java.lang.Object parameter;

  public UnknownTypeException(javax.lang.model.type.TypeMirror e, java.lang.Object object) {
    super("Unknown type: " + e);
    entity = e;
    parameter = object;
  }

  public javax.lang.model.type.TypeMirror getUnknownType() {
    return entity;
  }

  public java.lang.Object getArgument() {
    return parameter;
  }
}
