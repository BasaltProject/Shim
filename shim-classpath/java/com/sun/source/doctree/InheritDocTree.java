/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface InheritDocTree extends com.sun.source.doctree.InlineTagTree {
  default com.sun.source.doctree.ReferenceTree getSupertype() {
    return null;
  }
}
