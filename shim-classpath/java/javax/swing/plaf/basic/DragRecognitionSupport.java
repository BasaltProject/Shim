/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

class DragRecognitionSupport {
  DragRecognitionSupport() {
  }
  public static boolean mousePressed(java.awt.event.MouseEvent e) { return false; }
  public static java.awt.event.MouseEvent mouseReleased(java.awt.event.MouseEvent e) { return null; }

  public interface BeforeDrag {
    void dragStarting(java.awt.event.MouseEvent p0);
  }
}
