/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public interface DocSourcePositions extends com.sun.source.util.SourcePositions {
  long getEndPosition(com.sun.source.tree.CompilationUnitTree compilationUnitTree, com.sun.source.doctree.DocCommentTree docCommentTree, com.sun.source.doctree.DocTree docTree);
  long getStartPosition(com.sun.source.tree.CompilationUnitTree compilationUnitTree, com.sun.source.doctree.DocCommentTree docCommentTree, com.sun.source.doctree.DocTree docTree);
}
