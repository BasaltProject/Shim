/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public abstract class BufferStrategy {
  protected BufferStrategy() {
  }
  public void dispose() { throw new UnsupportedOperationException(); }
  public abstract java.awt.Graphics getDrawGraphics();
  public abstract boolean contentsRestored();
  public abstract void show();
  public abstract java.awt.BufferCapabilities getCapabilities();
  public abstract boolean contentsLost();
}
