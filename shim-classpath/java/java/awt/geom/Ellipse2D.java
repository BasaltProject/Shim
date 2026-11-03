/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public abstract class Ellipse2D extends RectangularShape {
  protected Ellipse2D() { }

  public static class Float extends Ellipse2D implements java.io.Serializable {
    public float x;
    public float y;
    public float width;
    public float height;

    public Float() { }

    public Float(float x, float y, float w, float h) {
      setFrame(x, y, w, h);
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getWidth() { return width; }

    public double getHeight() { return height; }

    public boolean isEmpty() { return width <= 0.0 || height <= 0.0; }

    public void setFrame(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
    }

    public void setFrame(double x, double y, double w, double h) {
      this.x = (float) x;
      this.y = (float) y;
      this.width = (float) w;
      this.height = (float) h;
    }

    public Rectangle2D getBounds2D() { return new Rectangle2D.Float(x, y, width, height); }
  }

  public static class Double extends Ellipse2D implements java.io.Serializable {
    public double x;
    public double y;
    public double width;
    public double height;

    public Double() { }

    public Double(double x, double y, double w, double h) {
      setFrame(x, y, w, h);
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getWidth() { return width; }

    public double getHeight() { return height; }

    public boolean isEmpty() { return width <= 0.0 || height <= 0.0; }

    public void setFrame(double x, double y, double w, double h) {
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
    }

    public Rectangle2D getBounds2D() { return new Rectangle2D.Double(x, y, width, height); }
  }

  public boolean contains(double x, double y) {
    double ew = getWidth();
    if (ew <= 0.0) {
      return false;
    }
    double nx = (x - getX()) / ew - 0.5;
    double eh = getHeight();
    if (eh <= 0.0) {
      return false;
    }
    double ny = (y - getY()) / eh - 0.5;
    return nx * nx + ny * ny < 0.25;
  }

  public boolean intersects(double x, double y, double w, double h) {
    if (w <= 0.0 || h <= 0.0) {
      return false;
    }
    double ew = getWidth();
    if (ew <= 0.0) {
      return false;
    }
    double nx0 = (x - getX()) / ew - 0.5;
    double nx1 = nx0 + w / ew;
    double eh = getHeight();
    if (eh <= 0.0) {
      return false;
    }
    double ny0 = (y - getY()) / eh - 0.5;
    double ny1 = ny0 + h / eh;
    double nearx;
    double neary;
    if (nx0 > 0.0) {
      nearx = nx0;
    } else if (nx1 < 0.0) {
      nearx = nx1;
    } else {
      nearx = 0.0;
    }
    if (ny0 > 0.0) {
      neary = ny0;
    } else if (ny1 < 0.0) {
      neary = ny1;
    } else {
      neary = 0.0;
    }
    return nearx * nearx + neary * neary < 0.25;
  }

  public boolean contains(double x, double y, double w, double h) {
    return contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(x + w, y + h);
  }

  public PathIterator getPathIterator(AffineTransform at) {
    return new ArcIterator(getX(), getY(), getWidth(), getHeight(), at);
  }

  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(getX());
    bits += java.lang.Double.doubleToLongBits(getY()) * 37;
    bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
    bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
    return (int) bits ^ (int) (bits >> 32);
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    Ellipse2D e = null;
    if (((e = obj instanceof Ellipse2D ? (Ellipse2D) obj : null) != null)) {
      return getX() == e.getX() && getY() == e.getY() && getWidth() == e.getWidth() && getHeight() == e.getHeight();
    }
    return false;
  }

  static final class ArcIterator implements PathIterator {
    static final double kCtrl = 0.5522847498307933;
    private final double[] pts;
    private final AffineTransform at;
    private int index;

    ArcIterator(double x, double y, double w, double h, AffineTransform at) {
      this.at = at;
      double r = w / 2;
      double s = h / 2;
      double cx = x + r;
      double cy = y + s;
      double kx = r * kCtrl;
      double ky = s * kCtrl;
      pts = new double[] {
          x + w, cy,
          x + w, cy - ky, cx + kx, y, cx, y,
          cx - kx, y, x, cy - ky, x, cy,
          x, cy + ky, cx - kx, y + h, cx, y + h,
          cx + kx, y + h, x + w, cy + ky, x + w, cy};
      if (w < 0 || h < 0) {
        index = 6;
      }
    }

    public int getWindingRule() { return WIND_NON_ZERO; }

    public boolean isDone() { return index > 5; }

    public void next() { index++; }

    public int currentSegment(float[] coords) {
      if (isDone()) {
        throw new java.util.NoSuchElementException("ellipse iterator out of bounds");
      }
      if (index == 5) {
        return SEG_CLOSE;
      }
      if (index == 0) {
        coords[0] = (float) pts[0];
        coords[1] = (float) pts[1];
        if (at != null) {
          at.transform(coords, 0, coords, 0, 1);
        }
        return SEG_MOVETO;
      }
      int base = 2 + (index - 1) * 6;
      for (int i = 0; i < 6; i++) {
        coords[i] = (float) pts[base + i];
      }
      if (at != null) {
        at.transform(coords, 0, coords, 0, 3);
      }
      return SEG_CUBICTO;
    }

    public int currentSegment(double[] coords) {
      if (isDone()) {
        throw new java.util.NoSuchElementException("ellipse iterator out of bounds");
      }
      if (index == 5) {
        return SEG_CLOSE;
      }
      if (index == 0) {
        coords[0] = pts[0];
        coords[1] = pts[1];
        if (at != null) {
          at.transform(coords, 0, coords, 0, 1);
        }
        return SEG_MOVETO;
      }
      int base = 2 + (index - 1) * 6;
      for (int i = 0; i < 6; i++) {
        coords[i] = pts[base + i];
      }
      if (at != null) {
        at.transform(coords, 0, coords, 0, 3);
      }
      return SEG_CUBICTO;
    }
  }
}
