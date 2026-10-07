/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public final class TaskEvent {
  public enum Kind {
    PARSE, ENTER, ANALYZE, GENERATE, ANNOTATION_PROCESSING, ANNOTATION_PROCESSING_ROUND, COMPILATION;
  }

  private final Kind kind;
  private final javax.tools.JavaFileObject file;
  private final com.sun.source.tree.CompilationUnitTree unit;
  private final javax.lang.model.element.TypeElement clazz;

  public TaskEvent(Kind kind) {
    this(kind, null, null, null);
  }

  public TaskEvent(Kind kind, javax.tools.JavaFileObject sourceFile) {
    this(kind, sourceFile, null, null);
  }

  public TaskEvent(Kind kind, com.sun.source.tree.CompilationUnitTree unit) {
    this(kind, unit.getSourceFile(), unit, null);
  }

  public TaskEvent(Kind kind, com.sun.source.tree.CompilationUnitTree unit, javax.lang.model.element.TypeElement clazz) {
    this(kind, unit.getSourceFile(), unit, clazz);
  }

  private TaskEvent(Kind kind, javax.tools.JavaFileObject file, com.sun.source.tree.CompilationUnitTree unit,
      javax.lang.model.element.TypeElement clazz) {
    this.kind = kind;
    this.file = file;
    this.unit = unit;
    this.clazz = clazz;
  }

  public Kind getKind() {
    return kind;
  }

  public javax.tools.JavaFileObject getSourceFile() {
    return file;
  }

  public com.sun.source.tree.CompilationUnitTree getCompilationUnit() {
    return unit;
  }

  public javax.lang.model.element.TypeElement getTypeElement() {
    return clazz;
  }

  @Override
  public String toString() {
    return "TaskEvent[" + kind + "," + file + "," + clazz + "]";
  }
}
