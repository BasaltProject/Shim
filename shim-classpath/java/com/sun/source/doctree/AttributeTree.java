/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface AttributeTree extends com.sun.source.doctree.DocTree {
  javax.lang.model.element.Name getName();
  java.util.List<? extends com.sun.source.doctree.DocTree> getValue();
  com.sun.source.doctree.AttributeTree.ValueKind getValueKind();

  public enum ValueKind {
    EMPTY, UNQUOTED, SINGLE, DOUBLE;


  }
}
