/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class AffineTransformOp implements BufferedImageOp, RasterOp {
  public static final int TYPE_NEAREST_NEIGHBOR = 1;
  public static final int TYPE_BILINEAR = 2;
  public static final int TYPE_BICUBIC = 3;

  private AffineTransform xform;
  RenderingHints hints;
  int interpolationType = TYPE_NEAREST_NEIGHBOR;

  public AffineTransformOp(AffineTransform xform, RenderingHints hints) {
    validateTransform(xform);
    this.xform = (AffineTransform) xform.clone();
    this.hints = hints;
    if (hints != null) {
      Object value = hints.get(RenderingHints.KEY_INTERPOLATION);
      if (value == null) {
        value = hints.get(RenderingHints.KEY_RENDERING);
        if (value == RenderingHints.VALUE_RENDER_SPEED) {
          interpolationType = TYPE_NEAREST_NEIGHBOR;
        } else if (value == RenderingHints.VALUE_RENDER_QUALITY) {
          interpolationType = TYPE_BILINEAR;
        }
      } else if (value == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
        interpolationType = TYPE_NEAREST_NEIGHBOR;
      } else if (value == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
        interpolationType = TYPE_BILINEAR;
      } else if (value == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
        interpolationType = TYPE_BICUBIC;
      }
    }
  }

  public AffineTransformOp(AffineTransform xform, int interpolationType) {
    validateTransform(xform);
    this.xform = (AffineTransform) xform.clone();
    switch (interpolationType) {
    case TYPE_NEAREST_NEIGHBOR:
    case TYPE_BILINEAR:
    case TYPE_BICUBIC:
      break;
    default:
      throw new IllegalArgumentException("Unknown interpolation type: " + interpolationType);
    }
    this.interpolationType = interpolationType;
  }

  public final int getInterpolationType() { return interpolationType; }

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
    BufferedImage out = dst != null ? dst : createCompatibleDestImage(work, null);
    int w = out.getWidth();
    int h = out.getHeight();
    AffineTransform inv;
    try {
      inv = xform.createInverse();
    } catch (NoninvertibleTransformException e) {
      throw new ImagingOpException("Unable to invert transform " + xform);
    }
    int sw = work.getWidth();
    int sh = work.getHeight();
    double[] pt = new double[2];
    boolean bilinear = interpolationType != TYPE_NEAREST_NEIGHBOR;
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        pt[0] = x + 0.5;
        pt[1] = y + 0.5;
        inv.transform(pt, 0, pt, 0, 1);
        double u = pt[0];
        double v = pt[1];
        int argb = 0;
        if (u >= 0 && v >= 0 && u < sw && v < sh) {
          if (!bilinear) {
            argb = work.getRGB((int) u, (int) v);
          } else {
            argb = bilinear(work, u - 0.5, v - 0.5, sw, sh);
          }
        }
        out.setRGB(x, y, argb);
      }
    }
    return out;
  }

  private static int bilinear(BufferedImage src, double u, double v, int sw, int sh) {
    int ix = (int) Math.floor(u);
    int iy = (int) Math.floor(v);
    double fx = u - ix;
    double fy = v - iy;
    double a = 0;
    double r = 0;
    double g = 0;
    double b = 0;
    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < 2; i++) {
        int sx = Math.max(0, Math.min(sw - 1, ix + i));
        int sy = Math.max(0, Math.min(sh - 1, iy + j));
        int p = src.getRGB(sx, sy);
        double wt = (i == 0 ? 1 - fx : fx) * (j == 0 ? 1 - fy : fy);
        double wa = (p >>> 24) * wt;
        a += wa;
        r += ((p >> 16) & 0xff) * wa;
        g += ((p >> 8) & 0xff) * wa;
        b += (p & 0xff) * wa;
      }
    }
    if (a <= 0) {
      return 0;
    }
    return ((int) (a + 0.5) << 24) | ((int) (r / a + 0.5) << 16) | ((int) (g / a + 0.5) << 8) | (int) (b / a + 0.5);
  }

  public final WritableRaster filter(Raster src, WritableRaster dst) {
    if (src == null) {
      throw new NullPointerException("src image is null");
    }
    if (dst == null) {
      dst = createCompatibleDestRaster(src);
    }
    if (src == dst) {
      throw new IllegalArgumentException("src image cannot be the same as the dst image");
    }
    if (src.getNumBands() != dst.getNumBands()) {
      throw new IllegalArgumentException("Number of src bands (" + src.getNumBands() + ") does not match number of  dst bands (" + dst.getNumBands() + ")");
    }
    AffineTransform inv;
    try {
      inv = xform.createInverse();
    } catch (NoninvertibleTransformException e) {
      throw new ImagingOpException("Unable to invert transform " + xform);
    }
    double[] pt = new double[2];
    for (int y = 0; y < dst.getHeight(); y++) {
      for (int x = 0; x < dst.getWidth(); x++) {
        pt[0] = x + 0.5;
        pt[1] = y + 0.5;
        inv.transform(pt, 0, pt, 0, 1);
        int sx = (int) Math.floor(pt[0]);
        int sy = (int) Math.floor(pt[1]);
        for (int b = 0; b < dst.getNumBands(); b++) {
          int v = sx >= 0 && sy >= 0 && sx < src.getWidth() && sy < src.getHeight()
              ? src.getSample(src.getMinX() + sx, src.getMinY() + sy, b) : 0;
          dst.setSample(dst.getMinX() + x, dst.getMinY() + y, b, v);
        }
      }
    }
    return dst;
  }

  public final Rectangle2D getBounds2D(BufferedImage src) { return getBounds2D(src.getRaster()); }

  public final Rectangle2D getBounds2D(Raster src) {
    int w = src.getWidth();
    int h = src.getHeight();
    double[] pts = {0, 0, w, 0, w, h, 0, h};
    xform.transform(pts, 0, pts, 0, 4);
    double minX = Math.min(Math.min(pts[0], pts[2]), Math.min(pts[4], pts[6]));
    double minY = Math.min(Math.min(pts[1], pts[3]), Math.min(pts[5], pts[7]));
    double maxX = Math.max(Math.max(pts[0], pts[2]), Math.max(pts[4], pts[6]));
    double maxY = Math.max(Math.max(pts[1], pts[3]), Math.max(pts[5], pts[7]));
    return new Rectangle2D.Float((float) minX, (float) minY, (float) (maxX - minX), (float) (maxY - minY));
  }

  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    Rectangle2D r = getBounds2D(src);
    int w = (int) Math.ceil(r.getX() + r.getWidth());
    int h = (int) Math.ceil(r.getY() + r.getHeight());
    ColorModel cm = destCM != null ? destCM : src.getColorModel();
    if (destCM == null && cm instanceof IndexColorModel) {
      return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }
    WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
    return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
  }

  public WritableRaster createCompatibleDestRaster(Raster src) {
    Rectangle2D r = getBounds2D(src);
    return src.createCompatibleWritableRaster(0, 0, (int) Math.ceil(r.getX() + r.getWidth()),
        (int) Math.ceil(r.getY() + r.getHeight()));
  }

  public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    return xform.transform(srcPt, dstPt);
  }

  public final AffineTransform getTransform() { return (AffineTransform) xform.clone(); }

  public final RenderingHints getRenderingHints() {
    if (hints == null) {
      Object val;
      switch (interpolationType) {
      case TYPE_BILINEAR:
        val = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        break;
      case TYPE_BICUBIC:
        val = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        break;
      default:
        val = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        break;
      }
      hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, val);
    }
    return hints;
  }

  private void validateTransform(AffineTransform xform) {
    if (Math.abs(xform.getDeterminant()) <= Double.MIN_VALUE) {
      throw new ImagingOpException("Unable to invert transform " + xform);
    }
  }
}
