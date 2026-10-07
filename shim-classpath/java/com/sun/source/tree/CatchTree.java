/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface CatchTree extends com.sun.source.tree.Tree {
  com.sun.source.tree.BlockTree getBlock();
  com.sun.source.tree.VariableTree getParameter();
}
