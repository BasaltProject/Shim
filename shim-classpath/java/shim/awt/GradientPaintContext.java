/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.GradientPaint;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class GradientPaintContext implements PaintContext {
  private static final ColorModel kOpaque = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

  private final double x1;
  private final double y1;
  private final double dx;
  private final double dy;
  private final boolean cyclic;
  private final int[] interp;
  private final ColorModel model;

  public GradientPaintContext(GradientPaint p, AffineTransform xform) {
    if (xform == null) {
      xform = new AffineTransform();
    }
    Point2D p1 = p.getPoint1();
    Point2D p2 = p.getPoint2();
    int c1 = p.getColor1().getRGB();
    int c2 = p.getColor2().getRGB();
    cyclic = p.isCyclic();
    Point2D xv = new Point2D.Double(1, 0);
    Point2D yv = new Point2D.Double(0, 1);
    try {
      AffineTransform inv = xform.createInverse();
      inv.deltaTransform(xv, xv);
      inv.deltaTransform(yv, yv);
    } catch (NoninvertibleTransformException e) {
      xv.setLocation(0, 0);
      yv.setLocation(0, 0);
    }
    double rx = p2.getX() - p1.getX();
    double ry = p2.getY() - p1.getY();
    double len2 = rx * rx + ry * ry;
    double ddx;
    double ddy;
    if (len2 <= Double.MIN_VALUE) {
      ddx = 0;
      ddy = 0;
    } else {
      ddx = (xv.getX() * rx + xv.getY() * ry) / len2;
      ddy = (yv.getX() * rx + yv.getY() * ry) / len2;
      if (cyclic) {
        ddx = ddx % 1.0;
        ddy = ddy % 1.0;
      } else if (ddx < 0) {
        Point2D t = p1;
        p1 = p2;
        p2 = t;
        int tc = c1;
        c1 = c2;
        c2 = tc;
        ddx = -ddx;
        ddy = -ddy;
      }
    }
    dx = ddx;
    dy = ddy;
    Point2D d1 = xform.transform(p1, null);
    x1 = d1.getX();
    y1 = d1.getY();
    int[] a = unpack(c1);
    int[] b = unpack(c2);
    model = a[0] == 255 && b[0] == a[0] ? kOpaque : ColorModel.getRGBdefault();
    interp = new int[cyclic ? 513 : 257];
    for (int i = 0; i <= 256; i++) {
      float f = i / 256.0f;
      int c = (int) (a[0] + (b[0] - a[0]) * f) << 24 | (int) (a[1] + (b[1] - a[1]) * f) << 16
          | (int) (a[2] + (b[2] - a[2]) * f) << 8 | (int) (a[3] + (b[3] - a[3]) * f);
      interp[i] = c;
      if (cyclic) {
        interp[512 - i] = c;
      }
    }
  }

  private static int[] unpack(int c) {
    return new int[] {(c >> 24) & 255, (c >> 16) & 255, (c >> 8) & 255, c & 255};
  }

  public void dispose() { }

  public ColorModel getColorModel() {
    return model;
  }

  public Raster getRaster(int x, int y, int w, int h) {
    double rowrel = (x - x1) * dx + (y - y1) * dy;
    WritableRaster r = model.createCompatibleWritableRaster(w, h);
    int[] row = new int[w];
    if (cyclic) {
      rowrel = rowrel % 2.0;
      int rel0 = (int) (rowrel * 1073741824.0) << 1;
      int dxFix = (int) (-dx * -2147483648.0);
      int dyFix = (int) (-dy * -2147483648.0);
      for (int j = 0; j < h; j++) {
        int rel = rel0;
        for (int i = 0; i < w; i++) {
          row[i] = interp[rel >>> 23];
          rel += dxFix;
        }
        r.setDataElements(0, j, w, 1, row);
        rel0 += dyFix;
      }
    } else {
      for (int j = 0; j < h; j++) {
        double rel = rowrel;
        int i = w;
        int o = 0;
        if (rel <= 0) {
          do {
            row[o++] = interp[0];
            rel += dx;
          } while (--i > 0 && rel <= 0);
        }
        while (rel < 1.0) {
          if (--i < 0) {
            break;
          }
          row[o++] = interp[(int) (rel * 256.0)];
          rel += dx;
        }
        if (i > 0) {
          do {
            row[o++] = interp[256];
          } while (--i > 0);
        }
        r.setDataElements(0, j, w, 1, row);
        rowrel += dy;
      }
    }
    return r;
  }
}
