/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public final class ImageGraphicsConfiguration extends GraphicsConfiguration {
  private final BufferedImage image;

  public ImageGraphicsConfiguration(BufferedImage image) {
    this.image = image;
  }

  public GraphicsDevice getDevice() {
    return new ImageGraphicsDevice(this);
  }

  public ColorModel getColorModel() {
    return image.getColorModel();
  }

  public ColorModel getColorModel(int transparency) {
    if (image.getColorModel().getTransparency() == transparency) {
      return image.getColorModel();
    }
    switch (transparency) {
      case Transparency.OPAQUE:
        return new java.awt.image.DirectColorModel(24, 0xff0000, 0xff00, 0xff);
      case Transparency.BITMASK:
        return new java.awt.image.DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000);
      case Transparency.TRANSLUCENT:
        return ColorModel.getRGBdefault();
      default:
        return null;
    }
  }

  public AffineTransform getDefaultTransform() {
    return new AffineTransform();
  }

  public AffineTransform getNormalizingTransform() {
    return new AffineTransform();
  }

  public Rectangle getBounds() {
    return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
  }
}
