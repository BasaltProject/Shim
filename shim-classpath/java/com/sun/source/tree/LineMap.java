/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface LineMap {
  long getColumnNumber(long l);
  long getLineNumber(long l);
  long getPosition(long l, long l2);
  long getStartPosition(long l);
}
