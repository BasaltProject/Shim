/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class TreePath implements java.lang.Iterable<com.sun.source.tree.Tree> {
  private final com.sun.source.tree.CompilationUnitTree compilationUnit;
  private final com.sun.source.tree.Tree leaf;
  private final TreePath parent;

  public static TreePath getPath(com.sun.source.tree.CompilationUnitTree unit, com.sun.source.tree.Tree target) {
    return getPath(new TreePath(unit), target);
  }

  public static TreePath getPath(TreePath path, com.sun.source.tree.Tree target) {
    path.getClass();
    target.getClass();
    if (path.getLeaf() == target) {
      return path;
    }
    final TreePath[] found = new TreePath[1];
    try {
      new TreePathScanner<Void, com.sun.source.tree.Tree>() {
        @Override
        public Void scan(com.sun.source.tree.Tree tree, com.sun.source.tree.Tree t) {
          if (tree == t) {
            found[0] = new TreePath(getCurrentPath(), t);
            throw new FoundPath();
          }
          return super.scan(tree, t);
        }
      }.scan(path, target);
    } catch (FoundPath e) {
      return found[0];
    }
    return null;
  }

  private static final class FoundPath extends RuntimeException {
    private static final long serialVersionUID = 1L;

    FoundPath() {
      super(null, null, false, false);
    }
  }

  public TreePath(com.sun.source.tree.CompilationUnitTree node) {
    this(null, node);
  }

  public TreePath(TreePath parent, com.sun.source.tree.Tree t) {
    if (t.getKind() == com.sun.source.tree.Tree.Kind.COMPILATION_UNIT) {
      compilationUnit = (com.sun.source.tree.CompilationUnitTree) t;
      this.parent = null;
    } else {
      compilationUnit = parent.compilationUnit;
      this.parent = parent;
    }
    this.leaf = t;
  }

  public com.sun.source.tree.CompilationUnitTree getCompilationUnit() {
    return compilationUnit;
  }

  public com.sun.source.tree.Tree getLeaf() {
    return leaf;
  }

  public TreePath getParentPath() {
    return parent;
  }

  @Override
  public java.util.Iterator<com.sun.source.tree.Tree> iterator() {
    return new java.util.Iterator<com.sun.source.tree.Tree>() {
      private TreePath next = TreePath.this;

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public com.sun.source.tree.Tree next() {
        if (next == null) {
          throw new java.util.NoSuchElementException();
        }
        com.sun.source.tree.Tree t = next.leaf;
        next = next.parent;
        return t;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
