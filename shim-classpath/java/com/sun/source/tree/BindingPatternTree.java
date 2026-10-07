/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface BindingPatternTree extends com.sun.source.tree.PatternTree {
  com.sun.source.tree.VariableTree getVariable();
}
