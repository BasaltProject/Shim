/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class GraphicsConfiguration {
  protected GraphicsConfiguration() {
  }
  public abstract java.awt.Rectangle getBounds();
  public abstract java.awt.GraphicsDevice getDevice();
  public abstract java.awt.image.ColorModel getColorModel();
  public abstract java.awt.image.ColorModel getColorModel(int p0);
  public java.awt.image.BufferedImage createCompatibleImage(int width, int height, int transparency) {
    if (getColorModel().getTransparency() == transparency) {
      return createCompatibleImage(width, height);
    }
    java.awt.image.ColorModel cm = getColorModel(transparency);
    if (cm == null) {
      throw new IllegalArgumentException("Unknown transparency: " + transparency);
    }
    java.awt.image.WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
    return new java.awt.image.BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
  }
  public java.awt.image.BufferedImage createCompatibleImage(int width, int height) {
    java.awt.image.ColorModel thisColorModel = getColorModel();
    java.awt.image.WritableRaster raster = thisColorModel.createCompatibleWritableRaster(width, height);
    return new java.awt.image.BufferedImage(thisColorModel, raster, thisColorModel.isAlphaPremultiplied(), null);
  }
  public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
    try {
      return createCompatibleVolatileImage(width, height, null, transparency);
    } catch (java.awt.AWTException e) {
      return null;
    }
  }
  public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height, java.awt.ImageCapabilities caps) throws java.awt.AWTException {
    return createCompatibleVolatileImage(width, height, caps, java.awt.Transparency.OPAQUE);
  }
  public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height, java.awt.ImageCapabilities caps, int transparency) throws java.awt.AWTException {
    java.awt.image.VolatileImage vi = new shim.awt.SoftVolatileImage(this, width, height, transparency);
    if (caps != null && caps.isAccelerated() != vi.getCapabilities().isAccelerated()) {
      throw new java.awt.AWTException("Supplied image capabilities could not be met by this graphics configuration.");
    }
    return vi;
  }
  public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height) {
    return createCompatibleVolatileImage(width, height, java.awt.Transparency.OPAQUE);
  }
  public java.awt.ImageCapabilities getImageCapabilities() { return new java.awt.ImageCapabilities(false); }
  public abstract java.awt.geom.AffineTransform getDefaultTransform();
  public abstract java.awt.geom.AffineTransform getNormalizingTransform();
  public java.awt.BufferCapabilities getBufferCapabilities() {
    return new java.awt.BufferCapabilities(getImageCapabilities(), getImageCapabilities(), null);
  }
  public boolean isTranslucencyCapable() { return false; }
}
