/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface ContainerListener extends java.util.EventListener {
  void componentAdded(java.awt.event.ContainerEvent p0);
  void componentRemoved(java.awt.event.ContainerEvent p0);
}
