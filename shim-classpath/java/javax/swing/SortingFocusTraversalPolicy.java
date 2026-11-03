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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortingFocusTraversalPolicy extends InternalFrameFocusTraversalPolicy {
  private Comparator<? super Component> comparator;
  private boolean implicitDownCycleTraversal = true;

  protected SortingFocusTraversalPolicy() { }

  public SortingFocusTraversalPolicy(Comparator<? super Component> comparator) { this.comparator = comparator; }

  private List<Component> cycle(Container root) {
    List<Component> out = new ArrayList<Component>();
    collect(root, out);
    if (comparator != null) {
      Collections.sort(out, comparator);
    }
    return out;
  }

  private void collect(Container c, List<Component> out) {
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component child = c.getComponent(i);
      if (accept(child)) {
        out.add(child);
      }
      if (child instanceof Container) {
        Container cc = (Container) child;
        if (!cc.isFocusCycleRoot() && !cc.isFocusTraversalPolicyProvider()) {
          collect(cc, out);
        }
      }
    }
  }

  protected boolean accept(Component aComponent) {
    if (!(aComponent.isVisible() && aComponent.isDisplayable() && aComponent.isEnabled())) {
      return false;
    }
    return aComponent.isFocusable();
  }

  protected Comparator<? super Component> getComparator() { return comparator; }

  protected void setComparator(Comparator<? super Component> comparator) { this.comparator = comparator; }

  public Component getDefaultComponent(Container aContainer) { return getFirstComponent(aContainer); }

  public Component getComponentAfter(Container aContainer, Component aComponent) {
    if (aContainer == null || aComponent == null) {
      throw new IllegalArgumentException("aContainer and aComponent cannot be null");
    }
    List<Component> list = cycle(aContainer);
    int i = list.indexOf(aComponent);
    if (i >= 0 && i + 1 < list.size()) {
      return list.get(i + 1);
    }
    return list.isEmpty() ? null : list.get(0);
  }

  public Component getComponentBefore(Container aContainer, Component aComponent) {
    if (aContainer == null || aComponent == null) {
      throw new IllegalArgumentException("aContainer and aComponent cannot be null");
    }
    List<Component> list = cycle(aContainer);
    int i = list.indexOf(aComponent);
    if (i > 0) {
      return list.get(i - 1);
    }
    return list.isEmpty() ? null : list.get(list.size() - 1);
  }

  public Component getFirstComponent(Container aContainer) {
    if (aContainer == null) {
      throw new IllegalArgumentException("aContainer cannot be null");
    }
    List<Component> list = cycle(aContainer);
    return list.isEmpty() ? null : list.get(0);
  }

  public Component getLastComponent(Container aContainer) {
    if (aContainer == null) {
      throw new IllegalArgumentException("aContainer cannot be null");
    }
    List<Component> list = cycle(aContainer);
    return list.isEmpty() ? null : list.get(list.size() - 1);
  }

  public void setImplicitDownCycleTraversal(boolean implicitDownCycleTraversal) {
    this.implicitDownCycleTraversal = implicitDownCycleTraversal;
  }

  public boolean getImplicitDownCycleTraversal() { return implicitDownCycleTraversal; }
}
