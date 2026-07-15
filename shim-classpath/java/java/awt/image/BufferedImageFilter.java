/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class BufferedImageFilter extends ImageFilter implements Cloneable {
  BufferedImageOp bufferedImageOp;
  ColorModel model;
  int width;
  int height;
  int[] argbPixels;

  public BufferedImageFilter(BufferedImageOp op) {
    super();
    if (op == null) {
      throw new NullPointerException("Operation cannot be null");
    }
    bufferedImageOp = op;
  }

  public BufferedImageOp getBufferedImageOp() { return bufferedImageOp; }

  public void setDimensions(int width, int height) {
    if (width <= 0 || height <= 0) {
      imageComplete(STATICIMAGEDONE);
      return;
    }
    this.width = width;
    this.height = height;
  }

  public void setColorModel(ColorModel model) { this.model = model; }

  private void ensurePixels() {
    if (argbPixels == null) {
      if (width == 0 || height == 0) {
        throw new InternalError("Width or Height not defined");
      }
      argbPixels = new int[width * height];
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    if (y < 0) {
      h += y;
      y = 0;
    }
    if (y + h > height) {
      h = height - y;
    }
    if (x < 0) {
      w += x;
      x = 0;
    }
    if (x + w > width) {
      w = width - x;
    }
    if (w <= 0 || h <= 0) {
      return;
    }
    ensurePixels();
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        argbPixels[(y + j) * width + x + i] = model.getRGB(pixels[off + j * scansize + i] & 0xff);
      }
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    if (y < 0) {
      h += y;
      y = 0;
    }
    if (y + h > height) {
      h = height - y;
    }
    if (x < 0) {
      w += x;
      x = 0;
    }
    if (x + w > width) {
      w = width - x;
    }
    if (w <= 0 || h <= 0) {
      return;
    }
    ensurePixels();
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        argbPixels[(y + j) * width + x + i] = model.getRGB(pixels[off + j * scansize + i]);
      }
    }
  }

  public void imageComplete(int status) {
    WritableRaster wr;
    switch (status) {
      case IMAGEERROR:
      case IMAGEABORTED:
        model = null;
        width = -1;
        height = -1;
        argbPixels = null;
        break;
      case SINGLEFRAMEDONE:
      case STATICIMAGEDONE:
        if (width <= 0 || height <= 0) {
          break;
        }
        if (argbPixels == null) {
          break;
        }
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, width, height, argbPixels, 0, width);
        bi = bufferedImageOp.filter(bi, null);
        int w = bi.getWidth();
        int h = bi.getHeight();
        int[] out = bi.getRGB(0, 0, w, h, null, 0, w);
        consumer.setDimensions(w, h);
        consumer.setColorModel(ColorModel.getRGBdefault());
        consumer.setPixels(0, 0, w, h, ColorModel.getRGBdefault(), out, 0, w);
        break;
    }
    consumer.imageComplete(status);
  }

}
