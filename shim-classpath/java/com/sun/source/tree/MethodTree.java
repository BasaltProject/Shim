/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface MethodTree extends com.sun.source.tree.Tree {
  com.sun.source.tree.BlockTree getBody();
  com.sun.source.tree.Tree getDefaultValue();
  com.sun.source.tree.ModifiersTree getModifiers();
  javax.lang.model.element.Name getName();
  java.util.List<? extends com.sun.source.tree.VariableTree> getParameters();
  com.sun.source.tree.VariableTree getReceiverParameter();
  com.sun.source.tree.Tree getReturnType();
  java.util.List<? extends com.sun.source.tree.ExpressionTree> getThrows();
  java.util.List<? extends com.sun.source.tree.TypeParameterTree> getTypeParameters();
}
