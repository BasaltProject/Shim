/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public abstract class QuadCurve2D implements java.awt.Shape, Cloneable {
  protected QuadCurve2D() { }

  public static class Float extends QuadCurve2D implements java.io.Serializable {
    public float x1;
    public float y1;
    public float ctrlx;
    public float ctrly;
    public float x2;
    public float y2;

    public Float() { }

    public Float(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
      setCurve(x1, y1, ctrlx, ctrly, x2, y2);
    }

    public double getX1() { return x1; }

    public double getY1() { return y1; }

    public Point2D getP1() { return new Point2D.Float(x1, y1); }

    public double getCtrlX() { return ctrlx; }

    public double getCtrlY() { return ctrly; }

    public Point2D getCtrlPt() { return new Point2D.Float(ctrlx, ctrly); }

    public double getX2() { return x2; }

    public double getY2() { return y2; }

    public Point2D getP2() { return new Point2D.Float(x2, y2); }

    public void setCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
      this.x1 = (float) x1;
      this.y1 = (float) y1;
      this.ctrlx = (float) ctrlx;
      this.ctrly = (float) ctrly;
      this.x2 = (float) x2;
      this.y2 = (float) y2;
    }

    public void setCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.ctrlx = ctrlx;
      this.ctrly = ctrly;
      this.x2 = x2;
      this.y2 = y2;
    }

    public Rectangle2D getBounds2D() {
      float left = Math.min(Math.min(x1, x2), ctrlx);
      float top = Math.min(Math.min(y1, y2), ctrly);
      float right = Math.max(Math.max(x1, x2), ctrlx);
      float bottom = Math.max(Math.max(y1, y2), ctrly);
      return new Rectangle2D.Float(left, top, right - left, bottom - top);
    }
  }

  public static class Double extends QuadCurve2D implements java.io.Serializable {
    public double x1;
    public double y1;
    public double ctrlx;
    public double ctrly;
    public double x2;
    public double y2;

    public Double() { }

    public Double(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
      setCurve(x1, y1, ctrlx, ctrly, x2, y2);
    }

    public double getX1() { return x1; }

    public double getY1() { return y1; }

    public Point2D getP1() { return new Point2D.Double(x1, y1); }

    public double getCtrlX() { return ctrlx; }

    public double getCtrlY() { return ctrly; }

    public Point2D getCtrlPt() { return new Point2D.Double(ctrlx, ctrly); }

    public double getX2() { return x2; }

    public double getY2() { return y2; }

    public Point2D getP2() { return new Point2D.Double(x2, y2); }

    public void setCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.ctrlx = ctrlx;
      this.ctrly = ctrly;
      this.x2 = x2;
      this.y2 = y2;
    }

    public Rectangle2D getBounds2D() {
      double left = Math.min(Math.min(x1, x2), ctrlx);
      double top = Math.min(Math.min(y1, y2), ctrly);
      double right = Math.max(Math.max(x1, x2), ctrlx);
      double bottom = Math.max(Math.max(y1, y2), ctrly);
      return new Rectangle2D.Double(left, top, right - left, bottom - top);
    }
  }

  public abstract double getX1();

  public abstract double getY1();

  public abstract Point2D getP1();

  public abstract double getCtrlX();

  public abstract double getCtrlY();

  public abstract Point2D getCtrlPt();

  public abstract double getX2();

  public abstract double getY2();

  public abstract Point2D getP2();

  public abstract void setCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2);

  public void setCurve(double[] coords, int offset) {
    setCurve(coords[offset], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4], coords[offset + 5]);
  }

  public void setCurve(Point2D p1, Point2D cp, Point2D p2) {
    setCurve(p1.getX(), p1.getY(), cp.getX(), cp.getY(), p2.getX(), p2.getY());
  }

  public void setCurve(Point2D[] pts, int offset) {
    setCurve(pts[offset].getX(), pts[offset].getY(), pts[offset + 1].getX(), pts[offset + 1].getY(),
        pts[offset + 2].getX(), pts[offset + 2].getY());
  }

  public void setCurve(QuadCurve2D c) {
    setCurve(c.getX1(), c.getY1(), c.getCtrlX(), c.getCtrlY(), c.getX2(), c.getY2());
  }

  public static double getFlatnessSq(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
    return Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx, ctrly);
  }

  public static double getFlatness(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
    return Line2D.ptSegDist(x1, y1, x2, y2, ctrlx, ctrly);
  }

  public static double getFlatnessSq(double[] coords, int offset) {
    return Line2D.ptSegDistSq(coords[offset], coords[offset + 1], coords[offset + 4], coords[offset + 5],
        coords[offset + 2], coords[offset + 3]);
  }

  public static double getFlatness(double[] coords, int offset) {
    return Line2D.ptSegDist(coords[offset], coords[offset + 1], coords[offset + 4], coords[offset + 5],
        coords[offset + 2], coords[offset + 3]);
  }

  public double getFlatnessSq() {
    return Line2D.ptSegDistSq(getX1(), getY1(), getX2(), getY2(), getCtrlX(), getCtrlY());
  }

  public double getFlatness() {
    return Line2D.ptSegDist(getX1(), getY1(), getX2(), getY2(), getCtrlX(), getCtrlY());
  }

  public void subdivide(QuadCurve2D left, QuadCurve2D right) {
    subdivide(this, left, right);
  }

  public static void subdivide(QuadCurve2D src, QuadCurve2D left, QuadCurve2D right) {
    double x1 = src.getX1();
    double y1 = src.getY1();
    double ctrlx = src.getCtrlX();
    double ctrly = src.getCtrlY();
    double x2 = src.getX2();
    double y2 = src.getY2();
    double ctrlx1 = (x1 + ctrlx) / 2.0;
    double ctrly1 = (y1 + ctrly) / 2.0;
    double ctrlx2 = (x2 + ctrlx) / 2.0;
    double ctrly2 = (y2 + ctrly) / 2.0;
    ctrlx = (ctrlx1 + ctrlx2) / 2.0;
    ctrly = (ctrly1 + ctrly2) / 2.0;
    if (left != null) {
      left.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx, ctrly);
    }
    if (right != null) {
      right.setCurve(ctrlx, ctrly, ctrlx2, ctrly2, x2, y2);
    }
  }

  public static void subdivide(double[] src, int srcoff, double[] left, int leftoff, double[] right, int rightoff) {
    double x1 = src[srcoff];
    double y1 = src[srcoff + 1];
    double ctrlx = src[srcoff + 2];
    double ctrly = src[srcoff + 3];
    double x2 = src[srcoff + 4];
    double y2 = src[srcoff + 5];
    if (left != null) {
      left[leftoff] = x1;
      left[leftoff + 1] = y1;
    }
    if (right != null) {
      right[rightoff + 4] = x2;
      right[rightoff + 5] = y2;
    }
    x1 = (x1 + ctrlx) / 2.0;
    y1 = (y1 + ctrly) / 2.0;
    x2 = (x2 + ctrlx) / 2.0;
    y2 = (y2 + ctrly) / 2.0;
    ctrlx = (x1 + x2) / 2.0;
    ctrly = (y1 + y2) / 2.0;
    if (left != null) {
      left[leftoff + 2] = x1;
      left[leftoff + 3] = y1;
      left[leftoff + 4] = ctrlx;
      left[leftoff + 5] = ctrly;
    }
    if (right != null) {
      right[rightoff] = ctrlx;
      right[rightoff + 1] = ctrly;
      right[rightoff + 2] = x2;
      right[rightoff + 3] = y2;
    }
  }

  public static int solveQuadratic(double[] eqn) {
    return solveQuadratic(eqn, eqn);
  }

  public static int solveQuadratic(double[] eqn, double[] res) {
    double a = eqn[2];
    double b = eqn[1];
    double c = eqn[0];
    int roots = 0;
    if (a == 0.0) {
      if (b == 0.0) {
        return -1;
      }
      res[roots++] = -c / b;
    } else {
      double d = b * b - 4.0 * a * c;
      if (d < 0.0) {
        return 0;
      }
      d = Math.sqrt(d);
      if (b < 0.0) {
        d = -d;
      }
      double q = (b + d) / -2.0;
      res[roots++] = q / a;
      if (q != 0.0) {
        res[roots++] = c / q;
      }
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
    double left = Math.min(Math.min(getX1(), getX2()), getCtrlX());
    double top = Math.min(Math.min(getY1(), getY2()), getCtrlY());
    double right = Math.max(Math.max(getX1(), getX2()), getCtrlX());
    double bottom = Math.max(Math.max(getY1(), getY2()), getCtrlY());
    return new Rectangle2D.Double(left, top, right - left, bottom - top);
  }

  public PathIterator getPathIterator(AffineTransform at) {
    Path2D.Double p = new Path2D.Double();
    p.moveTo(getX1(), getY1());
    p.quadTo(getCtrlX(), getCtrlY(), getX2(), getY2());
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
