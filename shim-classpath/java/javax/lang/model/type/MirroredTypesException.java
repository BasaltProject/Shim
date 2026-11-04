/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public class MirroredTypesException extends java.lang.RuntimeException {
  private static final long serialVersionUID = 269L;

  transient java.util.List<? extends javax.lang.model.type.TypeMirror> types;

  MirroredTypesException(String message, javax.lang.model.type.TypeMirror type) {
    super(message);
    java.util.List<javax.lang.model.type.TypeMirror> tmp = new java.util.ArrayList<javax.lang.model.type.TypeMirror>();
    tmp.add(type);
    types = java.util.Collections.unmodifiableList(tmp);
  }

  public MirroredTypesException(java.util.List<? extends javax.lang.model.type.TypeMirror> list) {
    super("Attempt to access Class objects for TypeMirrors " + (list = new java.util.ArrayList<javax.lang.model.type.TypeMirror>(list)).toString());
    types = java.util.Collections.unmodifiableList(list);
  }

  protected MirroredTypesException(String message, java.util.List<? extends javax.lang.model.type.TypeMirror> list) {
    super(message);
    types = java.util.Collections.unmodifiableList(new java.util.ArrayList<javax.lang.model.type.TypeMirror>(list));
  }

  public java.util.List<? extends javax.lang.model.type.TypeMirror> getTypeMirrors() {
    return types;
  }
}
