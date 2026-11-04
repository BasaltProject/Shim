/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.java.accessibility.util;

public interface TopLevelWindowListener extends java.util.EventListener {
  void topLevelWindowCreated(java.awt.Window window);
  void topLevelWindowDestroyed(java.awt.Window window);
}
