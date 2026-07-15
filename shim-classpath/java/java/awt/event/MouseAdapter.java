/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public abstract class MouseAdapter implements java.awt.event.MouseListener, java.awt.event.MouseWheelListener, java.awt.event.MouseMotionListener {
  protected MouseAdapter() {
  }
  public void mousePressed(java.awt.event.MouseEvent p0) { }
  public void mouseReleased(java.awt.event.MouseEvent p0) { }
  public void mouseClicked(java.awt.event.MouseEvent p0) { }
  public void mouseExited(java.awt.event.MouseEvent p0) { }
  public void mouseEntered(java.awt.event.MouseEvent p0) { }
  public void mouseMoved(java.awt.event.MouseEvent p0) { }
  public void mouseDragged(java.awt.event.MouseEvent p0) { }
  public void mouseWheelMoved(java.awt.event.MouseWheelEvent p0) { }
}
