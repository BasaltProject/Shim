/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface WhileLoopTree extends com.sun.source.tree.StatementTree {
  com.sun.source.tree.ExpressionTree getCondition();
  com.sun.source.tree.StatementTree getStatement();
}
