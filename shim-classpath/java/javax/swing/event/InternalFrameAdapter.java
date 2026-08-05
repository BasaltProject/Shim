/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public abstract class InternalFrameAdapter implements javax.swing.event.InternalFrameListener {
  protected InternalFrameAdapter() {
  }
  public void internalFrameOpened(javax.swing.event.InternalFrameEvent p0) { }
  public void internalFrameClosing(javax.swing.event.InternalFrameEvent p0) { }
  public void internalFrameClosed(javax.swing.event.InternalFrameEvent p0) { }
  public void internalFrameIconified(javax.swing.event.InternalFrameEvent p0) { }
  public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent p0) { }
  public void internalFrameActivated(javax.swing.event.InternalFrameEvent p0) { }
  public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent p0) { }
}
