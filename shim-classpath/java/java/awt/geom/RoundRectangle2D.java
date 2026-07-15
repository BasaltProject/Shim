/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public abstract class RoundRectangle2D extends RectangularShape {
  protected RoundRectangle2D() { }

  public static class Float extends RoundRectangle2D implements java.io.Serializable {
    public float x;
    public float y;
    public float width;
    public float height;
    public float arcwidth;
    public float archeight;

    public Float() { }

    public Float(float x, float y, float w, float h, float arcw, float arch) {
      setRoundRect(x, y, w, h, arcw, arch);
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getWidth() { return width; }

    public double getHeight() { return height; }

    public double getArcWidth() { return arcwidth; }

    public double getArcHeight() { return archeight; }

    public boolean isEmpty() { return width <= 0.0 || height <= 0.0; }

    public void setRoundRect(double x, double y, double w, double h, double arcw, double arch) {
      this.x = (float) x;
      this.y = (float) y;
      this.width = (float) w;
      this.height = (float) h;
      this.arcwidth = (float) arcw;
      this.archeight = (float) arch;
    }

    public void setRoundRect(float x, float y, float w, float h, float arcw, float arch) {
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
      this.arcwidth = arcw;
      this.archeight = arch;
    }

    public void setRoundRect(RoundRectangle2D rr) {
      this.x = (float) rr.getX();
      this.y = (float) rr.getY();
      this.width = (float) rr.getWidth();
      this.height = (float) rr.getHeight();
      this.arcwidth = (float) rr.getArcWidth();
      this.archeight = (float) rr.getArcHeight();
    }

    public Rectangle2D getBounds2D() { return new Rectangle2D.Float(x, y, width, height); }
  }

  public static class Double extends RoundRectangle2D implements java.io.Serializable {
    public double x;
    public double y;
    public double width;
    public double height;
    public double arcwidth;
    public double archeight;

    public Double() { }

    public Double(double x, double y, double w, double h, double arcw, double arch) {
      setRoundRect(x, y, w, h, arcw, arch);
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getWidth() { return width; }

    public double getHeight() { return height; }

    public double getArcWidth() { return arcwidth; }

    public double getArcHeight() { return archeight; }

    public boolean isEmpty() { return width <= 0.0 || height <= 0.0; }

    public void setRoundRect(double x, double y, double w, double h, double arcw, double arch) {
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
      this.arcwidth = arcw;
      this.archeight = arch;
    }

    public void setRoundRect(RoundRectangle2D rr) {
      this.x = rr.getX();
      this.y = rr.getY();
      this.width = rr.getWidth();
      this.height = rr.getHeight();
      this.arcwidth = rr.getArcWidth();
      this.archeight = rr.getArcHeight();
    }

    public Rectangle2D getBounds2D() { return new Rectangle2D.Double(x, y, width, height); }
  }

  public abstract double getArcWidth();

  public abstract double getArcHeight();

  public abstract void setRoundRect(double x, double y, double w, double h, double arcWidth, double arcHeight);

  public void setRoundRect(RoundRectangle2D rr) {
    setRoundRect(rr.getX(), rr.getY(), rr.getWidth(), rr.getHeight(), rr.getArcWidth(), rr.getArcHeight());
  }

  public void setFrame(double x, double y, double w, double h) {
    setRoundRect(x, y, w, h, getArcWidth(), getArcHeight());
  }

  public boolean contains(double x, double y) {
    if (isEmpty()) {
      return false;
    }
    double rrx0 = getX();
    double rry0 = getY();
    double rrx1 = rrx0 + getWidth();
    double rry1 = rry0 + getHeight();
    if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
      return false;
    }
    double aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0;
    double ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0;
    if (x >= (rrx0 += aw) && x < (rrx0 = rrx1 - aw)) {
      return true;
    }
    if (y >= (rry0 += ah) && y < (rry0 = rry1 - ah)) {
      return true;
    }
    x = (x - rrx0) / aw;
    y = (y - rry0) / ah;
    return x * x + y * y <= 1.0;
  }

  private int classify(double coord, double left, double right, double arcsize) {
    if (coord < left) {
      return 0;
    } else if (coord < left + arcsize) {
      return 1;
    } else if (coord < right - arcsize) {
      return 2;
    } else if (coord < right) {
      return 3;
    }
    return 4;
  }

  public boolean intersects(double x, double y, double w, double h) {
    if (isEmpty() || w <= 0 || h <= 0) {
      return false;
    }
    double rrx0 = getX();
    double rry0 = getY();
    double rrx1 = rrx0 + getWidth();
    double rry1 = rry0 + getHeight();
    if (x + w <= rrx0 || x >= rrx1 || y + h <= rry0 || y >= rry1) {
      return false;
    }
    double aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0;
    double ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0;
    int x0class = classify(x, rrx0, rrx1, aw);
    int x1class = classify(x + w, rrx0, rrx1, aw);
    int y0class = classify(y, rry0, rry1, ah);
    int y1class = classify(y + h, rry0, rry1, ah);
    if (x0class == 2 || x1class == 2 || y0class == 2 || y1class == 2) {
      return true;
    }
    if ((x0class < 2 && x1class > 2) || (y0class < 2 && y1class > 2)) {
      return true;
    }
    x = x1class == 1 ? (x = x + w - (rrx0 + aw)) : (x = x - (rrx1 - aw));
    y = y1class == 1 ? (y = y + h - (rry0 + ah)) : (y = y - (rry1 - ah));
    x = x / aw;
    y = y / ah;
    return x * x + y * y <= 1.0;
  }

  public boolean contains(double x, double y, double w, double h) {
    if (isEmpty() || w <= 0 || h <= 0) {
      return false;
    }
    return contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(x + w, y + h);
  }

  public PathIterator getPathIterator(AffineTransform at) {
    double x = getX();
    double y = getY();
    double w = getWidth();
    double h = getHeight();
    double aw = Math.min(w, Math.abs(getArcWidth())) / 2.0;
    double ah = Math.min(h, Math.abs(getArcHeight())) / 2.0;
    Path2D.Double p = new Path2D.Double();
    if (aw <= 0 || ah <= 0) {
      p.moveTo(x, y);
      p.lineTo(x + w, y);
      p.lineTo(x + w, y + h);
      p.lineTo(x, y + h);
      p.closePath();
      return p.getPathIterator(at);
    }
    double k = 1 - 0.5522847498307933;
    double x1 = x + w;
    double y1 = y + h;
    p.moveTo(x + aw, y);
    p.lineTo(x1 - aw, y);
    p.curveTo(x1 - aw * k, y, x1, y + ah * k, x1, y + ah);
    p.lineTo(x1, y1 - ah);
    p.curveTo(x1, y1 - ah * k, x1 - aw * k, y1, x1 - aw, y1);
    p.lineTo(x + aw, y1);
    p.curveTo(x + aw * k, y1, x, y1 - ah * k, x, y1 - ah);
    p.lineTo(x, y + ah);
    p.curveTo(x, y + ah * k, x + aw * k, y, x + aw, y);
    p.closePath();
    return p.getPathIterator(at);
  }

  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(getX());
    bits += java.lang.Double.doubleToLongBits(getY()) * 37;
    bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
    bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
    bits += java.lang.Double.doubleToLongBits(getArcWidth()) * 53;
    bits += java.lang.Double.doubleToLongBits(getArcHeight()) * 59;
    return (int) bits ^ (int) (bits >> 32);
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    RoundRectangle2D r = null;
    if (((r = obj instanceof RoundRectangle2D ? (RoundRectangle2D) obj : null) != null)) {
      return getX() == r.getX() && getY() == r.getY() && getWidth() == r.getWidth() && getHeight() == r.getHeight()
          && getArcWidth() == r.getArcWidth() && getArcHeight() == r.getArcHeight();
    }
    return false;
  }
}
