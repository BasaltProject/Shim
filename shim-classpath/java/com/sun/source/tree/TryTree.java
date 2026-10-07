/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface TryTree extends com.sun.source.tree.StatementTree {
  com.sun.source.tree.BlockTree getBlock();
  java.util.List<? extends com.sun.source.tree.CatchTree> getCatches();
  com.sun.source.tree.BlockTree getFinallyBlock();
  java.util.List<? extends com.sun.source.tree.Tree> getResources();
}
