/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface VariableTree extends com.sun.source.tree.StatementTree {
  com.sun.source.tree.ExpressionTree getInitializer();
  com.sun.source.tree.ModifiersTree getModifiers();
  javax.lang.model.element.Name getName();
  com.sun.source.tree.ExpressionTree getNameExpression();
  com.sun.source.tree.Tree getType();
}
