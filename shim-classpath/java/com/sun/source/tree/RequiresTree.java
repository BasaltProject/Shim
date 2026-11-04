/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface RequiresTree extends com.sun.source.tree.DirectiveTree {
  com.sun.source.tree.ExpressionTree getModuleName();
  boolean isStatic();
  boolean isTransitive();
}
