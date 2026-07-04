/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class DefaultFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
  public DefaultFocusTraversalPolicy() { }

  protected boolean accept(Component aComponent) {
    if (!(aComponent.isVisible() && aComponent.isDisplayable() && aComponent.isEnabled())) {
      return false;
    }
    if (!aComponent.isFocusable()) {
      return false;
    }
    return aComponent.isFocusTraversable();
  }
}
