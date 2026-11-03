/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.awt;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;

public final class SoftVolatileImage extends VolatileImage {
  private final BufferedImage image;
  private boolean validated;
  private final ImageCapabilities capabilities = new ImageCapabilities(false);

  public SoftVolatileImage(GraphicsConfiguration gc, int width, int height, int transparency) {
    ColorModel cm = gc.getColorModel(transparency);
    if (cm == null) {
      throw new IllegalArgumentException("Unknown transparency: " + transparency);
    }
    this.transparency = transparency;
    this.image = new BufferedImage(cm, cm.createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(),
        null);
    Graphics2D g = image.createGraphics();
    g.setColor(java.awt.Color.WHITE);
    g.fillRect(0, 0, width, height);
    g.dispose();
  }

  public BufferedImage getSnapshot() {
    ColorModel cm = image.getColorModel();
    BufferedImage copy = new BufferedImage(cm, cm.createCompatibleWritableRaster(image.getWidth(),
        image.getHeight()), cm.isAlphaPremultiplied(), null);
    image.copyData(copy.getRaster());
    return copy;
  }

  public int getWidth() {
    return image.getWidth();
  }

  public int getHeight() {
    return image.getHeight();
  }

  public int getWidth(ImageObserver observer) {
    return image.getWidth();
  }

  public int getHeight(ImageObserver observer) {
    return image.getHeight();
  }

  public Object getProperty(String name, ImageObserver observer) {
    if (name == null) {
      throw new NullPointerException("null property name is not allowed");
    }
    return Image.UndefinedProperty;
  }

  public Graphics2D createGraphics() {
    return image.createGraphics();
  }

  public int validate(GraphicsConfiguration gc) {
    if (!validated) {
      validated = true;
      return IMAGE_RESTORED;
    }
    return IMAGE_OK;
  }

  public boolean contentsLost() {
    return false;
  }

  public ImageCapabilities getCapabilities() {
    return capabilities;
  }
}
