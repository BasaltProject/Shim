/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class OpSupport {
  private OpSupport() { }

  public static BufferedImage compatibleDest(BufferedImage src, ColorModel destCM) {
    ColorModel cm = destCM != null ? destCM : src.getColorModel();
    if (cm instanceof IndexColorModel && destCM == null) {
      return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }
    WritableRaster r = cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight());
    return new BufferedImage(cm, r, cm.isAlphaPremultiplied(), null);
  }

  public static Point2D point(Point2D src, Point2D dst) {
    if (dst == null) {
      dst = new Point2D.Float();
    }
    dst.setLocation(src.getX(), src.getY());
    return dst;
  }

  public static Rectangle2D bounds(Raster r) {
    return r.getBounds();
  }

  public static Rectangle2D bounds(BufferedImage b) {
    return b.getRaster().getBounds();
  }

  public static int maxSample(Raster r, int band) {
    int bits = r.getSampleModel().getSampleSize(band);
    return bits >= 31 ? Integer.MAX_VALUE : (1 << bits) - 1;
  }

  public static void copyPixelsToDest(BufferedImage src, BufferedImage dst) {
    if (src.getColorModel().equals(dst.getColorModel())) {
      dst.getRaster().setRect(src.getRaster());
    } else {
      int w = Math.min(src.getWidth(), dst.getWidth());
      int h = Math.min(src.getHeight(), dst.getHeight());
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          dst.setRGB(x, y, src.getRGB(x, y));
        }
      }
    }
  }

  public static RenderingHints hints(RenderingHints h) {
    return h;
  }
}
