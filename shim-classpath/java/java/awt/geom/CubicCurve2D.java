/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public abstract class CubicCurve2D implements java.awt.Shape, Cloneable {
  protected CubicCurve2D() { }

  public static class Float extends CubicCurve2D implements java.io.Serializable {
    public float x1;
    public float y1;
    public float ctrlx1;
    public float ctrly1;
    public float ctrlx2;
    public float ctrly2;
    public float x2;
    public float y2;

    public Float() { }

    public Float(float x1, float y1, float ctrlx1, float ctrly1, float ctrlx2, float ctrly2, float x2, float y2) {
      setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
    }

    public double getX1() { return x1; }

    public double getY1() { return y1; }

    public Point2D getP1() { return new Point2D.Float(x1, y1); }

    public double getCtrlX1() { return ctrlx1; }

    public double getCtrlY1() { return ctrly1; }

    public Point2D getCtrlP1() { return new Point2D.Float(ctrlx1, ctrly1); }

    public double getCtrlX2() { return ctrlx2; }

    public double getCtrlY2() { return ctrly2; }

    public Point2D getCtrlP2() { return new Point2D.Float(ctrlx2, ctrly2); }

    public double getX2() { return x2; }

    public double getY2() { return y2; }

    public Point2D getP2() { return new Point2D.Float(x2, y2); }

    public void setCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2,
        double y2) {
      this.x1 = (float) x1;
      this.y1 = (float) y1;
      this.ctrlx1 = (float) ctrlx1;
      this.ctrly1 = (float) ctrly1;
      this.ctrlx2 = (float) ctrlx2;
      this.ctrly2 = (float) ctrly2;
      this.x2 = (float) x2;
      this.y2 = (float) y2;
    }

    public void setCurve(float x1, float y1, float ctrlx1, float ctrly1, float ctrlx2, float ctrly2, float x2, float y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.ctrlx1 = ctrlx1;
      this.ctrly1 = ctrly1;
      this.ctrlx2 = ctrlx2;
      this.ctrly2 = ctrly2;
      this.x2 = x2;
      this.y2 = y2;
    }

    public Rectangle2D getBounds2D() {
      float left = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2));
      float top = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2));
      float right = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2));
      float bottom = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2));
      return new Rectangle2D.Float(left, top, right - left, bottom - top);
    }
  }

  public static class Double extends CubicCurve2D implements java.io.Serializable {
    public double x1;
    public double y1;
    public double ctrlx1;
    public double ctrly1;
    public double ctrlx2;
    public double ctrly2;
    public double x2;
    public double y2;

    public Double() { }

    public Double(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2) {
      setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
    }

    public double getX1() { return x1; }

    public double getY1() { return y1; }

    public Point2D getP1() { return new Point2D.Double(x1, y1); }

    public double getCtrlX1() { return ctrlx1; }

    public double getCtrlY1() { return ctrly1; }

    public Point2D getCtrlP1() { return new Point2D.Double(ctrlx1, ctrly1); }

    public double getCtrlX2() { return ctrlx2; }

    public double getCtrlY2() { return ctrly2; }

    public Point2D getCtrlP2() { return new Point2D.Double(ctrlx2, ctrly2); }

    public double getX2() { return x2; }

    public double getY2() { return y2; }

    public Point2D getP2() { return new Point2D.Double(x2, y2); }

    public void setCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2,
        double y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.ctrlx1 = ctrlx1;
      this.ctrly1 = ctrly1;
      this.ctrlx2 = ctrlx2;
      this.ctrly2 = ctrly2;
      this.x2 = x2;
      this.y2 = y2;
    }

    public Rectangle2D getBounds2D() {
      double left = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2));
      double top = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2));
      double right = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2));
      double bottom = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2));
      return new Rectangle2D.Double(left, top, right - left, bottom - top);
    }
  }

  public abstract double getX1();

  public abstract double getY1();

  public abstract Point2D getP1();

  public abstract double getCtrlX1();

  public abstract double getCtrlY1();

  public abstract Point2D getCtrlP1();

  public abstract double getCtrlX2();

  public abstract double getCtrlY2();

  public abstract Point2D getCtrlP2();

  public abstract double getX2();

  public abstract double getY2();

  public abstract Point2D getP2();

  public abstract void setCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2,
      double x2, double y2);

  public void setCurve(double[] coords, int offset) {
    setCurve(coords[offset], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4],
        coords[offset + 5], coords[offset + 6], coords[offset + 7]);
  }

  public void setCurve(Point2D p1, Point2D cp1, Point2D cp2, Point2D p2) {
    setCurve(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), p2.getX(), p2.getY());
  }

  public void setCurve(Point2D[] pts, int offset) {
    setCurve(pts[offset].getX(), pts[offset].getY(), pts[offset + 1].getX(), pts[offset + 1].getY(),
        pts[offset + 2].getX(), pts[offset + 2].getY(), pts[offset + 3].getX(), pts[offset + 3].getY());
  }

  public void setCurve(CubicCurve2D c) {
    setCurve(c.getX1(), c.getY1(), c.getCtrlX1(), c.getCtrlY1(), c.getCtrlX2(), c.getCtrlY2(), c.getX2(), c.getY2());
  }

  public static double getFlatnessSq(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2,
      double x2, double y2) {
    return Math.max(Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx1, ctrly1), Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx2, ctrly2));
  }

  public static double getFlatness(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2,
      double x2, double y2) {
    return Math.sqrt(getFlatnessSq(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2));
  }

  public static double getFlatnessSq(double[] coords, int offset) {
    return getFlatnessSq(coords[offset], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4],
        coords[offset + 5], coords[offset + 6], coords[offset + 7]);
  }

  public static double getFlatness(double[] coords, int offset) {
    return getFlatness(coords[offset], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4],
        coords[offset + 5], coords[offset + 6], coords[offset + 7]);
  }

  public double getFlatnessSq() {
    return getFlatnessSq(getX1(), getY1(), getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(), getX2(), getY2());
  }

  public double getFlatness() {
    return getFlatness(getX1(), getY1(), getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(), getX2(), getY2());
  }

  public void subdivide(CubicCurve2D left, CubicCurve2D right) {
    subdivide(this, left, right);
  }

  public static void subdivide(CubicCurve2D src, CubicCurve2D left, CubicCurve2D right) {
    double[] s = {src.getX1(), src.getY1(), src.getCtrlX1(), src.getCtrlY1(), src.getCtrlX2(), src.getCtrlY2(),
        src.getX2(), src.getY2()};
    double[] l = new double[8];
    double[] r = new double[8];
    subdivide(s, 0, l, 0, r, 0);
    if (left != null) {
      left.setCurve(l, 0);
    }
    if (right != null) {
      right.setCurve(r, 0);
    }
  }

  public static void subdivide(double[] src, int srcoff, double[] left, int leftoff, double[] right, int rightoff) {
    double x1 = src[srcoff];
    double y1 = src[srcoff + 1];
    double ctrlx1 = src[srcoff + 2];
    double ctrly1 = src[srcoff + 3];
    double ctrlx2 = src[srcoff + 4];
    double ctrly2 = src[srcoff + 5];
    double x2 = src[srcoff + 6];
    double y2 = src[srcoff + 7];
    if (left != null) {
      left[leftoff] = x1;
      left[leftoff + 1] = y1;
    }
    if (right != null) {
      right[rightoff + 6] = x2;
      right[rightoff + 7] = y2;
    }
    x1 = (x1 + ctrlx1) / 2.0;
    y1 = (y1 + ctrly1) / 2.0;
    x2 = (x2 + ctrlx2) / 2.0;
    y2 = (y2 + ctrly2) / 2.0;
    double centerx = (ctrlx1 + ctrlx2) / 2.0;
    double centery = (ctrly1 + ctrly2) / 2.0;
    ctrlx1 = (x1 + centerx) / 2.0;
    ctrly1 = (y1 + centery) / 2.0;
    ctrlx2 = (x2 + centerx) / 2.0;
    ctrly2 = (y2 + centery) / 2.0;
    centerx = (ctrlx1 + ctrlx2) / 2.0;
    centery = (ctrly1 + ctrly2) / 2.0;
    if (left != null) {
      left[leftoff + 2] = x1;
      left[leftoff + 3] = y1;
      left[leftoff + 4] = ctrlx1;
      left[leftoff + 5] = ctrly1;
      left[leftoff + 6] = centerx;
      left[leftoff + 7] = centery;
    }
    if (right != null) {
      right[rightoff] = centerx;
      right[rightoff + 1] = centery;
      right[rightoff + 2] = ctrlx2;
      right[rightoff + 3] = ctrly2;
      right[rightoff + 4] = x2;
      right[rightoff + 5] = y2;
    }
  }

  public static int solveCubic(double[] eqn) {
    return solveCubic(eqn, eqn);
  }

  public static int solveCubic(double[] eqn, double[] res) {
    double d = eqn[3];
    if (d == 0.0) {
      return QuadCurve2D.solveQuadratic(eqn, res);
    }
    double a = eqn[2] / d;
    double b = eqn[1] / d;
    double c = eqn[0] / d;
    int roots = 0;
    double q = (a * a - 3.0 * b) / 9.0;
    double r = (2.0 * a * a * a - 9.0 * a * b + 27.0 * c) / 54.0;
    double r2 = r * r;
    double q3 = q * q * q;
    a = a / 3.0;
    if (r2 < q3) {
      double theta = Math.acos(r / Math.sqrt(q3));
      q = -2.0 * Math.sqrt(q);
      res[roots++] = q * Math.cos(theta / 3.0) - a;
      res[roots++] = q * Math.cos((theta + Math.PI * 2.0) / 3.0) - a;
      res[roots++] = q * Math.cos((theta - Math.PI * 2.0) / 3.0) - a;
    } else {
      boolean neg = r < 0.0;
      double s = Math.sqrt(r2 - q3);
      if (neg) {
        r = -r;
      }
      double aa = Math.cbrt(r + s);
      if (!neg) {
        aa = -aa;
      }
      double bb = aa == 0.0 ? 0.0 : q / aa;
      res[roots++] = (aa + bb) - a;
    }
    return roots;
  }

  public boolean contains(double x, double y) {
    return Path2D.contains(getPathIterator(null), x, y);
  }

  public boolean contains(Point2D p) {
    return contains(p.getX(), p.getY());
  }

  public boolean intersects(double x, double y, double w, double h) {
    return w > 0 && h > 0 && Path2D.intersects(getPathIterator(null), x, y, w, h);
  }

  public boolean intersects(Rectangle2D r) {
    return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public boolean contains(double x, double y, double w, double h) {
    return w > 0 && h > 0 && contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(x + w, y + h);
  }

  public boolean contains(Rectangle2D r) {
    return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
  }

  public java.awt.Rectangle getBounds() {
    return getBounds2D().getBounds();
  }

  public Rectangle2D getBounds2D() {
    double left = Math.min(Math.min(getX1(), getX2()), Math.min(getCtrlX1(), getCtrlX2()));
    double top = Math.min(Math.min(getY1(), getY2()), Math.min(getCtrlY1(), getCtrlY2()));
    double right = Math.max(Math.max(getX1(), getX2()), Math.max(getCtrlX1(), getCtrlX2()));
    double bottom = Math.max(Math.max(getY1(), getY2()), Math.max(getCtrlY1(), getCtrlY2()));
    return new Rectangle2D.Double(left, top, right - left, bottom - top);
  }

  public PathIterator getPathIterator(AffineTransform at) {
    Path2D.Double p = new Path2D.Double();
    p.moveTo(getX1(), getY1());
    p.curveTo(getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(), getX2(), getY2());
    return p.getPathIterator(at);
  }

  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return new FlatteningPathIterator(getPathIterator(at), flatness);
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
