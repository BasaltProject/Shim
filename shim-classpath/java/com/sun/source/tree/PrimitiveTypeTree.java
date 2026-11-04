/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface PrimitiveTypeTree extends com.sun.source.tree.Tree {
  javax.lang.model.type.TypeKind getPrimitiveTypeKind();
}
