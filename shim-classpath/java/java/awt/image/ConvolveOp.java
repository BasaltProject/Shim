/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ConvolveOp implements BufferedImageOp, RasterOp {
  public static final int EDGE_ZERO_FILL = 0;
  public static final int EDGE_NO_OP = 1;

  Kernel kernel;
  int edgeHint;
  RenderingHints hints;

  public ConvolveOp(Kernel kernel, int edgeCondition, RenderingHints hints) {
    this.kernel = kernel;
    this.edgeHint = edgeCondition;
    this.hints = hints;
  }

  public ConvolveOp(Kernel kernel) {
    this.kernel = kernel;
    this.edgeHint = EDGE_ZERO_FILL;
  }

  public int getEdgeCondition() { return edgeHint; }

  public final Kernel getKernel() { return (Kernel) kernel.clone(); }

  public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
    if (src == null) {
      throw new NullPointerException("src image is null");
    }
    if (src == dst) {
      throw new IllegalArgumentException("src image cannot be the same as the dst image");
    }
    ColorModel scm = src.getColorModel();
    BufferedImage work = src;
    if (scm instanceof IndexColorModel) {
      work = ((IndexColorModel) scm).convertToIntDiscrete(src.getRaster(), false);
    }
    BufferedImage out = dst;
    if (out == null) {
      out = createCompatibleDestImage(work, null);
    } else if (out.getColorModel() instanceof IndexColorModel) {
      out = createCompatibleDestImage(work, null);
    }
    filter(work.getRaster(), out.getRaster());
    if (dst != null && out != dst) {
      shim.awt.OpSupport.copyPixelsToDest(out, dst);
      return dst;
    }
    return out;
  }

  public final WritableRaster filter(Raster src, WritableRaster dst) {
    if (dst == null) {
      dst = createCompatibleDestRaster(src);
    } else if (src == dst) {
      throw new IllegalArgumentException("src image cannot be the same as the dst image");
    } else if (src.getNumBands() != dst.getNumBands()) {
      throw new ImagingOpException("Different number of bands in src  and dst Rasters");
    }
    int w = src.getWidth();
    int h = src.getHeight();
    int kw = kernel.getWidth();
    int kh = kernel.getHeight();
    int xo = kernel.getXOrigin();
    int yo = kernel.getYOrigin();
    float[] k = kernel.getKernelData(null);
    int bands = src.getNumBands();
    float[] row = new float[bands];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        boolean inside = x - xo + kw - 1 < w && x - xo >= 0 && y - yo + kh - 1 < h && y - yo >= 0
            && x + (kw - 1 - xo) < w && x - (kw - 1 - xo) >= 0 && y + (kh - 1 - yo) < h && y - (kh - 1 - yo) >= 0;
        boolean edge = x < xo || y < yo || x + (kw - 1 - xo) >= w || y + (kh - 1 - yo) >= h;
        if (edge) {
          for (int b = 0; b < bands; b++) {
            dst.setSample(dst.getMinX() + x, dst.getMinY() + y, b,
                edgeHint == EDGE_NO_OP ? src.getSample(src.getMinX() + x, src.getMinY() + y, b) : 0);
          }
          continue;
        }
        java.util.Arrays.fill(row, 0f);
        for (int j = 0; j < kh; j++) {
          for (int i = 0; i < kw; i++) {
            float kv = k[j * kw + i];
            if (kv == 0f) {
              continue;
            }
            int sx = src.getMinX() + x + xo - i;
            int sy = src.getMinY() + y + yo - j;
            for (int b = 0; b < bands; b++) {
              row[b] += kv * src.getSample(sx, sy, b);
            }
          }
        }
        for (int b = 0; b < bands; b++) {
          int max = shim.awt.OpSupport.maxSample(dst, b);
          int v = (int) (row[b] + 0.5f);
          if (row[b] < 0) {
            v = 0;
          }
          dst.setSample(dst.getMinX() + x, dst.getMinY() + y, b, v < 0 ? 0 : v > max ? max : v);
        }
      }
    }
    return dst;
  }

  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    return shim.awt.OpSupport.compatibleDest(src, destCM);
  }

  public WritableRaster createCompatibleDestRaster(Raster src) {
    return src.createCompatibleWritableRaster();
  }

  public final Rectangle2D getBounds2D(BufferedImage src) { return getBounds2D(src.getRaster()); }

  public final Rectangle2D getBounds2D(Raster src) { return src.getBounds(); }

  public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) { return shim.awt.OpSupport.point(srcPt, dstPt); }

  public final RenderingHints getRenderingHints() { return hints; }
}
