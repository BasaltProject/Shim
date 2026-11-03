/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class FocusTraversalPolicy {
  protected FocusTraversalPolicy() {
  }
  public abstract java.awt.Component getDefaultComponent(java.awt.Container p0);
  public abstract java.awt.Component getComponentAfter(java.awt.Container p0, java.awt.Component p1);
  public abstract java.awt.Component getComponentBefore(java.awt.Container p0, java.awt.Component p1);
  public abstract java.awt.Component getFirstComponent(java.awt.Container p0);
  public abstract java.awt.Component getLastComponent(java.awt.Container p0);
  public java.awt.Component getInitialComponent(java.awt.Window window) {
    if (window == null) {
      throw new IllegalArgumentException("window cannot be equal to null.");
    }
    java.awt.Component def = getDefaultComponent(window);
    if (def == null && window.isFocusableWindow()) {
      def = window;
    }
    return def;
  }
}
