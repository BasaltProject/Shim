/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;

public class PopupFactory {
  private static PopupFactory sharedFactory = new PopupFactory();

  public PopupFactory() { }

  public static void setSharedInstance(PopupFactory factory) {
    if (factory == null) {
      throw new IllegalArgumentException("PopupFactory can not be null");
    }
    sharedFactory = factory;
  }

  public static PopupFactory getSharedInstance() { return sharedFactory; }

  public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
    if (contents == null) {
      throw new IllegalArgumentException("Popup.getPopup must be passed non-null contents");
    }
    return getPopup(owner, contents, x, y, false);
  }

  protected Popup getPopup(Component owner, Component contents, int x, int y, boolean isHeavyWeightPopup)
      throws IllegalArgumentException {
    return new Popup(owner, contents, x, y);
  }
}
