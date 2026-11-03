/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Area implements java.awt.Shape, Cloneable {
  private static final int kUnion = 0;
  private static final int kSubtract = 1;
  private static final int kIntersect = 2;
  private static final int kXor = 3;
  private static final double kEps = 1e-9;

  // each trapezoid is {y0, y1, xl0, xl1, xr0, xr1}
  private List<double[]> traps = new ArrayList<>();
  private boolean curved;

  public Area() { }

  public Area(java.awt.Shape s) {
    Area a = null;
    if (((a = s instanceof Area ? (Area) s : null) != null)) {
      traps = new ArrayList<>(a.traps);
      curved = a.curved;
      return;
    }
    PathIterator probe = s.getPathIterator(null);
    double[] c = new double[6];
    while (!probe.isDone()) {
      int t = probe.currentSegment(c);
      if (t == PathIterator.SEG_QUADTO || t == PathIterator.SEG_CUBICTO) {
        curved = true;
      }
      probe.next();
    }
    PathIterator pi = new FlatteningPathIterator(s.getPathIterator(null), 0.02, 12);
    List<double[]> edges = new ArrayList<>();
    double mx = 0;
    double my = 0;
    double cx = 0;
    double cy = 0;
    while (!pi.isDone()) {
      switch (pi.currentSegment(c)) {
      case PathIterator.SEG_MOVETO:
        addEdge(edges, cx, cy, mx, my, 0);
        mx = cx = c[0];
        my = cy = c[1];
        break;
      case PathIterator.SEG_LINETO:
        addEdge(edges, cx, cy, c[0], c[1], 0);
        cx = c[0];
        cy = c[1];
        break;
      default:
        addEdge(edges, cx, cy, mx, my, 0);
        cx = mx;
        cy = my;
        break;
      }
      pi.next();
    }
    addEdge(edges, cx, cy, mx, my, 0);
    traps = decompose(edges, pi.getWindingRule() == PathIterator.WIND_NON_ZERO, true, kUnion);
  }

  private static void addEdge(List<double[]> edges, double x0, double y0, double x1, double y1, int owner) {
    if (y0 == y1) {
      return;
    }
    if (y0 < y1) {
      edges.add(new double[] {x0, y0, x1, y1, owner, 1});
    } else {
      edges.add(new double[] {x1, y1, x0, y0, owner, -1});
    }
  }

  private static double xAt(double[] e, double y) {
    return e[0] + (e[2] - e[0]) * (y - e[1]) / (e[3] - e[1]);
  }

  private List<double[]> toEdges(int owner) {
    List<double[]> edges = new ArrayList<>();
    for (double[] t : traps) {
      edges.add(new double[] {t[2], t[0], t[3], t[1], owner, 1});
      edges.add(new double[] {t[4], t[0], t[5], t[1], owner, -1});
    }
    return edges;
  }

  private static List<double[]> decompose(List<double[]> edges, boolean nonZeroA, boolean nonZeroB, int op) {
    double[] ys = new double[edges.size() * 2];
    int n = 0;
    for (double[] e : edges) {
      ys[n++] = e[1];
      ys[n++] = e[3];
    }
    int count = n;
    int m = edges.size();
    if (m <= 400) {
      double[] extra = new double[16];
      int en = 0;
      for (int i = 0; i < m; i++) {
        double[] a = edges.get(i);
        for (int j = i + 1; j < m; j++) {
          double[] b = edges.get(j);
          double lo = Math.max(a[1], b[1]);
          double hi = Math.min(a[3], b[3]);
          if (lo >= hi) {
            continue;
          }
          double da = xAt(a, lo) - xAt(b, lo);
          double db = xAt(a, hi) - xAt(b, hi);
          if ((da < -kEps && db > kEps) || (da > kEps && db < -kEps)) {
            double y = lo + (hi - lo) * da / (da - db);
            if (en == extra.length) {
              extra = Arrays.copyOf(extra, en * 2);
            }
            extra[en++] = y;
          }
        }
      }
      ys = Arrays.copyOf(ys, count + en);
      System.arraycopy(extra, 0, ys, count, en);
      count += en;
    }
    ys = Arrays.copyOf(ys, count);
    Arrays.sort(ys);
    List<double[]> out = new ArrayList<>();
    List<double[]> active = new ArrayList<>();
    for (int i = 0; i + 1 < ys.length; i++) {
      double y0 = ys[i];
      double y1 = ys[i + 1];
      if (y1 - y0 <= kEps) {
        continue;
      }
      double mid = (y0 + y1) / 2;
      active.clear();
      for (double[] e : edges) {
        if (e[1] <= mid && e[3] >= mid) {
          active.add(e);
        }
      }
      if (active.isEmpty()) {
        continue;
      }
      active.sort((p, q) -> java.lang.Double.compare(xAt(p, mid), xAt(q, mid)));
      int wa = 0;
      int wb = 0;
      double[] left = null;
      for (int k = 0; k < active.size(); k++) {
        double[] e = active.get(k);
        if (e[4] == 0) {
          wa += (int) e[5];
        } else {
          wb += (int) e[5];
        }
        boolean ia = inside(wa, nonZeroA);
        boolean ib = inside(wb, nonZeroB);
        boolean in = combine(op, ia, ib);
        if (in && left == null) {
          left = e;
        } else if (!in && left != null) {
          if (xAt(e, mid) - xAt(left, mid) > kEps) {
            out.add(new double[] {y0, y1, xAt(left, y0), xAt(left, y1), xAt(e, y0), xAt(e, y1)});
          }
          left = null;
        }
      }
    }
    return merge(out);
  }

  private static boolean inside(int w, boolean nonZero) {
    return nonZero ? w != 0 : (w & 1) != 0;
  }

  private static boolean combine(int op, boolean a, boolean b) {
    switch (op) {
    case kUnion:
      return a || b;
    case kSubtract:
      return a && !b;
    case kIntersect:
      return a && b;
    default:
      return a != b;
    }
  }

  private static List<double[]> merge(List<double[]> in) {
    List<double[]> out = new ArrayList<>();
    for (double[] t : in) {
      boolean merged = false;
      for (int i = out.size() - 1; i >= 0 && i >= out.size() - 8; i--) {
        double[] p = out.get(i);
        if (Math.abs(p[1] - t[0]) < kEps && Math.abs(p[3] - t[2]) < kEps && Math.abs(p[5] - t[4]) < kEps
            && sameSlope(p, t)) {
          out.set(i, new double[] {p[0], t[1], p[2], t[3], p[4], t[5]});
          merged = true;
          break;
        }
      }
      if (!merged) {
        out.add(t);
      }
    }
    return out;
  }

  private static boolean sameSlope(double[] p, double[] t) {
    double hp = p[1] - p[0];
    double ht = t[1] - t[0];
    return Math.abs((p[3] - p[2]) * ht - (t[3] - t[2]) * hp) < 1e-7 && Math.abs((p[5] - p[4]) * ht - (t[5] - t[4]) * hp) < 1e-7;
  }

  private void operate(Area other, int op) {
    List<double[]> edges = toEdges(0);
    edges.addAll(other.toEdges(1));
    traps = decompose(edges, true, true, op);
    curved |= other.curved;
  }

  public void add(Area rhs) {
    operate(rhs, kUnion);
  }

  public void subtract(Area rhs) {
    operate(rhs, kSubtract);
  }

  public void intersect(Area rhs) {
    operate(rhs, kIntersect);
  }

  public void exclusiveOr(Area rhs) {
    operate(rhs, kXor);
  }

  public void reset() {
    traps = new ArrayList<>();
    curved = false;
  }

  public boolean isEmpty() {
    return traps.isEmpty();
  }

  public boolean isPolygonal() {
    return !curved;
  }

  public boolean isRectangular() {
    if (traps.isEmpty()) {
      return true;
    }
    if (curved) {
      return false;
    }
    Rectangle2D b = getBounds2D();
    Area r = new Area(b);
    r.exclusiveOr(this);
    return r.isEmpty();
  }

  public boolean isSingular() {
    int n = traps.size();
    if (n <= 1) {
      return true;
    }
    int[] parent = new int[n];
    for (int i = 0; i < n; i++) {
      parent[i] = i;
    }
    for (int i = 0; i < n; i++) {
      double[] a = traps.get(i);
      for (int j = i + 1; j < n; j++) {
        double[] b = traps.get(j);
        boolean touch = false;
        if (Math.abs(a[1] - b[0]) < kEps) {
          touch = Math.max(a[3], b[2]) < Math.min(a[5], b[4]) - kEps;
        } else if (Math.abs(b[1] - a[0]) < kEps) {
          touch = Math.max(b[3], a[2]) < Math.min(b[5], a[4]) - kEps;
        }
        if (touch) {
          int ra = find(parent, i);
          int rb = find(parent, j);
          parent[ra] = rb;
        }
      }
    }
    int root = find(parent, 0);
    for (int i = 1; i < n; i++) {
      if (find(parent, i) != root) {
        return false;
      }
    }
    return true;
  }

  private static int find(int[] p, int i) {
    while (p[i] != i) {
      p[i] = p[p[i]];
      i = p[i];
    }
    return i;
  }

  public Rectangle2D getBounds2D() {
    if (traps.isEmpty()) {
      return new Rectangle2D.Double();
    }
    double x0 = java.lang.Double.MAX_VALUE;
    double y0 = java.lang.Double.MAX_VALUE;
    double x1 = -java.lang.Double.MAX_VALUE;
    double y1 = -java.lang.Double.MAX_VALUE;
    for (double[] t : traps) {
      y0 = Math.min(y0, t[0]);
      y1 = Math.max(y1, t[1]);
      x0 = Math.min(x0, Math.min(t[2], t[3]));
      x1 = Math.max(x1, Math.max(t[4], t[5]));
    }
    return new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0);
  }

  public java.awt.Rectangle getBounds() {
    return getBounds2D().getBounds();
  }

  public boolean equals(Area other) {
    if (other == null) {
      return false;
    }
    Area x = new Area(this);
    x.exclusiveOr(other);
    return x.isEmpty();
  }

  public void transform(AffineTransform t) {
    if (t == null) {
      throw new NullPointerException("transform must not be null");
    }
    Area a = new Area(new Path2D.Double(this, t));
    traps = a.traps;
    curved = curved || a.curved;
  }

  public Area createTransformedArea(AffineTransform t) {
    Area a = new Area(this);
    a.transform(t);
    return a;
  }

  public boolean contains(double x, double y) {
    for (double[] t : traps) {
      if (y >= t[0] && y < t[1]) {
        double f = (y - t[0]) / (t[1] - t[0]);
        double l = t[2] + (t[3] - t[2]) * f;
        double r = t[4] + (t[5] - t[4]) * f;
        if (x >= l && x < r) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
  }

  public boolean contains(double x, double y, double w, double h) {
    if (w < 0 || h < 0) {
      return false;
    }
    Area r = new Area(new Rectangle2D.Double(x, y, w, h));
    r.subtract(this);
    return r.isEmpty();
  }

  public boolean contains(Rectangle2D r) {
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public boolean intersects(double x, double y, double w, double h) {
    if (w < 0 || h < 0) {
      return false;
    }
    Area r = new Area(new Rectangle2D.Double(x, y, w, h));
    r.intersect(this);
    return !r.isEmpty();
  }

  public boolean intersects(Rectangle2D r) {
    return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public PathIterator getPathIterator(AffineTransform at) {
    Path2D.Double p = new Path2D.Double();
    for (double[] t : traps) {
      p.moveTo(t[2], t[0]);
      p.lineTo(t[4], t[0]);
      p.lineTo(t[5], t[1]);
      p.lineTo(t[3], t[1]);
      p.closePath();
    }
    return p.getPathIterator(at);
  }

  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return getPathIterator(at);
  }

  public Object clone() {
    return new Area(this);
  }
}
