/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface DesktopManager {
  void setBoundsForFrame(javax.swing.JComponent p0, int p1, int p2, int p3, int p4);
  void iconifyFrame(javax.swing.JInternalFrame p0);
  void openFrame(javax.swing.JInternalFrame p0);
  void closeFrame(javax.swing.JInternalFrame p0);
  void maximizeFrame(javax.swing.JInternalFrame p0);
  void minimizeFrame(javax.swing.JInternalFrame p0);
  void deiconifyFrame(javax.swing.JInternalFrame p0);
  void activateFrame(javax.swing.JInternalFrame p0);
  void deactivateFrame(javax.swing.JInternalFrame p0);
  void beginDraggingFrame(javax.swing.JComponent p0);
  void dragFrame(javax.swing.JComponent p0, int p1, int p2);
  void endDraggingFrame(javax.swing.JComponent p0);
  void beginResizingFrame(javax.swing.JComponent p0, int p1);
  void resizeFrame(javax.swing.JComponent p0, int p1, int p2, int p3, int p4);
  void endResizingFrame(javax.swing.JComponent p0);
}
