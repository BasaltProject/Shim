/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface CaseTree extends com.sun.source.tree.Tree {
  default com.sun.source.tree.Tree getBody() {
    return null;
  }
  default com.sun.source.tree.CaseTree.CaseKind getCaseKind() {
    return CaseKind.STATEMENT;
  }
  com.sun.source.tree.ExpressionTree getExpression();
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getExpressions();
  com.sun.source.tree.ExpressionTree getGuard();
  java.util.List<? extends com.sun.source.tree.CaseLabelTree> getLabels();
  java.util.List<? extends com.sun.source.tree.StatementTree> getStatements();

  public enum CaseKind {
    STATEMENT, RULE;


  }
}
