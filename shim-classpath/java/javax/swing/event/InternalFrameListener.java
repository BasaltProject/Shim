/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface InternalFrameListener extends java.util.EventListener {
  void internalFrameOpened(javax.swing.event.InternalFrameEvent p0);
  void internalFrameClosing(javax.swing.event.InternalFrameEvent p0);
  void internalFrameClosed(javax.swing.event.InternalFrameEvent p0);
  void internalFrameIconified(javax.swing.event.InternalFrameEvent p0);
  void internalFrameDeiconified(javax.swing.event.InternalFrameEvent p0);
  void internalFrameActivated(javax.swing.event.InternalFrameEvent p0);
  void internalFrameDeactivated(javax.swing.event.InternalFrameEvent p0);
}
