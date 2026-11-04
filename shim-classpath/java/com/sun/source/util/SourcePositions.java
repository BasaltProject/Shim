/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public interface SourcePositions {
  long getEndPosition(com.sun.source.tree.CompilationUnitTree compilationUnitTree, com.sun.source.tree.Tree tree);
  long getStartPosition(com.sun.source.tree.CompilationUnitTree compilationUnitTree, com.sun.source.tree.Tree tree);
}
