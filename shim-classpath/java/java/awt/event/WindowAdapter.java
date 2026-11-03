/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public abstract class WindowAdapter implements java.awt.event.WindowListener, java.awt.event.WindowStateListener, java.awt.event.WindowFocusListener {
  protected WindowAdapter() {
  }
  public void windowOpened(java.awt.event.WindowEvent p0) { }
  public void windowClosing(java.awt.event.WindowEvent p0) { }
  public void windowClosed(java.awt.event.WindowEvent p0) { }
  public void windowIconified(java.awt.event.WindowEvent p0) { }
  public void windowDeiconified(java.awt.event.WindowEvent p0) { }
  public void windowActivated(java.awt.event.WindowEvent p0) { }
  public void windowDeactivated(java.awt.event.WindowEvent p0) { }
  public void windowStateChanged(java.awt.event.WindowEvent p0) { }
  public void windowGainedFocus(java.awt.event.WindowEvent p0) { }
  public void windowLostFocus(java.awt.event.WindowEvent p0) { }
}
