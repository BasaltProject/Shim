/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public abstract class AccessibilityProvider {
  protected AccessibilityProvider() {
  }
  public abstract java.lang.String getName();
  public abstract void activate();
}
