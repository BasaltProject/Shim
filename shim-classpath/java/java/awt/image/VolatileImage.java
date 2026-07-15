/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public abstract class VolatileImage extends java.awt.Image implements java.awt.Transparency {
  public static final int IMAGE_OK = 0;
  public static final int IMAGE_RESTORED = 1;
  public static final int IMAGE_INCOMPATIBLE = 2;
  protected int transparency;
  protected VolatileImage() {
  }
  public abstract int validate(java.awt.GraphicsConfiguration p0);
  public java.awt.image.ImageProducer getSource() { return getSnapshot().getSource(); }
  public abstract int getWidth();
  public abstract int getHeight();
  public java.awt.Graphics getGraphics() { return createGraphics(); }
  public abstract java.awt.ImageCapabilities getCapabilities();
  public int getTransparency() { return transparency; }
  public abstract java.awt.image.BufferedImage getSnapshot();
  public abstract java.awt.Graphics2D createGraphics();
  public abstract boolean contentsLost();
}
