/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public final class ImageSupport {
  private ImageSupport() { }

  public static boolean isAccelerated(Image img) {
    return false;
  }

  public static BufferedImage toBuffered(Image img) {
    BufferedImage bi = null;
    if (((bi = img instanceof BufferedImage ? (BufferedImage) img : null) != null)) {
      return bi;
    }
    ImageProducer producer = img.getSource();
    if (producer == null) {
      throw new IllegalArgumentException("image has no pixel source");
    }
    Collector c = new Collector();
    producer.startProduction(c);
    producer.removeConsumer(c);
    return c.result();
  }

  public static BufferedImage fromProducer(ImageProducer producer) {
    Collector c = new Collector();
    producer.startProduction(c);
    producer.removeConsumer(c);
    return c.result();
  }

  public static BufferedImage toBuffered(RenderedImage ri) {
    BufferedImage bi2 = null;
    if (((bi2 = ri instanceof BufferedImage ? (BufferedImage) ri : null) != null)) {
      return bi2;
    }
    ColorModel cm = ri.getColorModel();
    WritableRaster raster = cm.createCompatibleWritableRaster(ri.getWidth(), ri.getHeight());
    ri.copyData(raster);
    return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
  }

  public static BufferedImage scale(BufferedImage src, int w, int h, boolean nearest) {
    BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    int sw = src.getWidth();
    int sh = src.getHeight();
    int[] in = src.getRGB(0, 0, sw, sh, null, 0, sw);
    int[] dst = new int[w * h];
    if (nearest || (w >= sw && h >= sh)) {
      for (int y = 0; y < h; y++) {
        int sy = Math.min(sh - 1, (int) ((long) y * sh / h));
        for (int x = 0; x < w; x++) {
          int sx = Math.min(sw - 1, (int) ((long) x * sw / w));
          dst[y * w + x] = in[sy * sw + sx];
        }
      }
    } else {
      for (int y = 0; y < h; y++) {
        double y0 = (double) y * sh / h;
        double y1 = (double) (y + 1) * sh / h;
        for (int x = 0; x < w; x++) {
          double x0 = (double) x * sw / w;
          double x1 = (double) (x + 1) * sw / w;
          double a = 0;
          double r = 0;
          double g = 0;
          double b = 0;
          double total = 0;
          for (int sy = (int) Math.floor(y0); sy < Math.ceil(y1) && sy < sh; sy++) {
            double wy = Math.min(y1, sy + 1) - Math.max(y0, sy);
            for (int sx = (int) Math.floor(x0); sx < Math.ceil(x1) && sx < sw; sx++) {
              double wx = Math.min(x1, sx + 1) - Math.max(x0, sx);
              double wt = wx * wy;
              int p = in[sy * sw + sx];
              double pa = (p >>> 24) * wt;
              a += pa;
              r += ((p >> 16) & 0xff) * pa;
              g += ((p >> 8) & 0xff) * pa;
              b += (p & 0xff) * pa;
              total += wt;
            }
          }
          if (a <= 0 || total <= 0) {
            dst[y * w + x] = 0;
          } else {
            int ia = (int) (a / total + 0.5);
            int ir = (int) (r / a + 0.5);
            int ig = (int) (g / a + 0.5);
            int ib = (int) (b / a + 0.5);
            dst[y * w + x] = (Math.min(ia, 255) << 24) | (Math.min(ir, 255) << 16) | (Math.min(ig, 255) << 8) | Math.min(ib, 255);
          }
        }
      }
    }
    out.setRGB(0, 0, w, h, dst, 0, w);
    return out;
  }

  private static final class Collector implements ImageConsumer {
    private int width;
    private int height;
    private int[] pixels;
    private ColorModel defaultModel = ColorModel.getRGBdefault();

    public void setDimensions(int width, int height) {
      this.width = width;
      this.height = height;
      pixels = new int[width * height];
    }

    public void setProperties(Hashtable<?, ?> props) { }

    public void setColorModel(ColorModel model) { }

    public void setHints(int hintflags) { }

    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pix, int off, int scansize) {
      for (int j = 0; j < h; j++) {
        for (int i = 0; i < w; i++) {
          if (x + i < width && y + j < height) {
            pixels[(y + j) * width + x + i] = model.getRGB(pix[off + j * scansize + i] & 0xff);
          }
        }
      }
    }

    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pix, int off, int scansize) {
      for (int j = 0; j < h; j++) {
        for (int i = 0; i < w; i++) {
          if (x + i < width && y + j < height) {
            pixels[(y + j) * width + x + i] = model.getRGB(pix[off + j * scansize + i]);
          }
        }
      }
    }

    public void imageComplete(int status) { }

    BufferedImage result() {
      BufferedImage bi = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
      if (pixels != null) {
        bi.setRGB(0, 0, width, height, pixels, 0, width);
      }
      return bi;
    }
  }
}
