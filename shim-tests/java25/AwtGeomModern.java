/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class AwtGeomModern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  static String path(Shape s, AffineTransform at) {
    StringBuilder sb = new StringBuilder();
    PathIterator it = s.getPathIterator(at);
    sb.append("rule=").append(it.getWindingRule());
    double[] c = new double[6];
    while (!it.isDone()) {
      int type = it.currentSegment(c);
      sb.append(" ").append(type);
      if (type != PathIterator.SEG_CLOSE) {
        sb.append("(").append(c[0]).append(",").append(c[1]).append(")");
      }
      it.next();
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    int max = Integer.MAX_VALUE;
    int min = Integer.MIN_VALUE;
    t("rect-ctor", () -> new Rectangle() + " " + new Rectangle(1, 2, 3, 4) + " " + new Rectangle(5, 6) + " "
        + new Rectangle(new Point(1, 2), new Dimension(3, 4)) + " " + new Rectangle(new Point(7, 8)) + " "
        + new Rectangle(new Dimension(9, 10)) + " " + new Rectangle(new Rectangle(1, 1, 1, 1)));
    t("rect-getters", () -> {
      Rectangle r = new Rectangle(1, 2, 3, 4);
      return r.getX() + " " + r.getY() + " " + r.getWidth() + " " + r.getHeight() + " " + r.getMinX() + " "
          + r.getMaxX() + " " + r.getCenterX() + " " + r.getCenterY() + " " + r.getLocation() + " " + r.getSize()
          + " " + r.getBounds() + " " + r.getBounds2D() + " " + r.getFrame() + " " + r.isEmpty();
    });
    t("rect-setrect", () -> {
      StringBuilder sb = new StringBuilder();
      double[][] in = {{1.5, 2.5, 3.2, 4.7}, {-1.5, -2.5, 3.2, 4.7}, {0, 0, -1, -1}, {1e12, 1e12, 5, 5},
          {-1e12, -1e12, 5, 5}, {3e9, 3e9, 5, 5}, {Double.NaN, 1, 1, 1}, {1, 1, Double.POSITIVE_INFINITY, 1},
          {0.5, 0.5, 0.5, 0.5}, {-0.5, -0.5, 1.0, 1.0}};
      for (double[] d : in) {
        Rectangle r = new Rectangle();
        r.setRect(d[0], d[1], d[2], d[3]);
        sb.append(r).append(' ');
      }
      return sb;
    });
    t("rect-translate", () -> {
      StringBuilder sb = new StringBuilder();
      Rectangle a = new Rectangle(max - 5, min + 5, 20, 20);
      a.translate(10, -10);
      sb.append(a).append(' ');
      Rectangle b = new Rectangle(min + 5, max - 5, 20, 20);
      b.translate(-10, 10);
      sb.append(b).append(' ');
      Rectangle c = new Rectangle(1, 1, 2, 2);
      c.translate(3, 4);
      sb.append(c);
      return sb;
    });
    t("rect-contains", () -> {
      Rectangle r = new Rectangle(10, 10, 20, 20);
      return r.contains(10, 10) + " " + r.contains(29, 29) + " " + r.contains(30, 30) + " " + r.contains(9, 10) + " "
          + r.contains(new Point(15, 15)) + " " + r.contains(new Rectangle(12, 12, 5, 5)) + " "
          + r.contains(new Rectangle(12, 12, 50, 5)) + " " + r.contains(new Rectangle(10, 10, 20, 20)) + " "
          + r.contains(new Rectangle(12, 12, 0, 0)) + " " + r.contains(new Rectangle(0, 0, -1, 5)) + " "
          + new Rectangle(0, 0, -1, 5).contains(0, 0) + " " + new Rectangle(min, min, max, max).contains(0, 0) + " "
          + new Rectangle(max - 1, 0, 5, 5).contains(max - 1, 0) + " " + r.contains(15.5, 15.5) + " "
          + r.contains(new Point2D.Double(30, 30)) + " " + r.contains(new Rectangle2D.Double(11, 11, 1.5, 1.5)) + " "
          + r.contains(10.0, 10.0, 20.0, 20.0);
    });
    t("rect-intersects", () -> {
      Rectangle r = new Rectangle(10, 10, 20, 20);
      return r.intersects(new Rectangle(25, 25, 10, 10)) + " " + r.intersects(new Rectangle(30, 30, 10, 10)) + " "
          + r.intersects(new Rectangle(0, 0, 10, 10)) + " " + r.intersects(new Rectangle(0, 0, 11, 11)) + " "
          + r.intersects(new Rectangle(15, 15, 0, 0)) + " " + r.intersects(new Rectangle(min, min, max, max)) + " "
          + new Rectangle(max - 5, max - 5, 10, 10).intersects(new Rectangle(max - 3, max - 3, 2, 2)) + " "
          + r.intersects(25.5, 25.5, 2, 2) + " " + r.intersects(new Rectangle2D.Double(29.5, 29.5, 1, 1));
    });
    t("rect-intersection", () -> {
      Rectangle r = new Rectangle(10, 10, 20, 20);
      return r.intersection(new Rectangle(25, 25, 10, 10)) + " " + r.intersection(new Rectangle(40, 40, 10, 10)) + " "
          + r.intersection(new Rectangle(min, min, max, max)) + " " + r.intersection(new Rectangle(0, 0, 5, 5)) + " "
          + r.createIntersection(new Rectangle2D.Double(25, 25, 10.5, 10.5)) + " "
          + r.createIntersection(new Rectangle2D.Float(25, 25, 10.5f, 10.5f)) + " "
          + r.createIntersection(new Rectangle(25, 25, 10, 10)).getClass().getName();
    });
    t("rect-union", () -> {
      Rectangle r = new Rectangle(10, 10, 20, 20);
      return r.union(new Rectangle(25, 25, 10, 10)) + " " + r.union(new Rectangle(-5, -5, 1, 1)) + " "
          + r.union(new Rectangle(0, 0, -1, -1)) + " " + new Rectangle(0, 0, -1, -1).union(r) + " "
          + r.union(new Rectangle(max - 10, max - 10, 10, 10)) + " " + r.union(new Rectangle(min, min, max, max)) + " "
          + r.createUnion(new Rectangle2D.Double(25, 25, 10.5, 10.5)) + " "
          + r.createUnion(new Rectangle2D.Float(25, 25, 10.5f, 10.5f));
    });
    t("rect-add", () -> {
      StringBuilder sb = new StringBuilder();
      Rectangle a = new Rectangle(10, 10, 5, 5);
      a.add(20, 3);
      sb.append(a).append(' ');
      Rectangle b = new Rectangle(0, 0, -1, -1);
      b.add(7, 8);
      sb.append(b).append(' ');
      Rectangle c = new Rectangle(10, 10, 5, 5);
      c.add(new Point(0, 30));
      sb.append(c).append(' ');
      Rectangle d = new Rectangle(10, 10, 5, 5);
      d.add(new Rectangle(0, 0, 3, 3));
      sb.append(d).append(' ');
      Rectangle e = new Rectangle(0, 0, -1, -1);
      e.add(new Rectangle(4, 4, 2, 2));
      sb.append(e).append(' ');
      Rectangle f = new Rectangle(1, 1, 2, 2);
      f.add(new Rectangle(0, 0, -1, -1));
      sb.append(f).append(' ');
      Rectangle g = new Rectangle(max - 3, 0, 2, 2);
      g.add(max, 5);
      sb.append(g).append(' ');
      Rectangle h = new Rectangle(1, 1, 2, 2);
      h.add(new Rectangle2D.Double(0.5, 0.5, 5, 5));
      sb.append(h).append(' ');
      Rectangle i = new Rectangle(1, 1, 2, 2);
      i.add(new Point2D.Double(10.5, -4.5));
      sb.append(i);
      return sb;
    });
    t("rect-grow", () -> {
      StringBuilder sb = new StringBuilder();
      Rectangle a = new Rectangle(10, 10, 20, 20);
      a.grow(5, 3);
      sb.append(a).append(' ');
      Rectangle b = new Rectangle(10, 10, 20, 20);
      b.grow(-15, -15);
      sb.append(b).append(' ');
      Rectangle c = new Rectangle(min + 2, 0, 10, 10);
      c.grow(10, 0);
      sb.append(c).append(' ');
      Rectangle d = new Rectangle(max - 2, 0, 1, 10);
      d.grow(10, 0);
      sb.append(d).append(' ');
      Rectangle e = new Rectangle(0, 0, 4, 4);
      e.grow(-10, 5);
      sb.append(e);
      return sb;
    });
    t("rect-outcode", () -> {
      Rectangle r = new Rectangle(10, 10, 20, 20);
      StringBuilder sb = new StringBuilder();
      double[][] pts = {{15, 15}, {5, 15}, {35, 15}, {15, 5}, {15, 35}, {5, 5}, {35, 35}, {10, 10}, {30, 30}};
      for (double[] p : pts) {
        sb.append(r.outcode(p[0], p[1])).append(' ');
      }
      sb.append(new Rectangle(0, 0, 0, 0).outcode(0, 0)).append(' ').append(r.outcode(new Point2D.Double(35, 5)));
      return sb;
    });
    t("rect-equals-hash", () -> {
      Rectangle a = new Rectangle(1, 2, 3, 4);
      Rectangle2D.Double b = new Rectangle2D.Double(1, 2, 3, 4);
      Rectangle2D.Float c = new Rectangle2D.Float(1, 2, 3, 4);
      return a.equals(new Rectangle(1, 2, 3, 4)) + " " + a.equals(b) + " " + b.equals(a) + " " + c.equals(a) + " "
          + (a.hashCode() == b.hashCode()) + " " + (b.hashCode() == c.hashCode()) + " " + a.hashCode() + " "
          + a.equals(null) + " " + a.equals("x") + " " + a.equals(new Rectangle(1, 2, 3, 5));
    });
    t("rect-setters", () -> {
      Rectangle r = new Rectangle();
      r.setBounds(1, 2, 3, 4);
      Rectangle s = new Rectangle();
      s.setBounds(r);
      s.setLocation(9, 8);
      s.setLocation(new Point(7, 6));
      s.setSize(5, 4);
      s.setSize(new Dimension(11, 12));
      Rectangle u = new Rectangle(1, 1, 1, 1);
      u.setFrame(2.5, 3.5, 4.5, 5.5);
      Rectangle v = new Rectangle(1, 1, 1, 1);
      v.setFrameFromDiagonal(10, 10, 4, 6);
      Rectangle w = new Rectangle(1, 1, 1, 1);
      w.setFrameFromCenter(10, 10, 12, 13);
      Rectangle x = new Rectangle();
      x.setFrame(new Point2D.Double(1, 2), new Dimension(3, 4));
      Rectangle y = new Rectangle();
      y.setFrame(new Rectangle2D.Double(1.2, 2.2, 3.2, 4.2));
      return r + " " + s + " " + u + " " + v + " " + w + " " + x + " " + y;
    });
    t("rect-isempty", () -> new Rectangle(0, 0, 0, 5).isEmpty() + " " + new Rectangle(0, 0, 5, -1).isEmpty() + " "
        + new Rectangle(0, 0, 1, 1).isEmpty());
    t("rect-path", () -> path(new Rectangle(1, 2, 3, 4), null) + " | " + path(new Rectangle(1, 2, 3, 4),
        AffineTransform.getScaleInstance(2, 3)) + " | " + path(new Rectangle(0, 0, -1, 5), null));
    t("rect-path-flat", () -> {
      PathIterator it = new Rectangle(1, 2, 3, 4).getPathIterator(null, 0.5);
      int n = 0;
      while (!it.isDone()) {
        n++;
        it.next();
      }
      return n;
    });
    t("rect-path-oob", () -> {
      PathIterator it = new Rectangle(1, 2, 3, 4).getPathIterator(null);
      for (int i = 0; i < 6; i++) {
        it.next();
      }
      return it.currentSegment(new float[6]);
    });
    t("rect-path-float", () -> {
      PathIterator it = new Rectangle2D.Double(0.1, 0.2, 0.3, 0.4).getPathIterator(null);
      float[] f = new float[6];
      StringBuilder sb = new StringBuilder();
      while (!it.isDone()) {
        int ty = it.currentSegment(f);
        sb.append(ty).append(Arrays.toString(Arrays.copyOf(f, 2))).append(' ');
        it.next();
      }
      return sb;
    });
    t("rect-bounds-float", () -> new Rectangle2D.Double(0.5, 0.5, 1.2, 1.2).getBounds() + " "
        + new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0).getBounds() + " " + new Rectangle2D.Double(0, 0, -1, 5)
        .getBounds() + " " + new Rectangle2D.Float(1.5f, 2.5f, 3.5f, 4.5f).getBounds2D());

    t("r2d-tostring", () -> new Rectangle2D.Double(1, 2, 3, 4) + " " + new Rectangle2D.Float(1.5f, 2, 3, 4));
    t("r2d-set", () -> {
      Rectangle2D.Float f = new Rectangle2D.Float();
      f.setRect(0.1, 0.2, 0.3, 0.4);
      Rectangle2D.Double d = new Rectangle2D.Double();
      d.setRect(f);
      Rectangle2D.Double e = new Rectangle2D.Double();
      e.setFrame(1, 2, 3, 4);
      return f + " " + d + " " + e + " " + f.isEmpty() + " " + new Rectangle2D.Double(0, 0, 0, 1).isEmpty();
    });
    t("r2d-contains", () -> {
      Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 10);
      return r.contains(0, 0) + " " + r.contains(10, 10) + " " + r.contains(9.999, 9.999) + " "
          + r.contains(1, 1, 8, 8) + " " + r.contains(1, 1, 9, 9) + " " + r.contains(1, 1, 9.0001, 9.0001) + " "
          + r.contains(-1, -1, 12, 12) + " " + r.intersects(10, 10, 1, 1) + " " + r.intersects(9, 9, 2, 2) + " "
          + r.intersects(5, 5, 0, 0) + " " + new Rectangle2D.Double(0, 0, 0, 0).contains(0, 0);
    });
    t("r2d-intersect-union", () -> {
      Rectangle2D a = new Rectangle2D.Double(0, 0, 10, 10);
      Rectangle2D b = new Rectangle2D.Double(5, 5, 10, 10);
      Rectangle2D c = new Rectangle2D.Double();
      Rectangle2D.intersect(a, b, c);
      Rectangle2D d = new Rectangle2D.Double();
      Rectangle2D.union(a, b, d);
      return c + " " + d + " " + a.createIntersection(b) + " " + a.createUnion(b) + " "
          + a.createIntersection(new Rectangle2D.Double(20, 20, 1, 1)) + " "
          + new Rectangle2D.Float(0, 0, 1, 1).createUnion(new Rectangle2D.Float(2, 2, 1, 1)).getClass().getName()
          + " " + new Rectangle2D.Float(0, 0, 1, 1).createUnion(new Rectangle2D.Double(2, 2, 1, 1)).getClass()
          .getName();
    });
    t("r2d-add", () -> {
      Rectangle2D.Double a = new Rectangle2D.Double(0, 0, 1, 1);
      a.add(5, -2);
      Rectangle2D.Double b = new Rectangle2D.Double(0, 0, 1, 1);
      b.add(new Point2D.Double(-1, 3));
      Rectangle2D.Double c = new Rectangle2D.Double(0, 0, 1, 1);
      c.add(new Rectangle2D.Double(3, 3, 2, 2));
      return a + " " + b + " " + c;
    });
    t("r2d-frames", () -> {
      Rectangle2D.Double a = new Rectangle2D.Double();
      a.setFrameFromCenter(5, 5, 8, 9);
      Rectangle2D.Double b = new Rectangle2D.Double();
      b.setFrameFromDiagonal(new Point2D.Double(9, 9), new Point2D.Double(3, 4));
      Rectangle2D.Double c = new Rectangle2D.Double();
      c.setFrameFromCenter(new Point2D.Double(0, 0), new Point2D.Double(-2, -3));
      return a + " " + b + " " + c + " " + a.getCenterX() + " " + a.getCenterY() + " " + a.getMaxX() + " "
          + a.getMaxY();
    });
    t("r2d-lines", () -> {
      Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 10);
      return r.intersectsLine(-5, 5, 15, 5) + " " + r.intersectsLine(-5, -5, -1, -1) + " " + r.intersectsLine(-5, 15,
          15, -5) + " " + r.intersectsLine(5, 5, 6, 6) + " " + r.intersectsLine(-5, 20, 20, 20) + " "
          + r.intersectsLine(new Line2D.Double(-5, 5, 15, 5));
    });
    t("r2d-clone", () -> {
      Rectangle2D.Double r = new Rectangle2D.Double(1, 2, 3, 4);
      Object c = r.clone();
      Rectangle2D b = r.getBounds2D();
      return c + " " + (c != r) + " " + c.equals(r) + " " + b + " " + (b != r);
    });

    t("line", () -> {
      Line2D.Double l = new Line2D.Double(1, 2, 3, 4);
      Line2D.Float f = new Line2D.Float(5, 6, 1, 0);
      return l.getP1() + " " + l.getP2() + " " + f.getP1() + " " + f.getP2() + " " + l.getBounds2D() + " "
          + f.getBounds2D() + " " + l.getBounds() + " " + path(l, null) + " " + path(f, AffineTransform
          .getTranslateInstance(1, 1));
    });
    t("line-ccw", () -> Line2D.relativeCCW(0, 0, 10, 0, 5, 5) + " " + Line2D.relativeCCW(0, 0, 10, 0, 5, -5) + " "
        + Line2D.relativeCCW(0, 0, 10, 0, 5, 0) + " " + Line2D.relativeCCW(0, 0, 10, 0, 15, 0) + " "
        + Line2D.relativeCCW(0, 0, 10, 0, -5, 0) + " " + new Line2D.Double(0, 0, 10, 0).relativeCCW(new Point2D
        .Double(5, 5)));
    t("line-intersect", () -> Line2D.linesIntersect(0, 0, 10, 10, 0, 10, 10, 0) + " " + Line2D.linesIntersect(0, 0, 1,
        1, 2, 2, 3, 3) + " " + Line2D.linesIntersect(0, 0, 10, 0, 0, 1, 10, 1) + " " + new Line2D.Double(0, 0, 10,
        10).intersectsLine(0, 10, 10, 0) + " " + new Line2D.Double(0, 0, 10, 10).intersects(new Rectangle2D.Double(4,
        4, 1, 1)) + " " + new Line2D.Double(0, 0, 10, 10).intersects(20, 20, 1, 1));
    t("line-dist", () -> Line2D.ptSegDist(0, 0, 10, 0, 5, 5) + " " + Line2D.ptSegDist(0, 0, 10, 0, -3, 4) + " "
        + Line2D.ptSegDist(0, 0, 10, 0, 13, 4) + " " + Line2D.ptLineDist(0, 0, 10, 0, 13, 4) + " "
        + Line2D.ptSegDistSq(0, 0, 10, 10, 0, 10) + " " + Line2D.ptLineDistSq(0, 0, 10, 10, 0, 10) + " "
        + new Line2D.Double(0, 0, 10, 0).ptSegDist(3, 4) + " " + new Line2D.Double(0, 0, 10, 0).ptLineDist(new Point2D
        .Double(3, 4)) + " " + new Line2D.Double(0, 0, 0, 0).ptSegDist(3, 4));
    t("line-set", () -> {
      Line2D.Double l = new Line2D.Double();
      l.setLine(new Point2D.Double(1, 2), new Point2D.Double(3, 4));
      Line2D.Float f = new Line2D.Float();
      f.setLine(l);
      return l.getX1() + " " + l.getY2() + " " + f.getX2() + " " + f.getY1() + " " + l.contains(1, 2) + " "
          + l.contains(new Rectangle2D.Double(0, 0, 9, 9));
    });

    t("at-identity", () -> {
      AffineTransform at = new AffineTransform();
      return at + " " + at.getType() + " " + at.isIdentity() + " " + at.getDeterminant() + " " + at.hashCode();
    });
    t("at-instances", () -> {
      StringBuilder sb = new StringBuilder();
      AffineTransform[] a = {AffineTransform.getTranslateInstance(3, 4), AffineTransform.getScaleInstance(2, 3),
          AffineTransform.getShearInstance(0.5, 0.25), AffineTransform.getRotateInstance(Math.PI / 2),
          AffineTransform.getRotateInstance(Math.PI), AffineTransform.getRotateInstance(-Math.PI / 2),
          AffineTransform.getRotateInstance(0), AffineTransform.getRotateInstance(Math.PI / 6),
          AffineTransform.getRotateInstance(Math.PI / 4, 10, 20), AffineTransform.getRotateInstance(0, 1),
          AffineTransform.getRotateInstance(-1, 0), AffineTransform.getRotateInstance(3, 4),
          AffineTransform.getRotateInstance(1, 0, 5, 5), AffineTransform.getQuadrantRotateInstance(1),
          AffineTransform.getQuadrantRotateInstance(2), AffineTransform.getQuadrantRotateInstance(3),
          AffineTransform.getQuadrantRotateInstance(4), AffineTransform.getQuadrantRotateInstance(-1),
          AffineTransform.getQuadrantRotateInstance(1, 10, 20), AffineTransform.getQuadrantRotateInstance(2, 10, 20),
          AffineTransform.getQuadrantRotateInstance(3, 10, 20), AffineTransform.getQuadrantRotateInstance(0, 10, 20),
          AffineTransform.getScaleInstance(-1, 1), AffineTransform.getScaleInstance(-2, -2),
          AffineTransform.getScaleInstance(-1, -1), AffineTransform.getScaleInstance(1, -1),
          AffineTransform.getScaleInstance(0, 5)};
      for (AffineTransform x : a) {
        sb.append(x).append(" t=").append(x.getType()).append('\n');
      }
      return "\n" + sb;
    });
    t("at-ctors", () -> {
      AffineTransform a = new AffineTransform(1f, 2f, 3f, 4f, 5f, 6f);
      AffineTransform b = new AffineTransform(new double[] {1, 2, 3, 4});
      AffineTransform c = new AffineTransform(new float[] {1, 2, 3, 4, 5, 6});
      AffineTransform d = new AffineTransform(a);
      double[] m = new double[6];
      a.getMatrix(m);
      return a + " " + b + " " + c + " " + d + " " + a.equals(d) + " " + a.equals(b) + " " + (a.hashCode() == d
          .hashCode()) + " " + Arrays.toString(m) + " " + a.getScaleX() + a.getScaleY() + a.getShearX()
          + a.getShearY() + a.getTranslateX() + a.getTranslateY();
    });
    t("at-ctor-short", () -> new AffineTransform(new double[] {1, 2, 3}));
    t("at-types", () -> {
      StringBuilder sb = new StringBuilder();
      double[][] ms = {{1, 0, 0, 1, 0, 0}, {1, 0, 0, 1, 5, 5}, {2, 0, 0, 2, 0, 0}, {2, 0, 0, 3, 0, 0},
          {-1, 0, 0, 1, 0, 0}, {0, 1, -1, 0, 0, 0}, {0, 1, 1, 0, 0, 0}, {0, 2, -2, 0, 0, 0}, {0, 2, -3, 0, 0, 0},
          {1, 1, 0, 1, 0, 0}, {0.6, 0.8, -0.8, 0.6, 1, 1}, {0.6, 0.8, 0.8, -0.6, 0, 0}, {1.2, 0.8, -0.8, 1.2, 0, 0},
          {2, 0, 0, -2, 0, 0}, {-1, 0, 0, -1, 0, 0}, {-2, 0, 0, -2, 0, 0}, {-1, 0, 0, -2, 0, 0}, {0, 0, 0, 0, 0, 0}};
      for (double[] m : ms) {
        AffineTransform a = new AffineTransform(m);
        sb.append(a.getType()).append('/').append(a.isIdentity()).append(' ');
      }
      return sb;
    });
    t("at-translate-scale", () -> {
      AffineTransform a = new AffineTransform();
      a.translate(3, 4);
      a.scale(2, 3);
      a.translate(1, 1);
      AffineTransform b = new AffineTransform();
      b.scale(2, 3);
      b.translate(3, 4);
      return a + " " + b;
    });
    t("at-rotate", () -> {
      StringBuilder sb = new StringBuilder();
      double[] thetas = {Math.PI / 2, Math.PI, -Math.PI / 2, 0, 1.0, 2 * Math.PI, 3 * Math.PI / 2, Math.PI / 3};
      for (double th : thetas) {
        AffineTransform a = new AffineTransform();
        a.rotate(th);
        AffineTransform b = AffineTransform.getTranslateInstance(5, 6);
        b.rotate(th);
        AffineTransform c = AffineTransform.getScaleInstance(2, 3);
        c.rotate(th);
        sb.append(a).append(b).append(c).append('\n');
      }
      return "\n" + sb;
    });
    t("at-rotate-vec", () -> {
      StringBuilder sb = new StringBuilder();
      double[][] vs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {3, 4}, {0, 0}, {-3, 4}};
      for (double[] v : vs) {
        AffineTransform a = AffineTransform.getTranslateInstance(1, 2);
        a.rotate(v[0], v[1]);
        AffineTransform b = new AffineTransform();
        b.rotate(v[0], v[1], 7, 8);
        sb.append(a).append(b).append('\n');
      }
      return "\n" + sb;
    });
    t("at-quadrant", () -> {
      StringBuilder sb = new StringBuilder();
      for (int q = -1; q <= 4; q++) {
        AffineTransform a = AffineTransform.getTranslateInstance(5, 6);
        a.scale(2, 3);
        a.quadrantRotate(q);
        AffineTransform b = AffineTransform.getTranslateInstance(5, 6);
        b.quadrantRotate(q, 10, 20);
        sb.append(a).append(b).append('\n');
      }
      return "\n" + sb;
    });
    t("at-shear-concat", () -> {
      AffineTransform a = new AffineTransform();
      a.shear(0.5, 0.25);
      AffineTransform b = AffineTransform.getTranslateInstance(1, 2);
      b.concatenate(a);
      AffineTransform c = AffineTransform.getScaleInstance(2, 3);
      c.preConcatenate(AffineTransform.getTranslateInstance(7, 8));
      AffineTransform d = AffineTransform.getRotateInstance(0.3);
      d.concatenate(AffineTransform.getScaleInstance(2, 2));
      AffineTransform e = AffineTransform.getRotateInstance(0.3);
      e.preConcatenate(AffineTransform.getScaleInstance(2, 2));
      return a + " " + b + " " + c + " " + d + " " + e;
    });
    t("at-settransform", () -> {
      AffineTransform a = new AffineTransform();
      a.setTransform(1, 2, 3, 4, 5, 6);
      AffineTransform b = new AffineTransform();
      b.setTransform(a);
      b.setToIdentity();
      AffineTransform c = new AffineTransform(a);
      c.setToTranslation(1, 2);
      AffineTransform d = new AffineTransform(a);
      d.setToScale(2, 3);
      AffineTransform e = new AffineTransform(a);
      e.setToShear(1, 2);
      AffineTransform f = new AffineTransform(a);
      f.setToRotation(Math.PI / 2, 1, 1);
      AffineTransform g = new AffineTransform(a);
      g.setToRotation(1, 1, 2, 2);
      AffineTransform h = new AffineTransform(a);
      h.setToQuadrantRotation(3, 1, 2);
      return a + " " + b + " " + c + " " + d + " " + e + " " + f + " " + g + " " + h;
    });
    t("at-invert", () -> {
      StringBuilder sb = new StringBuilder();
      AffineTransform[] as = {AffineTransform.getTranslateInstance(3, 4), AffineTransform.getScaleInstance(2, 4),
          AffineTransform.getScaleInstance(3, 7), AffineTransform.getRotateInstance(0.5),
          AffineTransform.getQuadrantRotateInstance(1), new AffineTransform(1, 2, 3, 4, 5, 6),
          new AffineTransform(0, 2, 4, 0, 6, 7), new AffineTransform(2, 0, 0, 3, 5, 7), new AffineTransform()};
      for (AffineTransform a : as) {
        try {
          sb.append(a.createInverse()).append('\n');
        } catch (Exception e) {
          sb.append(e).append('\n');
        }
      }
      return "\n" + sb;
    });
    t("at-invert-singular", () -> {
      StringBuilder sb = new StringBuilder();
      AffineTransform[] as = {new AffineTransform(0, 0, 0, 0, 0, 0), new AffineTransform(1, 2, 2, 4, 0, 0),
          new AffineTransform(0, 5, 0, 0, 0, 0), new AffineTransform(2, 0, 0, 0, 0, 0)};
      for (AffineTransform a : as) {
        try {
          sb.append(a.createInverse()).append('\n');
        } catch (Exception e) {
          sb.append(e).append('\n');
        }
      }
      return "\n" + sb;
    });
    t("at-transform-point", () -> {
      AffineTransform a = new AffineTransform(2, 1, -1, 3, 10, 20);
      Point2D p = a.transform(new Point2D.Double(1, 2), null);
      Point2D q = a.transform(new Point2D.Float(1, 2), null);
      Point2D r = a.transform(new Point(1, 2), null);
      Point2D s = a.deltaTransform(new Point2D.Double(1, 2), null);
      Point2D u = a.inverseTransform(new Point2D.Double(10, 20), null);
      return p + " " + q + " " + r + " " + s + " " + u;
    });
    t("at-transform-point-all-states", () -> {
      StringBuilder sb = new StringBuilder();
      AffineTransform[] as = {new AffineTransform(), AffineTransform.getTranslateInstance(3, 4),
          AffineTransform.getScaleInstance(2, 3), new AffineTransform(2, 0, 0, 3, 5, 6),
          AffineTransform.getQuadrantRotateInstance(1), new AffineTransform(0, 2, 3, 0, 5, 6),
          new AffineTransform(1, 2, 3, 4, 0, 0), new AffineTransform(1, 2, 3, 4, 5, 6)};
      for (AffineTransform a : as) {
        sb.append(a.transform(new Point2D.Double(7, 8), null)).append(a.deltaTransform(new Point2D.Double(7, 8),
            null));
        try {
          sb.append(a.inverseTransform(new Point2D.Double(7, 8), null));
        } catch (Exception e) {
          sb.append(e.getMessage());
        }
        sb.append('\n');
      }
      return "\n" + sb;
    });
    t("at-inverse-singular", () -> new AffineTransform(0, 0, 0, 0, 0, 0).inverseTransform(new Point2D.Double(1, 1),
        null));
    t("at-transform-arrays", () -> {
      AffineTransform a = new AffineTransform(2, 1, -1, 3, 10, 20);
      double[] d = {1, 2, 3, 4, 5, 6};
      double[] o = new double[8];
      a.transform(d, 0, o, 2, 3);
      float[] f = {1, 2, 3, 4};
      float[] g = new float[4];
      a.transform(f, 0, g, 0, 2);
      double[] h = new double[4];
      a.transform(f, 0, h, 0, 2);
      float[] i = new float[4];
      a.transform(d, 0, i, 0, 2);
      Point2D[] src = {new Point2D.Double(1, 2), new Point2D.Float(3, 4)};
      Point2D[] dst = new Point2D[2];
      a.transform(src, 0, dst, 0, 2);
      double[] ov = {1, 2, 3, 4, 5, 6};
      a.transform(ov, 0, ov, 2, 2);
      return Arrays.toString(o) + Arrays.toString(g) + Arrays.toString(h) + Arrays.toString(i) + Arrays.toString(dst)
          + Arrays.toString(ov);
    });
    t("at-equals-hash-clone", () -> {
      AffineTransform a = AffineTransform.getRotateInstance(0.5, 3, 4);
      Object c = a.clone();
      return c.equals(a) + " " + (c != a) + " " + (c.hashCode() == a.hashCode()) + " " + a.hashCode() + " "
          + a.equals(null) + " " + a.equals("x");
    });
    t("at-roundstring", () -> new AffineTransform(0.1 + 0.2, 1e-20, 1e20, 123456789.123456789, -0.0, 0.0));
  }
}
