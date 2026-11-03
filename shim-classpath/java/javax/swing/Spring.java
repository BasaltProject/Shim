/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;

public abstract class Spring {
  public static final int UNSET = Integer.MIN_VALUE;

  protected Spring() { }

  public abstract int getMinimumValue();

  public abstract int getPreferredValue();

  public abstract int getMaximumValue();

  public abstract int getValue();

  public abstract void setValue(int size);

  private static int range(Spring s, boolean forward) {
    return forward ? s.getMaximumValue() - s.getPreferredValue() : s.getPreferredValue() - s.getMinimumValue();
  }

  static double strainOf(Spring s) {
    double delta = s.getValue() - s.getPreferredValue();
    int r = range(s, delta >= 0);
    return r == 0 ? 0.0 : delta / r;
  }

  static void applyStrain(Spring s, double strain) {
    int r = range(s, strain >= 0);
    s.setValue(s.getPreferredValue() + (int) (strain * r));
  }

  public static Spring constant(int pref) { return constant(pref, pref, pref); }

  public static Spring constant(int min, int pref, int max) { return new ConstantSpring(min, pref, max); }

  public static Spring minus(Spring s) { return new MinusSpring(s); }

  public static Spring sum(Spring s1, Spring s2) { return new SumSpring(s1, s2); }

  public static Spring max(Spring s1, Spring s2) { return new MaxSpring(s1, s2); }

  public static Spring scale(Spring s, float factor) {
    if (s == null) {
      throw new NullPointerException("Spring argument must not be null");
    }
    return new ScaleSpring(s, factor);
  }

  public static Spring width(Component c) {
    if (c == null) {
      throw new NullPointerException("Component argument must not be null");
    }
    return new DimensionSpring(c, true);
  }

  public static Spring height(Component c) {
    if (c == null) {
      throw new NullPointerException("Component argument must not be null");
    }
    return new DimensionSpring(c, false);
  }

  private abstract static class Cached extends Spring {
    private int size = UNSET;
    private int min = UNSET;
    private int pref = UNSET;
    private int max = UNSET;

    abstract int computeMin();

    abstract int computePref();

    abstract int computeMax();

    void onSet(int value) { }

    void clear() {
      min = pref = max = size = UNSET;
    }

    public int getMinimumValue() {
      if (min == UNSET) {
        min = computeMin();
      }
      return min;
    }

    public int getPreferredValue() {
      if (pref == UNSET) {
        pref = computePref();
      }
      return pref;
    }

    public int getMaximumValue() {
      if (max == UNSET) {
        max = computeMax();
      }
      return max;
    }

    int defaultValue() { return getPreferredValue(); }

    public int getValue() { return size != UNSET ? size : defaultValue(); }

    public void setValue(int value) {
      if (size == value) {
        return;
      }
      if (value == UNSET) {
        clear();
      } else {
        size = value;
        onSet(value);
      }
    }
  }

  private static final class ConstantSpring extends Spring {
    private final int min;
    private final int pref;
    private final int max;
    private int value = UNSET;

    ConstantSpring(int min, int pref, int max) {
      this.min = min;
      this.pref = pref;
      this.max = max;
    }

    public int getMinimumValue() { return min; }

    public int getPreferredValue() { return pref; }

    public int getMaximumValue() { return max; }

    public int getValue() { return value == UNSET ? pref : value; }

    public void setValue(int size) { value = size; }

    public String toString() { return "[" + min + "," + pref + "," + max + "]"; }
  }

  private static final class DimensionSpring extends Cached {
    private final Component c;
    private final boolean horizontal;

    DimensionSpring(Component c, boolean horizontal) {
      this.c = c;
      this.horizontal = horizontal;
    }

    int computeMin() { return horizontal ? c.getMinimumSize().width : c.getMinimumSize().height; }

    int computePref() { return horizontal ? c.getPreferredSize().width : c.getPreferredSize().height; }

    int computeMax() {
      int v = horizontal ? c.getMaximumSize().width : c.getMaximumSize().height;
      return Math.min(v, 32767);
    }
  }

  private static final class MinusSpring extends Spring {
    private final Spring s;

    MinusSpring(Spring s) { this.s = s; }

    public int getMinimumValue() { return -s.getMaximumValue(); }

    public int getPreferredValue() { return -s.getPreferredValue(); }

    public int getMaximumValue() { return -s.getMinimumValue(); }

    public int getValue() { return -s.getValue(); }

    public void setValue(int size) { s.setValue(size == UNSET ? UNSET : -size); }
  }

  private static final class ScaleSpring extends Spring {
    private final Spring s;
    private final float factor;

    ScaleSpring(Spring s, float factor) {
      this.s = s;
      this.factor = factor;
    }

    public int getMinimumValue() { return Math.round((factor < 0 ? s.getMaximumValue() : s.getMinimumValue()) * factor); }

    public int getPreferredValue() { return Math.round(s.getPreferredValue() * factor); }

    public int getMaximumValue() { return Math.round((factor < 0 ? s.getMinimumValue() : s.getMaximumValue()) * factor); }

    public int getValue() { return Math.round(s.getValue() * factor); }

    public void setValue(int size) {
      if (size == UNSET) {
        s.setValue(UNSET);
      } else {
        s.setValue(Math.round(size / factor));
      }
    }
  }

  private static final class SumSpring extends Cached {
    private final Spring a;
    private final Spring b;

    SumSpring(Spring a, Spring b) {
      this.a = a;
      this.b = b;
    }

    int defaultValue() { return a.getValue() + b.getValue(); }

    int computeMin() { return a.getMinimumValue() + b.getMinimumValue(); }

    int computePref() { return a.getPreferredValue() + b.getPreferredValue(); }

    int computeMax() { return (int) Math.min((long) a.getMaximumValue() + (long) b.getMaximumValue(), Integer.MAX_VALUE); }

    void onSet(int value) {
      applyStrain(a, strainOf(this));
      b.setValue(value - a.getValue());
    }

    void clear() {
      super.clear();
      a.setValue(UNSET);
      b.setValue(UNSET);
    }
  }

  private static final class MaxSpring extends Cached {
    private final Spring a;
    private final Spring b;

    MaxSpring(Spring a, Spring b) {
      this.a = a;
      this.b = b;
    }

    int defaultValue() { return Math.max(a.getValue(), b.getValue()); }

    int computeMin() { return Math.max(a.getMinimumValue(), b.getMinimumValue()); }

    int computePref() { return Math.max(a.getPreferredValue(), b.getPreferredValue()); }

    int computeMax() { return Math.max(a.getMaximumValue(), b.getMaximumValue()); }

    void onSet(int value) {
      a.setValue(value);
      b.setValue(value);
    }

    void clear() {
      super.clear();
      a.setValue(UNSET);
      b.setValue(UNSET);
    }
  }
}
