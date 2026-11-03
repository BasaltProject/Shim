/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;

public class Popup {
  private Component contents;
  private boolean showing;

  protected Popup(Component owner, Component contents, int x, int y) { reset(owner, contents, x, y); }

  protected Popup() { }

  public void show() { showing = true; }

  public void hide() { showing = false; }

  void reset(Component owner, Component contents, int x, int y) {
    this.contents = contents;
    if (contents != null) {
      contents.setLocation(x, y);
    }
  }
}
