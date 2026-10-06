/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface LinkTree extends com.sun.source.doctree.InlineTagTree {
  java.util.List<? extends com.sun.source.doctree.DocTree> getLabel();
  com.sun.source.doctree.ReferenceTree getReference();
}
