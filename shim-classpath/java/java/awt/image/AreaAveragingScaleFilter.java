/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class AreaAveragingScaleFilter extends ReplicateScaleFilter {
  private int[] argb;
  private boolean receivedPixels;

  public AreaAveragingScaleFilter(int width, int height) {
    super(width, height);
  }

  public void setHints(int hints) {
    super.setHints(hints & ~ImageConsumer.RANDOMPIXELORDER | ImageConsumer.SINGLEPASS);
  }

  public void setDimensions(int w, int h) {
    super.setDimensions(w, h);
    argb = new int[w * h];
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        put(x + i, y + j, model.getRGB(pixels[off + j * scansize + i] & 0xff));
      }
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        put(x + i, y + j, model.getRGB(pixels[off + j * scansize + i]));
      }
    }
  }

  private void put(int x, int y, int rgb) {
    if (x >= 0 && y >= 0 && x < srcWidth && y < srcHeight) {
      argb[y * srcWidth + x] = rgb;
      receivedPixels = true;
    }
  }

  public void imageComplete(int status) {
    if (receivedPixels && argb != null) {
      java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(srcWidth, srcHeight,
          java.awt.image.BufferedImage.TYPE_INT_ARGB);
      src.setRGB(0, 0, srcWidth, srcHeight, argb, 0, srcWidth);
      java.awt.image.BufferedImage scaled = shim.awt.ImageSupport.scale(src, destWidth, destHeight, false);
      int[] out = scaled.getRGB(0, 0, destWidth, destHeight, null, 0, destWidth);
      consumer.setPixels(0, 0, destWidth, destHeight, ColorModel.getRGBdefault(), out, 0, destWidth);
    }
    super.imageComplete(status);
  }
}
