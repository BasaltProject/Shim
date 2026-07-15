/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class GradientPaintContext implements PaintContext {
  private final double x1;
  private final double y1;
  private final double dx;
  private final double dy;
  private final double invLen2;
  private final boolean cyclic;
  private final int c1;
  private final int c2;
  private final AffineTransform inverse;

  public GradientPaintContext(GradientPaint p, AffineTransform xform) {
    Point2D a = p.getPoint1();
    Point2D b = p.getPoint2();
    x1 = a.getX();
    y1 = a.getY();
    dx = b.getX() - a.getX();
    dy = b.getY() - a.getY();
    double len2 = dx * dx + dy * dy;
    invLen2 = len2 == 0 ? 0 : 1.0 / len2;
    cyclic = p.isCyclic();
    c1 = p.getColor1().getRGB();
    c2 = p.getColor2().getRGB();
    AffineTransform inv;
    try {
      inv = xform == null ? new AffineTransform() : xform.createInverse();
    } catch (NoninvertibleTransformException e) {
      inv = new AffineTransform();
    }
    inverse = inv;
  }

  public void dispose() { }

  public ColorModel getColorModel() {
    return ColorModel.getRGBdefault();
  }

  private int[] table() {
    int[] t = new int[cyclic ? 513 : 257];
    for (int i = 0; i < 256; i++) {
      t[i] = step(c1, c2, i);
    }
    t[256] = c2;
    if (cyclic) {
      for (int i = 1; i < 256; i++) {
        t[256 + i] = t[256 - i];
      }
      t[512] = c1;
    }
    return t;
  }

  private static int step(int a, int b, int i) {
    int aa = ((a >>> 24) & 0xff) + ((((b >>> 24) & 0xff) - ((a >>> 24) & 0xff)) * i >> 8);
    int ar = ((a >> 16) & 0xff) + ((((b >> 16) & 0xff) - ((a >> 16) & 0xff)) * i >> 8);
    int ag = ((a >> 8) & 0xff) + ((((b >> 8) & 0xff) - ((a >> 8) & 0xff)) * i >> 8);
    int ab = (a & 0xff) + (((b & 0xff) - (a & 0xff)) * i >> 8);
    return (aa << 24) | (ar << 16) | (ag << 8) | ab;
  }

  public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster r = getColorModel().createCompatibleWritableRaster(w, h);
    int[] row = new int[w];
    double[] pt = new double[2];
    int[] interp = table();
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        pt[0] = x + i;
        pt[1] = y + j;
        inverse.transform(pt, 0, pt, 0, 1);
        double t = ((pt[0] - x1) * dx + (pt[1] - y1) * dy) * invLen2;
        int idx;
        if (cyclic) {
          t = t - 2 * Math.floor(t / 2);
          idx = (int) (t * 256);
        } else {
          t = t < 0 ? 0 : t > 1 ? 1 : t;
          idx = (int) (t * 256);
        }
        row[i] = interp[idx];
      }
      r.setDataElements(0, j, w, 1, row);
    }
    return r;
  }
}
