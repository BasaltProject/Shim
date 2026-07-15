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

  private static final int kSamples = 4;

  private Rasterizer() { }

  public static void fill(Shape shape, AffineTransform at, int cx0, int cy0, int cx1, int cy1, boolean aa,
      RowSink sink) {
    if (cx1 <= cx0 || cy1 <= cy0) {
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
    int[] active = new int[n];
    int activeCount = 0;
    int next = 0;
    double[] xs = new double[n];
    int[] ds = new int[n];
    int ss = aa ? kSamples : 1;
    float weight = 1f / ss;
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
              a = Math.max(a, cx0);
              b = Math.min(b, cx1);
              int pa = (int) Math.floor(a);
              int pb = (int) Math.floor(b);
              if (pa == pb) {
                cov[pa - cx0] += (float) (b - a) * weight;
              } else {
                cov[pa - cx0] += (float) (pa + 1 - a) * weight;
                for (int p = pa + 1; p < pb; p++) {
                  cov[p - cx0] += weight;
                }
                if (pb < cx1) {
                  cov[pb - cx0] += (float) (b - pb) * weight;
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
        sink.row(y, rowMin, rowMax + 1, cov, cx0);
        Arrays.fill(cov, rowMin - cx0, rowMax + 1 - cx0, 0f);
      }
    }
  }
}
