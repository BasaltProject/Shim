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

public class LookupOp implements BufferedImageOp, RasterOp {
  private LookupTable ltable;
  private int numComponents;
  RenderingHints hints;

  public LookupOp(LookupTable lookup, RenderingHints hints) {
    this.ltable = lookup;
    this.hints = hints;
    numComponents = ltable.getNumComponents();
  }

  public final LookupTable getTable() { return ltable; }

  private int lookup(int band, int bands, boolean hasAlpha, int v, int max) {
    int comp = ltable.getNumComponents();
    int idx = comp == 1 ? 0 : band;
    int[] in = new int[comp];
    int[] out = new int[comp];
    if (comp == 1) {
      in[0] = v;
      ltable.lookupPixel(in, out);
      return out[0];
    }
    int[] src = new int[comp];
    java.util.Arrays.fill(src, v);
    ltable.lookupPixel(src, out);
    return out[idx];
  }

  public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
    ColorModel scm = src.getColorModel();
    BufferedImage work = src;
    if (scm instanceof IndexColorModel) {
      work = ((IndexColorModel) scm).convertToIntDiscrete(src.getRaster(), false);
      scm = work.getColorModel();
    }
    int nColor = scm.getNumColorComponents();
    int ncomps = ltable.getNumComponents();
    if (ncomps != 1 && ncomps != nColor && ncomps != scm.getNumComponents()) {
      throw new IllegalArgumentException("Number of arrays in the  lookup table (" + ncomps + ") is not compatible with the src image: " + src);
    }
    BufferedImage out = dst != null ? dst : createCompatibleDestImage(work, null);
    Raster sr = work.getRaster();
    WritableRaster dr = out.getRaster();
    int bands = sr.getNumBands();
    boolean alpha = scm.hasAlpha();
    boolean alphaLookup = ncomps == scm.getNumComponents() && ncomps != 1;
    for (int y = 0; y < sr.getHeight(); y++) {
      for (int x = 0; x < sr.getWidth(); x++) {
        for (int b = 0; b < bands; b++) {
          int v = sr.getSample(sr.getMinX() + x, sr.getMinY() + y, b);
          int max = shim.awt.OpSupport.maxSample(dr, b);
          if (alpha && b == bands - 1 && !alphaLookup) {
            dr.setSample(dr.getMinX() + x, dr.getMinY() + y, b, v);
            continue;
          }
          int r = lookup(b, bands, alpha, v, max);
          dr.setSample(dr.getMinX() + x, dr.getMinY() + y, b, r < 0 ? 0 : r > max ? max : r);
        }
      }
    }
    return out;
  }

  public final WritableRaster filter(Raster src, WritableRaster dst) {
    int ncomps = ltable.getNumComponents();
    if (ncomps != 1 && ncomps != src.getNumBands()) {
      throw new IllegalArgumentException("Number of arrays in the  lookup table (" + ncomps + ") is not compatible with the src Raster: " + src);
    }
    if (dst == null) {
      dst = createCompatibleDestRaster(src);
    }
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        for (int b = 0; b < src.getNumBands(); b++) {
          int v = src.getSample(src.getMinX() + x, src.getMinY() + y, b);
          int max = shim.awt.OpSupport.maxSample(dst, b);
          int r = lookup(b, src.getNumBands(), false, v, max);
          dst.setSample(dst.getMinX() + x, dst.getMinY() + y, b, r < 0 ? 0 : r > max ? max : r);
        }
      }
    }
    return dst;
  }

  public final Rectangle2D getBounds2D(BufferedImage src) { return getBounds2D(src.getRaster()); }

  public final Rectangle2D getBounds2D(Raster src) { return src.getBounds(); }

  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    return shim.awt.OpSupport.compatibleDest(src, destCM);
  }

  public WritableRaster createCompatibleDestRaster(Raster src) { return src.createCompatibleWritableRaster(); }

  public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) { return shim.awt.OpSupport.point(srcPt, dstPt); }

  public final RenderingHints getRenderingHints() { return hints; }
}
