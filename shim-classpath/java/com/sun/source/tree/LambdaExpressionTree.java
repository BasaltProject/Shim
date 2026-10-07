/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface LambdaExpressionTree extends com.sun.source.tree.ExpressionTree {
  com.sun.source.tree.Tree getBody();
  com.sun.source.tree.LambdaExpressionTree.BodyKind getBodyKind();
  java.util.List<? extends com.sun.source.tree.VariableTree> getParameters();

  public enum BodyKind {
    EXPRESSION, STATEMENT;


  }
}
