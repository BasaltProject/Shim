/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class MultiGradientContext implements PaintContext {
  private final boolean radial;
  private final double sx;
  private final double sy;
  private final double ex;
  private final double ey;
  private final double invLen2;
  private final double cx;
  private final double cy;
  private final double fx;
  private final double fy;
  private final double radius;
  private final MultipleGradientPaint.CycleMethod cycle;
  private final float[] fractions;
  private final int[] colors;
  private final boolean linearRgb;
  private final AffineTransform inverse;

  private static final double kFocusLimit = 0.97;

  public MultiGradientContext(MultipleGradientPaint p, boolean radial, double x1, double y1, double x2, double y2,
      double radius, AffineTransform xform) {
    this.radial = radial;
    this.sx = x1;
    this.sy = y1;
    this.ex = x2;
    this.ey = y2;
    double dx = x2 - x1;
    double dy = y2 - y1;
    double len2 = dx * dx + dy * dy;
    this.invLen2 = len2 == 0 ? 0 : 1.0 / len2;
    this.cx = x1;
    this.cy = y1;
    this.radius = radius;
    double fxx = x2;
    double fyy = y2;
    if (radial) {
      double fdx = fxx - cx;
      double fdy = fyy - cy;
      double dist = Math.sqrt(fdx * fdx + fdy * fdy);
      double limit = kFocusLimit * radius;
      if (dist > limit) {
        fxx = cx + fdx * limit / dist;
        fyy = cy + fdy * limit / dist;
      }
    }
    this.fx = fxx;
    this.fy = fyy;
    this.cycle = p.getCycleMethod();
    float[] f = p.getFractions();
    Color[] c = p.getColors();
    int n = f.length;
    boolean addFirst = f[0] != 0f;
    boolean addLast = f[n - 1] != 1f;
    int total = n + (addFirst ? 1 : 0) + (addLast ? 1 : 0);
    fractions = new float[total];
    colors = new int[total];
    int k = 0;
    if (addFirst) {
      fractions[k] = 0f;
      colors[k++] = c[0].getRGB();
    }
    for (int i = 0; i < n; i++) {
      fractions[k] = f[i];
      colors[k++] = c[i].getRGB();
    }
    if (addLast) {
      fractions[k] = 1f;
      colors[k] = c[n - 1].getRGB();
    }
    linearRgb = p.getColorSpace() == MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
    AffineTransform full = new AffineTransform();
    if (xform != null) {
      full.concatenate(xform);
    }
    full.concatenate(p.getTransform());
    AffineTransform inv;
    try {
      inv = full.createInverse();
    } catch (NoninvertibleTransformException e) {
      inv = new AffineTransform();
    }
    inverse = inv;
  }

  public void dispose() { }

  public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }

  public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster r = getColorModel().createCompatibleWritableRaster(w, h);
    int[] row = new int[w];
    double[] pt = new double[2];
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        pt[0] = x + i;
        pt[1] = y + j;
        inverse.transform(pt, 0, pt, 0, 1);
        row[i] = colorAt(position(pt[0], pt[1]));
      }
      r.setDataElements(0, j, w, 1, row);
    }
    return r;
  }

  private double position(double px, double py) {
    if (!radial) {
      return ((px - sx) * (ex - sx) + (py - sy) * (ey - sy)) * invLen2;
    }
    double dx = px - fx;
    double dy = py - fy;
    double a = dx * dx + dy * dy;
    if (a == 0) {
      return 0;
    }
    double gx = fx - cx;
    double gy = fy - cy;
    double b = 2 * (dx * gx + dy * gy);
    double c = gx * gx + gy * gy - radius * radius;
    double disc = b * b - 4 * a * c;
    if (disc < 0) {
      disc = 0;
    }
    double s = (-b + Math.sqrt(disc)) / (2 * a);
    return s == 0 ? 0 : 1.0 / s;
  }

  private int colorAt(double t) {
    switch (cycle) {
      case REPEAT:
        t = t - Math.floor(t);
        break;
      case REFLECT:
        t = Math.abs(t);
        t = t % 2.0;
        if (t > 1) {
          t = 2 - t;
        }
        break;
      default:
        t = t < 0 ? 0 : t > 1 ? 1 : t;
    }
    int last = fractions.length - 1;
    if (t <= fractions[0]) {
      return colors[0];
    }
    for (int i = 1; i <= last; i++) {
      if (t <= fractions[i]) {
        double span = fractions[i] - fractions[i - 1];
        double u = span == 0 ? 1 : (t - fractions[i - 1]) / span;
        return mix(colors[i - 1], colors[i], u);
      }
    }
    return colors[last];
  }

  private int mix(int a, int b, double u) {
    int aa = (int) (((a >>> 24) & 0xff) + (((b >>> 24) & 0xff) - ((a >>> 24) & 0xff)) * u + 0.5);
    int r = channel((a >> 16) & 0xff, (b >> 16) & 0xff, u);
    int g = channel((a >> 8) & 0xff, (b >> 8) & 0xff, u);
    int bl = channel(a & 0xff, b & 0xff, u);
    return (aa << 24) | (r << 16) | (g << 8) | bl;
  }

  private int channel(int a, int b, double u) {
    if (!linearRgb) {
      return (int) (a + (b - a) * u + 0.5);
    }
    double la = toLinear(a);
    double lb = toLinear(b);
    return toSrgb(la + (lb - la) * u);
  }

  private static double toLinear(int v) {
    double c = v / 255.0;
    return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
  }

  private static int toSrgb(double l) {
    double c = l <= 0.0031308 ? l * 12.92 : 1.055 * Math.pow(l, 1 / 2.4) - 0.055;
    int v = (int) (c * 255 + 0.5);
    return v < 0 ? 0 : v > 255 ? 255 : v;
  }
}
