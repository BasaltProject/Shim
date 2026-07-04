/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContainerOrderFocusTraversalPolicy extends FocusTraversalPolicy implements Serializable {
  private boolean implicitDownCycleTraversal = true;

  public ContainerOrderFocusTraversalPolicy() { }

  protected boolean accept(Component aComponent) {
    if (!(aComponent.isVisible() && aComponent.isDisplayable() && aComponent.isEnabled())) {
      return false;
    }
    if (!(aComponent.isFocusable() && aComponent.isFocusTraversable())) {
      return false;
    }
    return true;
  }

  private void collect(Container root, Container c, List<Component> out) {
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component child = c.getComponent(i);
      if (accept(child)) {
        out.add(child);
      }
      if (child instanceof Container) {
        Container cc = (Container) child;
        if (!cc.isFocusCycleRoot() && !cc.isFocusTraversalPolicyProvider() && cc.isVisible()) {
          collect(root, cc, out);
        }
      }
    }
  }

  private List<Component> order(Container root) {
    List<Component> out = new ArrayList<Component>();
    if (root.isVisible() && root.isDisplayable() && root.isEnabled()) {
      collect(root, root, out);
    }
    return out;
  }

  public Component getDefaultComponent(Container aContainer) { return getFirstComponent(aContainer); }

  public Component getComponentAfter(Container aContainer, Component aComponent) {
    if (aContainer == null || aComponent == null) {
      throw new IllegalArgumentException("aContainer and aComponent cannot be null");
    }
    List<Component> list = order(aContainer);
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
    List<Component> list = order(aContainer);
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
    List<Component> list = order(aContainer);
    return list.isEmpty() ? null : list.get(0);
  }

  public Component getLastComponent(Container aContainer) {
    if (aContainer == null) {
      throw new IllegalArgumentException("aContainer cannot be null");
    }
    List<Component> list = order(aContainer);
    return list.isEmpty() ? null : list.get(list.size() - 1);
  }

  public boolean getImplicitDownCycleTraversal() { return implicitDownCycleTraversal; }

  public void setImplicitDownCycleTraversal(boolean implicitDownCycleTraversal) {
    this.implicitDownCycleTraversal = implicitDownCycleTraversal;
  }
}
