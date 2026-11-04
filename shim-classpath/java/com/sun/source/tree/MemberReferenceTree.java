/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface MemberReferenceTree extends com.sun.source.tree.ExpressionTree {
  com.sun.source.tree.MemberReferenceTree.ReferenceMode getMode();
  javax.lang.model.element.Name getName();
  com.sun.source.tree.ExpressionTree getQualifierExpression();
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getTypeArguments();

  public enum ReferenceMode {
    INVOKE, NEW;


  }
}
