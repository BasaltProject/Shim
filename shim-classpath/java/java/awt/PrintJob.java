/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class PrintJob {
  protected PrintJob() {
  }
  public void finalize() { throw new UnsupportedOperationException(); }
  public abstract void end();
  public abstract java.awt.Dimension getPageDimension();
  public abstract int getPageResolution();
  public abstract boolean lastPageFirst();
  public abstract java.awt.Graphics getGraphics();
}
