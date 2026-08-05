/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpringLayout implements LayoutManager2 {
  public static final String NORTH = "North";
  public static final String SOUTH = "South";
  public static final String EAST = "East";
  public static final String WEST = "West";
  public static final String HORIZONTAL_CENTER = "HorizontalCenter";
  public static final String VERTICAL_CENTER = "VerticalCenter";
  public static final String BASELINE = "Baseline";
  public static final String WIDTH = "Width";
  public static final String HEIGHT = "Height";

  private final Map<Component, Constraints> constraintMap = new HashMap<>();

  public SpringLayout() { }

  private static final int kStart = 0;
  private static final int kSize = 1;
  private static final int kEnd = 2;
  private static final int kCenter = 3;
  private static final int kBase = 4;

  public static class Constraints {
    private final Spring[] h = new Spring[4];
    private final Spring[] v = new Spring[5];
    private final List<Integer> hHistory = new ArrayList<>();
    private final List<Integer> vHistory = new ArrayList<>();
    private Component component;
    private boolean parentMode;

    public Constraints() { }

    public Constraints(Spring x, Spring y) {
      setX(x);
      setY(y);
    }

    public Constraints(Spring x, Spring y, Spring width, Spring height) {
      setX(x);
      setY(y);
      setWidth(width);
      setHeight(height);
    }

    public Constraints(Component c) {
      this.component = c;
      setX(Spring.constant(c.getX()));
      setY(Spring.constant(c.getY()));
      setWidth(Spring.width(c));
      setHeight(Spring.height(c));
    }

    void bind(Component c) {
      if (component == null) {
        component = c;
      }
    }

    private void put(Spring[] arr, List<Integer> history, int idx, Spring s) {
      hCache = null;
      vCache = null;
      if (s == null) {
        arr[idx] = null;
        history.remove(Integer.valueOf(idx));
        return;
      }
      history.remove(Integer.valueOf(idx));
      history.add(idx);
      arr[idx] = s;
      while (history.size() > 2) {
        int drop = history.remove(0);
        arr[drop] = null;
      }
    }

    private Spring defaultSize(boolean horizontal) {
      Spring cachedDefault = horizontal ? hDefault : vDefault;
      if (cachedDefault == null) {
        cachedDefault = newDefaultSize(horizontal);
        if (horizontal) {
          hDefault = cachedDefault;
        } else {
          vDefault = cachedDefault;
        }
      }
      return cachedDefault;
    }

    private Spring hDefault;
    private Spring vDefault;

    private Spring newDefaultSize(boolean horizontal) {
      if (parentMode) {
        return Spring.constant(0, 0, Integer.MAX_VALUE);
      }
      if (component != null) {
        return horizontal ? Spring.width(component) : Spring.height(component);
      }
      return Spring.constant(0);
    }

    private Spring base() {
      final Component c = parentMode ? null : component;
      return new Spring() {
        private int value() {
          if (c == null) {
            return 0;
          }
          Dimension d = c.getPreferredSize();
          return Math.max(c.getBaseline(d.width, d.height), 0);
        }

        public int getMinimumValue() { return value(); }

        public int getPreferredValue() { return value(); }

        public int getMaximumValue() { return value(); }

        public int getValue() { return value(); }

        public void setValue(int size) { }
      };
    }

    private Spring[] hCache;
    private Spring[] vCache;

    private Spring[] solve(boolean horizontal) {
      Spring[] cached = horizontal ? hCache : vCache;
      if (cached == null) {
        cached = solveFresh(horizontal);
        if (horizontal) {
          hCache = cached;
        } else {
          vCache = cached;
        }
      }
      return cached;
    }

    private Spring[] solveFresh(boolean horizontal) {
      Spring[] a = horizontal ? h : v;
      int n = horizontal ? 4 : 5;
      Spring[] out = new Spring[n];
      Spring start = a[kStart];
      Spring size = a[kSize];
      Spring end = a[kEnd];
      Spring center = a[kCenter];
      Spring half;
      if (!horizontal && a[kBase] != null) {
        Spring baseline = a[kBase];
        if (start == null && size == null && end == null && center == null) {
          start = Spring.sum(baseline, Spring.minus(base()));
        } else if (start == null) {
          start = Spring.sum(baseline, Spring.minus(base()));
        }
        out[kBase] = baseline;
      }
      int set = (start != null ? 1 : 0) + (size != null ? 1 : 0) + (end != null ? 1 : 0) + (center != null ? 1 : 0);
      if (set < 2) {
        if (size == null && end == null && center == null) {
          size = defaultSize(horizontal);
          if (start == null) {
            start = Spring.constant(0);
          }
        } else if (start == null && size == null) {
          size = defaultSize(horizontal);
        } else if (start == null) {
          start = Spring.constant(0);
        } else if (size == null) {
          size = defaultSize(horizontal);
        }
      }
      if (start != null && size != null) {
        // nothing to derive for start and size
      } else if (start != null && end != null) {
        size = Spring.sum(end, Spring.minus(start));
      } else if (start != null && center != null) {
        size = Spring.scale(Spring.sum(center, Spring.minus(start)), 2f);
      } else if (size != null && end != null) {
        start = Spring.sum(end, Spring.minus(size));
      } else if (size != null && center != null) {
        start = Spring.sum(center, Spring.minus(Spring.scale(size, 0.5f)));
      } else if (end != null && center != null) {
        size = Spring.scale(Spring.sum(end, Spring.minus(center)), 2f);
        start = Spring.sum(end, Spring.minus(size));
      }
      if (start == null) {
        start = Spring.constant(0);
      }
      if (size == null) {
        size = defaultSize(horizontal);
      }
      if (end == null) {
        end = Spring.sum(start, size);
      }
      if (center == null) {
        center = Spring.sum(start, Spring.scale(size, 0.5f));
      }
      out[kStart] = start;
      out[kSize] = size;
      out[kEnd] = end;
      out[kCenter] = center;
      if (!horizontal && out[kBase] == null) {
        out[kBase] = Spring.sum(start, base());
      }
      return out;
    }

    public void setX(Spring x) { put(h, hHistory, kStart, x); }

    public Spring getX() { return solve(true)[kStart]; }

    public void setY(Spring y) { put(v, vHistory, kStart, y); }

    public Spring getY() { return solve(false)[kStart]; }

    public void setWidth(Spring width) { put(h, hHistory, kSize, width); }

    public Spring getWidth() { return solve(true)[kSize]; }

    public void setHeight(Spring height) { put(v, vHistory, kSize, height); }

    public Spring getHeight() { return solve(false)[kSize]; }

    public void setConstraint(String edgeName, Spring s) {
      edgeName = edgeName.intern();
      if (edgeName == WEST) {
        setX(s);
      } else if (edgeName == NORTH) {
        setY(s);
      } else if (edgeName == EAST) {
        put(h, hHistory, kEnd, s);
      } else if (edgeName == SOUTH) {
        put(v, vHistory, kEnd, s);
      } else if (edgeName == HORIZONTAL_CENTER) {
        put(h, hHistory, kCenter, s);
      } else if (edgeName == VERTICAL_CENTER) {
        put(v, vHistory, kCenter, s);
      } else if (edgeName == BASELINE) {
        put(v, vHistory, kBase, s);
      } else if (edgeName == WIDTH) {
        setWidth(s);
      } else if (edgeName == HEIGHT) {
        setHeight(s);
      }
    }

    public Spring getConstraint(String edgeName) {
      edgeName = edgeName.intern();
      return (edgeName == WEST) ? getX()
          : (edgeName == NORTH) ? getY()
          : (edgeName == EAST) ? solve(true)[kEnd]
          : (edgeName == SOUTH) ? solve(false)[kEnd]
          : (edgeName == HORIZONTAL_CENTER) ? solve(true)[kCenter]
          : (edgeName == VERTICAL_CENTER) ? solve(false)[kCenter]
          : (edgeName == BASELINE) ? solve(false)[kBase]
          : (edgeName == WIDTH) ? getWidth()
          : (edgeName == HEIGHT) ? getHeight() : null;
    }

    void reset() {
      for (Spring s : solve(true)) {
        if (s != null) {
          s.setValue(Spring.UNSET);
        }
      }
      for (Spring s : solve(false)) {
        if (s != null) {
          s.setValue(Spring.UNSET);
        }
      }
    }
  }

  public void addLayoutComponent(String name, Component c) { }

  public void removeLayoutComponent(Component c) { constraintMap.remove(c); }

  private static Dimension addInsets(int width, int height, Container p) {
    Insets i = p.getInsets();
    return new Dimension(width + i.left + i.right, height + i.top + i.bottom);
  }

  public Dimension minimumLayoutSize(Container parent) {
    setParent(parent);
    Constraints pc = getConstraints(parent);
    return addInsets(pc.getWidth().getMinimumValue(), pc.getHeight().getMinimumValue(), parent);
  }

  public Dimension preferredLayoutSize(Container parent) {
    setParent(parent);
    Constraints pc = getConstraints(parent);
    return addInsets(pc.getWidth().getPreferredValue(), pc.getHeight().getPreferredValue(), parent);
  }

  public Dimension maximumLayoutSize(Container parent) {
    setParent(parent);
    Constraints pc = getConstraints(parent);
    return addInsets(pc.getWidth().getMaximumValue(), pc.getHeight().getMaximumValue(), parent);
  }

  public void addLayoutComponent(Component component, Object constraints) {
    if (constraints instanceof Constraints) {
      Constraints c = (Constraints) constraints;
      c.bind(component);
      constraintMap.put(component, c);
    }
  }

  public float getLayoutAlignmentX(Container p) { return 0.5f; }

  public float getLayoutAlignmentY(Container p) { return 0.5f; }

  public void invalidateLayout(Container p) { }

  public void putConstraint(String e1, Component c1, int pad, String e2, Component c2) {
    putConstraint(e1, c1, Spring.constant(pad), e2, c2);
  }

  public void putConstraint(String e1, Component c1, Spring s, String e2, Component c2) {
    putConstraint(e1, c1, Spring.sum(s, getConstraint(e2, c2)));
  }

  private void putConstraint(String e, Component c, Spring s) {
    if (s != null) {
      getConstraints(c).setConstraint(e, s);
    }
  }

  public Constraints getConstraints(Component c) {
    Constraints result = constraintMap.get(c);
    if (result == null) {
      if (c instanceof JComponent) {
        Object cp = ((JComponent) c).getClientProperty(SpringLayout.class);
        if (cp instanceof Constraints) {
          return (Constraints) cp;
        }
      }
      result = new Constraints();
      result.bind(c);
      constraintMap.put(c, result);
    }
    return result;
  }

  public Spring getConstraint(String edgeName, Component c) {
    edgeName = edgeName.intern();
    return new SpringProxy(edgeName, c, this);
  }

  private static final class SpringProxy extends Spring {
    private final String edgeName;
    private final Component c;
    private final SpringLayout l;

    SpringProxy(String edgeName, Component c, SpringLayout l) {
      this.edgeName = edgeName;
      this.c = c;
      this.l = l;
    }

    private Spring real() { return l.getConstraints(c).getConstraint(edgeName); }

    public int getMinimumValue() { return real().getMinimumValue(); }

    public int getPreferredValue() { return real().getPreferredValue(); }

    public int getMaximumValue() { return real().getMaximumValue(); }

    public int getValue() { return real().getValue(); }

    public void setValue(int size) { real().setValue(size); }
  }

  private void setParent(Container p) {
    Constraints pc = getConstraints(p);
    pc.parentMode = true;
    pc.setX(Spring.constant(0));
    pc.setY(Spring.constant(0));
    for (Constraints cc : constraintMap.values()) {
      cc.reset();
    }
  }

  public void layoutContainer(Container parent) {
    setParent(parent);
    int n = parent.getComponentCount();
    Constraints pc = getConstraints(parent);
    Insets insets = parent.getInsets();
    pc.getX().setValue(0);
    pc.getY().setValue(0);
    pc.getWidth().setValue(parent.getWidth() - insets.left - insets.right);
    pc.getHeight().setValue(parent.getHeight() - insets.top - insets.bottom);
    for (int i = 0; i < n; i++) {
      Component c = parent.getComponent(i);
      Constraints cc = getConstraints(c);
      int x = cc.getX().getValue();
      int y = cc.getY().getValue();
      int width = cc.getWidth().getValue();
      int height = cc.getHeight().getValue();
      c.setBounds(insets.left + x, insets.top + y, width, height);
    }
  }
}
