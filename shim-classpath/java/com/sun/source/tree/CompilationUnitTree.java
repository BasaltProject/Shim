/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface CompilationUnitTree extends com.sun.source.tree.Tree {
  java.util.List<? extends com.sun.source.tree.ImportTree> getImports();
  com.sun.source.tree.LineMap getLineMap();
  default com.sun.source.tree.ModuleTree getModule() {
    return null;
  }
  com.sun.source.tree.PackageTree getPackage();
  java.util.List<? extends com.sun.source.tree.AnnotationTree> getPackageAnnotations();
  com.sun.source.tree.ExpressionTree getPackageName();
  javax.tools.JavaFileObject getSourceFile();
  java.util.List<? extends com.sun.source.tree.Tree> getTypeDecls();
}
