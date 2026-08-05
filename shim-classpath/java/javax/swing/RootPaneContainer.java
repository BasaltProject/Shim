/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface RootPaneContainer {
  void setContentPane(java.awt.Container p0);
  void setLayeredPane(javax.swing.JLayeredPane p0);
  java.awt.Component getGlassPane();
  void setGlassPane(java.awt.Component p0);
  javax.swing.JRootPane getRootPane();
  javax.swing.JLayeredPane getLayeredPane();
  java.awt.Container getContentPane();
}
