/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class DocTreePath implements java.lang.Iterable<com.sun.source.doctree.DocTree> {
  private final TreePath treePath;
  private final com.sun.source.doctree.DocCommentTree doccomment;
  private final com.sun.source.doctree.DocTree leaf;
  private final DocTreePath parent;

  public static DocTreePath getPath(TreePath treePath, com.sun.source.doctree.DocCommentTree doc,
      com.sun.source.doctree.DocTree target) {
    return getPath(new DocTreePath(treePath, doc), target);
  }

  public static DocTreePath getPath(DocTreePath path, com.sun.source.doctree.DocTree target) {
    path.getClass();
    target.getClass();
    if (path.getLeaf() == target) {
      return path;
    }
    final DocTreePath[] found = new DocTreePath[1];
    try {
      new DocTreePathScanner<Void, com.sun.source.doctree.DocTree>() {
        @Override
        public Void scan(com.sun.source.doctree.DocTree tree, com.sun.source.doctree.DocTree t) {
          if (tree == t) {
            found[0] = new DocTreePath(getCurrentPath(), t);
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

  public DocTreePath(TreePath treePath, com.sun.source.doctree.DocCommentTree t) {
    if (treePath == null || t == null) {
      throw new NullPointerException();
    }
    if (t.getKind() == com.sun.source.doctree.DocTree.Kind.DOC_COMMENT) {
      this.treePath = treePath;
      this.parent = null;
    } else {
      throw new IllegalArgumentException();
    }
    this.leaf = t;
    this.doccomment = t;
  }

  public DocTreePath(DocTreePath parent, com.sun.source.doctree.DocTree t) {
    if (t.getKind() == com.sun.source.doctree.DocTree.Kind.DOC_COMMENT) {
      throw new IllegalArgumentException();
    }
    this.treePath = parent.treePath;
    this.doccomment = parent.doccomment;
    this.parent = parent;
    this.leaf = t;
  }

  public TreePath getTreePath() {
    return treePath;
  }

  public com.sun.source.doctree.DocCommentTree getDocComment() {
    return doccomment;
  }

  public com.sun.source.doctree.DocTree getLeaf() {
    return leaf;
  }

  public DocTreePath getParentPath() {
    return parent;
  }

  @Override
  public java.util.Iterator<com.sun.source.doctree.DocTree> iterator() {
    return new java.util.Iterator<com.sun.source.doctree.DocTree>() {
      private DocTreePath next = DocTreePath.this;

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public com.sun.source.doctree.DocTree next() {
        if (next == null) {
          throw new java.util.NoSuchElementException();
        }
        com.sun.source.doctree.DocTree t = next.leaf;
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
