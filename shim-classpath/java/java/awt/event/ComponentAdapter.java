/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public abstract class ComponentAdapter implements java.awt.event.ComponentListener {
  protected ComponentAdapter() {
  }
  public void componentResized(java.awt.event.ComponentEvent p0) { }
  public void componentMoved(java.awt.event.ComponentEvent p0) { }
  public void componentShown(java.awt.event.ComponentEvent p0) { }
  public void componentHidden(java.awt.event.ComponentEvent p0) { }
}
