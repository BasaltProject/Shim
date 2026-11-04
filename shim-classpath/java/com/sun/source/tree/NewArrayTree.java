/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface NewArrayTree extends com.sun.source.tree.ExpressionTree {
  java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations();
  java.util.List<? extends java.util.List<? extends com.sun.source.tree.AnnotationTree>> getDimAnnotations();
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getDimensions();
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getInitializers();
  com.sun.source.tree.Tree getType();
}
