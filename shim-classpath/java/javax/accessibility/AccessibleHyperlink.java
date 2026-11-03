/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public abstract class AccessibleHyperlink implements javax.accessibility.AccessibleAction {
  protected AccessibleHyperlink() {
  }
  public abstract boolean isValid();
  public abstract int getEndIndex();
  public abstract java.lang.Object getAccessibleActionObject(int p0);
  public abstract java.lang.Object getAccessibleActionAnchor(int p0);
  public abstract int getStartIndex();
  public abstract int getAccessibleActionCount();
  public abstract java.lang.String getAccessibleActionDescription(int p0);
  public abstract boolean doAccessibleAction(int p0);
}
