/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface Name extends java.lang.CharSequence {
  boolean contentEquals(java.lang.CharSequence charSequence);
  boolean equals(java.lang.Object object);
  int hashCode();
}
