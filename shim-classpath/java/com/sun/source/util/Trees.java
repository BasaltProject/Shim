/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public abstract class Trees {
  public Trees() {
  }

  public abstract java.lang.String getDocComment(com.sun.source.util.TreePath treePath);
  public abstract javax.lang.model.element.Element getElement(com.sun.source.util.TreePath treePath);
  public abstract javax.lang.model.type.TypeMirror getLub(com.sun.source.tree.CatchTree catchTree);
  public abstract javax.lang.model.type.TypeMirror getOriginalType(javax.lang.model.type.ErrorType errorType);
  public abstract com.sun.source.util.TreePath getPath(com.sun.source.tree.CompilationUnitTree compilationUnitTree, com.sun.source.tree.Tree tree);
  public abstract com.sun.source.util.TreePath getPath(javax.lang.model.element.Element element, javax.lang.model.element.AnnotationMirror annotationMirror, javax.lang.model.element.AnnotationValue annotationValue);
  public abstract com.sun.source.util.TreePath getPath(javax.lang.model.element.Element element, javax.lang.model.element.AnnotationMirror annotationMirror);
  public abstract com.sun.source.util.TreePath getPath(javax.lang.model.element.Element element);
  public abstract com.sun.source.tree.Scope getScope(com.sun.source.util.TreePath treePath);
  public abstract com.sun.source.util.SourcePositions getSourcePositions();
  public abstract com.sun.source.tree.Tree getTree(javax.lang.model.element.Element element, javax.lang.model.element.AnnotationMirror annotationMirror, javax.lang.model.element.AnnotationValue annotationValue);
  public abstract com.sun.source.tree.Tree getTree(javax.lang.model.element.Element element, javax.lang.model.element.AnnotationMirror annotationMirror);
  public abstract com.sun.source.tree.Tree getTree(javax.lang.model.element.Element element);
  public abstract com.sun.source.tree.MethodTree getTree(javax.lang.model.element.ExecutableElement executableElement);
  public abstract com.sun.source.tree.ClassTree getTree(javax.lang.model.element.TypeElement typeElement);
  public abstract javax.lang.model.type.TypeMirror getTypeMirror(com.sun.source.util.TreePath treePath);
  public static com.sun.source.util.Trees instance(javax.annotation.processing.ProcessingEnvironment processingEnvironment) {
    throw new UnsupportedOperationException();
  }
  public static com.sun.source.util.Trees instance(javax.tools.JavaCompiler.CompilationTask compilationTask) {
    if (!(compilationTask instanceof shim.javac.JavacTaskImpl)) {
      throw new IllegalArgumentException();
    }
    return shim.javac.TreesImpl.instance((shim.javac.JavacTaskImpl) compilationTask);
  }
  public abstract boolean isAccessible(com.sun.source.tree.Scope scope, javax.lang.model.element.Element element, javax.lang.model.type.DeclaredType declaredType);
  public abstract boolean isAccessible(com.sun.source.tree.Scope scope, javax.lang.model.element.TypeElement typeElement);
  public abstract void printMessage(javax.tools.Diagnostic.Kind kind, java.lang.CharSequence charSequence, com.sun.source.tree.Tree tree, com.sun.source.tree.CompilationUnitTree compilationUnitTree);
}
