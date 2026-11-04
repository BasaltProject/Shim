/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface NewClassTree extends com.sun.source.tree.ExpressionTree {
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments();
  com.sun.source.tree.ClassTree getClassBody();
  com.sun.source.tree.ExpressionTree getEnclosingExpression();
  com.sun.source.tree.ExpressionTree getIdentifier();
  java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments();
}
