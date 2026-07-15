/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public final class Stroker {
  private Stroker() { }

  public static Shape stroke(Shape s, float width, int cap, int join, float miterLimit, float[] dash, float phase) {
    Path2D.Double out = new Path2D.Double(Path2D.WIND_NON_ZERO);
    double hw = width / 2.0;
    PathIterator pi = new java.awt.geom.FlatteningPathIterator(s.getPathIterator(null), 0.05, 10);
    double[] c = new double[6];
    List<double[]> pts = new ArrayList<>();
    boolean closed = false;
    while (!pi.isDone()) {
      int t = pi.currentSegment(c);
      switch (t) {
      case PathIterator.SEG_MOVETO:
        emit(out, pts, closed, hw, cap, join, miterLimit, dash, phase);
        pts.clear();
        closed = false;
        pts.add(new double[] {c[0], c[1]});
        break;
      case PathIterator.SEG_LINETO:
        pts.add(new double[] {c[0], c[1]});
        break;
      case PathIterator.SEG_CLOSE:
        closed = true;
        emit(out, pts, true, hw, cap, join, miterLimit, dash, phase);
        double[] first = pts.isEmpty() ? null : pts.get(0);
        pts.clear();
        if (first != null) {
          pts.add(first);
        }
        closed = false;
        pts.clear();
        break;
      default:
        break;
      }
      pi.next();
    }
    emit(out, pts, false, hw, cap, join, miterLimit, dash, phase);
    return out;
  }

  private static void emit(Path2D.Double out, List<double[]> raw, boolean closed, double hw, int cap, int join,
      double miterLimit, float[] dash, float phase) {
    if (raw.isEmpty()) {
      return;
    }
    List<double[]> pts = new ArrayList<>();
    for (double[] p : raw) {
      if (pts.isEmpty() || p[0] != pts.get(pts.size() - 1)[0] || p[1] != pts.get(pts.size() - 1)[1]) {
        pts.add(p);
      }
    }
    if (closed && pts.size() > 1) {
      double[] f = pts.get(0);
      double[] l = pts.get(pts.size() - 1);
      if (f[0] == l[0] && f[1] == l[1]) {
        pts.remove(pts.size() - 1);
      }
    }
    if (pts.size() == 1) {
      if (cap == java.awt.BasicStroke.CAP_ROUND) {
        circle(out, pts.get(0)[0], pts.get(0)[1], hw);
      } else if (cap == java.awt.BasicStroke.CAP_SQUARE) {
        double x = pts.get(0)[0];
        double y = pts.get(0)[1];
        poly(out, x - hw, y - hw, x + hw, y - hw, x + hw, y + hw, x - hw, y + hw);
      }
      return;
    }
    if (closed) {
      pts.add(pts.get(0));
    }
    if (dash == null || dash.length == 0) {
      strokePolyline(out, pts, closed, hw, cap, join, miterLimit);
      return;
    }
    List<List<double[]>> pieces = dashed(pts, dash, phase, closed);
    for (List<double[]> piece : pieces) {
      if (piece.size() == 1) {
        piece.add(piece.get(0));
      }
      strokePolyline(out, piece, false, hw, cap, join, miterLimit);
    }
  }

  private static List<List<double[]>> dashed(List<double[]> pts, float[] dash, float phase, boolean closed) {
    List<List<double[]>> result = new ArrayList<>();
    int idx = 0;
    double remaining = dash[0];
    boolean on = true;
    double ph = phase;
    double total = 0;
    for (float d : dash) {
      total += d;
    }
    if (dash.length % 2 == 1) {
      total *= 2;
    }
    if (total > 0) {
      ph = ph % total;
    }
    while (ph > 0) {
      if (ph >= remaining) {
        ph -= remaining;
        idx = (idx + 1) % dash.length;
        remaining = dash[idx];
        on = !on;
      } else {
        remaining -= ph;
        ph = 0;
      }
    }
    List<double[]> current = null;
    if (on) {
      current = new ArrayList<>();
      current.add(pts.get(0));
    }
    for (int i = 0; i + 1 < pts.size(); i++) {
      double[] a = pts.get(i);
      double[] b = pts.get(i + 1);
      double len = Math.hypot(b[0] - a[0], b[1] - a[1]);
      double pos = 0;
      while (len - pos > remaining) {
        pos += remaining;
        double f = pos / len;
        double[] p = {a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f};
        if (on) {
          current.add(p);
          result.add(current);
          current = null;
        } else {
          current = new ArrayList<>();
          current.add(p);
        }
        on = !on;
        idx = (idx + 1) % dash.length;
        remaining = dash[idx];
        if (remaining == 0 && on == false) {
          remaining = 0;
        }
        if (remaining <= 0) {
          remaining = 1e-6;
        }
      }
      remaining -= len - pos;
      if (on && current != null) {
        current.add(b);
      }
    }
    if (on && current != null && current.size() > 1) {
      result.add(current);
    }
    return result;
  }

  private static void strokePolyline(Path2D.Double out, List<double[]> pts, boolean closed, double hw, int cap, int join,
      double miterLimit) {
    int n = pts.size();
    for (int i = 0; i + 1 < n; i++) {
      double[] a = pts.get(i);
      double[] b = pts.get(i + 1);
      double dx = b[0] - a[0];
      double dy = b[1] - a[1];
      double len = Math.hypot(dx, dy);
      if (len == 0) {
        continue;
      }
      double ux = dx / len;
      double uy = dy / len;
      double nx = -uy * hw;
      double ny = ux * hw;
      double ax = a[0];
      double ay = a[1];
      double bx = b[0];
      double by = b[1];
      if (!closed && cap == java.awt.BasicStroke.CAP_SQUARE) {
        if (i == 0) {
          ax -= ux * hw;
          ay -= uy * hw;
        }
        if (i == n - 2) {
          bx += ux * hw;
          by += uy * hw;
        }
      }
      poly(out, ax + nx, ay + ny, bx + nx, by + ny, bx - nx, by - ny, ax - nx, ay - ny);
      if (!closed && cap == java.awt.BasicStroke.CAP_ROUND) {
        if (i == 0) {
          circle(out, a[0], a[1], hw);
        }
        if (i == n - 2) {
          circle(out, b[0], b[1], hw);
        }
      }
    }
    int joinStart = closed ? 0 : 1;
    int joinEnd = closed ? n - 1 : n - 1;
    for (int i = joinStart; i < joinEnd; i++) {
      double[] p = pts.get(i);
      double[] prev = i == 0 ? pts.get(n - 2) : pts.get(i - 1);
      double[] next = pts.get(i + 1);
      joinAt(out, prev, p, next, hw, join, miterLimit);
    }
  }

  private static void joinAt(Path2D.Double out, double[] prev, double[] p, double[] next, double hw, int join,
      double miterLimit) {
    double d1x = p[0] - prev[0];
    double d1y = p[1] - prev[1];
    double d2x = next[0] - p[0];
    double d2y = next[1] - p[1];
    double l1 = Math.hypot(d1x, d1y);
    double l2 = Math.hypot(d2x, d2y);
    if (l1 == 0 || l2 == 0) {
      return;
    }
    d1x /= l1;
    d1y /= l1;
    d2x /= l2;
    d2y /= l2;
    double cross = d1x * d2y - d1y * d2x;
    double dot = d1x * d2x + d1y * d2y;
    if (Math.abs(cross) < 1e-9 && dot > 0) {
      return;
    }
    if (join == java.awt.BasicStroke.JOIN_ROUND) {
      circle(out, p[0], p[1], hw);
      return;
    }
    double side = cross > 0 ? -1 : 1;
    double n1x = -d1y * hw * side;
    double n1y = d1x * hw * side;
    double n2x = -d2y * hw * side;
    double n2y = d2x * hw * side;
    double ax = p[0] + n1x;
    double ay = p[1] + n1y;
    double bx = p[0] + n2x;
    double by = p[1] + n2y;
    if (join == java.awt.BasicStroke.JOIN_MITER) {
      double cosHalf = Math.sqrt((1 + dot) / 2);
      double sinHalf = Math.sqrt(Math.max(0, (1 - dot) / 2));
      // miter ratio = 1 / sin(theta/2) where theta is the angle between segments
      double theta = Math.PI - Math.acos(Math.max(-1, Math.min(1, dot)));
      double ratio = 1 / Math.sin(theta / 2);
      if (ratio <= miterLimit && Math.abs(cross) > 1e-9) {
        double mx = n1x + n2x;
        double my = n1y + n2y;
        double ml = mx * mx + my * my;
        double scale = 2 * hw * hw / ml;
        double tipx = p[0] + mx * scale * 1.0;
        double tipy = p[1] + my * scale * 1.0;
        poly(out, p[0], p[1], ax, ay, tipx, tipy, bx, by);
        return;
      }
    }
    poly(out, p[0], p[1], ax, ay, bx, by);
  }

  private static void circle(Path2D.Double out, double cx, double cy, double r) {
    int n = Math.max(8, Math.min(48, (int) (r * 3) + 8));
    double[] xy = new double[n * 2];
    for (int i = 0; i < n; i++) {
      double a = 2 * Math.PI * i / n;
      xy[i * 2] = cx + r * Math.cos(a);
      xy[i * 2 + 1] = cy + r * Math.sin(a);
    }
    poly(out, xy);
  }

  private static void poly(Path2D.Double out, double... xy) {
    int n = xy.length / 2;
    double area = 0;
    for (int i = 0; i < n; i++) {
      int j = (i + 1) % n;
      area += xy[i * 2] * xy[j * 2 + 1] - xy[j * 2] * xy[i * 2 + 1];
    }
    if (area < 0) {
      for (int i = 0; i < n / 2; i++) {
        int j = n - 1 - i;
        double tx = xy[i * 2];
        double ty = xy[i * 2 + 1];
        xy[i * 2] = xy[j * 2];
        xy[i * 2 + 1] = xy[j * 2 + 1];
        xy[j * 2] = tx;
        xy[j * 2 + 1] = ty;
      }
    }
    out.moveTo(xy[0], xy[1]);
    for (int i = 1; i < n; i++) {
      out.lineTo(xy[i * 2], xy[i * 2 + 1]);
    }
    out.closePath();
  }
}
