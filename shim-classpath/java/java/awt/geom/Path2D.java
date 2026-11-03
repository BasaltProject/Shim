/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

import java.util.Arrays;

public abstract class Path2D implements java.awt.Shape, Cloneable {
  public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;
  public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

  static final int[] kCoordCount = {2, 2, 4, 6, 0};

  byte[] types;
  int typeCount;
  int coordCount;
  int rule;

  Path2D() { }

  Path2D(int rule, int capacity) {
    setWindingRule(rule);
    types = new byte[Math.max(capacity, 1)];
  }

  abstract double coord(int index);

  abstract void setCoord(int index, double value);

  abstract void ensureCoords(int extra);

  abstract Path2D copy();

  final void add(int type, double... values) {
    if (typeCount == 0 && type != PathIterator.SEG_MOVETO) {
      throw new IllegalPathStateException("missing initial moveto in path definition");
    }
    if (typeCount == types.length) {
      types = Arrays.copyOf(types, Math.max(8, typeCount * 2));
    }
    ensureCoords(values.length);
    types[typeCount++] = (byte) type;
    for (double v : values) {
      setCoord(coordCount++, v);
    }
  }

  public abstract void moveTo(double x, double y);

  public abstract void lineTo(double x, double y);

  public abstract void quadTo(double x1, double y1, double x2, double y2);

  public abstract void curveTo(double x1, double y1, double x2, double y2, double x3, double y3);

  public final synchronized void closePath() {
    if (typeCount == 0 || types[typeCount - 1] != PathIterator.SEG_CLOSE) {
      add(PathIterator.SEG_CLOSE);
    }
  }

  public final void append(java.awt.Shape s, boolean connect) {
    append(s.getPathIterator(null), connect);
  }

  public abstract void append(PathIterator pi, boolean connect);

  final void appendImpl(PathIterator pi, boolean connect) {
    double[] c = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(c)) {
      case PathIterator.SEG_MOVETO:
        if (!connect || typeCount == 0 || coordCount == 0) {
          moveTo(c[0], c[1]);
        } else if (types[typeCount - 1] != PathIterator.SEG_CLOSE && coord(coordCount - 2) == c[0]
            && coord(coordCount - 1) == c[1]) {
          // same point, nothing to connect
        } else {
          lineTo(c[0], c[1]);
        }
        break;
      case PathIterator.SEG_LINETO:
        lineTo(c[0], c[1]);
        break;
      case PathIterator.SEG_QUADTO:
        quadTo(c[0], c[1], c[2], c[3]);
        break;
      case PathIterator.SEG_CUBICTO:
        curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
        break;
      default:
        closePath();
        break;
      }
      pi.next();
      connect = false;
    }
  }

  public final synchronized int getWindingRule() {
    return rule;
  }

  public final void setWindingRule(int rule) {
    if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
      throw new IllegalArgumentException("winding rule must be WIND_EVEN_ODD or WIND_NON_ZERO");
    }
    this.rule = rule;
  }

  public final synchronized Point2D getCurrentPoint() {
    if (typeCount == 0 || coordCount == 0) {
      return null;
    }
    int index = coordCount;
    if (types[typeCount - 1] == PathIterator.SEG_CLOSE) {
      for (int i = typeCount - 2; i >= 0 && types[i] != PathIterator.SEG_MOVETO; i--) {
        index -= kCoordCount[types[i]];
      }
      return newPoint(coord(index - 2), coord(index - 1));
    }
    return newPoint(coord(index - 2), coord(index - 1));
  }

  abstract Point2D newPoint(double x, double y);

  public final synchronized void reset() {
    typeCount = 0;
    coordCount = 0;
  }

  public abstract void transform(AffineTransform at);

  final synchronized void transformImpl(AffineTransform at) {
    double[] p = new double[2];
    for (int i = 0; i + 1 < coordCount; i += 2) {
      p[0] = coord(i);
      p[1] = coord(i + 1);
      at.transform(p, 0, p, 0, 1);
      setCoord(i, p[0]);
      setCoord(i + 1, p[1]);
    }
  }

  public final synchronized java.awt.Shape createTransformedShape(AffineTransform at) {
    Path2D copy = copy();
    if (at != null) {
      copy.transform(at);
    }
    return copy;
  }

  public final java.awt.Rectangle getBounds() {
    return getBounds2D().getBounds();
  }

  public synchronized Rectangle2D getBounds2D() {
    double x1 = 0;
    double y1 = 0;
    double x2 = 0;
    double y2 = 0;
    for (int i = 0; i + 1 < coordCount; i += 2) {
      double x = coord(i);
      double y = coord(i + 1);
      if (i == 0) {
        x1 = x2 = x;
        y1 = y2 = y;
      } else {
        x1 = Math.min(x1, x);
        y1 = Math.min(y1, y);
        x2 = Math.max(x2, x);
        y2 = Math.max(y2, y);
      }
    }
    return newRect(x1, y1, x2 - x1, y2 - y1);
  }

  abstract Rectangle2D newRect(double x, double y, double w, double h);

  public final boolean contains(double x, double y) {
    return contains(getPathIterator(null), x, y);
  }

  public final boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
  }

  public final boolean contains(double x, double y, double w, double h) {
    return contains(getPathIterator(null), x, y, w, h);
  }

  public final boolean contains(Rectangle2D r) {
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public final boolean intersects(double x, double y, double w, double h) {
    return intersects(getPathIterator(null), x, y, w, h);
  }

  public final boolean intersects(Rectangle2D r) {
    return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public final PathIterator getPathIterator(AffineTransform at, double flatness) {
    return new FlatteningPathIterator(getPathIterator(at), flatness);
  }

  public abstract Object clone();

  public abstract void trimToSize();

  final synchronized void trimImpl() {
    types = Arrays.copyOf(types, typeCount);
  }

  private static double[][] flatten(PathIterator pi) {
    java.util.ArrayList<double[]> edges = new java.util.ArrayList<>();
    double[] c = new double[6];
    double mx = 0;
    double my = 0;
    double cx = 0;
    double cy = 0;
    PathIterator fi = new FlatteningPathIterator(pi, 0.05, 12);
    while (!fi.isDone()) {
      switch (fi.currentSegment(c)) {
      case PathIterator.SEG_MOVETO:
        if (cx != mx || cy != my) {
          edges.add(new double[] {cx, cy, mx, my});
        }
        mx = cx = c[0];
        my = cy = c[1];
        break;
      case PathIterator.SEG_LINETO:
        edges.add(new double[] {cx, cy, c[0], c[1]});
        cx = c[0];
        cy = c[1];
        break;
      default:
        edges.add(new double[] {cx, cy, mx, my});
        cx = mx;
        cy = my;
        break;
      }
      fi.next();
    }
    if (cx != mx || cy != my) {
      edges.add(new double[] {cx, cy, mx, my});
    }
    return edges.toArray(new double[0][]);
  }

  private static int winding(double[][] edges, double x, double y) {
    int w = 0;
    for (double[] e : edges) {
      if (e[1] <= y) {
        if (e[3] > y && (e[2] - e[0]) * (y - e[1]) - (x - e[0]) * (e[3] - e[1]) > 0) {
          w++;
        }
      } else if (e[3] <= y && (e[2] - e[0]) * (y - e[1]) - (x - e[0]) * (e[3] - e[1]) < 0) {
        w--;
      }
    }
    return w;
  }

  private static boolean inside(double[][] edges, int rule, double x, double y) {
    int w = winding(edges, x, y);
    return rule == WIND_NON_ZERO ? w != 0 : (w & 1) != 0;
  }

  private static boolean edgeHitsRect(double[] e, double x0, double y0, double x1, double y1) {
    double ax = e[0];
    double ay = e[1];
    double bx = e[2];
    double by = e[3];
    if (Math.max(ax, bx) <= x0 || Math.min(ax, bx) >= x1 || Math.max(ay, by) <= y0 || Math.min(ay, by) >= y1) {
      return false;
    }
    if (ax > x0 && ax < x1 && ay > y0 && ay < y1 || bx > x0 && bx < x1 && by > y0 && by < y1) {
      return true;
    }
    return Line2D.linesIntersect(ax, ay, bx, by, x0, y0, x1, y0) && !touchOnly(e, x0, y0, x1, y0)
        || Line2D.linesIntersect(ax, ay, bx, by, x1, y0, x1, y1) && !touchOnly(e, x1, y0, x1, y1)
        || Line2D.linesIntersect(ax, ay, bx, by, x1, y1, x0, y1) && !touchOnly(e, x1, y1, x0, y1)
        || Line2D.linesIntersect(ax, ay, bx, by, x0, y1, x0, y0) && !touchOnly(e, x0, y1, x0, y0);
  }

  private static boolean touchOnly(double[] e, double x0, double y0, double x1, double y1) {
    boolean collinear = Line2D.ptLineDist(e[0], e[1], x0, y0, x1, y1) == 0 && Line2D.ptLineDist(e[2], e[3], x0, y0, x1, y1) == 0;
    return collinear;
  }

  public static boolean contains(PathIterator pi, double x, double y) {
    return inside(flatten(pi), pi.getWindingRule(), x, y);
  }

  public static boolean contains(PathIterator pi, Point2D p) {
    return contains(pi, p.getX(), p.getY());
  }

  public static boolean contains(PathIterator pi, double x, double y, double w, double h) {
    if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h) || w <= 0 || h <= 0) {
      return false;
    }
    double[][] edges = flatten(pi);
    int rule = pi.getWindingRule();
    for (double[] e : edges) {
      if (edgeHitsRect(e, x, y, x + w, y + h)) {
        return false;
      }
    }
    return inside(edges, rule, x + w / 2, y + h / 2);
  }

  public static boolean contains(PathIterator pi, Rectangle2D r) {
    return contains(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public static boolean intersects(PathIterator pi, double x, double y, double w, double h) {
    if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h) || w <= 0 || h <= 0) {
      return false;
    }
    double[][] edges = flatten(pi);
    int rule = pi.getWindingRule();
    for (double[] e : edges) {
      if (edgeHitsRect(e, x, y, x + w, y + h)) {
        return true;
      }
    }
    return inside(edges, rule, x + w / 2, y + h / 2);
  }

  public static boolean intersects(PathIterator pi, Rectangle2D r) {
    return intersects(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  final class Iter implements PathIterator {
    private final AffineTransform at;
    private int type;
    private int coord;

    Iter(AffineTransform at) {
      this.at = at;
    }

    public int getWindingRule() {
      return rule;
    }

    public boolean isDone() {
      return type >= typeCount;
    }

    public void next() {
      coord += kCoordCount[types[type++]];
    }

    public int currentSegment(float[] out) {
      int t = types[type];
      int n = kCoordCount[t];
      for (int i = 0; i < n; i++) {
        out[i] = (float) coord(coord + i);
      }
      if (at != null && n > 0) {
        at.transform(out, 0, out, 0, n / 2);
      }
      return t;
    }

    public int currentSegment(double[] out) {
      int t = types[type];
      int n = kCoordCount[t];
      for (int i = 0; i < n; i++) {
        out[i] = coord(coord + i);
      }
      if (at != null && n > 0) {
        at.transform(out, 0, out, 0, n / 2);
      }
      return t;
    }
  }

  public static class Float extends Path2D implements java.io.Serializable {
    transient float[] coords;

    public Float() {
      this(WIND_NON_ZERO, 20);
    }

    public Float(int rule) {
      this(rule, 20);
    }

    public Float(int rule, int initialCapacity) {
      super(rule, initialCapacity);
      coords = new float[Math.max(initialCapacity, 1) * 2];
    }

    public Float(java.awt.Shape s) {
      this(s, null);
    }

    public Float(java.awt.Shape s, AffineTransform at) {
      this(WIND_NON_ZERO, 20);
      PathIterator pi = s.getPathIterator(at);
      setWindingRule(pi.getWindingRule());
      append(pi, false);
    }

    @Override
    double coord(int index) {
      return coords[index];
    }

    @Override
    void setCoord(int index, double value) {
      coords[index] = (float) value;
    }

    @Override
    void ensureCoords(int extra) {
      if (coordCount + extra > coords.length) {
        coords = Arrays.copyOf(coords, Math.max(coords.length * 2, coordCount + extra));
      }
    }

    @Override
    Path2D copy() {
      Path2D.Float p = this instanceof GeneralPath ? new GeneralPath() : new Path2D.Float();
      p.rule = rule;
      p.types = Arrays.copyOf(types, Math.max(typeCount, 1));
      p.typeCount = typeCount;
      p.coords = Arrays.copyOf(coords, Math.max(coordCount, 2));
      p.coordCount = coordCount;
      return p;
    }

    @Override
    Point2D newPoint(double x, double y) {
      return new Point2D.Float((float) x, (float) y);
    }

    @Override
    Rectangle2D newRect(double x, double y, double w, double h) {
      return new Rectangle2D.Float((float) x, (float) y, (float) w, (float) h);
    }

    public final synchronized void moveTo(float x, float y) {
      moveTo((double) x, (double) y);
    }

    @Override
    public final synchronized void moveTo(double x, double y) {
      if (typeCount > 0 && types[typeCount - 1] == PathIterator.SEG_MOVETO) {
        coords[coordCount - 2] = (float) x;
        coords[coordCount - 1] = (float) y;
      } else {
        add(PathIterator.SEG_MOVETO, x, y);
      }
    }

    public final synchronized void lineTo(float x, float y) {
      lineTo((double) x, (double) y);
    }

    @Override
    public final synchronized void lineTo(double x, double y) {
      add(PathIterator.SEG_LINETO, x, y);
    }

    public final synchronized void quadTo(float x1, float y1, float x2, float y2) {
      quadTo((double) x1, (double) y1, (double) x2, (double) y2);
    }

    @Override
    public final synchronized void quadTo(double x1, double y1, double x2, double y2) {
      add(PathIterator.SEG_QUADTO, x1, y1, x2, y2);
    }

    public final synchronized void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
      curveTo((double) x1, (double) y1, (double) x2, (double) y2, (double) x3, (double) y3);
    }

    @Override
    public final synchronized void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
      add(PathIterator.SEG_CUBICTO, x1, y1, x2, y2, x3, y3);
    }

    @Override
    public final void append(PathIterator pi, boolean connect) {
      appendImpl(pi, connect);
    }

    @Override
    public final void transform(AffineTransform at) {
      transformImpl(at);
    }

    @Override
    public final PathIterator getPathIterator(AffineTransform at) {
      return new Iter(at);
    }

    @Override
    public final Object clone() {
      return copy();
    }

    @Override
    public final synchronized void trimToSize() {
      trimImpl();
      coords = Arrays.copyOf(coords, coordCount);
    }
  }

  public static class Double extends Path2D implements java.io.Serializable {
    transient double[] coords;

    public Double() {
      this(WIND_NON_ZERO, 20);
    }

    public Double(int rule) {
      this(rule, 20);
    }

    public Double(int rule, int initialCapacity) {
      super(rule, initialCapacity);
      coords = new double[Math.max(initialCapacity, 1) * 2];
    }

    public Double(java.awt.Shape s) {
      this(s, null);
    }

    public Double(java.awt.Shape s, AffineTransform at) {
      this(WIND_NON_ZERO, 20);
      PathIterator pi = s.getPathIterator(at);
      setWindingRule(pi.getWindingRule());
      append(pi, false);
    }

    @Override
    double coord(int index) {
      return coords[index];
    }

    @Override
    void setCoord(int index, double value) {
      coords[index] = value;
    }

    @Override
    void ensureCoords(int extra) {
      if (coordCount + extra > coords.length) {
        coords = Arrays.copyOf(coords, Math.max(coords.length * 2, coordCount + extra));
      }
    }

    @Override
    Path2D copy() {
      Path2D.Double p = new Path2D.Double();
      p.rule = rule;
      p.types = Arrays.copyOf(types, Math.max(typeCount, 1));
      p.typeCount = typeCount;
      p.coords = Arrays.copyOf(coords, Math.max(coordCount, 2));
      p.coordCount = coordCount;
      return p;
    }

    @Override
    Point2D newPoint(double x, double y) {
      return new Point2D.Double(x, y);
    }

    @Override
    Rectangle2D newRect(double x, double y, double w, double h) {
      return new Rectangle2D.Double(x, y, w, h);
    }

    @Override
    public final synchronized void moveTo(double x, double y) {
      if (typeCount > 0 && types[typeCount - 1] == PathIterator.SEG_MOVETO) {
        coords[coordCount - 2] = x;
        coords[coordCount - 1] = y;
      } else {
        add(PathIterator.SEG_MOVETO, x, y);
      }
    }

    @Override
    public final synchronized void lineTo(double x, double y) {
      add(PathIterator.SEG_LINETO, x, y);
    }

    @Override
    public final synchronized void quadTo(double x1, double y1, double x2, double y2) {
      add(PathIterator.SEG_QUADTO, x1, y1, x2, y2);
    }

    @Override
    public final synchronized void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
      add(PathIterator.SEG_CUBICTO, x1, y1, x2, y2, x3, y3);
    }

    @Override
    public final void append(PathIterator pi, boolean connect) {
      appendImpl(pi, connect);
    }

    @Override
    public final void transform(AffineTransform at) {
      transformImpl(at);
    }

    @Override
    public final PathIterator getPathIterator(AffineTransform at) {
      return new Iter(at);
    }

    @Override
    public final Object clone() {
      return copy();
    }

    @Override
    public final synchronized void trimToSize() {
      trimImpl();
      coords = Arrays.copyOf(coords, coordCount);
    }
  }
}
