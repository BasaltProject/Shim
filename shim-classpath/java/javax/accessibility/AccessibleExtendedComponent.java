/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleExtendedComponent extends javax.accessibility.AccessibleComponent {
  java.lang.String getTitledBorderText();
  javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding();
  java.lang.String getToolTipText();
}
