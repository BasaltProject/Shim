/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

import java.util.NoSuchElementException;

public class FlatteningPathIterator implements PathIterator {
  private final PathIterator src;
  private final double flatness;
  private final int limit;
  private final double[] segs = new double[6];
  private final java.util.ArrayList<double[]> queue = new java.util.ArrayList<double[]>();
  private double curX;
  private double curY;
  private double moveX;
  private double moveY;
  private int pos;
  private boolean primed;

  public FlatteningPathIterator(PathIterator src, double flatness) {
    this(src, flatness, 10);
  }

  public FlatteningPathIterator(PathIterator src, double flatness, int limit) {
    if (flatness < 0.0) {
      throw new IllegalArgumentException("flatness must be >= 0");
    }
    if (limit < 0) {
      throw new IllegalArgumentException("limit must be >= 0");
    }
    this.src = src;
    this.flatness = flatness;
    this.limit = limit;
    advance();
  }

  public double getFlatness() {
    return flatness;
  }

  public int getRecursionLimit() {
    return limit;
  }

  @Override
  public int getWindingRule() {
    return src.getWindingRule();
  }

  private void emit(int type, double x, double y) {
    queue.add(new double[] {type, x, y});
  }

  private void flattenQuad(double x0, double y0, double x1, double y1, double x2, double y2, int level) {
    double dx = x1 - (x0 + x2) / 2;
    double dy = y1 - (y0 + y2) / 2;
    if (level >= limit || dx * dx + dy * dy <= flatness * flatness) {
      emit(SEG_LINETO, x2, y2);
      return;
    }
    double ax = (x0 + x1) / 2;
    double ay = (y0 + y1) / 2;
    double bx = (x1 + x2) / 2;
    double by = (y1 + y2) / 2;
    double mx = (ax + bx) / 2;
    double my = (ay + by) / 2;
    flattenQuad(x0, y0, ax, ay, mx, my, level + 1);
    flattenQuad(mx, my, bx, by, x2, y2, level + 1);
  }

  private void flattenCubic(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
      int level) {
    double d1 = distSq(x1, y1, x0, y0, x3, y3);
    double d2 = distSq(x2, y2, x0, y0, x3, y3);
    if (level >= limit || Math.max(d1, d2) <= flatness * flatness) {
      emit(SEG_LINETO, x3, y3);
      return;
    }
    double ax = (x0 + x1) / 2;
    double ay = (y0 + y1) / 2;
    double bx = (x1 + x2) / 2;
    double by = (y1 + y2) / 2;
    double cx = (x2 + x3) / 2;
    double cy = (y2 + y3) / 2;
    double abx = (ax + bx) / 2;
    double aby = (ay + by) / 2;
    double bcx = (bx + cx) / 2;
    double bcy = (by + cy) / 2;
    double mx = (abx + bcx) / 2;
    double my = (aby + bcy) / 2;
    flattenCubic(x0, y0, ax, ay, abx, aby, mx, my, level + 1);
    flattenCubic(mx, my, bcx, bcy, cx, cy, x3, y3, level + 1);
  }

  private static double distSq(double px, double py, double x1, double y1, double x2, double y2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double len = dx * dx + dy * dy;
    if (len == 0) {
      return (px - x1) * (px - x1) + (py - y1) * (py - y1);
    }
    double cross = (px - x1) * dy - (py - y1) * dx;
    return cross * cross / len;
  }

  private void advance() {
    queue.clear();
    pos = 0;
    primed = true;
    if (src.isDone()) {
      return;
    }
    int type = src.currentSegment(segs);
    switch (type) {
    case SEG_MOVETO:
      moveX = curX = segs[0];
      moveY = curY = segs[1];
      emit(SEG_MOVETO, curX, curY);
      break;
    case SEG_LINETO:
      curX = segs[0];
      curY = segs[1];
      emit(SEG_LINETO, curX, curY);
      break;
    case SEG_QUADTO:
      flattenQuad(curX, curY, segs[0], segs[1], segs[2], segs[3], 0);
      curX = segs[2];
      curY = segs[3];
      break;
    case SEG_CUBICTO:
      flattenCubic(curX, curY, segs[0], segs[1], segs[2], segs[3], segs[4], segs[5], 0);
      curX = segs[4];
      curY = segs[5];
      break;
    default:
      curX = moveX;
      curY = moveY;
      queue.add(new double[] {SEG_CLOSE, 0, 0});
      break;
    }
    src.next();
  }

  @Override
  public boolean isDone() {
    return queue.isEmpty() || pos >= queue.size();
  }

  @Override
  public void next() {
    pos++;
    if (pos >= queue.size() && !src.isDone()) {
      advance();
    } else if (pos >= queue.size()) {
      queue.clear();
    }
  }

  @Override
  public int currentSegment(float[] coords) {
    if (isDone()) {
      throw new NoSuchElementException("flattening iterator out of bounds");
    }
    double[] s = queue.get(pos);
    int type = (int) s[0];
    if (type != SEG_CLOSE) {
      coords[0] = (float) s[1];
      coords[1] = (float) s[2];
    }
    return type;
  }

  @Override
  public int currentSegment(double[] coords) {
    if (isDone()) {
      throw new NoSuchElementException("flattening iterator out of bounds");
    }
    double[] s = queue.get(pos);
    int type = (int) s[0];
    if (type != SEG_CLOSE) {
      coords[0] = s[1];
      coords[1] = s[2];
    }
    return type;
  }
}
