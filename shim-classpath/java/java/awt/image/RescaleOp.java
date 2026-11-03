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

public class RescaleOp implements BufferedImageOp, RasterOp {
  float[] scaleFactors;
  float[] offsets;
  int length = 0;
  RenderingHints hints;

  public RescaleOp(float[] scaleFactors, float[] offsets, RenderingHints hints) {
    length = Math.min(scaleFactors.length, offsets.length);
    this.scaleFactors = new float[length];
    this.offsets = new float[length];
    for (int i = 0; i < length; i++) {
      this.scaleFactors[i] = scaleFactors[i];
      this.offsets[i] = offsets[i];
    }
    this.hints = hints;
  }

  public RescaleOp(float scaleFactor, float offset, RenderingHints hints) {
    length = 1;
    this.scaleFactors = new float[] {scaleFactor};
    this.offsets = new float[] {offset};
    this.hints = hints;
  }

  public final float[] getScaleFactors(float[] scaleFactors) {
    if (scaleFactors == null) {
      return this.scaleFactors.clone();
    }
    System.arraycopy(this.scaleFactors, 0, scaleFactors, 0, Math.min(this.scaleFactors.length, scaleFactors.length));
    return scaleFactors;
  }

  public final float[] getOffsets(float[] offsets) {
    if (offsets == null) {
      return this.offsets.clone();
    }
    System.arraycopy(this.offsets, 0, offsets, 0, Math.min(this.offsets.length, offsets.length));
    return offsets;
  }

  public final int getNumFactors() { return length; }

  public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
    ColorModel scm = src.getColorModel();
    BufferedImage work = src;
    if (scm instanceof IndexColorModel) {
      work = ((IndexColorModel) scm).convertToIntDiscrete(src.getRaster(), false);
      scm = work.getColorModel();
    }
    int nColor = scm.getNumColorComponents();
    if (length != 1 && length != nColor && length != scm.getNumComponents()) {
      throw new IllegalArgumentException("Number of scaling constants does not equal the number of of color or color/alpha  components");
    }
    BufferedImage out = dst;
    if (out == null) {
      out = createCompatibleDestImage(work, null);
    }
    WritableRaster dr = out.getRaster();
    Raster sr = work.getRaster();
    boolean pre = scm.hasAlpha() && scm.isAlphaPremultiplied();
    int bands = sr.getNumBands();
    boolean alphaToo = length == scm.getNumComponents() && scm.hasAlpha();
    for (int y = 0; y < sr.getHeight(); y++) {
      for (int x = 0; x < sr.getWidth(); x++) {
        for (int b = 0; b < bands; b++) {
          boolean isAlpha = scm.hasAlpha() && b == bands - 1;
          int v = sr.getSample(sr.getMinX() + x, sr.getMinY() + y, b);
          if (isAlpha && !alphaToo) {
            dr.setSample(dr.getMinX() + x, dr.getMinY() + y, b, v);
            continue;
          }
          int idx = length == 1 ? 0 : b;
          int max = shim.awt.OpSupport.maxSample(dr, b);
          float f = v * scaleFactors[idx] + offsets[idx];
          int r = (int) f;
          dr.setSample(dr.getMinX() + x, dr.getMinY() + y, b, r < 0 ? 0 : r > max ? max : r);
        }
      }
    }
    return out;
  }

  public final WritableRaster filter(Raster src, WritableRaster dst) {
    if (dst == null) {
      dst = createCompatibleDestRaster(src);
    }
    int bands = src.getNumBands();
    if (length != 1 && length != bands) {
      throw new IllegalArgumentException("Number of scaling constants does not equal the number of of bands in the src raster");
    }
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        for (int b = 0; b < bands; b++) {
          int idx = length == 1 ? 0 : b;
          int max = shim.awt.OpSupport.maxSample(dst, b);
          float f = src.getSample(src.getMinX() + x, src.getMinY() + y, b) * scaleFactors[idx] + offsets[idx];
          int r = (int) f;
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
