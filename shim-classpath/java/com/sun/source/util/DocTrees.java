/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public abstract class DocTrees extends com.sun.source.util.Trees {
  public DocTrees() {
  }

  public abstract java.text.BreakIterator getBreakIterator();
  public abstract java.lang.String getCharacters(com.sun.source.doctree.EntityTree entityTree);
  public abstract javax.lang.model.util.Elements.DocCommentKind getDocCommentKind(com.sun.source.util.TreePath treePath);
  public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(com.sun.source.util.TreePath treePath);
  public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.lang.model.element.Element element, java.lang.String string) throws java.io.IOException;
  public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.lang.model.element.Element element);
  public abstract com.sun.source.doctree.DocCommentTree getDocCommentTree(javax.tools.FileObject fileObject);
  public abstract com.sun.source.util.DocTreeFactory getDocTreeFactory();
  public abstract com.sun.source.util.DocTreePath getDocTreePath(javax.tools.FileObject fileObject, javax.lang.model.element.PackageElement packageElement);
  public abstract javax.lang.model.element.Element getElement(com.sun.source.util.DocTreePath docTreePath);
  public abstract java.util.List<com.sun.source.doctree.DocTree> getFirstSentence(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  public abstract com.sun.source.util.DocSourcePositions getSourcePositions();
  public abstract javax.lang.model.type.TypeMirror getType(com.sun.source.util.DocTreePath docTreePath);
  public static com.sun.source.util.DocTrees instance(javax.annotation.processing.ProcessingEnvironment processingEnvironment) {
    throw new UnsupportedOperationException();
  }
  public static com.sun.source.util.DocTrees instance(javax.tools.JavaCompiler.CompilationTask compilationTask) {
    if (!(compilationTask instanceof shim.javac.JavacTaskImpl)) {
      throw new IllegalArgumentException();
    }
    return shim.javac.TreesImpl.instance((shim.javac.JavacTaskImpl) compilationTask);
  }
  public abstract void printMessage(javax.tools.Diagnostic.Kind kind, java.lang.CharSequence charSequence, com.sun.source.doctree.DocTree docTree, com.sun.source.doctree.DocCommentTree docCommentTree, com.sun.source.tree.CompilationUnitTree compilationUnitTree);
  public abstract void setBreakIterator(java.text.BreakIterator breakIterator);
}
