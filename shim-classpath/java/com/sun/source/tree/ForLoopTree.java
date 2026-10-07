/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface ForLoopTree extends com.sun.source.tree.StatementTree {
  com.sun.source.tree.ExpressionTree getCondition();
  java.util.List<? extends com.sun.source.tree.StatementTree> getInitializer();
  com.sun.source.tree.StatementTree getStatement();
  java.util.List<? extends com.sun.source.tree.ExpressionStatementTree> getUpdate();
}
