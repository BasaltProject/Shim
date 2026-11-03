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
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.FocusTraversalPolicy;

public class DefaultFocusManager extends FocusManager {
  final FocusTraversalPolicy gluePolicy = new DefaultFocusTraversalPolicy();

  public DefaultFocusManager() {
    setDefaultFocusTraversalPolicy(gluePolicy);
  }

  public Component getComponentAfter(Container aContainer, Component aComponent) {
    Container root = aContainer.isFocusCycleRoot() ? aContainer : aContainer.getFocusCycleRootAncestor();
    if (root != null) {
      FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
      Component comp = policy.getComponentAfter(root, aComponent);
      if (comp != null) {
        return comp;
      }
      return policy.getDefaultComponent(root);
    }
    return null;
  }

  public Component getComponentBefore(Container aContainer, Component aComponent) {
    Container root = aContainer.isFocusCycleRoot() ? aContainer : aContainer.getFocusCycleRootAncestor();
    if (root != null) {
      FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
      Component comp = policy.getComponentBefore(root, aComponent);
      if (comp != null) {
        return comp;
      }
      return policy.getLastComponent(root);
    }
    return null;
  }

  public Component getFirstComponent(Container aContainer) {
    Container root = aContainer.isFocusCycleRoot() ? aContainer : aContainer.getFocusCycleRootAncestor();
    if (root != null) {
      return root.getFocusTraversalPolicy().getFirstComponent(root);
    }
    return null;
  }

  public Component getLastComponent(Container aContainer) {
    Container root = aContainer.isFocusCycleRoot() ? aContainer : aContainer.getFocusCycleRootAncestor();
    if (root != null) {
      return root.getFocusTraversalPolicy().getLastComponent(root);
    }
    return null;
  }

  public boolean compareTabOrder(Component a, Component b) { return (a.getY() < b.getY()) || (a.getX() < b.getX()); }
}
