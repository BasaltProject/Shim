/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public class MirroredTypeException extends javax.lang.model.type.MirroredTypesException {
  private static final long serialVersionUID = 269L;

  private transient javax.lang.model.type.TypeMirror type;

  public MirroredTypeException(javax.lang.model.type.TypeMirror typeMirror) {
    super("Attempt to access Class object for TypeMirror " + typeMirror.toString(), typeMirror);
    type = typeMirror;
  }

  public javax.lang.model.type.TypeMirror getTypeMirror() {
    return type;
  }
}
