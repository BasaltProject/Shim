/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface ClassTree extends com.sun.source.tree.StatementTree {
  com.sun.source.tree.Tree getExtendsClause();
  java.util.List<? extends com.sun.source.tree.Tree> getImplementsClause();
  java.util.List<? extends com.sun.source.tree.Tree> getMembers();
  com.sun.source.tree.ModifiersTree getModifiers();
  default java.util.List<? extends com.sun.source.tree.Tree> getPermitsClause() {
    return java.util.Collections.emptyList();
  }
  javax.lang.model.element.Name getSimpleName();
  java.util.List<? extends com.sun.source.tree.TypeParameterTree> getTypeParameters();
}
