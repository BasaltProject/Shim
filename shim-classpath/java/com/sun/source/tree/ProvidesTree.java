/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface ProvidesTree extends com.sun.source.tree.DirectiveTree {
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getImplementationNames();
  com.sun.source.tree.ExpressionTree getServiceName();
}
