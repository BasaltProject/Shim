/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface UserDataHandler {
  short NODE_CLONED = 1;
  short NODE_IMPORTED = 2;
  short NODE_DELETED = 3;
  short NODE_RENAMED = 4;
  short NODE_ADOPTED = 5;

  void handle(short p0, String p1, Object p2, Node p3, Node p4);
}
