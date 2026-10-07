/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public abstract class JavacTask implements javax.tools.JavaCompiler.CompilationTask {
  protected JavacTask() {
  }

  public abstract void addTaskListener(com.sun.source.util.TaskListener taskListener);
  public abstract java.lang.Iterable<? extends javax.lang.model.element.Element> analyze() throws java.io.IOException;
  public abstract java.lang.Iterable<? extends javax.tools.JavaFileObject> generate() throws java.io.IOException;
  public abstract javax.lang.model.util.Elements getElements();
  public abstract javax.lang.model.type.TypeMirror getTypeMirror(java.lang.Iterable<? extends com.sun.source.tree.Tree> iterable);
  public abstract javax.lang.model.util.Types getTypes();
  public static com.sun.source.util.JavacTask instance(javax.annotation.processing.ProcessingEnvironment processingEnvironment) {
    throw new UnsupportedOperationException();
  }
  public abstract java.lang.Iterable<? extends com.sun.source.tree.CompilationUnitTree> parse() throws java.io.IOException;
  public abstract void removeTaskListener(com.sun.source.util.TaskListener taskListener);
  public void setParameterNameProvider(com.sun.source.util.ParameterNameProvider parameterNameProvider) {
    throw new UnsupportedOperationException();
  }
  public abstract void setTaskListener(com.sun.source.util.TaskListener taskListener);
}
