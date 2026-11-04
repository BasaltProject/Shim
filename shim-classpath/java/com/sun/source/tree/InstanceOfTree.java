/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface InstanceOfTree extends com.sun.source.tree.ExpressionTree {
  com.sun.source.tree.ExpressionTree getExpression();
  com.sun.source.tree.PatternTree getPattern();
  com.sun.source.tree.Tree getType();
}
