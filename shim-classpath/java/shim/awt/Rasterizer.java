/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.Arrays;

public final class Rasterizer {
  public interface RowSink {
    void row(int y, int xStart, int xEnd, float[] cov, int base);
  }

  private static final int kSamples = 8;
  private static final int kSubX = 256;
  private static final int kMaxCoverage = kSamples * kSubX;

  private Rasterizer() { }

  public static Shape adjusted(Shape shape, AffineTransform at) {
    PathIterator pi = shape.getPathIterator(at);
    java.awt.geom.Path2D.Float out = new java.awt.geom.Path2D.Float(pi.getWindingRule());
    float[] c = new float[6];
    float adjx = 0;
    float adjy = 0;
    while (!pi.isDone()) {
      switch (pi.currentSegment(c)) {
      case PathIterator.SEG_MOVETO: {
        float nx = (float) Math.floor(c[0] + 0.25f) + 0.25f;
        float ny = (float) Math.floor(c[1] + 0.25f) + 0.25f;
        adjx = nx - c[0];
        adjy = ny - c[1];
        out.moveTo(nx, ny);
        break;
      }
      case PathIterator.SEG_LINETO: {
        float nx = (float) Math.floor(c[0] + 0.25f) + 0.25f;
        float ny = (float) Math.floor(c[1] + 0.25f) + 0.25f;
        adjx = nx - c[0];
        adjy = ny - c[1];
        out.lineTo(nx, ny);
        break;
      }
      case PathIterator.SEG_QUADTO: {
        float nx = (float) Math.floor(c[2] + 0.25f) + 0.25f;
        float ny = (float) Math.floor(c[3] + 0.25f) + 0.25f;
        float dx = nx - c[2];
        float dy = ny - c[3];
        out.quadTo(c[0] + (dx + adjx) / 2, c[1] + (dy + adjy) / 2, nx, ny);
        adjx = dx;
        adjy = dy;
        break;
      }
      case PathIterator.SEG_CUBICTO: {
        float nx = (float) Math.floor(c[4] + 0.25f) + 0.25f;
        float ny = (float) Math.floor(c[5] + 0.25f) + 0.25f;
        float dx = nx - c[4];
        float dy = ny - c[5];
        out.curveTo(c[0] + adjx, c[1] + adjy, c[2] + dx, c[3] + dy, nx, ny);
        adjx = dx;
        adjy = dy;
        break;
      }
      default:
        out.closePath();
        break;
      }
      pi.next();
    }
    return out;
  }

  public static void fill(Shape shape, AffineTransform at, int cx0, int cy0, int cx1, int cy1, boolean aa,
      RowSink sink) {
    if (cx1 <= cx0 || cy1 <= cy0) {
      return;
    }
    if (aa && shape instanceof java.awt.geom.Rectangle2D && fillBox((java.awt.geom.Rectangle2D) shape, at, cx0, cy0, cx1, cy1, sink)) {
      return;
    }
    PathIterator pi = new FlatteningPathIterator(shape.getPathIterator(at), 0.1, 12);
    boolean evenOdd = pi.getWindingRule() == PathIterator.WIND_EVEN_ODD;
    double[] ex0 = new double[64];
    double[] ey0 = new double[64];
    double[] ex1 = new double[64];
    double[] ey1 = new double[64];
    int[] dir = new int[64];
    int n = 0;
    double[] c = new double[6];
    double mx = 0;
    double my = 0;
    double px = 0;
    double py = 0;
    boolean open = false;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    while (!pi.isDone()) {
      int t = pi.currentSegment(c);
      double tx0 = px;
      double ty0 = py;
      double tx1 = 0;
      double ty1 = 0;
      boolean edge = false;
      switch (t) {
      case PathIterator.SEG_MOVETO:
        if (open && (px != mx || py != my)) {
          tx1 = mx;
          ty1 = my;
          edge = true;
        }
        break;
      case PathIterator.SEG_LINETO:
        tx1 = c[0];
        ty1 = c[1];
        edge = true;
        break;
      case PathIterator.SEG_CLOSE:
        if (px != mx || py != my) {
          tx1 = mx;
          ty1 = my;
          edge = true;
        }
        break;
      default:
        break;
      }
      if (edge && ty0 != ty1 && !Double.isNaN(tx0 + ty0 + tx1 + ty1)) {
        if (n == ex0.length) {
          int m = n * 2;
          ex0 = Arrays.copyOf(ex0, m);
          ey0 = Arrays.copyOf(ey0, m);
          ex1 = Arrays.copyOf(ex1, m);
          ey1 = Arrays.copyOf(ey1, m);
          dir = Arrays.copyOf(dir, m);
        }
        if (ty0 < ty1) {
          ex0[n] = tx0;
          ey0[n] = ty0;
          ex1[n] = tx1;
          ey1[n] = ty1;
          dir[n] = 1;
        } else {
          ex0[n] = tx1;
          ey0[n] = ty1;
          ex1[n] = tx0;
          ey1[n] = ty0;
          dir[n] = -1;
        }
        minY = Math.min(minY, ey0[n]);
        maxY = Math.max(maxY, ey1[n]);
        n++;
      }
      switch (t) {
      case PathIterator.SEG_MOVETO:
        mx = px = c[0];
        my = py = c[1];
        open = true;
        break;
      case PathIterator.SEG_LINETO:
        px = c[0];
        py = c[1];
        break;
      case PathIterator.SEG_CLOSE:
        px = mx;
        py = my;
        break;
      default:
        break;
      }
      pi.next();
    }
    if (open && (px != mx || py != my)) {
      if (n == ex0.length) {
        int m = Math.max(4, n * 2);
        ex0 = Arrays.copyOf(ex0, m);
        ey0 = Arrays.copyOf(ey0, m);
        ex1 = Arrays.copyOf(ex1, m);
        ey1 = Arrays.copyOf(ey1, m);
        dir = Arrays.copyOf(dir, m);
      }
      if (py != my) {
        if (py < my) {
          ex0[n] = px;
          ey0[n] = py;
          ex1[n] = mx;
          ey1[n] = my;
          dir[n] = 1;
        } else {
          ex0[n] = mx;
          ey0[n] = my;
          ex1[n] = px;
          ey1[n] = py;
          dir[n] = -1;
        }
        minY = Math.min(minY, ey0[n]);
        maxY = Math.max(maxY, ey1[n]);
        n++;
      }
    }
    if (n == 0) {
      return;
    }
    int yStart = Math.max(cy0, (int) Math.floor(minY));
    int yEnd = Math.min(cy1, (int) Math.ceil(maxY));
    if (yStart >= yEnd) {
      return;
    }
    Integer[] order = new Integer[n];
    for (int i = 0; i < n; i++) {
      order[i] = i;
    }
    final double[] sortY = ey0;
    Arrays.sort(order, (a, b) -> Double.compare(sortY[a], sortY[b]));
    int[] ord = new int[n];
    for (int i = 0; i < n; i++) {
      ord[i] = order[i];
    }
    int width = cx1 - cx0;
    float[] cov = new float[width];
    int[] icov = new int[aa ? width : 0];
    int[] active = new int[n];
    int activeCount = 0;
    int next = 0;
    double[] xs = new double[n];
    int[] ds = new int[n];
    int ss = aa ? kSamples : 1;
    for (int y = yStart; y < yEnd; y++) {
      int rowMin = Integer.MAX_VALUE;
      int rowMax = Integer.MIN_VALUE;
      for (int s = 0; s < ss; s++) {
        double sy = y + (s + 0.5) / ss;
        while (next < n && ey0[ord[next]] <= sy) {
          active[activeCount++] = ord[next++];
        }
        int k = 0;
        int count = 0;
        for (int i = 0; i < activeCount; i++) {
          int e = active[i];
          if (ey1[e] <= sy) {
            continue;
          }
          active[k++] = e;
          double x = ex0[e] + (ex1[e] - ex0[e]) * (sy - ey0[e]) / (ey1[e] - ey0[e]);
          int j = count++;
          while (j > 0 && xs[j - 1] > x) {
            xs[j] = xs[j - 1];
            ds[j] = ds[j - 1];
            j--;
          }
          xs[j] = x;
          ds[j] = dir[e];
        }
        activeCount = k;
        int winding = 0;
        double spanStart = 0;
        for (int i = 0; i < count; i++) {
          boolean wasIn = evenOdd ? (winding & 1) != 0 : winding != 0;
          winding += ds[i];
          boolean isIn = evenOdd ? (winding & 1) != 0 : winding != 0;
          if (!wasIn && isIn) {
            spanStart = xs[i];
          } else if (wasIn && !isIn) {
            double a = spanStart;
            double b = xs[i];
            if (aa) {
              if (b <= cx0 || a >= cx1) {
                continue;
              }
              long qa = (long) Math.ceil(Math.max(a, cx0) * kSubX);
              long qb = (long) Math.ceil(Math.min(b, cx1) * kSubX);
              if (qb <= qa) {
                continue;
              }
              int pa = (int) (qa / kSubX);
              int pb = (int) (qb / kSubX);
              for (int p = pa; p <= pb && p < cx1; p++) {
                long lo = Math.max(qa, (long) p * kSubX);
                long hi = Math.min(qb, (long) (p + 1) * kSubX);
                if (hi > lo) {
                  icov[p - cx0] += (int) (hi - lo);
                }
              }
              rowMin = Math.min(rowMin, pa);
              rowMax = Math.max(rowMax, Math.min(pb, cx1 - 1));
            } else {
              int pa = (int) Math.ceil(a - 0.5);
              int pb = (int) Math.ceil(b - 0.5);
              pa = Math.max(pa, cx0);
              pb = Math.min(pb, cx1);
              for (int p = pa; p < pb; p++) {
                cov[p - cx0] = 1f;
              }
              if (pa < pb) {
                rowMin = Math.min(rowMin, pa);
                rowMax = Math.max(rowMax, pb - 1);
              }
            }
          }
        }
      }
      if (rowMin <= rowMax) {
        if (aa) {
          for (int p = rowMin - cx0; p <= rowMax - cx0; p++) {
            cov[p] = (float) ((icov[p] * 255L) / kMaxCoverage) / 255f;
            icov[p] = 0;
          }
        }
        sink.row(y, rowMin, rowMax + 1, cov, cx0);
        Arrays.fill(cov, rowMin - cx0, rowMax + 1 - cx0, 0f);
      }
    }
  }

  private static boolean fillBox(java.awt.geom.Rectangle2D r, AffineTransform at, int cx0, int cy0, int cx1, int cy1,
      RowSink sink) {
    final int kScaleOnly = AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_UNIFORM_SCALE
        | AffineTransform.TYPE_GENERAL_SCALE | AffineTransform.TYPE_FLIP;
    if (at != null && (at.getType() & ~kScaleOnly) != 0) {
      return false;
    }
    double[] p = {r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY()};
    if (at != null) {
      at.transform(p, 0, p, 0, 2);
    }
    double x0 = Math.min(p[0], p[2]);
    double x1 = Math.max(p[0], p[2]);
    double y0 = Math.min(p[1], p[3]);
    double y1 = Math.max(p[1], p[3]);
    int px0 = Math.max(cx0, (int) Math.floor(x0));
    int px1 = Math.min(cx1, (int) Math.ceil(x1));
    int py0 = Math.max(cy0, (int) Math.floor(y0));
    int py1 = Math.min(cy1, (int) Math.ceil(y1));
    if (px0 >= px1 || py0 >= py1) {
      return true;
    }
    float[] cov = new float[cx1 - cx0];
    for (int y = py0; y < py1; y++) {
      double yc = Math.min(y + 1, y1) - Math.max(y, y0);
      for (int x = px0; x < px1; x++) {
        double xc = Math.min(x + 1, x1) - Math.max(x, x0);
        cov[x - cx0] = (float) ((int) (xc * yc * 255 + 0.5)) / 255f;
      }
      sink.row(y, px0, px1, cov, cx0);
    }
    return true;
  }

  public static void fillExact(PathIterator pi, int cx0, int cy0, int cx1, int cy1, boolean glyph, RowSink sink) {
    java.util.ArrayList<double[]> edges = new java.util.ArrayList<>();
    double[] c = new double[6];
    double mx = 0;
    double my = 0;
    double px = 0;
    double py = 0;
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    while (!pi.isDone()) {
      int t = pi.currentSegment(c);
      double nx = px;
      double ny = py;
      boolean edge = false;
      if (t == PathIterator.SEG_MOVETO) {
        if (px != mx || py != my) {
          edges.add(new double[] {px, py, mx, my});
        }
        mx = px = c[0];
        my = py = c[1];
      } else if (t == PathIterator.SEG_LINETO) {
        nx = c[0];
        ny = c[1];
        edge = true;
      } else if (t == PathIterator.SEG_CLOSE) {
        nx = mx;
        ny = my;
        edge = px != mx || py != my;
      }
      if (edge) {
        edges.add(new double[] {px, py, nx, ny});
        px = nx;
        py = ny;
      }
      pi.next();
    }
    if (px != mx || py != my) {
      edges.add(new double[] {px, py, mx, my});
    }
    for (double[] e : edges) {
      minX = Math.min(minX, Math.min(e[0], e[2]));
      maxX = Math.max(maxX, Math.max(e[0], e[2]));
      minY = Math.min(minY, Math.min(e[1], e[3]));
      maxY = Math.max(maxY, Math.max(e[1], e[3]));
    }
    if (edges.isEmpty()) {
      return;
    }
    int bx0 = Math.max(cx0, (int) Math.floor(minX));
    int bx1 = Math.min(cx1, (int) Math.ceil(maxX) + 1);
    int by0 = Math.max(cy0, (int) Math.floor(minY));
    int by1 = Math.min(cy1, (int) Math.ceil(maxY));
    if (bx0 >= bx1 || by0 >= by1) {
      return;
    }
    int w = bx1 - bx0;
    int stride = w + 3;
    float[] acc = new float[(by1 - by0) * stride];
    for (double[] e : edges) {
      accumulate(acc, stride, bx0, by0, w, by1 - by0, e[0], e[1], e[2], e[3]);
    }
    float[] cov = new float[w];
    for (int y = by0; y < by1; y++) {
      double sum = 0;
      int rowMin = Integer.MAX_VALUE;
      int rowMax = Integer.MIN_VALUE;
      int base = (y - by0) * stride;
      for (int x = 0; x < w; x++) {
        sum += acc[base + x];
        double a = Math.min(Math.abs(sum), 1.0);
        int v = Math.min(255, (int) Math.floor(a * (glyph ? 256.0 : 255.0) + 1e-6));
        if (v > 0) {
          cov[x] = v / 255f;
          rowMin = Math.min(rowMin, x);
          rowMax = x;
        }
      }
      if (rowMin <= rowMax) {
        sink.row(y, bx0 + rowMin, bx0 + rowMax + 1, cov, bx0);
        Arrays.fill(cov, 0, w, 0f);
      }
    }
  }

  private static void accumulate(float[] a, int stride, int bx, int by, int w, int h, double x0, double y0, double x1,
      double y1) {
    if (y0 == y1) {
      return;
    }
    double ox = x0;
    double oy = y0;
    double odx = x1 - x0;
    double ody = y1 - y0;
    double dir = 1;
    if (y0 > y1) {
      dir = -1;
      double t = x0;
      x0 = x1;
      x1 = t;
      t = y0;
      y0 = y1;
      y1 = t;
    }
    double dxdy = (x1 - x0) / (y1 - y0);
    double x = x0;
    int yStart = (int) Math.floor(y0);
    if (y0 < by) {
      x += dxdy * (by - y0);
      y0 = by;
      yStart = by;
    }
    int yEnd = (int) Math.min(Math.ceil(y1), by + h);
    for (int y = yStart; y < yEnd; y++) {
      double dy = Math.min(y + 1, y1) - Math.max(y, y0);
      double xn = x + dxdy * dy;
      if (y + 1 < y1 && y + 1 > y0) {
        xn = ox + Math.floor((y + 1 - oy) * 256.0 * (odx * 256.0) / (ody * 256.0)) / 256.0;
      }
      double d = dy * dir;
      double xa = Math.min(x, xn);
      double xb = Math.max(x, xn);
      x = xn;
      xa = Math.min(Math.max(xa - bx, 0), w);
      xb = Math.min(Math.max(xb - bx, 0), w);
      int row = (y - by) * stride;
      double x0f = Math.floor(xa);
      int x0i = (int) x0f;
      double x1c = Math.ceil(xb);
      int x1i = (int) x1c;
      if (x1i <= x0i + 1) {
        double xmf = 0.5 * (xa + xb) - x0f;
        a[row + x0i] += (float) (d - d * xmf);
        a[row + x0i + 1] += (float) (d * xmf);
      } else {
        double s = 1.0 / (xb - xa);
        double xf = xa - x0f;
        double a0 = 0.5 * s * (1 - xf) * (1 - xf);
        double x1f = xb - x1c + 1;
        double am = 0.5 * s * x1f * x1f;
        a[row + x0i] += (float) (d * a0);
        if (x1i == x0i + 2) {
          a[row + x0i + 1] += (float) (d * (1 - a0 - am));
        } else {
          double a1 = s * (1.5 - xf);
          a[row + x0i + 1] += (float) (d * (a1 - a0));
          for (int xi = x0i + 2; xi < x1i - 1; xi++) {
            a[row + xi] += (float) (d * s);
          }
          double a2 = a1 + (x1i - x0i - 3) * s;
          a[row + x1i - 1] += (float) (d * (1 - a2 - am));
        }
        a[row + x1i] += (float) (d * am);
      }
    }
  }
}
