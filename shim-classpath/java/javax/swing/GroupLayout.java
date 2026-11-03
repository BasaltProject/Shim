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
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupLayout implements LayoutManager2 {
  public static final int DEFAULT_SIZE = -1;
  public static final int PREFERRED_SIZE = -2;

  private static final int kHorizontal = 0;
  private static final int kVertical = 1;
  private static final int kMin = 0;
  private static final int kPref = 1;
  private static final int kMax = 2;
  private static final int kCap = Integer.MAX_VALUE / 2;

  private final Container host;
  private Group horizontalGroup;
  private Group verticalGroup;
  private Group userHorizontal;
  private Group userVertical;
  private boolean autoCreateGaps;
  private boolean autoCreateContainerGaps;
  private boolean honorsVisibility = true;
  private LayoutStyle layoutStyle;
  private final Map<Component, Boolean> visibilityOverrides = new IdentityHashMap<Component, Boolean>();
  private final Map<Component, List<Component>[]> links = new IdentityHashMap<Component, List<Component>[]>();
  private final Map<Component, int[]> placed = new IdentityHashMap<Component, int[]>();

  public enum Alignment {
    LEADING, TRAILING, CENTER, BASELINE
  }

  public GroupLayout(Container host) {
    if (host == null) {
      throw new IllegalArgumentException("Container must be non-null");
    }
    this.host = host;
    setHorizontalGroup(createParallelGroup(Alignment.LEADING));
    setVerticalGroup(createParallelGroup(Alignment.LEADING));
    horizontalGroup = null;
    verticalGroup = null;
    userHorizontal = null;
    userVertical = null;
  }

  public void setHonorsVisibility(boolean honorsVisibility) {
    if (this.honorsVisibility != honorsVisibility) {
      this.honorsVisibility = honorsVisibility;
      invalidateHost();
    }
  }

  public boolean getHonorsVisibility() { return honorsVisibility; }

  public void setHonorsVisibility(Component component, Boolean honorsVisibility) {
    if (component == null) {
      throw new IllegalArgumentException("Component must be non-null");
    }
    if (honorsVisibility == null) {
      visibilityOverrides.remove(component);
    } else {
      visibilityOverrides.put(component, honorsVisibility);
    }
    invalidateHost();
  }

  public void setAutoCreateGaps(boolean autoCreatePadding) {
    if (this.autoCreateGaps != autoCreatePadding) {
      this.autoCreateGaps = autoCreatePadding;
      invalidateHost();
    }
  }

  public boolean getAutoCreateGaps() { return autoCreateGaps; }

  public void setAutoCreateContainerGaps(boolean autoCreateContainerPadding) {
    if (this.autoCreateContainerGaps != autoCreateContainerPadding) {
      this.autoCreateContainerGaps = autoCreateContainerPadding;
      horizontalGroup = createTopLevelGroup(userHorizontal);
      verticalGroup = createTopLevelGroup(userVertical);
      invalidateHost();
    }
  }

  public boolean getAutoCreateContainerGaps() { return autoCreateContainerGaps; }

  public void setHorizontalGroup(Group group) {
    if (group == null) {
      throw new IllegalArgumentException("Group must be non-null");
    }
    userHorizontal = group;
    horizontalGroup = createTopLevelGroup(group);
    invalidateHost();
  }

  public void setVerticalGroup(Group group) {
    if (group == null) {
      throw new IllegalArgumentException("Group must be non-null");
    }
    userVertical = group;
    verticalGroup = createTopLevelGroup(group);
    invalidateHost();
  }

  private Group createTopLevelGroup(Group specified) {
    if (specified == null) {
      return null;
    }
    SequentialGroup group = createSequentialGroup();
    if (getAutoCreateContainerGaps()) {
      group.addSpring(new ContainerGapSpring(true));
      group.addGroup(specified);
      group.addSpring(new ContainerGapSpring(false));
    } else {
      group.addGroup(specified);
    }
    return group;
  }

  public SequentialGroup createSequentialGroup() { return new SequentialGroup(); }

  public ParallelGroup createParallelGroup() { return createParallelGroup(Alignment.LEADING); }

  public ParallelGroup createParallelGroup(Alignment alignment) { return createParallelGroup(alignment, true); }

  public ParallelGroup createParallelGroup(Alignment alignment, boolean resizable) {
    if (alignment == null) {
      throw new IllegalArgumentException("alignment must be non null");
    }
    return new ParallelGroup(alignment, resizable);
  }

  public ParallelGroup createBaselineGroup(boolean resizable, boolean anchorBaselineToTop) {
    ParallelGroup g = new ParallelGroup(Alignment.BASELINE, resizable);
    return g;
  }

  public void linkSize(Component... components) { linkSize(kHorizontal, components); linkSize(kVertical, components); }

  @SuppressWarnings("unchecked")
  public void linkSize(int axis, Component... components) {
    if (components == null) {
      throw new IllegalArgumentException("Components must be non-null");
    }
    if (axis != SwingConstants.HORIZONTAL && axis != SwingConstants.VERTICAL) {
      throw new IllegalArgumentException("Axis must be one of SwingConstants.HORIZONTAL or SwingConstants.VERTICAL");
    }
    int ax = axis == SwingConstants.HORIZONTAL ? kHorizontal : kVertical;
    List<Component> group = new ArrayList<Component>();
    for (int i = components.length - 1; i >= 0; i--) {
      Component c = components[i];
      if (components[i] == null) {
        throw new IllegalArgumentException("Components must be non-null");
      }
      List<Component>[] slots = links.get(c);
      if (slots == null) {
        slots = new List[2];
        links.put(c, slots);
      }
      if (slots[ax] != null) {
        group.addAll(slots[ax]);
      }
      if (!group.contains(c)) {
        group.add(c);
      }
    }
    for (Component c : group) {
      List<Component>[] slots = links.get(c);
      if (slots == null) {
        slots = new List[2];
        links.put(c, slots);
      }
      slots[ax] = group;
    }
    invalidateHost();
  }

  public void replace(Component existingComponent, Component newComponent) {
    if (existingComponent == null || newComponent == null) {
      throw new IllegalArgumentException("Components must be non-null");
    }
    if (horizontalGroup != null) {
      horizontalGroup.replaceComponent(existingComponent, newComponent);
    }
    if (verticalGroup != null) {
      verticalGroup.replaceComponent(existingComponent, newComponent);
    }
    if (newComponent.getParent() != host) {
      host.add(newComponent);
    }
    if (existingComponent.getParent() == host) {
      host.remove(existingComponent);
    }
    invalidateHost();
  }

  public void setLayoutStyle(LayoutStyle layoutStyle) {
    this.layoutStyle = layoutStyle;
    invalidateHost();
  }

  public LayoutStyle getLayoutStyle() { return layoutStyle; }

  private LayoutStyle style() { return layoutStyle != null ? layoutStyle : LayoutStyle.getInstance(); }

  private void invalidateHost() {
    if (host != null) {
      host.invalidate();
    }
  }

  public void addLayoutComponent(String name, Component component) { }

  public void removeLayoutComponent(Component component) {
    if (horizontalGroup != null) {
      horizontalGroup.removeComponent(component);
    }
    if (verticalGroup != null) {
      verticalGroup.removeComponent(component);
    }
    links.remove(component);
  }

  private void checkAttached() {
    if (horizontalGroup == null) {
      throw new IllegalStateException("Horizontal group must be set");
    }
    if (verticalGroup == null) {
      throw new IllegalStateException("Vertical group must be set");
    }
    Set<Component> h = new LinkedHashSet<Component>();
    Set<Component> v = new LinkedHashSet<Component>();
    horizontalGroup.collectComponents(h);
    verticalGroup.collectComponents(v);
    for (Component c : host.getComponents()) {
      if (!h.contains(c)) {
        throw new IllegalStateException(c + " is not attached to a horizontal group");
      }
      if (!v.contains(c)) {
        throw new IllegalStateException(c + " is not attached to a vertical group");
      }
    }
  }

  private boolean isGroupsSet() { return horizontalGroup != null && verticalGroup != null; }

  public Dimension preferredLayoutSize(Container parent) { return sizeOf(parent, kPref); }

  public Dimension minimumLayoutSize(Container parent) { return sizeOf(parent, kMin); }

  public Dimension maximumLayoutSize(Container parent) { return sizeOf(parent, kMax); }

  private Dimension sizeOf(Container parent, int kind) {
    checkParent(parent);
    checkAttached();
    Insets insets = parent.getInsets();
    int w = horizontalGroup.size(kHorizontal, kind);
    int h = verticalGroup.size(kVertical, kind);
    return new Dimension(add(w, insets.left + insets.right), add(h, insets.top + insets.bottom));
  }

  private static int add(int a, int b) {
    long s = (long) a + b;
    return s > kCap ? kCap : (int) s;
  }

  public void layoutContainer(Container parent) {
    checkParent(parent);
    if (!isGroupsSet()) {
      return;
    }
    checkAttached();
    Insets insets = parent.getInsets();
    int width = parent.getWidth() - insets.left - insets.right;
    int height = parent.getHeight() - insets.top - insets.bottom;
    placed.clear();
    horizontalGroup.place(kHorizontal, insets.left, width);
    verticalGroup.place(kVertical, insets.top, height);
    for (Component c : host.getComponents()) {
      int[] xs = placed.get(c);
      if (xs != null) {
        c.setBounds(xs[0], xs[2], xs[1], xs[3]);
      }
    }
  }

  public void addLayoutComponent(Component component, Object constraints) { }

  public float getLayoutAlignmentX(Container parent) {
    checkParent(parent);
    return .5f;
  }

  public float getLayoutAlignmentY(Container parent) {
    checkParent(parent);
    return .5f;
  }

  public void invalidateLayout(Container parent) { checkParent(parent); }

  private void checkParent(Container parent) {
    if (parent != host) {
      throw new IllegalArgumentException("GroupLayout can only be used with one Container at a time");
    }
  }

  public String toString() {
    if (horizontalGroup == null || verticalGroup == null) {
      return super.toString();
    }
    return "HORIZONTAL\n" + horizontalGroup + "\nVERTICAL\n" + verticalGroup;
  }

  private boolean honors(Component c) {
    Boolean b = visibilityOverrides.get(c);
    return b != null ? b.booleanValue() : honorsVisibility;
  }

  private static void checkResizeType(int type, boolean isComponentSpring) {
    if (type < 0 && ((isComponentSpring && type != DEFAULT_SIZE && type != PREFERRED_SIZE)
        || (!isComponentSpring && type != PREFERRED_SIZE))) {
      throw new IllegalArgumentException("Invalid size");
    }
  }

  private static void checkSize(int min, int pref, int max, boolean isComponentSpring) {
    checkResizeType(min, isComponentSpring);
    checkResizeType(pref, isComponentSpring);
    checkResizeType(max, isComponentSpring);
    if ((min >= 0 && pref >= 0 && min > pref) || (pref >= 0 && max >= 0 && pref > max)) {
      throw new IllegalArgumentException("Following is not met: min<=pref<=max");
    }
  }

  static int[] distribute(int size, int[] mins, int[] prefs, int[] maxs, long fixed) {
    int n = prefs.length;
    int[] out = prefs.clone();
    long totalPref = fixed;
    for (int p : prefs) {
      totalPref += p;
    }
    long delta = size - totalPref;
    if (delta == 0 || n == 0) {
      return out;
    }
    int[] caps = new int[n];
    boolean grow = delta > 0;
    for (int i = 0; i < n; i++) {
      caps[i] = grow ? Math.max(0, maxs[i] - prefs[i]) : Math.max(0, prefs[i] - mins[i]);
    }
    long remaining = Math.abs(delta);
    Integer[] order = new Integer[n];
    int active = 0;
    for (int i = 0; i < n; i++) {
      if (caps[i] > 0) {
        order[active++] = i;
      }
    }
    final int[] capsRef = caps;
    java.util.Arrays.sort(order, 0, active, new java.util.Comparator<Integer>() {
      public int compare(Integer x, Integer y) { return Integer.compare(capsRef[x], capsRef[y]); }
    });
    int left = active;
    for (int k = 0; k < active && remaining > 0; k++) {
      int i = order[k];
      long g = remaining / left;
      left--;
      if (g > caps[i]) {
        g = caps[i];
      }
      remaining -= g;
      out[i] += grow ? (int) g : -(int) g;
    }
    return out;
  }

  abstract static class Spring {
    abstract int size(int axis, int kind);

    abstract void place(int axis, int origin, int size);

    void leading(int axis, List<Component> out) { }

    void trailing(int axis, List<Component> out) { }

    boolean visible() { return true; }

    void collectComponents(Set<Component> out) { }

    void replaceComponent(Component a, Component b) { }

    void removeComponent(Component c) { }

    boolean isGap() { return false; }
  }

  private final class ComponentSpring extends Spring {
    Component component;
    final int min;
    final int pref;
    final int max;

    ComponentSpring(Component c, int min, int pref, int max) {
      this.component = c;
      this.min = min;
      this.pref = pref;
      this.max = max;
    }

    boolean visible() { return !honors(component) || component.isVisible(); }

    private int natural(int axis, int kind) {
      Dimension d;
      switch (kind) {
        case kMin:
          d = component.getMinimumSize();
          break;
        case kMax:
          d = component.getMaximumSize();
          break;
        default:
          d = component.getPreferredSize();
          break;
      }
      return axis == kHorizontal ? d.width : d.height;
    }

    private int own(int axis, int kind) {
      int spec = kind == kMin ? min : kind == kPref ? pref : max;
      if (spec >= 0) {
        return spec;
      }
      if (spec == PREFERRED_SIZE) {
        return kind == kPref ? natural(axis, kPref) : own(axis, kPref);
      }
      return natural(axis, kind);
    }

    int size(int axis, int kind) {
      if (!visible()) {
        return 0;
      }
      int mn = own(axis, kMin);
      int mx = Math.max(mn, own(axis, kMax));
      int v = kind == kMin ? mn : kind == kMax ? mx : Math.min(Math.max(own(axis, kPref), mn), mx);
      List<Component>[] slots = links.get(component);
      if (slots != null && slots[axis] != null && allDefault(axis, slots[axis])) {
        for (Component other : slots[axis]) {
          if (other == component) {
            continue;
          }
          Spring s = findSpring(axis, other);
          int ov = s instanceof ComponentSpring ? ((ComponentSpring) s).ownLinked(axis, kind) : natural0(other, axis, kind);
          v = Math.max(v, ov);
        }
      }
      return v;
    }

    boolean isDefaultSpec() { return min == DEFAULT_SIZE && pref == DEFAULT_SIZE && max == DEFAULT_SIZE; }

    int ownLinked(int axis, int kind) { return visible() ? own(axis, kind) : 0; }

    private int natural0(Component c, int axis, int kind) {
      Dimension d = kind == kMin ? c.getMinimumSize() : kind == kMax ? c.getMaximumSize() : c.getPreferredSize();
      return axis == kHorizontal ? d.width : d.height;
    }

    void place(int axis, int origin, int size) {
      int[] xs = placed.get(component);
      if (xs == null) {
        xs = new int[4];
        placed.put(component, xs);
      }
      if (axis == kHorizontal) {
        xs[0] = origin;
        xs[1] = size;
      } else {
        xs[2] = origin;
        xs[3] = size;
      }
    }

    void leading(int axis, List<Component> out) {
      if (visible()) {
        out.add(component);
      }
    }

    void trailing(int axis, List<Component> out) {
      if (visible()) {
        out.add(component);
      }
    }

    void collectComponents(Set<Component> out) { out.add(component); }

    void replaceComponent(Component a, Component b) {
      if (component == a) {
        component = b;
      }
    }
  }

  private boolean allDefault(int axis, List<Component> group) {
    for (Component c : group) {
      Spring sp = findSpring(axis, c);
      if (sp instanceof ComponentSpring && !((ComponentSpring) sp).isDefaultSpec()) {
        return false;
      }
    }
    return true;
  }

  private Spring findSpring(int axis, Component c) {
    Group g = axis == kHorizontal ? horizontalGroup : verticalGroup;
    return g == null ? null : g.find(c);
  }

  private final class GapSpring extends Spring {
    final int min;
    final int pref;
    final int max;
    final boolean preferredType;
    final ComponentPlacement_ placement;

    GapSpring(int min, int pref, int max) {
      this.min = min;
      this.pref = pref;
      this.max = max;
      this.preferredType = false;
      this.placement = null;
    }

    GapSpring(ComponentPlacement_ placement, int pref, int max) {
      this.min = 0;
      this.pref = pref;
      this.max = max;
      this.preferredType = true;
      this.placement = placement;
    }

    boolean isGap() { return true; }

    int size(int axis, int kind) { return 0; }

    void place(int axis, int origin, int size) { }

    int gapSize(int axis, int kind, int natural) {
      int spec = kind == kMin ? min : kind == kPref ? pref : max;
      if (spec >= 0) {
        return spec;
      }
      if (spec == PREFERRED_SIZE && kind != kPref) {
        return gapSize(axis, kPref, natural);
      }
      return natural;
    }
  }

  private static final class ComponentPlacement_ {
    final LayoutStyle.ComponentPlacement type;
    JComponent c1;
    JComponent c2;

    ComponentPlacement_(LayoutStyle.ComponentPlacement type, JComponent c1, JComponent c2) {
      this.type = type;
      this.c1 = c1;
      this.c2 = c2;
    }
  }

  private final class ContainerGapSpring extends Spring {
    final boolean first;

    ContainerGapSpring(boolean first) { this.first = first; }

    boolean isGap() { return true; }

    int size(int axis, int kind) { return 0; }

    void place(int axis, int origin, int size) { }
  }

  public abstract class Group extends Spring {
    final List<Spring> springs = new ArrayList<Spring>();
    boolean topLevelWrapper;

    Group() { }

    public Group addGroup(Group group) { return addSpring(group); }

    public Group addComponent(Component component) { return addComponent(component, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE); }

    public Group addComponent(Component component, int min, int pref, int max) {
      return addSpring(newComponentSpring(component, min, pref, max));
    }

    public Group addGap(int size) { return addGap(size, size, size); }

    public Group addGap(int min, int pref, int max) {
      checkSize(min, pref, max, false);
      return addSpring(new GapSpring(min, pref, max));
    }

    ComponentSpring newComponentSpring(Component component, int min, int pref, int max) {
      if (component == null) {
        throw new IllegalArgumentException("Component must be non-null");
      }
      checkSize(min, pref, max, true);
      if (component.getParent() != host) {
        host.add(component);
      }
      return new ComponentSpring(component, min, pref, max);
    }

    Group addSpring(Spring s) {
      springs.add(s);
      return this;
    }

    Spring find(Component c) {
      for (Spring s : springs) {
        if (s instanceof ComponentSpring && ((ComponentSpring) s).component == c) {
          return s;
        }
        if (s instanceof Group) {
          Spring r = ((Group) s).find(c);
          if (r != null) {
            return r;
          }
        }
      }
      return null;
    }

    void collectComponents(Set<Component> out) {
      for (Spring s : springs) {
        s.collectComponents(out);
      }
    }

    void replaceComponent(Component a, Component b) {
      for (Spring s : springs) {
        s.replaceComponent(a, b);
      }
    }

    void removeComponent(Component c) {
      for (int i = springs.size() - 1; i >= 0; i--) {
        Spring s = springs.get(i);
        if (s instanceof ComponentSpring && ((ComponentSpring) s).component == c) {
          springs.remove(i);
        } else {
          s.removeComponent(c);
        }
      }
    }

    public String toString() { return getClass().getSimpleName() + springs; }
  }

  public final class SequentialGroup extends Group {
    SequentialGroup() { }

    public SequentialGroup addGroup(Group group) { return (SequentialGroup) super.addGroup(group); }

    public SequentialGroup addGroup(boolean useAsBaseline, Group group) {
      super.addGroup(group);
      return this;
    }

    public SequentialGroup addComponent(Component component) { return (SequentialGroup) super.addComponent(component); }

    public SequentialGroup addComponent(boolean useAsBaseline, Component component) {
      super.addComponent(component);
      return this;
    }

    public SequentialGroup addComponent(Component component, int min, int pref, int max) {
      return (SequentialGroup) super.addComponent(component, min, pref, max);
    }

    public SequentialGroup addComponent(boolean useAsBaseline, Component component, int min, int pref, int max) {
      super.addComponent(component, min, pref, max);
      return this;
    }

    public SequentialGroup addGap(int size) { return (SequentialGroup) super.addGap(size); }

    public SequentialGroup addGap(int min, int pref, int max) { return (SequentialGroup) super.addGap(min, pref, max); }

    public SequentialGroup addPreferredGap(JComponent comp1, JComponent comp2, LayoutStyle.ComponentPlacement type) {
      return addPreferredGap(comp1, comp2, type, DEFAULT_SIZE, PREFERRED_SIZE);
    }

    public SequentialGroup addPreferredGap(JComponent comp1, JComponent comp2, LayoutStyle.ComponentPlacement type,
        int pref, int max) {
      if (type == null) {
        throw new IllegalArgumentException("Type must be non-null");
      }
      if (comp1 == null || comp2 == null) {
        throw new IllegalArgumentException("Components must be non-null");
      }
      checkPreferredGapValues(pref, max);
      addSpring(new GapSpring(new ComponentPlacement_(type, comp1, comp2), pref, max));
      return this;
    }

    public SequentialGroup addPreferredGap(LayoutStyle.ComponentPlacement type) {
      return addPreferredGap(type, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public SequentialGroup addPreferredGap(LayoutStyle.ComponentPlacement type, int pref, int max) {
      if (type != LayoutStyle.ComponentPlacement.RELATED && type != LayoutStyle.ComponentPlacement.UNRELATED) {
        throw new IllegalArgumentException("Type must be one of LayoutStyle.ComponentPlacement.RELATED or "
            + "LayoutStyle.ComponentPlacement.UNRELATED");
      }
      checkPreferredGapValues(pref, max);
      addSpring(new GapSpring(new ComponentPlacement_(type, null, null), pref, max));
      return this;
    }

    private void checkPreferredGapValues(int pref, int max) {
      if ((pref < 0 && pref != DEFAULT_SIZE && pref != PREFERRED_SIZE) || (max < 0 && max != DEFAULT_SIZE
          && max != PREFERRED_SIZE)) {
        throw new IllegalArgumentException("Pref and max must be either DEFAULT_SIZE, PREFERRED_SIZE, or >= 0");
      }
    }

    public SequentialGroup addContainerGap() { return addContainerGap(DEFAULT_SIZE, DEFAULT_SIZE); }

    public SequentialGroup addContainerGap(int pref, int max) {
      if ((pref < 0 && pref != DEFAULT_SIZE) || (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE)) {
        throw new IllegalArgumentException("Pref and max must be either DEFAULT_VALUE or >= 0");
      }
      boolean first = springs.isEmpty();
      GapSpring g = new GapSpring(new ComponentPlacement_(null, null, null), pref, max);
      addSpring(g);
      containerGaps.add(g);
      return this;
    }

    final Set<GapSpring> containerGaps = new LinkedHashSet<GapSpring>();

    private int gapNatural(int axis, int index, Spring s) {
      List<Component> before = new ArrayList<Component>();
      List<Component> after = new ArrayList<Component>();
      for (int i = index - 1; i >= 0 && before.isEmpty(); i--) {
        if (!springs.get(i).isGap()) {
          springs.get(i).trailing(axis, before);
        }
      }
      for (int i = index + 1; i < springs.size() && after.isEmpty(); i++) {
        if (!springs.get(i).isGap()) {
          springs.get(i).leading(axis, after);
        }
      }
      int position = axis == kHorizontal ? SwingConstants.EAST : SwingConstants.SOUTH;
      int gap = 0;
      LayoutStyle ls = style();
      if (s instanceof GapSpring && ((GapSpring) s).placement != null) {
        ComponentPlacement_ cp = ((GapSpring) s).placement;
        if (cp.type == null) {
          Component only = after.isEmpty() ? (before.isEmpty() ? null : before.get(0)) : after.get(0);
          boolean leadingEdge = before.isEmpty();
          int pos = axis == kHorizontal ? (leadingEdge ? SwingConstants.WEST : SwingConstants.EAST)
              : (leadingEdge ? SwingConstants.NORTH : SwingConstants.SOUTH);
          return ls.getContainerGap(only instanceof JComponent ? (JComponent) only : null, pos, host);
        }
        if (cp.c1 != null) {
          return ls.getPreferredGap(cp.c1, cp.c2, cp.type, position, host);
        }
        for (Component a : before) {
          for (Component b : after) {
            if (a instanceof JComponent && b instanceof JComponent) {
              gap = Math.max(gap, ls.getPreferredGap((JComponent) a, (JComponent) b, cp.type, position, host));
            }
          }
        }
        return gap;
      }
      return gap;
    }

    private int autoGap(int axis, int index) {
      if (!autoCreateGaps) {
        return 0;
      }
      List<Component> before = new ArrayList<Component>();
      List<Component> after = new ArrayList<Component>();
      springs.get(index - 1).trailing(axis, before);
      springs.get(index).leading(axis, after);
      int position = axis == kHorizontal ? SwingConstants.EAST : SwingConstants.SOUTH;
      int gap = 0;
      LayoutStyle ls = style();
      for (Component a : before) {
        for (Component b : after) {
          if (a instanceof JComponent && b instanceof JComponent) {
            gap = Math.max(gap, ls.getPreferredGap((JComponent) a, (JComponent) b,
                LayoutStyle.ComponentPlacement.RELATED, position, host));
          }
        }
      }
      return gap;
    }

    private int[] sizes(int axis, int kind) {
      int n = springs.size();
      int[] out = new int[n];
      for (int i = 0; i < n; i++) {
        Spring s = springs.get(i);
        if (s instanceof GapSpring) {
          GapSpring g = (GapSpring) s;
          int nat = g.placement != null ? gapNatural(axis, i, s) : 0;
          out[i] = g.gapSize(axis, kind, nat);
          if (g.pref == PREFERRED_SIZE && kind == kMax) {
            out[i] = g.gapSize(axis, kPref, nat);
          }
          if (g.max == PREFERRED_SIZE && kind == kMax) {
            out[i] = g.gapSize(axis, kPref, nat);
          }
        } else if (s instanceof ContainerGapSpring) {
          out[i] = containerGap(axis, i, (ContainerGapSpring) s);
        } else {
          out[i] = s.size(axis, kind);
        }
      }
      return out;
    }

    private int containerGap(int axis, int index, ContainerGapSpring s) {
      List<Component> comps = new ArrayList<Component>();
      if (s.first) {
        for (int i = index + 1; i < springs.size() && comps.isEmpty(); i++) {
          springs.get(i).leading(axis, comps);
        }
      } else {
        for (int i = index - 1; i >= 0 && comps.isEmpty(); i--) {
          springs.get(i).trailing(axis, comps);
        }
      }
      int pos = axis == kHorizontal ? (s.first ? SwingConstants.WEST : SwingConstants.EAST)
          : (s.first ? SwingConstants.NORTH : SwingConstants.SOUTH);
      Component c = comps.isEmpty() ? null : comps.get(0);
      return style().getContainerGap(c instanceof JComponent ? (JComponent) c : null, pos, host);
    }

    int size(int axis, int kind) {
      int[] sz = sizes(axis, kind);
      long total = 0;
      for (int i = 0; i < sz.length; i++) {
        if (i > 0 && !springs.get(i).isGap() && !springs.get(i - 1).isGap() && spring_visible(i) && spring_visible(i - 1)) {
          total += autoGap(axis, i);
        }
        total += sz[i];
      }
      return (int) Math.min(total, kCap);
    }

    private boolean spring_visible(int i) { return springs.get(i).visible(); }

    void place(int axis, int origin, int size) {
      int n = springs.size();
      int[] mins = sizes(axis, kMin);
      int[] prefs = sizes(axis, kPref);
      int[] maxs = sizes(axis, kMax);
      int[] autos = new int[n];
      for (int i = 1; i < n; i++) {
        if (!springs.get(i).isGap() && !springs.get(i - 1).isGap() && spring_visible(i) && spring_visible(i - 1)) {
          autos[i] = autoGap(axis, i);
        }
      }
      long fixed = 0;
      for (int i = 0; i < n; i++) {
        fixed += autos[i];
      }
      int[] spans = distribute(size, mins, prefs, maxs, fixed);
      int pos = origin;
      for (int i = 0; i < n; i++) {
        pos += autos[i];
        springs.get(i).place(axis, pos, spans[i]);
        pos += spans[i];
      }
    }

    void leading(int axis, List<Component> out) {
      for (Spring s : springs) {
        if (!s.isGap()) {
          int before = out.size();
          s.leading(axis, out);
          if (out.size() > before) {
            return;
          }
        }
      }
    }

    void trailing(int axis, List<Component> out) {
      for (int i = springs.size() - 1; i >= 0; i--) {
        Spring s = springs.get(i);
        if (!s.isGap()) {
          int before = out.size();
          s.trailing(axis, out);
          if (out.size() > before) {
            return;
          }
        }
      }
    }

    boolean visible() {
      for (Spring s : springs) {
        if (!s.isGap() && s.visible()) {
          return true;
        }
      }
      return springs.isEmpty();
    }
  }

  public class ParallelGroup extends Group {
    private final Alignment childAlignment;
    private final boolean resizable;
    private final List<Alignment> alignments = new ArrayList<Alignment>();

    ParallelGroup(Alignment childAlignment, boolean resizable) {
      this.childAlignment = childAlignment;
      this.resizable = resizable;
    }

    public ParallelGroup addGroup(Group group) { return (ParallelGroup) super.addGroup(group); }

    public ParallelGroup addComponent(Component component) { return (ParallelGroup) super.addComponent(component); }

    public ParallelGroup addComponent(Component component, int min, int pref, int max) {
      return (ParallelGroup) super.addComponent(component, min, pref, max);
    }

    public ParallelGroup addGap(int pref) { return (ParallelGroup) super.addGap(pref); }

    public ParallelGroup addGap(int min, int pref, int max) { return (ParallelGroup) super.addGap(min, pref, max); }

    public ParallelGroup addGroup(Alignment alignment, Group group) {
      checkChildAlignment(alignment);
      super.addGroup(group);
      alignments.set(alignments.size() - 1, alignment);
      return this;
    }

    public ParallelGroup addComponent(Component component, Alignment alignment) {
      return addComponent(component, alignment, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public ParallelGroup addComponent(Component component, Alignment alignment, int min, int pref, int max) {
      checkChildAlignment(alignment);
      super.addComponent(component, min, pref, max);
      alignments.set(alignments.size() - 1, alignment);
      return this;
    }

    Group addSpring(Spring s) {
      springs.add(s);
      alignments.add(null);
      return this;
    }

    private void checkChildAlignment(Alignment alignment) {
      if (alignment == null) {
        throw new IllegalArgumentException("Alignment must be non-null");
      }
      if (alignment == Alignment.BASELINE && childAlignment != Alignment.BASELINE) {
        throw new IllegalArgumentException("Alignment must be one of:LEADING, TRAILING or CENTER");
      }
    }

    int size(int axis, int kind) {
      int result = 0;
      boolean any = false;
      for (Spring s : springs) {
        int v;
        if (s instanceof GapSpring) {
          GapSpring g = (GapSpring) s;
          v = g.gapSize(axis, kind, 0);
          if (kind == kMax && (g.pref == PREFERRED_SIZE || g.max == PREFERRED_SIZE)) {
            v = g.gapSize(axis, kPref, 0);
          }
        } else {
          v = s.size(axis, kind);
        }
        any = true;
        if (kind == kMax && !resizable) {
          v = s instanceof GapSpring ? v : s.size(axis, kPref);
        }
        result = Math.max(result, v);
      }
      return any ? result : 0;
    }

    void place(int axis, int origin, int size) {
      for (int i = 0; i < springs.size(); i++) {
        Spring s = springs.get(i);
        Alignment a = alignments.get(i) != null ? alignments.get(i) : childAlignment;
        int max = s instanceof GapSpring ? size : s.size(axis, kMax);
        int min = s.size(axis, kMin);
        int sz = size;
        if (sz > max && resizable) {
          sz = max;
        }
        if (!resizable) {
          sz = Math.min(sz, s.size(axis, kPref));
        }
        if (sz < min) {
          sz = min;
        }
        int pos = origin;
        switch (a) {
          case TRAILING:
            pos = origin + size - sz;
            break;
          case CENTER:
            pos = origin + (size - sz) / 2;
            break;
          default:
            break;
        }
        s.place(axis, pos, sz);
      }
    }

    void leading(int axis, List<Component> out) {
      for (Spring s : springs) {
        if (!s.isGap()) {
          s.leading(axis, out);
        }
      }
    }

    void trailing(int axis, List<Component> out) {
      for (Spring s : springs) {
        if (!s.isGap()) {
          s.trailing(axis, out);
        }
      }
    }

    boolean visible() {
      for (Spring s : springs) {
        if (!s.isGap() && s.visible()) {
          return true;
        }
      }
      return springs.isEmpty();
    }
  }
}
