/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.awt.image.BufferedImage;

public final class ImageFactory {
  private ImageFactory() { }

  public static BufferedImage create(int w, int h, int type, boolean alpha, boolean gray) {
    return new BufferedImage(w, h, type, type == BufferedImage.TYPE_CUSTOM || alpha && type != 2 && type != 6);
  }

  public static void fill(BufferedImage img, int[] argb, boolean gray, boolean grayAlpha) {
    int w = img.getWidth();
    int h = img.getHeight();
    if (gray) {
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          int v = argb[y * w + x] & 0xff;
          img.getRaster().setSample(x, y, 0, v);
        }
      }
      return;
    }
    if (gray == false && img.getType() == BufferedImage.TYPE_CUSTOM && grayAlpha) {
      for (int i = 0; i < argb.length; i++) {
        argb[i] = (argb[i] & 0xff000000) | (graySpace(argb[i] & 0xff) * 0x010101);
      }
    }
    img.setRGB(0, 0, w, h, argb, 0, w);
  }

  private static int graySpace(int v) {
    BufferedImage g = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
    g.getRaster().setSample(0, 0, 0, v);
    return g.getRGB(0, 0) & 0xff;
  }

  public static int graySample(BufferedImage img, int x, int y) {
    return img.getRaster().getSample(x, y, 0);
  }
}
