/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class TreePathScanner<R, P> extends TreeScanner<R, P> {
  private TreePath path;

  public TreePathScanner() {
  }

  public R scan(TreePath path, P p) {
    this.path = path;
    try {
      return path.getLeaf().accept(this, p);
    } finally {
      this.path = null;
    }
  }

  @Override
  public R scan(com.sun.source.tree.Tree tree, P p) {
    if (tree == null) {
      return null;
    }
    TreePath prev = path;
    path = new TreePath(path, tree);
    try {
      return tree.accept(this, p);
    } finally {
      path = prev;
    }
  }

  public TreePath getCurrentPath() {
    return path;
  }
}
