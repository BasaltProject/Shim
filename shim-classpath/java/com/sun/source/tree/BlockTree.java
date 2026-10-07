/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface BlockTree extends com.sun.source.tree.StatementTree {
  java.util.List<? extends com.sun.source.tree.StatementTree> getStatements();
  boolean isStatic();
}
