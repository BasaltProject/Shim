/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface AuthorTree extends com.sun.source.doctree.BlockTagTree {
  java.util.List<? extends com.sun.source.doctree.DocTree> getName();
}
