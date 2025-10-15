/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

public interface NodeChangeListener extends java.util.EventListener {
  void childAdded(NodeChangeEvent evt);

  void childRemoved(NodeChangeEvent evt);
}
