/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface ModuleTree extends com.sun.source.tree.Tree {
  java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations();
  java.util.List<? extends com.sun.source.tree.DirectiveTree> getDirectives();
  com.sun.source.tree.ModuleTree.ModuleKind getModuleType();
  com.sun.source.tree.ExpressionTree getName();

  public enum ModuleKind {
    OPEN, STRONG;


  }
}
