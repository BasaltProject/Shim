/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface PackageTree extends com.sun.source.tree.Tree {
  java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations();
  com.sun.source.tree.ExpressionTree getPackageName();
}
