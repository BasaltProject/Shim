/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface DocCommentTree extends com.sun.source.doctree.DocTree {
  java.util.List<? extends com.sun.source.doctree.DocTree> getBlockTags();
  java.util.List<? extends com.sun.source.doctree.DocTree> getBody();
  java.util.List<? extends com.sun.source.doctree.DocTree> getFirstSentence();
  default java.util.List<? extends com.sun.source.doctree.DocTree> getFullBody() {
    java.util.List<com.sun.source.doctree.DocTree> all = new java.util.ArrayList<com.sun.source.doctree.DocTree>(getFirstSentence());
    all.addAll(getBody());
    return all;
  }
  default java.util.List<? extends com.sun.source.doctree.DocTree> getPostamble() {
    return java.util.Collections.emptyList();
  }
  default java.util.List<? extends com.sun.source.doctree.DocTree> getPreamble() {
    return java.util.Collections.emptyList();
  }
}
