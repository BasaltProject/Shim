/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleValue {
  java.lang.Number getCurrentAccessibleValue();
  boolean setCurrentAccessibleValue(java.lang.Number p0);
  java.lang.Number getMinimumAccessibleValue();
  java.lang.Number getMaximumAccessibleValue();
}
